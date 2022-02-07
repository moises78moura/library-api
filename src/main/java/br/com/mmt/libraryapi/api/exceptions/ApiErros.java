package br.com.mmt.libraryapi.api.exceptions;

import br.com.mmt.libraryapi.exceptions.BusinessException;
import org.springframework.validation.BindingResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ApiErros {

    private List<String> errors;

    public ApiErros(BindingResult bindingResult) {
        errors = new ArrayList<>();
        bindingResult.getAllErrors().forEach(error -> errors.add(error.getDefaultMessage()));
    }

    public ApiErros(BusinessException exception) {
        errors = Arrays.asList(exception.getMessage());
    }

    public List<String> getErrors() {
        return errors;
    }
}
