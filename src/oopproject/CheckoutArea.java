package oopproject;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.table.DefaultTableModel;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

public class CheckoutArea extends JPanel {

    private final DefaultTableModel model;
    private final JTable table;
    private final JLabel totalLabel = new JLabel("₱0.00");
    private final JLabel statusLabel = new JLabel("Cart is empty");
    private double total = 0;

    public CheckoutArea() {
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel header = new JPanel(new BorderLayout(5, 5));
        JLabel title = new JLabel("Checkout Area");
        title.setFont(new Font("Arial", Font.BOLD, 18));
        JButton clearButton = new JButton("Clear All");
        header.add(title, BorderLayout.WEST);
        header.add(clearButton, BorderLayout.EAST);
        JPanel top = new JPanel(new BorderLayout(5, 5));
        top.add(header, BorderLayout.NORTH);
        top.add(statusLabel, BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);

        model = new DefaultTableModel(new Object[]{"Item", "Price", "Qty", "Subtotal"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        table.setRowHeight(30);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setResizingAllowed(false);
        table.getColumnModel().getColumn(0).setPreferredWidth(180);
        table.getColumnModel().getColumn(1).setPreferredWidth(90);
        table.getColumnModel().getColumn(2).setPreferredWidth(50);
        table.getColumnModel().getColumn(3).setPreferredWidth(90);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout(5, 5));
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton minusButton = new JButton("-");
        JButton plusButton = new JButton("+");
        JButton removeButton = new JButton("Remove");
        controls.add(minusButton);
        controls.add(plusButton);
        controls.add(removeButton);
        bottom.add(controls, BorderLayout.NORTH);

        JPanel subtotalPanel = new JPanel(new GridLayout(1, 2, 5, 5));
        subtotalPanel.add(new JLabel("Subtotal:"));
        subtotalPanel.add(totalLabel);
        bottom.add(subtotalPanel, BorderLayout.CENTER);

        JButton checkoutButton = new JButton("Checkout");
        checkoutButton.setBackground(new Color(0, 150, 0));
        checkoutButton.setForeground(Color.WHITE);
        checkoutButton.setOpaque(true);
        checkoutButton.setFocusPainted(false);
        bottom.add(checkoutButton, BorderLayout.SOUTH);
        add(bottom, BorderLayout.SOUTH);

        plusButton.addActionListener(e -> changeQuantity(1));
        minusButton.addActionListener(e -> changeQuantity(-1));
        removeButton.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Select an item first!");
                return;
            }
            model.removeRow(table.convertRowIndexToModel(row));
            updateTotal();
        });
        clearButton.addActionListener(e -> clearCart());

        checkoutButton.addActionListener(e -> {
            if (model.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "Cart is empty!");
                return;
            }
            Receipt receipt = new Receipt(SwingUtilities.getWindowAncestor(this), model, () -> {
                for (int i = 0; i < model.getRowCount(); i++) {
                    String item = String.valueOf(model.getValueAt(i, 0));
                    if (!item.startsWith("Room #") || !item.contains(" | ")) {
                        continue;
                    }

                    try {
                        String[] parts = item.split(" \\| ", 3);
                        int roomNumber = Integer.parseInt(parts[0].substring(6).trim());
                        String tenantName = parts[1];
                        AdminData.changeRoom(roomNumber, "Occupied", tenantName);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this,
                                "Payment completed, but room status could not be updated: " + ex.getMessage());
                    }
                }
                clearCart();
            });
            receipt.setVisible(true);
        });
    }

    public boolean hasRoomInCart(int roomNumber) {
        for (int i = 0; i < model.getRowCount(); i++) {
            String name = String.valueOf(model.getValueAt(i, 0));
            if (name.startsWith("Room #" + roomNumber + " | ")) {
                return true;
            }
        }
        return false;
    }

    // Called by Menu.java when the user clicks Add.
    public void addItem(String name, double price) {
        for (int row = 0; row < model.getRowCount(); row++) {
            if (model.getValueAt(row, 0).equals(name)) {
                int newQty = (Integer) model.getValueAt(row, 2) + 1;
                model.setValueAt(newQty, row, 2);
                model.setValueAt(price * newQty, row, 3);
                updateTotal();
                return;
            }
        }
        model.addRow(new Object[]{name, price, 1, price});
        updateTotal();
    }

    // Used by the receipt after Proceed or Cancel.
    public void clearCart() {
        model.setRowCount(0);
        updateTotal();
    }

    private void changeQuantity(int amount) {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select an item first!");
            return;
        }
        row = table.convertRowIndexToModel(row);
        int qty = (Integer) model.getValueAt(row, 2) + amount;
        double price = ((Number) model.getValueAt(row, 1)).doubleValue();
        if (qty <= 0) {
            model.removeRow(row);
        } else {
            model.setValueAt(qty, row, 2);
            model.setValueAt(price * qty, row, 3);
        }
        updateTotal();
    }

    private void updateTotal() {
        total = 0;
        int totalItems = 0;
        for (int row = 0; row < model.getRowCount(); row++) {
            total += ((Number) model.getValueAt(row, 3)).doubleValue();
            totalItems += ((Number) model.getValueAt(row, 2)).intValue();
        }
        totalLabel.setText(String.format(Locale.US, "₱%,.2f", total));
        statusLabel.setText(totalItems == 0 ? "Cart is empty" : totalItems + " item(s) in cart");
    }
}
