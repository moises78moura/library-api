package br.com.mmt.libraryapi.api.resource;

import br.com.mmt.libraryapi.api.dto.BookDTO;
import br.com.mmt.libraryapi.api.exceptions.ApiErros;
import br.com.mmt.libraryapi.exceptions.BusinessException;
import br.com.mmt.libraryapi.model.entity.Book;
import br.com.mmt.libraryapi.service.BookService;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private BookService service;
    private ModelMapper modelMapper;

    public BookController(BookService service, ModelMapper modelMapper) {
        this.service = service;
        this.modelMapper = modelMapper;
    }

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

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErros handlerValidationExceptions(MethodArgumentNotValidException ex){
        BindingResult bindingResult = ex.getBindingResult();
        return new ApiErros(bindingResult);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErros handlerBusinessExceptions(BusinessException exception){
        return new ApiErros(exception);
    }
}
