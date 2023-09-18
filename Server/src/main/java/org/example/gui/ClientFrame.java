package org.example.gui;

import javax.swing.*;

import org.example.SVGIcon;
import org.example.models.Client;
import org.example.socket.WorkerRunnable;

import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class ClientFrame extends JFrame {
    private Client client;
    private ClientNotifications notifications;
    private LoggingPane loggingPane;
    private JTextField textField;
    private FolderChooser folderChooser;
    private JButton selectFolderBtn;
    private JLabel folderObservedLabel;
    private JButton chooseBtn;
    private ClientLeftListener listener;

    public void setClientLeftListener(ClientLeftListener listener) {
        this.listener = listener;
    }

    public ClientFrame(Client client/*, ClientNotifications notifications*/){
        this.client = client;
//        this.notifications = notifications;

        // adding ui
        this.setLayout(new GridBagLayout());

        addClientLogging();
        addSelectFolderButton();
        addFolderObservedLabel();
//        addTextField();

        client.getClientRunnable().setClientListener(new WorkerRunnable.ClientListener() {
            @Override
            public void changeListModel(List<String> dirs) {
                folderChooser.getModel().removeAllElements();
                folderChooser.getModel().addAll(dirs);
            }

            @Override
            public void displayFolderChanges(String msg) {
                loggingPane.insertStringInNewLine(msg);
            }

            @Override
            public void closeSelectFolderDialog() {
                folderChooser.dispose();
            }

            @Override
            public void setEnableChooseBtn(boolean enabled) {
                chooseBtn.setEnabled(enabled);
            }

            @Override
            public void clientLeft() {
                listener.notifyClientLeft(client);
            }
            @Override
            public void clientLeftBeforeServerAction() {
                listener.notifyClientLeft(client);
                ClientFrame:dispose();
            }

            @Override
            public void changeListIcon() {
                listener.changeListIcon();
            }

            @Override
            public void setClientFolderName(String folderName) {
                client.setFolderName(folderName);
            }

            @Override
            public void setFolderObservedLabel(String folderName) {
                folderObservedLabel.setText(folderName);
            }

        });

    }


    public Client getClient() {
        return client;
    }

    private void addClientLogging(){
//        loggingPane = new LoggingPane();
//        loggingPane.setPreferredSize(new Dimension(700, 450));
//        JScrollPane scrollPane = new JScrollPane(loggingPane);
//        scrollPane.add(loggingPane);
//        scrollPane.setMinimumSize(new Dimension(700,450));
//        GridBagConstraints gbc = new GridBagConstraints();
//        gbc.gridx=0;
//        gbc.gridy=1;
//        gbc.gridwidth=2;
//        gbc.weighty=2;
//        gbc.fill=GridBagConstraints.HORIZONTAL;
//        this.add(scrollPane, gbc);

        loggingPane = new LoggingPane();
        loggingPane.setPreferredSize(new Dimension(700, 450));
        JScrollPane jScrollPane = new JScrollPane(loggingPane);
        jScrollPane.setMinimumSize(new Dimension(700,450));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx=0;
        gbc.gridy=1;
        gbc.gridwidth=2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(10,0,10,0);
        this.add(jScrollPane, gbc);
    }

    private void addTextField(){
        textField = new JTextField(13);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx=0;
        gbc.gridy=0;
        gbc.weightx=1;
        gbc.fill=GridBagConstraints.HORIZONTAL;
        this.add(textField, gbc);
    }

    private void addFolderObservedLabel(){
        folderObservedLabel = new JLabel();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx=1;
        gbc.gridy=0;
        gbc.insets = new Insets(10, 0, 0, 0);
        gbc.weightx=3;
        gbc.anchor = GridBagConstraints.CENTER;
        this.add(folderObservedLabel, gbc);
    }

    private void addSelectFolderButton(){
        selectFolderBtn = new JButton("Select Folder");
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy=0;
        gbc.gridx=0;
        gbc.weightx=1;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(10, 0, 0, 0);
        this.add(selectFolderBtn, gbc);

        selectFolderBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(client.isClosed()){
                    return;
                }
                folderChooser = new FolderChooser();
                folderChooser.pack();
                folderChooser.setVisible(true);

//                client.getClientThread().setChooseBtnClicked(true);
                synchronized (client.getClientRunnable()) {
                    client.getClientRunnable().notifyAll();
                }
                selectFolderBtn.setEnabled(false);
            }
        });
    }

    public interface ClientNotifications {
        void startObserving(String id, String folder);
    }

    private class FolderChooser extends JFrame {
        private DefaultListModel<String> model;
        private JList foldersList;

        public DefaultListModel<String> getModel() {
            return model;
        }

        public FolderChooser() throws HeadlessException {
            this.setLayout(new GridBagLayout());
            this.addBackButton();
            this.addFoldersList();
            this.addChooseButton();
            this.addCancelButton();

            this.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    selectFolderBtn.setEnabled(true);
                    super.windowClosing(e);
                }
            });
        }

        private void addBackButton(){
            JButton backBtn = new JButton(new ImageIcon(SVGIcon.fetchImage("/arrow-back.svg").getScaledInstance(20,20,Image.SCALE_SMOOTH)));
            backBtn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    synchronized (client.getClientRunnable()){
                        client.getClientRunnable().sendMsg(WorkerRunnable.Tag.FOLDER_BACK.toString());
                        client.getClientRunnable().notifyAll();

                    }
                }
            });
            backBtn.setPreferredSize(new Dimension(30,30));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx=0;
            gbc.gridy=0;
            gbc.anchor = GridBagConstraints.FIRST_LINE_START;
            gbc.insets = new Insets(10,  0, 10, 0);
            this.add(backBtn, gbc);
        }

        private void addFoldersList(){
            model = new DefaultListModel<>();
            foldersList = new JList(model);
//            foldersList.setPreferredSize(new Dimension(400,0));
            foldersList.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if(e.getClickCount() == 2){
                        int index = foldersList.getSelectedIndex();
                        String value = (String) foldersList.getModel().getElementAt(index);
                        System.out.println(value);
                        client.getClientRunnable().sendMsg(WorkerRunnable.Tag.FOLDER_NEXT.toString(), value);
                        synchronized (client.getClientRunnable()) {
                            client.getClientRunnable().notifyAll();
                        }
//                        System.out.println(2);
                    }
//                    super.mouseClicked(e);
                }
            });
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx=0;
            gbc.gridy=1;
            gbc.fill=GridBagConstraints.BOTH;
            gbc.gridwidth=2;
