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
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

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
        when(repository.existsByIsbn(Mockito.anyString())).thenReturn(false);

        when(repository.save(book)).thenReturn(Book.builder()
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
        when(repository.existsByIsbn(Mockito.anyString())).thenReturn(true);
        //Execução
        Throwable exception = Assertions.catchThrowable(() -> service.save(book));

        //Validações
        assertThat(exception)
                .isInstanceOf(BusinessException.class)
                .hasMessage("Isbn já cadastrado.");
        //Verica que a repository nunca chama o metodo salvar
        Mockito.verify(repository, Mockito.never()).save(book);

    }

    @Test
    @DisplayName("Deve obter um livro de acordo com o Id passado.")
    public void getBookByIdTest(){

        //cenário
        Book book = createMockBook();
        long id = 1L;
        book.setId(id);

        when(repository.findById(id)).thenReturn(Optional.of(book));

        //Execução
        Optional<Book> foundBook = service.getById(id);

        //Validações
        assertThat(foundBook.isPresent()).isTrue();
        assertThat(foundBook.get().getId()).isEqualTo(book.getId());
        assertThat(foundBook.get().getTitle()).isEqualTo(book.getTitle());
        assertThat(foundBook.get().getIsbn()).isEqualTo(book.getIsbn());
        assertThat(foundBook.get().getAuthor()).isEqualTo(book.getAuthor());

    }

    @Test
    @DisplayName("Deve retornar vazio quando não encontrar o book na base.")
    public void getBookNotFoundByIdTest(){

        //cenário
        long id = 1L;
        when(repository.findById(id)).thenReturn(Optional.empty());
        //Execução
        Optional<Book> foundBook = service.getById(id);
        //Validações
        assertThat(foundBook.isPresent()).isFalse();

    }

    @Test
    @DisplayName("Deve deletar um livro.")
    public void deleteBookTest(){
        //cenário
        Book book = Book.builder().id(1L).build();
        //Execução
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> service.delete(book));//valida se não foi lançado uma exceção
        //Validações
        Mockito.verify(repository, Mockito.times(1)).delete(book);
    }

    @Test
    @DisplayName("Deve lançar uma exception ao tentar deletar um livro.")
    public void deleteInvalidBookTest(){
        //cenário
        Book book = new Book();
        //Execução
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> service.delete(book));//foi lançado uma exceção
        //Validações
        Mockito.verify(repository, Mockito.never()).delete(book);
    }

    @Test
    @DisplayName("Deve lançar uma exception ao tentar atualizar um livro.")
    public void updateInvalidBookTest(){
        //cenário
        Book book = new Book();
        //Execução
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> service.update(book));//foi lançado uma exceção
        //Validações
        Mockito.verify(repository, Mockito.never()).save(book);
    }

    @Test
    @DisplayName("Deve atualizar um livro.")
    public void updatedBookTest(){
        //cenário
        long id = 1L;
        //livro a atualizar
        Book updating = Book.builder().id(id).build();

        //simulçao
        Book updatedBook = createMockBook();
        updatedBook.setId(id);
        //Execução
        when(repository.save(updating)).thenReturn(updatedBook);
        Book book = service.update(updating);

        //Validações
        assertThat(book.getId()).isEqualTo(updatedBook.getId());
        assertThat(book.getTitle()).isEqualTo(updatedBook.getTitle());
        assertThat(book.getIsbn()).isEqualTo(updatedBook.getIsbn());
        assertThat(book.getAuthor()).isEqualTo(updatedBook.getAuthor());

    }

    @Test
    @DisplayName("Deve filtrar livros pelas propriedades")
    public void findBookTest(){
        //Cenário
        Book book = createMockBook();

        PageRequest pageRequest = PageRequest.of(0, 10);
        List<Book> list = Arrays.asList(book);
        Page<Book> page = new PageImpl<Book>(list, pageRequest, 1);

        when(repository.findAll(Mockito.any(Example.class), Mockito.any(PageRequest.class)))
                .thenReturn(page);

        //execução
        Page<Book> result = service.find(book, pageRequest);

        //Validações
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).isEqualTo(list);
        assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
        assertThat(result.getPageable().getPageSize()).isEqualTo(10);

    }

    @Test
    @DisplayName("Deve obter um livro pelo isbn")
    public void getBookByIsbnTest(){
        //Cenário
        String isbn = "123";
        when(repository.findByIsbn(isbn)).thenReturn(Optional.of(Book.builder().id(1L).isbn(isbn).build()));

        //execução
        Optional<Book> bookByIsbn = service.getBookByIsbn(isbn);
        //Validações

        assertThat(bookByIsbn.isPresent()).isTrue();
        assertThat(bookByIsbn.get().getId()).isEqualTo(1L);
        assertThat(bookByIsbn.get().getIsbn()).isEqualTo(isbn);

        verify(repository, times(1)).findByIsbn(isbn);
    }

    private Book createMockBook() {
        return Book.builder()
                .author("Moises")
                .isbn("123")
                .title("Api Restfull")
                .build();
    }

}
