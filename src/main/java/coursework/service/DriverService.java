package coursework.service;

import coursework.model.Driver;
import coursework.model.VehicleType;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDate;
import java.util.Optional;

public class DriverService {

    private final ObservableList<Driver> drivers = FXCollections.observableArrayList();
    private long idCounter = 1;

    public Driver createDriver(String login,
                               String password,
                               String name,
                               String surname,
                               String phoneNumber,
                               String address,
                               String licence,
                               LocalDate bDate,
                               VehicleType vehicleType) {

        Driver d = new Driver();
        d.setId(idCounter++);
        d.setLogin(login);
        d.setPassword(password);
        d.setName(name);
        d.setSurname(surname);
        d.setPhoneNumber(phoneNumber);
        d.setAddress(address);
        d.setLicence(licence);
        d.setBDate(bDate);
        d.setVehicleType(vehicleType);

        drivers.add(d);
        return d;
    }

    public ObservableList<Driver> getAllDrivers() {
        return drivers;
    }

    public Driver findById(long id) {
        Optional<Driver> result = drivers.stream()
                .filter(d -> d.getId() == id)
                .findFirst();
        return result.orElse(null);
    }

    public boolean updateDriver(Driver updated) {
        for (int i = 0; i < drivers.size(); i++) {
            if (drivers.get(i).getId() == updated.getId()) {
                drivers.set(i, updated);
                return true;
            }
        }
        return false;
    }

    public boolean deleteDriver(long id) {
        return drivers.removeIf(d -> d.getId() == id);
    }
}
