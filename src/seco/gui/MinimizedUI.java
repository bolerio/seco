/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * MinimizedUI.java
 *
 * Created on 2009-9-8, 14:58:15
 */

package seco.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

import seco.things.CellGroupMember;
import seco.things.CellUtils;

/**
 * Panel representing a PSwingNode in minimized state
 */
public class MinimizedUI extends javax.swing.JPanel
{
    private JLabel imageLabel;
    private JTextArea textArea;
    private CellGroupMember cgm;
    
    /** Creates new form MinimizedUI */
    public MinimizedUI(final CellGroupMember cgm)
    {
        this.cgm = cgm;
        initComponents();
    }
    
    public void setTitle(String title)
    {
        textArea.setText(title);
    }
    
    private void initComponents()
    {
        imageLabel = new JLabel();
        textArea = new JTextArea();

        setBackground(new Color(255, 255, 255));
        setPreferredSize(new Dimension(65, 83));
        setLayout(new BorderLayout());

        imageLabel.setBackground(new Color(255, 255, 255));
        imageLabel.setIcon(new ImageIcon(getClass().getResource(
                "/seco/notebook/images/notebook.png"))); // NOI18N
        imageLabel.setMaximumSize(new Dimension(65, 57));
        imageLabel.setMinimumSize(new Dimension(65, 57));
        imageLabel.setPreferredSize(new Dimension(65, 57));
        imageLabel.setVerticalTextPosition(SwingConstants.BOTTOM);
        add(imageLabel, java.awt.BorderLayout.NORTH);

        textArea.setBackground(new Color(255, 255, 255));
        textArea.setFont(new java.awt.Font("Tahoma", Font.BOLD, 12));
        textArea.setColumns(10);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);
        textArea.setRows(2);
        textArea.setWrapStyleWord(true);
        textArea.setAlignmentX(0.0F);
        textArea.setAlignmentY(0.0F);
        textArea.setAutoscrolls(false);
        textArea.setBorder(null);
        textArea.setPreferredSize(new Dimension(65, 25));
        add(textArea, java.awt.BorderLayout.PAGE_END);
        
        MouseListener listener = new MouseAdapter(){
            @Override
            public void mouseClicked(MouseEvent e)
            {
                if(e.getClickCount() == 2)
                    CellUtils.toggleMinimized(cgm);
            }};
        imageLabel.addMouseListener(listener);  
        textArea.addMouseListener(listener);
        String text = CellUtils.getName(cgm);
        if (text == null) text = "Untitled";
        textArea.setText(text);
    }
        

    //TESTING METHODS...
    public static void main(String[] args)
    {
        JFrame frame = new JFrame("Main");
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e)
            {
                Window win = e.getWindow();
                win.setVisible(false);
                win.dispose();
                System.exit(0);
            }
        });

        frame.getContentPane().setLayout(new FlowLayout());

        MinimizedUI l = new MinimizedUI(
                "VeryStrange And Long TextHEREANDTHERE");
        frame.getContentPane().add(l);
        frame.pack();
        frame.setVisible(true);
    }
    
    private MinimizedUI(final String title)
    {
        initComponents();
        textArea.setText(title);
    }
}



