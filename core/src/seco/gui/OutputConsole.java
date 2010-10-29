package seco.gui;


import java.awt.Color;
import java.awt.Font;
import java.io.PrintStream;

import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

/*
 * Simple output console GUI, that handles redirected System.out and System.err.
 * Change the appearance of the err and out streams through the static err_attrs and out_attrs
 * variables
 */
public class OutputConsole extends JTextPane implements  DocumentListener
{
    private static final long serialVersionUID = -7922836398672418895L;

    private PrintStream out;
    private PrintStream err;
    private int outputMark = 0;

    private PrintStream oldErr;
    private PrintStream oldOut;

    public static SimpleAttributeSet err_attrs = new SimpleAttributeSet();
    public static SimpleAttributeSet out_attrs = new SimpleAttributeSet();
    static
    {
        StyleConstants.setForeground(err_attrs, Color.red);
        StyleConstants.setForeground(out_attrs, Color.blue);
    }

    public OutputConsole()
    {
        out = new PrintStream(new ConsoleWriter(false), true);
        err = new PrintStream(new ConsoleWriter(true), true);
        getDocument().addDocumentListener(this);
        setFont(new Font("Monospaced", 0, 12));
        setBackground(new Color(235, 235, 240));
    }

    @Override
    public void addNotify()
    {
        super.addNotify();
        attachToIO();
    }

    @Override
    public void removeNotify()
    {
        // restoreOldIO();
        super.removeNotify();
    }

    public void attachToIO()
    {
        if (System.err != getErr() && System.out != getOut())
        {
            oldErr = System.err;
            oldOut = System.out;
            System.setErr(getErr());
            System.setOut(getOut());
        }
    }

    public void restoreOldIO()
    {
        System.setErr(oldErr);
        System.setOut(oldOut);
    }

    public synchronized void write(String str, boolean err_or_out)
    {
        try
        {

            getDocument().insertString(outputMark, str,
                    (err_or_out) ? err_attrs : out_attrs);
            int len = str.length();
            outputMark += len;
            select(outputMark, outputMark);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    public synchronized void insertUpdate(DocumentEvent e)
    {
        int len = e.getLength();
        int off = e.getOffset();
        if (outputMark > off)
        {
            outputMark += len;
        }
    }

    public synchronized void removeUpdate(DocumentEvent e)
    {
        int len = e.getLength();
        int off = e.getOffset();
        if (outputMark > off)
        {
            if (outputMark >= off + len)
            {
                outputMark -= len;
            }
            else
            {
                outputMark = off;
            }
        }
    }

    public synchronized void changedUpdate(DocumentEvent e)
    {
    }

    public PrintStream getOut()
    {
        return out;
    }

    public PrintStream getErr()
    {
        return err;
    }

    class ConsoleWrite implements Runnable
    {
        private boolean err_or_out;
        private OutputConsole textArea;
        private String str;

        public ConsoleWrite(OutputConsole textArea, String str,
                boolean err_or_out)
        {
            this.textArea = textArea;
            this.str = str;
            this.err_or_out = err_or_out;
        }

        public void run()
        {
            textArea.write(str, err_or_out);
        }
    }

    class ConsoleWriter extends java.io.OutputStream
    {
        private boolean err_or_out;
        // private ConsoleTextArea textArea;
        private StringBuffer buffer;

        public ConsoleWriter(boolean err_or_out)
        {
            this.err_or_out = err_or_out;
            buffer = new StringBuffer();
        }

        @Override
        public synchronized void write(int ch)
        {
            buffer.append((char) ch);
            if (ch == '\n')
            {
                flushBuffer();
            }
        }

        public synchronized void write(char[] data, int off, int len)
        {
            for (int i = off; i < len; i++)
            {
                buffer.append(data[i]);
                if (data[i] == '\n')
                {
                    flushBuffer();
                }
            }
        }

        @Override
        public synchronized void flush()
        {
            if (buffer.length() > 0)
            {
                flushBuffer();
            }
        }

        @Override
        public void close()
        {
            flush();
        }

        private void flushBuffer()
        {
            String str = buffer.toString();
            buffer.setLength(0);
            SwingUtilities.invokeLater(new ConsoleWrite(OutputConsole.this,
                    str, err_or_out));
        }
    }
}
