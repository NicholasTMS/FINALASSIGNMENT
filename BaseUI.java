import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import java.util.HashMap;
import java.util.Map;

public abstract class BaseUI extends JFrame {
    protected final CardLayout cardLayout = new CardLayout();
    protected final JPanel cards = new JPanel(cardLayout);
    private final Map<String, JPanel> cardMap = new HashMap<>();


    protected JButton btnCreate, btnView, btnNotify, btnLogout, btnMyEvents;

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
        btnMyEvents = new JButton("My Events");
        leftNav.add(btnCreate);
        leftNav.add(btnView);
        leftNav.add(btnNotify);
        leftNav.add(btnMyEvents); 

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
        btnMyEvents.addActionListener(e -> actions.get("MY_EVENTS").run());
        btnLogout.addActionListener(e -> actions.get("LOGOUT").run());
    }

    protected void addCard(String name, JPanel panel) {
        cards.add(panel, name);
        cardMap.put(name, panel); // Add to map
    }

    protected JPanel getCard(String name) {
        return cardMap.get(name);
    }

    protected void showCard(String name) {
        cardLayout.show(cards, name);
    }

    protected void showEventDetails(Event e) {
        StringBuilder sb = new StringBuilder()
            .append("Event ID: ").append(e.getEventID()).append("\n")
            .append("Name: ").append(e.getEventName()).append("\n")
            .append("Venue: ").append(e.getVenue()).append("\n")
            .append("Date‑Time: ").append(e.getDate()).append("\n")
            .append("Capacity: ").append(e.getCapacity()).append("\n")
            .append("Registered: ").append(e.getTotalRegistered()).append("\n")
            .append("Base Fee: RM").append(e.getRegisterationFee()).append("\n")
            .append("Type: ").append(e.getEventType()).append("\n")
            .append("Organiser: ").append(e.getOrganiser()).append("\n");

        if (!e.getAvailableAdditionalServices().isEmpty()) {
            sb.append("Additional Services:\n");
            e.getAvailableAdditionalServices().forEach((s, c) ->
              sb.append(" • ").append(s).append(": RM").append(c).append("\n")
            );
            sb.append("\n");
        }
        if (!e.getAvailableDiscounts().isEmpty()) {
            sb.append("Discounts:\n");
            e.getAvailableDiscounts().forEach((d,v) ->
              sb.append(" • ").append(d).append(": -RM").append(v).append("\n")
            );
        }

        JOptionPane.showMessageDialog(
          this, sb.toString(),
          "Details for “" + e.getEventName() + "”",
          JOptionPane.INFORMATION_MESSAGE
        );
    }
}
