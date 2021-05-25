package mpc.project;

import java.math.BigInteger;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class WorkerDataReceiver {
    WorkerMain worker;
    public WorkerDataReceiver(WorkerMain worker){
        this.worker = worker;
        this.primesReadyFlag.lock();
        this.nPiecesReadyFlag.lock();
        this.gammaReadyFlag.lock();
        this.gammaSumReadyFlag.lock();
        this.verificationFactorsReadyFlag.lock();
        this.shadowsReadyFlag.lock();
    }
    public BigInteger[] pArr;          // An array holding p_i ( i \in [1, clusterNum])
    public BigInteger[] qArr;          // An array holding q_i ( i \in [1, clusterNum])
    public BigInteger[] hArr;          // An array holding h_i ( i \in [1, clusterNum])

    public BigInteger[] nPieceArr;
    public BigInteger[] gammaArr;
    public BigInteger[] gammaSumArr;

    public final Object exchangeGammaLock = new Object();
    private Lock gammaReadyFlag = new ReentrantLock();
    private int exchangeGammaCounter = 0;

    public final Object exchangeGammaSumLock = new Object();
    private Lock gammaSumReadyFlag = new ReentrantLock();
    private int exchangeGammaSumCounter = 0;


    public final Object exchangePrimesLock = new Object();
    private Lock primesReadyFlag = new ReentrantLock();
    private int exchangePrimesCounter = 0;

    public final Object exchangeNPiecesLock = new Object();
    private Lock nPiecesReadyFlag = new ReentrantLock();
    private int exchangeNPiecesCounter = 0;

    private final Object shadowReceivingLock = new Object();
    private Lock shadowsReadyFlag = new ReentrantLock();
    private int shadowReceivingCounter = 0;

    private final Object verificationFactorsLock = new Object();
    private Lock verificationFactorsReadyFlag = new ReentrantLock();
    private int verificationFactorsCounter = 0;

    public void receivePHQ(int id, BigInteger p, BigInteger q, BigInteger h){
        int i = id - 1;
        pArr[i] = p;
        qArr[i] = q;
        hArr[i] = h;
        synchronized (exchangePrimesLock) {
            exchangePrimesCounter++;
            if (exchangePrimesCounter == worker.getClusterSize()) {
                primesReadyFlag.unlock();
                exchangePrimesCounter = 0;
            }
        }
    }

    public void waitPHQ(){
        primesReadyFlag.lock();
    }

    public void receiveNPiece(int id, BigInteger nPiece){
        int i = id - 1;
        nPieceArr[i] = nPiece;
        synchronized (exchangeNPiecesLock){
            exchangeNPiecesCounter++;
            if(exchangeNPiecesCounter == worker.getClusterSize()){
                nPiecesReadyFlag.unlock();
                exchangeNPiecesCounter = 0;
            }
        }
    }

    public void waitNPieces(){
        nPiecesReadyFlag.lock();
    }

    public void receiveGamma(int id, BigInteger gamma){
        int i = id - 1;
        gammaArr[i] = gamma;
        synchronized (exchangeGammaLock){
            exchangeGammaCounter++;
            if(exchangeGammaCounter == worker.getClusterSize()){
                gammaReadyFlag.unlock();
                exchangeGammaCounter = 0;
            }
        }
    }

    public void waitGamma(){
        gammaReadyFlag.lock();
    }

    public void receiveGammaSum(int id, BigInteger gammaSum){
        int i = id - 1;
        gammaSumArr[i] = gammaSum;
        synchronized (exchangeGammaSumLock){
            exchangeGammaSumCounter++;
            if(exchangeGammaSumCounter == worker.getClusterSize()){
                gammaSumReadyFlag.unlock();
                exchangeGammaSumCounter = 0;
            }
        }
    }

    public void waitGammaSum(){
        gammaSumReadyFlag.lock();
    }

    public void receiveShadow(int id, String factor, String[] resultBucket){
        int j = id - 1;
        resultBucket[j] = factor;
        synchronized (shadowReceivingLock) {
            shadowReceivingCounter++;
            if (shadowReceivingCounter == worker.getClusterSize()) {
                shadowsReadyFlag.unlock();
            }
        }
    }

    public void waitShadows(){
        shadowsReadyFlag.lock();
    }

    public void receiveVerificationFactor(int id, BigInteger factor, BigInteger[] resultBucket){
        int j = id - 1;
        resultBucket[j] = factor;
        synchronized (verificationFactorsLock) {
            verificationFactorsCounter++;
            if (verificationFactorsCounter == worker.getClusterSize()) {
                verificationFactorsReadyFlag.unlock();
            }
        }
    }

    public void waitVerificationFactors(){
        verificationFactorsReadyFlag.lock();
    }
}
