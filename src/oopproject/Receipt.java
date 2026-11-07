package oopproject;

import java.awt.*;
import java.io.IOException;
import java.math.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import javax.swing.*;
import javax.swing.table.*;

public class Receipt extends JDialog {

    private DefaultTableModel soldItems;
    private JTextField paymentField = new JTextField();
    private JLabel changeAmountLabel = new JLabel("P0.00", SwingConstants.RIGHT);
    private JLabel orderIdLabel = new JLabel("Order not yet saved", SwingConstants.CENTER);
    private BigDecimal grandTotal = BigDecimal.ZERO;
    private Runnable clearCart;
    private static final BigDecimal TAX_RATE = new BigDecimal("0.05");

    public Receipt(Window owner, DefaultTableModel model, Runnable clearCartAction) {
        super(owner, "Receipt", Dialog.ModalityType.APPLICATION_MODAL);
        clearCart = clearCartAction;

        soldItems = new DefaultTableModel(new Object[]{"Item", "Price", "Qty", "Subtotal"}, 0) {
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        DefaultTableModel displayItems = new DefaultTableModel(new Object[]{"Item", "Price", "Qty", "Subtotal"}, 0) {
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        BigDecimal subtotal = BigDecimal.ZERO;
        for (int i = 0; i < model.getRowCount(); i++) {
            String name = String.valueOf(model.getValueAt(i, 0));
            BigDecimal price = new BigDecimal(String.valueOf(model.getValueAt(i, 1)));
            int qty = ((Number) model.getValueAt(i, 2)).intValue();
            BigDecimal amount = price.multiply(BigDecimal.valueOf(qty));

            soldItems.addRow(new Object[]{name, price, qty, amount});
            displayItems.addRow(new Object[]{name, money(price), qty, money(amount)});
            subtotal = subtotal.add(amount);
        }

        subtotal = subtotal.setScale(2, RoundingMode.HALF_UP);
        BigDecimal tax = subtotal.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
        grandTotal = subtotal.add(tax);

        setSize(540, 625);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(owner);
        setResizable(false);

        JPanel page = new JPanel(new BorderLayout(12, 12));
        page.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));
        setContentPane(page);

        JPanel title = new JPanel(new GridLayout(3, 1, 0, 2));
        JLabel heading = new JLabel("RECEIPT", SwingConstants.CENTER);
        heading.setFont(new Font("Arial", Font.BOLD, 23));
        orderIdLabel.setFont(new Font("Arial", Font.BOLD, 16));
        JLabel date = new JLabel(LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy  hh:mm a", Locale.ENGLISH)), SwingConstants.CENTER);
        title.add(heading);
        title.add(orderIdLabel);
        title.add(date);
        page.add(title, BorderLayout.NORTH);

        JTable table = new JTable(displayItems);
        table.setRowHeight(28);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setResizingAllowed(false);
        table.getColumnModel().getColumn(0).setPreferredWidth(180);
        table.getColumnModel().getColumn(1).setPreferredWidth(90);
        table.getColumnModel().getColumn(2).setPreferredWidth(40);
        table.getColumnModel().getColumn(3).setPreferredWidth(120);
        page.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel();
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        bottom.add(amountRow("Subtotal:", money(subtotal)));
        bottom.add(amountRow("Sales Tax (5%):", money(tax)));
        bottom.add(amountRow("TOTAL TO PAY:", money(grandTotal)));
        bottom.add(Box.createVerticalStrut(12));

        JPanel paymentRow = new JPanel(new BorderLayout(10, 0));
        JLabel paymentLabel = new JLabel("Cash Received (P):");
        paymentLabel.setPreferredSize(new Dimension(180, 30));
        paymentField.setFont(new Font("Arial", Font.PLAIN, 16));
        paymentRow.add(paymentLabel, BorderLayout.WEST);
        paymentRow.add(paymentField, BorderLayout.CENTER);
        bottom.add(paymentRow);
        bottom.add(Box.createVerticalStrut(7));

        JPanel changeRow = new JPanel(new BorderLayout(8, 0));
        changeRow.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
        JLabel changeLabel = new JLabel("Change:");
        changeLabel.setFont(new Font("Arial", Font.BOLD, 15));
        changeAmountLabel.setFont(new Font("Arial", Font.BOLD, 16));
        changeRow.add(changeLabel, BorderLayout.WEST);
        changeRow.add(changeAmountLabel, BorderLayout.EAST);
        bottom.add(changeRow);
        bottom.add(Box.createVerticalStrut(14));

        JPanel buttons = new JPanel(new GridLayout(1, 2, 10, 0));
        JButton confirm = new JButton("Proceed / Save Sale");
        JButton cancel = new JButton("Cancel");
        confirm.setBackground(new Color(0, 145, 60));
        confirm.setForeground(Color.WHITE);
        confirm.setFocusPainted(false);
        cancel.setBackground(new Color(190, 45, 45));
        cancel.setForeground(Color.WHITE);
        cancel.setFocusPainted(false);
        buttons.add(confirm);
        buttons.add(cancel);
        bottom.add(buttons);
        page.add(bottom, BorderLayout.SOUTH);

        paymentField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                updateChange();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                updateChange();
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                updateChange();
            }
        });

        confirm.addActionListener(e -> proceed());
        cancel.addActionListener(e -> dispose());
        getRootPane().setDefaultButton(confirm);
    }

    private JPanel amountRow(String name, String amount) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
        JLabel label = new JLabel(name);
        JLabel value = new JLabel(amount, SwingConstants.RIGHT);
        label.setFont(new Font("Arial", Font.BOLD, 15));
        value.setFont(new Font("Arial", Font.BOLD, 15));
        row.add(label, BorderLayout.WEST);
        row.add(value, BorderLayout.EAST);
        return row;
    }

    private BigDecimal paymentAmount() {
        BigDecimal amount = new BigDecimal(paymentField.getText().trim());
        if (amount.signum() < 0 || amount.scale() > 2) {
            throw new NumberFormatException();
        }
        return amount;
    }

    private void updateChange() {
        try {
            BigDecimal change = paymentAmount().subtract(grandTotal);
            changeAmountLabel.setText(change.signum() < 0 ? "Insufficient payment" : money(change));
        } catch (NumberFormatException ex) {
            changeAmountLabel.setText("Enter valid amount");
        }
    }

    private void proceed() {
        if (soldItems.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Cart is empty.");
            return;
        }

        BigDecimal paid;
        try {
            paid = paymentAmount();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Enter a valid cash amount (up to 2 decimal places).");
            paymentField.requestFocusInWindow();
            return;
        }

        if (paid.compareTo(grandTotal) < 0) {
            JOptionPane.showMessageDialog(this, "Cash received is less than the total.");
            paymentField.requestFocusInWindow();
            return;
        }

        String stockError = Inventory.checkStock(soldItems);
        if (stockError != null) {
            JOptionPane.showMessageDialog(this, stockError);
            return;
        }

        try {
            String orderId = RecentTransaction.recordSale(grandTotal);
            TransactionSorting.savePaymentDetails(orderId, paid, paid.subtract(grandTotal));
            TransactionSorting.saveOrderItems(orderId, soldItems);
            Inventory.reduceStock(soldItems);
            orderIdLabel.setText("Order ID: " + orderId);
            JOptionPane.showMessageDialog(this, "Order " + orderId + " saved!\nChange: " + money(paid.subtract(grandTotal)));
            clearCart.run();
            dispose();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error saving sale: " + ex.getMessage()
                    + "\nCheck transaction and inventory files before retrying.");
        }
    }

    private static String money(BigDecimal amount) {
        return "P" + String.format(Locale.US, "%,.2f", amount);
    }
}
