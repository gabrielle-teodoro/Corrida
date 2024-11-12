package com.example.corrida;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;

public class SafetCar extends Car{
    public SafetCar(Bitmap pista, Bitmap imagemCarro, int x, int y, ArrayList<Car> carros, Semaphore semaforo, double speed) {
        super(pista, imagemCarro, x, y, carros, semaforo, speed);
    }
}
