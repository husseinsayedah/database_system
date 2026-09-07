package ismin.cours.book.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import ismin.cours.book.dto.Score;
import ismin.cours.book.dto.Statistic;
import ismin.cours.book.model.Book;
import ismin.cours.book.model.Review;
import ismin.cours.book.repository.BookRepository;
import ismin.cours.book.repository.ReviewRepository;

/**
 * Tests du calcul des statistiques et du classement pondere.
 */
@SpringBootTest
class StatisticServiceTest {

    private static final long ISBN_A = 1000000001L;
    private static final long ISBN_B = 1000000002L;

    @Autowired
    private StatisticService statisticService;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    /** Repart d'une base vide avant chaque test, pour que chacun soit independant. */
    @BeforeEach
    void setUp() {
        reviewRepository.deleteAll();
        bookRepository.deleteAll();
    }

    @Test
    void statisticsReflectTheReviewsOfTheBook() {
        // GIVEN un livre note 3, 4 et 5
        givenBook(ISBN_A, "Fondation");
        givenReview(ISBN_A, 3);
        givenReview(ISBN_A, 4);
        givenReview(ISBN_A, 5);

        // WHEN on demande ses statistiques
        Statistic statistic = statisticService.computeStatistic(ISBN_A);

        // THEN la moyenne, le nombre d'avis et la repartition correspondent
        assertEquals(4.0f, statistic.getMark(), 0.001f);
        assertEquals(3, statistic.getNumberOfReviews());

        Map<Integer, Integer> repartition = statistic.getMarkRepartition();
        assertEquals(0, repartition.get(1));
        assertEquals(0, repartition.get(2));
        assertEquals(1, repartition.get(3));
        assertEquals(1, repartition.get(4));
        assertEquals(1, repartition.get(5));
    }

    @Test
    void statisticsOfABookWithoutReviewAreEmptyButNotAnError() {
        // GIVEN un livre sans aucun avis, dans une base sans avis
        givenBook(ISBN_A, "Fondation");

        // WHEN on demande ses statistiques
        Statistic statistic = statisticService.computeStatistic(ISBN_A);

        // THEN tout est a zero, et aucune exception n'est levee
        assertEquals(0.0f, statistic.getMark(), 0.001f);
        assertEquals(0, statistic.getNumberOfReviews());
        assertEquals(0, statistic.getMarkRepartition().get(5));
    }

    @Test
    void repartitionAlwaysCoversTheFiveMarks() {
        // GIVEN un livre note uniquement 5
        givenBook(ISBN_A, "Fondation");
        givenReview(ISBN_A, 5);

        // WHEN on demande ses statistiques
        Statistic statistic = statisticService.computeStatistic(ISBN_A);

        // THEN les notes 1 a 5 sont toutes presentes, celles sans avis valant 0
        Map<Integer, Integer> repartition = statistic.getMarkRepartition();
        assertEquals(5, repartition.size());
        for (int mark = 1; mark <= 4; mark++) {
            assertEquals(0, repartition.get(mark));
        }
        assertEquals(1, repartition.get(5));
    }

    @Test
    void aSingleExcellentReviewIsPulledTowardTheGeneralAverage() {
        // GIVEN un livre avec un seul 5, et un livre avec dix 3
        givenBook(ISBN_A, "Un seul avis");
        givenReview(ISBN_A, 5);

        givenBook(ISBN_B, "Beaucoup d avis");
        for (int i = 0; i < 10; i++) {
            givenReview(ISBN_B, 3);
        }

        // WHEN on calcule le score du livre au seul avis
        Statistic statistic = statisticService.computeStatistic(ISBN_A);

        // THEN son score est tres inferieur a sa moyenne brute de 5,
        // car le seuil de confiance le rapproche de la moyenne generale
        assertEquals(5.0f, statistic.getMark(), 0.001f);
        assertTrue(statistic.getScore() < 3.5f,
                "un unique 5 ne doit pas donner un score proche de 5, obtenu : "
                        + statistic.getScore());
    }

    @Test
    void rankingIgnoresBooksWithoutAnyReview() {
        // GIVEN un livre note et un livre sans avis
        givenBook(ISBN_A, "Note");
        givenReview(ISBN_A, 4);
        givenBook(ISBN_B, "Jamais note");

        // WHEN on demande le classement
        List<Score> ranking = statisticService.computeRanking();

        // THEN seul le livre note y figure
        assertEquals(1, ranking.size());
        assertEquals(ISBN_A, ranking.get(0).getBook().getIsbn());
    }

    @Test
    void rankingIsSortedFromBestToWorstScore() {
        // GIVEN deux livres also notes, l un mieux que l autre
        givenBook(ISBN_A, "Moins bon");
        givenReview(ISBN_A, 2);
        givenReview(ISBN_A, 2);

        givenBook(ISBN_B, "Meilleur");
        givenReview(ISBN_B, 5);
        givenReview(ISBN_B, 5);

        // WHEN on demande le classement
        List<Score> ranking = statisticService.computeRanking();

        // THEN le meilleur score arrive en premier
        assertEquals(2, ranking.size());
        assertEquals(ISBN_B, ranking.get(0).getBook().getIsbn());
        assertTrue(ranking.get(0).getScore() > ranking.get(1).getScore());
    }

    @Test
    void rankingOfAnEmptyCatalogueIsEmpty() {
        // GIVEN aucune donnee
        // WHEN on demande le classement
        List<Score> ranking = statisticService.computeRanking();

        // THEN la liste est vide, et aucune exception n'est levee
        assertEquals(0, ranking.size());
    }

    private void givenBook(long isbn, String title) {
        Book book = new Book();
        book.setIsbn(isbn);
        book.setTitle(title);
        book.setWriter("Auteur");
        book.setPublishingDate(LocalDate.of(2000, 1, 1));
        bookRepository.save(book);
    }

    private void givenReview(long bookIsbn, int rating) {
        Review review = new Review();
        review.setBookIsbn(bookIsbn);
        review.setReviewRating(rating);
        review.setReviewText("avis");
        review.setReviewDate(LocalDate.of(2026, 1, 1));
        reviewRepository.save(review);
    }
}
