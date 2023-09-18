package org.example;

import org.example.threads.ClientRunnable;
import org.example.threads.ObservingFolder;

import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
//        ObservingFolder observingFolder = null;
//        try {
//            observingFolder = new ObservingFolder("D:\\HK2-2223\\KTNN\\a");
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//        new Thread(observingFolder).start();

        Thread clientThread = null;
        try {
            clientThread = new Thread(new ClientRunnable(InetAddress.getLocalHost(), 1234));
            clientThread.start();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }

        if (SystemTray.isSupported()) {
            SystemTray tray = SystemTray.getSystemTray();
            Image image = Toolkit.getDefaultToolkit().getImage("path/to/icon.png");

            PopupMenu popup = new PopupMenu();
            MenuItem exitItem = new MenuItem("Exit");
            Thread finalClientThread = clientThread;
            exitItem.addActionListener(e -> {
                finalClientThread.interrupt();
                System.exit(0);
            });
            popup.add(exitItem);

            TrayIcon trayIcon = new TrayIcon(image, "My App", popup);
            trayIcon.setImageAutoSize(true);
            try {
                tray.add(trayIcon);
            } catch (AWTException e) {
                throw new RuntimeException(e);
            }
        }


//        DirectoryNode directoryNode = DirectoryNode.iterateDir("D:\\HK2-2223\\PTDLTM");
//        System.out.println(directoryNode.serialize());
//        DirectoryNode node = DirectoryNode.deserialize(directoryNode.serialize());
//        for(File file: File.listRoots()){
//            System.out.println(file.getAbsolutePath());
//        }
//        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
//
//        String dir = reader.readLine();
//        List<DirectoryNode> nodes = DirectoryNode.getSubDirectory(dir);

//        RootDirectories rootDirectories = new RootDirectories();
//        for(String str: rootDirectories.getRootsName()){
//            System.out.println(str);
//        }
    }
}