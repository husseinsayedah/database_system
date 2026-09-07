package ismin.cours.book.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import ismin.cours.book.model.Book;
import ismin.cours.book.model.Review;
import ismin.cours.book.repository.BookRepository;
import ismin.cours.book.repository.ReviewRepository;

/**
 * Tests des regles appliquees a la publication d un avis.
 */
@SpringBootTest
class ReviewControllerTest {

    private static final long KNOWN_ISBN = 1234567890L;
    private static final long UNKNOWN_ISBN = 9999999999L;

    @Autowired
    private ReviewController reviewController;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @BeforeEach
    void setUp() {
        reviewRepository.deleteAll();
        bookRepository.deleteAll();

        Book book = new Book();
        book.setIsbn(KNOWN_ISBN);
        book.setTitle("fondation");
        book.setWriter("Isaac Asimov");
        book.setPublishingDate(LocalDate.of(1951, 6, 1));
        bookRepository.save(book);
    }

    @Test
    void aReviewOnAnExistingBookIsAccepted() {
        // GIVEN un avis valide sur un livre du catalogue
        Review review = review(KNOWN_ISBN, 4);

        // WHEN on le publie
        Review saved = reviewController.addReview(review);

        // THEN il est enregistre
        assertEquals(4, saved.getReviewRating());
        assertEquals(1, reviewRepository.count());
    }

    @Test
    void aReviewOnAnUnknownBookIsRejected() {
        // GIVEN un avis portant sur un ISBN absent du catalogue
        Review review = review(UNKNOWN_ISBN, 4);

        // WHEN on tente de le publier
        // THEN le livre est declare absent
        ResponseStatusException thrown = assertThrows(ResponseStatusException.class,
                () -> reviewController.addReview(review));
        assertEquals(HttpStatus.NOT_FOUND, thrown.getStatusCode());
        assertEquals(0, reviewRepository.count());
    }

    @Test
    void aRatingAboveFiveIsRejected() {
        // GIVEN un avis note 47
        Review review = review(KNOWN_ISBN, 47);

        // WHEN on tente de le publier
        // THEN la requete est refusee comme invalide
        ResponseStatusException thrown = assertThrows(ResponseStatusException.class,
                () -> reviewController.addReview(review));
        assertEquals(HttpStatus.BAD_REQUEST, thrown.getStatusCode());
    }

    @Test
    void aRatingOfZeroIsRejected() {
        // GIVEN un avis note 0, alors que la note minimale est 1
        Review review = review(KNOWN_ISBN, 0);

        // WHEN on tente de le publier
        // THEN la requete est refusee comme invalide
        ResponseStatusException thrown = assertThrows(ResponseStatusException.class,
                () -> reviewController.addReview(review));
        assertEquals(HttpStatus.BAD_REQUEST, thrown.getStatusCode());
    }

    @Test
    void theBoundsOneAndFiveAreAccepted() {
        // GIVEN les deux notes extremes autorisees
        // WHEN on les publie
        reviewController.addReview(review(KNOWN_ISBN, 1));
        reviewController.addReview(review(KNOWN_ISBN, 5));

        // THEN les deux avis sont enregistres
        assertEquals(2, reviewRepository.count());
    }

    @Test
    void aReviewWithoutDateIsRejected() {
        // GIVEN un avis sans date de publication
        Review review = review(KNOWN_ISBN, 4);
        review.setReviewDate(null);

        // WHEN on tente de le publier
        // THEN la requete est refusee comme invalide
        ResponseStatusException thrown = assertThrows(ResponseStatusException.class,
                () -> reviewController.addReview(review));
        assertEquals(HttpStatus.BAD_REQUEST, thrown.getStatusCode());
    }

    @Test
    void updatingAReviewWithAnInvalidRatingIsRejected() {
        // GIVEN un avis valide deja enregistre
        Review saved = reviewController.addReview(review(KNOWN_ISBN, 4));

        // WHEN on le modifie avec une note hors bornes
        saved.setReviewRating(9);

        // THEN la modification est refusee, comme la creation le serait
        ResponseStatusException thrown = assertThrows(ResponseStatusException.class,
                () -> reviewController.updateReview(saved));
        assertEquals(HttpStatus.BAD_REQUEST, thrown.getStatusCode());
    }

    @Test
    void deletingAnUnknownReviewIsRejected() {
        // GIVEN aucun avis en base
        // WHEN on supprime un avis inexistant
        // THEN la ressource est declaree absente
        ResponseStatusException thrown = assertThrows(ResponseStatusException.class,
                () -> reviewController.deleteReview(404));
        assertEquals(HttpStatus.NOT_FOUND, thrown.getStatusCode());
    }

    private Review review(long bookIsbn, int rating) {
        Review review = new Review();
        review.setBookIsbn(bookIsbn);
        review.setReviewRating(rating);
        review.setReviewText("avis");
        review.setReviewDate(LocalDate.of(2026, 1, 1));
        return review;
    }
}
