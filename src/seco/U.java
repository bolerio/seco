/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco;

import org.hypergraphdb.HGSearchResult;
import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGQuery;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.query.*;
import java.util.*;

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
           home = System.getProperty("user.home");
        //on my Windows System.getenv().get("HOME") return a quoted value 
        if(home != null && home.startsWith(QUOTE))
           home = unquote(home);
        return home;
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
    
    public static void apply(Lambda f, Collection c)
    {
        apply(f, c.iterator());
    }
    
    public static void apply(Lambda f, Iterator i)
    {
        while (i.hasNext())
            f.E(i.next());
    }
    
    public static void apply(Lambda f, HGQuery query)
    {
        apply(f, query.execute());
    }
    
    public static HGHandle hgType(Class c)
    {
        return ThisNiche.getHyperGraph().getTypeSystem().getTypeHandle(c);
    }
    
    public static HGSearchResult<HGHandle> hfind(HGQueryCondition c)
    {
        return ThisNiche.getHyperGraph().find(c);
    }
    
    public static Object hget(HGHandle h)
    {
        return ThisNiche.getHyperGraph().get(h);
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
}