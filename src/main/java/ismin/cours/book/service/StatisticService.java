package ismin.cours.book.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import ismin.cours.book.dto.Score;
import ismin.cours.book.dto.Statistic;
import ismin.cours.book.model.Book;
import ismin.cours.book.model.Review;
import ismin.cours.book.repository.BookRepository;
import ismin.cours.book.repository.ReviewRepository;

/**
 * Exploite les avis pour produire les statistiques d'un livre
 * et le classement pondéré du catalogue.
 */
@Service
public class StatisticService {

    /**
     * Nombre minimal d'avis à partir duquel on fait confiance à la moyenne
     * d'un livre. En dessous, son score est tiré vers la moyenne générale.
     */
    private static final int CONFIDENCE_THRESHOLD = 10;

    private final BookRepository bookRepository;
    private final ReviewRepository reviewRepository;

    public StatisticService(BookRepository bookRepository, ReviewRepository reviewRepository) {
        this.bookRepository = bookRepository;
        this.reviewRepository = reviewRepository;
    }

    /**
     * Statistiques d'un livre : note moyenne, nombre d'avis,
     * répartition des notes de 1 à 5 et score pondéré.
     */
    public Statistic computeStatistic(long isbn) {
        List<Review> reviews = reviewRepository.findByBookIsbn(isbn);

        float mark = averageRating(reviews);
        float score = weightedScore(mark, reviews.size(), globalAverage());

        return new Statistic(mark, reviews.size(), markRepartition(reviews), score);
    }

    /**
     * Les livres évalués du catalogue, classés du meilleur score au moins bon.
     *
     * <p>Les livres sans aucun avis sont exclus : la formule leur attribuerait
     * exactement la moyenne générale {@code C}, ce qui les placerait au-dessus
     * de livres réellement notés mais légèrement en dessous de cette moyenne.
     * Un classement ne doit refléter que des livres que des lecteurs ont notés.
     */
    public List<Score> computeRanking() {
        float globalAverage = globalAverage();
        List<Score> ranking = new ArrayList<>();

        for (Book book : bookRepository.findAll()) {
            List<Review> reviews = reviewRepository.findByBookIsbn(book.getIsbn());
            if (reviews.isEmpty()) {
                continue;
            }
            float score = weightedScore(averageRating(reviews), reviews.size(), globalAverage);
            ranking.add(new Score(book, score));
        }

        ranking.sort(Comparator.comparing(Score::getScore).reversed());
        return ranking;
    }

    /** Moyenne des notes d'une liste d'avis, 0 si la liste est vide. */
    private float averageRating(List<Review> reviews) {
        if (reviews.isEmpty()) {
            return 0f;
        }
        int total = 0;
        for (Review review : reviews) {
            total += review.getReviewRating();
        }
        return (float) total / reviews.size();
    }

    /** Nombre d'avis pour chaque note de 1 à 5, les notes sans avis valant 0. */
    private Map<Integer, Integer> markRepartition(List<Review> reviews) {
        Map<Integer, Integer> repartition = new LinkedHashMap<>();
        for (int rating = 1; rating <= 5; rating++) {
            repartition.put(rating, 0);
        }
        for (Review review : reviews) {
            repartition.merge(review.getReviewRating(), 1, Integer::sum);
        }
        return repartition;
    }

    /** Moyenne de toutes les notes du catalogue, tous livres confondus. */
    private float globalAverage() {
        return averageRating(reviewRepository.findAll());
    }

    /**
     * Score pondéré : score = (v / (v + m)) * R + (m / (v + m)) * C
     *
     * @param bookAverage   R, la moyenne du livre
     * @param reviewCount   v, son nombre d'avis
     * @param globalAverage C, la moyenne générale du catalogue
     */
    private float weightedScore(float bookAverage, int reviewCount, float globalAverage) {
        float v = reviewCount;
        float m = CONFIDENCE_THRESHOLD;
        return (v / (v + m)) * bookAverage + (m / (v + m)) * globalAverage;
    }
}
