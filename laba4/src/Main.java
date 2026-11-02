import core.AppLog;
import core.Group;
import core.Settings;
import model.CargoClass;
import model.User;
import model.Vehicle;
import service.Checkpoint;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class Main {
    private final Scanner input = new Scanner(System.in);
    private final Path projectDir = findProjectDirectory();
    private final CRUD crud = new CRUD();
    private final DbHelper db = new DbHelper(projectDir.resolve("data"), crud);
    private Settings settings;
    private AppLog log;
    private User current;

    public static void main(String[] args) {
        Main program = new Main();
        program.run();
    }

    private void run() {
        try {
            settings = Settings.load(projectDir.resolve("settings.properties"));
            log = new AppLog(projectDir.resolve("logs").resolve("app.log"), settings.debug);
            db.load();
            log.info("Database loaded: users=" + crud.users().size() + ", vehicles=" + crud.vehicles().size());
            User configured = crud.findUser(settings.login);
            if (configured == null) {
                crud.addUser(settings.login, settings.password, settings.group);
                db.save();
                log.info("User created from settings; database saved");
            } else if (!configured.getPassword().equals(settings.password) || configured.getGroup() != settings.group) {
                crud.changeUser(configured.getId(), settings.login, settings.password, settings.group);
                db.save();
                log.info("User updated from settings; database saved");
            }
            System.out.println("Welcome, " + settings.login);
            String last = db.loadCurrentUser();
            log.info("Loaded current_user.txt, last user=" + last);
            System.out.println("Last user: " + last);
            current = login(last);
            if (current == null) return;
            db.saveCurrentUser(current);
            log.info("Program started: " + current.getLogin());
            log.info("Saved current_user.txt: " + current.getLogin());
            System.out.println("Logged in as: " + current.getLogin() + " (" + current.getGroup() + ")");
            if (settings.autotests) autotests();
            menu();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            if (log != null) log.error(e.toString());
        } finally {
            if (log != null) log.info("Program finished");
        }
    }

    private static Path findProjectDirectory() {
        Path workingDirectory = Paths.get("").toAbsolutePath();
        if (workingDirectory.resolve("settings.properties").toFile().exists()) return workingDirectory;
        Path labDirectory = workingDirectory.resolve("laba4");
        if (labDirectory.resolve("settings.properties").toFile().exists()) return labDirectory;
        throw new IllegalStateException("Run the program from the laba4 directory or the repository root.");
    }

    private User login(String last) {
        for (int attempt = 0; attempt < 3; attempt++) {
            String prompt = "Login: ";
            if (crud.findUser(last) != null) prompt = "Login [" + last + "]: ";
            String name = ask(prompt);
            if (name.isEmpty() && crud.findUser(last) != null) name = last;
            String password = ask("Password: ");
            User user = crud.findUser(name);
            if (user != null && user.getPassword().equals(password)) return user;
            System.out.println("Incorrect login or password.");
            log.error("Failed login attempt: " + name);
        }
        return null;
    }

    private void menu() {
        while (true) {
            System.out.println("\n1. List vehicles  2. Vehicle by ID  3. Add vehicle  4. Edit vehicle  5. Delete vehicle");
            System.out.println("6. Save data  7. Load data  8. Check vehicle at checkpoint");
            if (current.getGroup() == Group.ROOT) System.out.println("9. Users  10. Debug info  11. Self-tests");
            System.out.println("0. Exit");
            try {
                switch (ask("Choice: ")) {
                    case "0": return;
                    case "1": listVehicles(); break;
                    case "2": showVehicle(); break;
                    case "3": {
                        long id = crud.addVehicle(readVehicle());
                        save("added vehicle " + id);
                        System.out.println("Added vehicle ID " + id);
                        break;
                    }
                    case "4": {
                        long id = number("ID: ");
                        if (crud.getVehicle(id) == null) throw new IllegalArgumentException("Vehicle not found");
                        crud.changeVehicle(id, readVehicle());
                        save("updated vehicle " + id);
                        break;
                    }
                    case "5": {
                        long id = number("ID: ");
                        crud.deleteVehicle(id);
                        save("deleted vehicle " + id);
                        break;
                    }
                    case "6": save("manual save"); break;
                    case "7":
                        db.load();
                        log.info("Database loaded: users=" + crud.users().size() + ", vehicles=" + crud.vehicles().size());
                        if (!sameSession()) {
                            System.out.println("The current account has changed; please log in again.");
                            return;
                        }
                        System.out.println("Loaded users: " + crud.users().size()
                                + ", vehicles: " + crud.vehicles().size()
                                + "; current user: " + db.loadCurrentUser());
                        listVehicles();
                        break;
                    case "8": checkVehicle(); break;
                    case "9":
                        checkRoot();
                        usersMenu();
                        break;
                    case "10":
                        checkRoot();
                        System.out.println("Debug enabled: " + settings.debug
                                + "; vehicles: " + crud.vehicles().size() + ", users: " + crud.users().size());
                        log.debug("State: vehicles=" + crud.vehicles().size()
                                + ", users=" + crud.users().size());
                        break;
                    case "11":
                        checkRoot();
                        autotests();
                        break;
                    default: System.out.println("Invalid menu choice.");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
                log.error(e.toString());
            }
        }
    }
    private boolean sameSession() {
        User user = crud.getUser(current.getId());
        if (user == null) return false;
        if (!user.getLogin().equals(current.getLogin())) return false;
        if (!user.getPassword().equals(current.getPassword())) return false;
        return user.getGroup() == current.getGroup();
    }
    private void checkRoot() {
        if (current.getGroup() != Group.ROOT) {
            throw new IllegalArgumentException("Access denied");
        }
    }
    private void save(String reason) throws IOException {
        db.save();
        log.info("Database saved: " + reason);
        System.out.println("Data saved.");
    }
    private void listVehicles() {
        if (crud.vehicles().isEmpty()) System.out.println("No vehicles.");
        for (Vehicle vehicle : crud.vehicles()) {
            System.out.println(vehicle.getId() + ": " + vehicle);
        }
        log.debug("Displayed vehicle list");
    }
    private void showVehicle() {
        long id = number("ID: ");
        Vehicle vehicle = crud.getVehicle(id);
        if (vehicle == null) throw new IllegalArgumentException("Vehicle not found");
        System.out.println(id + ": " + vehicle);
    }

    private void checkVehicle() {
        long id = number("ID: ");
        Vehicle vehicle = crud.getVehicle(id);
        if (vehicle == null) throw new IllegalArgumentException("Vehicle not found");
        Checkpoint checkpoint = new Checkpoint();
        boolean pass = checkpoint.Pass(vehicle);
        if (pass) {
            System.out.println("Passage allowed");
        } else {
            System.out.println("Passage denied: " + checkpoint.getReason());
        }
        log.debug("Checkpoint: vehicle=" + id + ", allowed=" + pass + ", reason=" + checkpoint.getReason());
    }
    private Vehicle readVehicle() {
        String type = ask("Type (CAR, BUS, TRUCK, TRAILER, CAR_WITH_TRAILER): ");
        int dimensions = integer("Dimensions, cm: ");
        int weight = integer("Weight, kg: ");
        int speed = integer("Speed, km/h: ");
        CargoClass cargo = CargoClass.valueOf(ask("Cargo (SAFE, FLAMMABLE, CHEMICALLY_DANGEROUS, EXPLOSIVE, SPECIAL_IMPORTANCE): ").toUpperCase());
        return CRUD.createVehicle(type, dimensions, weight, speed, cargo);
    }
    private void usersMenu() throws IOException {
        while (true) {
            System.out.println("\nUsers: 1. List  2. By ID  3. Add  4. Edit  5. Delete  0. Back");
            try {
                switch (ask("Choice: ")) {
                    case "0": return;
                    case "1":
                        for (User user : crud.users()) System.out.println(user);
                        break;
                    case "2": {
                        User user = crud.getUser(number("ID: "));
                        if (user == null) System.out.println("Not found");
                        else System.out.println(user);
                        break;
                    }
                    case "3": {
                        User user = crud.addUser(ask("Login: "), ask("Password: "), group());
                        save("added user " + user.getId());
                        break;
                    }
                    case "4": {
                        long id = number("ID: ");
                        if (id == current.getId()) {
                            throw new IllegalArgumentException("You cannot edit your own account during this session");
                        }
                        crud.changeUser(id, ask("New login: "), ask("New password: "), group());
                        save("updated user " + id);
                        break;
                    }
                    case "5": {
                        long id = number("ID: ");
                        if (id == current.getId()) throw new IllegalArgumentException("You cannot delete the current user");
                        crud.deleteUser(id);
                        save("deleted user " + id);
                        break;
                    }
                    default: System.out.println("Invalid menu choice.");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
                log.error(e.toString());
            }
        }
    }
    private Group group() {
        return Group.valueOf(ask("Group (root/user): ").toUpperCase());
    }

    private String ask(String prompt) {
        System.out.print(prompt);
        if (!input.hasNextLine()) throw new IllegalStateException("Input ended");
        return input.nextLine().trim();
    }

    private long number(String prompt) {
        return Long.parseLong(ask(prompt));
    }

    private int integer(String prompt) {
        return Integer.parseInt(ask(prompt));
    }
    private void autotests() {
        Checkpoint cp = new Checkpoint();
        int passed = 0;
        passed += test("car within limits", cp.Pass(CRUD.createVehicle("CAR", 470, 1800, 55, CargoClass.SAFE)));
        passed += test("dimensions exceeded", !cp.Pass(CRUD.createVehicle("CAR", 520, 1800, 55, CargoClass.SAFE)));
        passed += test("dangerous cargo in a car", !cp.Pass(CRUD.createVehicle("CAR", 470, 1800, 20, CargoClass.FLAMMABLE)));
        passed += test("explosive cargo too fast", !cp.Pass(CRUD.createVehicle("TRUCK", 1100, 11000, 25, CargoClass.EXPLOSIVE)));
        String result = "Self-tests: " + passed + "/4 passed";
        System.out.println(result);
        log.info(result);
    }
    private int test(String name, boolean passed) {
        String result;
        if (passed) result = "Test " + name + ": OK";
        else result = "Test " + name + ": FAIL";
        System.out.println(result);
        log.info(result);
        if (passed) return 1;
        return 0;
    }
}
