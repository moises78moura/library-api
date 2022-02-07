package br.com.mmt.libraryapi.exceptions;

public class BusinessException extends RuntimeException {
    public BusinessException(String mensagemErro) {
        super(mensagemErro);
    }
}
