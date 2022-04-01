import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        Model gameModel = new Model();
        Controller gameController = new Controller(gameModel);
        JFrame game = new JFrame();

        game.setTitle("2048");
        game.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        game.setSize(450, 500);
        game.setResizable(false);
        game.add(gameController.getView());
        game.setLocationRelativeTo(null);
        game.setVisible(true);
    }
}