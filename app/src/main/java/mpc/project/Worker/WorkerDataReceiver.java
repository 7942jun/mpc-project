package mpc.project.Worker;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class WorkerDataReceiver {
    WorkerMain worker;

    public WorkerDataReceiver(WorkerMain worker) {
        this.worker = worker;
        try {
            this.gammaReadyFlag.acquire();
            this.gammaSumReadyFlag.acquire();
            this.verificationFactorsReadyFlag.acquire();
            this.shadowsReadyFlag.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public BigInteger[] gammaArr;
    public BigInteger[] gammaSumArr;

    public final Object exchangeGammaLock = new Object();
    private final Semaphore gammaReadyFlag = new Semaphore(1);
    private int exchangeGammaCounter = 0;

    public final Object exchangeGammaSumLock = new Object();
    private final Semaphore gammaSumReadyFlag = new Semaphore(1);
    private int exchangeGammaSumCounter = 0;

    private final Object shadowReceivingLock = new Object();
    private final Semaphore shadowsReadyFlag = new Semaphore(1);
    private int shadowReceivingCounter = 0;

    private final Object verificationFactorsLock = new Object();
    private final Semaphore verificationFactorsReadyFlag = new Semaphore(1);
    private int verificationFactorsCounter = 0;

    private Map<Long, Semaphore> modulusReadyFlagMap = new ConcurrentHashMap<>();
    private Map<Long, AtomicInteger> modulusCounterMap = new ConcurrentHashMap<>();
    private Map<Long, BigInteger> modulusMap = new ConcurrentHashMap<>();
    // Todo: Implement elegant GC mechanics

    synchronized private void emptyCheckModulus(long workflowID){
        if(!modulusReadyFlagMap.containsKey(workflowID)){
            modulusReadyFlagMap.put(workflowID, new Semaphore(0));
            modulusCounterMap.put(workflowID, new AtomicInteger(0));
        }
    }

    public void receiveModulus(int id, BigInteger modulus, long workflowID) {
        emptyCheckModulus(workflowID);
        if(modulusCounterMap.get(workflowID).incrementAndGet() == worker.getClusterSize()){
            modulusMap.put(workflowID, modulus);
            modulusCounterMap.get(workflowID).set(0);
            modulusReadyFlagMap.get(workflowID).release();
        }
    }

    public BigInteger waitModulus(long workflowID){
        emptyCheckModulus(workflowID);
        try {
            modulusReadyFlagMap.get(workflowID).acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return modulusMap.get(workflowID);
    }

    private Map<Long, Semaphore> primesReadyFlagMap = new ConcurrentHashMap<>();
    private Map<Long, AtomicInteger> exchangePrimesCounterMap = new ConcurrentHashMap<>();
    private Map<Long, BigInteger[]> pArrMap = new ConcurrentHashMap<>();
    private Map<Long, BigInteger[]> qArrMap = new ConcurrentHashMap<>();
    private Map<Long, BigInteger[]> hArrMap = new ConcurrentHashMap<>();

    synchronized private void emptyCheckPrimes(long workflowID){
        if(!primesReadyFlagMap.containsKey(workflowID)){
            primesReadyFlagMap.put(workflowID, new Semaphore(0));
            exchangePrimesCounterMap.put(workflowID, new AtomicInteger(0));
            pArrMap.put(workflowID, new BigInteger[worker.getClusterSize()]);
            qArrMap.put(workflowID, new BigInteger[worker.getClusterSize()]);
            hArrMap.put(workflowID, new BigInteger[worker.getClusterSize()]);
        }
    }

    public void receivePHQ(int id, BigInteger p, BigInteger q, BigInteger h, long workflowID){
        emptyCheckPrimes(workflowID);
        pArrMap.get(workflowID)[id-1] = p;
        qArrMap.get(workflowID)[id-1] = q;
        hArrMap.get(workflowID)[id-1] = h;
        if(exchangePrimesCounterMap.get(workflowID).incrementAndGet()==worker.getClusterSize()){
            primesReadyFlagMap.get(workflowID).release();
            exchangePrimesCounterMap.get(workflowID).set(0);
        }
    }

    public void waitPHQ(long workflowID, BigInteger[] pArr, BigInteger[] qArr, BigInteger[] hArr){
        emptyCheckPrimes(workflowID);
        try {
            primesReadyFlagMap.get(workflowID).acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Arrays.setAll(pArr, i -> pArrMap.get(workflowID)[i]);
        Arrays.setAll(qArr, i -> qArrMap.get(workflowID)[i]);
        Arrays.setAll(hArr, i -> hArrMap.get(workflowID)[i]);
    }

    private Map<Long, Semaphore> nPieceReadyFlagMap = new ConcurrentHashMap<>();
    private Map<Long, AtomicInteger> nPieceCounterMap = new ConcurrentHashMap<>();
    private Map<Long, BigInteger[]> nPieceArrMap = new ConcurrentHashMap<>();

    synchronized private void emptyCheckNPiece(long workflowID){
        if(!nPieceReadyFlagMap.containsKey(workflowID)){
            nPieceReadyFlagMap.put(workflowID, new Semaphore(0));
            nPieceCounterMap.put(workflowID, new AtomicInteger(0));
            nPieceArrMap.put(workflowID, new BigInteger[worker.getClusterSize()]);
        }
    }

    public void receiveNPiece(int id, BigInteger nPiece, long workflowID) {
        emptyCheckNPiece(workflowID);
        nPieceArrMap.get(workflowID)[id-1] = nPiece;
        if(nPieceCounterMap.get(workflowID).incrementAndGet() == worker.getClusterSize()){
            nPieceCounterMap.get(workflowID).set(0);
            nPieceReadyFlagMap.get(workflowID).release();
        }
    }

    public void waitNPieces(long workflowID, BigInteger[] nPieceArr){
        emptyCheckNPiece(workflowID);
        try {
            nPieceReadyFlagMap.get(workflowID).acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Arrays.setAll(nPieceArr, i -> nPieceArrMap.get(workflowID)[i]);
    }


    public void receiveGamma(int id, BigInteger gamma) {
        int i = id - 1;
        gammaArr[i] = gamma;
        synchronized (exchangeGammaLock) {
            exchangeGammaCounter++;
            if (exchangeGammaCounter == worker.getClusterSize()) {
                gammaReadyFlag.release();
                exchangeGammaCounter = 0;
            }
        }
    }

    public void waitGamma() {
        try {
            gammaReadyFlag.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private Map<Long, Semaphore> gammaReadyFlagMap = new ConcurrentHashMap<>();
    private Map<Long, AtomicInteger> gammaCounterMap = new ConcurrentHashMap<>();
    private Map<Long, BigInteger[]> gammaArrMap = new ConcurrentHashMap<>();

    synchronized private void emptyCheckGamma(long workflowID){
        if(!gammaReadyFlagMap.containsKey(workflowID)){
            gammaReadyFlagMap.put(workflowID, new Semaphore(0));
            gammaCounterMap.put(workflowID, new AtomicInteger(0));
            gammaArrMap.put(workflowID, new BigInteger[worker.getClusterSize()]);
        }
    }

    public void receiveGamma(int id, BigInteger gamma, long workflowID) {
        emptyCheckGamma(workflowID);
        gammaArrMap.get(workflowID)[id-1] = gamma;
        if(gammaCounterMap.get(workflowID).incrementAndGet() == worker.getClusterSize()){
            gammaCounterMap.get(workflowID).set(0);
            gammaReadyFlagMap.get(workflowID).release();
        }
    }

    public void waitGamma(long workflowID, BigInteger[] gammaArr){
        emptyCheckGamma(workflowID);
        try {
            gammaReadyFlagMap.get(workflowID).acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Arrays.setAll(gammaArr, i -> gammaArrMap.get(workflowID)[i]);
    }

    public void receiveGammaSum(int id, BigInteger gammaSum) {
        int i = id - 1;
        gammaSumArr[i] = gammaSum;
        synchronized (exchangeGammaSumLock) {
            exchangeGammaSumCounter++;
            if (exchangeGammaSumCounter == worker.getClusterSize()) {
                gammaSumReadyFlag.release();
                exchangeGammaSumCounter = 0;
            }
        }
    }

    public void waitGammaSum() {
        try {
            gammaSumReadyFlag.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private Map<Long, Semaphore> gammaSumReadyFlagMap = new ConcurrentHashMap<>();
    private Map<Long, AtomicInteger> gammaSumCounterMap = new ConcurrentHashMap<>();
    private Map<Long, BigInteger[]> gammaSumArrMap = new ConcurrentHashMap<>();

    synchronized private void emptyCheckGammaSum(long workflowID){
        if(!gammaSumReadyFlagMap.containsKey(workflowID)){
            gammaSumReadyFlagMap.put(workflowID, new Semaphore(0));
            gammaSumCounterMap.put(workflowID, new AtomicInteger(0));
            gammaSumArrMap.put(workflowID, new BigInteger[worker.getClusterSize()]);
        }
    }

    public void receiveGammaSum(int id, BigInteger gammaSum, long workflowID) {
        emptyCheckGammaSum(workflowID);
        gammaSumArrMap.get(workflowID)[id-1] = gammaSum;
        if(gammaSumCounterMap.get(workflowID).incrementAndGet() == worker.getClusterSize()){
            gammaSumCounterMap.get(workflowID).set(0);
            gammaSumReadyFlagMap.get(workflowID).release();
        }
    }

    public void waitGammaSum(long workflowID, BigInteger[] gammaSumArr){
        emptyCheckGammaSum(workflowID);
        try {
            gammaSumReadyFlagMap.get(workflowID).acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Arrays.setAll(gammaSumArr, i -> gammaArrMap.get(workflowID)[i]);
    }

    public void receiveShadow(int id, String factor, String[] resultBucket) {
        int j = id - 1;
        resultBucket[j] = factor;
        synchronized (shadowReceivingLock) {
            shadowReceivingCounter++;
            if (shadowReceivingCounter == worker.getClusterSize()) {
                shadowsReadyFlag.release();
                shadowReceivingCounter = 0;
            }
        }
    }

    public void waitShadows() {
        try {
            shadowsReadyFlag.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private Map<Long, Semaphore> shadowReadyFlagMap = new ConcurrentHashMap<>();
    private Map<Long, AtomicInteger> shadowCounterMap = new ConcurrentHashMap<>();
    private Map<Long, String[]> shadowArrMap = new ConcurrentHashMap<>();

    synchronized private void emptyCheckShadow(long workflowID){
        if(!shadowReadyFlagMap.containsKey(workflowID)){
            shadowReadyFlagMap.put(workflowID, new Semaphore(0));
            shadowCounterMap.put(workflowID, new AtomicInteger(0));
            shadowArrMap.put(workflowID, new String[worker.getClusterSize()]);
        }
    }

    public void receiveShadow(int id, String shadow, long workflowID) {
        emptyCheckShadow(workflowID);
        shadowArrMap.get(workflowID)[id-1] = shadow;
        if(shadowCounterMap.get(workflowID).incrementAndGet() == worker.getClusterSize()){
            shadowCounterMap.get(workflowID).set(0);
            shadowReadyFlagMap.get(workflowID).release();
        }
    }

    public void waitShadow(long workflowID, String[] shadowArr){
        emptyCheckShadow(workflowID);
        try {
            shadowReadyFlagMap.get(workflowID).acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Arrays.setAll(shadowArr, i -> shadowArrMap.get(workflowID)[i]);
    }

    public void receiveVerificationFactor(int id, BigInteger factor, BigInteger[] resultBucket) {
        int j = id - 1;
        resultBucket[j] = factor;
        synchronized (verificationFactorsLock) {
            verificationFactorsCounter++;
            if (verificationFactorsCounter == worker.getClusterSize()) {
                verificationFactorsReadyFlag.release();
                verificationFactorsCounter = 0;
            }
        }
    }

    public void waitVerificationFactors() {
        try {
            verificationFactorsReadyFlag.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private Map<Long, Semaphore> verificationFactorReadyFlagMap = new ConcurrentHashMap<>();
    private Map<Long, AtomicInteger> verificationFactorCounterMap = new ConcurrentHashMap<>();
    private Map<Long, BigInteger[]> verificationFactorArrMap = new ConcurrentHashMap<>();

    synchronized private void emptyCheckVerificationFactor(long workflowID){
        if(!verificationFactorReadyFlagMap.containsKey(workflowID)){
            verificationFactorReadyFlagMap.put(workflowID, new Semaphore(0));
            verificationFactorCounterMap.put(workflowID, new AtomicInteger(0));
            verificationFactorArrMap.put(workflowID, new BigInteger[worker.getClusterSize()]);
        }
    }

    public void receiveVerificationFactor(int id, BigInteger verificationFactor, long workflowID) {
        emptyCheckVerificationFactor(workflowID);
        verificationFactorArrMap.get(workflowID)[id-1] = verificationFactor;
        if(verificationFactorCounterMap.get(workflowID).incrementAndGet() == worker.getClusterSize()){
            verificationFactorCounterMap.get(workflowID).set(0);
            verificationFactorReadyFlagMap.get(workflowID).release();
        }
    }

    public void waitVerificationFactor(long workflowID, BigInteger[] verificationFactorArr){
        emptyCheckVerificationFactor(workflowID);
        try {
            verificationFactorReadyFlagMap.get(workflowID).acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Arrays.setAll(verificationFactorArr, i -> verificationFactorArrMap.get(workflowID)[i]);
    }
}
