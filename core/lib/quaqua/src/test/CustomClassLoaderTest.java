/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package test;

import java.lang.reflect.*;
import java.net.*;


/**
 *
 * @author werni
 */
public class CustomClassLoaderTest {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            // For this test to succeed, Quaqua must not be in the System classpath
            if (Class.forName("ch.randelshofer.quaqua.QuaquaLookAndFeel") != null) {
                System.err.println("Quaqua must not be in the System classpath for this test to succeed");
            }
        } catch (ClassNotFoundException ex) {
            try {
                // All fine, we can proceed with the test.
                URLClassLoader l = new URLClassLoader(new URL[]{
                    new URL("file:./quaqua-test.jar"),
                    new URL("file:./quaqua.jar"),
                });
                
                Class c = l.loadClass("test.QuaquaTest");
                Method m = c.getMethod("main", new Class[] {String[].class});
                m.invoke(null, new Object[] {args});
                
            } catch (Exception e) {
                e.printStackTrace();
            }
            
        }
    }

}
