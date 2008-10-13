/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.notebook.util;

import java.lang.ref.WeakReference;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.Timer;

/**
 * Action listener that has a weak reference
 * to the source action listener so it doesn't prevent
 * it to be garbage collected.
 * The calls to the <code>actionPerformed</code> are automatically
 * propagated to the source action listener.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public class WeakTimerListener implements ActionListener {

    private WeakReference ref;

    private boolean stopTimer;

    /** Construct new listener with automatic timer stopping.
     */
    public WeakTimerListener(ActionListener source) {
        this(source, true);
    }

    /** Construct new listener.
     * @param source source action listener to which this listener delegates.
     * @param stopTimer whether the timer should be stopped automatically when
     *  the timer fires and the source listener was garbage collected.
     */
    public WeakTimerListener(ActionListener source, boolean stopTimer) {
        this.ref = new WeakReference(source);
        this.stopTimer = stopTimer;
    }

    public void actionPerformed(ActionEvent evt) {
        ActionListener src = (ActionListener)ref.get();
        if (src != null) {
            src.actionPerformed(evt);

        } else { // source listener was garbage collected
            if (evt.getSource() instanceof Timer) {
                Timer timer = (Timer)evt.getSource();
                timer.removeActionListener(this);

                if (stopTimer) {
                    timer.stop();
                }
            }
        }
    }

}
