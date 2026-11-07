package oopproject;

import java.awt.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class RecentTransaction extends JPanel {

    static final Path FILE = Paths.get(System.getProperty("user.home"), "oopproject_transactions.csv");
    static final Path ITEMS = Paths.get(System.getProperty("user.home"), "oopproject_transaction_items.csv");
    static final Path PAYMENTS = Paths.get(System.getProperty("user.home"), "oopproject_transaction_payments.csv");

    static final DateTimeFormatter SAVED_DATE = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    static final DateTimeFormatter SHOWN_DATE = DateTimeFormatter.ofPattern("MMM dd, hh:mm a", Locale.ENGLISH);

    static int nextNumber = 101;
    static ArrayList<RecentTransaction> panels = new ArrayList<>();

    DefaultTableModel model;
    String account;

    public static class Sale {

        public String id;
        public LocalDateTime date;
        public BigDecimal total = BigDecimal.ZERO;
        public BigDecimal change = BigDecimal.ZERO;
        public int food, drinks, rent;
    }

    static {
        loadNextNumber();
    }

    public RecentTransaction() {
        setLayout(new BorderLayout());
        account = EmployeeSession.getUsername();

        model = new DefaultTableModel(new Object[]{"OrderID", "Date & Time", "Amount"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        JTable table = new JTable(model);
        table.setFont(new Font("Arial", Font.PLAIN, 15));
        table.setRowHeight(45);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setPreferredWidth(90);
        table.getColumnModel().getColumn(1).setPreferredWidth(200);
        table.getColumnModel().getColumn(2).setPreferredWidth(90);
        table.getTableHeader().setResizingAllowed(false);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 16));

        add(new JScrollPane(table), BorderLayout.CENTER);
        panels.add(this);
        refresh();
    }

    public void refresh() {
        model.setRowCount(0);

        for (Sale sale : getSalesFor(account)) {
            model.addRow(new Object[]{
                sale.id,
                sale.date.format(SHOWN_DATE).toUpperCase(Locale.ENGLISH),
                money(sale.total) + " | Change: " + money(sale.change)
            });
        }
    }

    public static synchronized String nextOrderId() {
        return "#" + nextNumber;
    }

    public static synchronized String recordSale(BigDecimal total) throws IOException {
        if (total == null || total.signum() < 0) {
            throw new IllegalArgumentException("Invalid sale total");
        }

        Files.createDirectories(FILE.toAbsolutePath().getParent());

        String id = nextOrderId();
        String employee = EmployeeSession.getUsername();
        if (employee == null) {
            employee = "";
        }
        employee = employee.replace(",", "").trim();
        if (employee.isEmpty()) {
            employee = "Unassigned";
        }

        String line = nextNumber + "," + LocalDateTime.now().format(SAVED_DATE) + ","
                + total.setScale(2, RoundingMode.HALF_UP).toPlainString() + ","
                + employee + System.lineSeparator();

        Files.write(FILE, line.getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);

        nextNumber++;

        SwingUtilities.invokeLater(() -> {
            for (RecentTransaction panel : new ArrayList<>(panels)) {
                panel.refresh();
            }
        });

        return id;
    }

    public static ArrayList<Sale> getSalesFor(String username) {
        ArrayList<Sale> sales = new ArrayList<>();
        if (username == null || username.trim().isEmpty() || !Files.exists(FILE)) {
            return sales;
        }

        HashMap<Integer, int[]> items = loadItems();
        HashMap<Integer, BigDecimal> changes = loadChanges();

        try {
            for (String line : Files.readAllLines(FILE, StandardCharsets.UTF_8)) {
                String[] p = line.split(",", 4);
                if (p.length != 4 || !p[3].trim().equalsIgnoreCase(username.trim())) {
                    continue;
                }

                try {
                    int number = Integer.parseInt(p[0].trim().replace("#", ""));
                    Sale sale = new Sale();
                    sale.id = "#" + number;
                    sale.date = LocalDateTime.parse(p[1].trim(), SAVED_DATE);
                    sale.total = new BigDecimal(p[2].trim());
                    sale.change = changes.getOrDefault(number, BigDecimal.ZERO);

                    int[] count = items.getOrDefault(number, new int[]{0, 0, 0});
                    sale.food = count[0];
                    sale.drinks = count[1];
                    sale.rent = count[2];
                    sales.add(sale);
                } catch (Exception ex) {
                    // Skip a damaged transaction.
                }
            }
        } catch (IOException ex) {
            System.out.println("Cannot read transactions: " + ex.getMessage());
        }

        Collections.reverse(sales);
        return sales;
    }

    private static HashMap<Integer, int[]> loadItems() {
        HashMap<Integer, int[]> items = new HashMap<>();
        if (!Files.exists(ITEMS)) {
            return items;
        }

        try {
            for (String line : Files.readAllLines(ITEMS, StandardCharsets.UTF_8)) {
                String[] p = line.split(",", 4);
                if (p.length != 4) {
                    continue;
                }

                try {
                    int id = Integer.parseInt(p[0].trim().replace("#", ""));
                    int food = Integer.parseInt(p[1].trim());
                    int drinks = Integer.parseInt(p[2].trim());
                    int rent = Integer.parseInt(p[3].trim());
                    items.put(id, new int[]{food, drinks, rent});
                } catch (NumberFormatException ex) {
                    // Skip invalid item counts.
                }
            }
        } catch (IOException ex) {
            System.out.println("Cannot read items: " + ex.getMessage());
        }

        return items;
    }

    private static HashMap<Integer, BigDecimal> loadChanges() {
        HashMap<Integer, BigDecimal> changes = new HashMap<>();
        if (!Files.exists(PAYMENTS)) {
            return changes;
        }

        try {
            for (String line : Files.readAllLines(PAYMENTS, StandardCharsets.UTF_8)) {
                String[] p = line.split(",", 3);
                if (p.length != 3) {
                    continue;
                }

                try {
                    int id = Integer.parseInt(p[0].trim().replace("#", ""));
                    changes.put(id, new BigDecimal(p[2].trim()));
                } catch (NumberFormatException ex) {
                    // Skip invalid payment details.
                }
            }
        } catch (IOException ex) {
            System.out.println("Cannot read payments: " + ex.getMessage());
        }

        return changes;
    }

    private static void loadNextNumber() {
        if (!Files.exists(FILE)) {
            return;
        }

        try {
            for (String line : Files.readAllLines(FILE, StandardCharsets.UTF_8)) {
                String[] p = line.split(",", 2);
                try {
                    int id = Integer.parseInt(p[0].trim().replace("#", ""));
                    if (id >= nextNumber) {
                        nextNumber = id + 1;
                    }
                } catch (NumberFormatException ex) {
                    // Skip invalid order ID.
                }
            }
        } catch (IOException ex) {
            System.out.println("Cannot read next order ID: " + ex.getMessage());
        }
    }

    public static String money(BigDecimal amount) {
        return "P" + String.format(Locale.US, "%,.2f", amount);
    }
}
