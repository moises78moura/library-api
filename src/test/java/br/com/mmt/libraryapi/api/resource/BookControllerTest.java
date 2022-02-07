package br.com.mmt.libraryapi.api.resource;

import br.com.mmt.libraryapi.api.dto.BookDTO;
import br.com.mmt.libraryapi.exceptions.BusinessException;
import br.com.mmt.libraryapi.model.entity.Book;
import br.com.mmt.libraryapi.service.BookService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@WebMvcTest
@AutoConfigureMockMvc
public class BookControllerTest {

    static String BOOK_API = "/api/books";

    @Autowired
    MockMvc mockMvc;

    @MockBean
    BookService service;

    @Test
    @DisplayName("Deve criar um livro com sucesso!")
    public void createBookTest() throws Exception {

        BookDTO bookDTO = createNewBookMock();
        Book savedBook = Book.builder().id(1L).author("Author").title("Meu Livro").isbn("123456").build();

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

    private BookDTO createNewBookMock() {
        return BookDTO.builder().author("Author").title("Meu Livro").isbn("123456").build();
    }

}
