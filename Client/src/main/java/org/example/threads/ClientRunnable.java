package org.example.threads;

import org.example.DirectoryNode;
import org.example.RootDirectories;

import javax.swing.*;
import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.*;

public class ClientRunnable implements Runnable {
    private final InetAddress address;
    private final int port;
    private final Socket clientSocket;

    private DataOutputStream outToServer;
    private DataInputStream inFromServer;

    public ClientRunnable(InetAddress address, int port) {
        this.address = address;
        this.port = port;
        this.clientSocket = new Socket();
    }

    @Override
    public void run() {
//        try (Socket clientSocket = new Socket()) {
//            clientSocket.connect(new InetSocketAddress(address, port));

        try {
            init();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Can not find server at port " + port,"Error", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
            throw new RuntimeException(e);
        }
        JOptionPane.showMessageDialog(null, "Successfully connect with server", "Notification", JOptionPane.INFORMATION_MESSAGE);
        //noinspection ConditionalBreakInInfiniteLoop
//        while (true) {
////                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
////                System.out.print("Client: ");
////                sentMsg = bufferedReader.readLine();
////                outToServer.writeUTF(sentMsg);
////                outToServer.flush();
////
////                if (sentMsg.equalsIgnoreCase("quit")) {
////                    break;
////                } else {
////                    receivedMsg = inFromSever.readUTF();
////                    System.out.println("Server: " + receivedMsg);
////                }
//            String dirPath = null;
////            try {
////                startFolderObserving(dirPath);
////            } catch (IOException e) {
////                throw new RuntimeException(e);
////            }
//        }
//            outToServer.close();
//            inFromServer.close();

        Thread observeFolderThread = null;
        RootDirectories rootDirectories = new RootDirectories();
        Stack<DirectoryNode> directoryNodeStack = new Stack<>();
        DirectoryNode current = null;
        ObservingFolder observingFolder = null;
        while(true){
            String msg = null;
            try {
                msg = inFromServer.readUTF();
            } catch (IOException e) {
                // Server left, client application force to close
                if(observingFolder != null){
                    observingFolder.setCancel(true);
                }
                System.out.println("Server left");
                System.exit(0);
                if(observeFolderThread != null){
                    observeFolderThread.interrupt();
                }
                break;
//                throw new RuntimeException(e);
            }
            System.out.println(msg);
            String[] tokens = msg.split("\n");
            if(tokens.length == 0){
                // lost msg
                continue;
            }

            String tag = tokens[0];
            if(tag.equals(Tag.START_CHOOSING_FOLDER.toString())){
                try {
                    outToServer.writeUTF(serializeDirsList(rootDirectories.getRootsName()));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            else if(tag.equals(Tag.FOLDER_NEXT.toString())){
                String folderName = tokens[1];
                System.out.println(current);
                if(current == null){
                    for(DirectoryNode root: rootDirectories.getRoots()){
                        if(root.getName().equals(folderName)){
                            current = root;
                        }
                    }
                }
                else{
                    directoryNodeStack.push(current);
                    for(DirectoryNode node: current.getChildren()){
                        if(node.getName().equals(folderName)){
                            current = node;
                        }
                    }
                }
                List<String> childrenName = new ArrayList<>();
                for(DirectoryNode child: current.getChildren()){
                    childrenName.add(child.getName());
                }
                try {
                    outToServer.writeUTF(serializeDirsList(childrenName));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            else if(tag.equals(Tag.FOLDER_BACK.toString())){
                if(!directoryNodeStack.empty()){
                    DirectoryNode node = directoryNodeStack.pop();
                    current = node;
                    List<String> childrenName = new ArrayList<>();
                    for(DirectoryNode child: current.getChildren()){
                        childrenName.add(child.getName());
                    }
                    try {
                        outToServer.writeUTF(serializeDirsList(childrenName));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                else{
                    current = null;
                    try {
                        outToServer.writeUTF(serializeDirsList(rootDirectories.getRootsName()));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            else if(tag.equals(Tag.CANCEL.toString())){
                current = null;
            }
            else if(tag.equals(Tag.SELECT.toString())){
                String folderName = tokens[1];
                try {
                    observingFolder = new ObservingFolder(folderName, new ObservingFolder.Listener() {
                        @Override
                        public void notifyServer(String str) {
                            try {
                                outToServer.writeUTF(str);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            try {
                                outToServer.flush();
                            } catch (IOException e) {
                                System.out.println("Seems like server is stopped");
                                throw new RuntimeException(e);
                            }
                        }
                    });
                } catch (IOException e) {
                    try {
                        outToServer.writeUTF("Inaccessible files or folders");
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                    continue;
//                        throw new RuntimeException(e);
                }
                try {
                    outToServer.writeUTF("OK\n" + folderName);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                observeFolderThread = new Thread(observingFolder);
                observeFolderThread.start();
            }
        }
    }

    private void init() throws IOException {
        clientSocket.connect(new InetSocketAddress(address, port));
        System.out.println("Connected to server, port: " + clientSocket.getPort());
        outToServer = new DataOutputStream(clientSocket.getOutputStream());
        inFromServer = new DataInputStream(clientSocket.getInputStream());
    }
    private void sendMessage(String msg) throws IOException {
        outToServer.writeUTF(msg);
        outToServer.flush();
    }
    private String serializeDirsList(List<String> dirs){
        StringBuilder builder = new StringBuilder();
//        String str = null;
        for(int i = 0; i < dirs.size(); i++){
            String str = dirs.get(i);
            builder.append(str);
            if(i != dirs.size()-1){
                builder.append("\n");
            }
        }
        return builder.toString();
    }


//    private void startFolderObserving(String dirPath) throws IOException {
//        ObservingFolder observingFolder = new ObservingFolder(dirPath, new ObservingFolder.Listener() {
//            @Override
//            public void notifyServer(String str) {
//                try {
//                    sendMessage(str);
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//            }
//        });
//        new Thread(observingFolder).start();
//    }

    private enum Tag{
        START_CHOOSING_FOLDER,
        FOLDER_BACK,
        FOLDER_NEXT,
        SELECT,
        CANCEL
    }
}
