package com.postech.oficinamecanica.domain.shared.exceptions;

public class InvalidParametersException extends DomainException{
    public InvalidParametersException(String parameterName, String errorDescription) {
        super(String.format("O parâmetro '%s' é inválido: %s", parameterName, errorDescription));
    }
}
