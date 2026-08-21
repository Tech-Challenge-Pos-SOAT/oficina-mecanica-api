package com.postech.oficinamecanica.domain.customer;

public class DuplicateDocumentException extends RuntimeException {
    public DuplicateDocumentException(Document document) {
        super("Document already exists: " + document.value());
    }
}
