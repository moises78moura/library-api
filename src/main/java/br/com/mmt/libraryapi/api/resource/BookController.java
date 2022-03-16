package br.com.mmt.libraryapi.api.resource;

import br.com.mmt.libraryapi.api.dto.BookDTO;
import br.com.mmt.libraryapi.api.dto.LoanDTO;
import br.com.mmt.libraryapi.api.exceptions.ApiErros;
import br.com.mmt.libraryapi.exceptions.BusinessException;
import br.com.mmt.libraryapi.model.entity.Book;
import br.com.mmt.libraryapi.model.entity.Loan;
import br.com.mmt.libraryapi.service.BookService;
import br.com.mmt.libraryapi.service.LoanService;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/books")
@AllArgsConstructor
public class BookController {

    private final BookService service;
    private final ModelMapper modelMapper;
    private final LoanService loanService;

//    public BookController(BookService service, ModelMapper modelMapper, LoanService loanService) {
//        this.service = service;
//        this.modelMapper = modelMapper;
//        this.loanService = loanService;
//    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookDTO create(@RequestBody @Valid BookDTO bookDTO){
        Book entity = modelMapper.map(bookDTO, Book.class);
//        Book entity = Book.builder()
//                .id(bookDTO.getId())
//                .author(bookDTO.getAuthor())
//                .title(bookDTO.getTitle())
//                .isbn(bookDTO.getIsbn())
//                .build();

        entity = service.save(entity);

        return modelMapper.map(entity, BookDTO.class);
//                BookDTO.builder()
//                .id(entity.getId())
//                .author(entity.getAuthor())
//                .title(entity.getTitle())
//                .isbn(entity.getIsbn())
//                .build();
    }

    @GetMapping("{id}")
    public BookDTO get(@PathVariable Long id){
        return service.getById(id).map(book -> modelMapper.map(book, BookDTO.class))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id){
        Book book = service.getById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));;
        service.delete(book);
    }

    @PutMapping("{id}")
    public BookDTO update(@PathVariable Long id, BookDTO dto){
        return service.getById(id).map(book -> {
            book.setAuthor(dto.getAuthor());
            book.setTitle(dto.getTitle());
            book = service.update(book);
            return modelMapper.map(book, BookDTO.class);
        }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @GetMapping
    public Page<BookDTO> find(BookDTO dto, Pageable pageRequest){
        Book filter = modelMapper.map(dto, Book.class);
        Page<Book> result = service.find(filter, pageRequest);
        List<BookDTO> list = result.getContent()
                .stream()
                .map(entity -> modelMapper.map(entity, BookDTO.class))//para cada item da lista(entity) o map transforma de Book(vindo da base) para BookDTO(objeto de retorno)
                .collect(Collectors.toList());//Converte para List(no caso o map retorna uma Stream)
        return new PageImpl<BookDTO>(list, pageRequest, result.getTotalElements());
    }

    @GetMapping("{id}/loans")
    public Page<LoanDTO> loansByBook(@PathVariable Long id, Pageable pageable){
        Book book = service.getById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Page<Loan> loansByBook = loanService.getLoansByBook(book, pageable);
        List<LoanDTO> result = loansByBook.getContent().stream().map(loan -> {
            LoanDTO loanDTO = getLoanDTO(loan);
            return loanDTO;
        }).collect(Collectors.toList());
        return new PageImpl<LoanDTO>(result, pageable, loansByBook.getTotalElements());
    }

    private LoanDTO getLoanDTO(Loan loan) {
        Book loanBook = loan.getBook();
        BookDTO bookDTO = modelMapper.map(loanBook, BookDTO.class);
        LoanDTO loanDTO = modelMapper.map(loan, LoanDTO.class);
        loanDTO.setBook(bookDTO);
        return loanDTO;
    }

}
