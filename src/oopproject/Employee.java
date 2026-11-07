package oopproject;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

public class Employee extends JFrame implements ActionListener {

    private DefaultTableModel transactionModel;

    private final Color NORMAL = Color.WHITE;
    private final Color SELECTED = new Color(255, 190, 80);
    private final Color RED = new Color(178, 59, 59);

    private ImageIcon backg1 = new ImageIcon("C:\\Users\\ryce2\\Downloads\\OOP_PICTURES\\profile.png");
    ImageIcon icon = new ImageIcon("C:\\Users\\ryce2\\Downloads\\OOP_PICTURES\\image-removebg-preview.png");
    private JLabel bg1 = new JLabel(backg1);

    JButton logout = new JButton("Logout");
    JButton dashboard = new JButton("Dashboard");
    JButton room = new JButton("Rooms");
    JButton inventory = new JButton("Inventory");
    JButton transaction = new JButton("Transaction History");
    JButton employee = new JButton("Employee");

    JLabel employeeInitial = new JLabel();
    JLabel employeeName = new JLabel();

    String account = EmployeeSession.getUsername();
    ArrayList<RecentTransaction.Sale> sales = RecentTransaction.getSalesFor(account);

    private void resetSideButtons() {
        dashboard.setBackground(NORMAL);
        room.setBackground(NORMAL);
        inventory.setBackground(NORMAL);
        transaction.setBackground(NORMAL);
        employee.setBackground(NORMAL);
    }

