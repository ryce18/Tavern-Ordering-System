package oopproject;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

public class Room extends JFrame implements ActionListener {

    public static void main(String[] args) {
        new Room();
    }

    private final Color NORMAL = Color.WHITE;
    private final Color SELECTED = new Color(255, 190, 80);
    private final Color VACANT = new Color(155, 225, 140);
    private final Color OCCUPIED = new Color(220, 80, 75);
    private final Color OVERDUE = new Color(245, 160, 70);

    private ImageIcon backg1 = new ImageIcon("C:\\Users\\ryce2\\Downloads\\OOP_PICTURES\\inventory.png");
    ImageIcon icon = new ImageIcon("C:\\Users\\ryce2\\Downloads\\OOP_PICTURES\\image-removebg-preview.png");
    private JLabel bg1 = new JLabel(backg1);

    JButton dashboard = new JButton("Dashboard");
    JButton room = new JButton("Rooms");
    JButton inventory = new JButton("Inventory");
    JButton transaction = new JButton("Transaction History");
    JButton employee = new JButton("Employee");

    private ArrayList<JButton> roomBoxes = new ArrayList<>();
    private ArrayList<AdminData.RoomInfo> roomList = new ArrayList<>();
    private JPanel roomGrid = new JPanel(null);
    private JLabel selectedLabel = new JLabel();
    private JButton markOverdue = new JButton("Mark as Overdue");
    private JButton pingAdmin = new JButton("Ping Admin");
    private JButton addRoom = new JButton("Add Room");
    private int selectedRoom = -1;

    private void resetSideButtons() {
        dashboard.setBackground(NORMAL);
        room.setBackground(NORMAL);
        inventory.setBackground(NORMAL);
        transaction.setBackground(NORMAL);
        employee.setBackground(NORMAL);
    }

    public Room() {
        resetSideButtons();

        JButton[] nav = {dashboard, room, inventory, transaction, employee};
        for (JButton b : nav) {
            b.setBackground(NORMAL);
            b.setFocusPainted(false);
            b.addActionListener(this);
            add(b);
        }
        room.setBackground(SELECTED);
        dashboard.setBounds(9, 216, 190, 55);
        room.setBounds(9, 307, 190, 55);
        inventory.setBounds(9, 398, 190, 55);
        transaction.setBounds(9, 488, 190, 55);
        employee.setBounds(9, 578, 190, 55);

        JLabel heading = new JLabel("MANAGING ROOMS / TENANTS");
        heading.setFont(new Font("Arial", Font.BOLD, 26));
        heading.setForeground(new Color(72, 43, 25));
        heading.setBounds(270, 68, 700, 40);
        add(heading);

        roomGrid.setOpaque(false);
        JScrollPane scroll = new JScrollPane(roomGrid);
        scroll.setBounds(235, 130, 1015, 480);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(20);
        add(scroll);

        addRoom.setBounds(1020, 85, 180, 40);
        addRoom.setBackground(SELECTED);
        addRoom.setFocusPainted(false);
        addRoom.setVisible("admin".equalsIgnoreCase(EmployeeSession.getRole()));
        addRoom.addActionListener(e -> addNewRoom());
        add(addRoom);

        selectedLabel.setFont(new Font("Arial", Font.BOLD, 17));
        selectedLabel.setBounds(265, 625, 500, 40);
        add(selectedLabel);

        markOverdue.setBounds(800, 620, 175, 40);
        markOverdue.setBackground(SELECTED);
        markOverdue.setFocusPainted(false);
        markOverdue.addActionListener(e -> markRoomOverdue());
        add(markOverdue);

        pingAdmin.setBounds(1000, 620, 200, 40);
        pingAdmin.setBackground(SELECTED);
        pingAdmin.setFocusPainted(false);
        pingAdmin.addActionListener(e -> sendPing());
        add(pingAdmin);

        selectedLabel.setVisible(false);
        markOverdue.setVisible(false);
        pingAdmin.setVisible(false);
        refreshRooms();

        add(bg1);
        setIconImage(icon.getImage());
        setTitle("Traveler's Rest");
        setSize(1290, 758);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
        setLayout(null);
        setResizable(false);

        addWindowFocusListener(new WindowAdapter() {
            @Override
            public void windowGainedFocus(WindowEvent e) {
                refreshRooms();
            }
        });

    }

    private void selectRoom(int number) {
        selectedRoom = number;
        refreshRooms();
    }

