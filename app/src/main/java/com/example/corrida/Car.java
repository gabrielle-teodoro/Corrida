package com.example.corrida;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import android.graphics.Paint;
import android.graphics.Color;  // Para definir cores
import android.util.Log;

import com.example.gamelib.Calculos;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;


public class Car implements Runnable {

    private Bitmap pista;
    private Bitmap imagemCarro;
    private int id;
    private int x;
    private int y;
    private ArrayList<Car> carros;
    //private int fuelTank;
    private double speed;
    private float angle;
    private int laps;
    private int distance;
    //private int penalty;
    private Sensor sensor1; //sensor direito
    private Sensor sensor2; //sensor esquerdo
    private boolean rodando;
    private boolean pausado;
    private Semaphore semaforo;
    private boolean flagSemaforo;
    private boolean flagColisao;
    private boolean flagLinhaChegada;

    long tempoInicioVolta;
    long tempoFimVolta;
    long tempoVolta;
    long tempoInicioT1;
    long tempoFimT1;
    long tempoT1;
    long tempoInicioT2;
    long tempoFimT2;
    long tempoT2;
    long tempoInicioT3;
    long tempoFimT3;
    long tempoT3;
    long tempoInicioT4;
    long tempoFimT4;
    long tempoT4;
    long tempoInicioT5;
    long tempoFimT5;
    long tempoT5;

    public Car(Bitmap pista, Bitmap imagemCarro, int id, int x, int y, ArrayList<Car> carros, Semaphore semaforo, double speed) {

        this.pista = pista;
        this.imagemCarro = imagemCarro;
        this.id = id;
        this.x = x;
        this.y = y;
        this.carros = carros;
        this.semaforo = semaforo;
        this.speed = speed; // Velocidade inicial
        this.angle = 0; // Angulo inicial (graus)
        this.laps = 0;

        this.distance = 0;

        this.rodando = true;
        this.pausado = false;

        this.flagSemaforo = false;
        this.flagColisao = false;
        this.flagLinhaChegada = false;

        this.tempoInicioVolta = 0;
        this.tempoFimVolta = 0;
        this.tempoVolta = 0;


    }

