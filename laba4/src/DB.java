

import model.*;
import model.freight.*;
import model.passenger.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

import core.Group;

public class DB {
    private final Path dir;
    private final Map<Long, User> users = new LinkedHashMap<>();
    private final Map<Long, Vehicle> vehicles = new LinkedHashMap<>();
    private long nextUser = 1, nextVehicle = 1;
    public DB(Path dir) { this.dir = dir; }
    public Collection<User> users() { return new ArrayList<>(users.values()); }
    public Map<Long, Vehicle> vehicles() { return new LinkedHashMap<>(vehicles); }
    public User getUser(long id) { return users.get(id); }
    public Vehicle getVehicle(long id) { return vehicles.get(id); }
    public User findUser(String login) { for (User u : users.values()) if (u.getLogin().equals(login)) return u; return null; }

    public void load() throws IOException {
        Files.createDirectories(dir);
        Map<Long, User> newUsers = new LinkedHashMap<>();
        Map<Long, Vehicle> newVehicles = new LinkedHashMap<>();
        Path uf = dir.resolve("users.txt"), vf = dir.resolve("vehicles.txt");
        if (Files.exists(uf)) {
            int number = 0;
            for (String line : Files.readAllLines(uf, StandardCharsets.UTF_8)) {
                number++; if (line.trim().isEmpty()) continue;
                try {
                    String[] f = line.split("\t", -1);
                    if (f.length != 4) throw new IllegalArgumentException("ожидалось 4 поля");
                    User u = new User(Long.parseLong(f[0]), f[1], f[2], Group.valueOf(f[3]));
                    if (newUsers.containsKey(u.getId())) throw new IllegalArgumentException("повтор ID");
                    for (User old : newUsers.values()) if (old.getLogin().equals(u.getLogin())) throw new IllegalArgumentException("повтор логина");
                    newUsers.put(u.getId(), u);
                } catch (IllegalArgumentException e) { throw new IOException(uf + ", строка " + number + ": " + e.getMessage(), e); }
            }
        }
        if (Files.exists(vf)) {
            int number = 0;
            for (String line : Files.readAllLines(vf, StandardCharsets.UTF_8)) {
                number++; if (line.trim().isEmpty()) continue;
                try {
                    String[] f = line.split("\t", -1);
                    if (f.length != 6) throw new IllegalArgumentException("ожидалось 6 полей");
                    long id = Long.parseLong(f[0]);
                    if (id < 1 || newVehicles.containsKey(id)) throw new IllegalArgumentException("неверный или повторный ID");
                    newVehicles.put(id, createVehicle(f[1], Integer.parseInt(f[2]), Integer.parseInt(f[3]), Integer.parseInt(f[4]), CargoClass.valueOf(f[5])));
                } catch (IllegalArgumentException e) { throw new IOException(vf + ", строка " + number + ": " + e.getMessage(), e); }
            }
        }
        users.clear(); users.putAll(newUsers); vehicles.clear(); vehicles.putAll(newVehicles);
        nextUser = users.keySet().stream().mapToLong(Long::longValue).max().orElse(0) + 1;
        nextVehicle = vehicles.keySet().stream().mapToLong(Long::longValue).max().orElse(0) + 1;
    }

    public void save() throws IOException {
        List<String> ul = new ArrayList<>(), vl = new ArrayList<>();
        for (User u : users.values()) ul.add(u.getId() + "\t" + u.getLogin() + "\t" + u.getPassword() + "\t" + u.getGroup().name());
        for (Map.Entry<Long, Vehicle> e : vehicles.entrySet()) {
            Vehicle v = e.getValue();
            vl.add(e.getKey() + "\t" + type(v) + "\t" + v.getDimensions() + "\t" + v.getWeight() + "\t" + v.getSpeed() + "\t" + v.getCargoClass().name());
        }
        write(dir.resolve("users.txt"), ul); write(dir.resolve("vehicles.txt"), vl);
    }
    public void saveCurrentUser(User u) throws IOException { write(dir.resolve("current_user.txt"), Collections.singletonList(u.getId() + "\t" + u.getLogin())); }
    public String loadCurrentUser() throws IOException {
        Path file = dir.resolve("current_user.txt");
        if (!Files.exists(file)) return "нет";
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        if (lines.size() != 1) throw new IOException("В current_user.txt должна быть одна запись");
        String[] f = lines.get(0).split("\t", -1);
        if (f.length != 2) throw new IOException("Некорректный current_user.txt");
        try {
            User u = users.get(Long.parseLong(f[0]));
            return u != null && u.getLogin().equals(f[1]) ? u.getLogin() : "не найден среди пользователей";
        } catch (NumberFormatException e) { throw new IOException("Некорректный ID в current_user.txt", e); }
    }
    public User addUser(String login, String password, Group group) {
        check(login); check(password);
        if (findUser(login) != null) throw new IllegalArgumentException("Логин уже существует");
        User u = new User(nextUser++, login, password, group); users.put(u.getId(), u); return u;
    }
    public void changeUser(long id, String login, String password, Group group) {
        if (!users.containsKey(id)) throw new IllegalArgumentException("Пользователь не найден");
        check(login); check(password);
        User other = findUser(login);
        if (other != null && other.getId() != id) throw new IllegalArgumentException("Логин уже существует");
        users.put(id, new User(id, login, password, group));
    }
    public void deleteUser(long id) { if (users.remove(id) == null) throw new IllegalArgumentException("Пользователь не найден"); }
    public long addVehicle(Vehicle v) { long id = nextVehicle++; vehicles.put(id, v); return id; }
    public void changeVehicle(long id, Vehicle v) { if (!vehicles.containsKey(id)) throw new IllegalArgumentException("Машина не найдена"); vehicles.put(id, v); }
    public void deleteVehicle(long id) { if (vehicles.remove(id) == null) throw new IllegalArgumentException("Машина не найдена"); }
    public static Vehicle createVehicle(String type, int dimensions, int weight, int speed, CargoClass cargo) {
        if (dimensions <= 0 || weight <= 0 || speed < 0) throw new IllegalArgumentException("Габарит и масса должны быть положительными, скорость неотрицательной");
        switch (type.toUpperCase()) {
            case "CAR": return new Car(dimensions, weight, speed, cargo);
            case "BUS": return new Bus(dimensions, weight, speed, cargo);
            case "TRUCK": return new Truck(dimensions, weight, speed, cargo);
            case "TRAILER": return new Trailer(dimensions, weight, speed, cargo);
            case "CAR_WITH_TRAILER": return new CarWithTrailer(dimensions, weight, speed, cargo);
            default: throw new IllegalArgumentException("Неизвестный тип: " + type);
        }
    }
    private static String type(Vehicle v) {
        if (v instanceof Car) return "CAR"; if (v instanceof Bus) return "BUS";
        if (v instanceof Truck) return "TRUCK"; if (v instanceof Trailer) return "TRAILER";
        if (v instanceof CarWithTrailer) return "CAR_WITH_TRAILER";
        throw new IllegalArgumentException("Неизвестный тип машины");
    }
    private static void check(String value) {
        if (value == null || value.trim().isEmpty() || value.contains("\t") || value.contains("\n") || value.contains("\r"))
            throw new IllegalArgumentException("Поле пустое или содержит табуляцию/перенос строки");
    }
    private static void write(Path file, List<String> lines) throws IOException {
        Files.createDirectories(file.getParent());
        Path temp = Files.createTempFile(file.getParent(), "db-", ".tmp");
        try { Files.write(temp, lines, StandardCharsets.UTF_8); Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING); }
        finally { Files.deleteIfExists(temp); }
    }
}
