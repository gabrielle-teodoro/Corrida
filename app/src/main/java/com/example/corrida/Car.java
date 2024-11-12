package com.example.corrida;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import android.graphics.Paint;
import android.graphics.Color;  // Para definir cores
import android.util.Log;

import com.example.gamelib.Calculos;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;


public class Car implements Runnable{

    private Bitmap pista;
    //private String name;
    private Bitmap imagemCarro;
    private int x;
    private int y;
    private ArrayList<Car> carros;
    //private int fuelTank;
    private double speed;
    private float angle;
    //private int laps;
    private int distance;
    //private int penalty;
    private Sensor sensor1; //sensor direito
    private Sensor sensor2; //sensor esquerdo
    private boolean rodando;
    private boolean pausado;
    private Semaphore semaforo;
    private boolean flagSemaforo;
    private boolean flagColisao;

    public Car(Bitmap pista, Bitmap imagemCarro, int x, int y, ArrayList<Car> carros, Semaphore semaforo, double speed) {

        this.pista = pista;
        this.imagemCarro = imagemCarro;
        this.x = x;
        this.y = y;
        this.carros = carros;
        this.semaforo = semaforo;
        this.speed = speed; // Velocidade inicial
        this.angle = 0; // Angulo inicial (graus)

        this.distance = 0;

        this.rodando = true;
        this.pausado = false;

        this.flagSemaforo = false;
        this.flagColisao = false;

        //criarSensores();

    }

    public void criarSensores(){
        int x1 = getX() + getImage().getWidth() + 15; // x1 = x2

        int y1 = getY() + getImage().getHeight() + 10;
        int y2 = getY() - 10;

        sensor1 = new Sensor(x1,y1);
        sensor2 = new Sensor(x1,y2);
    }

    // Método para desenhar o carro
    public void draw(Canvas canvas) {
        try {
            synchronized (this) {
                if (imagemCarro != null) {
                    // Rotacionar o Canvas antes de desenhar o carro
                    canvas.save();
                    canvas.rotate(angle, x + imagemCarro.getWidth() / 2, y + imagemCarro.getHeight() / 2);
                    // Desenha o Bitmap do carro na posição (x, y)
                    canvas.drawBitmap(imagemCarro, x, y, null);
                    canvas.restore();
                }

                // Desenhar um ponto visual para os sensores
                if (sensor1 != null) {
                    // Desenha um círculo (pode ser qualquer forma) para o sensor1 (direito)
                    canvas.drawCircle(sensor1.getX(), sensor1.getY(), 10, getSensorPaint()); // 10 é o raio do círculo
                }

                if (sensor2 != null) {
                    // Desenha um círculo (pode ser qualquer forma) para o sensor2 (esquerdo)
                    canvas.drawCircle(sensor2.getX(), sensor2.getY(), 10, getSensorPaint()); // 10 é o raio do círculo
                }
            }
        } catch (Exception e){
            Log.e("Car", "Erro ao desenhar o carro", e);
        }
    }

    // Método para retornar a pintura (cor) do marcador do sensor
    private Paint getSensorPaint() {
        Paint paint = new Paint();
        paint.setColor(Color.RED);  // Defina a cor que deseja para o marcador
        paint.setStyle(Paint.Style.FILL);  // Pinta o círculo
        return paint;
    }

    // Método para mover o carro com base no centro de massa
    public void mover() {
        try {
            synchronized (this) {

                // Verificar se o carro está fora dos limites da pista
                if (!sensor1.pixelBranco(pista)) {
                    virarEsquerda();
                }

                if (!sensor2.pixelBranco(pista)) {
                    virarDireita();
                }

                // Verificar se há colisão com outros carros
                for (Car carro : carros) {
                    if (this != carro) {
                        if (sensor1.estaColidindo(carro) || sensor2.estaColidindo(carro) || flagColisao) {
                            flagColisao = true;
                            ajustarVelocidade(0);
                        } else {
                            ajustarVelocidade(5);
                        }
                    }
                }

                flagColisao = false;

                // Atualizar a posição
                double dx = Calculos.atualizarPosicaoX(getSpeed(), getAngle());
                double dy = Calculos.atualizarPosicaoY(getSpeed(), getAngle());

                x = (int) (x + dx);
                y = (int) (y + dy);

                setDistance();

                atualizarPosicaoSensores();
            }
        } catch (Exception e){
            Log.e("Car", "Erro ao mover o carro", e);
        }
    }

    private float getAngle() {
        return angle;
    }

    private double getSpeed() {
        return speed;
    }

    private void atualizarPosicaoSensores(){

        int distanciaX = 50;
        int distanciaY = 40;

        float centroCarroX = x + imagemCarro.getWidth() / 2;
        float centroCarroY = y + imagemCarro.getHeight() / 2;

        int x1 = Calculos.novaCoordenadaXSensor(1,centroCarroX,distanciaX,distanciaY,getAngle());
        int y1 = Calculos.novaCoordenadaYSensor(1,centroCarroY,distanciaX,distanciaY,getAngle());

        int x2 = Calculos.novaCoordenadaXSensor(2,centroCarroX,distanciaX,distanciaY,getAngle());
        int y2 = Calculos.novaCoordenadaYSensor(2,centroCarroY,distanciaX,distanciaY,getAngle());

        sensor1.setXY(x1, y1);
        sensor2.setXY(x2,y2);
    }

    // Método para ajustar a velocidade
    public void ajustarVelocidade(float novaVelocidade) {
        this.speed = novaVelocidade;
    }

    // Método para virar o carro à esquerda
    public void virarEsquerda() {
        angle -= 15;  // Ajuste o valor conforme a suavidade da curva
    }

    // Método para virar o carro à direita
    public void virarDireita() {
        angle += 15;  // Ajuste o valor conforme a suavidade da curva
    }

    // Método para pausar o movimento
    public void pausar() {
        pausado = true;
    }

    // Método para retomar o movimento
    public void retomar() {
        pausado = false;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Bitmap getImage() {
        return imagemCarro;
    }

    public void setDistance(){
        this.distance++;
    }

    @Override
    public void run() {
        while (rodando && !Thread.currentThread().isInterrupted()) {
            if (!pausado) {
                try {
                    // Verifica se o carro está entrando na curva
                    if (estaNaLinha1(sensor1.getX(), sensor1.getY()) && !flagSemaforo) {
                        semaforo.acquire();
                        flagSemaforo = true;
                        Log.d("Car","Carro entrou na curva");
                    }

                    // Verifica se o carro está saindo da curva
                    if (estaNaLinha2(sensor1.getX(), sensor1.getY()) && flagSemaforo){
                        semaforo.release();
                        flagSemaforo = false;
                        Log.d("Car","Carro saiu da curva");
                    }

                    mover();  // Movimentação do carro

                } catch (InterruptedException e) {
                    // Thread foi interrompida
                    Thread.currentThread().interrupt();
                }
            }

            // Delay ou controle de velocidade do carro
            try {
                Thread.sleep(20);  // Controle de tempo entre movimentos
            } catch (InterruptedException e) {
                // Thread foi interrompida
                Thread.currentThread().interrupt();
            }
        }
    }

    public boolean estaNaLinha1(int sensorX, int sensorY){
        int x1 = sensorX;
        int y1 = sensorY;

        return (x1 >= 490 && x1 <= 500 && y1 >= 330 && y1 <= 630);
    }

    public boolean estaNaLinha2(int sensorX, int sensorY){
        int x1 = sensorX;
        int y1 = sensorY;

        return (x1 >= 590 && x1 <= 600 && y1 >= 700 && y1 <= 1000);
    }
}
