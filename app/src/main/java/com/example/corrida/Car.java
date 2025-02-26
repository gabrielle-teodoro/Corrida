package com.example.corrida;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import com.example.gamelib.Calculos;
import com.example.gamelib.Reconciliacao;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;


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
    private boolean flagSemaforo1;
    private boolean flagSemaforo2;
    private boolean flagSemaforo3;
    private boolean flagColisao;
    private boolean flagLinhaLargada;
    private boolean flagLinhaChegada;

    private boolean flagF1;
    private boolean flagF2;
    private boolean flagF3;
    private boolean flagF4;
    private boolean flagF5;
    private boolean flagF6;
    private boolean flagF7;
    private boolean flagF8;
    private boolean flagF9;
    private boolean flagF10;

    private long tempoInicio;
    private long tempoFim;
    //private long tempoTotal;
    private long tempoF1;
    private long tempoF2;
    private long tempoF3;
    private long tempoF4;
    private long tempoF5;
    private long tempoF6;
    private long tempoF7;
    private long tempoF8;
    private long tempoF9;
    private long tempoF10;

    private long[] tempos;

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

        this.flagSemaforo1 = false;
        this.flagSemaforo2 = false;
        this.flagSemaforo3 = false;
        this.flagColisao = false;
        this.flagLinhaLargada = false;
        this.flagLinhaChegada = false;

        this.tempos = new long[12];

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

    public long getTempoInicio(){
        return tempoInicio;
    }

    public long getTempoFim(){
        return tempoFim;
    }

    public long getTempoF1(){
        return tempoF1;
    }

    public long getTempoF2(){
        return tempoF2;
    }

    public long getTempoF3(){
        return tempoF3;
    }

    public long getTempoF4(){
        return tempoF4;
    }

    public long getTempoF5(){
        return tempoF5;
    }

    public long getTempoF6(){
        return tempoF6;
    }

    public long getTempoF7(){
        return tempoF7;
    }

    public long getTempoF8(){
        return tempoF8;
    }

    public long getTempoF9(){
        return tempoF9;
    }

    public long getTempoF10(){
        return tempoF10;
    }

    public long[] getTempos() {
        return tempos;
    }

    private int tempoDeEspera(){
        return ThreadLocalRandom.current().nextInt(100, 2001); // alterar para gerar atrasos mais significativos
    }

    @Override
    public void run() {
        while (rodando && !Thread.currentThread().isInterrupted()) {
            if (!pausado) {
                try {
                    // Verifica se o carro cruzou a linha de chegada e conta as voltas
                    if (estaNaLinhaLargada(sensor1.getX(), sensor1.getY()) && !flagLinhaLargada) {

                        // Trecho usado para a primeira rota (largada = chegada)
                        if(getLaps() == 0){
                            tempoInicio = System.nanoTime();

                            Log.d("Car", "Tempo inicial: " + getTempoInicio());

                            /*// Teste da reconciliacao
                            // Conjunto de dados para serem reconciliados - Rota 1
                            double[] y1 = new double[] {0.8790, 2.3677, 1.6830, 5.0804, 3.4663, 2.2905, 4.8316, 2.0978, 1.8849, 2.5127, 0.5864, 30.0000};
                            double[] y2 = new double[] {0.8772, 2.3598, 1.6649, 5.0413, 3.4363, 2.2661, 4.8176, 2.0867, 1.8678, 2.4959, 0.5878, 30.0000};
                            double[] y3 = new double[] {0.8841,	2.3534,	1.6698,	5.0521,	3.4478,	2.2692,	4.8099,	2.0952,	1.8659,	2.5034,	0.5794,	30.0000};
                            double[] y4 = new double[] {0.8718,	2.3578,	1.6593,	3.8140,	3.3897,	2.2587,	4.2839,	2.0724,	1.8599,	2.8388,	0.5785,	30.0000};
                            double[] y5 = new double[] {0.8684,	2.3489,	1.6625,	5.2395,	3.4236,	2.2722,	4.7629,	2.0706,	1.8601,	3.9992,	0.5762,	30.0000};
                            double[] y6 = new double[] {0.8734,	2.3810,	1.6840,	5.0836,	3.4755,	2.2911,	4.8229,	2.0886,	1.8607,	2.4958,	0.5821,	30.0000};
                            double[] y7 = new double[] {0.8742,	2.3532,	1.6760,	3.8580,	3.4121,	2.2500,	4.3072,	2.0715,	1.8742,	2.8408,	0.5798,	30.0000};
                            double[] y8 = new double[] {0.8651,	2.3302,	1.6563,	5.2197,	3.4148,	2.2613,	4.7488,	2.0632,	1.8526,	3.9899,	0.5794,	30.0000};
                            double[] y9 = new double[] {0.8817,	2.3518,	1.6565,	5.2883,	3.4237,	2.2583,	3.1482,	2.0741,	1.8668,	3.9019,	0.5772,	30.0000};
                            double[] y10 = new double[] {0.8667, 2.3430, 1.6579, 4.3169, 3.4218, 2.2621, 4.7054, 2.0816, 1.8558, 4.0265, 0.5837, 30.0000};
                            double[] y11 = new double[] {0.8726, 2.3474, 1.6571, 3.8042, 3.4200, 2.2638, 3.3328, 2.0695, 1.8630, 3.2069, 0.5805, 30.0000};
                            double[] y12 = new double[] {0.8686, 2.3460, 1.6555, 3.6713, 3.4078, 2.2584, 3.2243, 2.0571, 1.8616, 2.7663, 0.5800, 30.0000};
                            double[] y13 = new double[] {0.8708, 2.3396, 1.6591, 4.7603, 3.4188, 2.2592, 4.4349, 2.0761, 1.8720, 3.8991, 0.5775, 30.0000};
                            double[] y14 = new double[] {0.8709, 2.3514, 1.6616, 5.2857, 3.4246, 2.2582, 4.8534, 2.0771, 1.8660, 3.7982, 0.5754, 30.0000};
                            double[] y15 = new double[] {0.8652, 2.3258, 1.6628, 4.2762, 3.4212, 2.2600, 3.1328, 2.0712, 1.8669, 4.1126, 0.5782, 30.0000};
                            double[] y16 = new double[] {0.8709, 2.3406, 1.6610, 4.5451, 3.4305, 2.2651, 3.2413, 2.0753, 1.8623, 2.9230, 0.5801, 30.0000};
                            double[] y17 = new double[] {0.8705, 2.3372, 1.6619, 5.0095, 3.4142, 2.2511, 4.1881, 2.0607, 1.8524, 3.2938, 0.5837, 30.0000};
                            double[] y18 = new double[] {0.8672, 2.3295, 1.6527, 4.6574, 3.4070, 2.2414, 4.3914, 2.0644, 1.8660, 4.1489, 0.5824, 30.0000};
                            double[] y19 = new double[] {0.8735, 2.3361, 1.6559, 4.9584, 3.4246, 2.2617, 4.1355, 2.0720, 1.8627, 3.5918, 0.5846, 30.0000};
                            double[] y20 = new double[] {0.8638, 2.3326, 1.6455, 4.4426, 3.4089, 2.2588, 3.8033, 2.0665, 1.8580, 4.0964, 0.5750, 30.0000};

                            //double[] V = new double[] {Math.pow(0.005,2), Math.pow(0.014,2), Math.pow(0.010,2), Math.pow(0.548,2), Math.pow(0.020,2), Math.pow(0.012,2), Math.pow(0.648,2), Math.pow(0.011,2), Math.pow(0.007,2), Math.pow(0.643,2), Math.pow(0.004,2), Math.pow(Math.pow(1,-20),2)};
                            double[] V = new double[] {0.005, 0.014, 0.010, 0.548, 0.020, 0.012, 0.648, 0.011, 0.007, 0.643, 0.004, Math.pow(1,-20)};

                            double[] A = new double[] {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, -1};

                            /*Reconciliacao rec = new Reconciliacao(y, V, A);
                            System.out.println("Y_hat:");
                            rec.printMatrix(rec.getReconciledFlow());

                            Reconciliacao.reconciliacao(y20,V,A);

                            Thread.currentThread().interrupt();*/

                            // Conjunto de dados para serem reconciliados - Rota 2
                            double[] y1 = new double[] {0.8128,	1.1930,	1.0523,	1.5582,	0.7831,	0.6526,	2.8699,	6.7703,	0.8192,	1.0243,	0.6273,	20.0000};
                            double[] y2 = new double[] {0.8161,	1.1889,	1.0464,	1.5345,	0.7633,	0.6429,	1.6598,	6.2270,	0.8014,	1.0161,	0.6187,	20.0000};
                            double[] y3 = new double[] {0.8092,	1.1735,	1.0385,	1.5294,	0.7695,	0.6460,	3.0618,	6.6734,	0.8118,	1.0175,	0.6213,	20.0000};
                            double[] y4 = new double[] {0.8141,	1.1751,	1.0432,	1.5453,	0.7683,	0.6480,	2.8626,	6.7485,	0.8129,	1.0224,	0.6334,	20.0000};
                            double[] y5 = new double[] {0.8151,	1.1850,	1.0471,	1.5394,	0.7698,	0.6429,	1.6671,	6.2183,	0.8060,	1.0074,	0.6205,	20.0000};
                            double[] y6 = new double[] {0.8175,	1.1966,	1.0441,	1.5550,	0.7865,	0.6535,	2.8731,	6.8015,	0.8173,	1.0285,	0.6284,	20.0000};
                            double[] y7 = new double[] {0.8141,	1.1969,	1.0432,	1.5376,	0.7682,	0.6437,	1.6668,	6.2419,	0.8103,	1.0143,	0.6244,	20.0000};
                            double[] y8 = new double[] {0.8115,	1.1846,	1.0349,	1.5318,	0.7679,	0.6403,	3.0610,	6.6922,	0.8069,	1.0144,	0.6252,	20.0000};
                            double[] y9 = new double[] {0.8110,	1.1878,	1.0405,	1.5393,	0.7726,	0.6428,	2.8670,	6.7372,	0.8174,	1.0184,	0.6247,	20.0000};
                            double[] y10 = new double[] {0.8120, 1.1874, 1.0417, 1.5385, 0.7687, 0.6427, 1.6612, 6.2361, 0.8102, 1.0158, 0.6227, 20.0000};
                            double[] y11 = new double[] {0.8101, 1.1896, 1.0373, 1.5342, 0.7680, 0.6420, 3.0645, 6.6964, 0.8096, 1.0172, 0.6211, 20.0000};
                            double[] y12 = new double[] {0.8140, 1.1926, 1.0372, 1.5356, 0.7712, 0.6484, 2.8657, 6.7514, 0.8147, 1.0171, 0.6263, 20.0000};
                            double[] y13 = new double[] {0.8122, 1.1894, 1.0411, 1.5422, 0.7689, 0.6487, 1.6652, 6.2472, 0.8088, 1.0160, 0.6223, 20.0000};
                            double[] y14 = new double[] {0.8080, 1.1853, 1.0285, 1.5280, 0.7676, 0.6404, 3.0547, 6.6725, 0.8093, 1.0207, 0.6182, 20.0000};
                            double[] y15 = new double[] {0.8134, 1.1882, 1.0400, 1.5348, 0.7677, 0.6457, 3.1088, 5.0778, 0.8086, 1.0138, 0.6228, 20.0000};
                            double[] y16 = new double[] {0.8161, 1.1821, 1.0404, 1.5365, 0.7711, 0.6459, 2.8666, 6.7924, 0.8173, 1.0228, 0.6258, 20.0000};
                            double[] y17 = new double[] {0.8132, 1.1873, 1.0400, 1.5384, 0.7742, 0.6463, 2.8638, 6.7617, 0.8110, 1.0167, 0.6270, 20.0000};
                            double[] y18 = new double[] {0.8154, 1.1941, 1.0453, 1.5435, 0.7695, 0.6443, 1.6644, 6.2445, 0.8093, 1.0186, 0.6218, 20.0000};
                            double[] y19 = new double[] {0.8027, 1.1813, 1.0334, 1.5265, 0.7645, 0.6386, 3.0585, 6.6783, 0.8058, 1.0131, 0.6174, 20.0000};
                            double[] y20 = new double[] {0.8129, 1.1943, 1.0471, 1.5538, 0.7820, 0.6538, 2.8664, 6.7861, 0.8187, 1.0272, 0.6281, 20.0000};

                            //double[] V = new double[] {Math.pow(0.005,2), Math.pow(0.014,2), Math.pow(0.010,2), Math.pow(0.548,2), Math.pow(0.020,2), Math.pow(0.012,2), Math.pow(0.648,2), Math.pow(0.011,2), Math.pow(0.007,2), Math.pow(0.643,2), Math.pow(0.004,2), Math.pow(Math.pow(1,-20),2)};
                            double[] V = new double[] {0.0033, 0.0063, 0.0054, 0.0086, 0.0060, 0.0043, 0.6122, 0.4098, 0.0048, 0.0050, 0.0040, Math.pow(1,-20)};

                            double[] A = new double[] {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, -1};

                            /*Reconciliacao rec = new Reconciliacao(y, V, A);
                            System.out.println("Y_hat:");
                            rec.printMatrix(rec.getReconciledFlow());*/

                            Reconciliacao.reconciliacao(y1,V,A);

                            Thread.currentThread().interrupt();

                        }else{
                            tempoFim = System.nanoTime();
                            Log.d("Car", "Tempo final: " + getTempoFim());

                            /*// Teste da reconciliacao
                            double[] y = new double[] {0.879, 2.368, 1.683, 5.080, 3.466, 2.291, 4.832, 2.098, 1.885, 2.513, 0.586, 25.000};

                            //double[] V = new double[] {Math.pow(0.005,2), Math.pow(0.014,2), Math.pow(0.010,2), Math.pow(0.548,2), Math.pow(0.020,2), Math.pow(0.012,2), Math.pow(0.648,2), Math.pow(0.011,2), Math.pow(0.007,2), Math.pow(0.643,2), Math.pow(0.004,2), Math.pow(Math.pow(1,-20),2)};
                            double[] V = new double[] {0.005, 0.014, 0.010, 0.548, 0.020, 0.012, 0.648, 0.011, 0.007, 0.643, 0.004, Math.pow(1,-30)};

                            double[] A = new double[] {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, -1};

                            /*Reconciliacao rec = new Reconciliacao(y, V, A);
                            System.out.println("Y_hat:");
                            rec.printMatrix(rec.getReconciledFlow());*/

                            //Reconciliacao.reconciliacao(y,V,A);

                            //Thread.currentThread().interrupt();*/
                        }

                        /*// Trecho usado para a segunda rota
                        tempoInicio = System.nanoTime()/1_000;*/

                        /*tempoFim = System.currentTimeMillis();
                        tempoTotal = tempoFim - tempoInicio;
                        //registrarVolta();
                        tempoInicio = tempoFim; // Atualiza o tempo para a próxima volta*/

                        setLaps();
                        flagLinhaLargada = true;
                        //Log.d("Car", "Carro " + getId() + " iniciou a volta " + getLaps());

                        /*if (getLaps() > 1) {
                            Log.d("Car", "Tempo da volta " + (getLaps() - 1) + " do carro " + getId() + ": " + getTempoTotal());
                        }*/
                    }

                    if(!estaNaLinhaLargada(sensor1.getX(), sensor1.getY()) && flagLinhaLargada){
                        flagLinhaLargada = false;
                    }

                    // Verifica se o carro está entrando no semáforo 1
                    if (estaNaLinha1(sensor1.getX(), sensor1.getY()) && !flagSemaforo1) {
                        semaforo.acquire();
                        flagSemaforo1 = true;
                        //Log.d("Car", "Carro " + getId() + " entrou no semaforo 1");
                        Thread.sleep(tempoDeEspera()); // faz o carro esperar um tempo aleatório para continuar
                    }

                    // Verifica se o carro está saindo do semáforo 1
                    if (estaNaLinha2(sensor1.getX(), sensor1.getY()) && flagSemaforo1) {
                        semaforo.release();
                        flagSemaforo1 = false;
                        //Log.d("Car", "Carro " + getId() + "  saiu do semaforo 1");
                    }

                    // Verifica se o carro está entrando no semáforo 2
                    if (estaNaLinha3(sensor1.getX(), sensor1.getY()) && !flagSemaforo2) {
                        semaforo.acquire();
                        flagSemaforo2 = true;
                        //Log.d("Car", "Carro " + getId() + " entrou no semaforo 2");
                        Thread.sleep(tempoDeEspera()); // faz o carro esperar um tempo aleatório para continuar
                    }

                    // Verifica se o carro está saindo do semáforo 2
                    if (estaNaLinha4(sensor1.getX(), sensor1.getY()) && flagSemaforo2) {
                        semaforo.release();
                        flagSemaforo2 = false;
                        //Log.d("Car", "Carro " + getId() + "  saiu do semaforo 2");
                    }

                    /*// Verifica se o carro está entrando no semáforo 3
                    if (estaNaLinha5(sensor1.getX(), sensor1.getY()) && !flagSemaforo3) {
                        semaforo.acquire();
                        flagSemaforo3 = true;
                        //Log.d("Car", "Carro " + getId() + " entrou no semaforo 3");
                        Thread.sleep(tempoDeEspera()); // faz o carro esperar um tempo aleatório para continuar
                    }

                    // Verifica se o carro está saindo do semáforo 3
                    if (estaNaLinha6(sensor1.getX(), sensor1.getY()) && flagSemaforo3) {
                        semaforo.release();
                        flagSemaforo3 = false;
                        //Log.d("Car", "Carro " + getId() + "  saiu do semaforo 3");
                    }*/

                    // Verifica se o carros está nos medidores de fluxo
                    if (estaNoFluxo1(sensor1.getX(), sensor1.getY()) && !flagF1){
                        tempoF1 = System.nanoTime();
                        Log.d("Car", "Tempo F1: " + getTempoF1());
                        flagF1 = true;
                    }

                    if(!estaNoFluxo1(sensor1.getX(), sensor1.getY()) && flagF1){
                        flagF1 = false;
                    }

                    if (estaNoFluxo2(sensor1.getX(), sensor1.getY()) && !flagF2){
                        tempoF2 = System.nanoTime();
                        Log.d("Car", "Tempo F2: " + getTempoF2());
                        flagF2 = true;
                    }

                    if(!estaNoFluxo2(sensor1.getX(), sensor1.getY()) && flagF2){
                        flagF2 = false;
                    }

                    if (estaNoFluxo3(sensor1.getX(), sensor1.getY()) && !flagF3){
                        tempoF3 = System.nanoTime();
                        Log.d("Car", "Tempo F3: " + getTempoF3());
                        flagF3 = true;
                    }

                    if(!estaNoFluxo3(sensor1.getX(), sensor1.getY()) && flagF3){
                        flagF3 = false;
                    }

                    if (estaNoFluxo4(sensor1.getX(), sensor1.getY()) && !flagF4){
                        tempoF4 = System.nanoTime();
                        Log.d("Car", "Tempo F4: " + getTempoF4());
                        flagF4 = true;
                    }

                    if(!estaNoFluxo4(sensor1.getX(), sensor1.getY()) && flagF4){
                        flagF4 = false;
                    }

                    if (estaNoFluxo5(sensor1.getX(), sensor1.getY()) && !flagF5){
                        tempoF5 = System.nanoTime();
                        Log.d("Car", "Tempo F5: " + getTempoF5());
                        flagF5 = true;
                    }

                    if(!estaNoFluxo5(sensor1.getX(), sensor1.getY()) && flagF5){
                        flagF5 = false;
                    }

                    if (estaNoFluxo6(sensor1.getX(), sensor1.getY()) && !flagF6){
                        tempoF6 = System.nanoTime();
                        Log.d("Car", "Tempo F6: " + getTempoF6());
                        flagF6 = true;
                    }

                    if(!estaNoFluxo6(sensor1.getX(), sensor1.getY()) && flagF6){
                        flagF6 = false;
                    }

                    if (estaNoFluxo7(sensor1.getX(), sensor1.getY()) && !flagF7){
                        tempoF7 = System.nanoTime();
                        Log.d("Car", "Tempo F7: " + getTempoF7());
                        flagF7 = true;
                    }

                    if(!estaNoFluxo7(sensor1.getX(), sensor1.getY()) && flagF7){
                        flagF7 = false;
                    }

                    if (estaNoFluxo8(sensor1.getX(), sensor1.getY()) && !flagF8){
                        tempoF8 = System.nanoTime();
                        Log.d("Car", "Tempo F8: " + getTempoF8());
                        flagF8 = true;
                    }

                    if(!estaNoFluxo8(sensor1.getX(), sensor1.getY()) && flagF8){
                        flagF8 = false;
                    }

                    if (estaNoFluxo9(sensor1.getX(), sensor1.getY()) && !flagF9){
                        tempoF9 = System.nanoTime();
                        Log.d("Car", "Tempo F9: " + getTempoF9());
                        flagF9 = true;
                    }

                    if(!estaNoFluxo9(sensor1.getX(), sensor1.getY()) && flagF9){
                        flagF9 = false;
                    }

                    if (estaNoFluxo10(sensor1.getX(), sensor1.getY()) && !flagF10){
                        tempoF10 = System.nanoTime();
                        Log.d("Car", "Tempo F10: " + getTempoF10());
                        flagF10 = true;
                    }

                    if(!estaNoFluxo10(sensor1.getX(), sensor1.getY()) && flagF10){
                        flagF10 = false;
                    }

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

    public boolean estaNaLinhaLargada(int sensorX, int sensorY) {
        int x1 = sensorX;
        int y1 = sensorY;

        // (540, 1019, 540, 1362)
        return (x1 >= 540 && x1 <= 550 && y1 >= 1020 && y1 <= 1360);
    }

    public boolean estaNaLinhaChegada2(int sensorX, int sensorY) {
        int x1 = sensorX;
        int y1 = sensorY;

        // alterar os valores para a segunda rota
        return (x1 >= 540 && x1 <= 550 && y1 >= 1020 && y1 <= 1360);
    }

    // entrada do semáforo 1
    public boolean estaNaLinha1(int sensorX, int sensorY) {
        int x1 = sensorX;
        int y1 = sensorY;

        return (x1 >= 0 && x1 <= 255 && y1 >= 410 && y1 <= 420);
    }

    // saída do semáforo 1
    public boolean estaNaLinha2(int sensorX, int sensorY) {
        int x1 = sensorX;
        int y1 = sensorY;

        return (x1 >= 280 && x1 <= 290 && y1 >= 450 && y1 <= 720);
    }

    // entrada do semáforo 2
    public boolean estaNaLinha3(int sensorX, int sensorY) {
        int x1 = sensorX;
        int y1 = sensorY;

        return (x1 >= 600 && x1 <= 840 && y1 >= 670 && y1 <= 680);
    }

    // saída do semáforo 2
    public boolean estaNaLinha4(int sensorX, int sensorY) {
        int x1 = sensorX;
        int y1 = sensorY;

        return (x1 >= 570 && x1 <= 580 && y1 >= 1070 && y1 <= 1320);
    }

    /*// entrada do semáforo 3
    public boolean estaNaLinha5(int sensorX, int sensorY) {
        int x1 = sensorX;
        int y1 = sensorY;

        return (x1 >= 40 && x1 <= 280 && y1 >= 1040 && y1 <= 1050);
    }

    // saída do semáforo 3
    public boolean estaNaLinha6(int sensorX, int sensorY) {
        int x1 = sensorX;
        int y1 = sensorY;

        return (x1 >= 300 && x1 <= 310 && y1 >= 1070 && y1 <= 1320);
    }*/

    // medidor de fluxo 1
    public boolean estaNoFluxo1(int sensorX, int sensorY) {
        int x1 = sensorX;
        int y1 = sensorY;

        return (x1 >= 850 && x1 <= 860 && y1 >= 1070 && y1 <= 1320);
    }

    // medidor de fluxo 2
    public boolean estaNoFluxo2(int sensorX, int sensorY) {
        int x1 = sensorX;
        int y1 = sensorY;

        return (x1 >= 850 && x1 <= 1080 && y1 >= 880 && y1 <= 890);
    }

    // medidor de fluxo 3
    public boolean estaNoFluxo3(int sensorX, int sensorY) {
        int x1 = sensorX;
        int y1 = sensorY;

        return (x1 >= 850 && x1 <= 1080 && y1 >= 480 && y1 <= 490);
    }

    // medidor de fluxo 4
    public boolean estaNoFluxo4(int sensorX, int sensorY) {
        int x1 = sensorX;
        int y1 = sensorY;

        return (x1 >= 800 && x1 <= 810 && y1 >= 0 && y1 <= 260);
    }

    // medidor de fluxo 5
    public boolean estaNoFluxo5(int sensorX, int sensorY) {
        int x1 = sensorX;
        int y1 = sensorY;

        return (x1 >= 500 && x1 <= 510 && y1 >= 20 && y1 <= 280);
    }

    // medidor de fluxo 6
    public boolean estaNoFluxo6(int sensorX, int sensorY) {
        int x1 = sensorX;
        int y1 = sensorY;

        return (x1 >= 280 && x1 <= 290 && y1 >= 80 && y1 <= 380);
    }

    // medidor de fluxo 7
    public boolean estaNoFluxo7(int sensorX, int sensorY) {
        int x1 = sensorX;
        int y1 = sensorY;

        return (x1 >= 0 && x1 <= 255 && y1 >= 430 && y1 <= 440);
    }

    // medidor de fluxo 8
    public boolean estaNoFluxo8(int sensorX, int sensorY) {
        int x1 = sensorX;
        int y1 = sensorY;

        return (x1 >= 300 && x1 <= 310 && y1 >= 720 && y1 <= 970);
    }

    // medidor de fluxo 9
    public boolean estaNoFluxo9(int sensorX, int sensorY) {
        int x1 = sensorX;
        int y1 = sensorY;

        return (x1 >= 40 && x1 <= 280 && y1 >= 1020 && y1 <= 1030);
    }

    // medidor de fluxo 10
    public boolean estaNoFluxo10(int sensorX, int sensorY) {
        int x1 = sensorX;
        int y1 = sensorY;

        return (x1 >= 300 && x1 <= 310 && y1 >= 1070 && y1 <= 1320);
    }
}
