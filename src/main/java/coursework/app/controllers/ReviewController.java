package coursework.app.controllers;

import coursework.model.BasicUser;
import coursework.model.Driver;
import coursework.model.Restaurant;
import coursework.model.Review;
import coursework.service.BasicUserService;
import coursework.service.DriverService;
import coursework.service.RestaurantService;
import coursework.service.ReviewService;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;

public class ReviewController {

    private final ReviewService reviewService;
    private final BasicUserService userService;
    private final RestaurantService restaurantService;
    private final DriverService driverService;

    private final TableView<Review> table = new TableView<>();

    private ComboBox<BasicUser> ownerBox;
    private ComboBox<BasicUser> feedbackBox;
    private ComboBox<Restaurant> restaurantBox;
    private ComboBox<Driver> driverBox;
    private ComboBox<Integer> ratingBox;
    private TextArea textArea;

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public ReviewController(ReviewService reviewService,
                            BasicUserService userService,
                            RestaurantService restaurantService,
                            DriverService driverService) {
        this.reviewService = reviewService;
        this.userService = userService;
        this.restaurantService = restaurantService;
        this.driverService = driverService;
    }

    public BorderPane getView() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        setupTable();
        root.setCenter(table);

        ownerBox = new ComboBox<>();
        feedbackBox = new ComboBox<>();
        restaurantBox = new ComboBox<>();
        driverBox = new ComboBox<>();
        ratingBox = new ComboBox<>();
        textArea = new TextArea();

        ownerBox.setPromptText("Comment owner (client)");
        restaurantBox.setPromptText("Restaurant (optional)");
        driverBox.setPromptText("Driver (optional)");
        feedbackBox.setPromptText("Handled by (staff)");
        ratingBox.setPromptText("Rating (1–5)");
        textArea.setPromptText("Review text");
        textArea.setPrefRowCount(3);

        // Tik klientai (isAdmin == false)
        ownerBox.setItems(userService.getAllUsers().filtered(u -> !u.isAdmin()));

        // Tik darbuotojai/adminai (isAdmin == true)
        feedbackBox.setItems(userService.getAllUsers().filtered(BasicUser::isAdmin));

        restaurantBox.setItems(restaurantService.getAllRestaurants());
        driverBox.setItems(driverService.getAllDrivers());

        // Rating 1–5
        ratingBox.getItems().addAll(1, 2, 3, 4, 5);

