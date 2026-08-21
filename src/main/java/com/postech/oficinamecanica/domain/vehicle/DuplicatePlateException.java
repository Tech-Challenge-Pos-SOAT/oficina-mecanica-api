package com.postech.oficinamecanica.domain.vehicle;

public class DuplicatePlateException extends RuntimeException {
    public DuplicatePlateException(Plate plate) {
        super("Plate already exists: " + plate.value());
    }
}
