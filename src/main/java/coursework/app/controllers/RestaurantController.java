package coursework.app.controllers;

import coursework.model.Restaurant;
import coursework.service.RestaurantService;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class RestaurantController {

    private final RestaurantService service;
    private final TableView<Restaurant> table = new TableView<>();

    // form fields
    private TextField loginField;
    private TextField nameField;
    private TextField phoneField;
    private TextField addressField;
    private PasswordField passwordField;

    public RestaurantController(RestaurantService service) {
        this.service = service;

        // demo duomenys – tik jei kol kas nėra restoranų
        if (service.getAllRestaurants().isEmpty()) {
            service.createRestaurant("rest1", "pass1", "Resto One",
                    "Owner", "111111111", "Vilnius");
            service.createRestaurant("rest2", "pass2", "Resto Two",
                    "Owner", "222222222", "Kaunas");
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
        passwordField = new PasswordField();

        loginField.setPromptText("Login");
        passwordField.setPromptText("Password");
        nameField.setPromptText("Name");
        phoneField.setPromptText("Phone");
        addressField.setPromptText("Address");

        Button addBtn = new Button("Add Restaurant");
        Button updateBtn = new Button("Update Restaurant");
        Button deleteBtn = new Button("Delete Restaurant");

        addBtn.setOnAction(e -> addRestaurant());
        updateBtn.setOnAction(e -> updateRestaurant());
        deleteBtn.setOnAction(e -> deleteRestaurant());

        VBox form = new VBox(8,
                new Label("Restaurant Form"),
                new Label("Login:"),
                loginField,
                new Label("Password:"),
                passwordField,
                new Label("Name:"),
                nameField,
                new Label("Phone:"),
                phoneField,
                new Label("Address:"),
                addressField,
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
        TableColumn<Restaurant, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Restaurant, String> loginCol = new TableColumn<>("Login");
        loginCol.setCellValueFactory(new PropertyValueFactory<>("login"));

        TableColumn<Restaurant, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Restaurant, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));

        TableColumn<Restaurant, String> addressCol = new TableColumn<>("Address");
        addressCol.setCellValueFactory(new PropertyValueFactory<>("address"));

        table.setItems(service.getAllRestaurants());
        table.getColumns().setAll(idCol, loginCol, nameCol, phoneCol, addressCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void fillForm(Restaurant r) {
        loginField.setText(r.getLogin());
        nameField.setText(r.getName());
        phoneField.setText(r.getPhoneNumber());
        addressField.setText(r.getAddress());
        // password nerodom – formoj keitimas optional, jei palieki tuščią – nekeičiam
        passwordField.clear();
    }

    private void addRestaurant() {
        // čia password PRIVALOMAS
        if (!validate(true)) return;

        String password = passwordField.getText();

        service.createRestaurant(
                loginField.getText(),
                password,
                nameField.getText(),
                "Restaurant", // role / surname tau nesvarbu
                phoneField.getText(),
                addressField.getText()
        );

        showInfo("Restaurant added.");
        table.refresh();
    }

    private void updateRestaurant() {
        Restaurant selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Select restaurant first.");
            return;
        }
        // updatinant password neprivalomas
        if (!validate(false)) return;

        selected.setLogin(loginField.getText());
        selected.setName(nameField.getText());
        selected.setPhoneNumber(phoneField.getText());
        selected.setAddress(addressField.getText());

        // jeigu formoje įrašai naują password – atnaujinam; jei tuščia, paliekam seną
        if (!passwordField.getText().isBlank()) {
            selected.setPassword(passwordField.getText());
        }

        service.updateRestaurant(selected);
        table.refresh();
        showInfo("Restaurant updated.");
    }

    private void deleteRestaurant() {
        Restaurant selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Select restaurant first.");
            return;
        }

        service.deleteRestaurant(selected.getId());
        table.getItems().remove(selected);
        showInfo("Restaurant deleted.");
    }

    // bendras validate, bet su parametru ar privalomas password
    private boolean validate(boolean requirePassword) {
        StringBuilder errors = new StringBuilder();

        if (loginField.getText().isBlank()) {
            errors.append("- Login cannot be empty.\n");
        }
        if (nameField.getText().isBlank()) {
            errors.append("- Name cannot be empty.\n");
        }

        if (requirePassword && passwordField.getText().isBlank()) {
            errors.append("- Password is required.\n");
        }

        String phone = phoneField.getText().trim();
        if (phone.isEmpty()) {
            errors.append("- Phone is required.\n");
        } else if (!phone.matches("\\+?[0-9 ]{6,15}")) {
            errors.append("- Phone must contain only digits (and optional leading '+').\n");
        }

        if (addressField.getText().isBlank()) {
            errors.append("- Address cannot be empty.\n");
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
