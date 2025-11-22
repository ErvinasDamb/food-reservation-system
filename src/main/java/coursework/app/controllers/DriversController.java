package coursework.app.controllers;

import coursework.model.Driver;
import coursework.model.VehicleType;
import coursework.service.DriverService;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import java.time.LocalDate;

public class DriversController {

    private final DriverService service;
    private final TableView<Driver> table = new TableView<>();

    // Form fields
    private TextField loginField;
    private TextField nameField;
    private TextField phoneField;
    private TextField addressField;
    private TextField licenceField;
    private DatePicker bDatePicker;
    private ComboBox<VehicleType> vehicleTypeBox;

    public DriversController(DriverService service) {
        this.service = service;

        // demo data – tik jei sąrašas tuščias
        if (service.getAllDrivers().isEmpty()) {
            service.createDriver(
                    "driver1", "pass1", "Tomas", "Driver",
                    "123456789", "Vilnius",
                    "B12345", LocalDate.of(1995, 1, 1), VehicleType.CAR
            );
            service.createDriver(
                    "driver2", "pass2", "Mantas", "Driver",
                    "987654321", "Kaunas",
                    "C98765", LocalDate.of(1990, 5, 20), VehicleType.BY_FOOT
            );
        }
    }

    public BorderPane getView() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        setupTable();
        root.setCenter(table);

        // form
        loginField = new TextField();
        nameField = new TextField();
        phoneField = new TextField();
        addressField = new TextField();
        licenceField = new TextField();
        bDatePicker = new DatePicker();
        vehicleTypeBox = new ComboBox<>();

        loginField.setPromptText("Login");
        nameField.setPromptText("Name");
        phoneField.setPromptText("Phone");
        addressField.setPromptText("Address");
        licenceField.setPromptText("Licence");
        bDatePicker.setPromptText("Birth date");
        vehicleTypeBox.getItems().setAll(VehicleType.values());
        vehicleTypeBox.setPromptText("Vehicle type");

        Button addBtn = new Button("Add Driver");
        Button updateBtn = new Button("Update Driver");
        Button deleteBtn = new Button("Delete Driver");

        addBtn.setOnAction(e -> addDriver());
        updateBtn.setOnAction(e -> updateDriver());
        deleteBtn.setOnAction(e -> deleteDriver());

        VBox form = new VBox(8,
                new Label("Driver Form"),
                loginField,
                nameField,
                phoneField,
                addressField,
                licenceField,
                bDatePicker,
                vehicleTypeBox,
                addBtn,
                updateBtn,
                deleteBtn
        );
        form.setPadding(new Insets(10));

        root.setRight(form);

        table.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null) fillForm(selected);
        });

        return root;
    }

    private void setupTable() {
        TableColumn<Driver, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Driver, String> loginCol = new TableColumn<>("Login");
        loginCol.setCellValueFactory(new PropertyValueFactory<>("login"));

        TableColumn<Driver, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Driver, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));

        TableColumn<Driver, String> addressCol = new TableColumn<>("Address");
        addressCol.setCellValueFactory(new PropertyValueFactory<>("address"));

        TableColumn<Driver, String> licenceCol = new TableColumn<>("Licence");
        licenceCol.setCellValueFactory(new PropertyValueFactory<>("licence"));

        TableColumn<Driver, LocalDate> bDateCol = new TableColumn<>("Birth date");
        bDateCol.setCellValueFactory(new PropertyValueFactory<>("bDate"));

        TableColumn<Driver, VehicleType> vehicleTypeCol = new TableColumn<>("Vehicle");
        vehicleTypeCol.setCellValueFactory(new PropertyValueFactory<>("vehicleType"));

        // shared list
        table.setItems(service.getAllDrivers());
        table.getColumns().setAll(
                idCol, loginCol, nameCol, phoneCol,
                addressCol, licenceCol, bDateCol, vehicleTypeCol
        );
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void fillForm(Driver d) {
        loginField.setText(d.getLogin());
        nameField.setText(d.getName());
        phoneField.setText(d.getPhoneNumber());
        addressField.setText(d.getAddress());
        licenceField.setText(d.getLicence());
        bDatePicker.setValue(d.getBDate());
        vehicleTypeBox.setValue(d.getVehicleType());
    }

    private void addDriver() {
        if (!validate()) return;

        service.createDriver(
                loginField.getText(),
                "defaultPass",
                nameField.getText(),
                "Driver",
                phoneField.getText(),
                addressField.getText(),
                licenceField.getText(),
                bDatePicker.getValue(),
                vehicleTypeBox.getValue()
        );

        // TableView = shared list, atsinaujina automatiškai
        showInfo("Driver added.");
    }

    private void updateDriver() {
        Driver selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Select driver first.");
            return;
        }
        if (!validate()) return;

        selected.setLogin(loginField.getText());
        selected.setName(nameField.getText());
        selected.setPhoneNumber(phoneField.getText());
        selected.setAddress(addressField.getText());
        selected.setLicence(licenceField.getText());
        selected.setBDate(bDatePicker.getValue());
        selected.setVehicleType(vehicleTypeBox.getValue());

        service.updateDriver(selected);
        table.refresh();
        showInfo("Driver updated.");
    }

    private void deleteDriver() {
        Driver selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Select driver first.");
            return;
        }

        service.deleteDriver(selected.getId());
        showInfo("Driver deleted.");
    }

    private boolean validate() {
        if (loginField.getText().isBlank()) {
            showError("Login cannot be empty.");
            return false;
        }
        if (nameField.getText().isBlank()) {
            showError("Name cannot be empty.");
            return false;
        }
        if (bDatePicker.getValue() == null) {
            showError("Birth date required.");
            return false;
        }
        if (vehicleTypeBox.getValue() == null) {
            showError("Vehicle type required.");
            return false;
        }
        return true;
    }

    private void showError(String msg) {
        new Alert(Alert.AlertType.ERROR, msg).showAndWait();
    }

    private void showInfo(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg).showAndWait();
    }
}
