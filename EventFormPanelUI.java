// EventFormPanelUI.java
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.EnumMap;

public class EventFormPanelUI extends JPanel {
    private final EventController controller;

    // form fields
    private JTextField nameField, venueField, dateField, timeField, capacityField, feeField;
    private JComboBox<EventType> typeCombo;
    private JButton uploadButton;
    private JLabel pictureLabel;
    private byte[] uploadedPictureData;

    // extra panels
    private EnumMap<AdditionalServices, JCheckBox> serviceChecks = new EnumMap<>(AdditionalServices.class);
    private EnumMap<AdditionalServices, JTextField> serviceCostFields = new EnumMap<>(AdditionalServices.class);
    private EnumMap<DiscountType, JCheckBox> discountChecks = new EnumMap<>(DiscountType.class);
    private EnumMap<DiscountType, JTextField> discountAmountFields = new EnumMap<>(DiscountType.class);

    // output & state
    private JTextArea outputArea;
    private Event  editingEvent;    // null = create new, non-null = editing

    public EventFormPanelUI(EventController controller) {
        this.controller = controller;
        setLayout(new BorderLayout());
        buildForm();
    }

    private void buildForm() {
        // --- Form fields ---
        JPanel formPanel = new JPanel(new GridLayout(7, 2, 5, 5));
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

        // --- Picture upload ---
        JPanel picturePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        uploadButton = new JButton("Upload Picture…");
        pictureLabel = new JLabel();
        picturePanel.add(uploadButton);
        picturePanel.add(pictureLabel);
        uploadButton.addActionListener(this::onUpload);

        // --- Additional Services ---
        JPanel servicePanel = new JPanel(new GridLayout(0, 5));
        servicePanel.setBorder(BorderFactory.createTitledBorder("Additional Services (per person)"));
        for (AdditionalServices s : AdditionalServices.values()) {
            JCheckBox cb = new JCheckBox(s.name());
            JTextField tf = new JTextField("0.0");
            serviceChecks.put(s, cb);
            serviceCostFields.put(s, tf);
            servicePanel.add(cb);
            servicePanel.add(new JLabel(""));
            servicePanel.add(new JLabel(""));
            servicePanel.add(new JLabel("Cost per person RM:"));
            servicePanel.add(tf);
        }

        // --- Discounts ---
        JPanel discountPanel = new JPanel(new GridLayout(0, 5));
        discountPanel.setBorder(BorderFactory.createTitledBorder("Discount"));
        for (DiscountType d : DiscountType.values()) {
            JCheckBox cb = new JCheckBox(d.name());
            JTextField tf = new JTextField("0.0");
            discountChecks.put(d, cb);
            discountAmountFields.put(d, tf);
            discountPanel.add(cb);
            discountPanel.add(new JLabel(""));
            discountPanel.add(new JLabel(""));
            discountPanel.add(new JLabel("Discount per person RM:"));
            discountPanel.add(tf);
        }

        // --- Create/Update button & output ---
        JButton createBtn = new JButton("Create/Update Event");
        createBtn.addActionListener(this::onUpdateCreate);
        outputArea = new JTextArea(5, 40);
        outputArea.setEditable(false);
        JScrollPane outputScroll = new JScrollPane(outputArea);

        // --- Assemble top half ---
        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.add(formPanel);
        top.add(picturePanel);
        top.add(servicePanel);
        top.add(discountPanel);

        add(top, BorderLayout.NORTH);
        add(createBtn, BorderLayout.CENTER);
        add(outputScroll, BorderLayout.SOUTH);
    }

