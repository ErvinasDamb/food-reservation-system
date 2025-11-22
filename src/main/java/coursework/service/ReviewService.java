package coursework.service;

import coursework.model.BasicUser;
import coursework.model.Driver;
import coursework.model.Restaurant;
import coursework.model.Review;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDateTime;

public class ReviewService {

    private final ObservableList<Review> reviews = FXCollections.observableArrayList();
    private int idCounter = 1;

    public Review createReview(BasicUser commentOwner,
                               BasicUser feedbackUser,
                               Restaurant restaurant,
                               Driver driver,
                               int rating,
                               String text) {

        Review r = new Review();
        r.setId(idCounter++); // in-memory ID
        r.setCommentOwner(commentOwner);
        r.setFeedbackUser(feedbackUser);
        r.setRestaurant(restaurant);
        r.setDriver(driver);
        r.setRating(rating);
        r.setText(text);
        r.setCreatedAt(LocalDateTime.now());
        r.setChat(null); // kol kas nenaudojam

        reviews.add(r);
        return r;
    }

    public ObservableList<Review> getAllReviews() {
        return reviews;
    }

    public boolean updateReview(Review updated) {
        for (int i = 0; i < reviews.size(); i++) {
            if (reviews.get(i).getId() == updated.getId()) {
                reviews.set(i, updated);
                return true;
            }
        }
        return false;
    }

    public boolean deleteReview(int id) {
        return reviews.removeIf(r -> r.getId() == id);
    }
}
