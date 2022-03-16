package br.com.mmt.libraryapi.api;

import br.com.mmt.libraryapi.api.exceptions.ApiErros;
import br.com.mmt.libraryapi.exceptions.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

//Indica que a classe tem configurações globais para todas as APIs
@RestControllerAdvice
public class ApplicationControllerAdvice {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErros handlerValidationExceptions(MethodArgumentNotValidException ex){
        BindingResult bindingResult = ex.getBindingResult();
        return new ApiErros(bindingResult);
    }

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErros handlerBusinessExceptions(BusinessException exception){
        return new ApiErros(exception);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity handlerResponseStatusExceptions(ResponseStatusException exception){
        return new ResponseEntity(new ApiErros(exception), exception.getStatus());
    }


}
