package com.example.corrida;

import static org.junit.Assert.*;

import org.junit.Test;

public class TesteEstaNaLinha1 {

    Car carro = new Car(null, null, 0, 0, 0, null, null, 0);

    @Test
    public void testeEstaNaLinha1() {
        boolean resultado = carro.estaNaLinha1(500, 500);

        assertEquals(true, resultado);
    }
}