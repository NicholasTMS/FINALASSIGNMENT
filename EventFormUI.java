// EventFormUI.java
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.EnumMap;

public class EventFormUI extends JFrame {

    private final EventController controller;

    private JTextField nameField, venueField, dateField, timeField, capacityField, feeField;
    private JComboBox<EventType> typeCombo;
    private JTextArea outputArea;
    private JButton uploadButton;
    private JLabel pictureLabel;
    private byte[] uploadedPictureData;

    private EnumMap<AdditionalServices, JCheckBox> serviceChecks = new EnumMap<>(AdditionalServices.class);
    private EnumMap<AdditionalServices, JTextField> serviceCostFields = new EnumMap<>(AdditionalServices.class);

    private EnumMap<DiscountType, JCheckBox> discountChecks = new EnumMap<>(DiscountType.class);
    private EnumMap<DiscountType, JTextField> discountAmountFields = new EnumMap<>(DiscountType.class);

    public EventFormUI(EventController controller) {
        this.controller = controller;

        setTitle("Create Event");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1920, 1080);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // --- Main form panel ---
        JPanel formPanel = new JPanel(new GridLayout(12, 2, 5, 5));
        nameField     = new JTextField();
        venueField    = new JTextField();
        dateField     = new JTextField();
        timeField     = new JTextField();
        capacityField = new JTextField();
        feeField      = new JTextField();
        typeCombo     = new JComboBox<>(EventType.values());     

        formPanel.add(new JLabel("Event Name:"));        formPanel.add(nameField);
        formPanel.add(new JLabel("Venue:"));             formPanel.add(venueField);
        formPanel.add(new JLabel("Date (yyyy-MM-dd):")); formPanel.add(dateField);
        formPanel.add(new JLabel("Time (HH:mm):"));      formPanel.add(timeField);
        formPanel.add(new JLabel("Capacity:"));          formPanel.add(capacityField);
        formPanel.add(new JLabel("Registration Fee:"));  formPanel.add(feeField);
        formPanel.add(new JLabel("Event Type:"));        formPanel.add(typeCombo);
        
        // --- Picture form panel ---
        JPanel picturePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        uploadButton = new JButton("Upload Picture…");
        pictureLabel = new JLabel();
        picturePanel.add(uploadButton);
        picturePanel.add(pictureLabel);

        // Upload button 
        uploadButton.addActionListener(evt -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File imgFile = chooser.getSelectedFile();
                try {
                uploadedPictureData = Files.readAllBytes(imgFile.toPath());
                ImageIcon icon = new ImageIcon(
                    new ImageIcon(uploadedPictureData)
                    .getImage()
                    .getScaledInstance(100,100,Image.SCALE_SMOOTH));
                pictureLabel.setIcon(icon);
                } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                    "Failed to load image: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // --- Additional Services panel ---
        JPanel servicePanel = new JPanel(new GridLayout(0, 5));
        servicePanel.setBorder(BorderFactory.createTitledBorder("Additional Services (per person)"));
        for (AdditionalServices s : AdditionalServices.values()) {
            JCheckBox cb = new JCheckBox(s.name());
            JTextField costField = new JTextField("0.0");
            serviceChecks.put(s, cb);
            serviceCostFields.put(s, costField);
            servicePanel.add(cb);
            servicePanel.add(new JLabel(""));
            servicePanel.add(new JLabel(""));
            servicePanel.add(new JLabel("Cost per person RM:"));
            servicePanel.add(costField);
        }

        // --- Discount panel ---
        JPanel discountPanel = new JPanel(new GridLayout(0, 5));
        discountPanel.setBorder(BorderFactory.createTitledBorder("Discount"));
        for (DiscountType dt : DiscountType.values()) {
            JCheckBox cb = new JCheckBox(dt.name());
            JTextField discountField = new JTextField("0.0");
            discountChecks.put(dt, cb);
            discountAmountFields.put(dt, discountField);
            discountPanel.add(cb);
            discountPanel.add(new JLabel(""));
            discountPanel.add(new JLabel(""));
            discountPanel.add(new JLabel("Discount per person RM:"));
            discountPanel.add(discountField);
        }

        // --- Create button ---
        JButton createButton = new JButton("Create Event");
        createButton.addActionListener(this::CreateEvent);
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(createButton);

        // --- Output area ---
        outputArea = new JTextArea(8, 60);
        outputArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputArea);

        // --- Assemble top section ---
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.add(formPanel);
        topPanel.add(picturePanel);
        topPanel.add(servicePanel);
        topPanel.add(discountPanel);

        add(topPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.CENTER);
        add(scrollPane, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void CreateEvent(ActionEvent e) {
        try {
            // Gather basic fields
            String name      = nameField.getText().trim();
            String venue     = venueField.getText().trim();
            String dateInput = dateField.getText().trim();
            String timeInput = timeField.getText().trim();
            LocalDateTime dt = LocalDateTime.parse(dateInput + "T" + timeInput);
            int capacity     = Integer.parseInt(capacityField.getText().trim());
            double fee       = Double.parseDouble(feeField.getText().trim());
            EventType type   = (EventType) typeCombo.getSelectedItem();

            // Collect services
            EnumMap<AdditionalServices, Double> services = new EnumMap<>(AdditionalServices.class);
            for (AdditionalServices s : serviceChecks.keySet()) {
                if (serviceChecks.get(s).isSelected()) {
                    double cost = Double.parseDouble(serviceCostFields.get(s).getText().trim());
                    if (cost >= 0) services.put(s, cost);
                }
            }

            // Collect discounts
            EnumMap<DiscountType, Double> discounts = new EnumMap<>(DiscountType.class);
            for (DiscountType dtp : discountChecks.keySet()) {
                if (discountChecks.get(dtp).isSelected()) {
                    double val = Double.parseDouble(discountAmountFields.get(dtp).getText().trim());
                    if (val >= 0) discounts.put(dtp, val);
                }
            }

            // Delegate creation/update to the controller
            controller.createEvent(name, venue, dt, capacity, fee, type, services, discounts, uploadedPictureData );

            // Fetch the single model back for summary
            Event event = controller.getModel();

            // Build and show summary
            StringBuilder sb = new StringBuilder("✅ Event Created:\n")
                .append("Name: ").append(event.getEventName()).append("\n")
                .append("Type: ").append(event.getEventType()).append("\n")
                .append("Date: ").append(event.getDate()).append("\n")
                .append("Capacity: ").append(event.getCapacity()).append("\n")
                .append("Base Fee: RM").append(event.getRegisterationFee()).append("\n\n");

            if (!event.getAvailableAdditionalServices().isEmpty()) {
                sb.append("Additional Services:\n");
                event.getAvailableAdditionalServices().forEach((s, c) ->
                    sb.append(" • ").append(s).append(": RM").append(c).append("\n")
                );
            }
            if (!event.getAvailableDiscounts().isEmpty()) {
                sb.append("\nDiscounts:\n");
                event.getAvailableDiscounts().forEach((d, v) ->
                    sb.append(" • ").append(d).append(": -RM").append(v).append("\n")
                );
            }
            outputArea.setText(sb.toString());

        } catch (DateTimeParseException ex) {
            outputArea.setText("❌ Invalid date format. Use yyyy-MM-dd and HH:mm");
        } catch (NumberFormatException ex) {
            outputArea.setText("❌ Enter valid numeric values");
        } catch (Exception ex) {
            outputArea.setText("❌ Error: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            EventController ctrl = new EventController();
            new EventFormUI(ctrl).setVisible(true);
        });
    }
}



