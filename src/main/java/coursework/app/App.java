package coursework.app;

import coursework.app.controllers.*;
import coursework.service.*;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) {

        // shared services
        BasicUserService basicUserService = new BasicUserService();
        RestaurantService restaurantService = new RestaurantService();
        DriverService driverService = new DriverService();
        CuisineService cuisineService = new CuisineService();
        OrderService orderService = new OrderService();
        ReviewService reviewService = new ReviewService();

        // controllers
        UsersController usersController = new UsersController(basicUserService);
        RestaurantController restaurantController = new RestaurantController(restaurantService);
        DriversController driversController = new DriversController(driverService);
        CuisineController cuisineController = new CuisineController(restaurantService, cuisineService);
        OrdersController ordersController = new OrdersController(
                orderService,
                restaurantService,
                driverService,
                cuisineService,
                basicUserService
        );
        ReviewController reviewController = new ReviewController(
                reviewService,
                basicUserService,
                restaurantService,
                driverService
        );

        // tabs
        TabPane tabPane = new TabPane();

        Tab usersTab = new Tab("Users", usersController.getView());
        usersTab.setClosable(false);

        Tab restaurantsTab = new Tab("Restaurants", restaurantController.getView());
        restaurantsTab.setClosable(false);

        Tab driversTab = new Tab("Drivers", driversController.getView());
        driversTab.setClosable(false);

        Tab cuisineTab = new Tab("Cuisine", cuisineController.getView());
        cuisineTab.setClosable(false);

        Tab ordersTab = new Tab("Orders", ordersController.getView());
        ordersTab.setClosable(false);

        Tab reviewsTab = new Tab("Reviews", reviewController.getView());
        reviewsTab.setClosable(false);

        tabPane.getTabs().addAll(
                usersTab,
                restaurantsTab,
                driversTab,
                cuisineTab,
                ordersTab,
                reviewsTab
        );


        Scene scene = new Scene(tabPane, 1000, 600);
        primaryStage.setTitle("Budget Wolt System");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
