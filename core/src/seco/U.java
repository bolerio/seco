/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco;

import org.hypergraphdb.HGConfiguration;
import org.hypergraphdb.HGSearchResult;
import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGQuery;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.query.*;
import org.hypergraphdb.storage.bje.BJEStorageImplementation;

import seco.util.task.CallableCallback;
import seco.util.task.CompletionCallback;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

/**
 * <p>
 * A bag of stuff -- utilities, that is. 
 * </p>
 * 
 * @author Borislav Iordanov
 */
public class U
{
	private static final String ESCAPE = "\\";
	private static final String ESCAPE_ESCAPE = "\\\\";
	private static final String	QUOTE = "\"";
	private static final String	QUOTE_ESCAPE = "\\\"";
	
	public static HGConfiguration dbConfig()
	{
		HGConfiguration config = new HGConfiguration();
		config.setStoreImplementation(new BJEStorageImplementation());
		return config;
	}
	
    public static String quote(String s)
    {
    	if (s == null)
    		return s;
    	s.replace(ESCAPE, ESCAPE_ESCAPE).replace(QUOTE, QUOTE_ESCAPE);
    	StringBuffer result = new StringBuffer(s);
    	result.insert(0, QUOTE);
    	result.append(QUOTE);
    	return result.toString();
    }
    
    public static String unquote(String s)
    {
    	s = s.substring(1, s.length() - 1);
    	return s.replace(QUOTE_ESCAPE, QUOTE).replace(ESCAPE_ESCAPE, ESCAPE);
    }
    
    public static String findUserHome()
    {
    	// unix and cygwin take precedence over the long and not often used by developers
    	// windows "user.home"
        String home = System.getenv().get("HOME");
        if(home == null)
           home = System.getProperty("user.home"); //System.getenv().get("USERPROFILE");
        //on my Windows System.getenv().get("HOME") return a quoted value 
        if(home != null && home.startsWith(QUOTE))
           home = unquote(home);
        return home;
    }
    
    public static String getResourceContentAsString(String name)
    {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        InputStream in = loader.getResourceAsStream(name);
        if (in == null)
            return null;
        try
        {
            InputStreamReader reader = new InputStreamReader(in);
            StringBuffer result = new StringBuffer();
            char [] buf = new char[1024];
            for (int c = reader.read(buf); c > -1; c = reader.read(buf))
                result.append(buf);
            return result.toString();
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }
    }
    
    /**
     * <p>
     * An inteface to define a "closure" with an arbitrary number
     * of arguments. This could be useful if we could define it using generics
     * so as to type check the functions. However, it doesn't play well with 
     * composition and with the automatic conversion to/from Object[]. So,
     * we have to leave everything untyped.
     * </p>
     * <p>
     * NOTE: this is mostly experimental as an API, see if we can provide some
     * high-level, nice syntactic sugar to the HyperGraph too Java-ish API.
     * </p>
     * @author Borislav Iordanov
     *
     */
    public static abstract class Lambda
    {
        private Lambda compose = null;
        
        Object E(Object...args)
        {
            if (compose != null)
                return eval(compose.E(args));
            else 
                return eval(args);
        }
        
        public Lambda() { }
        public Lambda(Lambda compose) 
            { this.compose = compose; }
        
        /**
         * Return a function that first evaluates the passed in argument
         * and then <em>this</em> function.
         * @param other
         * @return
         */
        public Lambda after(Lambda other) 
            { this.compose = other; return this; }
        
        /**
         * Return a function that first evaluates <em>this</em> function
         * and then the passed in argument.
         * @param other
         * @return
         */
        public Lambda before(Lambda other)
        {
            other.compose = this; return other; 
        }
        
        public abstract Object eval(Object ...args);        
    }
    
    /**
     * <code>hgDeref: [HGHandle](n) -> [Object](n)</code>
     * 
     * Transform N HyperGraph handles into their corresponding atoms.
     */
    public static final Lambda hgDeref = new Lambda() 
    { 
        final HyperGraph hg = ThisNiche.getHyperGraph();
        public Object eval(Object ...args)
        {
            return hg.get((HGHandle)args[0]);
        }
    };
    
    public static void apply(Lambda f, Collection<?> c)
    {
        apply(f, c.iterator());
    }
    
    public static void apply(Lambda f, Iterator<?> i)
    {
        while (i.hasNext())
            f.E(i.next());
    }
    
    public static void apply(Lambda f, HGQuery<?> query)
    {
        apply(f, query.execute());
    }
    
    public static HGHandle hgType(Class<?> c)
    {
        return ThisNiche.getHyperGraph().getTypeSystem().getTypeHandle(c);
    }
    
    public static HGSearchResult<HGHandle> hfind(HGQueryCondition c)
    {
        return ThisNiche.getHyperGraph().find(c);
    }

    @SuppressWarnings("unchecked")
    public static <T> T hget(HGHandle h)
    {
        T result = (T)ThisNiche.getHyperGraph().get(h); 
        return result;
    }

    public static HGHandle htype(HGHandle h)
    {
        return ThisNiche.getHyperGraph().getType(h); 
    }
    
    public static <T> List<T> hget(HGQueryCondition cond)
    {
        return hg.getAll(ThisNiche.getHyperGraph(), cond);
    }
    
    @SuppressWarnings("unchecked")
    public static <T> T hgetOne(HGQueryCondition cond)
    {
        T result = (T)hg.getOne(ThisNiche.getHyperGraph(), cond); 
        return result;
    }
    
    public static HGHandle hhandle(Object x)
    {
        return ThisNiche.getHyperGraph().getHandle(x);
    }
    
    public static void close(HGSearchResult<?> rs)
    {
    	if (rs != null)
    		rs.close();
    }
    
    public static void closeNoException(HGSearchResult<?> rs)
    {
    	if (rs != null)
    		try { rs.close(); } catch (Throwable t) { }
    }
    
    public static void run(Runnable r) { ThisNiche.executorService.execute(r); }
    public static <V> Future<V> run(Callable<V> c) { return ThisNiche.executorService.submit(c); }
    public static <V> Future<V> run(Runnable r, final CompletionCallback<V> callback)
    {
        FutureTask<V> ft = new FutureTask<V>(r, null) 
        {
            protected void done()
            {
                try
                {
                    callback.onCompletion(get(), null);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
                catch (ExecutionException e)
                {
                    callback.onCompletion(null, e.getCause());
                }
            }
        };
        ThisNiche.executorService.submit(ft);
        return ft;
    }
    public static <V> Future<V> run(final Callable<V> c, final CompletionCallback<V> callback)
    {
        FutureTask<V> ft = new FutureTask<V>(c) 
        {
            protected void done()
            {
                try
                {
                    callback.onCompletion(get(), null);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
                catch (ExecutionException e)
                {
                    callback.onCompletion(null, e.getCause());
                }
            }
        };
        ThisNiche.executorService.submit(ft);    
        return ft;
    }    
    
    public static <V> Future<V> run(final CallableCallback<V> cc)
    {
        FutureTask<V> ft = new FutureTask<V>(cc) 
        {
            protected void done()
            {
                try
                {
                    cc.onCompletion(get(), null);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
                catch (ExecutionException e)
                {
                    cc.onCompletion(null, e.getCause());
                }
            }
        };
        ThisNiche.executorService.submit(ft);      
        return ft;
    }
}