package com.example.gamelib;

public class Calculos {

    public static int atualizarPosicaoX(double speed, float angle){
        return (int)(speed * Math.cos(Math.toRadians(angle)));
    }

    public static int atualizarPosicaoY(double speed, float angle){
        return (int) (speed * Math.sin(Math.toRadians(angle)));
    }

    public static int novaCoordenadaXSensor(int sensor, float centroCarroX, int distanciaX, int distanciaY, float angle){
        int x = 0;

        if (sensor ==1){
            x = (int) (centroCarroX + (float) (distanciaX * Math.cos(Math.toRadians(angle)) - distanciaY * Math.sin(Math.toRadians(angle))));
        }
        else if (sensor == 2) {
            x = (int) (centroCarroX + (float) (distanciaX * Math.cos(Math.toRadians(angle)) + distanciaY * Math.sin(Math.toRadians(angle))));
        }

        return x;
    }

    public static int novaCoordenadaYSensor(int sensor, float centroCarroY, int distanciaX, int distanciaY, float angle){
        int y = 0;

        if (sensor == 1) {
            y = (int) (centroCarroY + (float) (distanciaX * Math.sin(Math.toRadians(angle)) + distanciaY * Math.cos(Math.toRadians(angle))));
        }
        else if (sensor == 2) {
            y = (int) (centroCarroY + (float) (distanciaX * Math.sin(Math.toRadians(angle)) - distanciaY * Math.cos(Math.toRadians(angle))));
        }

        return y;
    }
}
