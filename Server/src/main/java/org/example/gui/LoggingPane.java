package org.example.gui;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class LoggingPane extends JTextPane {
    private final StyledDocument doc;
    private final Style blueStyle;
    public LoggingPane() {
        this.setEditable(false);
        doc = this.getStyledDocument();

        blueStyle = this.addStyle("BlueStyle", null);;
        StyleConstants.setForeground(blueStyle, Color.BLUE);
        this.setDocument(doc);
    }

    synchronized public void insertStringInNewLine(String str){

        String timeStamp = new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime());

        try {
            doc.insertString(doc.getLength(), timeStamp + "\t\t" , blueStyle);
            doc.insertString(doc.getLength(), str + "\n", null);
        } catch (BadLocationException e) {
            throw new RuntimeException(e);
        }

    }
}
