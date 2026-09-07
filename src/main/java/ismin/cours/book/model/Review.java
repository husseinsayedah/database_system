package ismin.cours.book.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "REVIEW")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "BOOK_ISBN")
    private long bookIsbn;

    @Column(name = "REVIEW_DATE")
    private LocalDate reviewDate;

    @Column(name = "REVIEW_TEXT")
    private String reviewText;

    @Column(name = "REVIEW_RATING")
    private int reviewRating;
}
