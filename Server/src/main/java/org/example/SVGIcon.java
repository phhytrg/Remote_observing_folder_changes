package org.example;

import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.batik.util.XMLResourceDescriptor;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class SVGIcon extends ImageIcon {

    private final BufferedImage image;

    public SVGIcon(String path) {
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(path);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        TranscoderInput transcoderInput = new TranscoderInput(inputStream);

        PNGTranscoder pngTranscoder = new PNGTranscoder();
        pngTranscoder.addTranscodingHint(ImageTranscoder.KEY_WIDTH, (float) 1000.0);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        TranscoderOutput transcoderOutput = new TranscoderOutput(outputStream);

        try {
            pngTranscoder.transcode(transcoderInput, transcoderOutput);
            image = ImageIO.read(new ByteArrayInputStream(outputStream.toByteArray()));
        } catch (TranscoderException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void paintIcon(Component c, @NotNull Graphics g, int x, int y) {
        g.drawImage(image, x, y, null);
    }

    @Override
    public int getIconWidth() {
        return image.getWidth();
    }

    @Override
    public int getIconHeight() {
        return image.getHeight();
    }

    @Override
    public Image getImage() {
        return image;
    }

    public static Image fetchImage(String path){
        InputStream inputStream = null;
        inputStream = SVGIcon.class.getResourceAsStream(path);
        //        InputStream inputStream = null;
//        try {
//            inputStream = new FileInputStream(path);
//        } catch (FileNotFoundException e) {
//            throw new RuntimeException(e);
//        }

        TranscoderInput transcoderInput = new TranscoderInput(inputStream);

        PNGTranscoder pngTranscoder = new PNGTranscoder();
        pngTranscoder.addTranscodingHint(ImageTranscoder.KEY_WIDTH, (float) 1000.0);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        TranscoderOutput transcoderOutput = new TranscoderOutput(outputStream);

        BufferedImage targetImage;
        try {
            pngTranscoder.transcode(transcoderInput, transcoderOutput);
            targetImage = ImageIO.read(new ByteArrayInputStream(outputStream.toByteArray()));
            return targetImage;
        } catch (TranscoderException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
