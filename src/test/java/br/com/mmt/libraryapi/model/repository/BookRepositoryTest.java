package br.com.mmt.libraryapi.model.repository;

import br.com.mmt.libraryapi.model.entity.Book;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest
public class BookRepositoryTest {

    @Autowired
    TestEntityManager entityManager;

    @Autowired
    BookRepository repository;

    @Test
    @DisplayName("Deve retornar verdadeiro quando existir um livro na base com o isbn informado")
    public void returTrueWhenIsbnExists(){

        //cenario
        String isbn = "123";
        Book book = createMockBook();
        entityManager.persist(book);
        //execução
        boolean exists = repository.existsByIsbn(isbn);
        //verificação
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Deve retornar false quando não existir um livro na base com o isbn informado")
    public void returFalseWhenIsbnDoesntExists(){
        //cenario
        String isbn = "123";
        //execução
        boolean exists = repository.existsByIsbn(isbn);
        //verificação
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Deve retornar um livro de acordo com o id informado")
    public void findByIdTest(){

        //Cenário
        Book book = createMockBook();
        entityManager.persist(book);
        //Execução
        Optional<Book> foundBook = repository.findById(book.getId());
        //Validação
        assertThat(foundBook.isPresent()).isTrue();
    }

    @Test
    @DisplayName("Deve salvar um livro")
    public void saveBookTest(){

        //Cenário
        Book book = createMockBook();
        //Execução
        Book savedBook = repository.save(book);
        //Validação
        assertThat(savedBook.getId()).isNotNull();
    }

    @Test
    @DisplayName("Deve deletar um livro")
    public void deletBookTest(){

        //Cenário
        Book book = createMockBook();
        entityManager.persist(book);
        Book foundBook = entityManager.find(Book.class, book.getId());
        //Execução
        repository.delete(foundBook);
        //Validação
        Book deletedBook = entityManager.find(Book.class, book.getId());

        assertThat(deletedBook).isNull();
    }

    public static Book createMockBook() {
        return Book.builder()
                .author("Moises")
                .isbn("123")
                .title("Api Restfull")
                .build();
    }
}
