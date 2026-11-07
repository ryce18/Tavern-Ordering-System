package oopproject;

import java.awt.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class Admin extends JFrame {

    private JTabbedPane tabs = new JTabbedPane();
    private JLabel totalMoney = new JLabel(), todayOrders = new JLabel(), tenantCount = new JLabel(), allOrders = new JLabel();
    private DefaultTableModel overviewRooms = new DefaultTableModel(new String[]{"Room", "Status", "Tenant"}, 0);
    private DefaultTableModel menuModel = new DefaultTableModel(new String[]{"Name", "Price", "Stock"}, 0);
    private DefaultTableModel stockModel = new DefaultTableModel(new String[]{"Item", "Stock"}, 0);
    private DefaultTableModel roomModel = new DefaultTableModel(new String[]{"Room", "Status", "Tenant"}, 0);
    private DefaultTableModel salesModel = new DefaultTableModel(new String[]{"Order ID", "Date & Time", "Total", "Paid", "Change", "Staff Account"}, 0);
    private JTable menuTable = new JTable(menuModel), roomTable = new JTable(roomModel), salesTable = new JTable(salesModel);
    private JTextArea notifications = new JTextArea(), accounts = new JTextArea();
    private JLabel notificationCount = new JLabel("Notifications");
    private int seenPingCount = 0;

    public Admin() {
        if (!EmployeeSession.getRole().equalsIgnoreCase("admin")) {
            JOptionPane.showMessageDialog(null, "Only an admin account can open this page.");
            return;
        }
        setTitle("Admin Panel - " + EmployeeSession.getUsername());
        setSize(1250, 800);
        setMinimumSize(new Dimension(990, 650));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        JPanel root = new JPanel(new BorderLayout(0, 5));
        root.setBackground(new Color(250, 244, 231));
        setContentPane(root);
        JPanel heading = new JPanel(new BorderLayout());
        heading.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
        heading.setBackground(new Color(70, 43, 23));
        JLabel title = new JLabel("ADMIN DASHBOARD   |   " + EmployeeSession.getUsername());
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setForeground(Color.WHITE);
        JButton logout = new JButton("Log Out");
        logout.setBackground(new Color(255, 190, 80));
        logout.addActionListener(e -> {
            EmployeeSession.logout();
            dispose();
            new LogIn();
        });
        heading.add(title, BorderLayout.WEST);
        heading.add(logout, BorderLayout.EAST);
        root.add(heading, BorderLayout.NORTH);
        tabs.setFont(new Font("Arial", Font.BOLD, 14));
        root.add(tabs, BorderLayout.CENTER);
        tabs.addTab("Overview", createOverview());
        tabs.addTab("Food & Drinks", createMenu());
        tabs.addTab("Inventory", createInventory());
        tabs.addTab("Notifications", createNotifications());
        tabs.addTab("Rooms & Tenants", createRooms());
        tabs.addTab("Staff Accounts", createAccounts());
        tabs.addTab("Transactions", createSales());
        tabs.addChangeListener(e -> refresh());
        javax.swing.Timer timer = new javax.swing.Timer(12000, e -> {
            if (!isDisplayable()) {
                ((javax.swing.Timer) e.getSource()).stop();
                return;
            }
            refreshPings();
        });
        timer.start();
        refresh();
        setVisible(true);
    }

    private JPanel page() {
        JPanel p = new JPanel(new BorderLayout(12, 12));
        p.setBackground(new Color(250, 244, 231));
        p.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));
        return p;
    }

    private JLabel bigText(String text) {
        JLabel result = new JLabel(text, SwingConstants.CENTER);
        result.setFont(new Font("Arial", Font.BOLD, 25));
        return result;
    }

    private JPanel metric(String heading, JLabel value) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(220, 206, 184)),
                BorderFactory.createEmptyBorder(15, 5, 15, 5)));
        JLabel label = new JLabel(heading, SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 15));
        p.add(label, BorderLayout.NORTH);
        p.add(value, BorderLayout.CENTER);
        return p;
    }

    private JPanel topBar(String title, String button, Runnable action) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        JLabel label = new JLabel(title);
        label.setFont(new Font("Arial", Font.BOLD, 22));
        panel.add(label, BorderLayout.WEST);
        JButton refresh = new JButton(button);
        refresh.addActionListener(e -> action.run());
        panel.add(refresh, BorderLayout.EAST);
        return panel;
    }

    private JPanel createOverview() {
        JPanel page = page();
        JPanel cards = new JPanel(new GridLayout(1, 4, 13, 13));
        cards.setOpaque(false);
        cards.add(metric("Total Earnings", totalMoney));
        cards.add(metric("Orders Today", todayOrders));
        cards.add(metric("Total Orders", allOrders));
        cards.add(metric("Occupied / Overdue Rooms", tenantCount));
        JPanel top = new JPanel(new BorderLayout(0, 16));
        top.setOpaque(false);
        top.add(topBar("Company Overview", "Refresh", this::refresh), BorderLayout.NORTH);
        top.add(cards, BorderLayout.CENTER);
        top.setPreferredSize(new Dimension(0, 180));
        page.add(top, BorderLayout.NORTH);
        JPanel middle = new JPanel(new BorderLayout(4, 10));
        middle.setOpaque(false);
        JLabel text = new JLabel("Current Tenants and Room Status");
        text.setFont(new Font("Arial", Font.BOLD, 18));
        middle.add(text, BorderLayout.NORTH);
        JTable table = new JTable(overviewRooms);
        table.setRowHeight(30);
        middle.add(new JScrollPane(table), BorderLayout.CENTER);
        page.add(middle, BorderLayout.CENTER);
        return page;
    }

    private JPanel createMenu() {
        JPanel p = page();
        p.add(topBar("Manage Food and Beverages", "Refresh", this::refreshMenu), BorderLayout.NORTH);
        menuTable.setRowHeight(32);
        menuTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        p.add(new JScrollPane(menuTable), BorderLayout.CENTER);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 6));
        JButton addFood = new JButton("Add Food");
        JButton addDrink = new JButton("Add Drink");
        addFood.setBackground(new Color(255, 190, 80));
        addDrink.setBackground(new Color(255, 190, 80));
        addFood.addActionListener(e -> addMenuItem("Food"));
        addDrink.addActionListener(e -> addMenuItem("Drink"));
        actions.add(addFood);
        actions.add(addDrink);
        p.add(actions, BorderLayout.SOUTH);
        return p;
    }

    private void addMenuItem(String type) {
        JTextField name = new JTextField();
        JTextField price = new JTextField();
        JTextField quantity = new JTextField("0");
        JPanel form = new JPanel(new GridLayout(6, 1, 4, 4));
        form.add(new JLabel("Item Name:"));
        form.add(name);
        form.add(new JLabel("Price (P):"));
        form.add(price);
        form.add(new JLabel("Starting Stock:"));
        form.add(quantity);
        if (JOptionPane.showConfirmDialog(this, form, "Add " + type, JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
            return;
        }
        try {
            AdminData.addItem(name.getText(), type, new BigDecimal(price.getText().trim()),
                    Integer.parseInt(quantity.getText().trim()));
            refreshMenu();
            refreshInventory();
            JOptionPane.showMessageDialog(this, type + " added to staff menu and inventory.");
        } catch (Exception ex) {
            error(ex);
        }
    }

    private JPanel createInventory() {
        JPanel p = page();
        p.add(topBar("Restock Available Items", "Refresh", this::refreshInventory), BorderLayout.NORTH);
        JTable stockTable = new JTable(stockModel);
        stockTable.setRowHeight(32);
        stockTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        p.add(new JScrollPane(stockTable), BorderLayout.CENTER);
        JButton restock = new JButton("Restock Selected Item");
        restock.setBackground(new Color(255, 190, 80));
        restock.addActionListener(e -> {
            int row = stockTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Select an item first.");
                return;
            }
            String name = String.valueOf(stockTable.getValueAt(row, 0));
            String qty = JOptionPane.showInputDialog(this, "Add how many stocks for " + name + "?");
            if (qty == null) {
                return;
            }
            try {
                AdminData.restock(name, Integer.parseInt(qty.trim()));
                refreshInventory();
                refreshMenu();
                JOptionPane.showMessageDialog(this, "Stock updated.");
            } catch (Exception ex) {
                error(ex);
            }
        });
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(restock);
        p.add(buttons, BorderLayout.SOUTH);
        return p;
    }

    private JPanel createNotifications() {
        JPanel p = page();
        JPanel head = topBar("Admin Notifications", "Refresh", this::refreshPings);
        p.add(head, BorderLayout.NORTH);
        notifications.setEditable(false);
        notifications.setFont(new Font("Monospaced", Font.PLAIN, 13));
        notifications.setMargin(new Insets(12, 12, 12, 12));
        p.add(new JScrollPane(notifications), BorderLayout.CENTER);
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(notificationCount, BorderLayout.WEST);
        JButton clear = new JButton("Clear Notifications");
        clear.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(this, "Clear all saved admin notifications?", "Confirm", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                return;
            }
            try {
                AdminData.write(AdminData.PINGS, new ArrayList<>());
                seenPingCount = 0;
                refreshPings();
            } catch (IOException ex) {
                error(ex);
            }
        });
        bottom.add(clear, BorderLayout.EAST);
        p.add(bottom, BorderLayout.SOUTH);
        return p;
    }

    private JPanel createRooms() {
        JPanel p = page();
        p.add(topBar("Rooms and Tenants", "Refresh", this::refreshRooms), BorderLayout.NORTH);
        roomTable.setRowHeight(32);
        roomTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        p.add(new JScrollPane(roomTable), BorderLayout.CENTER);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 6));
        JButton newRoom = new JButton("Add Room");
        JButton name = new JButton("Set Tenant Name");
        JButton kick = new JButton("Kick Tenant / Vacate");
        JButton overdue = new JButton("Mark Overdue");
        newRoom.addActionListener(e -> {
            try {
                int id = AdminData.addRoom();
                refreshRooms();
                JOptionPane.showMessageDialog(this, "Added Room " + id);
            } catch (Exception ex) {
                error(ex);
            }
        });
        name.addActionListener(e -> {
            AdminData.RoomInfo r = chosenRoom();
            if (r == null) {
                return;
            }
            String text = JOptionPane.showInputDialog(this, "Tenant name for Room " + r.number + ":", r.tenant);
            if (text == null) {
                return;
            }
            if (text.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Enter a tenant name.");
                return;
            }
            try {
                AdminData.changeRoom(r.number, r.status.equals("Vacant") ? "Occupied" : r.status, text);
                refreshRooms();
            } catch (Exception ex) {
                error(ex);
            }
        });
        overdue.addActionListener(e -> {
            AdminData.RoomInfo r = chosenRoom();
            if (r == null) {
                return;
            }
            if (r.tenant.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Set a tenant name first.");
                return;
            }
            try {
                AdminData.changeRoom(r.number, "Overdue", r.tenant);
                refreshRooms();
            } catch (Exception ex) {
                error(ex);
            }
        });
        kick.setBackground(new Color(255, 170, 160));
        kick.addActionListener(e -> {
            AdminData.RoomInfo r = chosenRoom();
            if (r == null) {
                return;
            }
            if (r.status.equals("Vacant")) {
                JOptionPane.showMessageDialog(this, "Room is already vacant.");
                return;
            }
            if (JOptionPane.showConfirmDialog(this, "Kick " + (r.tenant.isEmpty() ? "tenant" : r.tenant) + " from Room " + r.number + "?",
                    "Confirm Vacate", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                return;
            }
            try {
                AdminData.changeRoom(r.number, "Vacant", "");
                refreshRooms();
            } catch (Exception ex) {
                error(ex);
            }
        });
        actions.add(newRoom);
        actions.add(name);
        actions.add(overdue);
        actions.add(kick);
        p.add(actions, BorderLayout.SOUTH);
        return p;
    }

    private AdminData.RoomInfo chosenRoom() {
        int row = roomTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Choose a room first.");
            return null;
        }
        int id = Integer.parseInt(String.valueOf(roomTable.getValueAt(row, 0)));
        try {
            for (AdminData.RoomInfo r : AdminData.getRooms()) {
                if (r.number == id) {
                    return r;
                }
            }
        } catch (IOException ex) {
            error(ex);
        }
        return null;
    }

    private JPanel createAccounts() {
        JPanel p = page();
        p.add(topBar("Staff Accounts", "Refresh", this::refreshAccounts), BorderLayout.NORTH);
        accounts.setEditable(false);
        accounts.setFont(new Font("Monospaced", Font.PLAIN, 15));
        accounts.setMargin(new Insets(15, 15, 15, 15));
        p.add(new JScrollPane(accounts), BorderLayout.CENTER);
        JButton add = new JButton("Create Staff Account");
        add.setBackground(new Color(255, 190, 80));
        add.addActionListener(e -> createStaff());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(add);
        p.add(buttons, BorderLayout.SOUTH);
        return p;
    }

    private void createStaff() {
        JTextField name = new JTextField();
        JPasswordField pwd = new JPasswordField();
        JPasswordField again = new JPasswordField();
        JPanel form = new JPanel(new GridLayout(6, 1, 5, 5));
        form.add(new JLabel("Username (3-30 letters, numbers, underscore):"));
        form.add(name);
        form.add(new JLabel("Password (at least 8 characters):"));
        form.add(pwd);
        form.add(new JLabel("Confirm Password:"));
        form.add(again);
        if (JOptionPane.showConfirmDialog(this, form, "Create Staff Account", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
            return;
        }
        char[] password = pwd.getPassword(), second = again.getPassword();
        try {
            if (!Arrays.equals(password, second)) {
                throw new IllegalArgumentException("Passwords do not match.");
            }
            AdminData.addAccount(name.getText().trim(), password, "staff");
            refreshAccounts();
            JOptionPane.showMessageDialog(this, "Staff account created.");
        } catch (Exception ex) {
            error(ex);
        } finally {
            Arrays.fill(password, '\0');
            Arrays.fill(second, '\0');
        }
    }

    private JPanel createSales() {
        JPanel p = page();
        p.add(topBar("Transaction History by Staff Account", "Refresh", this::refreshSales), BorderLayout.NORTH);
        salesTable.setRowHeight(32);
        salesTable.setAutoCreateRowSorter(true);
        p.add(new JScrollPane(salesTable), BorderLayout.CENTER);
        return p;
    }

    private void refresh() {
        int index = tabs.getSelectedIndex();
        if (index == 0) {
            refreshOverview();
        }
        if (index == 1) {
            refreshMenu();
        }
        if (index == 2) {
            refreshInventory();
        }
        if (index == 3) {
            refreshPings();
        }
        if (index == 4) {
            refreshRooms();
        }
        if (index == 5) {
            refreshAccounts();
        }
        if (index == 6) {
            refreshSales();
        }
    }

    private void refreshOverview() {
        try {
            BigDecimal total = BigDecimal.ZERO;
            int today = 0;
            ArrayList<AdminData.SaleInfo> sales = AdminData.getSales();
            for (AdminData.SaleInfo s : sales) {
                total = total.add(s.total);
                try {
                    if (LocalDateTime.parse(s.date).toLocalDate().equals(LocalDate.now())) {
                        today++;
                    }
                } catch (Exception ex) {
                    /* Ignore malformed date. */ }
            }
            ArrayList<AdminData.RoomInfo> rooms = AdminData.getRooms();
            int occupied = 0;
            overviewRooms.setRowCount(0);
            for (AdminData.RoomInfo r : rooms) {
                if (!r.status.equals("Vacant")) {
                    occupied++;
                }
                overviewRooms.addRow(new Object[]{r.number, r.status, r.tenant.isEmpty() ? "(Not recorded)" : r.tenant});
            }
            totalMoney.setText("P" + total.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString());
            todayOrders.setText(String.valueOf(today));
            allOrders.setText(String.valueOf(sales.size()));
            tenantCount.setText(occupied + " / " + rooms.size());
        } catch (Exception ex) {
            error(ex);
        }
    }

    private void refreshMenu() {
        menuModel.setRowCount(0);
        try {
            Map<String, Integer> stock = AdminData.getStocks();
            for (AdminData.Item item : AdminData.getItems()) {
                menuModel.addRow(new Object[]{item.name, "P" + item.price.toPlainString(), stock.getOrDefault(item.name, 0)});
            }
        } catch (IOException ex) {
            error(ex);
        }
    }

    private void refreshInventory() {
        stockModel.setRowCount(0);
        try {
            Map<String, Integer> stock = AdminData.getStocks();
            for (Map.Entry<String, Integer> item : stock.entrySet()) {
                stockModel.addRow(new Object[]{item.getKey(), item.getValue()});
            }
        } catch (IOException ex) {
            error(ex);
        }
    }

    private void refreshPings() {
        try {
            java.util.List<String> lines = AdminData.read(AdminData.PINGS);
            notifications.setText(lines.isEmpty() ? "No notifications received." : String.join("\n\n", lines));
            notificationCount.setText(lines.size() + " notification(s)");
            if (tabs.getSelectedIndex() == 3) {
                seenPingCount = lines.size();
            }
            int newPings = Math.max(0, lines.size() - seenPingCount);
            tabs.setTitleAt(3, newPings > 0 ? "Notifications (" + newPings + " new)" : "Notifications");
            notifications.setCaretPosition(0);
        } catch (IOException ex) {
            error(ex);
        }
    }

    private void refreshRooms() {
        roomModel.setRowCount(0);
        try {
            for (AdminData.RoomInfo r : AdminData.getRooms()) {
                roomModel.addRow(new Object[]{r.number, r.status, r.tenant});
            }
        } catch (IOException ex) {
            error(ex);
        }
        refreshOverview();
    }

    private void refreshAccounts() {
        StringBuilder text = new StringBuilder("USERNAME                        ROLE\n--------------------------------------------\n");
        try {
            int count = 0;
            for (String line : AdminData.read(AdminData.ACCOUNTS)) {
                String[] p = line.split(",", 3);
                if (p.length == 3) {
                    text.append(String.format("%-30s  %s\n", p[0].trim(), p[2].trim()));
                    count++;
                }
            }
            if (count == 0) {
                text.append("No accounts saved in: " + AdminData.ACCOUNTS.toAbsolutePath());
            }
            accounts.setText(text.toString());
            accounts.setCaretPosition(0);
        } catch (IOException ex) {
            error(ex);
        }
    }

    private void refreshSales() {
        salesModel.setRowCount(0);
        try {
            ArrayList<AdminData.SaleInfo> sales = AdminData.getSales();
            Collections.reverse(sales);
            for (AdminData.SaleInfo s : sales) {
                salesModel.addRow(new Object[]{"#" + s.id, s.date.replace('T', ' '), "P" + s.total, "P" + s.paid, "P" + s.change, s.employee});
            }
        } catch (IOException ex) {
            error(ex);
        }
    }

    private void error(Exception ex) {
        JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}
