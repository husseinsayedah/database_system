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
import ismin.cours.book.repository.BookRepository;
import ismin.cours.book.repository.ReviewRepository;

/**
 * Tests des regles appliquees a la creation et a la modification d un livre.
 */
@SpringBootTest
class BookControllerTest {

    /**
     * Le sujet demande l ISBN 0123456789. Un ISBN est modelise par un long,
     * or un nombre ne conserve pas son zero initial : 0123456789 devient
     * 123456789, soit neuf chiffres, que la validation refuse. On utilise donc
     * un ISBN a dix chiffres reellement representable.
     */
    private static final long ISBN_FONDATION = 1234567890L;

    @Autowired
    private BookController bookController;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @BeforeEach
    void setUp() {
        reviewRepository.deleteAll();
        bookRepository.deleteAll();
    }

    @Test
    void addingABookWithAnAlreadyUsedIsbnIsRejected() {
        // GIVEN une base contenant "fondation" avec un ISBN donne
        bookController.addBook(book(ISBN_FONDATION, "fondation", "Isaac Asimov"));

        // WHEN on ajoute "Le monde sans fin" avec le meme ISBN
        Book duplicate = book(ISBN_FONDATION, "Le monde sans fin", "Jean-Marc Jancovici");

        // THEN la creation est refusee par un conflit
        ResponseStatusException thrown = assertThrows(ResponseStatusException.class,
                () -> bookController.addBook(duplicate));
        assertEquals(HttpStatus.CONFLICT, thrown.getStatusCode());
        assertEquals(1, bookRepository.count());
    }

    @Test
    void addingTwoBooksWithDifferentIsbnIsAccepted() {
        // GIVEN une base contenant "fondation"
        bookController.addBook(book(ISBN_FONDATION, "fondation", "Isaac Asimov"));

        // WHEN on ajoute un autre livre avec un ISBN different
        bookController.addBook(book(9781234567897L, "Le monde sans fin", "Jean-Marc Jancovici"));

        // THEN les deux livres coexistent
        assertEquals(2, bookRepository.count());
    }

    @Test
    void anIsbnWithTheWrongNumberOfDigitsIsRejected() {
        // GIVEN un livre dont l ISBN ne compte que trois chiffres
        Book invalid = book(123L, "Trop court", "Auteur");

        // WHEN on tente de l ajouter
        // THEN la requete est refusee comme invalide
        ResponseStatusException thrown = assertThrows(ResponseStatusException.class,
                () -> bookController.addBook(invalid));
        assertEquals(HttpStatus.BAD_REQUEST, thrown.getStatusCode());
        assertEquals(0, bookRepository.count());
    }

    @Test
    void aBookWithoutTitleIsRejected() {
        // GIVEN un livre sans titre
        Book invalid = book(ISBN_FONDATION, null, "Auteur");

        // WHEN on tente de l ajouter
        // THEN la requete est refusee comme invalide
        ResponseStatusException thrown = assertThrows(ResponseStatusException.class,
                () -> bookController.addBook(invalid));
        assertEquals(HttpStatus.BAD_REQUEST, thrown.getStatusCode());
    }

    @Test
    void aBookWithABlankTitleIsRejected() {
        // GIVEN un livre dont le titre ne contient que des espaces
        Book invalid = book(ISBN_FONDATION, "   ", "Auteur");

        // WHEN on tente de l ajouter
        // THEN la requete est refusee comme invalide
        ResponseStatusException thrown = assertThrows(ResponseStatusException.class,
                () -> bookController.addBook(invalid));
        assertEquals(HttpStatus.BAD_REQUEST, thrown.getStatusCode());
    }

    @Test
    void readingAnUnknownBookIsRejected() {
        // GIVEN une base vide
        // WHEN on demande un livre inexistant
        // THEN la ressource est declaree absente
        ResponseStatusException thrown = assertThrows(ResponseStatusException.class,
                () -> bookController.getBookById(404));
        assertEquals(HttpStatus.NOT_FOUND, thrown.getStatusCode());
    }

    @Test
    void deletingAnUnknownBookIsRejected() {
        // GIVEN une base vide
        // WHEN on supprime un livre inexistant
        // THEN la ressource est declaree absente
        ResponseStatusException thrown = assertThrows(ResponseStatusException.class,
                () -> bookController.deleteBook(404));
        assertEquals(HttpStatus.NOT_FOUND, thrown.getStatusCode());
    }

    @Test
    void deletingAnExistingBookRemovesIt() {
        // GIVEN un livre enregistre
        Book saved = bookController.addBook(book(ISBN_FONDATION, "fondation", "Isaac Asimov"));

        // WHEN on le supprime
        bookController.deleteBook(saved.getId());

        // THEN il ne reste rien en base
        assertEquals(0, bookRepository.count());
    }

    private Book book(long isbn, String title, String writer) {
        Book book = new Book();
        book.setIsbn(isbn);
        book.setTitle(title);
        book.setWriter(writer);
        book.setPublishingDate(LocalDate.of(1951, 6, 1));
        return book;
    }
}
