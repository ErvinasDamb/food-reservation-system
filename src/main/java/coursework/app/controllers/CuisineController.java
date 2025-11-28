package coursework.app.controllers;

import coursework.model.Cuisine;
import coursework.model.Restaurant;
import coursework.service.CuisineService;
import coursework.service.RestaurantService;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class CuisineController {

    private final CuisineService service;
    private final RestaurantService restaurantService;

    private final TableView<Cuisine> table = new TableView<>();

    // form fields
    private TextField nameField;
    private TextArea ingredientsArea;
    private TextField priceField;
    private CheckBox spicyBox;
    private CheckBox veganBox;
    private ComboBox<Restaurant> restaurantBox;

    public CuisineController(RestaurantService restaurantService,
                             CuisineService cuisineService) {
        this.restaurantService = restaurantService;
        this.service = cuisineService;

        // demo patiekalas, jei yra restoranų ir kol kas nėra patiekalų
        var allRestaurants = restaurantService.getAllRestaurants();
        if (!allRestaurants.isEmpty() && service.getAllCuisines().isEmpty()) {
            Restaurant r1 = allRestaurants.get(0);
            service.createCuisine("Burger", "Beef, bun, cheese", 8.5, false, false, r1);
        }
    }

    public BorderPane getView() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        setupTable();
        root.setCenter(table);

        // --- form fields ---
        nameField = new TextField();
        ingredientsArea = new TextArea();
        priceField = new TextField();
        spicyBox = new CheckBox("Spicy");
        veganBox = new CheckBox("Vegan");
        restaurantBox = new ComboBox<>();

        nameField.setPromptText("Name");
        ingredientsArea.setPromptText("Ingredients / description");
        ingredientsArea.setPrefRowCount(3);
        priceField.setPromptText("Price");

        restaurantBox.setItems(restaurantService.getAllRestaurants());

        restaurantBox.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Restaurant item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getName());
            }
        });
        restaurantBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Restaurant item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getName());
            }
        });

        Button addBtn = new Button("Add dish");
        Button updateBtn = new Button("Update dish");
        Button deleteBtn = new Button("Delete dish");

        addBtn.setOnAction(e -> addCuisine());
        updateBtn.setOnAction(e -> updateCuisine());
        deleteBtn.setOnAction(e -> deleteCuisine());

        VBox form = new VBox(8,
                new Label("Cuisine / Dish Form"),
                new Label("Name:"),
                nameField,
                new Label("Ingredients / description:"),
                ingredientsArea,
                new Label("Price:"),
                priceField,
                spicyBox,
                veganBox,
                new Label("Restaurant:"),
                restaurantBox,
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
        TableColumn<Cuisine, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Cuisine, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Cuisine, String> ingredientsCol = new TableColumn<>("Ingredients");
        ingredientsCol.setCellValueFactory(new PropertyValueFactory<>("ingredients"));

        TableColumn<Cuisine, Double> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));

        TableColumn<Cuisine, Boolean> spicyCol = new TableColumn<>("Spicy");
        spicyCol.setCellValueFactory(new PropertyValueFactory<>("spicy"));

        TableColumn<Cuisine, Boolean> veganCol = new TableColumn<>("Vegan");
        veganCol.setCellValueFactory(new PropertyValueFactory<>("vegan"));

        TableColumn<Cuisine, String> restaurantCol = new TableColumn<>("Restaurant");
        restaurantCol.setCellValueFactory(cellData -> {
            Restaurant r = cellData.getValue().getRestaurant();
            return new SimpleStringProperty(r != null ? r.getName() : "");
        });

        table.setItems(service.getAllCuisines());
        table.getColumns().setAll(
                idCol, nameCol, ingredientsCol, priceCol,
                spicyCol, veganCol, restaurantCol
        );
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void fillForm(Cuisine c) {
        nameField.setText(c.getName());
        ingredientsArea.setText(c.getIngredients());
        priceField.setText(String.valueOf(c.getPrice()));
        spicyBox.setSelected(c.isSpicy());
        veganBox.setSelected(c.isVegan());
        restaurantBox.setValue(c.getRestaurant());
    }

    private void addCuisine() {
        if (!validate()) return;

        double price;
        try {
            price = Double.parseDouble(priceField.getText());
        } catch (NumberFormatException e) {
            showError("Price must be a number.");
            return;
        }

        service.createCuisine(
                nameField.getText(),
                ingredientsArea.getText(),
                price,
                spicyBox.isSelected(),
                veganBox.isSelected(),
                restaurantBox.getValue()
        );

        showInfo("Dish added.");
        table.refresh();
    }

    private void updateCuisine() {
        Cuisine selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Select a row first.");
            return;
        }
        if (!validate()) return;

        double price;
        try {
            price = Double.parseDouble(priceField.getText());
        } catch (NumberFormatException e) {
            showError("Price must be a number.");
            return;
        }

        selected.setName(nameField.getText());
        selected.setIngredients(ingredientsArea.getText());
        selected.setPrice(price);
        selected.setSpicy(spicyBox.isSelected());
        selected.setVegan(veganBox.isSelected());
        selected.setRestaurant(restaurantBox.getValue());

        service.updateCuisine(selected);
        table.refresh();
        showInfo("Dish updated.");
    }

    private void deleteCuisine() {
        Cuisine selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Select a row first.");
            return;
        }

        service.deleteCuisine(selected.getId());
        table.getItems().remove(selected);
        showInfo("Dish deleted.");
    }

    private boolean validate() {
        StringBuilder errors = new StringBuilder();

        if (nameField.getText().isBlank()) {
            errors.append("- Dish name is required.\n");
        }
        if (restaurantBox.getValue() == null) {
            errors.append("- Restaurant must be selected.\n");
        }

        String priceText = priceField.getText().trim();
        if (priceText.isEmpty()) {
            errors.append("- Price is required.\n");
        } else {
            try {
                Double.parseDouble(priceText);
            } catch (NumberFormatException e) {
                errors.append("- Price must be a valid number.\n");
            }
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
