import java.util.Scanner;

public class View {
    private Scanner scanner;

    public View(Scanner scanner) {
        this.scanner = scanner;
    }

    public int ViewInputDigit() {
        System.out.print("Input privat number: ");
        int privat_number = scanner.nextInt();
        return privat_number;
    }

    public String GetAnswerToGuessNumber(int current_number) {
        System.out.println("Your number > " + current_number + "?(yes/no)");
        String answer = scanner.next();
        return answer;
    }

    public void CorrectNumber(int correct_number) {
        System.out.println("Your number is " + correct_number);
    }

    public void ViewLie() {
        System.out.println("Don't lie");
    }
}
