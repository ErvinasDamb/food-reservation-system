package coursework.app.controllers;

import coursework.model.*;
import coursework.service.CuisineService;
import coursework.service.DriverService;
import coursework.service.OrderService;
import coursework.service.RestaurantService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import java.time.LocalDateTime;
import javafx.collections.transformation.FilteredList;
import java.time.format.DateTimeFormatter;
import coursework.service.BasicUserService;
import coursework.model.BasicUser;



import java.util.ArrayList;
import java.util.List;

public class OrdersController {

    private final OrderService orderService;
    private final RestaurantService restaurantService;
    private final DriverService driverService;
    private final CuisineService cuisineService;
    private final BasicUserService basicUserService;

    private final TableView<FoodOrder> table = new TableView<>();

    private ComboBox<Restaurant> restaurantBox;
    private ComboBox<Driver> driverBox;
    private ComboBox<OrderStatus> statusBox;
    private ListView<Cuisine> dishesList;
    private TextField priceField;
    private TextArea chatArea;
    private FilteredList<Cuisine> filteredCuisines;
    private static final DateTimeFormatter CREATED_AT_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private ComboBox<BasicUser> buyerBox;


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

        buyerBox.setPromptText("Buyer");
        restaurantBox.setPromptText("Restaurant");
        driverBox.setPromptText("Driver");
        statusBox.setPromptText("Status");
        priceField.setPromptText("Total price (auto)");
        priceField.setEditable(false);
        chatArea.setPromptText("Chat messages…");
        chatArea.setPrefRowCount(3);

        buyerBox.setItems(basicUserService.getAllUsers().filtered(u -> !u.isAdmin()));
        restaurantBox.setItems(restaurantService.getAllRestaurants());
        driverBox.setItems(driverService.getAllDrivers());
        statusBox.setItems(FXCollections.observableArrayList(OrderStatus.values()));

        restaurantBox.setItems(restaurantService.getAllRestaurants());
        driverBox.setItems(driverService.getAllDrivers());
        statusBox.setItems(FXCollections.observableArrayList(OrderStatus.values()));

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

        // SUKURIAM FILTEREDLIST iš visų patiekalų
        filteredCuisines = new FilteredList<>(cuisineService.getAllCuisines(), c -> true);

        // NAUDOJAM filtered list vietoj all cuisines
        dishesList.setItems(filteredCuisines);
        dishesList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // kaip rodom patiekalų tekstą
        dishesList.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Cuisine item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null
                        ? ""
                        : item.getName() + " (" + item.getPrice() + ")");
            }
        });

        // kaip rodyti buyer
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

        addBtn.setOnAction(e -> addOrder());
        updateBtn.setOnAction(e -> updateOrder());
        deleteBtn.setOnAction(e -> deleteOrder());

        VBox form = new VBox(8,
                new Label("Order Form"),
                buyerBox,
                restaurantBox,
                driverBox,
                statusBox,
                new Label("Dishes (multi-select):"),
                dishesList,
                priceField,
                new Label("Chat:"),
                chatArea,
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
                text = dt.format(CREATED_AT_FORMATTER);  // yyyy-MM-dd HH:mm
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
                dateCol      // ← įdedam čia
        );

        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }


    private void fillForm(FoodOrder order) {
        buyerBox.setValue(order.getBuyer());
        restaurantBox.setValue(order.getRestaurant());
        driverBox.setValue(order.getDriver());
        statusBox.setValue(order.getStatus());

        // select dishes
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
        order.setCreatedAt(java.time.LocalDateTime.now());

        if (!chatArea.getText().isBlank()) {
            Chat chat = new Chat();
            chat.setMessages(chatArea.getText());
            chat.setOrder(order);
            order.setChat(chat);
        }

        orderService.add(order);
        priceField.setText(String.valueOf(totalPrice));
        showInfo("Order added.");
        order.setCreatedAt(LocalDateTime.now());
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
        showInfo("Order deleted.");
    }

    private boolean validate() {

        if (buyerBox.getValue() == null) {
            showError("Buyer required.");
            return false;
        }

        if (restaurantBox.getValue() == null) {
            showError("Select restaurant.");
            return false;
        }
        if (driverBox.getValue() == null) {
            showError("Select driver.");
            return false;
        }
        if (statusBox.getValue() == null) {
            showError("Select status.");
            return false;
        }
        if (dishesList.getSelectionModel().getSelectedItems().isEmpty()) {
            showError("Select at least one dish.");
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
