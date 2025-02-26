package com.example.corrida;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;

public class GameView extends View {

    private Bitmap pista;
    private ArrayList<Car> carros;
    private ArrayList<Thread> threadsCarros;
    private Semaphore semaforo;
    private boolean corridaAtiva;
    private boolean corridaPausada;

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Inicializar a lista de carros
        carros = new ArrayList<Car>();
        threadsCarros = new ArrayList<Thread>();

        // A corrida não começa de imediato
        corridaAtiva = false;
        corridaPausada = false;

        // Carregar e redimensionar a pista
        inicializarPista();
    }

    // Método para carregar a imagem da pista e redimensioná-la
    private void inicializarPista() {
        // Carregar o Bitmap original da pista
        Bitmap pistaOriginal = BitmapFactory.decodeResource(getResources(), R.drawable.pista);

        // Redimensionar a pista de acordo com o tamanho da GameView
        // Só será possível após a View ser renderizada, então usa-se getWidth() e getHeight()
        post(new Runnable() {
            @Override
            public void run() {
                try {
                    int larguraGameView = getWidth();
                    int alturaGameView = getHeight();

                    // Redimensiona a pista para caber na GameView
                    pista = Bitmap.createScaledBitmap(pistaOriginal, larguraGameView, alturaGameView, true);

                    // Redesenha a GameView com a pista ajustada
                    invalidate();
                } catch (Exception e){
                    Log.e("GameView", "Erro ao inicializar a pista", e);
                }
            }
        });
    }

    public void iniciarCorrida(int numCarros) {
        try {
            if (!corridaPausada) {
                corridaAtiva = true;
                corridaPausada = false;

                semaforo = new Semaphore(1);

                // Carregar a imagem do safetcar
                Bitmap imagemSafetCarOriginal = BitmapFactory.decodeResource(getResources(), R.drawable.carro); //alterar posteriormente

                // Redimensionar o bitmap para o tamanho desejado (exemplo: 150x75 pixels)
                int novoLargura = 120;  // Largura desejada
                int novoAltura = 60;    // Altura desejada
                Bitmap imagemSafetCar = Bitmap.createScaledBitmap(imagemSafetCarOriginal, novoLargura, novoAltura, true);

                // Carregar a imagem do carro
                Bitmap imagemCarroOriginal = BitmapFactory.decodeResource(getResources(), R.drawable.carro);

                // Redimensionar o bitmap para o tamanho desejado (exemplo: 150x75 pixels)
                int novoLargura2 = 100;  // Largura desejada
                int novoAltura2 = 50;    // Altura desejada
                Bitmap imagemCarro = Bitmap.createScaledBitmap(imagemCarroOriginal, novoLargura2, novoAltura2, true);

                // Definir a posição inicial (no eixo X) e um espaçamento fixo no eixo Y
                int startX = 390;  // Posição X fixa, por exemplo, 390 pixels à direita da borda esquerda da pista
                int startY = 1170;  // Posição inicial no eixo Y (primeiro carro)
                int espacoEntreCarros = 10;  // Espaçamento entre os carros

                for (int i = 0; i < numCarros; i++) { //Desfazer alteração teste
                    if (i == 0) {
                        // Criar safetcar na posição inicial
                        Car safetCar = new SafetCar(pista, imagemSafetCar, i,startX + 10, startY, carros, semaforo, 8);
                        safetCar.criarSensores();
                        // Adicionar safetcar à lista de carros
                        carros.add(safetCar);

                        // Criar uma nova Thread para cada carro
                        Thread threadSafetCar = new Thread(safetCar);
                        threadsCarros.add(threadSafetCar);
                        threadSafetCar.start();  // Inicia a thread do carro
                        threadSafetCar.setPriority(5); // Muda a prioridade do safetcar
                    } else {
                        // Criar um novo carro na próxima posição, um atrás do outro no eixo Y
                        Car carro = new RacingCar(pista, imagemCarro, i, startX - i * (imagemCarro.getWidth() + espacoEntreCarros), startY, carros, semaforo, 5);
                        carro.criarSensores();
                        // Adicionar o carro à lista de carros
                        carros.add(carro);

                        // Criar uma nova Thread para cada carro
                        Thread threadCarro = new Thread(carro);
                        threadsCarros.add(threadCarro);
                        threadCarro.start();  // Inicia a thread do carro
                        threadCarro.setPriority(5); // Muda a prioridade do carro
                    }
                }
            } else {
                corridaPausada = false;
                for (Car carro : carros) {
                    carro.retomar();  // Método para remover o flag de pausa do carro
                }
            }

            invalidate();
        } catch (Exception e){
            Log.e("GameView", "Erro ao iniciar corrida", e);
        }
    }

    // Método para pausar a corrida (sem interromper as threads completamente)
    public void pausarCorrida() {
        corridaPausada = true;  // Ativar o flag de pausa em todos os carros
        for (Car carro : carros) {
            carro.pausar();  // Método que define o flag de pausa no carro
        }
    }

    // Método para finalizar a corrida e interromper as threads
    public void finalizarCorrida() {
        try {
            corridaAtiva = false;

            // Interrompe todas as threads
            for (Thread thread : threadsCarros) {
                thread.interrupt();  // Interrompe as threads ativas
            }

            carros.clear();  // Limpar a lista de carros
            threadsCarros.clear();  // Limpar a lista de threads

            invalidate();  // Redesenhar a tela sem os carros
        } catch (Exception e){
            Log.e("GameView", "Erro ao finalizar corrida", e);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        try {
            super.onDraw(canvas);

            // Desenhar a pista no plano de fundo
            if (pista != null) {
                canvas.drawBitmap(pista, 0, 0, null);  // Desenha a pista na posição (0,0)
            }

            desenharLinhasCurva(canvas);

            if (corridaAtiva) {
                // Desenhar os carros (apenas se houver carros na lista)
                for (Car carro : carros) {
                    carro.draw(canvas);  // Método que desenha o carro
                }

                // Redesenhar a tela continuamente
                if (!corridaPausada) {
                    invalidate();  // Continuar chamando onDraw
                }
            }
        } catch (Exception e){
            Log.e("GameView", "Erro ao desenhar tela do jogo", e);
        }
    }

    // Método para desenhar as linhas de início e fim da curva
    private void desenharLinhasCurva(Canvas canvas) {
        // Definir cor e espessura da linha
        Paint paint1 = new Paint();   // Largada e chegada
        paint1.setColor(Color.GRAY);  // Cor da linha
        paint1.setStrokeWidth(16);    // Espessura da linha

        Paint paint2 = new Paint();   // Semáforos
        paint2.setColor(Color.RED);   // Cor da linha
        paint2.setStrokeWidth(16);    // Espessura da linha

        Paint paint3 = new Paint();   // Medidores de fluxo
        paint3.setColor(Color.BLUE);  // Cor da linha
        paint3.setStrokeWidth(16);    // Espessura da linha

        //Linha de largada/chegada
        canvas.drawLine(540, 1019, 540, 1362, paint1);

        // Linha 1: Início do semáforo 1
        //canvas.drawLine(850, 300, 1080, 300, paint2); // rota 1
        canvas.drawLine(0, 410, 255, 410, paint2); // rota 2

        // Linha 2: Fim do semáforo 1
        //canvas.drawLine(800, 0, 800, 260, paint2); // rota 1
        canvas.drawLine(280, 450, 280, 720, paint2); // rota 2

        // Linha 3: Início do semáforo 2
        //canvas.drawLine(490, 350, 490, 620, paint2);
        canvas.drawLine(600, 670, 840, 670, paint2);

        // Linha 4: Fim do semáforo 2
        //canvas.drawLine(600, 660, 840, 660, paint2);
        canvas.drawLine(570, 720, 570, 960, paint2);

        // Linha 5: Início do semáforo 3
        //canvas.drawLine(40, 1040, 280, 1040, paint2);

        // Linha 6: Fim do semáforo 3
        //canvas.drawLine(300, 1070, 300, 1320, paint2);


        // Linha do medidor F1
        //canvas.drawLine(750, 1070, 750, 1320, paint3); // rota 1
        canvas.drawLine(850, 1070, 850, 1320, paint3);

        // Linha do medidor F2
        canvas.drawLine(850, 880, 1080, 880, paint3);

        // Linha do medidor F3
        canvas.drawLine(850, 480, 1080, 480, paint3);

        // Linha do medidor F4
        //canvas.drawLine(600, 10, 600, 270, paint3); // rota 1
        canvas.drawLine(800, 0, 800, 260, paint3);

        // Linha do medidor F5
        //canvas.drawLine(0, 410, 255, 410, paint3);
        canvas.drawLine(500, 20, 500, 280, paint3);

        // Linha do medidor F6
        //canvas.drawLine(380, 395, 380, 675, paint3);
        canvas.drawLine(280, 80, 280, 380, paint3);

        // Linha do medidor F7
        //canvas.drawLine(600, 690, 840, 690, paint3);
        canvas.drawLine(0, 430, 255, 430, paint3);

        // Linha do medidor F8
        //canvas.drawLine(450, 720, 450, 970, paint3);
        canvas.drawLine(300, 720, 300, 970, paint3);

        // Linha do medidor F9
        //canvas.drawLine(40, 990, 280, 990, paint3);
        canvas.drawLine(40, 1020, 280, 1020, paint3);

        // Linha do medidor F10
        //canvas.drawLine(400, 1070, 400, 1320, paint3); // rota 1
        canvas.drawLine(300, 1070, 300, 1320, paint3);
    }

}