package ismin.cours.book.dto;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Statistiques calculées pour un livre. Objet de réponse uniquement :
 * il n'est jamais stocké en base.
 */
@Data
@AllArgsConstructor
public class Statistic {

    /** Note moyenne du livre. */
    private float mark;

    /** Nombre total d'avis reçus. */
    private int numberOfReviews;

    /** Nombre d'avis pour chaque note de 1 à 5. */
    private Map<Integer, Integer> markRepartition;

    /** Score pondéré utilisé pour le classement. */
    private float score;
}