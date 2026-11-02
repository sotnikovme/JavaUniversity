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
            log.info("Чтение БД: users=" + crud.users().size() + ", vehicles=" + crud.vehicles().size());
            User configured = crud.findUser(settings.login);
            if (configured == null) {
                crud.addUser(settings.login, settings.password, settings.group);
                db.save();
                log.info("Создан пользователь из настроек; запись БД");
            } else if (!configured.getPassword().equals(settings.password) || configured.getGroup() != settings.group) {
                crud.changeUser(configured.getId(), settings.login, settings.password, settings.group);
                db.save();
                log.info("Обновлён пользователь из настроек; запись БД");
            }
            System.out.println("Добро пожаловать — " + settings.login);
            String last = db.loadCurrentUser();
            log.info("Чтение БД: current_user.txt, последний пользователь=" + last);
            System.out.println("Последний пользователь: " + last);
            current = login(last);
            if (current == null) return;
            db.saveCurrentUser(current);
            log.info("Старт программы: " + current.getLogin());
            log.info("Запись БД: current_user.txt, " + current.getLogin());
            System.out.println("Вход выполнен: " + current.getLogin() + " (" + current.getGroup() + ")");
            if (settings.autotests) autotests();
            menu();
        } catch (Exception e) {
            System.err.println("Ошибка: " + e.getMessage());
            if (log != null) log.error(e.toString());
        } finally {
            if (log != null) log.info("Завершение программы");
        }
    }

    private static Path findProjectDirectory() {
        Path workingDirectory = Paths.get("").toAbsolutePath();
        if (workingDirectory.resolve("settings.properties").toFile().exists()) return workingDirectory;
        Path labDirectory = workingDirectory.resolve("laba4");
        if (labDirectory.resolve("settings.properties").toFile().exists()) return labDirectory;
        throw new IllegalStateException("Запустите программу из каталога laba4 или корня репозитория.");
    }

    private User login(String last) {
        for (int attempt = 0; attempt < 3; attempt++) {
            String prompt = "Логин: ";
            if (crud.findUser(last) != null) prompt = "Логин [" + last + "]: ";
            String name = ask(prompt);
            if (name.isEmpty() && crud.findUser(last) != null) name = last;
            String password = ask("Пароль: ");
            User user = crud.findUser(name);
            if (user != null && user.getPassword().equals(password)) return user;
            System.out.println("Неверный логин или пароль.");
            log.error("Неудачная попытка входа: " + name);
        }
        return null;
    }

    private void menu() {
        while (true) {
            System.out.println("\n1. Список машин  2. Машина по ID  3. Добавить машину  4. Изменить машину  5. Удалить машину");
            System.out.println("6. Сохранить БД  7. Загрузить БД  8. Проверить машину на КПП");
            if (current.getGroup() == Group.ROOT) System.out.println("9. Пользователи  10. Отладка  11. Автотесты");
            System.out.println("0. Выход");
            try {
                switch (ask("Выбор: ")) {
                    case "0": return;
                    case "1": listVehicles(); break;
                    case "2": showVehicle(); break;
                    case "3": {
                        long id = crud.addVehicle(readVehicle());
                        save("добавлена машина " + id);
                        System.out.println("Добавлена машина ID " + id);
                        break;
                    }
                    case "4": {
                        long id = number("ID: ");
                        if (crud.getVehicle(id) == null) throw new IllegalArgumentException("Машина не найдена");
                        crud.changeVehicle(id, readVehicle());
                        save("изменена машина " + id);
                        break;
                    }
                    case "5": {
                        long id = number("ID: ");
                        crud.deleteVehicle(id);
                        save("удалена машина " + id);
                        break;
                    }
                    case "6": save("ручная запись"); break;
                    case "7":
                        db.load();
                        log.info("Чтение БД: users=" + crud.users().size() + ", vehicles=" + crud.vehicles().size());
                        if (!sameSession()) {
                            System.out.println("Текущая учётная запись изменена; повторный вход необходим.");
                            return;
                        }
                        System.out.println("Загружено пользователей: " + crud.users().size()
                                + ", машин: " + crud.vehicles().size()
                                + "; текущий пользователь: " + db.loadCurrentUser());
                        listVehicles();
                        break;
                    case "8": checkVehicle(); break;
                    case "9":
                        checkRoot();
                        usersMenu();
                        break;
                    case "10":
                        checkRoot();
                        System.out.println("Отладка в настройках: " + settings.debug
                                + "; машин: " + crud.vehicles().size() + ", пользователей: " + crud.users().size());
                        log.debug("Состояние: машин=" + crud.vehicles().size()
                                + ", пользователей=" + crud.users().size());
                        break;
                    case "11":
                        checkRoot();
                        autotests();
                        break;
                    default: System.out.println("Нет такого пункта.");
                }
            } catch (Exception e) {
                System.out.println("Ошибка: " + e.getMessage());
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
            throw new IllegalArgumentException("Недостаточно прав");
        }
    }
    private void save(String reason) throws IOException {
        db.save();
        log.info("Запись БД: " + reason);
        System.out.println("Данные сохранены.");
    }
    private void listVehicles() {
        if (crud.vehicles().isEmpty()) System.out.println("Машин нет.");
        for (Vehicle vehicle : crud.vehicles()) {
            System.out.println(vehicle.getId() + ": " + vehicle);
        }
        log.debug("Выведен список машин");
    }
    private void showVehicle() {
        long id = number("ID: ");
        Vehicle vehicle = crud.getVehicle(id);
        if (vehicle == null) throw new IllegalArgumentException("Машина не найдена");
        System.out.println(id + ": " + vehicle);
    }

    private void checkVehicle() {
        long id = number("ID: ");
        Vehicle vehicle = crud.getVehicle(id);
        if (vehicle == null) throw new IllegalArgumentException("Машина не найдена");
        Checkpoint checkpoint = new Checkpoint();
        boolean pass = checkpoint.Pass(vehicle);
        if (pass) {
            System.out.println("Проезд разрешён");
        } else {
            System.out.println("Проезд запрещён: " + checkpoint.getReason());
        }
        log.debug("КПП: машина=" + id + ", допуск=" + pass + ", причина=" + checkpoint.getReason());
    }
    private Vehicle readVehicle() {
        String type = ask("Тип (CAR, BUS, TRUCK, TRAILER, CAR_WITH_TRAILER): ");
        int dimensions = integer("Габарит, см: ");
        int weight = integer("Масса, кг: ");
        int speed = integer("Скорость, км/ч: ");
        CargoClass cargo = CargoClass.valueOf(ask("Груз (SAFE, FLAMMABLE, CHEMICALLY_DANGEROUS, EXPLOSIVE, SPECIAL_IMPORTANCE): ").toUpperCase());
        return CRUD.createVehicle(type, dimensions, weight, speed, cargo);
    }
    private void usersMenu() throws IOException {
        while (true) {
            System.out.println("\nПользователи: 1. Список  2. По ID  3. Добавить  4. Изменить  5. Удалить  0. Назад");
            try {
                switch (ask("Выбор: ")) {
                    case "0": return;
                    case "1":
                        for (User user : crud.users()) System.out.println(user);
                        break;
                    case "2": {
                        User user = crud.getUser(number("ID: "));
                        if (user == null) System.out.println("Не найден");
                        else System.out.println(user);
                        break;
                    }
                    case "3": {
                        User user = crud.addUser(ask("Логин: "), ask("Пароль: "), group());
                        save("добавлен пользователь " + user.getId());
                        break;
                    }
                    case "4": {
                        long id = number("ID: ");
                        if (id == current.getId()) {
                            throw new IllegalArgumentException("Свою учётную запись нельзя изменить в текущем сеансе");
                        }
                        crud.changeUser(id, ask("Новый логин: "), ask("Новый пароль: "), group());
                        save("изменён пользователь " + id);
                        break;
                    }
                    case "5": {
                        long id = number("ID: ");
                        if (id == current.getId()) throw new IllegalArgumentException("Нельзя удалить текущего пользователя");
                        crud.deleteUser(id);
                        save("удалён пользователь " + id);
                        break;
                    }
                    default: System.out.println("Нет такого пункта.");
                }
            } catch (Exception e) {
                System.out.println("Ошибка: " + e.getMessage());
                log.error(e.toString());
            }
        }
    }
    private Group group() {
        return Group.valueOf(ask("Группа (root/user): ").toUpperCase());
    }

    private String ask(String prompt) {
        System.out.print(prompt);
        if (!input.hasNextLine()) throw new IllegalStateException("Ввод завершён");
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
        passed += test("легковая машина в пределах норм", cp.Pass(CRUD.createVehicle("CAR", 470, 1800, 55, CargoClass.SAFE)));
        passed += test("превышение габарита", !cp.Pass(CRUD.createVehicle("CAR", 520, 1800, 55, CargoClass.SAFE)));
        passed += test("опасный груз на легковой машине", !cp.Pass(CRUD.createVehicle("CAR", 470, 1800, 20, CargoClass.FLAMMABLE)));
        passed += test("взрывчатый груз слишком быстро", !cp.Pass(CRUD.createVehicle("TRUCK", 1100, 11000, 25, CargoClass.EXPLOSIVE)));
        String result = "Автотесты: " + passed + "/4 пройдено";
        System.out.println(result);
        log.info(result);
    }
    private int test(String name, boolean passed) {
        String result;
        if (passed) result = "Тест " + name + ": OK";
        else result = "Тест " + name + ": FAIL";
        System.out.println(result);
        log.info(result);
        if (passed) return 1;
        return 0;
    }
}
