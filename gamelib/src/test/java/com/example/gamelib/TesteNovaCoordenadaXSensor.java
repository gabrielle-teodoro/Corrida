package com.example.gamelib;

import static org.junit.Assert.*;

import org.junit.Test;

public class TesteNovaCoordenadaXSensor {

    Calculos x = new Calculos();

    @Test
    public void testeNovaCoordenadaXSensor() {
        int resultado = x.novaCoordenadaXSensor(2, 300, 10, 20, 90);

        assertEquals(5, resultado);
    }
}