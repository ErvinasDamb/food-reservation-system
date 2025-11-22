package coursework.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    // Kas parašė review (VISADA klientas)
    @ManyToOne
    private BasicUser commentOwner;

    // Kas su tuo review dirba (tik darbuotojas/adminas)
    @ManyToOne
    private BasicUser feedbackUser;

    // Review tikslas – ARBA restoranas, ARBA driveris (viena iš šitų bus null)
    @ManyToOne
    private Restaurant restaurant;

    @ManyToOne
    private Driver driver;

    // Bendra žvaigždučių sistema 1–5
    private int rating;

    // Tekstinis komentaras
    private String text;

    // Kada review sukurtas (data + laikas)
    private LocalDateTime createdAt;

    // Chat – paliekam, bet dabar nenaudojam UI
    @OneToOne
    private Chat chat;
}
