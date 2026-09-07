
package ismin.cours.book.dto;

import ismin.cours.book.model.Book;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Un livre accompagné de son score pondéré, utilisé pour le classement.
 * Objet de réponse uniquement.
 */
@Data
@AllArgsConstructor
public class Score {

    /** Le livre concerné. */
    private Book book;

    /** Score pondéré calculé à partir des avis. */
    private float score;
}