package oopproject;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class Inventory extends JFrame implements ActionListener {

    private final Color NORMAL = Color.WHITE;
    private final Color SELECTED = new Color(255, 190, 80);

    private ImageIcon backg1 = new ImageIcon("C:\\Users\\ryce2\\Downloads\\OOP_PICTURES\\inventory.png");
    ImageIcon icon = new ImageIcon("C:\\Users\\ryce2\\Downloads\\OOP_PICTURES\\image-removebg-preview.png");

    private JLabel bg1 = new JLabel(backg1);

    JButton dashboard = new JButton("Dashboard");
    JButton room = new JButton("Rooms");
    JButton inventory = new JButton("Inventory");
    JButton transaction = new JButton("Transaction History");
    JButton employee = new JButton("Employee");

    JButton allFood = new JButton("All Food");
    JButton allDrinks = new JButton("All Drinks");
    JButton sortStock = new JButton("Sort Stock");
    JButton pingAdmin = new JButton("Ping Admin");

    DefaultTableModel model = new DefaultTableModel(new Object[]{"Item", "Category", "Stock"}, 0) {
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    };

    JTable table = new JTable(model);
    JLabel selected = new JLabel("");

    String category = "Food";
    boolean ascending = false;

    private void resetSideButtons() {
        dashboard.setBackground(NORMAL);
        room.setBackground(NORMAL);
        inventory.setBackground(NORMAL);
        transaction.setBackground(NORMAL);
        employee.setBackground(NORMAL);
    }

    public Inventory() {

        resetSideButtons();
        inventory.setBackground(SELECTED);

        JButton[] buttons = {dashboard, room, inventory, transaction, employee};

        for (JButton button : buttons) {
            button.setOpaque(true);
            button.setFocusPainted(false);
            button.addActionListener(this);
            add(button);
        }

        dashboard.setBounds(9, 216, 190, 55);
        room.setBounds(9, 307, 190, 55);
        inventory.setBounds(9, 398, 190, 55);
        transaction.setBounds(9, 488, 190, 55);
        employee.setBounds(9, 578, 190, 55);

        JLabel title = new JLabel("INVENTORY");
        title.setFont(new Font("Arial", Font.BOLD, 26));
        title.setForeground(new Color(72, 43, 25));
        title.setBounds(270, 68, 300, 40);
        add(title);

        JLabel description = new JLabel("Available stocks for food and drinks");
        description.setFont(new Font("Arial", Font.PLAIN, 15));
        description.setForeground(new Color(72, 43, 25));
        description.setBounds(270, 109, 400, 25);
        add(description);

        allFood.setBounds(768, 136, 136, 40);
        allFood.setFocusPainted(false);
        allFood.setBackground(NORMAL);
        allFood.addActionListener(this);
        add(allFood);

        allDrinks.setBounds(911, 136, 136, 40);
        allDrinks.setFocusPainted(false);
        allDrinks.setBackground(NORMAL);
        allDrinks.addActionListener(this);
        add(allDrinks);

        sortStock.setBounds(1054, 136, 174, 40);
        sortStock.setFocusPainted(false);
        sortStock.setBackground(NORMAL);
        sortStock.addActionListener(this);
        add(sortStock);

        pingAdmin.setBounds(1000, 585, 188, 45);
        pingAdmin.setBackground(new Color(80, 125, 63));
        pingAdmin.setForeground(Color.WHITE);
        pingAdmin.setOpaque(true);
        pingAdmin.setFocusPainted(false);
        pingAdmin.addActionListener(this);
        add(pingAdmin);

        table.setRowHeight(32);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setSelectionBackground(SELECTED);
        table.setSelectionForeground(Color.BLACK);
        table.getTableHeader().setReorderingAllowed(false);
        table.getColumnModel().getColumn(0).setPreferredWidth(300);
        table.getColumnModel().getColumn(1).setPreferredWidth(130);
        table.getColumnModel().getColumn(2).setPreferredWidth(140);
//        table.getColumnModel().getColumn(3).setPreferredWidth(180);
        table.getSelectionModel().addListSelectionListener(e -> {

            int row = table.getSelectedRow();

            if (row >= 0) {
                selected.setText("Selected: " + table.getValueAt(row, 0));
                selected.setVisible(true);
                pingAdmin.setVisible(true);
            } else {
                selected.setVisible(false);
                pingAdmin.setVisible(false);
            }

        });

        allFood.addActionListener(e -> {
            category = "Food";
            showItems();
        });

        allDrinks.addActionListener(e -> {
            category = "Drink";
            showItems();
        });

        sortStock.addActionListener(e -> {
            ascending = !ascending;
            showItems();
        });

        pingAdmin.addActionListener(e -> {

            int row = table.getSelectedRow();

            if (row >= 0) {

                String item = table.getValueAt(row, 0).toString();
                String stock = table.getValueAt(row, 2).toString();

                try {

                    AdminData.sendPing("Inventory: " + item
                            + " (stock " + stock + ") from "
                            + EmployeeSession.getUsername());

                    JOptionPane.showMessageDialog(this,
                            "Notification sent to admin.");

                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage());
                }
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBounds(265, 190, 958, 375);
        add(scroll);

        JLabel selectedItem = new JLabel("  Selected Item: None");
        selectedItem.setBounds(270, 585, 580, 42);
        selectedItem.setFont(new Font("Arial", Font.BOLD, 16));
        selectedItem.setForeground(new Color(72, 43, 25));
        selectedItem.setOpaque(true);
        selectedItem.setBackground(Color.WHITE);
        selectedItem.setBorder(BorderFactory.createLineBorder(new Color(185, 185, 185)));
        selectedItem.setVisible(true);
        add(selectedItem);

        add(bg1);
        setIconImage(icon.getImage());
        setTitle("Traveler's Rest");
        setSize(1290, 758);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
        setResizable(false);
        showItems();
    }

    public void showItems() {

        model.setRowCount(0);

        try {

            Map<String, Integer> stocks = AdminData.getStocks();
            ArrayList<AdminData.Item> items = AdminData.getItems();

            for (int i = 0; i < items.size(); i++) {

                for (int j = i + 1; j < items.size(); j++) {

                    int stock1 = stocks.getOrDefault(items.get(i).name, 0);
                    int stock2 = stocks.getOrDefault(items.get(j).name, 0);

                    if ((ascending && stock1 > stock2)
                            || (!ascending && stock1 < stock2)) {

                        AdminData.Item temp = items.get(i);
                        items.set(i, items.get(j));
                        items.set(j, temp);
                    }
                }
            }

            for (int i = 0; i < items.size(); i++) {

                AdminData.Item item = items.get(i);

                if (item.category.equals(category)) {

                    model.addRow(new Object[]{
                        item.name,
                        item.category,
                        stocks.getOrDefault(item.name, 0)
                    });
                }
            }

            table.clearSelection();
            selected.setVisible(false);
            pingAdmin.setVisible(false);

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    public static synchronized int getStock(String name) {

        try {
            return AdminData.getStocks().getOrDefault(name, -1);
        } catch (IOException ex) {
            return -1;
        }
    }

    public static synchronized String checkStock(javax.swing.table.TableModel cart) {
        java.util.Map<String, Integer> items = new java.util.HashMap<>();
        try {
            for (int i = 0; i < cart.getRowCount(); i++) {
                String name = String.valueOf(cart.getValueAt(i, 0));
                if (name.startsWith("Room #")) {
                    continue;
                }

                int quantity = Integer.parseInt(String.valueOf(cart.getValueAt(i, 2)));
                if (quantity <= 0) {
                    return "Invalid quantity for " + name;
                }
                items.put(name, Math.addExact(items.getOrDefault(name, 0), quantity));
            }
            return AdminData.checkStock(items);
        } catch (Exception ex) {
            return "Cannot check stock: " + ex.getMessage();
        }
    }

    public static synchronized void reduceStock(javax.swing.table.TableModel cart) throws IOException {
        java.util.Map<String, Integer> items = new java.util.HashMap<>();
        for (int i = 0; i < cart.getRowCount(); i++) {
            String name = String.valueOf(cart.getValueAt(i, 0));
            if (name.startsWith("Room #")) {
                continue;
            }

            int quantity = Integer.parseInt(String.valueOf(cart.getValueAt(i, 2)));
            if (quantity <= 0) {
                throw new IOException("Invalid quantity for " + name);
            }
            items.put(name, Math.addExact(items.getOrDefault(name, 0), quantity));
        }
        AdminData.removeStock(items);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == dashboard) {
            resetSideButtons();
            dashboard.setBackground(SELECTED);
            new Dashboard();
            dispose();
        }
        if (e.getSource() == room) {
            resetSideButtons();
            room.setBackground(SELECTED);
            new Room();
            dispose();
        }
        if (e.getSource() == transaction) {
            resetSideButtons();
            transaction.setBackground(SELECTED);
            new TransactionHistory();
            dispose();
        }
        if (e.getSource() == inventory) {
            resetSideButtons();
            inventory.setBackground(SELECTED);
            new Inventory();
            dispose();
        }
        if (e.getSource() == employee) {
            resetSideButtons();
            employee.setBackground(SELECTED);
            new Employee();
            dispose();
        }
    }
}
