package oopproject;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.*;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class AdminData {

    static final Path MENU = Paths.get("data", "oopproject_menu.txt");
    static final Path STOCKS = Paths.get("data", "oopproject_inventory.txt");
    static final Path ROOMS = Paths.get("data", "oopproject_rooms.txt");
    static final Path TENANTS = Paths.get("data", "oopproject_tenants.txt");
    static final Path PINGS = Paths.get("data", "oopproject_admin_pings.txt");
    static final Path SALES = Paths.get("data", "oopproject_transactions.txt");
    static final Path PAYMENTS = Paths.get("data", "oopproject_transaction_payments.txt");
    static final Path ACCOUNTS = Paths.get("data", "users.txt"); // All saved files are inside the project data folder.

    static class Item {

        String name, category, subcategory;
        boolean bestseller;
        BigDecimal price;

        Item(String n, String c, BigDecimal p, String sc, boolean best) {
            name = n;
            category = c;
            price = p;
            subcategory = sc;
            bestseller = best;
        }
    }

    static class RoomInfo {

        int number;
        String status, tenant;

        RoomInfo(int n, String s, String t) {
            number = n;
            status = s;
            tenant = t;
        }
    }

    static class SaleInfo {

        String id, employee, date;
        BigDecimal total, paid, change;

        SaleInfo(String i, String d, BigDecimal t, String e) {
            id = i;
            date = d;
            total = t;
            employee = e;
            paid = BigDecimal.ZERO;
            change = BigDecimal.ZERO;
        }
    }

    static String clean(String s) {
        return s == null ? "" : s.trim().replace(',', ' ').replace('|', ' ').replace('\n', ' ').replace('\r', ' ');
    }

    // Import old data only when the new file is missing or empty.
    // Original files are left untouched as backups.
    private static synchronized void importOldFile(Path file) throws IOException {
        if (Files.exists(file)) {
            boolean hasData = false;
            for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                if (!line.trim().isEmpty()) {
                    hasData = true;
                    break;
                }
            }
            if (hasData) {
                return;
            }
        }

        String filename = file.getFileName().toString();
        String oldName = filename.endsWith(".txt")
                ? filename.substring(0, filename.length() - 4) + ".csv" : filename;
        Path home = Paths.get(System.getProperty("user.home"));
        ArrayList<Path> oldLocations = new ArrayList<>();
        if (filename.equals("users.txt")) {
            oldLocations.add(Paths.get("users.txt"));
            oldLocations.add(home.resolve("users.txt"));
        } else {
            oldLocations.add(home.resolve(oldName));
            oldLocations.add(Paths.get(oldName));
            oldLocations.add(home.resolve(filename));
            oldLocations.add(Paths.get(filename));
        }
        for (Path oldFile : oldLocations) {
            if (file.toAbsolutePath().normalize().equals(oldFile.toAbsolutePath().normalize())
                    || !Files.isRegularFile(oldFile)) {
                continue;
            }
            ArrayList<String> saved = new ArrayList<>(Files.readAllLines(oldFile, StandardCharsets.UTF_8));
            boolean hasData = false;
            for (String line : saved) {
                if (!line.trim().isEmpty()) {
                    hasData = true;
                    break;
                }
            }
            if (hasData) {
                write(file, saved);
                System.out.println("Imported old records from " + oldFile.toAbsolutePath() + " to " + file.toAbsolutePath());
                return;
            }
        }
    }

    static synchronized List<String> read(Path file) throws IOException {
        importOldFile(file);
        if (!Files.exists(file)) {
            return new ArrayList<>();
        }
        return Files.readAllLines(file, StandardCharsets.UTF_8);
    }

    static void write(Path file, List<String> lines) throws IOException {
        Path target = file.toAbsolutePath();
        Files.createDirectories(target.getParent());
        Path temp = target.resolveSibling(target.getFileName() + ".tmp");
        Files.write(temp, lines, StandardCharsets.UTF_8);
        try {
            Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException ex) {
            Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    static final String[][] DEFAULT_ITEMS = {
        {"Kinilaw na axolotl", "Food", "150"}, {"Sinigang na wombat", "Food", "250"},
        {"Tinolang Dodo bird", "Food", "180"}, {"Pritong Sabaw", "Food", "120"},
        {"Adobo", "Food", "140"}, {"Bulalo", "Food", "350"}, {"Steak", "Food", "450"},
        {"Sisig", "Food", "120"}, {"Burger", "Food", "150"}, {"Chicken", "Food", "180"},
        {"Beer", "Drink", "80"}, {"Wine", "Drink", "300"}, {"Whisky", "Drink", "500"},
        {"Coke", "Drink", "40"}, {"Sprite", "Drink", "40"}
    };
    static final String[][] DEFAULT_STOCK = {
        {"Kinilaw na axolotl", "20"}, {"Sinigang na wombat", "15"},
        {"Tinolang Dodo bird", "30"}, {"Pritong Sabaw", "10"}, {"Adobo", "25"},
        {"Bulalo", "8"}, {"Steak", "5"}, {"Sisig", "10"}, {"Burger", "20"},
        {"Chicken", "30"}, {"Beer", "50"}, {"Wine", "12"}, {"Whisky", "8"},
        {"Coke", "100"}, {"Sprite", "100"}
    };

    static String defaultSubcategory(String name, String category) {
        if (category.equals("Food")) {
            if (name.equals("Adobo") || name.equals("Steak") || name.equals("Sisig") || name.equals("Burger") || name.equals("Chicken")) {
                return "Meat";
            }
            return "Other";
        }
        if (name.equals("Beer") || name.equals("Wine") || name.equals("Whisky")) {
            return "Alcohol";
        }
        if (name.equals("Coke") || name.equals("Sprite")) {
            return "Soft Drinks";
        }
        return "Other";
    }

    static boolean defaultBestseller(String name) {
        return name.equals("Adobo") || name.equals("Sisig") || name.equals("Beer") || name.equals("Wine");
    }

    static synchronized ArrayList<Item> getItems() throws IOException {
        importOldFile(MENU);
        if (!Files.exists(MENU)) {
            ArrayList<String> seed = new ArrayList<>();
            for (String[] i : DEFAULT_ITEMS) {
                seed.add(i[0] + "|" + i[1] + "|" + i[2]);
            }
            write(MENU, seed);
        }
        ArrayList<Item> items = new ArrayList<>();
        for (String line : read(MENU)) {
            String[] p = line.split("\\|", -1);
            if (p.length < 3) {
                continue;
            }
            try {
                BigDecimal price = new BigDecimal(p[2]);
                if (!p[0].trim().isEmpty() && (p[1].equals("Food") || p[1].equals("Drink")) && price.signum() >= 0) {
                    items.add(new Item(p[0], p[1], price, p.length >= 4 ? p[3] : defaultSubcategory(p[0], p[1]),
                            p.length >= 5 ? Boolean.parseBoolean(p[4]) : defaultBestseller(p[0])));
                }
            } catch (NumberFormatException ex) {
                /* Ignore damaged row. */ }
        }
        return items;
    }

    static synchronized void addItem(String name, String category, BigDecimal price, int quantity) throws IOException {
        addItem(name, category, "Other", false, price, quantity);
    }

    // Old five-field menu records are still supported for compatibility.
    static synchronized void addItem(String name, String category, String subcategory, boolean bestseller, BigDecimal price, int quantity) throws IOException {
        name = clean(name);
        subcategory = clean(subcategory);
        if (name.isEmpty() || name.length() > 70 || (!category.equals("Food") && !category.equals("Drink"))
                || price == null || price.signum() <= 0 || quantity < 0) {
            throw new IllegalArgumentException("Invalid item details.");
        }
        for (Item i : getItems()) {
            if (i.name.equalsIgnoreCase(name)) {
                throw new IllegalArgumentException("Item already exists.");
            }
        }
        Map<String, Integer> stock = getStocks();
        for (String existing : stock.keySet()) {
            if (existing.equalsIgnoreCase(name)) {
                throw new IllegalArgumentException("This item name already exists in inventory.");
            }
        }
        ArrayList<String> lines = new ArrayList<>(read(MENU));
        lines.add(name + "|" + category + "|" + price.toPlainString() + "|" + subcategory + "|" + bestseller);
        write(MENU, lines);
        stock.put(name, quantity);
        saveStocks(stock);
    }

    static synchronized Map<String, Integer> getStocks() throws IOException {
        LinkedHashMap<String, Integer> stock = new LinkedHashMap<>();
        for (String[] s : DEFAULT_STOCK) {
            stock.put(s[0], Integer.parseInt(s[1]));
        }
        for (String line : read(STOCKS)) {
            String[] p = line.split(",", 2);
            if (p.length != 2) {
                continue;
            }
            try {
                int q = Integer.parseInt(p[1].trim());
                if (q >= 0 && !p[0].trim().isEmpty()) {
                    stock.put(p[0].trim(), q);
                }
            } catch (NumberFormatException ex) {
                /* Skip damaged stock. */ }
        }
        for (Item i : getItems()) {
            if (!stock.containsKey(i.name)) {
                stock.put(i.name, 0);
            }
        }
        return stock;
    }

    static synchronized void saveStocks(Map<String, Integer> stock) throws IOException {
        ArrayList<String> lines = new ArrayList<>();
        for (Map.Entry<String, Integer> e : stock.entrySet()) {
            lines.add(clean(e.getKey()) + "," + e.getValue());
        }
        write(STOCKS, lines);
    }

    static synchronized void restock(String name, int extra) throws IOException {
        if (extra <= 0) {
            throw new IllegalArgumentException("Enter a quantity greater than zero.");
        }
        Map<String, Integer> stock = getStocks();
        if (!stock.containsKey(name)) {
            throw new IllegalArgumentException("Item not found.");
        }
        stock.put(name, Math.addExact(stock.get(name), extra));
        saveStocks(stock);
    }

    static synchronized String checkStock(Map<String, Integer> needed) throws IOException {
        Map<String, Integer> stock = getStocks();
        for (Map.Entry<String, Integer> e : needed.entrySet()) {
            if (!stock.containsKey(e.getKey())) {
                return e.getKey() + " is not in the inventory.";
            }
            if (e.getValue() > stock.get(e.getKey())) {
                return e.getKey() + " only has " + stock.get(e.getKey()) + " left.";
            }
        }
        return null;
    }

    static synchronized void removeStock(Map<String, Integer> needed) throws IOException {
        String error = checkStock(needed);
        if (error != null) {
            throw new IOException(error);
        }
        Map<String, Integer> stock = getStocks();
        for (Map.Entry<String, Integer> e : needed.entrySet()) {
            stock.put(e.getKey(), stock.get(e.getKey()) - e.getValue());
        }
        saveStocks(stock);
    }

    static synchronized ArrayList<RoomInfo> getRooms() throws IOException {
        ArrayList<String> roomLines = new ArrayList<>(read(ROOMS));
        if (!Files.exists(ROOMS)) {
            for (int n = 101; n <= 108; n++) {
                roomLines.add(n + ",Vacant");
            }
            write(ROOMS, roomLines);
        }
        HashMap<Integer, String> names = new HashMap<>();
        for (String line : read(TENANTS)) {
            String[] p = line.split(",", 2);
            if (p.length == 2) try {
                names.put(Integer.parseInt(p[0].trim()), p[1].trim());
            } catch (NumberFormatException ex) {
                /* Skip damaged name. */ }
        }
        ArrayList<RoomInfo> rooms = new ArrayList<>();
        HashSet<Integer> seen = new HashSet<>();
        for (String line : roomLines) {
            String[] p = line.split(",", 2);
            if (p.length != 2) {
                continue;
            }
            try {
                int number = Integer.parseInt(p[0].trim());
                String status = p[1].trim();
                if (number >= 101 && seen.add(number) && (status.equals("Vacant") || status.equals("Occupied") || status.equals("Overdue"))) {
                    rooms.add(new RoomInfo(number, status, status.equals("Vacant") ? "" : names.getOrDefault(number, "")));
                }
            } catch (NumberFormatException ex) {
                /* Skip bad room. */ }
        }
        return rooms;
    }

    static synchronized void saveRooms(List<RoomInfo> rooms) throws IOException {
        ArrayList<String> roomLines = new ArrayList<>(), tenantLines = new ArrayList<>();
        for (RoomInfo r : rooms) {
            roomLines.add(r.number + "," + r.status);
            if (!r.status.equals("Vacant") && !r.tenant.isEmpty()) {
                tenantLines.add(r.number + "," + clean(r.tenant));
            }
        }
        write(ROOMS, roomLines);
        write(TENANTS, tenantLines);
    }

    static synchronized void changeRoom(int room, String status, String tenant) throws IOException {
        ArrayList<RoomInfo> rooms = getRooms();
        for (RoomInfo r : rooms) {
            if (r.number == room) {
                r.status = status;
                r.tenant = status.equals("Vacant") ? "" : clean(tenant);
                saveRooms(rooms);
                return;
            }
        }
        throw new IllegalArgumentException("Room not found.");
    }

    static synchronized void removeRoom(int number) throws IOException {
        ArrayList<RoomInfo> rooms = getRooms();
        RoomInfo found = null;
        for (RoomInfo room : rooms) {
            if (room.number == number) {
                found = room;
            }
        }
        if (found == null) {
            throw new IllegalArgumentException("Room not found.");
        }
        if (!found.status.equals("Vacant")) {
            throw new IllegalArgumentException("Vacate this room before removing it.");
        }
        rooms.remove(found);
        saveRooms(rooms);
    }

    static synchronized int addRoom() throws IOException {
        ArrayList<RoomInfo> rooms = getRooms();
        int next = 101;
        for (RoomInfo r : rooms) {
            next = Math.max(next, r.number + 1);
        }
        rooms.add(new RoomInfo(next, "Vacant", ""));
        saveRooms(rooms);
        return next;
    }

    static synchronized void sendPing(String message) throws IOException {
        read(PINGS); // Preserve old notifications before appending.
        String date = java.time.LocalDateTime.now().toString();
        Files.createDirectories(PINGS.toAbsolutePath().getParent());
        Files.write(PINGS, (date + " - " + clean(message) + System.lineSeparator()).getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    static ArrayList<SaleInfo> getSales() throws IOException {
        ArrayList<SaleInfo> sales = new ArrayList<>();
        HashMap<String, BigDecimal[]> payments = new HashMap<>();
        for (String line : read(PAYMENTS)) {
            String[] p = line.split(",", 3);
            if (p.length != 3) {
                continue;
            }
            try {
                payments.put(p[0].trim().replace("#", ""), new BigDecimal[]{new BigDecimal(p[1].trim()), new BigDecimal(p[2].trim())});
            } catch (NumberFormatException ex) {
                /* Skip bad payment. */ }
        }
        for (String line : read(SALES)) {
            String[] p = line.split(",", 4);
            if (p.length < 3) {
                continue;
            }
            try {
                SaleInfo sale = new SaleInfo(p[0].trim().replace("#", ""), p[1].trim(), new BigDecimal(p[2].trim()),
                        p.length >= 4 ? p[3].trim() : "Unassigned");
                BigDecimal[] paid = payments.get(sale.id);
                if (paid != null) {
                    sale.paid = paid[0];
                    sale.change = paid[1];
                }
                sales.add(sale);
            } catch (NumberFormatException ex) {
                /* Skip bad record. */ }
        }
        return sales;
    }

    static boolean isValidUsername(String name) {
        return name != null && name.matches("[A-Za-z0-9_]{3,30}");
    }

    static boolean hasAccounts() throws IOException {
        return !read(ACCOUNTS).isEmpty();
    }

    static synchronized void addAccount(String name, char[] password, String role) throws Exception {
        if (!isValidUsername(name) || password.length < 8 || !role.equals("admin") && !role.equals("staff")) {
            throw new IllegalArgumentException("Username: 3-30 letters/numbers/underscore. Password: at least 8 characters.");
        }
        for (String line : read(ACCOUNTS)) {
            String[] p = line.split(",", 3);
            if (p.length > 0 && p[0].trim().equalsIgnoreCase(name)) {
                throw new IllegalArgumentException("Username already exists.");
            }
        }
        ArrayList<String> lines = new ArrayList<>(read(ACCOUNTS));
        lines.add(name + "," + hashPassword(password) + "," + role);
        write(ACCOUNTS, lines);
    }

    static String verifyLogin(String name, char[] password) throws Exception {
        for (String line : read(ACCOUNTS)) {
            String[] p = line.split(",", 3);
            if (p.length != 3 || !p[0].trim().equalsIgnoreCase(name)) {
                continue;
            }
            String stored = p[1].trim();
            boolean match;
            if (stored.startsWith("pbkdf2$")) {
                String[] parts = stored.split("\\$");
                if (parts.length != 3) {
                    continue;
                }
                byte[] salt = Base64.getDecoder().decode(parts[1]);
                byte[] expected = Base64.getDecoder().decode(parts[2]);
                PBEKeySpec spec = new PBEKeySpec(password, salt, 120000, 256);
                byte[] actual = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
                spec.clearPassword();
                match = MessageDigest.isEqual(expected, actual);
                Arrays.fill(actual, (byte) 0);
            } else {
                // Accept old project's username,password,role records without breaking existing accounts.
                match = MessageDigest.isEqual(stored.getBytes(StandardCharsets.UTF_8),
                        new String(password).getBytes(StandardCharsets.UTF_8));
            }
            return match ? p[2].trim().toLowerCase(Locale.ROOT) : null;
        }
        return null;
    }

    static String hashPassword(char[] password) throws Exception {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        PBEKeySpec spec = new PBEKeySpec(password, salt, 120000, 256);
        byte[] result = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        spec.clearPassword();
        return "pbkdf2$" + Base64.getEncoder().encodeToString(salt) + "$" + Base64.getEncoder().encodeToString(result);
    }
}