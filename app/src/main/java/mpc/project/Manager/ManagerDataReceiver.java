package mpc.project.Manager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

public class ManagerDataReceiver {
    final private ManagerMain manager;

    public ManagerDataReceiver(ManagerMain manager) {
        this.manager = manager;
        try {
            networkFormedFlag.acquire();
            primalityTestCompleteFlag.acquire();
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

    private Map<Long, Semaphore> modulusGenerationFlagMap = new ConcurrentHashMap<>();

    synchronized private void checkEmptyModulusGeneration(long workflowID){
        if(!modulusGenerationFlagMap.containsKey(workflowID)){
            modulusGenerationFlagMap.put(workflowID, new Semaphore(0));
        }
    }

    public void receiveModulusGenerationResponse(long workflowID) {
        checkEmptyModulusGeneration(workflowID);
        modulusGenerationFlagMap.get(workflowID).release();
    }

    public void waitModulusGeneration(long workflowID) {
        checkEmptyModulusGeneration(workflowID);
        try {
            modulusGenerationFlagMap.get(workflowID).acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    final private Semaphore primalityTestCompleteFlag = new Semaphore(1);
    volatile private boolean primalityTestResult;

    public void receivePrimalityTestResult(boolean primalityTestResult) {
        this.primalityTestResult = primalityTestResult;
        primalityTestCompleteFlag.release();
    }

    public boolean waitPrimalityTestResult() {
        try {
            primalityTestCompleteFlag.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return primalityTestResult;
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
