/*
 * @(#)JColorWheel.java
 *
 * Copyright (c) 2005-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package ch.randelshofer.quaqua.colorchooser;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

/**
 * The JColorWheel displays a hue/saturation wheel of the HSB color model.
 * The user can click at the wheel to pick a color on the JColorWheel.
 * The JColorWheel should be used together with a HSB brightness color slider.
 *
 * @author  Werner Randelshofer
 * @version $Id: JColorWheel.java 462 2014-03-22 09:23:12Z wrandelshofer $
 */
public class JColorWheel extends JPanel {

    protected Insets wheelInsets;
    private Image colorWheelImage;
    protected ColorWheelImageProducer colorWheelProducer;
    protected HSBColorSliderModel model;

    private class MouseHandler implements MouseListener, MouseMotionListener {

        public void mouseClicked(MouseEvent e) {
        }

        public void mouseDragged(MouseEvent e) {
            update(e);
        }

        public void mouseEntered(MouseEvent e) {
        }

        public void mouseExited(MouseEvent e) {
        }

        public void mouseMoved(MouseEvent e) {
        }

        public void mousePressed(MouseEvent e) {
        }

        public void mouseReleased(MouseEvent e) {
            update(e);
        }

        private void update(MouseEvent e) {
            float[] hsb = getColorAt(e.getX(), e.getY());
            model.setValue(0, (int) (360 * hsb[0]));
            model.setValue(1, (int) (100 * hsb[1]));

            // FIXME - We should only repaint the damaged area
            repaint();
        }
    }
    private MouseHandler mouseHandler;

    private class ModelHandler implements ChangeListener {

        public void stateChanged(ChangeEvent e) {
            repaint();
        }
    }
    private ModelHandler modelHandler;

    /**
     * Creates a new instance.
     */
    public JColorWheel() {
        wheelInsets = new Insets(0, 0, 0, 0);
        model = new HSBColorSliderModel();
        initComponents();
        colorWheelProducer = createWheelProducer(0, 0);
        modelHandler = new ModelHandler();
        model.addChangeListener(modelHandler);
        installMouseListeners();
        setOpaque(false);
    }

    protected void installMouseListeners() {
        mouseHandler = new MouseHandler();
        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);
    }

    public void setModel(HSBColorSliderModel m) {
        if (model != null) {
            model.removeChangeListener(modelHandler);
        }
        model = m;
        if (model != null) {
            model.addChangeListener(modelHandler);
            repaint();
        }
    }

    public void setWheelInsets(Insets newValue) {
        wheelInsets = newValue;
        repaint();
    }

    public Insets getWheelInsets() {
        return wheelInsets;
    }

    public Dimension getPreferredSize() {
        return new Dimension(100, 100);
    }

    public HSBColorSliderModel getModel() {
        return model;
    }

    public void paintComponent(Graphics gr) {
        Graphics2D g = (Graphics2D) gr;
        paintWheel(g);
        paintThumb(g);
    }

    protected ColorWheelImageProducer createWheelProducer(int w, int h) {
        return new ColorWheelImageProducer(w, h);
    }
    
    protected void paintWheel(Graphics2D g) {
        int w = getWidth() - wheelInsets.left - wheelInsets.right;
        int h = getHeight() - wheelInsets.top - wheelInsets.bottom;

        if (colorWheelImage == null || colorWheelImage.getWidth(this) != w || colorWheelImage.getHeight(this) != h) {
            if (colorWheelImage != null) {
                colorWheelImage.flush();
            }
            colorWheelProducer = createWheelProducer(w, h);
            colorWheelImage = createImage(colorWheelProducer);
        }

        colorWheelProducer.setBrightness(model.getValue(2) / 100f);
        colorWheelProducer.regenerateColorWheel();

        g.drawImage(colorWheelImage, wheelInsets.left, wheelInsets.top, this);
    }

    protected void paintThumb(Graphics2D g) {
        Point p = getThumbLocation();

        g.setColor(Color.white);
        g.fillRect(p.x - 1, p.y - 1, 2, 2);
        g.setColor(Color.black);
        g.drawRect(p.x - 2, p.y - 2, 3, 3);
    }

    protected Point getThumbLocation() {
        return getColorLocation(
                model.getValue(0) / 360f,
                model.getValue(1) / 100f,
                model.getValue(2) / 100f);
    }
    protected Point getCenter() {
        int w = getWidth() - wheelInsets.left - wheelInsets.right;
        int h = getHeight() - wheelInsets.top - wheelInsets.bottom;

        return new Point(
                wheelInsets.left + w / 2,
                wheelInsets.top + h / 2);
    }

    protected Point getColorLocation(Color c) {
        Point p = colorWheelProducer.getColorLocation(c, 
                getWidth() - wheelInsets.left - wheelInsets.right,
                getHeight() - wheelInsets.top - wheelInsets.bottom);
        p.x += wheelInsets.left;
        p.y += wheelInsets.top;
        return p;
    }

    protected Point getColorLocation(float hue, float saturation, float brightness) {
        Point p = colorWheelProducer.getColorLocation(hue, saturation, brightness, 
                getWidth() - wheelInsets.left - wheelInsets.right,
                getHeight() - wheelInsets.top - wheelInsets.bottom);
        p.x += wheelInsets.left;
        p.y += wheelInsets.top;
        return p;
    }

    protected float[] getColorAt(int x, int y) {
        return colorWheelProducer.getColorAt(x - wheelInsets.left, y - wheelInsets.top,
                getWidth() - wheelInsets.left - wheelInsets.right,
                getHeight() - wheelInsets.top - wheelInsets.bottom);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(new java.awt.BorderLayout());
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
