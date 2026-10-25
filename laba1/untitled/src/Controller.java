// import src.View;
// import src.Model;

public class Controller {
    private View view;
    private Model model;

    String IS_YES = "yes";
    String IS_NO = "no";
    String IS_SHORT_YES = "y";
    String IS_SHORT_NO = "n";

    public Controller(View view, Model model) {
        this.view = view;
        this.model = model;
    }

    public void StartGame() {
        while (true) {
            if (model.mid == model.private_number) {
                view.CorrectNumber(model.mid);
                break;
            }
            String answer = view.GetAnswerToGuessNumber(model.mid);

            if (answer.equals(IS_YES) || answer.equals(IS_SHORT_YES)) {

                if (model.CheckTrustLowerHigher(model.mid, model.private_number)) {
                    model.ChangeLowToMid();
                } else {
                    view.ViewLie();
                }

            }

            else if (answer.equals(IS_NO) || answer.equals(IS_SHORT_NO)) {

                if (model.CheckTrustLowerHigher(model.private_number, model.mid)) {
                    model.ChangeHighToMid();
                } else {
                    view.ViewLie();

                }
            }
        }
    }
}
