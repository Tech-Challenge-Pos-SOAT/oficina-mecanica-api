package com.postech.oficinamecanica;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Ponto de entrada do MVP de back-end da Oficina Mecanica.
 *
 * Tech Challenge Fase 1 - POSTECH/FIAP.
 * Monolito organizado em camadas (domain / application / infrastructure / interfaces),
 * seguindo os principios de Domain-Driven Design definidos na documentacao do Miro.
 */
@SpringBootApplication
public class OficinaMecanicaApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(OficinaMecanicaApiApplication.class, args);
    }
}
