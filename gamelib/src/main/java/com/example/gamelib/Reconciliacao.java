package com.example.gamelib;

import android.util.Log;

import org.ejml.data.DMatrixRMaj;
import org.ejml.simple.SimpleMatrix;

public class Reconciliacao {

    private double[] reconciledFlowDouble;
    private SimpleMatrix reconciledFlow;
    private SimpleMatrix adjustment;
    private SimpleMatrix rawMeasurement;
    private SimpleMatrix standardDeviation;
    private SimpleMatrix varianceMatrix;
    private SimpleMatrix incidenceMatrix;
    private SimpleMatrix diagonalMatrix;
    private SimpleMatrix weightsArray;


    public Reconciliacao(double[] _rawMeasurement, double[] _standardDeviation, double[] _incidenceMatrix) {

        if ((_rawMeasurement != null) && (_standardDeviation != null) && (_incidenceMatrix != null)) {
            if ((_rawMeasurement.length == _standardDeviation.length) && (_standardDeviation.length == _incidenceMatrix.length)) {

                this.incidenceMatrix = new SimpleMatrix(_incidenceMatrix.length, 1, true, _incidenceMatrix);
                this.rawMeasurement = new SimpleMatrix(_rawMeasurement.length, 1, true, _rawMeasurement);
                this.standardDeviation = new SimpleMatrix(_standardDeviation.length, 1, true, _standardDeviation);

                double[][] auxDiagonalMatrix = new double[_rawMeasurement.length + 1][_rawMeasurement.length + 1];
                double[] auxWeightsArray = new double[_rawMeasurement.length + 1];
                for (int i = 0; i < _rawMeasurement.length; i++) {
                    double auxMP = Math.pow((/*_rawMeasurement[i] **/ _standardDeviation[i]), 2);
                    auxDiagonalMatrix[i][i] = 2 / auxMP;
                    auxWeightsArray[i] = (2 * _rawMeasurement[i]) / auxMP;
                }

                this.diagonalMatrix = new SimpleMatrix(auxDiagonalMatrix);
                this.diagonalMatrix.setColumn(auxDiagonalMatrix.length - 1, 0, _incidenceMatrix);
                this.diagonalMatrix.setRow(auxDiagonalMatrix.length - 1, 0, _incidenceMatrix);
                this.weightsArray = new SimpleMatrix(auxWeightsArray.length, 1, true, auxWeightsArray);

                this.reconciledFlow = this.diagonalMatrix.invert().mult(this.weightsArray);
                DMatrixRMaj temp = this.reconciledFlow.getMatrix();
                this.reconciledFlowDouble = temp.getData();

            } else {
                System.out.println(
                        "the rawMeasurement and/or standardDeviation and/or incidenceMatrix have inconsistent data/size.");
            }

        } else {
            System.out.println("the rawMeasurement and/or standardDeviation and/or incidenceMatrix have null data.");
        }
    }

    public void printMatrix(double[] _m) {

        if (_m != null) {
            for (int i = 0; i < _m.length; i++) {
                System.out.println("| " + _m[i] + " | ");
            }
            System.out.println("");

        } else {
            System.out.println("the array has null data.");
        }
    }

    public double[] getReconciledFlow() {
        return this.reconciledFlowDouble;
    }

    public SimpleMatrix getAdjustment() {
        return this.adjustment;
    }

    public SimpleMatrix getRawMeasurement() {
        return this.rawMeasurement;
    }

    public SimpleMatrix getStandardDeviation() {
        return this.standardDeviation;
    }

    public SimpleMatrix getVarianceMatrix() {
        return this.varianceMatrix;
    }

    public SimpleMatrix getIncidenceMatrix() {
        return this.incidenceMatrix;
    }

    public SimpleMatrix getDiagonalMatrix() {
        return this.diagonalMatrix;
    }

    public SimpleMatrix getWeightsArray() {
        return this.weightsArray;
    }


    public static void reconciliacao(double[] _rawMeasurement, double[] _standardDeviation, double[] _incidenceMatrix) {

        if ((_rawMeasurement != null) && (_standardDeviation != null) && (_incidenceMatrix != null)) {
            if ((_rawMeasurement.length == _standardDeviation.length) && (_standardDeviation.length == _incidenceMatrix.length)) {

                double[] mpesoArray = new double[_rawMeasurement.length + 1];
                for (int i = 0; i < _rawMeasurement.length; i++) {
                    mpesoArray[i] = 2 * _rawMeasurement[i] / (_standardDeviation[i] * _standardDeviation[i]);
                }
                mpesoArray[_rawMeasurement.length] = 0; // Último elemento do vetor mpeso

                SimpleMatrix mpeso = new SimpleMatrix(mpesoArray.length, 1, true, mpesoArray);

                // Matriz Diagonal inversa
                double[][] diag1Array = new double[_rawMeasurement.length][_rawMeasurement.length];
                for (int i = 0; i < _rawMeasurement.length; i++) {
                    diag1Array[i][i] = 2 / (_standardDeviation[i] * _standardDeviation[i]);
                }
                SimpleMatrix Diag1 = new SimpleMatrix(diag1Array);

                // Monta a matriz de pesos (Peso)
                double[][] pesoArray = new double[_rawMeasurement.length + 1][_rawMeasurement.length + 1];

                // Copia a matriz Diag1 para Peso
                for (int i = 0; i < _rawMeasurement.length; i++) {
                    for (int j = 0; j < _rawMeasurement.length; j++) {
                        pesoArray[i][j] = Diag1.get(i, j);
                    }
                }

                // Insere a matriz b na última coluna e última linha
                for (int i = 0; i < _rawMeasurement.length; i++) {
                    pesoArray[i][_rawMeasurement.length] = _incidenceMatrix[i]; // Última coluna
                    pesoArray[_rawMeasurement.length][i] = _incidenceMatrix[i]; // Última linha
                }
                pesoArray[_rawMeasurement.length][_rawMeasurement.length] = 0;

                SimpleMatrix Peso = new SimpleMatrix(pesoArray);

                // Calcula a inversa da matriz Peso
                SimpleMatrix InvPeso = Peso.invert();

                // Calcula as medidas reconciliadas e o multiplicador de Lagrange
                SimpleMatrix Result = InvPeso.mult(mpeso);

                // Extrai as medidas reconciliadas
                double[] mrec = new double[_rawMeasurement.length];
                for (int i = 0; i < _rawMeasurement.length; i++) {
                    mrec[i] = Result.get(i, 0);
                }

                //Log.d("Reconciliacao", "y_hat: " + Arrays.toString(mrec));

                System.out.println("Valores reconciliados (Y_hat):");
                for (int i = 0; i < mrec.length; i++) {
                    System.out.println("| " + mrec[i] + " | ");
                    System.out.println("");
                    //System.out.println(String.format("%.3f", mrec[i]));
                }
                System.out.println("");

            } else {
                Log.d("Reconciliacao","the rawMeasurement and/or standardDeviation and/or incidenceMatrix have inconsistent data/size.");
            }

        } else {
            Log.d("Reconciliacao","the rawMeasurement and/or standardDeviation and/or incidenceMatrix have null data.");
        }
    }

}
