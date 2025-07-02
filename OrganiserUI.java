import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class OrganiserUI extends JFrame implements Observer{
    private final User organiser;
    private final EventController controller = new EventController();
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cards = new JPanel(cardLayout);
    private final EventFormPanelUI createUpdatePanel;
    private final JPanel viewModifyPage;


    public OrganiserUI(User organiser) {
        this.organiser = organiser;
        setTitle("Organiser");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Event form panel (create / update)
        createUpdatePanel = new EventFormPanelUI(controller, this.organiser);

        controller.registerObserver(this);

        // Top navigation bar
        JPanel navBar = new JPanel(new BorderLayout());
        navBar.setBackground(new Color(0, 102, 204));

        // Left panel for main nav buttons
        JPanel leftNav = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        leftNav.setOpaque(false);  // Inherit background
        JButton btnCreate = new JButton("Create Event");
        JButton btnView   = new JButton("View/Modify Events");
        JButton btnNotify = new JButton("Notifications");
        leftNav.add(btnCreate);
        leftNav.add(btnView);
        leftNav.add(btnNotify);

        // Right panel for logout button
        JPanel rightNav = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        rightNav.setOpaque(false);
        JButton btnLogout = new JButton("Logout");
        rightNav.add(btnLogout);
        btnLogout.addActionListener(e -> {
            OrganiserUI.this.dispose();
            new LoginGUI();
        });


        // Add both to the nav bar
        navBar.add(leftNav, BorderLayout.WEST);
        navBar.add(rightNav, BorderLayout.EAST);
        add(navBar, BorderLayout.NORTH);

        // Build create/update page
        JPanel createUpdatePage = new JPanel(new BorderLayout());
        JLabel header = new JLabel("Create / Update Event", SwingConstants.CENTER);
        header.setFont(header.getFont().deriveFont(Font.BOLD, 24f));
        createUpdatePage.add(header, BorderLayout.NORTH);
        createUpdatePage.add(createUpdatePanel, BorderLayout.CENTER);

        // Build view/modify page once
        viewModifyPage = new JPanel(new BorderLayout());
        buildViewModifyPage();

        // Notifications page placeholder
        JPanel notificationsPage = new JPanel();
        notificationsPage.add(new JLabel("Notifications Page"));

        // Add pages to card deck
        cards.add(createUpdatePage, "CREATE_UPDATE");
        cards.add(viewModifyPage,   "VIEW_MODIFY");
        cards.add(notificationsPage,"NOTIFY");
        add(cards, BorderLayout.CENTER);

        // Button actions to switch cards
        btnCreate.addActionListener(e -> cardLayout.show(cards, "CREATE_UPDATE"));
        btnView  .addActionListener(e -> cardLayout.show(cards, "VIEW_MODIFY"));
        btnNotify.addActionListener(e -> cardLayout.show(cards, "NOTIFY"));

        // Show default
        cardLayout.show(cards, "VIEW_MODIFY");

        setVisible(true);
    }

    @Override
    public void update() {
        refreshGrid();
    }
    /** (Re)builds the “View / Modify” page from fresh DB data. */
    private void buildViewModifyPage() {
        viewModifyPage.removeAll();

        JLabel updHeader = new JLabel("View / Modify Events", SwingConstants.CENTER);
        updHeader.setFont(updHeader.getFont().deriveFont(Font.BOLD, 20f));
        viewModifyPage.add(updHeader, BorderLayout.NORTH);
        viewModifyPage.add(createScrollableEventGrid(), BorderLayout.CENTER);

        viewModifyPage.revalidate();
        viewModifyPage.repaint();
    }

    /** Refreshes the grid after a change, then shows the view/modify card. */
    private void refreshGrid() {
        buildViewModifyPage();
        cardLayout.show(cards, "VIEW_MODIFY");
    }

    private JScrollPane createScrollableEventGrid() {
        List<Event> events = controller.loadAllEventsForOrganiser(organiser.getUsername());
        JPanel grid = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        grid.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        for (Event ev : events) {
            EventCard card = new EventCard(ev);
            card.setPreferredSize(new Dimension(200, 240));
            grid.add(card);
        }

        JScrollPane scroll = new JScrollPane(grid);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
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
                createUpdatePanel.loadEventForEdit(event);
                cardLayout.show(cards, "CREATE_UPDATE");
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



