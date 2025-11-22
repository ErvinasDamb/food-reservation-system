package coursework.service;

import coursework.model.Cuisine;
import coursework.model.Restaurant;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Optional;

public class CuisineService {

    private final ObservableList<Cuisine> cuisines = FXCollections.observableArrayList();
    private int idCounter = 1;

    public Cuisine createCuisine(String name,
                                 String ingredients,
                                 double price,
                                 boolean spicy,
                                 boolean vegan,
                                 Restaurant restaurant) {

        Cuisine c = new Cuisine();
        c.setId(idCounter++);
        c.setName(name);
        c.setIngredients(ingredients);
        c.setPrice(price);
        c.setSpicy(spicy);
        c.setVegan(vegan);
        c.setRestaurant(restaurant);

        cuisines.add(c);
        return c;
    }

    public ObservableList<Cuisine> getAllCuisines() {
        return cuisines;
    }

    public Cuisine findById(int id) {
        Optional<Cuisine> result = cuisines.stream()
                .filter(c -> c.getId() == id)
                .findFirst();
        return result.orElse(null);
    }

    public boolean updateCuisine(Cuisine updated) {
        for (int i = 0; i < cuisines.size(); i++) {
            if (cuisines.get(i).getId() == updated.getId()) {
                cuisines.set(i, updated);
                return true;
            }
        }
        return false;
    }

    public boolean deleteCuisine(int id) {
        return cuisines.removeIf(c -> c.getId() == id);
    }
}
