
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JOptionPane;
import javax.swing.ImageIcon;
import javax.swing.BorderFactory;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.util.*;
import java.awt.Dimension;


public class ParticipantUI extends BaseUI implements Observer {
    private final EventController controller = new EventController();
    private final JPanel browsePage;

    public ParticipantUI(User participant) {
        super("Participant");
        controller.registerObserver(this);

        // We no longer add a CREATE_UPDATE card
        browsePage = new JPanel(new BorderLayout());
        addCard("VIEW_MODIFY", browsePage);
        addCard("NOTIFY", buildNotifyPage());

        btnCreate.setVisible(false);

        btnView.setText("Browse Events");
        btnNotify.setText("Messages");
        btnLogout.setText("Sign Out");

        registerNavActions(Map.of(
            "VIEW",   () -> showCard("VIEW_MODIFY"),
            "NOTIFY", () -> showCard("NOTIFY"),
            "LOGOUT", () -> { dispose(); new LoginGUI(); }
        ));

        showCard("VIEW_MODIFY");
        buildBrowsePage();
    }

    private JPanel buildNotifyPage() {
        JPanel p = new JPanel();
        p.add(new JLabel("Your Notification Center"));
        return p;
    }

    private void buildBrowsePage() {
        browsePage.removeAll();

        JLabel header = new JLabel("Available Events", SwingConstants.CENTER);
        header.setFont(header.getFont().deriveFont(Font.BOLD, 20f));
        browsePage.add(header, BorderLayout.NORTH);

        browsePage.add(createScrollableEventGrid(), BorderLayout.CENTER);

        browsePage.revalidate();
        browsePage.repaint();
    }

    private JScrollPane createScrollableEventGrid() {
       List<Event> events = controller.loadAllEvents();

       JPanel grid = new JPanel(new GridLayout(0, 5, 10, 10));
        // 0 rows = “as many rows as needed,” 3 columns
        grid.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        for (Event ev : events) {
            EventCard card = new EventCard(ev);
            grid.add(card);
        }

        JScrollPane scroll = new JScrollPane(grid);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    @Override
    public void update() {
        buildBrowsePage();
        showCard("VIEW_MODIFY");
    }

    private class EventCard extends JPanel {
        EventCard(Event ev) {
            setLayout(new BorderLayout(5,5));
            setBorder(BorderFactory.createLineBorder(Color.GRAY));

            JLabel pic = new JLabel();
            byte[] img = ev.getPictureData();
            if (img != null) {
                ImageIcon ico = new ImageIcon(
                    new ImageIcon(img).getImage()
                                     .getScaledInstance(180,120,Image.SCALE_SMOOTH)
                );
                pic.setIcon(ico);
            }
            add(pic, BorderLayout.NORTH);

            add(new JLabel(ev.getEventName(), SwingConstants.CENTER),
                BorderLayout.CENTER);

            JPanel bar = new JPanel(new GridLayout(1,2,5,0));
            JButton btnReg  = new JButton("Register");
            JButton btnInfo = new JButton("Details");
            bar.add(btnReg);
            bar.add(btnInfo);
            add(bar, BorderLayout.SOUTH);

            btnReg.addActionListener(e -> {
                //controller.register(ev.getEventID());
            });
            btnInfo.addActionListener(e -> {
                JOptionPane.showMessageDialog(
                    ParticipantUI.this,
                    "Details for " + ev.getEventName()
                );
            });
        }
    }
}

