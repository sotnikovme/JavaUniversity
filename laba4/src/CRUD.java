import core.Group;
import model.CargoClass;
import model.User;
import model.Vehicle;
import model.freight.CarWithTrailer;
import model.freight.Trailer;
import model.freight.Truck;
import model.passenger.Bus;
import model.passenger.Car;

import java.util.ArrayList;

public class CRUD {
    private final ArrayList<User> users = new ArrayList<User>();
    private final ArrayList<Vehicle> vehicles = new ArrayList<Vehicle>();
    private long nextUser = 1;
    private long nextVehicle = 1;

    public ArrayList<User> users() { return users; }
    public ArrayList<Vehicle> vehicles() { return vehicles; }

    public User getUser(long id) {
        for (User user : users) {
            if (user.getId() == id) return user;
        }
        return null;
    }

    public Vehicle getVehicle(long id) {
        for (Vehicle vehicle : vehicles) {
            if (vehicle.getId() == id) return vehicle;
        }
        return null;
    }

    public User findUser(String login) {
        for (User user : users) {
            if (user.getLogin().equals(login)) return user;
        }
        return null;
    }

    public User addUser(String login, String password, Group group) {
        check(login);
        check(password);
        if (findUser(login) != null) throw new IllegalArgumentException("Логин уже существует");
        User user = new User(nextUser, login, password, group);
        nextUser++;
        users.add(user);
        return user;
    }

    public void changeUser(long id, String login, String password, Group group) {
        if (getUser(id) == null) throw new IllegalArgumentException("Пользователь не найден");
        check(login);
        check(password);
        User other = findUser(login);
        if (other != null && other.getId() != id) throw new IllegalArgumentException("Логин уже существует");
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getId() == id) {
                users.set(i, new User(id, login, password, group));
                return;
            }
        }
    }

    public void deleteUser(long id) {
        User user = getUser(id);
        if (user == null) throw new IllegalArgumentException("Пользователь не найден");
        users.remove(user);
    }

    public long addVehicle(Vehicle vehicle) {
        vehicle.setId(nextVehicle);
        nextVehicle++;
        vehicles.add(vehicle);
        return vehicle.getId();
    }

    public void changeVehicle(long id, Vehicle vehicle) {
        for (int i = 0; i < vehicles.size(); i++) {
            if (vehicles.get(i).getId() == id) {
                vehicle.setId(id);
                vehicles.set(i, vehicle);
                return;
            }
        }
        throw new IllegalArgumentException("Машина не найдена");
    }

    public void deleteVehicle(long id) {
        Vehicle vehicle = getVehicle(id);
        if (vehicle == null) throw new IllegalArgumentException("Машина не найдена");
        vehicles.remove(vehicle);
    }

    public static Vehicle createVehicle(String type, int dimensions, int weight, int speed, CargoClass cargo) {
        if (dimensions <= 0 || weight <= 0 || speed < 0) {
            throw new IllegalArgumentException("Габарит и масса должны быть положительными, скорость неотрицательной");
        }
        switch (type.toUpperCase()) {
            case "CAR": return new Car(dimensions, weight, speed, cargo);
            case "BUS": return new Bus(dimensions, weight, speed, cargo);
            case "TRUCK": return new Truck(dimensions, weight, speed, cargo);
            case "TRAILER": return new Trailer(dimensions, weight, speed, cargo);
            case "CAR_WITH_TRAILER": return new CarWithTrailer(dimensions, weight, speed, cargo);
            default: throw new IllegalArgumentException("Неизвестный тип: " + type);
        }
    }

    void replaceAll(ArrayList<User> loadedUsers, ArrayList<Vehicle> loadedVehicles) {
        users.clear();
        users.addAll(loadedUsers);
        vehicles.clear();
        vehicles.addAll(loadedVehicles);
        nextUser = 1;
        for (User user : users) {
            if (user.getId() >= nextUser) nextUser = user.getId() + 1;
        }
        nextVehicle = 1;
        for (Vehicle vehicle : vehicles) {
            if (vehicle.getId() >= nextVehicle) nextVehicle = vehicle.getId() + 1;
        }
    }

    private static void check(String value) {
        if (value == null || value.trim().isEmpty() || value.contains("\t")
                || value.contains("\n") || value.contains("\r")) {
            throw new IllegalArgumentException("Поле пустое или содержит табуляцию/перенос строки");
        }
    }
}
