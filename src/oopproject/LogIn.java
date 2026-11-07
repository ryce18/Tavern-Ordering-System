package oopproject;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class LogIn extends JFrame implements ActionListener {

//==================================================================================
//====================================objects=======================================
//==================================================================================
    ImageIcon backg = new ImageIcon("C:\\Users\\ryce2\\Downloads\\OOP_PICTURES\\login.png");
    ImageIcon icon = new ImageIcon("C:\\Users\\ryce2\\Downloads\\OOP_PICTURES\\image-removebg-preview.png");

    JLabel bg = new JLabel();
    JLabel uName = new JLabel("Username: ");
    JLabel pWord = new JLabel("Password: ");

    JTextField username = new JTextField();
    JPasswordField pass = new JPasswordField();

    JButton login = new JButton("Log-In!");

//==================================================================================
//====================================FRAME 1=======================================
//==================================================================================
    public LogIn() {

        login.addActionListener(this);

        pWord.setForeground(Color.WHITE);
        uName.setForeground(Color.WHITE);
        pWord.setBounds(874, 370, 100, 40);
        uName.setBounds(874, 320, 100, 40);
        username.setBounds(874, 350, 350, 30);
        pass.setBounds(874, 400, 350, 30);
        login.setBounds(975, 450, 150, 50);

        bg.setIcon(backg);
        add(uName);
        add(pWord);
        add(login);
        add(username);
        add(pass);
        add(bg);

        setIconImage(icon.getImage());
        setTitle("Traveler's Rest");
        setSize(1390, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
        setLayout(null);
        setResizable(false);
    }

//==================================================================================================================
//====================================LOG IN BUTTON WHERE KNOW IF ADMIN OR NO=======================================
//==================================================================================================================
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == login) {
            String name = username.getText().trim();
            String password = new String(pass.getPassword());
            boolean found = false;

            try (BufferedReader br = new BufferedReader(new FileReader("users.txt"))) {
                String line;

                while ((line = br.readLine()) != null) {
                    String[] data = line.split(",");
                    if (data.length < 3) {
                        continue;
                    }

                    if (data[0].trim().equals(name) && data[1].trim().equals(password)) {
                        found = true;
                        EmployeeSession.login(name, data[2]);

                        if (data[2].trim().equalsIgnoreCase("admin")) {
                            JOptionPane.showMessageDialog(null, "Welcome Admin!");
                            new Admin();
                            dispose();
                        } else {
                            JOptionPane.showMessageDialog(null, "Welcome Staff!");
                            new Dashboard();
                            dispose();
                        }
                        break;
                    }
                }

                if (!found) {
                    JOptionPane.showMessageDialog(null, "Invalid username or Password!");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "File not found!");
            }
        }
    }
}
