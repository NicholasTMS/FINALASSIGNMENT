
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JOptionPane;
import javax.swing.ImageIcon;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.util.*;
import java.awt.Dimension;
import javax.swing.Box;



public class ParticipantUI extends BaseUI implements Observer {
    private final EventController controller = new EventController();
    private final RegistrationController regController = new RegistrationController(); 
    private final JPanel browsePage;
    private final JPanel registerPage;
    private Event selectedEvent;
    private User participant;

    public ParticipantUI(User participant) {
        super("Participant");
        regController.registerObserver(this);
        this.participant = participant;

        // We no longer add a CREATE_UPDATE card
        browsePage = new JPanel(new BorderLayout());
        addCard("VIEW_MODIFY", browsePage);
        addCard("NOTIFY", buildNotifyPage());
        registerPage = new JPanel(new BorderLayout());
        addCard("REGISTER", registerPage);
        JPanel myEventsPage = new JPanel(new BorderLayout());
        addCard("MY_EVENTS", myEventsPage);


        btnCreate.setVisible(false);

        btnView.setText("Browse Events");
        btnNotify.setText("Messages");
        btnLogout.setText("Sign Out");

        registerNavActions(Map.of(
            "VIEW",   () -> showCard("VIEW_MODIFY"),
            "NOTIFY", () -> showCard("NOTIFY"),
            "MY_EVENTS", () -> { buildMyEventsPage(); showCard("MY_EVENTS"); },
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

    private void buildRegisterPage() {
        registerPage.removeAll();

        // --- Top: Event details ---
        JPanel details = new JPanel();
        details.setLayout(new BoxLayout(details, BoxLayout.Y_AXIS));
        details.setBorder(BorderFactory.createTitledBorder("Event Details"));
        details.add(new JLabel("Name: "     + selectedEvent.getEventName()));
        details.add(new JLabel("Venue: "    + selectedEvent.getVenue()));
        details.add(new JLabel("Date‑Time: "+ selectedEvent.getDate()));
        details.add(new JLabel("Capacity: " + selectedEvent.getCapacity()));
        details.add(new JLabel("Fee: RM"    + selectedEvent.getRegisterationFee()));
        registerPage.add(details, BorderLayout.NORTH);

        // --- Center: Registration form + dynamic services ---
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBorder(BorderFactory.createTitledBorder("Your Registration"));

        // 1) Ticket quantity as free‑form text field
        JPanel qtyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        qtyPanel.add(new JLabel("# of Tickets:"));
        JTextField tfTickets = new JTextField(5);
        tfTickets.setText("1");
        qtyPanel.add(tfTickets);
        center.add(qtyPanel);

        // 2) Available services checkboxes
        center.add(new JLabel("Additional Services (per person):"));
        Map<AdditionalServices, Double> services = selectedEvent.getAvailableAdditionalServices();
        List<JCheckBox> serviceBoxes = new ArrayList<>();
        for (var entry : services.entrySet()) {
            JCheckBox box = new JCheckBox(
                entry.getKey().name() + " (RM" + entry.getValue() + ")"
            );
            serviceBoxes.add(box);
            center.add(box);
        }

        registerPage.add(center, BorderLayout.CENTER);

        // --- Bottom: Buttons ---
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT,10,10));
        JButton btnBack   = new JButton("Back");
        JButton btnSubmit = new JButton("Submit");
        buttons.add(btnBack);
        buttons.add(btnSubmit);
        registerPage.add(buttons, BorderLayout.SOUTH);

        btnBack.addActionListener(e -> showCard("VIEW_MODIFY"));

        btnSubmit.addActionListener(e -> {
            // parse tickets
            int tickets;
            try {
                tickets = Integer.parseInt(tfTickets.getText().trim());
                if (tickets <= 0 || tickets + selectedEvent.getTotalRegistered() > selectedEvent.getCapacity()) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(
                    this,
                    "Invalid ticket amount. Please ensure it is more than 0 but less than the capacity + total registered",
                    "Invalid Input",
                    JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            // sum selected services
            double svcSumPerPerson = 0;
            for (int i = 0; i < serviceBoxes.size(); i++) {
                if (serviceBoxes.get(i).isSelected()) {
                    svcSumPerPerson += services.values().toArray(new Double[0])[i];
                }
            }

            double baseFee    = selectedEvent.getRegisterationFee();
            double netPerPerson = baseFee + svcSumPerPerson;

            // auto‑compute discount per person
            int alreadyRegistered = selectedEvent.getTotalRegistered();
            double earlyBirdDisc = 0, groupDisc = 0;
            if (alreadyRegistered < 10) {
                earlyBirdDisc = 0.10 * netPerPerson;   // 10%
            }
            if (tickets > 5) {
                groupDisc = 0.05 * netPerPerson;       // 5%
            }
            // pick the larger discount
            double perPersonDiscount = Math.max(earlyBirdDisc, groupDisc);

            double totalServices = svcSumPerPerson * tickets;
            double totalDiscount = perPersonDiscount * tickets;
            double totalPrice    = (netPerPerson - perPersonDiscount) * tickets;

            // call controller
            regController.registerParticipant(
                selectedEvent.getEventID(),
                participant.getUsername(),
                tickets,
                totalServices,
                totalDiscount,
                totalPrice
            );

            JOptionPane.showMessageDialog(
                this,
                String.format("Registered! Total Amount: RM%.2f", totalPrice),
                "Success", JOptionPane.INFORMATION_MESSAGE
            );
            showCard("VIEW_MODIFY");
        });

        registerPage.revalidate();
        registerPage.repaint();
    }

    private void buildMyEventsPage() {
        JPanel myEventsPage = getCard("MY_EVENTS");
        myEventsPage.removeAll();

        JLabel header = new JLabel("Your Registered Events", SwingConstants.CENTER);
        header.setFont(header.getFont().deriveFont(Font.BOLD, 20f));
        myEventsPage.add(header, BorderLayout.NORTH);

        JPanel grid = new JPanel();
        grid.setLayout(new BoxLayout(grid, BoxLayout.Y_AXIS));
        grid.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        List<Registration> registrations = new RegistrationController()
            .loadRegistrationsByUser(participant.getUsername());

        if (registrations.isEmpty()) {
            JLabel emptyMsg = new JLabel("You haven’t registered for any events yet.");
            emptyMsg.setFont(emptyMsg.getFont().deriveFont(Font.PLAIN, 16f));
            grid.add(emptyMsg);
        } else {
            EventController evController = new EventController();
            for (Registration reg : registrations) {
                Event ev = evController.loadEventById(reg.getEventId());
                JPanel panel = new JPanel(new BorderLayout());
                panel.setBorder(BorderFactory.createTitledBorder(ev.getEventName()));

                String info = "<html>" +
                    "Venue: " + ev.getVenue() + "<br>" +
                    "Date: " + ev.getDate() + "<br>" +
                    "Tickets: " + reg.getTickets() + "<br>" +
                    "Service Cost: RM " + String.format("%.2f", reg.getServicesCost()) + "<br>" +
                    "Discount Applied: -RM " + String.format("%.2f", reg.getDiscountAmount()) + "<br>" +
                    "<b>Total Paid: RM " + String.format("%.2f", reg.getTotalPrice()) + "</b>" +
                    "</html>";

                panel.add(new JLabel(info), BorderLayout.CENTER);
                grid.add(panel);
                grid.add(Box.createVerticalStrut(10)); // spacing between cards
            }
        }

        JScrollPane scroll = new JScrollPane(grid);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        myEventsPage.add(scroll, BorderLayout.CENTER);

        myEventsPage.revalidate();
        myEventsPage.repaint();
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
            
            btnInfo.addActionListener(e -> {
                showEventDetails(ev);
            });

            btnReg.addActionListener(e -> {
                selectedEvent = ev;
                buildRegisterPage();
                showCard("REGISTER");
            });
        }
    }
}

