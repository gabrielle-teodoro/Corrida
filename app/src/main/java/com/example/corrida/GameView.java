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
                Bitmap imagemSafetCarOriginal = BitmapFactory.decodeResource(getResources(), R.drawable.safetcar);

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
                int startY = 1150;  // Posição inicial no eixo Y (primeiro carro)
                int espacoEntreCarros = 10;  // Espaçamento entre os carros

                for (int i = 0; i < numCarros; i++) { //Desfazer alteração teste
                    if (i == 0) {
                        // Criar safetcar na posição inicial
                        Car safetCar = new SafetCar(pista, imagemSafetCar, i,startX + 10, startY, carros, semaforo, 5);
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
        Paint paint = new Paint();
        paint.setColor(Color.GRAY);  // Cor da linha (vermelha)
        paint.setStrokeWidth(16);    // Espessura da linha

        // Linha 1: Início da curva (x = 400, y = 200 até y = 500)
        canvas.drawLine(490, 330, 490, 630, paint);

        // Linha 2: Fim da curva (x = 500, y = 700 até y = 1000)
        canvas.drawLine(590, 700, 590, 1000, paint);

        //Linha de largada/chegada
        canvas.drawLine(540, 1019, 540, 1362, paint);
    }
}