package com.postech.oficinamecanica.domain.shared.exceptions;

public class ResourceNotFoundException extends DomainException{
    public ResourceNotFoundException(String resourceName, Object identifier) {
        super(String.format("%s com identificador '%s' não foi encontrado.", resourceName, identifier));
    }
}
