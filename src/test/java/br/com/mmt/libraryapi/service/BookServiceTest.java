package br.com.mmt.libraryapi.service;

import br.com.mmt.libraryapi.exceptions.BusinessException;
import br.com.mmt.libraryapi.model.entity.Book;
import br.com.mmt.libraryapi.model.repository.BookRepository;
import br.com.mmt.libraryapi.service.impl.BookServiceImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class BookServiceTest {

    BookService service;

    @MockBean
    BookRepository repository;

    @BeforeEach
    public void setUp(){
        this.service = new BookServiceImpl(repository);
    }

    @Test
    @DisplayName("Deve salvar um livro")
    public void saveBookTest(){
        //cenário
        Book book = createMockBook();
        Mockito.when(repository.existsByIsbn(Mockito.anyString())).thenReturn(false);

        Mockito.when(repository.save(book)).thenReturn(Book.builder()
                        .id(1L)
                .author("Moises")
                .isbn("123")
                .title("Api Restfull")
                .build());
        // execução
        Book savedBook = service.save(book);

        //verificação
        assertThat(savedBook.getId()).isNotNull();
        assertThat(savedBook.getAuthor()).isEqualTo("Moises");
        assertThat(savedBook.getTitle()).isEqualTo("Api Restfull");
        assertThat(savedBook.getIsbn()).isEqualTo("123");

    }

    @Test
    @DisplayName("Deve lançar erro de negócio ao tentar salvar um livro com isbn duplicado.")
    public void shouldNotSaveBookWithDuplicatedISBN(){

        //cenário
        Book book = createMockBook();
        Mockito.when(repository.existsByIsbn(Mockito.anyString())).thenReturn(true);
        //Execução
        Throwable exception = Assertions.catchThrowable(() -> service.save(book));

        //Validações
        assertThat(exception)
                .isInstanceOf(BusinessException.class)
                .hasMessage("Isbn já cadastrado.");
        //Verica que a repository nunca chama o metodo salvar
        Mockito.verify(repository, Mockito.never()).save(book);

    }

    private Book createMockBook() {
        return Book.builder()
                .author("Moises")
                .isbn("123")
                .title("Api Restfull")
                .build();
    }

}
