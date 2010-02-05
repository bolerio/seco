/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.notebook.util;

import java.util.EventListener;


/** Listener which can be notifies when a task finishes.
* @see Task
*
* @author Jaroslav Tulach
*/
public interface TaskListener extends EventListener {
    /** Called when a task finishes running.
    * @param task the finished task
    */
    public void taskFinished(Task task);
}
