import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class OrganiserUI extends JFrame {
    private Organiser organiser;
    private EventController controller = new EventController(); // or inject your DAO
    private CardLayout cardLayout = new CardLayout();
    private JPanel    cards      = new JPanel(cardLayout);
    private EventFormPanelUI createPanel;

    public OrganiserUI(Organiser organiser) {
        setTitle("Organiser");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        
        // current logged in user
        this.organiser = organiser;
        // set event form panel
        createPanel = new EventFormPanelUI(this.controller);

        // Build navBar with buttons
        JPanel navBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        navBar.setBackground(new Color(0, 102, 204));


        JButton btnCreate     = new JButton("Create Event");
        JButton btnUpdate     = new JButton("Update Event");
        JButton btnDelete     = new JButton("Delete Event");
        JButton btnNotify     = new JButton("Notifications");

        navBar.add(btnCreate);
        navBar.add(btnUpdate);
        navBar.add(btnDelete);
        navBar.add(btnNotify);

        add(navBar, BorderLayout.NORTH);

        // Create each page as its own panel
        // create page panel styling
        JPanel createPage = new JPanel(new BorderLayout()); 
        JLabel header = new JLabel("Create Event Form", SwingConstants.CENTER);
        header.setFont(header.getFont().deriveFont(Font.BOLD, 24f));createPage.add(createPanel); 
        createPage.add(header,      BorderLayout.NORTH);
        createPage.add(createPanel, BorderLayout.CENTER);
        
        
        // Update page styling
        JPanel updatePage = new JPanel(new BorderLayout());
        JLabel updHeader = new JLabel("Update Events", SwingConstants.CENTER);
        updHeader.setFont(updHeader.getFont().deriveFont(Font.BOLD, 20f));
        updatePage.add(updHeader, BorderLayout.NORTH);
        updatePage.add(createScrollableEventGrid(), BorderLayout.CENTER);
                           


        // othe rpage styling
        JPanel deletePage       = new JPanel();                   deletePage.add(new JLabel("Delete Event Page"));
        JPanel notificationsPage= new JPanel();                   notificationsPage.add(new JLabel("Notifications Page"));

        // Add them to the 'cards' container with a key
        cards.add(createPage,        "CREATE");
        cards.add(updatePage,        "UPDATE");
        cards.add(deletePage,        "DELETE");
        cards.add(notificationsPage, "NOTIFY");

        add(cards, BorderLayout.CENTER);

        // Hook up buttons to flip the cards
        btnCreate    .addActionListener(e -> cardLayout.show(cards, "CREATE"));
        btnUpdate    .addActionListener(e -> cardLayout.show(cards, "UPDATE"));
        btnDelete    .addActionListener(e -> cardLayout.show(cards, "DELETE"));
        btnNotify    .addActionListener(e -> cardLayout.show(cards, "NOTIFY"));

        // Show the default page
        cardLayout.show(cards, "UPDATE");

        setVisible(true);
    }
    

    private JScrollPane createScrollableEventGrid() {
        List<Event> events = controller.loadAllEvents();

        // Use FlowLayout so cards keep their preferred size
        JPanel grid = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        grid.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        for (Event ev : events) {
            EventCard card = new EventCard(ev);
            card.setPreferredSize(new Dimension(200,200));
            grid.add(card);
        }

        JScrollPane scroll = new JScrollPane(grid);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }



    /**
     * Inner class: a single “card” showing picture + name, clickable to show details.
     */
    private class EventCard extends JPanel {
        EventCard(Event event) {
            setLayout(new BorderLayout(5,5));
            setBorder(BorderFactory.createLineBorder(Color.GRAY));
            setPreferredSize(new Dimension(20, 40));

            // Thumbnail
            byte[] img = event.getPictureData();
            JLabel pic = new JLabel();
            if (img != null) {
                ImageIcon ico = new ImageIcon(
                  new ImageIcon(img)
                    .getImage()
                    .getScaledInstance(180, 120, Image.SCALE_SMOOTH)
                );
                pic.setIcon(ico);
            }
            add(pic, BorderLayout.CENTER);

            // Name
            JLabel name = new JLabel(event.getEventName(), SwingConstants.CENTER);
            add(name, BorderLayout.SOUTH);

            // Click listener
            addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    showEventDetails(event);
                }
            });
        }
    }

    /** Pops up a dialog showing more info about this event. */
    private void showEventDetails(Event e) {
        String message =
            "Name: " + e.getEventName() + "\n" +
            "Venue: " + e.getVenue() + "\n" +
            "DateTime:  " + e.getDate() + "\n" +
            "Fee:   RM" + e.getRegisterationFee();
        JOptionPane.showMessageDialog(
            this,
            message,
            "Details for “" + e.getEventName() + "”",
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
        Organiser guest = new Organiser("OrganiserGuest","Organiser");  
        new OrganiserUI(guest);
    });
}
}


