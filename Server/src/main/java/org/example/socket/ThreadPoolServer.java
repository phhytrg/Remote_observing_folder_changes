package org.example.socket;

import org.example.models.Client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPoolServer implements Runnable{
//    INSTANCE;
    private Integer serverPort;
    private int maxClients;
    private boolean isStopped = true;
    private ServerSocket serverSocket;
    private ExecutorService threadPool;
//    private ThreadPoolServer instance;

    private ServerNotificationListener listener;

    public ThreadPoolServer(int serverPort, ServerNotificationListener listener) {
        this.serverPort = serverPort;
        this.maxClients = 10;
        this.threadPool = Executors.newFixedThreadPool(this.maxClients);
        this.listener = listener;
    }
    public ThreadPoolServer(int serverPort, int maxClients, ServerNotificationListener listener){
        this.serverPort = serverPort;
        this.maxClients = maxClients;
        this.threadPool = Executors.newFixedThreadPool(this.maxClients);
        this.listener = listener;
    }

    @Override
    public void run() {
        try {
            openServerSocket();
        } catch (IOException e) {
            throw new RuntimeException("Cannot open port " + serverPort);
        }

        this.listener.loggingForServerState();
        while (!isStopped){
            Socket clientSocket;
            try {
                clientSocket = serverSocket.accept();
            } catch (IOException e) {
                if(isStopped){
                    //No longer listen to the new connect
                    System.out.println("Server Stopped!!");
                    return;
                }
                throw new RuntimeException("Error accepting client connection",e);
            }

            WorkerRunnable clientThread = new WorkerRunnable(clientSocket, listener);
            Client client = new Client(clientThread);
//            Client.getClientList().add(client);
            listener.newClientConnected(client);

            this.threadPool.execute(clientThread);
        }
    }

    private void openServerSocket() throws IOException {
        this.serverSocket = new ServerSocket();
        serverSocket.setReuseAddress(true);
        serverSocket.bind(new InetSocketAddress(serverPort));
        isStopped = false;


    }

    public synchronized void stop() throws IOException {
        this.isStopped = true;
        serverSocket.close();
        listener.loggingForServerState();
    }

    public boolean isStopped(){
        return isStopped;
    }

    public int getPort(){
        return serverPort;
    }

    public interface ServerNotificationListener {
        void loggingForServerState();
        void newClientConnected(Client client);
    }

//    private static class SingletonBuilder{
//        private final Integer port;
//        private Integer maxClients;
//
//        private SingletonBuilder(){
//            port = 1234;
//            maxClients = 10;
//        }
//
//        public SingletonBuilder(int port, int maxClients){
//            this.port = port;
//            this.maxClients = maxClients;
//        }
//
//        public void build(){
//            ThreadPoolServer.INSTANCE.build(this);
//        }
//    }
//
//    private void build(SingletonBuilder singletonBuilder) {
//        this.serverPort = singletonBuilder.port;
//        Integer maxClients = singletonBuilder.maxClients;
//        threadPool = Executors.newFixedThreadPool(maxClients);
//    }
//
//    public static ThreadPoolServer getInstance(){
//        return INSTANCE;
//    }
}
