package com.postech.oficinamecanica.domain.customer;

public class InvalidDocumentException extends RuntimeException {
    public InvalidDocumentException(String document) {
        super("Invalid document: " + document);
    }
}
