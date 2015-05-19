/*
 * @(#)OSXPainterTest.java  1.0  2011-07-26
 * 
 * Copyright (c) 2011-2013 Werner Randelshofer, Switzerland.
 * All rights reserved.
 * 
 * You may not use, copy or modify this file, except in compliance with the
 * license agreement you entered into with Werner Randelshofer.
 * For details see accompanying license terms.
 */
package test;

import ch.randelshofer.quaqua.osx.OSXAquaPainter;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.DataBufferInt;
import java.awt.image.Kernel;
import java.util.Arrays;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import static java.lang.Math.*;

/**
 * OSXPainterTest.
 *
 * @author Werner Randelshofer
 * @version 1.0 2011-07-26 Created.
 */
public class OSXPainterTest extends javax.swing.JPanel {

    private static class Canvas extends JPanel {

        private BufferedImage image;
        private OSXAquaPainter painter;

        public Canvas() {
            System.out.println("OSXPainterTest nativeCodeAvailable=" + OSXAquaPainter.isNativeCodeAvailable());
            painter = new OSXAquaPainter();
            painter.setWidget(OSXAquaPainter.Widget.buttonCheckBox);
            painter.setValueByKey(OSXAquaPainter.Key.value,1.0);
            painter.setValueByKey(OSXAquaPainter.Key.focused,1.0);
        }

        static float[] gaussian(float radius, float s,float sum) {
            int r = (int) Math.ceil(radius);
            float[] gaussian = new float[r * 2 + 1];

            // compute the gaussian
            float h = 1f; // height of the peak 
            float c = r; // position of the centre of the peak 
            //float s = radius/1.5f; // width of the 'bell'
            float invs2sq = 1f/(2f * s * s);
            for (int i = 0; i < gaussian.length; i++) {
                float x = i;
                gaussian[i] = (float) (h * exp(-pow(x - c, 2) * invs2sq));
            }

            normalizeKernel(gaussian,sum);
System.out.println("g="+Arrays.toString(gaussian));
            return gaussian;
        }
        static float[] pyramid(float radius, float sum) {
            int r = (int) Math.ceil(radius);
            float[] gaussian = new float[r * 2 + 1];

            // compute the pyramid
            float c = r; // position of the centre of the peak 
            
            for (int i = 0; i < gaussian.length; i++) {
                float x = i;
                gaussian[i] = (float) c-abs(x-c);
            }

            normalizeKernel(gaussian,sum);
System.out.println("p="+Arrays.toString(gaussian));
            return gaussian;
        }

        /** Normalizes the kernel so that all its elements add up to the given
         * sum. 
         * 
         * @param kernel
         * @param sum 
         */
        static void normalizeKernel(float[] kernel, float sum) {
            float total = 0;
            for (int i = 0; i < kernel.length; i++) {
                total += kernel[i];
            }
            if (abs(total) > 1e-20) {
                total = sum / total;
                for (int i = 0; i < kernel.length; i++) {
                    kernel[i] *= total;
                }
            }

        }

        @Override
        public void paintComponent(Graphics grr) {
            Graphics2D gr = (Graphics2D) grr;
            int w = getWidth(), h = getHeight();
            if (image == null || image.getWidth() != w || image.getHeight() != h) {
                image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB_PRE);
            }

            int[] data = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
            //painter.paint(data, image.getWidth(), image.getHeight(), 0, 0, 40, 40);
            painter.paint(image, 6, 6, 32, 20);
            gr.drawImage(image, 0, 0, this);
            gr.drawImage(image, 40, 0, this);
            gr.drawImage(image, 80, 0, this);
            gr.drawImage(image, 120, 0, this);

            //
            float intensity = 1.8f;
            int blur = 7;
            final float blurry = intensity / (blur * blur);
            final float[] blurKernel = new float[blur * blur];
            for (int i = 0; i < blurKernel.length; i++) {
                blurKernel[i] = blurry;
            }
            ConvolveOp blurOp = new ConvolveOp(new Kernel(blur, blur, blurKernel));

