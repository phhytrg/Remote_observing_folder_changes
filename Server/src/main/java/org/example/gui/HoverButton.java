package org.example.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

//Not using class
public class HoverButton extends JButton {
    public HoverButton() {
        addHoverEffect();
    }

    public HoverButton(Icon icon) {
        super(icon);
        addHoverEffect();
    }

    public HoverButton(String text) {
        super(text);
        addHoverEffect();
    }

    public HoverButton(Action a) {
        super(a);
        addHoverEffect();
    }

    public HoverButton(String text, Icon icon) {
        super(text, icon);
        addHoverEffect();
    }

    private void addHoverEffect(){
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                super.mouseEntered(e);
                setBackground(Color.GREEN);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                super.mouseExited(e);
                setBackground(UIManager.getColor("control"));
            }
        });
    }
}
