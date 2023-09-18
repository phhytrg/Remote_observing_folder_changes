package org.example.socket;

import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WorkerRunnable implements Runnable{

    private final Socket socket;
//    private ThreadPoolServer.ServerNotificationListener listener;
    private ClientListener clientListener;
    private String sendMsg;
    private boolean isCancel = false;

    public void setClientListener(ClientListener clientListener) {
        this.clientListener = clientListener;
    }

    private boolean chooseBtnClicked;

    public WorkerRunnable(Socket socket, ThreadPoolServer.ServerNotificationListener listener) {
        this.socket = socket;
//        this.client = client;
//        this.listener = listener;
    }

    public void setCancel(boolean cancel) {
        isCancel = cancel;
    }

    public DataInputStream getInFromClient() {
        return inFromClient;
    }

    DataInputStream inFromClient;
    DataOutputStream outToClient;
    @Override
    public void run() {
        try{
            inFromClient = new DataInputStream(socket.getInputStream());
            outToClient = new DataOutputStream(socket.getOutputStream());
//            listener.clientLeft(client);
        }
        catch (IOException e) {
            // Client disconnected
//            listener.clientLeft(client);
//            clientListener.clientLeft();
//            throw new RuntimeException(e);
        }

        //Wait for server to select folder
        synchronized (this){
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        if(this.isCancel){
            try {
                inFromClient.close();
                outToClient.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            System.out.println("Worker Stop");
            return;
        }

        try {
            signalizeClient();
        } catch (IOException e) {
            clientListener.clientLeftBeforeServerAction();
            return;
        }

        while(!isCancel) {
            String receivedMsg = null;
            try {
                receivedMsg = inFromClient.readUTF();
                String[] tokens = receivedMsg.split("\n");
                if(tokens[0].equals("OK")){
                    String folderName = tokens[1];
                    clientListener.setClientFolderName(folderName);
                    clientListener.setFolderObservedLabel(folderName);
                    clientListener.closeSelectFolderDialog();
                    clientListener.changeListIcon();
                    break;
                }
                else if(tokens[0].equals("Inaccessible files or folders")){
                    JOptionPane.showMessageDialog(null, receivedMsg,"Error", JOptionPane.ERROR_MESSAGE);
                }
                else{
                    List<String> dirs = retrieveDirList(receivedMsg);
                    clientListener.changeListModel(dirs);
                }
                clientListener.setEnableChooseBtn(true);
                synchronized (this) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                try {
                    outToClient.writeUTF(sendMsg);
                    clientListener.setEnableChooseBtn(false);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } catch (IOException e) {
                clientListener.clientLeft();
                return;
            }
        }

        while(!isCancel){
            try {
                String msg = inFromClient.readUTF();
                clientListener.displayFolderChanges(msg);
            } catch (IOException e) {
                try {
                    inFromClient.close();
                    outToClient.close();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }

                System.out.println("WorkerRunnable - stop");
                clientListener.clientLeft();
                return;
//                throw new RuntimeException(e);
            }
        }

        try {
            inFromClient.close();
            outToClient.close();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        clientListener.clientLeft();
    }

    private List<String> retrieveDirList(String msg){
        List<String> dirs = new ArrayList<>(Arrays.asList(msg.split("\n")));
        return dirs;
    }

    public void signalizeClient() throws IOException {
        outToClient.writeUTF(String.valueOf(Tag.START_CHOOSING_FOLDER));
    }

    public void sendMsg(String tag){
        this.sendMsg = tag;
    }
    public void sendMsg(String tag, String msg) {
        this.sendMsg = tag + "\n" + msg;

    }

    public enum Tag{
        START_CHOOSING_FOLDER,
        FOLDER_BACK,
        FOLDER_NEXT,
        SELECT,
        CANCEL
    }

    public interface ClientListener{
        void changeListModel(List<String> dirs);
        void displayFolderChanges(String msg);
        void closeSelectFolderDialog();
        void setEnableChooseBtn(boolean enabled);
        void clientLeft();
        void clientLeftBeforeServerAction();
        void changeListIcon();
        void setClientFolderName(String folderName);
        void setFolderObservedLabel(String folderName);
    }
}
