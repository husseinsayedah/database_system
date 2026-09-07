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
        requireExistingBook(review.getBookIsbn());
        return reviewRepository.save(review);
    }

    @PutMapping
    public Review updateReview(@RequestBody Review review) {
        requireExistingBook(review.getBookIsbn());
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

    private void requireExistingBook(long bookIsbn) {
        if (!bookRepository.existsByIsbn(bookIsbn)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "No book with ISBN " + bookIsbn);
        }
    }
}