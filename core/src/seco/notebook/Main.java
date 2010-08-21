/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.notebook;

import seco.ThisNiche;
import seco.boot.StartMeUp;
import seco.gui.StandaloneFrame;
import seco.gui.TopFrame;

/**
 *
 * @author bolerio
 */
public class Main 
{	
    /** Creates a new instance of Main */
    public Main() 
    {
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) 
    {
        ThisNiche.guiControllerClassName = StandaloneFrame.class.getName();
        StartMeUp.main(null);
        //ThisNiche.guiController.getFrame().setVisible(true);
    }
    
}
