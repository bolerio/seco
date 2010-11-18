/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * TestPanel.java
 *
 * Created on 2009-9-9, 3:50:25
 */

package seco.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import seco.gui.visual.CellContainerVisual;
import seco.gui.visual.TabbedPaneVisual;
import seco.things.CellGroupMember;
import seco.things.CellUtils;
import seco.util.IconManager;

/**
 * Panel representing a PSwingNode in minimized state
 */
public class MinimizedUI extends JPanel
{
    private static final long serialVersionUID = -8794591394128930541L;
    private static ImageIcon nb_icon = IconManager.resolveIcon("notebook.png");
    private static ImageIcon container_icon = IconManager.resolveIcon("window.png");//container.png");
    private static ImageIcon tabbed_icon = IconManager.resolveIcon("container1.png");
    
    private JTextArea textArea;
    private CellGroupMember cgm;
    private ImageIcon icon;

    public MinimizedUI(final CellGroupMember cgm)
    {
        this.cgm = cgm;
        initComponents();
        if(CellContainerVisual.getHandle().equals(cgm.getVisual()))
            icon = container_icon;
        else if(TabbedPaneVisual.getHandle().equals(cgm.getVisual()))
            icon = tabbed_icon;
        else 
            icon= nb_icon;
        String text = CellUtils.getName(cgm);
        if (text == null) text = "Untitled";
        setTitle(text);
    }

    public void setTitle(String title)
    {
        textArea.setText(title);
        putClientProperty("tooltip", title);
    }
    
    private void initComponents()
    {
        setBackground(Color.white);
        setPreferredSize(new Dimension(64, 64));
        setLayout(new GridBagLayout());

        textArea = new JTextArea();
        textArea.setFont(new Font("Tahoma", Font.BOLD, 12));
        textArea.setColumns(10);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setRows(2);
        textArea.setTabSize(4);
        textArea.setWrapStyleWord(true);
        textArea.setAutoscrolls(false);
        textArea.setBorder(null);
        textArea.setHighlighter(null);
        textArea.setMaximumSize(new Dimension(63, 30));
        textArea.setMinimumSize(new Dimension(63, 30));
        textArea.setOpaque(false);
        textArea.setPreferredSize(new Dimension(63, 30));
        add(textArea, new GridBagConstraints());

        MouseListener listener = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                if (e.getClickCount() == 2) CellUtils.toggleMinimized(cgm);
            }
        };
        textArea.addMouseListener(listener);
    }
    
    /*
     * Returns the underlying JTextArea that actually displays the text.
     * Use it to change font, foreground color, etc. 
     */
    public JTextArea getTextArea()
    {
        return textArea;
    }

    public void paintComponent(Graphics g)
    {
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());
        
        g.drawImage(icon.getImage(), (getWidth() - icon.getIconWidth())/2, 
                (getHeight() - icon.getIconHeight())/2, null);
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
