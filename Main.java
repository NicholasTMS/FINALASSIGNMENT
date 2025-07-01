import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        Database.initialize(); // one time command to create tables if they dont exists
        SwingUtilities.invokeLater(() -> {
            EventController controller = new EventController();
            EventFormUI view = new EventFormUI(controller);
            view.setVisible(true);
        });
    }
}

// javac -cp ".;sqlite-jdbc-3.50.2.0.jar" *.java // compile command
// java -cp ".;sqlite-jdbc-3.50.2.0.jar" Main // run command