    private void refreshRooms() {
        try {
            roomList = AdminData.getRooms();
        } catch (IOException ex) {
            error(ex);
            return;
        }

        roomBoxes.clear();
        roomGrid.removeAll();
        boolean selectedExists = false;
        boolean canMarkOverdue = false;

        for (int i = 0; i < roomList.size(); i++) {
            AdminData.RoomInfo r = roomList.get(i);
            JButton btn = new JButton();
            btn.setBounds(33 + (i % 4) * 250, 10 + (i / 4) * 220, 200, 200);
            btn.setFont(new Font("Arial", Font.BOLD, 23));
            btn.setFocusPainted(false);
            btn.setOpaque(true);

            String tenant = r.tenant == null ? "" : r.tenant.trim();
            String status = r.status == null ? "" : r.status.trim();
            tenant = tenant.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
            status = status.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");

            // Separate labels stop Swing from cutting the whole text with "...".
            btn.setText("");
            btn.setLayout(null);

            JLabel roomNumber = new JLabel("Room #" + r.number, SwingConstants.CENTER);
            roomNumber.setFont(new Font("Arial", Font.BOLD, 22));
            roomNumber.setBounds(5, 28, 190, 35);
            btn.add(roomNumber);

            JLabel roomStatus = new JLabel("<html><center>" + status.replaceAll("\\s+", "<br>")
                    + "</center></html>", SwingConstants.CENTER);
            roomStatus.setFont(new Font("Arial", Font.BOLD, 18));
            roomStatus.setBounds(8, 68, 184, 65);
            btn.add(roomStatus);

            JLabel tenantName = new JLabel("<html><center>" + tenant.replaceAll("\\s+", "<br>")
                    + "</center></html>", SwingConstants.CENTER);
            tenantName.setFont(new Font("Arial", Font.PLAIN, 15));
            tenantName.setBounds(8, 139, 184, 52);
            btn.add(tenantName);

            if (r.status.equals("Vacant")) {
                btn.setBackground(VACANT);
                btn.setForeground(Color.BLACK);
            } else if (r.status.equals("Occupied")) {
                btn.setBackground(OCCUPIED);
                btn.setForeground(Color.WHITE);
            } else if (r.status.equals("Overdue")) {
                btn.setBackground(OVERDUE);
                btn.setForeground(Color.BLACK);
            } else {
                btn.setBackground(Color.GRAY);
                btn.setForeground(Color.WHITE);
            }

            roomNumber.setForeground(btn.getForeground());
            roomStatus.setForeground(btn.getForeground());
            tenantName.setForeground(btn.getForeground());

            MouseAdapter labelClick = new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    selectRoom(r.number);
                }
            };
            roomNumber.addMouseListener(labelClick);
            roomStatus.addMouseListener(labelClick);
            tenantName.addMouseListener(labelClick);

            if (r.number == selectedRoom) {
                selectedExists = true;
                canMarkOverdue = !"Vacant".equalsIgnoreCase(r.status)
                        && !"Overdue".equalsIgnoreCase(r.status);
                selectedLabel.setText("Selected: Room " + r.number + " - " + r.status);
                btn.setBorder(BorderFactory.createLineBorder(Color.BLACK, 5));
            } else {
                btn.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
            }

            btn.addActionListener(e -> selectRoom(r.number));
            roomBoxes.add(btn);
            roomGrid.add(btn);
        }

        roomGrid.setPreferredSize(new Dimension(1000, Math.max(480, ((roomBoxes.size() + 3) / 4) * 220 + 20)));
        roomGrid.revalidate();
        roomGrid.repaint();

        if (!selectedExists) {
            selectedRoom = -1;
            selectedLabel.setText("");
        }
        selectedLabel.setVisible(selectedExists);
        markOverdue.setVisible(selectedExists);
        markOverdue.setEnabled(canMarkOverdue);
        pingAdmin.setVisible(selectedExists);
    }

    private void markRoomOverdue() {
        if (selectedRoom == -1) {
            JOptionPane.showMessageDialog(this, "Choose a room first.");
            return;
        }
        for (AdminData.RoomInfo r : roomList) {
            if (r.number == selectedRoom) {
                if ("Vacant".equalsIgnoreCase(r.status) || "Overdue".equalsIgnoreCase(r.status)) {
                    JOptionPane.showMessageDialog(this, "Only occupied rooms can be marked overdue.");
                    return;
                }
                try {
                    AdminData.changeRoom(selectedRoom, "Overdue", r.tenant == null ? "" : r.tenant);
                    refreshRooms();
                } catch (IOException ex) {
                    error(ex);
                }
                return;
            }
        }
    }

    private void addNewRoom() {
        if (!"admin".equalsIgnoreCase(EmployeeSession.getRole())) {
            return;
        }
        try {
            int number = AdminData.addRoom();
            selectedRoom = number;
            refreshRooms();
            for (int i = 0; i < roomList.size(); i++) {
                if (roomList.get(i).number == number) {
                    roomGrid.scrollRectToVisible(roomBoxes.get(i).getBounds());
                    break;
                }
            }
        } catch (IOException ex) {
            error(ex);
        }
    }

    private void sendPing() {
        if (selectedRoom == -1) {
            JOptionPane.showMessageDialog(this, "Choose a room first.");
            return;
        }
        try {
            String username = EmployeeSession.getUsername();
            if (username == null || username.trim().isEmpty()) {
                username = "Employee";
            }
            AdminData.sendPing("Room " + selectedRoom + " needs attention, reported by " + username);
            JOptionPane.showMessageDialog(this, "Admin notified.");
        } catch (IOException ex) {
            error(ex);
        }
    }

    private void error(Exception e) {
        JOptionPane.showMessageDialog(this, "Could not save or load rooms: " + e.getMessage());
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
