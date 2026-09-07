package ismin.cours.book.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ismin.cours.book.model.Book;

@Repository
public interface BookRepository extends JpaRepository<Book, Integer> {

    boolean existsByIsbn(long isbn);
}