package mpc.project;

import io.grpc.stub.StreamObserver;
import mpc.project.util.RpcUtility;

import java.math.BigInteger;

public class WorkerRPCSender {
    private WorkerServiceGrpc.WorkerServiceStub[] stubs;
    private ManagerServiceGrpc.ManagerServiceBlockingStub managerStub;

    public void setManagerStub(ManagerServiceGrpc.ManagerServiceBlockingStub managerStub) {
        this.managerStub = managerStub;
    }

    private WorkerMain worker;
    public WorkerRPCSender(WorkerMain worker){
        this.worker = worker;
    }

    public void setStubs(WorkerServiceGrpc.WorkerServiceStub[] stubs) {
        this.stubs = stubs;
    }

    public void sendPQH(int i, BigInteger p, BigInteger q, BigInteger h) {
        ExchangePrimespqhRequest request = RpcUtility.Request.newExchangePrimesRequest(worker.getId(), p, q, h);
        stubs[i - 1].exchangePrimesPQH(request, new StreamObserver<>() {
            @Override
            public void onNext(StdResponse response) {
            }

            @Override
            public void onError(Throwable t) {
                System.out.println("exchangePQH RPC error for " + i + " : " + t.getMessage());
                System.exit(-1);
            }

            @Override
            public void onCompleted() {
//                System.out.println("sent!");
            }
        });
    }

    public void sendNPiece(int i, BigInteger nPiece) {
        StdRequest request = RpcUtility.Request.newStdRequest(worker.getId(), nPiece);
        stubs[i - 1].exchangeNPiece(request, new StreamObserver<StdResponse>() {
            @Override
            public void onNext(StdResponse response) {
            }

            @Override
            public void onError(Throwable t) {
                System.out.println("sendNPiece RPC error for " + i + " : " + t.getMessage());
                System.exit(-1);
            }

            @Override
            public void onCompleted() {
//                System.out.println("sent!");
            }
        });
    }

    public void sendPrimalityTestRequest(int i, BigInteger g, BigInteger[] resultBucket) {
        StdRequest request = RpcUtility.Request.newStdRequest(worker.getId(), g);
        stubs[i].primalityTest(request, new StreamObserver<PrimalityTestResponse>() {
            @Override
            public void onNext(PrimalityTestResponse value) {
                int id = value.getId();
                BigInteger v = new BigInteger(value.getV().toByteArray());
                worker.getDataReceiver().receiveVerificationFactor(id, v, resultBucket);
            }

            @Override
            public void onError(Throwable t) {
                System.out.println("primalityTest to Guests RPC Error: " + t.getMessage());
                System.exit(-1);
            }

            @Override
            public void onCompleted() {
            }
        });
    }

    public void sendGamma(int i, BigInteger gamma) {
        StdRequest request = RpcUtility.Request.newStdRequest(worker.getId(), gamma);
        stubs[i - 1].exchangeGamma(request, new StreamObserver<StdResponse>() {
            @Override
            public void onNext(StdResponse response) {
//                System.out.println("received by " + response.getId());
            }

            @Override
            public void onError(Throwable t) {
                System.out.println("sendGamma RPC error for " + i + " : " + t.getMessage());
                System.exit(-1);
            }

            @Override
            public void onCompleted() {
            }
        });
    }

    public void sendGammaSum(int i, BigInteger gammaSum) {
        StdRequest request = RpcUtility.Request.newStdRequest(worker.getId(), gammaSum);
        stubs[i - 1].exchangeGammaSum(request, new StreamObserver<StdResponse>() {
            @Override
            public void onNext(StdResponse response) {
//                System.out.println("received by " + response.getId());
            }

            @Override
            public void onError(Throwable t) {
                System.out.println("sendGamma RPC error for " + i + " : " + t.getMessage());
                System.exit(-1);
            }

            @Override
            public void onCompleted() {
            }
        });
    }

    public void sendDecryptRequest(int i, String encryptedMessage, String[] resultBucket){
        stubs[i].decrypt(RpcUtility.Request.newStdRequest(worker.getId(), encryptedMessage),
                new StreamObserver<StdResponse>() {
                    @Override
                    public void onNext(StdResponse response) {
                        int id = response.getId();
                        String shadow = new String(response.getContents().toByteArray());
                        worker.getDataReceiver().receiveShadow(id, shadow, resultBucket);
                    }

                    @Override
                    public void onError(Throwable t) {
                        System.out.println("trial decryption error: " + t.getMessage());
                        System.exit(-1);
                    }

                    @Override
                    public void onCompleted() {

                    }
                });
    }
}
