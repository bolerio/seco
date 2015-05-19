/*
 * @(#)StrictSecurityManager15.java  1.0  February 10, 2007
 *
 * Copyright (c) 2006 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */

package test;

import java.awt.AWTPermission;
import java.io.FilePermission;
import java.lang.reflect.ReflectPermission;
import java.net.*;
import java.security.*;
import java.util.*;
import java.util.logging.LoggingPermission;

/**
 * StrictSecurityManager15 that disallows almost everything.
 * This is used to test Quaqua in security restricted environments.
 * 
 * @author Werner Randelshofer
 * @version 1.0 February 10, 2007 Created.
 */
public class StrictSecurityManager15 extends SecurityManager {
    private static final List<String> ALLOWED_HOSTNAMES = Arrays.asList(new String[]{
        "localhost", "127.0.0.1",
    });
    private static final List<Permission> ALLOWED_PERMISSIONS = Arrays.asList(new Permission[] {
        new AWTPermission("accessClipboard"),
        new AWTPermission("showWindowWithoutWarningBanner"),
        //new AWTPermission("listenToAllAWTEvents"),
        //new AWTPermission("accessEventQueue"),
        new FilePermission("/-","read"),
        new LoggingPermission("control",null),
        new PropertyPermission("*","read"),
        new PropertyPermission("apple.laf.useScreenMenuBar","write"),
        new PropertyPermission("com.apple.macos.useScreenMenuBar","write"),
        new PropertyPermission("swing.aatext","write"),
        new PropertyPermission("sun.awt.exception.handler","write"),
        new PropertyPermission("user.timezone","write"),
        new ReflectPermission("suppressAccessChecks"),
        new RuntimePermission("accessClassInPackage.*"),
        new RuntimePermission("accessDeclaredMembers"),
        new RuntimePermission("createClassLoader"),
        new RuntimePermission("exitVM"),
        new RuntimePermission("loadLibrary.*"),
        new RuntimePermission("modifyThread"),
        new RuntimePermission("modifyThreadGroup"),
        new RuntimePermission("setContextClassLoader"),
        new RuntimePermission("canProcessApplicationEvents"),
        new RuntimePermission("setFactory"),
        new SecurityPermission("getProperty.networkaddress.cache.*"),
    });
    
    /** Creates a new instance. */
    public StrictSecurityManager15() {
    }
    
    public void checkConnect(String host, int port, Object context) {
        checkConnect(host, port);
    }
    
    public void checkConnect(String host, int port) {
        if (host == null) {
            throw new NullPointerException("host can't be null");
        }
        if (!host.startsWith("[") && host.indexOf(':') != -1) {
            host = "[" + host + "]";
        }
        if (ALLOWED_HOSTNAMES.contains(host)) {
            return;
        }
        String hostPort;
        if (port == -1) {
            hostPort = host;
        } else {
            hostPort = host + ":" + port;
        }
        String message = "Opening a socket connection to " + hostPort + " is restricted.";
        throw new AccessControlException(message, new SocketPermission(hostPort, "connect"));
    }
    
    private boolean isImplied(Permission perm) {
        for (Permission p : ALLOWED_PERMISSIONS) {
            if (p.implies(perm)) {
                return true;
            }
        }
        return false;
    }
    
    public void checkPermission(Permission perm) {
           StackTraceElement[] stack = Thread.currentThread().getStackTrace();
           boolean needsRestriction = false;
           String restrictor = "";
           for (int i=3; i < stack.length; i++) {
               String clazz = stack[i].getClassName();
               String method = stack[i].getMethodName();
               if (clazz.equals("java.security.AccessController") &&
                       method.equals("doPrivileged")) {
                   break;
               }
               if (clazz.startsWith("java.") ||
                       clazz.startsWith("apple.") ||
                       clazz.startsWith("javax.") ||
                       clazz.startsWith("sun.")) {
                   
               } else {
                   needsRestriction = true;
                   restrictor = stack[i].toString();
                   break;
               }
           }
           /*
           if (! needsRestriction) {
           System.out.println("NO RESTRICTION  "+Arrays.asList(cc));
           }*/
        // Allow all other actions
        if (needsRestriction && ! isImplied(perm)) {
        System.err.println("StrictSecurityManager.checkPermision("+perm+")");
        System.err.println("  "+Arrays.asList(stack));
        System.err.println("  "+restrictor);
            throw new AccessControlException("Not allowed "+perm, perm);
        }
    }
    
    public void checkPermission(Permission perm, Object context) {
        // Allow all other actions
        throw new AccessControlException("Not allowed context ", perm);
    }
}
