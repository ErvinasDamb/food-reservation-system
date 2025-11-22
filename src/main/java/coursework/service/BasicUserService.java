package coursework.service;

import coursework.model.BasicUser;
import java.util.Optional;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class BasicUserService {

    private final ObservableList<BasicUser> users = FXCollections.observableArrayList();
    private long idCounter = 1;

    // CREATE
    public BasicUser createBasicUser(String login, String password, String name, String surname,
                                     String phoneNumber, String address, boolean isAdmin) {

        BasicUser user = new BasicUser();
        user.setId(idCounter++);
        user.setLogin(login);
        user.setPassword(password);
        user.setName(name);
        user.setSurname(surname);
        user.setPhoneNumber(phoneNumber);
        user.setAddress(address);
        user.setAdmin(isAdmin);

        users.add(user);
        return user;
    }

    // READ ALL
    public ObservableList<BasicUser> getAllUsers() {
        return users;
    }

    // READ ONE
    public BasicUser findById(long id) {
        Optional<BasicUser> result = users.stream()
                .filter(u -> u.getId() == id)
                .findFirst();
        return result.orElse(null);
    }

    // UPDATE
    public boolean updateBasicUser(BasicUser updated) {
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getId() == updated.getId()) {
                users.set(i, updated);
                return true;
            }
        }
        return false;
    }

    // DELETE
    public boolean deleteBasicUser(long id) {
        return users.removeIf(u -> u.getId() == id);
    }
}
