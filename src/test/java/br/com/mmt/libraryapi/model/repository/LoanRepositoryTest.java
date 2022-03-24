package br.com.mmt.libraryapi.model.repository;

import br.com.mmt.libraryapi.model.entity.Book;
import br.com.mmt.libraryapi.model.entity.Loan;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.List;

import static br.com.mmt.libraryapi.model.repository.BookRepositoryTest.createMockBook;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest
public class LoanRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private LoanRepository repository;

    @Test
    @DisplayName("deve verificar se existe emprestimo não devolvido para o livro")
    public void existByBookAndNotReturnedTest(){
        //Cenário
        Loan loan = createAndPersistLoan(LocalDate.now());
        //Execução
        boolean exists = repository.existsByBookAndNotReturned(loan.getBook());
        //Validação
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("deve buscar emprestimo pelo isbn do livro ou customer")
    public void findhByBookIsbnOrCustomerTest(){
        //Cenário
        Loan loan = createAndPersistLoan(LocalDate.now());
        //Execução
        Page<Loan> loans = repository.findhByBookIsbnOrCustomer("123", "Cicrano", PageRequest.of(0,10));
        //Validação
        assertThat(loans.getContent()).hasSize(1);
        assertThat(loans.getContent()).contains(loan);
        assertThat(loans.getPageable().getPageSize()).isEqualTo(10);
        assertThat(loans.getPageable().getPageNumber()).isEqualTo(0);
        assertThat(loans.getTotalElements()).isEqualTo(1);

    }

    @Test
    @DisplayName("Deve obter empréstimos cuja data emprestimo fo menor ou igual a três dias atras e não retornados.")
    public void findByLoanDateLessThanAndNotReturnedTest(){
        Loan loan = createAndPersistLoan(LocalDate.now().minusDays(5));

        List<Loan> result = repository.findByLoanDateLessThanAndNotReturned(LocalDate.now().minusDays(4));
        assertThat(result).hasSize(1).contains(loan);
    }

    @Test
    @DisplayName("Deve retornar vazio quando tiver emprestimo atrasado")
    public void notFindByLoanDateLessThanAndNotReturnedTest(){
        Loan loan = createAndPersistLoan(LocalDate.now());

        List<Loan> result = repository.findByLoanDateLessThanAndNotReturned(LocalDate.now().minusDays(4));
        assertThat(result).isEmpty();
    }

    private Loan createAndPersistLoan(LocalDate loanDate) {
        Book book = createMockBook();
        entityManager.persist(book);
        Loan loan = Loan.builder()
                .book(book)
                .customer("Cicrano")
                .loanDate(loanDate)
                .build();
        entityManager.persist(loan);
        return loan;
    }

}
