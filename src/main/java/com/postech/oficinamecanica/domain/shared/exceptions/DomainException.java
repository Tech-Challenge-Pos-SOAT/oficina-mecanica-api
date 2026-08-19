package com.postech.oficinamecanica.domain.shared.exceptions;

public abstract class DomainException extends RuntimeException{
    protected DomainException(String message) {
        super(message);
    }
}
