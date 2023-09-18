package org.example.gui;

import javax.swing.*;
import java.awt.*;

//Not using class
public class ItemPanel extends JPanel {
    private final JButton imageButton = new HoverButton();
    private final JLabel jLabel = new JLabel("Select folder...");
    private final String label;
    public ItemPanel(String label) {
        this.label = label;
        this.setLayout(new BorderLayout());
        imageButton.setIcon(new ImageIcon());
        this.add(imageButton, BorderLayout.CENTER);
    }

    public void activatedClient(Image image){
        imageButton.setIcon(new ImageIcon(image));
    }

}
