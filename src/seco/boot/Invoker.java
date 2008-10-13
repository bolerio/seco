/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.boot;


import java.util.Iterator;
import java.io.File;
import java.util.Vector;
import java.util.Comparator;
import java.util.List;
import java.util.LinkedList;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Collections;

import seco.classloader.AdaptiveClassLoader;

/**
 * A dynamic class path invoker class.
 *
 * Loads another command-line invokable class
 * with a certain set of dynamically loaded directories and classpaths.
 */
public class Invoker
{

    /**
     * Parses arguments and loads the class.
     */
    private void loader(String [] args)
    {
        Vector dynamicClassPath = new Vector();
        String className = null;
        String [] passedArgs = null;
        boolean verbose = false;
        boolean systemClassPathLast = false;
        String  loaderType = null;


        int i=0;
        try
        {
            while (i<args.length)
            {
                if (args[i].equals("-loader"))
                {
                    loaderType = args[i+1];
                    i++;
                }
                else
                if (args[i].equals("-verbose"))
                {
                    verbose = true;
                }
                else
                if (args[i].equals("-systemcplast"))
                {
                    systemClassPathLast = true;
                }
                else
                if (args[i].equals("-cpalljars"))
                {
                    File dir = new File( args[i+1] );
                    if (dir.isDirectory())
                        dynamicClassPath.addAll(addRecursive( dir) );
                    else
                    {
                        System.err.println("Not a directory: " + dir.getAbsolutePath());
                        return;
                    }
                    i++;
                }
                else
                if (args[i].equals("-cpdir"))
                {
                    File dir = new File( args[i+1] );
                    if (dir.isDirectory())
                        dynamicClassPath.add( dir );
                    else
                    {
                        System.err.println("Not a directory: " + dir.getAbsolutePath());
                        return;
                    }
                    i++;
                }
                else
                if (args[i].equals("-cpjar"))
                {
                    File jarfile = new File( args[i+1] );
                    if (jarfile.isFile() && jarfile.canRead())
                        dynamicClassPath.add( jarfile );
                    else
                    {
                        System.err.println("Not a JAR file: " + jarfile.getAbsolutePath());
                        return;
                    }
                    i++;
                }
                else
                {
                    className = args[i];
                    i++;
                    passedArgs = new String [ args.length - i];
                    System.arraycopy(args, i, passedArgs, 0, passedArgs.length);
                    break;
                }
                i++;
            }
        }
        catch (ArrayIndexOutOfBoundsException e)
        {
            System.err.println("Required parameter missing for " + args[i]);
            help();
            return;
        }

        if (className == null)
        {
            System.err.println("You did not specify class name to be loaded.");
            help();
            return;
        }

        if (verbose)
        {
            System.out.println("Attempting to load class " + className + " using classpath repositories:");
            for (int j = 0; j < dynamicClassPath.size(); j++)
            {
                File repository = (File) dynamicClassPath.get(j);
                System.out.println("\t" +
                                   (repository.isDirectory() ? "(dir) " : "(jar) ")
                                   + repository);
            }
            System.out.println("System class loader used " + (systemClassPathLast ? "after" : "before") + " the dynamic class repositories.");
        }

        // attempt to load the specified class using dynamic class loader.
        ClassLoader loader = getLoaderForClasspath(dynamicClassPath, systemClassPathLast, loaderType);

        if (verbose)
        {
            final long start = System.currentTimeMillis();
            Runtime.getRuntime().addShutdownHook(
                new Thread()
                {
                    public void run()
                    {
                        System.out.println("Execution time: " +
                                           java.text.MessageFormat.format("{0,number,#.###}", new Object[] {
                                                new Double( (System.currentTimeMillis() - start)/1000.0 )}));
                    }
                }
            );
        }

        try
        {
            Thread.currentThread().setContextClassLoader(loader);

            Class invokeMeClass = loader.loadClass( className );

            Method main = invokeMeClass.getMethod("main", new Class [] { String[].class });
            int    modifiers = main.getModifiers();
            if (Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers))
            {
                // invoke the main method.
                main.invoke(invokeMeClass, new Object[] { passedArgs });
            }
            else
                throw new NoSuchMethodException();
        }
        catch (InvocationTargetException e)
        {
            System.out.println("[" + this.getClass().getName() + "] " + className + " has thrown an exception.");
            e.getTargetException().printStackTrace();
        }
        catch (NoSuchMethodException e)
        {
            System.err.println("Class " + className + " has no command line entry point method.");
        }
        catch (IllegalAccessException e)
        {
            System.err.println("Class " + className + " is not accessible: " + e.getMessage());
        }
        catch (ClassNotFoundException e)
        {
            System.err.println("Class " + className + " not found. Use -verbose to see repositories list.");
        }
    }


    /**
     * Compares two files by name.
     */
    private static Comparator fileComparator = new Comparator()
    {
        public int compare( Object a, Object b )
        {
            File fa = (File) a;
            File fb = (File) b;
            return fa.getName().compareTo(fb.getName());
        }
    };


    /**
     * Adds jars recursively from a directory.
     */
    private List addRecursive( File dir )
    {
        List jars = new LinkedList();
        List subdirs = new LinkedList();

        if (dir.isDirectory())
        {
            File [] subs = dir.listFiles();
            for (int i=0;i<subs.length;i++)
            {
                if (subs[i].isDirectory())
                    subdirs.add(subs[i]);
                else
                if (subs[i].getName().endsWith(".jar"))
                {
                    jars.add(subs[i]);
                }
            }

            // sort jar files
            Collections.sort(jars, fileComparator);
            Collections.sort(subdirs, fileComparator);

            for (Iterator i = subdirs.iterator();i.hasNext();)
            {
                File subdir = (File) i.next();
                jars.addAll( addRecursive( subdir ));
            }

            return jars;
        }
        return Collections.EMPTY_LIST;
    }


    /**
     * Returns an instance of a loader that includes all class repositories
     * given in the input argument.
     */
    public ClassLoader getLoaderForClasspath( Vector repositories, boolean systemClassPathLast, String loaderType)
    {
        if (loaderType != null)
        {
            if (loaderType.equals("accounting"))
            {
                final AdaptiveClassLoader loader = new AdaptiveClassLoader( repositories, systemClassPathLast );
                Runtime.getRuntime().addShutdownHook(
                    new Thread()
                    {
                        public void run()
                        {
                            System.out.println("Accounting class loader stats:");
                            System.out.println( loader.toString() );
                        }
                    }
                );

                return loader;
            }
            else
                throw new RuntimeException("Cannot determine loader type.");
        }
        else
            return new AdaptiveClassLoader( repositories, systemClassPathLast );
    }


    /**
     * Display help.
     */
    public static void help()
    {
        System.out.println("Usage: java {java options} -jar Invoker.jar [OPTION] class.to.be.instantiated arg1 arg2 ...");
        System.out.println("  Where [OPTION] can be any of the following:");
        System.out.println("    -cpalljars directory  Recursively adds all JAR");
        System.out.println("                          files from a given directory and");
        System.out.println("                          its subdirectories.");
        System.out.println("    -cpdir directory      Adds a directory (class folders structure)");
        System.out.println("    -cpjar file           Adds a JAR file to class repositories");
        System.out.println("    -systemcplast         System class loader is called after the");
        System.out.println("                          repositories have been scanned (override)");
        System.out.println("    -loader name          Other class loaders, current options are");
        System.out.println("            accouting     Class loader which at the end displays");
        System.out.println("                          the number of resolved classes");
        System.out.println("    -verbose              Displays lots of useful info about loaded classes");
    }


    /**
     * Parse command line arguments and invoke another class.
     */
    public static void main(String[] args)
            throws Exception
    {
        new Invoker().loader( args );
    }
}