    public void criarSensores() {
        int x1 = getX() + getImage().getWidth() + 15; // x1 = x2

        int y1 = getY() + getImage().getHeight() + 10;
        int y2 = getY() - 10;

        sensor1 = new Sensor(x1, y1);
        sensor2 = new Sensor(x1, y2);
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
        } catch (Exception e) {
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

                tempoInicioT1 = System.nanoTime();
                // Verificar se o carro está fora dos limites da pista
                if (!sensor1.pixelBranco(pista)) {
                    virarEsquerda();
                }

                if (!sensor2.pixelBranco(pista)) {
                    virarDireita();
                }
                tempoFimT1 = System.nanoTime();
                tempoT1 = (tempoFimT1 - tempoInicioT1) / 1_000;
                Log.d("Car", "Tempo da tarefa T1: " + getTempoT1());

                tempoInicioT3 = System.nanoTime();
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

                tempoFimT3 = System.nanoTime();
                tempoT3 = (tempoFimT3 - tempoInicioT3) / 1_000;
                Log.d("Car", "Tempo da tarefa T3: " + getTempoT3());

                flagColisao = false;

                tempoInicioT2 = System.nanoTime();
                // Atualizar a posição
                double dx = Calculos.atualizarPosicaoX(getSpeed(), getAngle());
                double dy = Calculos.atualizarPosicaoY(getSpeed(), getAngle());

                x = (int) (x + dx);
                y = (int) (y + dy);

                tempoFimT2 = System.nanoTime();
                tempoT2 = (tempoFimT2 - tempoFimT2) / 1_000;
                Log.d("Car", "Tempo da tarefa T2: " + getTempoT2());

                setDistance();

                atualizarPosicaoSensores();
            }
        } catch (Exception e) {
            Log.e("Car", "Erro ao mover o carro", e);
        }
    }

    private float getAngle() {
        return angle;
    }

    private double getSpeed() {
        return speed;
    }

    private void atualizarPosicaoSensores() {

        int distanciaX = 50;
        int distanciaY = 40;

        float centroCarroX = x + imagemCarro.getWidth() / 2;
        float centroCarroY = y + imagemCarro.getHeight() / 2;

        int x1 = Calculos.novaCoordenadaXSensor(1, centroCarroX, distanciaX, distanciaY, getAngle());
        int y1 = Calculos.novaCoordenadaYSensor(1, centroCarroY, distanciaX, distanciaY, getAngle());

        int x2 = Calculos.novaCoordenadaXSensor(2, centroCarroX, distanciaX, distanciaY, getAngle());
        int y2 = Calculos.novaCoordenadaYSensor(2, centroCarroY, distanciaX, distanciaY, getAngle());

        sensor1.setXY(x1, y1);
        sensor2.setXY(x2, y2);
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

    public int getId() {
        return id;
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

    public int getDistance() {
        return distance;
    }

    public void setDistance() {
        this.distance++;
    }

    public int getLaps() {
        return laps;
    }

    public void setLaps() {
        this.laps++;
    }

    public long getTempoVolta(){
        return tempoVolta;
    }

    public long getTempoT1(){
        return tempoT1;
    }

    public long getTempoT2(){
        return tempoT2;
    }

    public long getTempoT3(){
        return tempoT3;
    }

    public long getTempoT4(){
        return tempoT4;
    }

    public long getTempoT5(){
        return tempoT5;
    }

    @Override
    public void run() {
        while (rodando && !Thread.currentThread().isInterrupted()) {
            if (!pausado) {
                try {
                    // Verifica se o carro cruzou a linha de chegada e conta as voltas
                    if (estaNaLinhaChegada(sensor1.getX(), sensor1.getY()) && !flagLinhaChegada) {

                        tempoFimVolta = System.currentTimeMillis();
                        tempoVolta = tempoFimVolta - tempoInicioVolta;
                        //registrarVolta();
                        tempoInicioVolta = tempoFimVolta; // Atualiza o tempo para a próxima volta

                        setLaps();
                        flagLinhaChegada = true;
                        Log.d("Car", "Carro " + getId() + " iniciou a volta " + getLaps());

                        if (getLaps() > 1) {
                            Log.d("Car", "Tempo da volta " + (getLaps() - 1) + " do carro " + getId() + ": " + getTempoVolta());
                        }
                    }

                    if(!estaNaLinhaChegada(sensor1.getX(), sensor1.getY()) && flagLinhaChegada){
                        flagLinhaChegada = false;
                    }

                    // Verifica se o carro está entrando na curva
                    if (estaNaLinha1(sensor1.getX(), sensor1.getY()) && !flagSemaforo) {
                        tempoInicioT4 = System.nanoTime();
                        semaforo.acquire();
                        flagSemaforo = true;
                        Log.d("Car", "Carro " + getId() + " entrou na curva");
                    }

                    // Verifica se o carro está saindo da curva
                    if (estaNaLinha2(sensor1.getX(), sensor1.getY()) && flagSemaforo) {
                        tempoFimT4 = System.nanoTime();
                        semaforo.release();
                        flagSemaforo = false;
                        Log.d("Car", "Carro " + getId() + "  saiu da curva");
                    }

                    tempoT4 = (tempoFimT4 - tempoInicioT4) / 1_000;
                    Log.d("Car", "Tempo da tarefa T4: " + getTempoT4());

                    mover();  // Movimentação do carro
                    //Log.d("Car","Carro " + getId() + " se movendo. Distância percorrida: " + getDistance());

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

    public boolean estaNaLinhaChegada(int sensorX, int sensorY) {
        int x1 = sensorX;
        int y1 = sensorY;

        return (x1 >= 540 && x1 <= 550 && y1 >= 1020 && y1 <= 1360);
    }

    public boolean estaNaLinha1(int sensorX, int sensorY) {
        int x1 = sensorX;
        int y1 = sensorY;

        return (x1 >= 490 && x1 <= 500 && y1 >= 330 && y1 <= 630);
    }

    public boolean estaNaLinha2(int sensorX, int sensorY) {
        int x1 = sensorX;
        int y1 = sensorY;

        return (x1 >= 590 && x1 <= 600 && y1 >= 700 && y1 <= 1000);
    }
}
