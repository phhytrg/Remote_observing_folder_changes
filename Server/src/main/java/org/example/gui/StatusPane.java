package org.example.gui;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class StatusPane extends JPanel {

    private JLabel pIdLabel;
    private JLabel stateLabel;
    private JLabel portLabel;
    private int port;
    public StatusPane(int port) {
        this.port = port;
        this.setLayout(new GridBagLayout());
        pIdLabel = new JLabel("PID: ");
        stateLabel = new JLabel("Status: ");
        portLabel = new JLabel("Port: " + port);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.FIRST_LINE_START;
        gbc.gridy = 0;
        gbc.gridx = 0;
        this.add(stateLabel, gbc);
        gbc.gridy = 1;
        this.add(portLabel, gbc);
        gbc.gridy = 2;
        this.add(pIdLabel, gbc);

        this.setPreferredSize(new Dimension(150,200));
    }

    private int findPId(){
        String cmd = "netstat -a -n -o | findstr :"+port;
        ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", cmd);
        Process process = null;
        int pId = -1;
        try {
            process = builder.start();
            process.waitFor();

            BufferedReader stream = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String res = stream.readLine();
            String[] parts = res.split("\\s+");
            pId = Integer.parseInt(parts[parts.length - 1]);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        return pId;
    }
    public void updateStatus(){
    }

    public void setShowStatus(boolean enabled){
        this.portLabel.setEnabled(enabled);
        this.pIdLabel.setEnabled(enabled);
        if(enabled){
            this.portLabel.setText("Port: "+ port);
            this.stateLabel.setText("Status: Running");
            this.pIdLabel.setText("PID: " + this.findPId());
        }
        else{
            this.stateLabel.setText("Status: Close");
        }
    }
}
