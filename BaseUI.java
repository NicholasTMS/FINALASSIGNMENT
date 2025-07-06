import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.util.Map;

public abstract class BaseUI extends JFrame {
    protected final CardLayout cardLayout = new CardLayout();
    protected final JPanel cards = new JPanel(cardLayout);

    protected JButton btnCreate, btnView, btnNotify, btnLogout;

    public BaseUI(String title) {
        super(title);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        initNavBar();
        add(cards, BorderLayout.CENTER);
        setVisible(true);
    }

    private void initNavBar() {
        JPanel navBar = new JPanel(new BorderLayout());
        navBar.setBackground(new Color(0, 102, 204));

        JPanel leftNav = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        leftNav.setOpaque(false);
        btnCreate = new JButton("Create Event");
        btnView   = new JButton("View/Modify Events");
        btnNotify = new JButton("Notifications");
        leftNav.add(btnCreate);
        leftNav.add(btnView);
        leftNav.add(btnNotify);

        JPanel rightNav = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        rightNav.setOpaque(false);
        btnLogout = new JButton("Logout");
        rightNav.add(btnLogout);

        navBar.add(leftNav, BorderLayout.WEST);
        navBar.add(rightNav, BorderLayout.EAST);
        add(navBar, BorderLayout.NORTH);
    }

    protected void registerNavActions(Map<String, Runnable> actions) {
        btnCreate.addActionListener(e -> actions.get("CREATE").run());
        btnView.addActionListener(e -> actions.get("VIEW").run());
        btnNotify.addActionListener(e -> actions.get("NOTIFY").run());
        btnLogout.addActionListener(e -> actions.get("LOGOUT").run());
    }

    protected void addCard(String name, JPanel panel) {
        cards.add(panel, name);
    }

    protected void showCard(String name) {
        cardLayout.show(cards, name);
    }
}
