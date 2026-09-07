package ismin.cours.book.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ismin.cours.book.model.Review;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {

    List<Review> findByBookIsbn(long bookIsbn);
}
