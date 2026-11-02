import core.*;
import model.*;
import service.Checkpoint;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Scanner;

public class Main {
    private final Scanner input = new Scanner(System.in);
    private final Path projectDir = findProjectDirectory();
    private final DB db = new DB(projectDir.resolve("data"));
    private Settings settings;
    private AppLog log;
    private User current;

    public static void main(String[] args) { new Main().run(); }

    private void run() {
        try {
            log = new AppLog(projectDir.resolve("logs").resolve("app.log"), false);
            settings = Settings.load(projectDir.resolve("settings.properties"));
            log = new AppLog(projectDir.resolve("logs").resolve("app.log"), settings.debug);
            db.load(); log.info("Чтение БД: users=" + db.users().size() + ", vehicles=" + db.vehicles().size());
            User configured = db.findUser(settings.login);
            if (configured == null) {
                db.addUser(settings.login, settings.password, settings.group);
                db.save(); log.info("Создан пользователь из настроек; запись БД");
            } else if (!configured.getPassword().equals(settings.password) || configured.getGroup() != settings.group) {
                db.changeUser(configured.getId(), settings.login, settings.password, settings.group);
                db.save(); log.info("Обновлён пользователь из настроек; запись БД");
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
        } catch (IOException | RuntimeException e) {
            System.err.println("Ошибка: " + e.getMessage());
            if (log != null) log.error(e.toString());
        } finally { if (log != null) log.info("Завершение программы" + (current == null ? "" : ": " + current.getLogin())); }
    }

    private static Path findProjectDirectory() {
        Path fromWorkingDirectory = locate(Paths.get("").toAbsolutePath());
        if (fromWorkingDirectory != null) return fromWorkingDirectory;
        try {
            Path classes = Paths.get(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            Path fromClasses = locate(classes);
            if (fromClasses != null) return fromClasses;
        } catch (Exception ignored) { }
        throw new IllegalStateException("Не найден laba4/settings.properties. Запустите программу из каталога проекта или его родительского каталога.");
    }

    private static Path locate(Path start) {
        for (Path dir = start; dir != null; dir = dir.getParent()) {
            if (Files.isRegularFile(dir.resolve("settings.properties"))) return dir;
            Path child = dir.resolve("laba4");
            if (Files.isRegularFile(child.resolve("settings.properties"))) return child;
        }
        return null;
    }

    private User login(String last) {
        for (int attempt = 0; attempt < 3; attempt++) {
            String name = ask("Логин" + (db.findUser(last) != null ? " [" + last + "]" : "") + ": ");
            if (name.isEmpty() && db.findUser(last) != null) name = last;
            String password = ask("Пароль: ");
            User user = db.findUser(name);
            if (user != null && user.getPassword().equals(password)) return user;
            System.out.println("Неверный логин или пароль."); log.error("Неудачная попытка входа: " + name);
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
                    case "3": { long id = db.addVehicle(readVehicle()); save("добавлена машина " + id); System.out.println("Добавлена машина ID " + id); break; }
                    case "4": { long id = number("ID: "); if (db.getVehicle(id) == null) throw new IllegalArgumentException("Машина не найдена"); db.changeVehicle(id, readVehicle()); save("изменена машина " + id); break; }
                    case "5": { long id = number("ID: "); db.deleteVehicle(id); save("удалена машина " + id); break; }
                    case "6": save("ручная запись"); break;
                    case "7": db.load(); log.info("Чтение БД: users=" + db.users().size() + ", vehicles=" + db.vehicles().size());
                        if (!sameSession()) { System.out.println("Текущая учётная запись изменена; повторный вход необходим."); return; }
                        System.out.println("Загружено пользователей: " + db.users().size() + ", машин: " + db.vehicles().size() + "; текущий пользователь: " + db.loadCurrentUser()); listVehicles(); break;
                    case "8": checkVehicle(); break;
                    case "9": ifRoot(() -> usersMenu()); break;
                    case "10": ifRoot(() -> { System.out.println("Отладка в настройках: " + settings.debug + "; машин: " + db.vehicles().size() + ", пользователей: " + db.users().size()); log.debug("Состояние: машин=" + db.vehicles().size() + ", пользователей=" + db.users().size()); }); break;
                    case "11": ifRoot(() -> autotests()); break;
                    default: System.out.println("Нет такого пункта.");
                }
            } catch (IOException | RuntimeException e) { System.out.println("Ошибка: " + e.getMessage()); log.error(e.toString()); }
        }
    }
    private boolean sameSession() { User u = db.getUser(current.getId()); return u != null && u.getLogin().equals(current.getLogin()) && u.getPassword().equals(current.getPassword()) && u.getGroup() == current.getGroup(); }
    private void ifRoot(Action action) throws IOException { if (current.getGroup() != Group.ROOT) throw new IllegalArgumentException("Недостаточно прав"); action.run(); }
    private interface Action { void run() throws IOException; }
    private void save(String reason) throws IOException { db.save(); log.info("Запись БД: " + reason); System.out.println("Данные сохранены."); }
    private void listVehicles() { if (db.vehicles().isEmpty()) System.out.println("Машин нет."); for (Map.Entry<Long, Vehicle> e : db.vehicles().entrySet()) System.out.println(e.getKey() + ": " + e.getValue()); log.debug("Выведен список машин"); }
    private void showVehicle() { long id = number("ID: "); Vehicle v = db.getVehicle(id); if (v == null) throw new IllegalArgumentException("Машина не найдена"); System.out.println(id + ": " + v); }
    private void checkVehicle() { long id = number("ID: "); Vehicle v = db.getVehicle(id); if (v == null) throw new IllegalArgumentException("Машина не найдена"); Checkpoint cp = new Checkpoint(); boolean pass = cp.Pass(v); System.out.println(pass ? "Проезд разрешён" : "Проезд запрещён: " + cp.getReason()); log.debug("КПП: машина=" + id + ", допуск=" + pass + ", причина=" + cp.getReason()); }
    private Vehicle readVehicle() {
        String type = ask("Тип (CAR, BUS, TRUCK, TRAILER, CAR_WITH_TRAILER): ");
        int dimensions = integer("Габарит, см: "), weight = integer("Масса, кг: "), speed = integer("Скорость, км/ч: ");
        CargoClass cargo = CargoClass.valueOf(ask("Груз (SAFE, FLAMMABLE, CHEMICALLY_DANGEROUS, EXPLOSIVE, SPECIAL_IMPORTANCE): ").toUpperCase());
        return DB.createVehicle(type, dimensions, weight, speed, cargo);
    }
    private void usersMenu() throws IOException {
        while (true) {
            System.out.println("\nПользователи: 1. Список  2. По ID  3. Добавить  4. Изменить  5. Удалить  0. Назад");
            try {
                switch (ask("Выбор: ")) {
                    case "0": return;
                    case "1": for (User u : db.users()) System.out.println(u); break;
                    case "2": { User u = db.getUser(number("ID: ")); System.out.println(u == null ? "Не найден" : u); break; }
                    case "3": { User u = db.addUser(ask("Логин: "), ask("Пароль: "), group()); save("добавлен пользователь " + u.getId()); break; }
                    case "4": { long id = number("ID: "); if (id == current.getId()) throw new IllegalArgumentException("Свою учётную запись нельзя изменить в текущем сеансе"); db.changeUser(id, ask("Новый логин: "), ask("Новый пароль: "), group()); save("изменён пользователь " + id); break; }
                    case "5": { long id = number("ID: "); if (id == current.getId()) throw new IllegalArgumentException("Нельзя удалить текущего пользователя"); db.deleteUser(id); save("удалён пользователь " + id); break; }
                    default: System.out.println("Нет такого пункта.");
                }
            } catch (IOException | RuntimeException e) { System.out.println("Ошибка: " + e.getMessage()); log.error(e.toString()); }
        }
    }
    private Group group() { return Group.valueOf(ask("Группа (root/user): ").toUpperCase()); }
    private String ask(String prompt) { System.out.print(prompt); if (!input.hasNextLine()) throw new IllegalStateException("Ввод завершён"); return input.nextLine().trim(); }
    private long number(String prompt) { return Long.parseLong(ask(prompt)); }
    private int integer(String prompt) { return Integer.parseInt(ask(prompt)); }
    private void autotests() {
        Checkpoint cp = new Checkpoint(); int passed = 0;
        passed += test("легковая машина в пределах норм", cp.Pass(DB.createVehicle("CAR", 470, 1800, 55, CargoClass.SAFE)));
        passed += test("превышение габарита", !cp.Pass(DB.createVehicle("CAR", 520, 1800, 55, CargoClass.SAFE)));
        passed += test("опасный груз на легковой машине", !cp.Pass(DB.createVehicle("CAR", 470, 1800, 20, CargoClass.FLAMMABLE)));
        passed += test("взрывчатый груз слишком быстро", !cp.Pass(DB.createVehicle("TRUCK", 1100, 11000, 25, CargoClass.EXPLOSIVE)));
        String result = "Автотесты: " + passed + "/4 пройдено"; System.out.println(result); log.info(result);
    }
    private int test(String name, boolean passed) { String result = "Тест " + name + ": " + (passed ? "OK" : "FAIL"); System.out.println(result); log.info(result); return passed ? 1 : 0; }
}
