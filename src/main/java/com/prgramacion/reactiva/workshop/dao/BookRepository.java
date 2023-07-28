package com.prgramacion.reactiva.workshop.dao;

import com.prgramacion.reactiva.workshop.model.Book;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface BookRepository extends ReactiveCrudRepository<Book, Long> {
}
