package coursework.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class User implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @Column(unique = true)
    protected String login;

    protected String password;
    protected String name;
    protected String surname;
    protected String phoneNumber;
    protected LocalDateTime dateCreated;
    protected LocalDateTime dateUpdated;
    protected boolean isAdmin;

    public User(String login, String password, String name, String surname, String phoneNumber) {
        this.login = login;
        this.password = password;
        this.name = name;
        this.surname = surname;
        this.phoneNumber = phoneNumber;
        this.dateCreated = LocalDateTime.now();
        this.dateUpdated = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "Name: " + name + " Surname: " + surname;
    }
}
