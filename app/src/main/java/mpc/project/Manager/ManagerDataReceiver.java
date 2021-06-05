package mpc.project.Manager;

import mpc.project.util.Pair;
import org.checkerframework.checker.units.qual.C;

import java.math.BigInteger;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

public class ManagerDataReceiver {
    final private ManagerMain manager;

    public ManagerDataReceiver(ManagerMain manager) {
        this.manager = manager;
        try {
            networkFormedFlag.acquire();
            privateKeyGenerationFlag.acquire();
            shadowCollectedFlag.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    final private Object formNetworkLock = new Object();
    final private Semaphore networkFormedFlag = new Semaphore(1);
    volatile private int formNetworkCounter = 0;

    public void receiveNetworkFormingResponse() {
        synchronized (formNetworkLock) {
            formNetworkCounter++;
            if (formNetworkCounter == manager.getClusterSize()) {
                formNetworkCounter = 0;
                networkFormedFlag.release();
            }
        }
    }

    public void waitNetworkForming() {
        try {
            networkFormedFlag.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private final Semaphore modulusGenerationFlag = new Semaphore(0);
    private Pair<BigInteger, Long> modulusWorkflowPair;

    public void receiveModulusGenerationResponse(BigInteger modulus, long workflowID) {
        modulusWorkflowPair = new Pair<>(modulus, workflowID);
        modulusGenerationFlag.release();
    }

    public Pair<BigInteger, Long> waitModulusGeneration() {
        try {
            modulusGenerationFlag.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Pair<BigInteger, Long> result = new Pair<>(
                modulusWorkflowPair.first,
                modulusWorkflowPair.second
        );
        return result;
    }

    private final Object primalityTestLock = new Object();
    private final Map<Long, Semaphore> primalityTestFlagMap = new ConcurrentHashMap<>();
    private final Map<Long, Boolean> primalityTestResultMap = new ConcurrentHashMap<>();

    public void checkEmptyPrimalityTest(long workflowID){
        synchronized (primalityTestLock){
            if(!primalityTestFlagMap.containsKey(workflowID)){
                primalityTestFlagMap.put(workflowID, new Semaphore(0));
            }
        }
    }

    public void cleanPrimalityTestBucket(long workflowID){
        synchronized (primalityTestLock){
            primalityTestFlagMap.remove(workflowID);
            primalityTestResultMap.remove(workflowID);
        }
    }

    public void receivePrimalityTestResult(boolean primalityTestResult, long workflowID) {
        checkEmptyPrimalityTest(workflowID);
        primalityTestResultMap.put(workflowID, primalityTestResult);
        primalityTestFlagMap.get(workflowID).release();
    }

    public boolean waitPrimalityTestResult(long workflowID) {
        checkEmptyPrimalityTest(workflowID);
        try {
            primalityTestFlagMap.get(workflowID).acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        boolean result = primalityTestResultMap.get(workflowID);
        cleanPrimalityTestBucket(workflowID);
        return result;
    }

    final Object privateKeyGenerationLock = new Object();
    final private Semaphore privateKeyGenerationFlag = new Semaphore(1);
    volatile private int privateKeyGenerationCounter = 0;

    public void receivePrivateKeyGenerationResponse() {
        synchronized (privateKeyGenerationLock) {
            privateKeyGenerationCounter++;
            if (privateKeyGenerationCounter == manager.getClusterSize()) {
                privateKeyGenerationCounter = 0;
                privateKeyGenerationFlag.release();
            }
        }
    }

    public void waitPrivateKeyGeneration() {
        try {
            privateKeyGenerationFlag.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    final private Object decryptionLock = new Object();
    final private Semaphore shadowCollectedFlag = new Semaphore(1);
    volatile private int decryptionCounter = 0;

    public void receiveDecryptionResult(int id, String shadow, String[] resultBucket) {
        resultBucket[id - 1] = shadow;
        synchronized (decryptionLock) {
            decryptionCounter++;
            if (decryptionCounter == manager.getClusterSize()) {
                decryptionCounter = 0;
                shadowCollectedFlag.release();
            }
        }
    }

    public void waitDecryptionShadow() {
        try {
            shadowCollectedFlag.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
