package coursework.service;

import coursework.model.FoodOrder;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class OrderService {

    private final ObservableList<FoodOrder> orders = FXCollections.observableArrayList();
    private int idCounter = 1;

    public void add(FoodOrder order) {
        order.setId(idCounter++);
        orders.add(order);
    }

    public ObservableList<FoodOrder> getAllOrders() {
        return orders;
    }

    public boolean update(FoodOrder updated) {
        for (int i = 0; i < orders.size(); i++) {
            if (orders.get(i).getId() == updated.getId()) {
                orders.set(i, updated);
                return true;
            }
        }
        return false;
    }

    public boolean delete(int id) {
        return orders.removeIf(o -> o.getId() == id);
    }
}
