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

import ismin.cours.book.model.Book;
import ismin.cours.book.repository.BookRepository;

@RestController
@RequestMapping("/api/book")
public class BookController {

    /** Un ISBN valide compte 10 chiffres (ancien format) ou 13 (format actuel). */
    private static final int ISBN_SHORT_LENGTH = 10;
    private static final int ISBN_LONG_LENGTH = 13;

    private final BookRepository bookRepository;

    public BookController(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @GetMapping
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    @GetMapping("/{id}")
    public Book getBookById(@PathVariable Integer id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No book with id " + id));
    }

    @PostMapping
    public Book addBook(@RequestBody Book book) {
        validate(book);
        if (bookRepository.existsByIsbn(book.getIsbn())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A book with ISBN " + book.getIsbn() + " already exists");
        }
        return bookRepository.save(book);
    }

    @PutMapping
    public Book updateBook(@RequestBody Book book) {
        validate(book);
        return bookRepository.save(book);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBook(@PathVariable Integer id) {
        if (!bookRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "No book with id " + id);
        }
        bookRepository.deleteById(id);
    }

    /**
     * Contrôle qu'un livre est exploitable avant de l'enregistrer :
     * ISBN de 10 ou 13 chiffres, titre, auteur et date de publication présents.
     */
    private void validate(Book book) {
        long isbn = book.getIsbn();
        if (isbn <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "isbn must be a positive number, received " + isbn);
        }
        int digits = String.valueOf(isbn).length();
        if (digits != ISBN_SHORT_LENGTH && digits != ISBN_LONG_LENGTH) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "isbn must contain " + ISBN_SHORT_LENGTH + " or " + ISBN_LONG_LENGTH
                            + " digits, received " + digits);
        }
        requireText(book.getTitle(), "title");
        requireText(book.getWriter(), "writer");
        if (book.getPublishingDate() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "publishingDate is required");
        }
    }

    /** Refuse une chaîne absente ou vide. */
    private void requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, fieldName + " is required");
        }
    }
}
