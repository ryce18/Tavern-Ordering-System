package oopproject;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.IOException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class Menu {

    private final Color NORMAL = Color.WHITE;
    private final Color SELECTED = new Color(255, 190, 80);
    private final JPanel MENU = new JPanel();
    private final JPanel CATEGORYPANEL = new JPanel();
    private final CheckoutArea CHECKOUT;

    private JScrollPane scrollMenu = new JScrollPane();
    private String currentCategory = "FOOD";
    private String currentFilter = "All";

    public Menu(CheckoutArea area) {
        CHECKOUT = area;

        MENU.setBackground(Color.decode("#452C0F"));
        MENU.setPreferredSize(new Dimension(690, 950));
        MENU.setLayout(new FlowLayout(FlowLayout.LEFT, 27, 20));

        scrollMenu = new JScrollPane(MENU);
        scrollMenu.setBounds(229, 170, 710, 525);
        scrollMenu.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scrollMenu.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollMenu.getVerticalScrollBar().setUnitIncrement(20);

        showFoods();
    }

    public void showRents() {

        MENU.removeAll();

        try {
            java.util.ArrayList<AdminData.RoomInfo> rooms = AdminData.getRooms();

            for (AdminData.RoomInfo r : rooms) {
                int number = r.number;
                int beds = number % 2 == 1 ? 1 : 2;
                int price = beds == 1 ? 750 : 850;

                JPanel card = new JPanel(null);
                card.setPreferredSize(new Dimension(200, 120));
                card.setBorder(BorderFactory.createLineBorder(new Color(120, 70, 30), 2));
                card.setBackground(new Color(70, 35, 15));

                if ("Overdue".equals(r.status)) {
                    card.setBackground(new Color(245, 197, 55));
                }

                JLabel name = new JLabel("Room #" + (number - 100));
                name.setBounds(15, 8, 190, 32);
                name.setFont(new Font("Arial", Font.BOLD, 16));
                name.setForeground(Color.WHITE);
                card.add(name);

                JLabel type = new JLabel(beds + (beds == 1 ? " Bedder" : " Bedders"));
                type.setBounds(15, 40, 120, 20);
                type.setForeground(new Color(238, 197, 111));
                card.add(type);

                JLabel amount = new JLabel("P" + price + " / night");
                amount.setBounds(15, 85, 120, 25);
                amount.setFont(new Font("Arial", Font.BOLD, 15));
                amount.setForeground(new Color(255, 200, 50));
                card.add(amount);

                JButton button = new JButton("Rent");
                button.setBounds(120, 83, 70, 30);
                button.setFocusable(false);

                if ("Vacant".equals(r.status)) {
                    button.addActionListener(e -> rentRoom(number, price));
                } else if ("Overdue".equals(r.status)) {
                    button.setText("Due");
                    button.setEnabled(false);
                    name.setForeground(Color.BLACK);
                    type.setForeground(Color.BLACK);
                    amount.setForeground(Color.BLACK);
                } else {
                    button.setText("Occupied");
                    button.setFont(new Font("Arial", Font.BOLD, 10));
                    button.setBackground(new Color(180, 88, 87));
                    button.setEnabled(false);
                }

                card.add(button);
                MENU.add(card);
            }
            int rows = (rooms.size() + 2) / 3;
            MENU.setPreferredSize(new Dimension(690, Math.max(700, rows * 152 + 20)));
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Cannot load rooms: " + ex.getMessage());
        }

        MENU.revalidate();
        MENU.repaint();
        scrollMenu.revalidate();
        scrollMenu.repaint();
    }

    private void rentRoom(int number, int price) {
        try {
            boolean vacant = false;
            for (AdminData.RoomInfo room : AdminData.getRooms()) {
                if (room.number == number && "Vacant".equals(room.status)) {
                    vacant = true;
                    break;
                }
            }
            if (!vacant) {
                JOptionPane.showMessageDialog(null, "This room is no longer vacant.");
                showRents();
                return;
            }
            if (CHECKOUT.hasRoomInCart(number)) {
                JOptionPane.showMessageDialog(null, "This room is already in the checkout cart.");
                return;
            }

            String guest = JOptionPane.showInputDialog(null, "Tenant name for Room #" + (number - 100));
            if (guest == null) {
                return;
            }
            guest = guest.trim().replace('|', ' ').replace(',', ' ');
            if (guest.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Please enter the tenant name.");
                return;
            }

            String input = JOptionPane.showInputDialog(null, "How many nights?", "1");
            if (input == null) {
                return;
            }

            int nights;
            try {
                nights = Integer.parseInt(input.trim());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Enter a valid number of nights.");
                return;
            }
            if (nights < 1 || nights > 365) {
                JOptionPane.showMessageDialog(null, "Nights must be between 1 and 365.");
                return;
            }

            String item = "Room #" + number + " | " + guest + " | Rent " + nights + " night(s)";
            CHECKOUT.addItem(item, price * (double) nights);
            JOptionPane.showMessageDialog(null, "Room added to checkout: P" + (price * nights)
                    + "\nComplete checkout to occupy the room.");
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Cannot check room: " + ex.getMessage());
        }
    }

    private JPanel createFoodCard(String name, int stock, double price) {
        if (stock < 0) {
            stock = 0;
        }

        JPanel card = new JPanel();
        card.setPreferredSize(new Dimension(200, 120));
        card.setBackground(new Color(70, 35, 15));
        card.setLayout(null);
        card.setBorder(BorderFactory.createLineBorder(new Color(120, 70, 30), 2));

        String safeName = name.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");

        JLabel foodName = new JLabel("<html>" + safeName + "</html>");
        foodName.setForeground(Color.WHITE);
        foodName.setFont(new Font("Arial", Font.BOLD, 16));
        foodName.setBounds(15, 8, 190, 32);
        foodName.setToolTipText(name);

        JLabel foodStock = new JLabel("Stock: " + stock);
        foodStock.setForeground(new Color(255, 200, 100));
        foodStock.setBounds(15, 40, 120, 20);

        JLabel foodPrice = new JLabel("P" + price);
        foodPrice.setForeground(new Color(255, 200, 50));
        foodPrice.setFont(new Font("Arial", Font.BOLD, 18));
        foodPrice.setBounds(15, 85, 120, 25);

        JButton add = new JButton("order");
        add.setBounds(120, 83, 70, 30);
        add.setEnabled(stock > 0);

        add.addActionListener(e -> {
            if (Inventory.getStock(name) <= 0) {
                JOptionPane.showMessageDialog(null, "This item is out of stock.");
                showCategory(currentFilter);
                return;
            }

            CHECKOUT.addItem(name, price);
        });

        card.add(foodName);
        card.add(foodStock);
        card.add(foodPrice);
        card.add(add);

        return card;
    }

    private void createCategories(String... categories) {
        CATEGORYPANEL.removeAll();

        for (String name : categories) {
            JButton button = new JButton(name);
            button.setPreferredSize(new Dimension(90, 35));
            button.setOpaque(true);
            button.setFocusPainted(false);
            button.setBackground(name.equals("All") ? SELECTED : NORMAL);

            button.addActionListener(e -> {
                for (Component c : CATEGORYPANEL.getComponents()) {
                    c.setBackground(NORMAL);
                }

                button.setBackground(SELECTED);
                showCategory(name);
            });

            CATEGORYPANEL.add(button);
        }

        CATEGORYPANEL.revalidate();
        CATEGORYPANEL.repaint();
    }

    private void showCategory(String category) {
        currentFilter = category;
        MENU.removeAll();

        try {
            int count = 0;

            for (AdminData.Item item : AdminData.getItems()) {

                if (!currentCategory.equalsIgnoreCase(item.category)) {
                    continue;
                }

                if (category.equals("Best Sellers") && !item.bestseller) {
                    continue;
                } else if (category.equals("Available") && Inventory.getStock(item.name) <= 0) {
                    continue;
                } else if (!category.equals("All") && !category.equals("Best Sellers")
                        && !category.equals("Available") && !category.equalsIgnoreCase(item.subcategory)) {
                    continue;
                }

                MENU.add(createFoodCard(item.name, Inventory.getStock(item.name), item.price.doubleValue()));
                count++;
            }

            if (count == 0) {
                JLabel empty = new JLabel("No items to show.");
                empty.setForeground(Color.WHITE);
                MENU.add(empty);
            }

            MENU.setPreferredSize(new Dimension(690, Math.max(475, ((count + 1) / 2) * 160 + 20)));

        } catch (IOException ex) {
            JLabel error = new JLabel("Menu could not be loaded: " + ex.getMessage());
            error.setForeground(Color.WHITE);
            MENU.add(error);
            MENU.setPreferredSize(new Dimension(690, 475));
        }

        MENU.revalidate();
        MENU.repaint();
    }

    public void showFoods() {
        currentCategory = "Food";
        CATEGORYPANEL.setVisible(true);

        createCategories("All", "Available", "Best Sellers", "Meat", "Vegetables", "Desserts");
        showCategory("All");
    }

    public void showDrinks() {
        currentCategory = "Drink";
        CATEGORYPANEL.setVisible(true);

        createCategories("All", "Available", "Best Sellers", "Alcohol", "Juices", "Soft Drinks", "Coffee");
        showCategory("All");
    }

    public void refreshMenu() {
        if (currentCategory.equals("Rent")) {
            showRents();
        } else {
            showCategory(currentFilter);
        }
    }

    public JPanel getCategoryPanel() {
        return CATEGORYPANEL;
    }

    public JScrollPane getScrollMenu() {
        return scrollMenu;
    }
}
