package com.example.corrida;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

import java.util.ArrayList;

public class Sensor {

    private int x;
    private int y;  // Coordenadas do centro do sensor

    public Sensor(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX(){
        return x;
    }

    public int getY() {
        return y;
    }

    public void setXY(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public boolean pixelBranco(Bitmap pista){
        try {
            int px = getX();
            int py = getY();

            int corPixel = pista.getPixel(px, py);

            int red = Color.red(corPixel);
            int green = Color.green(corPixel);
            int blue = Color.blue(corPixel);

            return (red == 255 && green == 255 && blue == 255);
        } catch (Exception e){
            Log.e("Sensor", "Erro ao detectar cor do pixel", e);
            return false;
        }
    }

    public boolean estaColidindo(Car outroCarro){
        try {
            float px = getX() - (outroCarro.getX() + (float) outroCarro.getImage().getWidth() / 2);
            float py = getY() - (outroCarro.getY() + (float) outroCarro.getImage().getHeight() / 2);

            float distancia = (float) Math.sqrt(Math.pow(px, 2) + Math.pow(py, 2));

            double raio = Math.sqrt(Math.pow((double) outroCarro.getImage().getWidth() / 2, 2) + Math.pow((double) outroCarro.getImage().getHeight() / 2, 2));

            return distancia <= raio + 20;
        } catch (Exception e){
            Log.e("Sensor", "Erro ao detectar colisão", e);
            return false;
        }
    }

}