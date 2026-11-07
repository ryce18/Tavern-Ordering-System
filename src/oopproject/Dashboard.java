package oopproject;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class Dashboard extends JFrame implements ActionListener {

//colors==============
    private final Color NORMAL = Color.WHITE;
    private final Color SELECTED = new Color(255, 190, 80);

    private void resetTopButtons() {
        food.setBackground(NORMAL);
        drink.setBackground(NORMAL);
        rent.setBackground(NORMAL);
    }

    private void resetSideButtons() {
        dashboard.setBackground(NORMAL);
        room.setBackground(NORMAL);
        inventory.setBackground(NORMAL);
        transaction.setBackground(NORMAL);
        employee.setBackground(NORMAL);
    }

//objects============
    CheckoutArea checkoutPanel = new CheckoutArea();
    Menu menuPanel = new Menu(checkoutPanel);
    RecentTransaction recent = new RecentTransaction();

//background=========
    private ImageIcon backg1 = new ImageIcon("C:\\Users\\ryce2\\Downloads\\OOP_PICTURES\\dashboard(3).png");
    ImageIcon icon = new ImageIcon("C:\\Users\\ryce2\\Downloads\\OOP_PICTURES\\image-removebg-preview.png");

    private JLabel bg1 = new JLabel(backg1);

//buttons===========
    JButton food = new JButton("FOODS!");
    JButton drink = new JButton("DRINKS!");
    JButton rent = new JButton("RENTS!");
    JButton dashboard = new JButton("Dashboard");
    JButton room = new JButton("Rooms");
    JButton inventory = new JButton("Inventory");
    JButton transaction = new JButton("Transaction History");
    JButton employee = new JButton("Employee");

    public Dashboard() {

        resetTopButtons();
        food.setBackground(SELECTED);

        resetSideButtons();
        dashboard.setBackground(SELECTED);

        JButton[] buttons = {food, drink, rent, dashboard, room, inventory, transaction, employee};

        for (JButton button : buttons) {
            button.setOpaque(true);
            button.setFocusPainted(false);
            button.addActionListener(this);
        }

        food.setBounds(255, 34, 195, 85);
        drink.setBounds(485, 34, 195, 85);
        rent.setBounds(717, 34, 195, 85);

        dashboard.setBounds(9, 216, 190, 55);
        room.setBounds(9, 307, 190, 55);
        inventory.setBounds(9, 398, 190, 55);
        transaction.setBounds(9, 488, 190, 55);
        employee.setBounds(9, 578, 190, 55);

        recent.setBounds(968, 382, 301, 330);
        checkoutPanel.setBounds(968, 10, 301, 352);

        add(menuPanel.getCategoryPanel());
        add(menuPanel.getScrollMenu());
        add(recent);
        add(checkoutPanel);
        add(food);
        add(drink);
        add(rent);
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
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == food) {
            resetTopButtons();

            food.setBackground(SELECTED);

            menuPanel.showFoods();
        }
        if (e.getSource() == drink) {
            resetTopButtons();

            drink.setBackground(SELECTED);

            menuPanel.showDrinks();
        }
        if (e.getSource() == rent) {
            resetTopButtons();

            rent.setBackground(SELECTED);

            menuPanel.showRents();
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
