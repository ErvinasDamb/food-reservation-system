package coursework.service;

import coursework.model.Restaurant;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Optional;

public class RestaurantService {

    private final ObservableList<Restaurant> restaurants = FXCollections.observableArrayList();
    private long idCounter = 1;

    public Restaurant createRestaurant(String login, String password, String name, String surname,
                                       String phoneNumber, String address) {

        Restaurant r = new Restaurant();
        r.setId(idCounter++);
        r.setLogin(login);
        r.setPassword(password);
        r.setName(name);
        r.setSurname(surname);
        r.setPhoneNumber(phoneNumber);
        r.setAddress(address);

        restaurants.add(r);
        return r;
    }

    public ObservableList<Restaurant> getAllRestaurants() {
        return restaurants;
    }

    public Restaurant findById(long id) {
        Optional<Restaurant> result = restaurants.stream()
                .filter(r -> r.getId() == id)
                .findFirst();
        return result.orElse(null);
    }

    public boolean updateRestaurant(Restaurant updated) {
        for (int i = 0; i < restaurants.size(); i++) {
            if (restaurants.get(i).getId() == updated.getId()) {
                restaurants.set(i, updated);
                return true;
            }
        }
        return false;
    }

    public boolean deleteRestaurant(long id) {
        return restaurants.removeIf(r -> r.getId() == id);
    }
}
