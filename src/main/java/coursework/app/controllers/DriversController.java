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
    private TextField surnameField;
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
        surnameField = new TextField();
        phoneField = new TextField();
        addressField = new TextField();
        licenceField = new TextField();
        bDatePicker = new DatePicker();

        bDatePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(java.time.LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (date.isAfter(java.time.LocalDate.now())) {
                    setDisable(true);
                }
            }
        });

        vehicleTypeBox = new ComboBox<>();

        loginField.setPromptText("Login");
        nameField.setPromptText("Name");
        surnameField.setPromptText("Surname");
        phoneField.setPromptText("Phone");
        addressField.setPromptText("Address");
        licenceField.setPromptText("Licence");
        bDatePicker.setPromptText("Birth date");
        vehicleTypeBox.getItems().setAll(VehicleType.values());


        Button addBtn = new Button("Add Driver");
        Button updateBtn = new Button("Update Driver");
        Button deleteBtn = new Button("Delete Driver");

        addBtn.setOnAction(e -> addDriver());
        updateBtn.setOnAction(e -> updateDriver());
        deleteBtn.setOnAction(e -> deleteDriver());

        VBox form = new VBox(8,
                new Label("Driver Form"),
                new Label("Login:"),
                loginField,
                new Label("Name:"),
                nameField,
                new Label("Surname:"),
                surnameField,
                new Label("Address:"),
                addressField,
                new Label("Phone:"),
                phoneField,
                new Label("Licence number:"),
                licenceField,
                new Label("Birth date:"),
                bDatePicker,
                new Label("Vehicle type:"),
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

        TableColumn<Driver, String> surnameCol = new TableColumn<>("Surname");
        surnameCol.setCellValueFactory(new PropertyValueFactory<>("surname"));

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

        table.setItems(service.getAllDrivers());
        table.getColumns().setAll(
                idCol, loginCol, nameCol, surnameCol, phoneCol,
                addressCol, licenceCol, bDateCol, vehicleTypeCol
        );
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void fillForm(Driver d) {
        loginField.setText(d.getLogin());
        nameField.setText(d.getName());
        surnameField.setText(d.getSurname());
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
                surnameField.getText(),
                phoneField.getText(),
                addressField.getText(),
                licenceField.getText(),
                bDatePicker.getValue(),
                vehicleTypeBox.getValue()
        );

        showInfo("Driver added.");
        table.refresh();
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
        selected.setSurname(surnameField.getText());
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
        table.getItems().remove(selected);
        showInfo("Driver deleted.");
    }

    private boolean validate() {
        StringBuilder errors = new StringBuilder();

        if (loginField.getText().isBlank()) {
            errors.append("- Login cannot be empty.\n");
        }
        if (nameField.getText().isBlank()) {
            errors.append("- Name cannot be empty.\n");
        }
        if (surnameField.getText().isBlank()) {
            errors.append("- Surname cannot be empty.\n");
        }
        if (addressField.getText().isBlank()) {
            errors.append("- Address cannot be empty.\n");
        }

        // PHONE CHECK
        String phone = phoneField.getText().trim();
        if (phone.isEmpty()) {
            errors.append("- Phone is required.\n");
        } else if (!phone.matches("\\+?[0-9 ]{6,15}")) {
            errors.append("- Must be a valid phone number.\n");
        }

        // LICENCE CHECK
        if (licenceField.getText().isBlank()) {
            errors.append("- Licence number is required.\n");
        }

        // BIRTH DATE CHECKS
        if (bDatePicker.getValue() == null) {
            errors.append("- Birth date is required.\n");
        } else {
            var birthDate = bDatePicker.getValue();

            // negali būti ateityje
            if (birthDate.isAfter(java.time.LocalDate.now())) {
                errors.append("- Birth date cannot be in the future.\n");
            }

            // turi būti bent 18 metų
            int age = java.time.Period.between(birthDate, java.time.LocalDate.now()).getYears();
            if (age < 18) {
                errors.append("- Driver must be at least 18 years old.\n");
            }
        }

        // VEHICLE TYPE
        if (vehicleTypeBox.getValue() == null) {
            errors.append("- Vehicle type is required.\n");
        }

        if (errors.length() > 0) {
            showError(errors.toString());
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
