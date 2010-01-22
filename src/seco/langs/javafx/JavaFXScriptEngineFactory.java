package seco.langs.javafx;

//package com.sun.tools.javafx.script;

import java.util.*;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;

import com.sun.tools.javafx.script.JavaFXScriptEngineImpl;

// Referenced classes of package com.sun.tools.javafx.script:
//            JavaFXScriptEngineImpl

public class JavaFXScriptEngineFactory
    implements ScriptEngineFactory
{

    public JavaFXScriptEngineFactory()
    {
    }

    public String getEngineName()
    {
        return "JavaFX Script Engine";
    }

    public String getEngineVersion()
    {
        return "1.0";
    }

    public List getExtensions()
    {
        return extensions;
    }

    public String getLanguageName()
    {
        return "javafx";
    }

    public String getLanguageVersion()
    {
        return "1.0";
    }

    public String getMethodCallSyntax(String obj, String m, String args[])
    {
        StringBuilder buf = new StringBuilder();
        buf.append(obj);
        buf.append(".");
        buf.append(m);
        buf.append("(");
        if(args.length != 0)
        {
            int i;
            for(i = 0; i < args.length - 1; i++)
                buf.append((new StringBuilder()).append(args[i]).append(", ").toString());

            buf.append(args[i]);
        }
        buf.append(")");
        return buf.toString();
    }

    public List getMimeTypes()
    {
        return mimeTypes;
    }

    public List getNames()
    {
        return names;
    }

    public String getOutputStatement(String toDisplay)
    {
        StringBuilder buf = new StringBuilder();
        buf.append("java.lang.System.out.print(\"");
        int len = toDisplay.length();
        for(int i = 0; i < len; i++)
        {
            char ch = toDisplay.charAt(i);
            switch(ch)
            {
            case 34: // '"'
                buf.append("\\\"");
                break;

            case 92: // '\\'
                buf.append("\\\\");
                break;

            default:
                buf.append(ch);
                break;
            }
        }

        buf.append("\");");
        return buf.toString();
    }

    public String getParameter(String key)
    {
        if(key.equals("javax.script.engine"))
            return getEngineName();
        if(key.equals("javax.script.engine_version"))
            return getEngineVersion();
        if(key.equals("javax.script.name"))
            return getEngineName();
        if(key.equals("javax.script.language"))
            return getLanguageName();
        if(key.equals("javax.script.language_version"))
            return getLanguageVersion();
        if(key.equals("THREADING"))
            return "MULTITHREADED";
        else
            return null;
    }

    public String getProgram(String statements[])
    {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < statements.length; i++)
        {
            sb.append(statements[i]);
            sb.append("\n");
        }

        return sb.toString();
    }

    public ScriptEngine getScriptEngine()
    {
        JavaFXScriptEngineImpl engine = new JavaFXScriptEngineImpl();
        //engine.setFactory(this);
        return engine;
    }

    private static long nextClassNum = 0L;
    private static List names;
    private static List extensions;
    private static List mimeTypes;

    static 
    {
        names = new ArrayList(1);
        names.add("fx");
        extensions = Collections.unmodifiableList(names);
        names.add("javafx");
        names = Collections.unmodifiableList(names);
        mimeTypes = new ArrayList(0);
        mimeTypes.add("application/x-javafx-source");
        mimeTypes = Collections.unmodifiableList(mimeTypes);
    }
 }
