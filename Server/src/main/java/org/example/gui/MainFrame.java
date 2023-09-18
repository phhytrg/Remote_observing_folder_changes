package org.example.gui;

import org.example.Constants;
import org.example.SVGIcon;
import org.example.models.Client;
import org.example.socket.ThreadPoolServer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class MainFrame extends JFrame {
    private ThreadPoolServer threadPoolServer;
    private StatusPane statusPane;
    private JButton onOffButton;
    private LoggingPane loggingPane;
    private DynamicList dynamicList;
    private TreeMap<Integer, ClientFrame> clientFrameMap = new TreeMap<>();
    private DefaultListModel<Client> listModel;

    public MainFrame() throws HeadlessException {
        threadPoolServer = new ThreadPoolServer(1234, 10, new ThreadPoolServer.ServerNotificationListener() {
            @Override
            public void loggingForServerState() {
                if(threadPoolServer.isStopped()){
                    onOffButton.setEnabled(true);
                    onOffButton.setText("Start");
                    statusPane.setShowStatus(false);
                    MainFrame:appendLogging("Close all connection at port " + threadPoolServer.getPort());
                    MainFrame:appendLogging("Server stopped");

                    //Close socket :)
                }
                else{
                    onOffButton.setEnabled(true);
                    onOffButton.setText("Stop");
                    statusPane.setShowStatus(true);
                    MainFrame:appendLogging("Server is running at port " + threadPoolServer.getPort());
                    MainFrame:appendLogging("Listen for connection..");
                }
            }

            @Override
            public void newClientConnected(Client client) {
                dynamicList.addNewClient(client);
                ClientFrame clientFrame = clientFrameMap.get(client.getId());
                if(clientFrame == null){
                    clientFrame = new ClientFrame(client);
                    clientFrame.setClientLeftListener(new ClientFrame.ClientLeftListener() {
                        @Override
                        public void notifyClientLeft(Client client) {
                            listModel.removeElement(client);
                            clientFrameMap.remove(client.getId());
                            MainFrame:appendLogging("Client " + client.getId() +" left");
                        }

                        @Override
                        public void changeListIcon() {
                            repaint();
                        }
                    });
                    clientFrameMap.put(client.getId(), clientFrame);
                }
                MainFrame:appendLogging("New client has connected " + client.getId());
            }
        });

        this.setLayout(new GridBagLayout());
        this.addOnOffButton();
        this.addClientsList();
        this.addServerLogging();
        this.addServerStatus();

    }

    private void addClientsList(){
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx=1;
        gbc.weighty=1;
        gbc.gridheight=2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(0,0,0,0);
        dynamicList = new DynamicList();
        dynamicList.addClientNotification(new ClientFrame.ClientNotifications() {
            @Override
            public void startObserving(String id, String folder) {
                MainFrame:appendLogging("Client " + id + " start observing folder: " + folder);
            }
        });
        this.add(dynamicList, gbc);
    }

    private void addOnOffButton(){
        onOffButton = new JButton("Start");

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.PAGE_START;
        gbc.insets = new Insets(10,10,10,10);
        this.add(onOffButton, gbc);

        onOffButton.addActionListener(new ActionListener() {
            Thread thread;
            @Override
            public void actionPerformed(ActionEvent e) {
                if(threadPoolServer.isStopped()){
                    onOffButton.setEnabled(false);
                    thread = new Thread(threadPoolServer);
                    thread.start();
                }
                else{
                    try {
                        onOffButton.setEnabled(false);
                        threadPoolServer.stop();
                        for(ClientFrame frame: clientFrameMap.values()){
                            frame.getClient().getClientThread().interrupt();
//                            frame.getClient().getClientRunnable().setCancel(true);
                            frame.getClient().getClientRunnable().getInFromClient().close();
                            synchronized (frame.getClient().getClientRunnable()) {
                                frame.getClient().getClientRunnable().notifyAll();
                            }
                        }
                        thread.interrupt();
                        clientFrameMap.clear();
                        Client.resetId();
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        });
    }

    private void addServerStatus(){
        statusPane = new StatusPane(Constants.PORT);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx=1;
//        JButton button = new JButton(new ImageIcon(SVGIcon.fetchImage(getClass().getResourceAsStream("resources/setting-computer.svg"))));
//        JButton button = new JButton(new ImageIcon(SVGIcon.fetchImage("src/main/resources/setting-computer.svg")));
        this.add(statusPane, gbc);
    }

    private void addServerLogging(){
        loggingPane = new LoggingPane();
        loggingPane.setPreferredSize(new Dimension(700,250));
        GridBagConstraints cons = new GridBagConstraints();
        cons.gridx = 0;
        cons.gridy = 2;
        cons.gridwidth = 2;
        cons.fill = GridBagConstraints.HORIZONTAL;

//        loggingArea.setMinimumSize(new Dimension(900,250));
        JScrollPane scrollPane = new JScrollPane(loggingPane);
        scrollPane.setMinimumSize(new Dimension(700,250));
        this.add(scrollPane, cons);
    }

    private void appendLogging(String msg){
        loggingPane.insertStringInNewLine(msg);
    }
//    SVGIcon svgIcon = new SVGIcon("src/main/resources/setting-computer.svg");
//    Image newing = svgIcon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
//    JButton onOffButton = new JButton("Start", new ImageIcon(newing));

    private class DynamicList extends JPanel {
        public static int UN_CHOSEN_FOLDER_CLIENT = 0;
        public static int CHOSEN_FOLDER_CLIENT =1;
        private JList<Client> list;
        private Map<Integer, String> imagePaths;
        private Map<String, ImageIcon> iconCache = new HashMap<>();
//        private ClientFrame.ClientNotifications clientListener;

        public DynamicList() {
            super(new BorderLayout());
            listModel = new DefaultListModel<>();
            list = new JList<>(listModel);
            list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

            imagePaths = new TreeMap<>();
            imagePaths.put(UN_CHOSEN_FOLDER_CLIENT, "src/main/resources/setting-computer.svg");
            imagePaths.put(CHOSEN_FOLDER_CLIENT, "src/main/resources/new-computer.svg");

            list.setCellRenderer(new ImageListCellRenderer());
            list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
            list.setVisibleRowCount(-1);

            JScrollPane scrollPane = new JScrollPane(list, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            add(scrollPane, BorderLayout.CENTER);
            this.setPreferredSize(new Dimension(705, 450));
            this.setMinimumSize(new Dimension(705, 450));

//        this.setPreferredSize(new Dimension(600,600));

            list.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if(e.getClickCount() == 2){
                        int index = list.locationToIndex(e.getPoint());
                        Client client = list.getModel().getElementAt(index);
                        ClientFrame clientFrame = clientFrameMap.get(client.getId());
                        clientFrame.pack();
                        clientFrame.setVisible(true);
                        System.out.println(client);
                    }
                }
            });
        }

        public void addNewClient(Client client){
            listModel.addElement(client);
        }

        public void removeClient(Client client){
            listModel.removeElement(client);
        }

        public void addClientNotification(ClientFrame.ClientNotifications listener){

        }

        private ImageIcon loadIcon(String path){
            ImageIcon icon = iconCache.get(path);
            if(icon == null){
                icon = new ImageIcon(SVGIcon.fetchImage(path).getScaledInstance(80,80,Image.SCALE_SMOOTH));
                iconCache.put(path, icon);
            }
            return icon;
        }

        private class ImageListCellRenderer extends JPanel implements ListCellRenderer<Client> {
            private JLabel textLabel;
            private JLabel imageLabel;

            public ImageListCellRenderer() {
                super(new BorderLayout());
//            this.setPreferredSize(new Dimension(80, 80));
                this.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
                textLabel = new JLabel();
                textLabel.setHorizontalAlignment(SwingConstants.CENTER);
                imageLabel = new JLabel();
                imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
                this.add(textLabel, BorderLayout.SOUTH);
                this.add(imageLabel, BorderLayout.CENTER);
            }

            @Override
            public Component getListCellRendererComponent(JList<? extends Client> list, Client value, int index, boolean isSelected, boolean cellHasFocus) {

                textLabel.setText("Client " + value.getId());
                if(value.getFolderName() == null){
                    imageLabel.setIcon(loadIcon("/setting-computer.svg"));
                }
                else{
                    imageLabel.setIcon(loadIcon("/new-computer.svg"));
                }

                if(isSelected){
                    this.setBackground(list.getSelectionBackground());
                }
                else{
                    this.setBackground(UIManager.getColor("List.background"));
                }
                return this;
            }
        }
    }
}