        // Kaip rodom user/restaurant/driver pavadinimus
        ownerBox.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(BasicUser item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" :
                        item.getName() + " " + item.getSurname() + " (Client)");
            }
        });
        ownerBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(BasicUser item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" :
                        item.getName() + " " + item.getSurname() + " (Client)");
            }
        });

        feedbackBox.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(BasicUser item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" :
                        item.getName() + " " + item.getSurname() + " (Staff)");
            }
        });
        feedbackBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(BasicUser item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" :
                        item.getName() + " " + item.getSurname() + " (Staff)");
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

        Button addBtn = new Button("Add review");
        Button updateBtn = new Button("Update review");
        Button deleteBtn = new Button("Delete review");

        addBtn.setOnAction(e -> addReview());
        updateBtn.setOnAction(e -> updateReview());
        deleteBtn.setOnAction(e -> deleteReview());

        VBox form = new VBox(8,
                new Label("Review Form"),
                ownerBox,
                restaurantBox,
                driverBox,
                feedbackBox,
                ratingBox,
                textArea,
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
        TableColumn<Review, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Review, String> typeCol = new TableColumn<>("Target type");
        typeCol.setCellValueFactory(cell -> {
            Review r = cell.getValue();
            String type;
            if (r.getRestaurant() != null) type = "Restaurant";
            else if (r.getDriver() != null) type = "Driver";
            else type = "-";
            return new SimpleStringProperty(type);
        });

        TableColumn<Review, String> targetCol = new TableColumn<>("Target");
        targetCol.setCellValueFactory(cell -> {
            Review r = cell.getValue();
            if (r.getRestaurant() != null) {
                return new SimpleStringProperty(r.getRestaurant().getName());
            } else if (r.getDriver() != null) {
                return new SimpleStringProperty(
                        r.getDriver().getName() + " " + r.getDriver().getSurname()
                );
            } else {
                return new SimpleStringProperty("-");
            }
        });

        TableColumn<Review, String> ownerCol = new TableColumn<>("Owner");
        ownerCol.setCellValueFactory(cell -> {
            BasicUser u = cell.getValue().getCommentOwner();
            return new SimpleStringProperty(
                    u != null ? u.getName() + " " + u.getSurname() : ""
            );
        });

        TableColumn<Review, String> handlerCol = new TableColumn<>("Handled by");
        handlerCol.setCellValueFactory(cell -> {
            BasicUser u = cell.getValue().getFeedbackUser();
            return new SimpleStringProperty(
                    u != null ? u.getName() + " " + u.getSurname() : ""
            );
        });

        TableColumn<Review, Integer> ratingCol = new TableColumn<>("Rating");
        ratingCol.setCellValueFactory(new PropertyValueFactory<>("rating"));

        TableColumn<Review, String> dateCol = new TableColumn<>("Created");
        dateCol.setCellValueFactory(cell ->
                new SimpleStringProperty(
                        cell.getValue().getCreatedAt() != null
                                ? cell.getValue().getCreatedAt().format(DATE_FORMATTER)
                                : ""
                ));

        TableColumn<Review, String> textCol = new TableColumn<>("Text");
        textCol.setCellValueFactory(cell ->
                new SimpleStringProperty(
                        cell.getValue().getText() != null
                                ? trimText(cell.getValue().getText())
                                : ""
                ));

        table.setItems(reviewService.getAllReviews());
        table.getColumns().setAll(
                idCol,
                typeCol,
                targetCol,
                ownerCol,
                handlerCol,
                ratingCol,
                dateCol,
                textCol
        );
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private String trimText(String t) {
        if (t.length() <= 40) return t;
        return t.substring(0, 37) + "...";
    }

    private void fillForm(Review r) {
        ownerBox.setValue(r.getCommentOwner());
        feedbackBox.setValue(r.getFeedbackUser());
        restaurantBox.setValue(r.getRestaurant());
        driverBox.setValue(r.getDriver());
        ratingBox.setValue(r.getRating());
        textArea.setText(r.getText() != null ? r.getText() : "");
    }

    private void addReview() {
        if (!validate()) return;

        Review r = reviewService.createReview(
                ownerBox.getValue(),
                feedbackBox.getValue(),
                restaurantBox.getValue(),
                driverBox.getValue(),
                ratingBox.getValue(),
                textArea.getText()
        );
        table.getSelectionModel().select(r);
        showInfo("Review added.");
    }

    private void updateReview() {
        Review selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Select review first.");
            return;
        }
        if (!validate()) return;

        selected.setCommentOwner(ownerBox.getValue());
        selected.setFeedbackUser(feedbackBox.getValue());
        selected.setRestaurant(restaurantBox.getValue());
        selected.setDriver(driverBox.getValue());
        selected.setRating(ratingBox.getValue());
        selected.setText(textArea.getText());

        reviewService.updateReview(selected);
        table.refresh();
        showInfo("Review updated.");
    }

    private void deleteReview() {
        Review selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Select review first.");
            return;
        }
        reviewService.deleteReview(selected.getId());
        showInfo("Review deleted.");
    }

    private boolean validate() {
        if (ownerBox.getValue() == null) {
            showError("Comment owner is required.");
            return false;
        }
        if (ratingBox.getValue() == null) {
            showError("Rating is required.");
            return false;
        }
        // Bent vienas iš target – restoranAS arba driveris
        if (restaurantBox.getValue() == null && driverBox.getValue() == null) {
            showError("Select restaurant OR driver.");
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
