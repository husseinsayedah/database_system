package ismin.cours.book.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import ismin.cours.book.model.Review;
import ismin.cours.book.repository.BookRepository;
import ismin.cours.book.repository.ReviewRepository;

@RestController
@RequestMapping("/api/review")
public class ReviewController {

    /** Bornes autorisées pour une note, conformément au sujet. */
    private static final int MIN_RATING = 1;
    private static final int MAX_RATING = 5;

    private final ReviewRepository reviewRepository;
    private final BookRepository bookRepository;

    public ReviewController(ReviewRepository reviewRepository, BookRepository bookRepository) {
        this.reviewRepository = reviewRepository;
        this.bookRepository = bookRepository;
    }

    @GetMapping
    public List<Review> getAllReviews() {
        return reviewRepository.findAll();
    }

    @GetMapping("/{id}")
    public Review getReviewById(@PathVariable Integer id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No review with id " + id));
    }

    @PostMapping
    public Review addReview(@RequestBody Review review) {
        validate(review);
        return reviewRepository.save(review);
    }

    @PutMapping
    public Review updateReview(@RequestBody Review review) {
        validate(review);
        return reviewRepository.save(review);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReview(@PathVariable Integer id) {
        if (!reviewRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "No review with id " + id);
        }
        reviewRepository.deleteById(id);
    }

    /**
     * Contrôle qu'un avis est exploitable avant de l'enregistrer :
     * note dans les bornes, date présente, et livre existant.
     */
    private void validate(Review review) {
        if (review.getReviewRating() < MIN_RATING || review.getReviewRating() > MAX_RATING) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "reviewRating must be between " + MIN_RATING + " and " + MAX_RATING
                            + ", received " + review.getReviewRating());
        }
        if (review.getReviewDate() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "reviewDate is required");
        }
        requireExistingBook(review.getBookIsbn());
    }

    private void requireExistingBook(long bookIsbn) {
        if (!bookRepository.existsByIsbn(bookIsbn)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "No book with ISBN " + bookIsbn);
        }
    }
}
