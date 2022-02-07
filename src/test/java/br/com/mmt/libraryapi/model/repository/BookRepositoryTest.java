package br.com.mmt.libraryapi.model.repository;

import br.com.mmt.libraryapi.model.entity.Book;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
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

    private Book createMockBook() {
        return Book.builder()
                .author("Moises")
                .isbn("123")
                .title("Api Restfull")
                .build();
    }
}
