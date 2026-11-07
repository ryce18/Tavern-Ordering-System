package oopproject;

import java.time.LocalDateTime;

public class EmployeeSession {

    static String username = "";
    static String role = "Employee";
    static LocalDateTime loginTime;

    public static void login(String name, String userRole) {
        username = name.trim();
        role = userRole.trim();
        loginTime = LocalDateTime.now();
    }

    public static String getUsername() {
        return username;
    }

    public static String getRole() {
        return role;
    }

    public static LocalDateTime getLoginTime() {
        return loginTime;
    }

    public static void logout() {
        username = "";
        role = "Employee";
        loginTime = null;
    }

}
