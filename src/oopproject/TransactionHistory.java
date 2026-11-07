package oopproject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TransactionHistory extends JFrame implements ActionListener {

    private final Color NORMAL = Color.WHITE;
    private final Color SELECTED = new Color(255, 190, 80);

    private ImageIcon backg1 = new ImageIcon("C:\\Users\\ryce2\\Downloads\\OOP_PICTURES\\transaction history.png");
    ImageIcon icon = new ImageIcon("C:\\Users\\ryce2\\Downloads\\OOP_PICTURES\\image-removebg-preview.png");

    private JLabel bg1 = new JLabel(backg1);

    JButton sortId = new JButton("ID");
    JButton sortAmount = new JButton("Amount");
    JButton sortDate = new JButton("Date");
    JButton filterBy = new JButton("Filter");
    JButton dashboard = new JButton("Dashboard");
    JButton room = new JButton("Rooms");
    JButton inventory = new JButton("Inventory");
    JButton transaction = new JButton("Transaction History");
    JButton employee = new JButton("Employee");

    private void resetTopButtons() {
        sortId.setBackground(NORMAL);
        sortAmount.setBackground(NORMAL);
        sortDate.setBackground(NORMAL);
        filterBy.setBackground(NORMAL);
    }

    private void resetSideButtons() {
        dashboard.setBackground(NORMAL);
        room.setBackground(NORMAL);
        inventory.setBackground(NORMAL);
        transaction.setBackground(NORMAL);
        employee.setBackground(NORMAL);
    }

    public TransactionHistory() {
        resetSideButtons();
        resetTopButtons();
        transaction.setBackground(SELECTED);
        sortId.setBackground(SELECTED);

        JButton[] buttons = {dashboard, room, inventory, transaction, employee, sortAmount, sortId, sortDate, filterBy};

        for (JButton button : buttons) {
            button.setOpaque(true);
            button.setFocusPainted(false);
            button.addActionListener(this);
        }

        sortId.setBounds(264, 38, 194, 52);
        sortAmount.setBounds(467, 38, 195, 52);
        sortDate.setBounds(670, 38, 196, 52);
        filterBy.setBounds(1025, 38, 196, 52);

        dashboard.setBounds(9, 216, 190, 55);
        room.setBounds(9, 307, 190, 55);
        inventory.setBounds(9, 398, 190, 55);
        transaction.setBounds(9, 488, 190, 55);
        employee.setBounds(9, 578, 190, 55);

        add(sortId);
        add(sortAmount);
        add(sortDate);
        add(filterBy);
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
        new TransactionSorting(this);
        setResizable(false);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == sortId) {
            resetTopButtons();
            sortId.setBackground(SELECTED);
        }
        if (e.getSource() == sortAmount) {
            resetTopButtons();
            sortAmount.setBackground(SELECTED);
        }
        if (e.getSource() == sortDate) {
            resetTopButtons();
            sortDate.setBackground(SELECTED);
        }
        if (e.getSource() == filterBy) {
            resetTopButtons();
            filterBy.setBackground(SELECTED);
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
