package com.prgramacion.reactiva.workshop.service;

import com.prgramacion.reactiva.workshop.dao.BookRepository;
import com.prgramacion.reactiva.workshop.model.Book;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
public class BookService {

    private final BookRepository bookRepository;

    @Autowired
    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public Flux<Book> getAllBooks() {
        return bookRepository.findAll().delayElements(Duration.ofSeconds(1)).log();
    }

    public Flux<Book> getAllBooksBackPresure(int limitRequest) {
        // With the method delayElements we can emit elements by an interval of time
        Flux<Book> bookFlux = bookRepository.findAll().delayElements(Duration.ofSeconds(1)).log();
        // If the limitRequest is 2 for example, our subscribe will work with only 2 elements for batch of the data flux
        /*bookFlux.limitRate(limitRequest).subscribe(
                book -> {
                    System.out.println(book.getTitle());
                }
        );*/

        return bookFlux.limitRate(limitRequest); // Here we can handle the backpressure to control and limit the speed
                                                // of the elements which are emitted
        // BackPressure: It is a mechanism for balancing the speed of data production and consumption in reactive
        // applications, ensuring that the consumer is not overwhelmed with data and that the producer is not wasting
        // resources sending data that cannot be processed in time.
    }

    public Mono<Book> findById(Long id){
        return bookRepository.findById(id);
    }

    public Mono<Book> postBook(Book book) {
        return bookRepository.save(book).log();
    }

    public Mono<ResponseEntity<Book>> updateBook(Long id, Book book) {
        // Here, for example we can't use a map in the first operation because we need return a Mono<Book> with the
        // method save.
        return bookRepository.findById(id)
                .flatMap(oldBook -> {
                    oldBook.setTitle(book.getTitle());
                    oldBook.setAuthor(book.getAuthor());
                    return bookRepository.save(oldBook);
                })
                .map(updatedBook -> new ResponseEntity<>(updatedBook, HttpStatus.OK))
                .defaultIfEmpty(new ResponseEntity<>(HttpStatus.OK));
    }

    public Mono<Book> deleteUser(long id) {
        // Here we can see the advantage of FlatMap, because it allows us return a new flow(Mono or Flux)
        // Other advantage is that we can do operations by sequential way like the elimination
        return bookRepository.findById(id)
                .flatMap(deletedBook -> bookRepository.delete(deletedBook)
                        .then(Mono.just(deletedBook)));
    }

}
