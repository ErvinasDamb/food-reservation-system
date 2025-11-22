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

        loginField.setPromptText("Login");
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
                loginField,
                nameField,
                phoneField,
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

        // ČIA BŪTINA: TableView tiesiogiai naudoja shared ObservableList
        table.setItems(service.getAllRestaurants());
        table.getColumns().setAll(idCol, loginCol, nameCol, phoneCol, addressCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void fillForm(Restaurant r) {
        loginField.setText(r.getLogin());
        nameField.setText(r.getName());
        phoneField.setText(r.getPhoneNumber());
        addressField.setText(r.getAddress());
    }

    private void addRestaurant() {
        if (!validate()) return;

        service.createRestaurant(
                loginField.getText(),
                "defaultPass",                // password – nerodomas formoje
                nameField.getText(),
                "Restaurant",                 // surname – neaktualu formoje
                phoneField.getText(),
                addressField.getText()
        );

        // TableView susirišęs su ObservableList, todėl pats atsinaujina
        showInfo("Restaurant added.");
    }

    private void updateRestaurant() {
        Restaurant selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Select restaurant first.");
            return;
        }
        if (!validate()) return;

        selected.setLogin(loginField.getText());
        selected.setName(nameField.getText());
        selected.setPhoneNumber(phoneField.getText());
        selected.setAddress(addressField.getText());

        service.updateRestaurant(selected);
        table.refresh(); // kad atsinaujintų cell’iai
        showInfo("Restaurant updated.");
    }

    private void deleteRestaurant() {
        Restaurant selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Select restaurant first.");
            return;
        }

        service.deleteRestaurant(selected.getId());
        // TableView susirišęs su ObservableList, removeIf jau pašalina iš sąrašo
        showInfo("Restaurant deleted.");
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
        return true;
    }

    private void showError(String msg) {
        new Alert(Alert.AlertType.ERROR, msg).showAndWait();
    }

    private void showInfo(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg).showAndWait();
    }
}
