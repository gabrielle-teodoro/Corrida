package com.example.gamelib;

import static org.junit.Assert.*;

import org.junit.Test;

public class TesteAtualizarPosicaoX {

    Calculos x = new Calculos();

    @Test
    public void testeAtualizarPosicaoX() {
        int resultado = x.atualizarPosicaoX(5, 90);

        assertEquals(5, resultado);
    }
}