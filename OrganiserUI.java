import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Map;

public class OrganiserUI extends BaseUI implements Observer {
    private final EventController controller = new EventController();
    private final EventFormPanelUI formPanel;
    private final JPanel viewModifyPage;
    private User organiser;

    public OrganiserUI(User organiser) {
        super("Organiser");
        controller.registerObserver(this);
        this.organiser = organiser;
        formPanel = new EventFormPanelUI(controller, organiser);

        // Prepare cards
        addCard("CREATE_UPDATE", buildCreateUpdatePage());
        viewModifyPage = new JPanel(new BorderLayout());
        buildViewModifyPage();
        addCard("VIEW_MODIFY", viewModifyPage);
        addCard("NOTIFY", buildNotifyPage());

        registerNavActions(Map.of(
            "CREATE", () -> showCard("CREATE_UPDATE"),
            "VIEW",   () -> showCard("VIEW_MODIFY"),
            "NOTIFY", () -> showCard("NOTIFY"),
            "LOGOUT", () -> { dispose(); new LoginGUI(); }
        ));

        showCard("VIEW_MODIFY");
    }

    private JPanel buildCreateUpdatePage() {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel header = new JLabel("Create / Update Event", SwingConstants.CENTER);
        header.setFont(header.getFont().deriveFont(Font.BOLD, 24f));
        panel.add(header, BorderLayout.NORTH);
        panel.add(formPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildNotifyPage() {
        JPanel p = new JPanel();
        p.add(new JLabel("Notifications Page"));
        return p;
    }

    private void buildViewModifyPage() {
        viewModifyPage.removeAll();
        JLabel header = new JLabel("View / Modify Events", SwingConstants.CENTER);
        header.setFont(header.getFont().deriveFont(Font.BOLD, 20f));
        viewModifyPage.add(header, BorderLayout.NORTH);
        viewModifyPage.add(createScrollableEventGrid(), BorderLayout.CENTER);
        viewModifyPage.revalidate();
        viewModifyPage.repaint();
    }

    private JScrollPane createScrollableEventGrid() {
        List<Event> events = controller.loadAllEventsForOrganiser(organiser.getUsername());
        JPanel grid = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        grid.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        for (Event ev : events) {
            EventCard card = new EventCard(ev);
            card.setPreferredSize(new Dimension(200,240));
            grid.add(card);
        }
        JScrollPane scroll = new JScrollPane(grid);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    @Override public void update() {
        buildViewModifyPage();
        showCard("VIEW_MODIFY");
    }

    private class EventCard extends JPanel {
        EventCard(Event event) {
            setLayout(new BorderLayout(5,5));
            setBorder(BorderFactory.createLineBorder(Color.GRAY));
            setPreferredSize(new Dimension(200, 240));

            // Thumbnail
            JLabel pic = new JLabel();
            byte[] img = event.getPictureData();
            if (img != null) {
                ImageIcon ico = new ImageIcon(
                  new ImageIcon(img)
                    .getImage()
                    .getScaledInstance(180, 120, Image.SCALE_SMOOTH)
                );
                pic.setIcon(ico);
            }
            add(pic, BorderLayout.NORTH);

            // Name
            JLabel name = new JLabel(event.getEventName(), SwingConstants.CENTER);
            add(name, BorderLayout.CENTER);

            // Buttons panel
            JPanel buttonBar = new JPanel(new GridLayout(1,2,5,0));
            JButton btnUpd = new JButton("Update");
            JButton btnDel = new JButton("Delete");
            buttonBar.add(btnUpd);
            buttonBar.add(btnDel);
            add(buttonBar, BorderLayout.SOUTH);

            // Update action: load into form, switch to create/update card
            btnUpd.addActionListener(e -> {
                formPanel.loadEventForEdit(event);
                showCard("CREATE_UPDATE");
            });

            // Delete action: confirm, delete, then refresh view
            btnDel.addActionListener(e -> {
                int ans = JOptionPane.showConfirmDialog(
                  OrganiserUI.this,
                  "Delete “" + event.getEventName() + "”?",
                  "Confirm delete",
                  JOptionPane.YES_NO_OPTION
                );
                if (ans == JOptionPane.YES_OPTION) {
                    controller.deleteEvent(event.getEventID());
                }
            });

            // Clicking the card body shows details
            addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    showEventDetails(event);
                }
            });
        }
    }

    private void showEventDetails(Event e) {
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

    

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            User guest = new User("OrganiserGuest","1234","Organiser");
            new OrganiserUI(guest);
        });
    }
}



