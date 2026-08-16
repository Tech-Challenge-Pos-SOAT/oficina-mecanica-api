package com.postech.oficinamecanica.domain.vehicle;

public class InvalidPlateException extends RuntimeException {
    public InvalidPlateException(String plate) {
        super("Invalid plate: " + plate);
    }
}
