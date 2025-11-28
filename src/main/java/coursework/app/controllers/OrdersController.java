package coursework.app.controllers;

import coursework.model.*;
import coursework.service.BasicUserService;
import coursework.service.CuisineService;
import coursework.service.DriverService;
import coursework.service.OrderService;
import coursework.service.RestaurantService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class OrdersController {

    private final OrderService orderService;
    private final RestaurantService restaurantService;
    private final DriverService driverService;
    private final CuisineService cuisineService;
    private final BasicUserService basicUserService;

    private final TableView<FoodOrder> table = new TableView<>();

    private ComboBox<BasicUser> buyerBox;
    private ComboBox<Restaurant> restaurantBox;
    private ComboBox<Driver> driverBox;
    private ComboBox<OrderStatus> statusBox;
    private ListView<Cuisine> dishesList;
    private TextField priceField;
    private TextArea chatArea;
    private FilteredList<Cuisine> filteredCuisines;

    private static final DateTimeFormatter CREATED_AT_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public OrdersController(OrderService orderService,
                            RestaurantService restaurantService,
                            DriverService driverService,
                            CuisineService cuisineService,
                            BasicUserService basicUserService) {

        this.orderService = orderService;
        this.restaurantService = restaurantService;
        this.driverService = driverService;
        this.cuisineService = cuisineService;
        this.basicUserService = basicUserService;
    }

    public BorderPane getView() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        setupTable();
        root.setCenter(table);

        buyerBox = new ComboBox<>();
        restaurantBox = new ComboBox<>();
        driverBox = new ComboBox<>();
        statusBox = new ComboBox<>();
        dishesList = new ListView<>();
        priceField = new TextField();
        chatArea = new TextArea();

        priceField.setPromptText("Total price (auto)");
        priceField.setEditable(false);
        chatArea.setPromptText("Chat messages…");
        chatArea.setPrefRowCount(3);

        // duomenys į combo/list
        buyerBox.setItems(basicUserService.getAllUsers().filtered(u -> !u.isAdmin()));
        restaurantBox.setItems(restaurantService.getAllRestaurants());
        driverBox.setItems(driverService.getAllDrivers());
        statusBox.setItems(FXCollections.observableArrayList(OrderStatus.values()));

        // patiekalų sąrašas su filtru
        filteredCuisines = new FilteredList<>(cuisineService.getAllCuisines(), c -> true);
        dishesList.setItems(filteredCuisines);
        dishesList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // filtravimas pagal restoraną – listenerį DĖDAM PO filteredCuisines sukūrimo
        restaurantBox.valueProperty().addListener((obs, oldRest, newRest) -> {
            if (newRest == null) {
                filteredCuisines.setPredicate(c -> true);
            } else {
                filteredCuisines.setPredicate(c ->
                        c.getRestaurant() != null &&
                                c.getRestaurant().equals(newRest)
                );
            }
            dishesList.getSelectionModel().clearSelection();
            priceField.clear();
        });

        // ListCell’ai – kaip rodom tekstą
        dishesList.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Cuisine item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null
                        ? ""
                        : item.getName() + " (" + item.getPrice() + ")");
            }
        });

        buyerBox.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(BasicUser item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" :
                        item.getName() + " " + item.getSurname());
            }
        });
        buyerBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(BasicUser item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" :
                        item.getName() + " " + item.getSurname());
            }
        });

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

        driverBox.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Driver item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" :
                        item.getName() + " " + item.getSurname());
            }
        });
        driverBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Driver item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" :
                        item.getName() + " " + item.getSurname());
            }
        });

        Button addBtn = new Button("Add order");
        Button updateBtn = new Button("Update order");
        Button deleteBtn = new Button("Delete order");
        Button statsBtn = new Button("Order stats");

        addBtn.setOnAction(e -> addOrder());
        updateBtn.setOnAction(e -> updateOrder());
        deleteBtn.setOnAction(e -> deleteOrder());
        statsBtn.setOnAction(e -> showOrderStats());

        VBox form = new VBox(8,
                new Label("Order Form"),
                new Label("Buyer:"),
                buyerBox,
                new Label("Restaurant:"),
                restaurantBox,
                new Label("Driver:"),
                driverBox,
                new Label("Status:"),
                statusBox,
                new Label("Dishes (multi-select):"),
                dishesList,
                new Label("Total price:"),
                priceField,
                new Label("Chat:"),
                chatArea,
                addBtn,
                updateBtn,
                deleteBtn,
                statsBtn
        );
        form.setPadding(new Insets(10));
        root.setRight(form);

        table.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null) fillForm(selected);
        });

        return root;
    }

    private void setupTable() {
        TableColumn<FoodOrder, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<FoodOrder, String> buyerCol = new TableColumn<>("Buyer");
        buyerCol.setCellValueFactory(cell -> {
            BasicUser u = cell.getValue().getBuyer();
            return new SimpleStringProperty(
                    u != null ? u.getName() + " " + u.getSurname() : ""
            );
        });

        TableColumn<FoodOrder, String> restaurantCol = new TableColumn<>("Restaurant");
        restaurantCol.setCellValueFactory(cell -> {
            Restaurant r = cell.getValue().getRestaurant();
            return new SimpleStringProperty(r != null ? r.getName() : "");
        });

        TableColumn<FoodOrder, String> driverCol = new TableColumn<>("Driver");
        driverCol.setCellValueFactory(cell -> {
            Driver d = cell.getValue().getDriver();
            return new SimpleStringProperty(
                    d != null ? d.getName() + " " + d.getSurname() : ""
            );
        });

        TableColumn<FoodOrder, OrderStatus> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        TableColumn<FoodOrder, Double> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));

        TableColumn<FoodOrder, String> itemsCol = new TableColumn<>("Items");
        itemsCol.setCellValueFactory(cell -> {
            var list = cell.getValue().getCuisineList();
            int count = (list != null) ? list.size() : 0;
            return new SimpleStringProperty(String.valueOf(count));
        });

        TableColumn<FoodOrder, String> dateCol = new TableColumn<>("Created At");
        dateCol.setCellValueFactory(cell -> {
            LocalDateTime dt = cell.getValue().getCreatedAt();
            String text = "";
            if (dt != null) {
                text = dt.format(CREATED_AT_FORMATTER);
            }
            return new SimpleStringProperty(text);
        });

        table.setItems(orderService.getAllOrders());
        table.getColumns().setAll(
                idCol,
                buyerCol,
                restaurantCol,
                driverCol,
                statusCol,
                priceCol,
                itemsCol,
                dateCol
        );

        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void fillForm(FoodOrder order) {
        buyerBox.setValue(order.getBuyer());
        restaurantBox.setValue(order.getRestaurant());
        driverBox.setValue(order.getDriver());
        statusBox.setValue(order.getStatus());

        dishesList.getSelectionModel().clearSelection();
        if (order.getCuisineList() != null) {
            for (Cuisine c : order.getCuisineList()) {
                int index = cuisineService.getAllCuisines().indexOf(c);
                if (index >= 0) {
                    dishesList.getSelectionModel().select(index);
                }
            }
        }

        priceField.setText(order.getPrice() != null ? order.getPrice().toString() : "");

        if (order.getChat() != null) {
            chatArea.setText(order.getChat().getMessages());
        } else {
            chatArea.clear();
        }
    }

    private void addOrder() {
        if (!validate()) return;

        List<Cuisine> selectedDishes = new ArrayList<>(dishesList.getSelectionModel().getSelectedItems());
        double totalPrice = selectedDishes.stream()
                .mapToDouble(Cuisine::getPrice)
                .sum();

        FoodOrder order = new FoodOrder();
        order.setName("Order");
        order.setBuyer(buyerBox.getValue());
        order.setRestaurant(restaurantBox.getValue());
        order.setDriver(driverBox.getValue());
        order.setStatus(statusBox.getValue());
        order.setCuisineList(selectedDishes);
        order.setPrice(totalPrice);
        order.setCreatedAt(LocalDateTime.now());

        if (!chatArea.getText().isBlank()) {
            Chat chat = new Chat();
            chat.setMessages(chatArea.getText());
            chat.setOrder(order);
            order.setChat(chat);
        }

        orderService.add(order);
        priceField.setText(String.valueOf(totalPrice));
        showInfo("Order added.");
        table.refresh();
    }

    private void updateOrder() {
        FoodOrder selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Select order first.");
            return;
        }
        if (!validate()) return;

        List<Cuisine> selectedDishes = new ArrayList<>(dishesList.getSelectionModel().getSelectedItems());
        double totalPrice = selectedDishes.stream()
                .mapToDouble(Cuisine::getPrice)
                .sum();

        selected.setBuyer(buyerBox.getValue());
        selected.setRestaurant(restaurantBox.getValue());
        selected.setDriver(driverBox.getValue());
        selected.setStatus(statusBox.getValue());
        selected.setCuisineList(selectedDishes);
        selected.setPrice(totalPrice);

        if (selected.getChat() == null && !chatArea.getText().isBlank()) {
            Chat chat = new Chat();
            chat.setOrder(selected);
            selected.setChat(chat);
        }
        if (selected.getChat() != null) {
            selected.getChat().setMessages(chatArea.getText());
        }

        orderService.update(selected);
        priceField.setText(String.valueOf(totalPrice));
        table.refresh();
        showInfo("Order updated.");
    }

    private void deleteOrder() {
        FoodOrder selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Select order first.");
            return;
        }

        orderService.delete(selected.getId());
        table.getItems().remove(selected);
        showInfo("Order deleted.");
    }

    private boolean validate() {
        StringBuilder errors = new StringBuilder();

        if (buyerBox.getValue() == null) {
            errors.append("- Buyer must be selected.\n");
        }
        if (restaurantBox.getValue() == null) {
            errors.append("- Restaurant must be selected.\n");
        }
        if (driverBox.getValue() == null) {
            errors.append("- Driver must be selected.\n");
        }
        if (statusBox.getValue() == null) {
            errors.append("- Order status must be selected.\n");
        }
        if (dishesList.getSelectionModel().getSelectedItems().isEmpty()) {
            errors.append("- Select at least one dish.\n");
        }

        if (errors.length() > 0) {
            showError(errors.toString());
            return false;
        }
        return true;
    }

    private void showOrderStats() {
        Stage statsStage = new Stage();
        statsStage.setTitle("Order statistics");

        int totalOrders = orderService.getAllOrders().size();
        long delivered = orderService.getAllOrders().stream()
                .filter(o -> o.getStatus() == OrderStatus.DELIVERED)
                .count();

        Label totalLabel = new Label("Total orders: " + totalOrders);
        Label deliveredLabel = new Label("Delivered orders: " + delivered);

        VBox root = new VBox(10, totalLabel, deliveredLabel);
        root.setPadding(new Insets(10));

        statsStage.setScene(new Scene(root, 250, 120));
        statsStage.initOwner(table.getScene().getWindow());
        statsStage.show();
    }

    private void showError(String msg) {
        new Alert(Alert.AlertType.ERROR, msg).showAndWait();
    }

    private void showInfo(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg).showAndWait();
    }
}
