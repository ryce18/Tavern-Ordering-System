package oopproject;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import javax.swing.*;
import javax.swing.table.TableModel;

public class TransactionSorting extends JPanel {

    private JPanel rows = new JPanel();
    private String order = "None";
    private String filter = "All";
    private boolean reverse = false;

    static final Path COUNTS = Paths.get("data", "oopproject_transaction_items.txt");
    static final Path PAYMENTS = Paths.get("data", "oopproject_transaction_payments.txt");
    static final Path TRANSACTIONS = Paths.get("data", "oopproject_transactions.txt");
    static final Path OLD_TRANSACTIONS = Paths.get(System.getProperty("user.home"), "oopproject_transactions.csv");
    static final Path OLD_COUNTS = Paths.get(System.getProperty("user.home"), "oopproject_transaction_items.csv");
    static final Path OLD_PAYMENTS = Paths.get(System.getProperty("user.home"), "oopproject_transaction_payments.csv");

    private static class Sale {

        String id, date, employee;
        BigDecimal total, change;

        Sale(String id, String date, BigDecimal total, BigDecimal change, String employee) {
            this.id = id;
            this.date = date;
            this.total = total;
            this.change = change;
            this.employee = employee;
        }
    }

    public TransactionSorting(TransactionHistory history) {
        setOpaque(false);
        setLayout(new BorderLayout());
        setBounds(260, 148, 960, 520);

        rows.setOpaque(false);
        rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));
        JScrollPane scroller = new JScrollPane(rows);
        scroller.setOpaque(false);
        scroller.getViewport().setOpaque(false);
        scroller.setBorder(BorderFactory.createEmptyBorder());
        scroller.getVerticalScrollBar().setUnitIncrement(25);
        add(scroller, BorderLayout.CENTER);

        history.getContentPane().add(this, 0);
        history.sortId.addActionListener(e -> setOrder("ID"));
        history.sortAmount.addActionListener(e -> setOrder("Amount"));
        history.sortDate.addActionListener(e -> setOrder("Date"));

        JPopupMenu popup = new JPopupMenu();
        for (String text : new String[]{"All", "Most Food", "Most Drinks", "Most Rents"}) {
            JMenuItem item = new JMenuItem(text);
            item.addActionListener(e -> {
                filter = text;
                history.filterBy.setText("Filter: " + text);
                showTransactions();
            });
            popup.add(item);
        }
        history.filterBy.setText("Filter ?");
        history.filterBy.addActionListener(e -> popup.show(history.filterBy, 0, history.filterBy.getHeight()));
        history.addWindowFocusListener(new WindowAdapter() {
            public void windowGainedFocus(WindowEvent e) {
                showTransactions();
            }
        });
        showTransactions();
    }

    private void setOrder(String by) {
        reverse = order.equals(by) ? !reverse : false;
        order = by;
        showTransactions();
    }

    private void showTransactions() {
        rows.removeAll();
        try {
            ArrayList<Sale> sales = loadSales();
            HashMap<String, int[]> counts = loadCounts();
            HashMap<String, BigDecimal[]> payments = loadPayments();

            for (int i = sales.size() - 1; i >= 0; i--) {
                Sale s = sales.get(i);
                int[] c = counts.getOrDefault(s.id, new int[]{0, 0, 0});
                if (filter.equals("Most Food") && (c[0] == 0 || c[0] < c[1] || c[0] < c[2])) {
                    sales.remove(i);
                } else if (filter.equals("Most Drinks") && (c[1] == 0 || c[1] < c[0] || c[1] < c[2])) {
                    sales.remove(i);
                } else if (filter.equals("Most Rents") && (c[2] == 0 || c[2] < c[0] || c[2] < c[1])) {
                    sales.remove(i);
                }
            }

            Comparator<Sale> by;
            if (order.equals("ID")) {
                by = Comparator.comparingInt(s -> Integer.parseInt(s.id));
            } else if (order.equals("Amount")) {
                by = Comparator.comparing(s -> s.total);
            } else {
                by = Comparator.comparing(s -> s.date);
            }
            if (order.equals("ID")) {
                sales.sort(reverse ? by.reversed() : by);
            } else {
                sales.sort(reverse ? by : by.reversed());
            }

            for (Sale s : sales) {
                int[] c = counts.getOrDefault(s.id, new int[]{0, 0, 0});
                BigDecimal[] payment = payments.get(s.id);
                BigDecimal paid = payment == null ? null : payment[0];
                BigDecimal change = payment == null ? s.change : payment[1];
                rows.add(Box.createVerticalStrut(8));
                rows.add(makeSaleRow(s, c, paid, change));
            }
            if (sales.isEmpty()) {
                JLabel empty = new JLabel("No transactions found.", SwingConstants.CENTER);
                empty.setForeground(Color.BLACK);
                rows.add(empty);
            }
        } catch (Exception ex) {
            rows.add(new JLabel("Cannot open transactions: " + ex.getMessage()));
            ex.printStackTrace();
        }
        rows.revalidate();
        rows.repaint();
    }

    // Sales written by Receipt -> RecentTransaction.recordSale() are the main source.
    // Older orders and AdminData orders are also loaded when available.
    private static ArrayList<Sale> loadSales() throws IOException {
        LinkedHashMap<String, Sale> found = new LinkedHashMap<>();

        try {
            for (AdminData.SaleInfo s : AdminData.getSales()) {
                if (s.id == null || !s.id.trim().replace("#", "").matches("\\d+")) {
                    continue;
                }
                String id = s.id.trim().replace("#", "");
                found.put(id, new Sale(id, s.date == null ? "" : s.date,
                        s.total == null ? BigDecimal.ZERO : s.total, s.change,
                        s.employee == null ? "Unassigned" : s.employee));
            }
        } catch (Exception ex) {
            System.err.println("Admin sales could not be read: " + ex.getMessage());
        }

        readSalesFile(OLD_TRANSACTIONS, found);
        readSalesFile(TRANSACTIONS, found);
        readSalesFile(RecentTransaction.FILE, found);
        return new ArrayList<>(found.values());
    }

    private static void readSalesFile(Path file, LinkedHashMap<String, Sale> found) throws IOException {
        if (!Files.exists(file)) {
            return;
        }
        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            String[] p = line.split(",", 4);
            if (p.length < 3) {
                continue;
            }
            try {
                String id = p[0].trim().replace("#", "");
                if (!id.matches("\\d+")) {
                    continue;
                }
                String date = p[1].trim();
                LocalDateTime.parse(date); // Ignore broken dates instead of hiding all history.
                BigDecimal total = new BigDecimal(p[2].trim());
                if (total.signum() < 0) {
                    continue;
                }
                String employee = p.length == 4 && !p[3].trim().isEmpty() ? p[3].trim() : "Unassigned";
                Sale previous = found.get(id);
                BigDecimal change = previous == null ? null : previous.change;
                found.put(id, new Sale(id, date, total, change, employee));
            } catch (Exception ex) {
                System.err.println("Skipping damaged transaction in " + file + ": " + line);
            }
        }
    }

    private static HashMap<String, int[]> loadCounts() throws IOException {
        HashMap<String, int[]> result = new HashMap<>();
        readCountsFile(OLD_COUNTS, result);
        readCountsFile(COUNTS, result);
        return result;
    }

    private static void readCountsFile(Path file, HashMap<String, int[]> result) throws IOException {
        if (!Files.exists(file)) {
            return;
        }
        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            String[] p = line.split(",", 4);
            if (p.length != 4) {
                continue;
            }
            try {
                result.put(p[0].trim().replace("#", ""), new int[]{
                    Integer.parseInt(p[1].trim()), Integer.parseInt(p[2].trim()), Integer.parseInt(p[3].trim())
                });
            } catch (NumberFormatException ex) {
            }
        }
    }

    private static HashMap<String, BigDecimal[]> loadPayments() throws IOException {
        HashMap<String, BigDecimal[]> result = new HashMap<>();
        readPaymentsFile(OLD_PAYMENTS, result);
        readPaymentsFile(PAYMENTS, result);
        return result;
    }

    private static void readPaymentsFile(Path file, HashMap<String, BigDecimal[]> result) throws IOException {
        if (!Files.exists(file)) {
            return;
        }
        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            String[] p = line.split(",", 3);
            if (p.length != 3) {
                continue;
            }
            try {
                result.put(p[0].trim().replace("#", ""), new BigDecimal[]{
                    new BigDecimal(p[1].trim()), new BigDecimal(p[2].trim())
                });
            } catch (NumberFormatException ex) {
            }
        }
    }

    private JPanel makeSaleRow(Sale s, int[] c, BigDecimal paid, BigDecimal change) {
        JPanel record = new JPanel(new GridBagLayout());
        record.setBackground(new Color(128, 96, 72));
        record.setPreferredSize(new Dimension(950, 105));
        record.setMaximumSize(new Dimension(Integer.MAX_VALUE, 105));
        record.setBorder(BorderFactory.createLineBorder(Color.WHITE));

        GridBagConstraints g = new GridBagConstraints();
        g.gridy = 0;
        g.weighty = 1;
        g.fill = GridBagConstraints.BOTH;

        g.gridx = 0;
        g.weightx = 0.7;
        record.add(makeCell("#" + s.id, SwingConstants.CENTER), g);

        g.gridx = 1;
        g.weightx = 3.8;
        String detail = "Food: " + c[0] + "    Drinks: " + c[1] + "    Rents: " + c[2];
        JPanel itemColumn = new JPanel(new GridLayout(2, 1, 0, 3));
        itemColumn.setOpaque(false);
        itemColumn.add(makeLabel(detail, SwingConstants.CENTER));
        itemColumn.add(makeLabel("Employee: " + s.employee, SwingConstants.CENTER));
        record.add(itemColumn, g);

        g.gridx = 2;
        g.weightx = 1.7;
        JPanel amountColumn = new JPanel(new GridLayout(3, 1, 0, 3));
        amountColumn.setOpaque(false);
        amountColumn.add(makeLabel("TOTAL: " + money(s.total), SwingConstants.RIGHT));
        amountColumn.add(makeLabel("PAID: " + money(paid), SwingConstants.RIGHT));
        amountColumn.add(makeLabel("CHANGE: " + money(change), SwingConstants.RIGHT));
        record.add(amountColumn, g);

        g.gridx = 3;
        g.weightx = 1.8;
        JPanel dateColumn = new JPanel(new GridLayout(2, 1, 0, 3));
        dateColumn.setOpaque(false);
        try {
            LocalDateTime date = LocalDateTime.parse(s.date);
            dateColumn.add(makeLabel(date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.ENGLISH)).toUpperCase(Locale.ENGLISH), SwingConstants.RIGHT));
            dateColumn.add(makeLabel(date.format(DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH)), SwingConstants.RIGHT));
        } catch (Exception ex) {
            dateColumn.add(makeLabel(s.date.replace("T", " "), SwingConstants.RIGHT));
        }
        record.add(dateColumn, g);
        return record;
    }

    private JPanel makeCell(String text, int alignment) {
        JPanel cell = new JPanel(new BorderLayout());
        cell.setOpaque(false);
        cell.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 2, Color.WHITE));
        cell.add(makeLabel(text, alignment), BorderLayout.CENTER);
        return cell;
    }

    private JLabel makeLabel(String text, int alignment) {
        JLabel label = new JLabel(text, alignment);
        label.setFont(new Font("Arial", Font.BOLD, 13));
        label.setForeground(Color.WHITE);
        label.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
        return label;
    }

    private String money(BigDecimal value) {
        return value == null ? "Not saved" : "P" + value.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    public static synchronized void savePaymentDetails(String orderId, BigDecimal paid, BigDecimal change) throws IOException {
        if (orderId == null || paid == null || change == null) {
            throw new IllegalArgumentException("Invalid payment details.");
        }
        String id = orderId.replace("#", "").trim();
        String row = id + "," + paid.toPlainString() + "," + change.toPlainString() + System.lineSeparator();
        Files.createDirectories(PAYMENTS.toAbsolutePath().getParent());
        Files.write(PAYMENTS, row.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    public static synchronized void saveOrderItems(String orderId, TableModel model) throws IOException {
        int food = 0, drinks = 0, rents = 0;
        HashSet<String> drinkNames = new HashSet<>();
        for (AdminData.Item item : AdminData.getItems()) {
            if (item.category.equalsIgnoreCase("Drink") || item.category.equalsIgnoreCase("Drinks") || item.category.equalsIgnoreCase("Beverage") || item.category.equalsIgnoreCase("Beverages")) {
                drinkNames.add(item.name.toLowerCase(Locale.ROOT).trim());
            }
        }
        for (int i = 0; i < model.getRowCount(); i++) {
            String name = String.valueOf(model.getValueAt(i, 0)).trim();
            Object value = model.getValueAt(i, 2);
            int qty = value instanceof Number ? ((Number) value).intValue() : Integer.parseInt(String.valueOf(value).trim());
            if (name.toLowerCase(Locale.ROOT).contains("rent")) {
                rents += qty;
            } else if (drinkNames.contains(name.toLowerCase(Locale.ROOT))) {
                drinks += qty;
            } else {
                food += qty;
            }
        }
        saveCounts(orderId, food, drinks, rents);
    }

    public static synchronized void saveCounts(String orderId, int food, int drinks, int rents) throws IOException {
        if (orderId == null || food < 0 || drinks < 0 || rents < 0) {
            throw new IllegalArgumentException("Invalid order counts.");
        }
        String row = orderId.replace("#", "").trim() + "," + food + "," + drinks + "," + rents + System.lineSeparator();
        Files.createDirectories(COUNTS.toAbsolutePath().getParent());
        Files.write(COUNTS, row.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }
}