//        gbc.weightx=1;
            gbc.insets = new Insets(10,  0, 10, 0);
            JScrollPane scrollPane = new JScrollPane(foldersList);
            scrollPane.setPreferredSize(new Dimension(400,400));
            scrollPane.setMinimumSize(new Dimension(400,400));
            this.add(scrollPane, gbc);
        }

        private void addChooseButton(){
            chooseBtn = new JButton("Choose");
            chooseBtn.addActionListener(e -> {
//                selectFolderBtn.setEnabled(true);
                int index = foldersList.getSelectedIndex();
                String value = (String) foldersList.getModel().getElementAt(index);
                synchronized (client.getClientRunnable()) {
                    client.getClientRunnable().sendMsg(WorkerRunnable.Tag.SELECT.toString(), value);
                    client.getClientRunnable().notifyAll();
                }
//                this.dispose();
            });
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx=0;
            gbc.gridy=2;
            gbc.weightx=1;
//        gbc.anchor = GridBagConstraints.EAST;
            gbc.insets = new Insets(10,0,10,20);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            this.add(chooseBtn, gbc);
        }

        private void addCancelButton(){
            JButton cancelButton = new JButton("Cancel");
            cancelButton.addActionListener((e)->{
                selectFolderBtn.setEnabled(true);
                synchronized (client.getClientRunnable()) {
                    client.getClientRunnable().sendMsg(WorkerRunnable.Tag.CANCEL.toString());
                    client.getClientRunnable().notifyAll();
                }
                this.dispose();
            });
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx=1;
            gbc.gridy=2;
            gbc.weightx=1;
//        gbc.anchor = GridBagConstraints.EAST;
            gbc.insets = new Insets(10,20,10,0);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            this.add(cancelButton, gbc);
        }
    }

    public interface ClientLeftListener{
        void notifyClientLeft(Client client);
        void changeListIcon();
    }
}