            // 
            ConvolveOp sobelTL = new ConvolveOp(new Kernel(3, 3, (new float[]{2, 1, 0, 1, 0, -1, 0, -1, -2})));
            ConvolveOp sobelBR = new ConvolveOp(new Kernel(3, 3, (new float[]{-2, -1, 0, -1, 0, 1, 0, 1, 2})));
/*
            ConvolveOp edgeLeftOp = new ConvolveOp(new Kernel(2, 1, new float[]{1, -1}));
            ConvolveOp edgeRightOp = new ConvolveOp(new Kernel(2, 1, new float[]{-1, 1}));
            ConvolveOp edgeTopOp = new ConvolveOp(new Kernel(1, 2, new float[]{1, -1}));
            ConvolveOp edgeBottomOp = new ConvolveOp(new Kernel(1, 2, new float[]{-1, 1}));
            */
            ConvolveOp edgeLeftOp = new ConvolveOp(new Kernel(3, 1, new float[]{1, 0,-1}));
            ConvolveOp edgeRightOp = new ConvolveOp(new Kernel(3, 1, new float[]{-1, 0,1}));
            ConvolveOp edgeTopOp = new ConvolveOp(new Kernel(1, 3, new float[]{1, 0,-1}));
            ConvolveOp edgeBottomOp = new ConvolveOp(new Kernel(1, 3, new float[]{-1,0, 1}));
            /*
            float[] edgy=new float[]{0.5f,1, 0,-1,-0.5f};
            float[] medgy=new float[edgy.length];
            for (int i=0;i<edgy.length;i++)medgy[i]=-edgy[i];
            ConvolveOp edgeLeftOp = new ConvolveOp(new Kernel(edgy.length, 1, edgy));
            ConvolveOp edgeRightOp = new ConvolveOp(new Kernel(edgy.length, 1, medgy));
            ConvolveOp edgeTopOp = new ConvolveOp(new Kernel(1, edgy.length, edgy));
            ConvolveOp edgeBottomOp = new ConvolveOp(new Kernel(1, edgy.length, medgy));
*/
            float[] gaussian=gaussian(2.0f,2.5f/2.25f,0.9f);
            ConvolveOp gaussianOpV=new ConvolveOp(new Kernel(1,gaussian.length,gaussian));
            ConvolveOp gaussianOpH=new ConvolveOp(new Kernel(gaussian.length,1,gaussian));
            float[] pyramid=pyramid(2.5f,0.8f);
            ConvolveOp pyramidOpV=new ConvolveOp(new Kernel(1,pyramid.length,pyramid));
            ConvolveOp pyramidOpH=new ConvolveOp(new Kernel(pyramid.length,1,pyramid));
            
            // blur the prior image back into the same pixels
            Graphics2D g;
            //imgG = (Graphics2D)image.getGraphics();

            // clear 
            BufferedImage focusImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB_PRE);
            g = focusImg.createGraphics();
            g.setComposite(AlphaComposite.Src);
            g.setColor(new Color(0, true));
            g.fillRect(0, 0, w, h);
            //g.setColor(UIManager.getColor("Focus.color"));
            //g.drawRect(4,4, 32, 32);


            //g.setComposite(AlphaComposite.DstAtop);
            //g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            //g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
            //g.drawImage(image, blurOp, 40, 0);

            g.setComposite(AlphaComposite.SrcOver);
            g.drawImage(image, edgeLeftOp, 0, 0);
            g.drawImage(image, edgeRightOp, 0, 0);
            g.drawImage(image, edgeTopOp, 0, 0);
            g.drawImage(image, edgeBottomOp, 0, 0);
            g.setComposite(AlphaComposite.SrcIn);
            g.setColor(UIManager.getColor("Focus.color"));
            g.fillRect(0, 0, w, h);
            //
            g.dispose();
            gr.drawImage(focusImg, 40, 0,null);
            gr.drawImage(focusImg, gaussianOpH, 80, 0);
            gr.drawImage(focusImg, gaussianOpV, 80, 0);
            gr.drawImage(focusImg, pyramidOpH, 120, 0);
            gr.drawImage(focusImg, pyramidOpV, 120, 0);
        }
    }
    private Canvas canvas;

    /** Creates new form OSXPainterTest */
    public OSXPainterTest() {
        initComponents();
        add(canvas = new Canvas());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                JFrame f = new JFrame("OSXPainterTest");
                f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                f.add(new OSXPainterTest());
                f.setSize(400, 400);
                f.setVisible(true);
            }
        });
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(new java.awt.BorderLayout());
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
