package org.example.models;

import org.example.socket.WorkerRunnable;

import java.util.Calendar;
import java.util.Date;

public class Client {
    private static int count = 0;
    private final int id;
    private final Date joinTime;

    private boolean isClosed = false;
    private Thread clientThread;
    private WorkerRunnable clientRunnable;
    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }
    private String folderName;
    public void setClosed(boolean closed) {
        isClosed = closed;
    }
    public boolean isClosed() {
        return isClosed;
    }
    public WorkerRunnable getClientRunnable() {
        return clientRunnable;
    }
    public Client(WorkerRunnable clientRunnable) {
        this.id = count++;
        this.joinTime = Calendar.getInstance().getTime();
        this.clientRunnable = clientRunnable;
        this.clientThread = new Thread(clientThread);
    }
//    public static List<Client> getClientList() {
//        return clientList;
//    }
    public int getId() {
        return id;
    }
    public Date getJoinTime() {
        return joinTime;
    }
    public String getFolderName() {
        return folderName;
    }
    public Thread getClientThread() {
        return clientThread;
    }
    public static void resetId(){
        count = 0;
    }
}
