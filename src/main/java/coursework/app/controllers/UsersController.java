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
    private CheckBox adminBox;

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
        adminBox = new CheckBox("Admin");

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
                loginField,
                passwordField,
                nameField,
                surnameField,
                phoneField,
                addressField,
                adminBox,
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
        adminBox.setSelected(u.isAdmin());
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
                adminBox.isSelected()
        );
        showInfo("User added.");
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
        selected.setAdmin(adminBox.isSelected());

        service.updateBasicUser(selected);
        table.refresh();
        showInfo("User updated.");
    }

    private void deleteUser() {
        BasicUser selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Select user first.");
            return;
        }
        service.deleteBasicUser(selected.getId());
        showInfo("User deleted.");
    }

    private boolean validate() {
        if (loginField.getText().isBlank()) {
            showError("Login is required.");
            return false;
        }
        if (nameField.getText().isBlank()) {
            showError("Name is required.");
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
