package br.com.mmt.libraryapi.api.resource;

import br.com.mmt.libraryapi.api.dto.BookDTO;
import br.com.mmt.libraryapi.api.dto.LoanDTO;
import br.com.mmt.libraryapi.api.dto.LoanFilterDTO;
import br.com.mmt.libraryapi.api.dto.ReturnedLoanDTO;
import br.com.mmt.libraryapi.model.entity.Book;
import br.com.mmt.libraryapi.model.entity.Loan;
import br.com.mmt.libraryapi.service.BookService;
import br.com.mmt.libraryapi.service.LoanService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;

    private final BookService bookService;

    private final ModelMapper modelMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Long create(@RequestBody LoanDTO dto){
        Book bookByIsbn = bookService.getBookByIsbn(dto.getIsbn()).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Book not found for passed isbn.")
        );
        Loan loan = Loan.builder()
                .book(bookByIsbn)
                .customer(dto.getCustomer())
                .loanDate(LocalDate.now())
                .build();
        Loan savedLoan = loanService.save(loan);
        return savedLoan.getId();
    }

    @PatchMapping("{id}")
    public void returnedBook(@PathVariable Long id, @RequestBody ReturnedLoanDTO dto){
        Loan loan = loanService.getById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        loan.setReturned(dto.getReturned());
        loanService.update(loan);
    }

    @GetMapping
    public Page<LoanDTO> find(LoanFilterDTO filter, Pageable page){

        Page<Loan> result = loanService.find(filter, page);

        List<LoanDTO> loanDTOS = result.getContent().stream().map(loan -> {
            Book book = loan.getBook();
            BookDTO bookDTO = modelMapper.map(book, BookDTO.class);
            LoanDTO loanDTO = modelMapper.map(loan, LoanDTO.class);
            loanDTO.setBook(bookDTO);
            return loanDTO;
        }).collect(Collectors.toList());

        return new PageImpl<LoanDTO>(loanDTOS, page, result.getTotalElements());
    }


}
