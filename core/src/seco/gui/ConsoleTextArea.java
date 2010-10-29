package seco.gui;

/* -*- Mode: java; tab-width: 8; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 *
 * ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Rhino JavaScript Debugger code, released
 * November 21, 2000.
 *
 * The Initial Developer of the Original Code is
 * See Beyond Corporation.
 * Portions created by the Initial Developer are Copyright (C) 2000
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Christopher Oliver
 *
 * Alternatively, the contents of this file may be used under the terms of
 * the GNU General Public License Version 2 or later (the "GPL"), in which
 * case the provisions of the GPL are applicable instead of those above. If
 * you wish to allow use of your version of this file only under the terms of
 * the GPL and not to allow others to use your version of this file under the
 * MPL, indicate your decision by deleting the provisions above and replacing
 * them with the notice and other provisions required by the GPL. If you do
 * not delete the provisions above, a recipient may use your version of this
 * file under either the MPL or the GPL.
 *
 * ***** END LICENSE BLOCK ***** */
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
 * Simple output console.
 * Loosely based on the org.mozilla.javascript.tools.ConsoleTextArea
 */
public class ConsoleTextArea extends JTextPane implements  DocumentListener
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

    public ConsoleTextArea()
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
        private ConsoleTextArea textArea;
        private String str;

        public ConsoleWrite(ConsoleTextArea textArea, String str,
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
            SwingUtilities.invokeLater(new ConsoleWrite(ConsoleTextArea.this,
                    str, err_or_out));
        }
    }
}
