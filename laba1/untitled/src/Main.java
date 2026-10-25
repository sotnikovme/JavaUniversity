import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        View view = new View(scanner);
        int privat_number = view.ViewInputDigit();

        Model model = new Model(privat_number);

        Controller controller = new Controller(view, model);
        controller.StartGame();

        scanner.close();
    }
}
