import core.Group;
import model.CargoClass;
import model.User;
import model.Vehicle;
import model.freight.CarWithTrailer;
import model.freight.Trailer;
import model.freight.Truck;
import model.passenger.Bus;
import model.passenger.Car;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class DbHelper {
    private final Path dir;
    private final CRUD crud;

    public DbHelper(Path dir, CRUD crud) {
        this.dir = dir;
        this.crud = crud;
    }

    public void load() throws IOException {
        Files.createDirectories(dir);
        ArrayList<User> newUsers = new ArrayList<User>();
        ArrayList<Vehicle> newVehicles = new ArrayList<Vehicle>();
        Path userFile = dir.resolve("users.txt");
        Path vehicleFile = dir.resolve("vehicles.txt");

        if (Files.exists(userFile)) {
            List<String> lines = Files.readAllLines(userFile, StandardCharsets.UTF_8);
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line.trim().isEmpty()) continue;
                try {
                    String[] fields = line.split("\t", -1);
                    if (fields.length != 4) throw new IllegalArgumentException("ожидалось 4 поля");
                    long id = Long.parseLong(fields[0]);
                    User user = new User(id, fields[1], fields[2], Group.valueOf(fields[3]));
                    for (User old : newUsers) {
                        if (old.getId() == id) throw new IllegalArgumentException("повтор ID");
                        if (old.getLogin().equals(user.getLogin())) throw new IllegalArgumentException("повтор логина");
                    }
                    newUsers.add(user);
                } catch (IllegalArgumentException e) {
                    throw new IOException(userFile + ", строка " + (i + 1) + ": " + e.getMessage(), e);
                }
            }
        }

        if (Files.exists(vehicleFile)) {
            List<String> lines = Files.readAllLines(vehicleFile, StandardCharsets.UTF_8);
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line.trim().isEmpty()) continue;
                try {
                    String[] fields = line.split("\t", -1);
                    if (fields.length != 6) throw new IllegalArgumentException("ожидалось 6 полей");
                    long id = Long.parseLong(fields[0]);
                    if (id < 1) throw new IllegalArgumentException("неверный ID");
                    for (Vehicle old : newVehicles) {
                        if (old.getId() == id) throw new IllegalArgumentException("повтор ID");
                    }
                    Vehicle vehicle = CRUD.createVehicle(fields[1], Integer.parseInt(fields[2]),
                            Integer.parseInt(fields[3]), Integer.parseInt(fields[4]),
                            CargoClass.valueOf(fields[5]));
                    vehicle.setId(id);
                    newVehicles.add(vehicle);
                } catch (IllegalArgumentException e) {
                    throw new IOException(vehicleFile + ", строка " + (i + 1) + ": " + e.getMessage(), e);
                }
            }
        }
        crud.replaceAll(newUsers, newVehicles);
    }

    public void save() throws IOException {
        List<String> userLines = new ArrayList<String>();
        List<String> vehicleLines = new ArrayList<String>();
        for (User user : crud.users()) {
            userLines.add(user.getId() + "\t" + user.getLogin() + "\t"
                    + user.getPassword() + "\t" + user.getGroup().name());
        }
        for (Vehicle vehicle : crud.vehicles()) {
            vehicleLines.add(vehicle.getId() + "\t" + type(vehicle) + "\t"
                    + vehicle.getDimensions() + "\t" + vehicle.getWeight() + "\t"
                    + vehicle.getSpeed() + "\t" + vehicle.getCargoClass().name());
        }
        Files.write(dir.resolve("users.txt"), userLines, StandardCharsets.UTF_8);
        Files.write(dir.resolve("vehicles.txt"), vehicleLines, StandardCharsets.UTF_8);
    }

    public void saveCurrentUser(User user) throws IOException {
        List<String> lines = new ArrayList<String>();
        lines.add(user.getId() + "\t" + user.getLogin());
        Files.write(dir.resolve("current_user.txt"), lines, StandardCharsets.UTF_8);
    }

    public String loadCurrentUser() throws IOException {
        Path file = dir.resolve("current_user.txt");
        if (!Files.exists(file)) return "нет";
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        if (lines.size() != 1) throw new IOException("В current_user.txt должна быть одна запись");
        String[] fields = lines.get(0).split("\t", -1);
        if (fields.length != 2) throw new IOException("Некорректный current_user.txt");
        try {
            User user = crud.getUser(Long.parseLong(fields[0]));
            if (user != null && user.getLogin().equals(fields[1])) return user.getLogin();
            return "не найден среди пользователей";
        } catch (NumberFormatException e) {
            throw new IOException("Некорректный ID в current_user.txt", e);
        }
    }

    private static String type(Vehicle vehicle) {
        if (vehicle instanceof Car) return "CAR";
        if (vehicle instanceof Bus) return "BUS";
        if (vehicle instanceof Truck) return "TRUCK";
        if (vehicle instanceof Trailer) return "TRAILER";
        if (vehicle instanceof CarWithTrailer) return "CAR_WITH_TRAILER";
        throw new IllegalArgumentException("Неизвестный тип машины");
    }
}