    public Employee() {
        if (account.isEmpty()) {
            account = "Guest";
        }
        employee.setText("Employee(" + account + ")");
        resetSideButtons();
        employee.setBackground(SELECTED);

        JButton[] buttons = {dashboard, room, inventory, transaction, employee, logout};

        for (JButton button : buttons) {
            button.setOpaque(true);
            button.setFocusPainted(false);
            button.addActionListener(this);
        }

        logout.setBackground(RED);
        logout.setForeground(Color.WHITE);
        logout.setBounds(268, 603, 177, 60);

        dashboard.setBounds(9, 216, 190, 55);
        room.setBounds(9, 307, 190, 55);
        inventory.setBounds(9, 398, 190, 55);
        transaction.setBounds(9, 488, 190, 55);
        employee.setBounds(9, 578, 190, 55);

        showTransactions();
        showSummary();
        showProfile();

        add(logout);
        add(dashboard);
        add(room);
        add(inventory);
        add(transaction);
        add(employee);
        add(bg1);

        setIconImage(icon.getImage());
        setTitle("Traveler's Rest");
        setSize(1290, 758);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
        setLayout(null);
        setResizable(false);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowActivated(WindowEvent e) {
                refreshTransactions();
            }
        });
    }

    private void showTransactions() {
        JLabel title = new JLabel("My Recent Transactions");
        title.setBounds(760, 76, 410, 35);
        add(title);

        JLabel sub = new JLabel("Transactions of " + account);
        sub.setBounds(760, 109, 390, 22);
        add(sub);

        transactionModel = new DefaultTableModel(
                new Object[]{"ID", "Food / Drink / Rent",
                    "Amount / Change", "Date"}, 0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(transactionModel);
        table.setFont(new Font("Arial", Font.PLAIN, 11));
        table.setRowHeight(48);

        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 11));
        table.getTableHeader().setReorderingAllowed(false);

        table.getColumnModel().getColumn(0).setPreferredWidth(48);
        table.getColumnModel().getColumn(1).setPreferredWidth(142);
        table.getColumnModel().getColumn(2).setPreferredWidth(132);
        table.getColumnModel().getColumn(3).setPreferredWidth(106);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBounds(760, 150, 430, 365);
        add(scroll);

        refreshTransactions();
    }

    private void refreshTransactions() {
        if (transactionModel == null) {
            return;
        }

        sales = RecentTransaction.getSalesFor(account);
        transactionModel.setRowCount(0);

        for (RecentTransaction.Sale sale : sales) {
            String items = sale.food + " Food / "
                    + sale.drinks + " Drink / "
                    + sale.rent + " Rent";

            String amount = "<html>"
                    + RecentTransaction.money(sale.total)
                    + "<br>Change: "
                    + RecentTransaction.money(sale.change)
                    + "</html>";

            String date = "<html>"
                    + sale.date.format(DateTimeFormatter.ofPattern("MM dd, yyyy"))
                    + "<br>"
                    + sale.date.format(DateTimeFormatter.ofPattern("hh:mm a"))
                    + "</html>";

            transactionModel.addRow(new Object[]{
                sale.id, items, amount, date
            });
        }
    }

    private void showSummary() {
        JLabel title = new JLabel("Today's Summary");
        title.setFont(new Font("Arial", Font.BOLD, 19));
        title.setBounds(540, 320, 400, 30);
        add(title);

        int orders = 0;
        BigDecimal total = BigDecimal.ZERO;
        BigDecimal change = BigDecimal.ZERO;

        for (RecentTransaction.Sale sale : sales) {
            if (sale.date.toLocalDate().equals(LocalDate.now())) {
                orders++;
                total = total.add(sale.total);
                change = change.add(sale.change);
            }
        }

        JLabel orderLabel = new JLabel("Orders: " + orders);
        JLabel salesLabel = new JLabel("Sales: " + RecentTransaction.money(total));
        JLabel changeLabel = new JLabel("Change: " + RecentTransaction.money(change));

        orderLabel.setBounds(550, 345, 132, 40);
        salesLabel.setBounds(550, 375, 155, 40);
        changeLabel.setBounds(550, 405, 155, 40);

        orderLabel.setFont(new Font("Arial", Font.BOLD, 13));
        salesLabel.setFont(new Font("Arial", Font.BOLD, 13));
        changeLabel.setFont(new Font("Arial", Font.BOLD, 12));

        add(orderLabel);
        add(salesLabel);
    }

    private void showProfile() {
        employeeInitial.setText(account.substring(0, 1).toUpperCase());
        employeeInitial.setHorizontalAlignment(SwingConstants.CENTER);
        employeeInitial.setOpaque(true);
        employeeInitial.setBounds(345, 105, 154, 155);
        add(employeeInitial);

        employeeName.setText(account);
        employeeName.setHorizontalAlignment(SwingConstants.CENTER);
        employeeName.setBounds(180, 245, 490, 45);
        add(employeeName);

        JLabel role = new JLabel(EmployeeSession.getRole() + " Account", SwingConstants.CENTER);
        role.setBounds(180, 275, 490, 25);
        add(role);

        JLabel info = new JLabel("Employee Information");
        info.setBounds(510, 80, 480, 32);
        add(info);

        JLabel accountLabel = new JLabel("Account name: " + account);
        JLabel roleLabel = new JLabel("Role: " + EmployeeSession.getRole());
        JLabel statusLabel = new JLabel("Account status: Active");
        JLabel loginLabel = new JLabel("Last login: Not available");

        LocalDateTime loginTime = EmployeeSession.getLoginTime();
        if (loginTime != null) {
            loginLabel.setText("Last login: "
                    + loginTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy - hh:mm a")));
        }

        JLabel[] infoLabels = {accountLabel, roleLabel, statusLabel, loginLabel};
        for (int i = 0; i < infoLabels.length; i++) {
            infoLabels[i].setFont(new Font("Arial", Font.PLAIN, 16));
            infoLabels[i].setBounds(510, 110 + (i * 33), 600, 30);
            add(infoLabels[i]);
        }

        JLabel activity = new JLabel("My Activity");
        activity.setFont(new Font("Arial", Font.BOLD, 20));
        activity.setBounds(320, 320, 450, 35);
        add(activity);

        BigDecimal totalSales = BigDecimal.ZERO;
        int food = 0;
        int drinks = 0;
        int rents = 0;
        for (RecentTransaction.Sale sale : sales) {
            totalSales = totalSales.add(sale.total);
            food += sale.food;
            drinks += sale.drinks;
            rents += sale.rent;
        }

        JLabel totalOrders = new JLabel("Transactions handled: " + sales.size());
        JLabel totalAmount = new JLabel("Total sales: " + RecentTransaction.money(totalSales));
        JLabel totalItems = new JLabel("Food / Drinks / Rents: " + food + " / " + drinks + " / " + rents);

        totalOrders.setBounds(320, 350, 570, 27);
        totalAmount.setBounds(320, 380, 570, 27);
        totalItems.setBounds(320, 410, 570, 27);

        totalOrders.setFont(new Font("Arial", Font.PLAIN, 16));
        totalAmount.setFont(new Font("Arial", Font.PLAIN, 16));
        totalItems.setFont(new Font("Arial", Font.PLAIN, 16));

        add(totalOrders);
        add(totalAmount);
        add(totalItems);

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == logout) {
            int option = JOptionPane.showConfirmDialog(null, "Sure you want to log out?", "LOG OUT", JOptionPane.YES_NO_OPTION);
            if (option == 0) {
                JOptionPane.showMessageDialog(null, "Have a nice day!");
                new LogIn();
                dispose();
            }
        }
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