    private void onUpload(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            try {
                uploadedPictureData = Files.readAllBytes(f.toPath());
                ImageIcon icon = new ImageIcon(
                  new ImageIcon(uploadedPictureData)
                    .getImage()
                    .getScaledInstance(100,100,Image.SCALE_SMOOTH)
                );
                pictureLabel.setIcon(icon);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                    "Failed to load image: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onUpdateCreate(ActionEvent e) {
        try {
            String name      = nameField.getText().trim();
            String venue     = venueField.getText().trim();
            String dateIn    = dateField.getText().trim();
            String timeIn    = timeField.getText().trim();
            LocalDateTime dt = LocalDateTime.parse(dateIn + "T" + timeIn);
            int capacity     = Integer.parseInt(capacityField.getText().trim());
            double fee       = Double.parseDouble(feeField.getText().trim());
            EventType type   = (EventType) typeCombo.getSelectedItem();

            // collect services
            EnumMap<AdditionalServices, Double> services = new EnumMap<>(AdditionalServices.class);
            for (AdditionalServices s : serviceChecks.keySet())
                if (serviceChecks.get(s).isSelected())
                    services.put(s, Double.parseDouble(serviceCostFields.get(s).getText().trim()));

            // collect discounts
            EnumMap<DiscountType, Double> discounts = new EnumMap<>(DiscountType.class);
            for (DiscountType d : discountChecks.keySet())
                if (discountChecks.get(d).isSelected())
                    discounts.put(d, Double.parseDouble(discountAmountFields.get(d).getText().trim()));

            if (editingEvent == null) {
                // create new
                controller.createEvent(
                  name, venue, dt, capacity, fee, type,
                  services, discounts, uploadedPictureData
                );
            } else {
                // update existing
                controller.updateEvent(
                  editingEvent.getEventID(),
                  name, venue, dt, capacity, fee, type,
                  services, discounts, uploadedPictureData
                );
            }

            Event ev = controller.getModel();
            displaySummary(ev);
            clearForm();

        } catch (DateTimeParseException ex) {
            outputArea.setText("❌ Invalid date format. Use yyyy-MM-dd and HH:mm");
        } catch (NumberFormatException ex) {
            outputArea.setText("❌ Enter valid numeric values");
        } catch (Exception ex) {
            outputArea.setText("❌ Error: " + ex.getMessage());
        }
    }

    private void displaySummary(Event ev) {
        StringBuilder sb = new StringBuilder("✅ Saved:\n")
            .append("Name: ").append(ev.getEventName()).append("\n")
            .append("Type: ").append(ev.getEventType()).append("\n")
            .append("Date: ").append(ev.getDate()).append("\n")
            .append("Capacity: ").append(ev.getCapacity()).append("\n")
            .append("Base Fee: RM").append(ev.getRegisterationFee()).append("\n\n");

        if (!ev.getAvailableAdditionalServices().isEmpty()) {
            sb.append("Additional Services:\n");
            ev.getAvailableAdditionalServices().forEach((s,c)->
              sb.append(" • ").append(s).append(": RM").append(c).append("\n"));
        }
        if (!ev.getAvailableDiscounts().isEmpty()) {
            sb.append("\nDiscounts:\n");
            ev.getAvailableDiscounts().forEach((d,v)->
              sb.append(" • ").append(d).append(": -RM").append(v).append("\n"));
        }
        outputArea.setText(sb.toString());
    }

    private void clearForm() {
        editingEvent = null;
        nameField.setText(""); venueField.setText("");
        dateField.setText(""); timeField.setText("");
        capacityField.setText(""); feeField.setText("");
        typeCombo.setSelectedIndex(0);
        pictureLabel.setIcon(null);
        uploadedPictureData = null;
        serviceChecks.forEach((s,cb)->{ cb.setSelected(false); serviceCostFields.get(s).setText("0.0"); });
        discountChecks.forEach((d,cb)->{ cb.setSelected(false); discountAmountFields.get(d).setText("0.0"); });
    }

    /** Call this to load an existing event into the form for editing */
    public void loadEventForEdit(Event e) {
        editingEvent = e;
        nameField.setText(e.getEventName());
        venueField.setText(e.getVenue());
        dateField.setText(e.getDate().toLocalDate().toString());
        timeField.setText(e.getDate().toLocalTime().toString());
        capacityField.setText(String.valueOf(e.getCapacity()));
        feeField.setText(String.valueOf(e.getRegisterationFee()));
        typeCombo.setSelectedItem(e.getEventType());

        if (e.getPictureData() != null) {
            uploadedPictureData = e.getPictureData();
            ImageIcon icon = new ImageIcon(
              new ImageIcon(uploadedPictureData)
                .getImage().getScaledInstance(100,100,Image.SCALE_SMOOTH)
            );
            pictureLabel.setIcon(icon);
        }

        e.getAvailableAdditionalServices().forEach((s,c)-> {
            serviceChecks.get(s).setSelected(true);
            serviceCostFields.get(s).setText(String.valueOf(c));
        });
        e.getAvailableDiscounts().forEach((d,v)-> {
            discountChecks.get(d).setSelected(true);
            discountAmountFields.get(d).setText(String.valueOf(v));
        });

        outputArea.setText("✏️ Editing “" + e.getEventName() + "”\nMake changes and click Create/Update.");
    }
}
