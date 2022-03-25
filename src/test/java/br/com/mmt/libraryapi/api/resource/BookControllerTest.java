package br.com.mmt.libraryapi.api.resource;

import br.com.mmt.libraryapi.api.dto.BookDTO;
import br.com.mmt.libraryapi.exceptions.BusinessException;
import br.com.mmt.libraryapi.model.entity.Book;
import br.com.mmt.libraryapi.service.BookService;
import br.com.mmt.libraryapi.service.LoanService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@WebMvcTest(controllers = BookController.class)
@AutoConfigureMockMvc
public class BookControllerTest {

    static String BOOK_API = "/api/books";

    @Autowired
    MockMvc mockMvc;

    @MockBean
    BookService service;

    @MockBean
    LoanService loanService;

    @Test
    @DisplayName("Deve criar um livro com sucesso!")
    public void createBookTest() throws Exception {

        BookDTO bookDTO = createNewBookMock();
        Book savedBook = Book.builder().id(1L).author("Moises").title("Meu Livro").isbn("123456").build();

        BDDMockito.given(service.save(Mockito.any(Book.class))).willReturn(savedBook);

        String json = new ObjectMapper().writeValueAsString(bookDTO);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(BOOK_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").isNotEmpty())
                .andExpect(jsonPath("title").value(bookDTO.getTitle()))
                .andExpect(jsonPath("author").value(bookDTO.getAuthor()))
                .andExpect(jsonPath("isbn").value(bookDTO.getIsbn()));


    }


    @Test
    @DisplayName("Deve lançar erro de validação ao criar um livro!")
    public void createInvalidBookTest() throws Exception {

        String json = new ObjectMapper().writeValueAsString(new BookDTO());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(BOOK_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors", hasSize(3)));
    }

    @Test
    @DisplayName("Deve lançar erro ao tentar criar um livro com um isbn já utilizado por outro.")
    public void createBookWithDuplicatedIsbn() throws Exception {

        //Cria o mock do livro
        BookDTO bookDTO = createNewBookMock();
        //gera o json enviado na requisição
        String json = new ObjectMapper().writeValueAsString(bookDTO);

        String mensagemErro = "Isbn já cadastrado.";
        //Mock: Quando tentar salvar qualquer Livro com um isbn já existente
        BDDMockito.given(service.save(BDDMockito.any(Book.class)))
                .willThrow(new BusinessException(mensagemErro));//Lançará uma exception
        //Aqui chama o endpoint para salvar o livro
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(BOOK_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors", hasSize(1)))
                .andExpect(jsonPath("errors[0]").value(mensagemErro));

    }

    @Test
    @DisplayName("Deve obter informações de um livro")
    public void getBookDetailsTest() throws Exception {
        //Cenario (given)
        Long id = 1L;

        Book book = Book.builder()
                .id(id)
                .author(createNewBookMock().getAuthor())
                .isbn(createNewBookMock().getIsbn())
                .title(createNewBookMock().getTitle())
                .build();
        BDDMockito.given(service.getById(id)).willReturn(Optional.of(book));

        //Execução
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(BOOK_API.concat("/" + id))
                .accept(MediaType.APPLICATION_JSON);

        //Validações
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(id))
                .andExpect(jsonPath("title").value(book.getTitle()))
                .andExpect(jsonPath("author").value(book.getAuthor()))
                .andExpect(jsonPath("isbn").value(book.getIsbn()));

    }

    @Test
    @DisplayName("Deve retornar resource not found quando o livro procurado não existir")
    public void bookNotFoundTest() throws Exception {

        //Cenario (given)
        BDDMockito.given(service.getById(Mockito.anyLong())).willReturn(Optional.empty());

        //Execução
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(BOOK_API.concat("/" + 1))
                .accept(MediaType.APPLICATION_JSON);

        //Validações
        mockMvc.perform(request).andExpect(status().isNotFound());

    }

    @Test
    @DisplayName("Deve deletar um livro")
    public void deleteBookTest() throws Exception {

        //Cenario (given)
        BDDMockito.given(service.getById(Mockito.anyLong())).willReturn(Optional.of(Book.builder().id(1L).build()));

        //Execução
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .delete(BOOK_API.concat("/" + 1))
                .accept(MediaType.APPLICATION_JSON);
        //Validações
        mockMvc.perform(request).andExpect(status().isNoContent());

    }

    @Test
    @DisplayName("Deve retornar resource not found quando não encontrar  um livro para deletar")
    public void deleteInexistentsBookTest() throws Exception {

        //Cenario (given)
        BDDMockito.given(service.getById(Mockito.anyLong())).willReturn(Optional.empty());

        //Execução
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .delete(BOOK_API.concat("/" + 1))
                .accept(MediaType.APPLICATION_JSON);
        //Validações
        mockMvc.perform(request).andExpect(status().isNotFound());

    }

    @Test
    @DisplayName("Deve atualizar um livro")
    public void updateBookTest() throws Exception {

        //Cenario (given)
        Long id = 1L;
        String json = new ObjectMapper().writeValueAsString(createNewBookMock());

        Book updatingBook = Book.builder().id(id).author("some author").title("some title").build();
        BDDMockito.given(service.getById(id)).willReturn(Optional.of(updatingBook));

        Book updateBook = Book.builder().id(id).author("Moises").title("Meu Livro").isbn("123456").build();

        BDDMockito.given(service.update(updatingBook)).willReturn(updateBook);

        //Execução
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .put(BOOK_API.concat("/" + 1))
                .content(json)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON);
        //Validações
        mockMvc.perform(request).andExpect(status().isOk())
                .andExpect(jsonPath("id").value(id))
                .andExpect(jsonPath("title").value(createNewBookMock().getTitle()))
                .andExpect(jsonPath("author").value(createNewBookMock().getAuthor()))
                .andExpect(jsonPath("isbn").value(createNewBookMock().getIsbn()));

    }

    @Test
    @DisplayName("Deve retornar 404 ao atualizar um livro inexistente.")
    public void updateInexistentBookTest() throws Exception {

        //Cenario (given)
        String json = new ObjectMapper().writeValueAsString(createNewBookMock());
        BDDMockito.given(service.getById(Mockito.anyLong())).willReturn(Optional.empty());

        //Execução
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .put(BOOK_API.concat("/" + 1))
                .content(json)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON);
        //Validações
        mockMvc.perform(request).andExpect(status().isNotFound());

    }

    @Test
    @DisplayName("Deve filtrar livros")
    public void findBooksTest() throws Exception {

        //Cenario
        Long id = 1L;

        Book book = Book.builder()
                .id(id)
                .title(createNewBookMock().getTitle())
                .author(createNewBookMock().getAuthor())
                .isbn(createNewBookMock().getIsbn())
                .build();

        BDDMockito.given(service.find(Mockito.any(Book.class),
                Mockito.any(Pageable.class)))
                .willReturn(new PageImpl<Book>(Arrays.asList(book),
                        PageRequest.of(0, 100), 1) );

        String queryString = String.format("?title=%s&author=%s&page=0&size=100",
                book.getTitle(), book.getAuthor());
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(BOOK_API.concat(queryString))
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(request).andExpect(status().isOk())
                .andExpect(jsonPath("content", Matchers.hasSize(1)))
                .andExpect(jsonPath("totalElements").value(1))
                .andExpect(jsonPath("pageable.pageSize").value(100))
                .andExpect(jsonPath("pageable.pageNumber").value(0));


    }

    private BookDTO createNewBookMock() {
        return BookDTO.builder().author("Moises").title("Meu Livro").isbn("123456").build();
    }

}
