package com.postech.oficinamecanica.domain.customer;

public record Document(String value) {
    public Document {
        if (value == null || value.isBlank()) {
            throw new InvalidDocumentException("Document cannot be blank");
        }
    }
}
