package br.com.mmt.libraryapi.service;

import br.com.mmt.libraryapi.api.dto.LoanFilterDTO;
import br.com.mmt.libraryapi.exceptions.BusinessException;
import br.com.mmt.libraryapi.model.entity.Book;
import br.com.mmt.libraryapi.model.entity.Loan;
import br.com.mmt.libraryapi.model.repository.LoanRepository;
import br.com.mmt.libraryapi.service.impl.LoanServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class LoanServiceTest {

    @MockBean
    LoanRepository repository;

    LoanService service;

    @BeforeEach
    public void setUp(){
        service = new LoanServiceImpl(repository);
    }

    @Test
    @DisplayName("Deve salvar um emprestimo")
    public void saveLoanTest(){

        //Cenário
        Book book = Book.builder().id(1L).build();
        String customer = "Cicrano";
        Loan savingLoan = Loan.builder()
                .book(book)
                .customer(customer)
                .loanDate(LocalDate.now())
                .build();

        Loan savedLoan = Loan.builder()
                .id(1L)
                .book(book)
                .customer(customer)
                .loanDate(LocalDate.now())
                .build();

        when(repository.existsByBookAndNotReturned(book)).thenReturn(false);
        when(repository.save(savingLoan)).thenReturn(savedLoan);
        //Execução
        Loan loan = service.save(savingLoan);
        //Validações
        assertThat(loan.getId()).isEqualTo(savedLoan.getId());
        assertThat(loan.getBook()).isEqualTo(savedLoan.getBook());
        assertThat(loan.getCustomer()).isEqualTo(savedLoan.getCustomer());
        assertThat(loan.getLoanDate()).isEqualTo(savedLoan.getLoanDate());
    }

    @Test
    @DisplayName("Deve lançar erro de negocio ao salvar um emprestimo com livro já emprestado")
    public void loanedBookSaveLoanTest(){

        //Cenário
        Book book = Book.builder().id(1L).build();
        String customer = "Cicrano";
        Loan savingLoan = Loan.builder()
                .book(book)
                .customer(customer)
                .loanDate(LocalDate.now())
                .build();
        when(repository.existsByBookAndNotReturned(book)).thenReturn(true);
        //Execução
        Throwable throwable = catchThrowable(() -> service.save(savingLoan));
        //Validações
        assertThat(throwable)
                .isInstanceOf(BusinessException.class)
                .hasMessage("Book already loaned.");
        verify(repository, never()).save(savingLoan);

    }

    @Test
    @DisplayName("Deve obter as informações de um emprestimo pelo ID")
    public void getLoanDetailsTest(){

        //Cenário
        Long id = 1l;

        Loan loan = createLoan();
        loan.setId(id);
        Mockito.when(repository.findById(id)).thenReturn(Optional.of(loan));

        //Execução
        Optional<Loan> result = service.getById(id);
        //Validações

        assertThat(result.isPresent()).isTrue();
        assertThat(result.get().getId()).isEqualTo(id);
        assertThat(result.get().getCustomer()).isEqualTo(loan.getCustomer());
        assertThat(result.get().getBook()).isEqualTo(loan.getBook());
        assertThat(result.get().getLoanDate()).isEqualTo(loan.getLoanDate());

        verify(repository).findById(id);
    }

    @Test
    @DisplayName("Deve atualizar um emprestimo")
    public void updateLoanTest(){
        //cenário
        Loan loan = createLoan();
        loan.setId(1l);
        loan.setReturned(true);
        //Execução
        when(repository.save(loan)).thenReturn(loan);
        Loan updateLoan = service.update(loan);
        //Validação
        assertThat(updateLoan.getReturned()).isTrue();
        verify(repository).save(loan);

    }

    @Test
    @DisplayName("Deve filtrar emprestimos pelas propriedades.")
    public void findLoanTest(){
        //cenário
        LoanFilterDTO filterDTO = LoanFilterDTO.builder().customer("Cicrano").isbn("321").build();

        Loan loan = createLoan();
        loan.setId(1l);

        PageRequest pageRequest = PageRequest.of(0, 10);
        List<Loan> list = Arrays.asList(loan);
        PageImpl<Loan> page = new PageImpl<Loan>(list, pageRequest, list.size());
        when(repository.findhByBookIsbnOrCustomer(
                Mockito.anyString(),
                Mockito.anyString(),
                Mockito.any(PageRequest.class))).thenReturn(page);

        //Execução
        Page<Loan> result = service.find(filterDTO, pageRequest);

        //Validação
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).isEqualTo(list);
        assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
        assertThat(result.getPageable().getPageSize()).isEqualTo(10);

    }

    public static Loan createLoan(){
        Book book = Book.builder().id(1L).build();
        String customer = "Cicrano";
        Loan savingLoan = Loan.builder()
                .book(book)
                .customer(customer)
                .loanDate(LocalDate.now())
                .build();
        return savingLoan;
    }
}
