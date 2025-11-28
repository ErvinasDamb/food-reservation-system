package coursework.app.controllers;

import coursework.model.BasicUser;
import coursework.model.Driver;
import coursework.model.Restaurant;
import coursework.model.Review;
import coursework.service.BasicUserService;
import coursework.service.DriverService;
import coursework.service.RestaurantService;
import coursework.service.ReviewService;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class ReviewController {

    private final ReviewService reviewService;
    private final BasicUserService userService;
    private final RestaurantService restaurantService;
    private final DriverService driverService;

    // TableView inicializuojam čia ir daugiau NEBEKURIAM iš naujo
    private final TableView<Review> table = new TableView<>();

    private ComboBox<BasicUser> ownerBox;
    private ComboBox<BasicUser> feedbackBox;
    private ComboBox<Restaurant> restaurantBox;
    private ComboBox<Driver> driverBox;
    private ComboBox<Integer> ratingBox;
    private TextArea textArea;
    private RadioButton restaurantRadio;
    private RadioButton driverRadio;

    public ReviewController(ReviewService reviewService,
                            BasicUserService userService,
                            RestaurantService restaurantService,
                            DriverService driverService) {
        this.reviewService = reviewService;
        this.userService = userService;
        this.restaurantService = restaurantService;
        this.driverService = driverService;
    }

    public Parent getView() {
        BorderPane root = new BorderPane();

        // --- TABLE ---

        TableColumn<Review, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(c ->
                new SimpleLongProperty(c.getValue().getId()).asObject()
        );

        TableColumn<Review, String> ownerCol = new TableColumn<>("Owner");
        ownerCol.setCellValueFactory(c ->
                new SimpleStringProperty(
                        c.getValue().getCommentOwner() != null
                                ? c.getValue().getCommentOwner().getLogin()
                                : ""
                )
        );

        TableColumn<Review, String> targetCol = new TableColumn<>("Target");
        targetCol.setCellValueFactory(c -> {
            Review r = c.getValue();
            if (r.getRestaurant() != null) {
                return new SimpleStringProperty("Restaurant: " + r.getRestaurant().getName());
            } else if (r.getDriver() != null) {
                return new SimpleStringProperty("Driver: " + r.getDriver().getName());
            } else {
                return new SimpleStringProperty("");
            }
        });

        TableColumn<Review, String> feedbackCol = new TableColumn<>("Feedback user");
        feedbackCol.setCellValueFactory(c ->
                new SimpleStringProperty(
                        c.getValue().getFeedbackUser() != null
                                ? c.getValue().getFeedbackUser().getLogin()
                                : ""
                )
        );

        TableColumn<Review, Integer> ratingCol = new TableColumn<>("Rating");
        ratingCol.setCellValueFactory(c ->
                new SimpleIntegerProperty(c.getValue().getRating()).asObject()
        );

        TableColumn<Review, String> textCol = new TableColumn<>("Text");
        textCol.setCellValueFactory(c ->
                new SimpleStringProperty(
                        c.getValue().getText() != null ? c.getValue().getText() : ""
                )
        );

        table.getColumns().setAll(idCol, ownerCol, targetCol, feedbackCol, ratingCol, textCol);
        table.setItems(FXCollections.observableArrayList(reviewService.getAllReviews()));

        table.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, selected) -> {
                    if (selected != null) {
                        fillForm(selected);
                    }
                });

        root.setCenter(table);

        // --- FORM ---

        ownerBox = new ComboBox<>();
        feedbackBox = new ComboBox<>();
        restaurantBox = new ComboBox<>();
        driverBox = new ComboBox<>();
        ratingBox = new ComboBox<>();
        textArea = new TextArea();

        // owner – tik ne adminai
        ownerBox.setItems(FXCollections.observableArrayList(
                userService.getAllUsers()
                        .stream()
                        .filter(u -> !u.isAdmin())
                        .toList()
        ));

        // feedback – tik adminai
        feedbackBox.setItems(FXCollections.observableArrayList(
                userService.getAllUsers()
                        .stream()
                        .filter(BasicUser::isAdmin)
                        .toList()
        ));

        restaurantBox.setItems(FXCollections.observableArrayList(
                restaurantService.getAllRestaurants()
        ));
        driverBox.setItems(FXCollections.observableArrayList(
                driverService.getAllDrivers()
        ));

        ratingBox.getItems().setAll(1, 2, 3, 4, 5);

        // -- target type: restaurant / driver --

        ToggleGroup targetGroup = new ToggleGroup();
        restaurantRadio = new RadioButton("Restaurant review");
        driverRadio = new RadioButton("Driver review");
        restaurantRadio.setToggleGroup(targetGroup);
        driverRadio.setToggleGroup(targetGroup);

        restaurantRadio.selectedProperty().addListener((obs, oldVal, isSelected) -> {
            if (isSelected) {
                driverBox.setValue(null);
            }
        });
        driverRadio.selectedProperty().addListener((obs, oldVal, isSelected) -> {
            if (isSelected) {
                restaurantBox.setValue(null);
            }
        });

        Button addBtn = new Button("Add review");
        Button updateBtn = new Button("Update review");
        Button deleteBtn = new Button("Delete review");

        addBtn.setOnAction(e -> addReview());
        updateBtn.setOnAction(e -> updateReview());
        deleteBtn.setOnAction(e -> deleteReview());

        VBox form = new VBox(8,
                new Label("Review form"),
                new Label("Owner:"),
                ownerBox,
                restaurantRadio,
                restaurantBox,
                driverRadio,
                driverBox,
                new Label("Feedback user:"),
                feedbackBox,
                new Label("Rating:"),
                ratingBox,
                new Label("Text:"),
                textArea,
                addBtn,
                updateBtn,
                deleteBtn
        );
        form.setPadding(new Insets(10));

        root.setRight(form);

        return root;
    }

    private void fillForm(Review r) {
        ownerBox.setValue(r.getCommentOwner());
        feedbackBox.setValue(r.getFeedbackUser());
        restaurantBox.setValue(r.getRestaurant());
        driverBox.setValue(r.getDriver());
        ratingBox.setValue(r.getRating());
        textArea.setText(r.getText() != null ? r.getText() : "");

        if (r.getRestaurant() != null) {
            restaurantRadio.setSelected(true);
        } else if (r.getDriver() != null) {
            driverRadio.setSelected(true);
        } else {
            restaurantRadio.setSelected(false);
            driverRadio.setSelected(false);
        }
    }

    private void addReview() {
        if (!validate()) return;

        Restaurant targetRestaurant = restaurantRadio.isSelected() ? restaurantBox.getValue() : null;
        Driver targetDriver = driverRadio.isSelected() ? driverBox.getValue() : null;

        Review r = reviewService.createReview(
                ownerBox.getValue(),
                feedbackBox.getValue(),
                targetRestaurant,
                targetDriver,
                ratingBox.getValue(),
                textArea.getText()
        );

        // pridėti į TableView, nes items yra snapshot’as, o ne live ObservableList iš service
        table.getItems().add(r);
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
        selected.setRestaurant(restaurantRadio.isSelected() ? restaurantBox.getValue() : null);
        selected.setDriver(driverRadio.isSelected() ? driverBox.getValue() : null);
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
        table.getItems().remove(selected);
        showInfo("Review deleted.");
    }

    private boolean validate() {
        StringBuilder errors = new StringBuilder();

        if (ownerBox.getValue() == null) {
            errors.append("- Comment owner is required.\n");
        }
        if (ratingBox.getValue() == null) {
            errors.append("- Rating is required.\n");
        }

        boolean restaurantSelected = restaurantRadio.isSelected();
        boolean driverSelected = driverRadio.isSelected();

        if (!restaurantSelected && !driverSelected) {
            errors.append("- Select review type: restaurant or driver.\n");
        } else {
            if (restaurantSelected && restaurantBox.getValue() == null) {
                errors.append("- Select restaurant for restaurant review.\n");
            }
            if (driverSelected && driverBox.getValue() == null) {
                errors.append("- Select driver for driver review.\n");
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
