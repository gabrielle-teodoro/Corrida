package com.example.corrida;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private GameView gameView;
    private TextView labelQuantCars;
    private EditText carCountInput;
    private Button startButton, pauseButton, stopButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gameView = findViewById(R.id.gameView);
        labelQuantCars = findViewById(R.id.labelQuantCars);
        carCountInput = findViewById(R.id.carCountInput);
        startButton = findViewById(R.id.startButton);
        pauseButton = findViewById(R.id.pauseButton);
        stopButton = findViewById(R.id.stopButton);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRace();
            }
        });

        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pauseRace();
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopRace();
            }
        });
    }

    private void startRace() {
        String carCountString = carCountInput.getText().toString();
        if (!carCountString.isEmpty()) {
            try {
                int carCount = Integer.parseInt(carCountString);
                if (carCount > 0) {
                    //Aqui você implementa o código para iniciar a corrida com o número de carros especificado
                    gameView.iniciarCorrida(carCount);
                    Toast.makeText(this, "Corrida iniciada com " + carCount + " carros", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Por favor, insira um número maior que zero", Toast.LENGTH_SHORT).show();
                }
            }catch (NumberFormatException e) {
                Toast.makeText(this, "Por favor, insira um valor numérico", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Por favor, insira um valor", Toast.LENGTH_SHORT).show();
        }
    }

    private void pauseRace() {
        // Aqui você implementa o código para pausar a corrida
        gameView.pausarCorrida();
        Toast.makeText(this, "Corrida pausada", Toast.LENGTH_SHORT).show();
    }

    private void stopRace() {
        // Aqui você implementa o código para parar a corrida
        gameView.finalizarCorrida();
        Toast.makeText(this, "Corrida encerrada", Toast.LENGTH_SHORT).show();
    }
}


