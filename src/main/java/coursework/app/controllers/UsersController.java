package coursework.app.controllers;

import coursework.model.BasicUser;
import coursework.service.BasicUserService;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class UsersController {

    private final BasicUserService service;
    private final TableView<BasicUser> table = new TableView<>();

    private TextField loginField;
    private PasswordField passwordField;
    private TextField nameField;
    private TextField surnameField;
    private TextField phoneField;
    private TextField addressField;
    private CheckBox adminCheck;

    public UsersController(BasicUserService service) {
        this.service = service;

        // demo tik jei sąrašas tuščias
        if (service.getAllUsers().isEmpty()) {
            service.createBasicUser("user1", "pass1", "Jonas", "Jonaitis", "123456789", "Adresas 1", false);
            service.createBasicUser("admin", "admin", "Adminas", "Adminas", "987654321", "Adresas 2", true);
        }
    }

    public BorderPane getView() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        setupTable();
        root.setCenter(table);

        loginField = new TextField();
        passwordField = new PasswordField();
        nameField = new TextField();
        surnameField = new TextField();
        phoneField = new TextField();
        addressField = new TextField();
        adminCheck = new CheckBox("Is admin");

        loginField.setPromptText("Login");
        passwordField.setPromptText("Password");
        nameField.setPromptText("Name");
        surnameField.setPromptText("Surname");
        phoneField.setPromptText("Phone");
        addressField.setPromptText("Address");

        Button addBtn = new Button("Add User");
        Button updateBtn = new Button("Update User");
        Button deleteBtn = new Button("Delete User");

        addBtn.setOnAction(e -> addUser());
        updateBtn.setOnAction(e -> updateUser());
        deleteBtn.setOnAction(e -> deleteUser());

        VBox form = new VBox(8,
                new Label("User Form"),
                new Label("Login:"),
                loginField,
                new Label("Password:"),
                passwordField,
                new Label("Name:"),
                nameField,
                new Label("Surname:"),
                surnameField,
                new Label("Phone:"),
                phoneField,
                new Label("Address:"),
                addressField,
                adminCheck,
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
        TableColumn<BasicUser, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<BasicUser, String> loginCol = new TableColumn<>("Login");
        loginCol.setCellValueFactory(new PropertyValueFactory<>("login"));

        TableColumn<BasicUser, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<BasicUser, String> surnameCol = new TableColumn<>("Surname");
        surnameCol.setCellValueFactory(new PropertyValueFactory<>("surname"));

        TableColumn<BasicUser, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));

        TableColumn<BasicUser, String> addressCol = new TableColumn<>("Address");
        addressCol.setCellValueFactory(new PropertyValueFactory<>("address"));

        TableColumn<BasicUser, Boolean> adminCol = new TableColumn<>("Admin");
        adminCol.setCellValueFactory(new PropertyValueFactory<>("admin"));

        table.setItems(service.getAllUsers());
        table.getColumns().setAll(idCol, loginCol, nameCol, surnameCol, phoneCol, addressCol, adminCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void fillForm(BasicUser u) {
        loginField.setText(u.getLogin());
        passwordField.setText(u.getPassword());
        nameField.setText(u.getName());
        surnameField.setText(u.getSurname());
        phoneField.setText(u.getPhoneNumber());
        addressField.setText(u.getAddress());
        adminCheck.setSelected(u.isAdmin());
    }

    private void addUser() {
        if (!validate()) return;

        service.createBasicUser(
                loginField.getText(),
                passwordField.getText(),
                nameField.getText(),
                surnameField.getText(),
                phoneField.getText(),
                addressField.getText(),
                adminCheck.isSelected()
        );
        showInfo("User added.");
        table.refresh();
    }

    private void updateUser() {
        BasicUser selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Select user first.");
            return;
        }
        if (!validate()) return;

        selected.setLogin(loginField.getText());
        selected.setPassword(passwordField.getText());
        selected.setName(nameField.getText());
        selected.setSurname(surnameField.getText());
        selected.setPhoneNumber(phoneField.getText());
        selected.setAddress(addressField.getText());
        selected.setAdmin(adminCheck.isSelected());

        service.updateBasicUser(selected);
        table.refresh();
        showInfo("User updated.");
    }

    private boolean confirmDelete(String what) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete " + what + "?",
                ButtonType.OK, ButtonType.CANCEL);
        alert.setHeaderText(null);
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    private void deleteUser() {
        BasicUser selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Select user first.");
            return;
        }

        if (!confirmDelete("user " + selected.getLogin())) {
            return;
        }

        service.deleteBasicUser(selected.getId());
        table.getItems().remove(selected);
        showInfo("User deleted.");
    }

    private boolean validate() {
        StringBuilder errors = new StringBuilder();

        if (loginField.getText().isBlank()) {
            errors.append("- Login is required.\n");
        }
        if (passwordField.getText().isBlank()) {
            errors.append("- Password is required.\n");
        }
        if (nameField.getText().isBlank()) {
            errors.append("- Name is required.\n");
        }
        if (surnameField.getText().isBlank()) {
            errors.append("- Surname is required.\n");
        }

        String phone = phoneField.getText().trim();
        if (phone.isEmpty()) {
            errors.append("- Phone is required.\n");
        } else if (!phone.matches("\\+?[0-9 ]{6,15}")) {
            errors.append("- Must be a valid phone number.\n");
        }

        if (addressField.getText().isBlank()) {
            errors.append("- Address is required.\n");
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
