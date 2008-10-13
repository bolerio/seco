/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.notebook.syntax.completion;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import seco.notebook.storage.ClassRepository;
import seco.notebook.storage.DocInfo;
import seco.notebook.storage.PackageInfo;

public class JavaDocManager
{
	public static final boolean SHOW_DOC = true;
	//private static final String JAVADOC =
	//	"file:///C:/Program Files/Java/jdk1.5.0_01/docs/api/";
		//"http://java.sun.com/j2se/1.5.0/docs/api/";
	
	private static JavaDocManager instance;
	private static Map<Object, String> doc_cache = new HashMap<Object, String>();
     
	public static JavaDocManager getInstance()
	{
		if(instance == null)
			instance = new JavaDocManager();
		return instance;
	}
	
	public String getHTML(Object content)
	{
		boolean pkg = (content instanceof PackageInfo);
		String c = doc_cache.get(content);
		if(c == null)
    	{
    		URL url = constructURL(content);
    	    if(url != null)
    	    {
    	    	c = HTMLJavadocParser.getJavadocText(url, pkg);
    	        doc_cache.put(content, c);
    	    }
    	}//else
    	 		//System.out.println("Found");
    	return c;
	}
	
	private URL constructURL(Object content)
    {
    	return resolveURL(makeDocURL(content));
    }
	
	private static URL resolveURL(String path)
	{
		URL url = null;
    	try{
    		url = new URL(path);
    	}
    	catch(Exception ex)
    	{
    		//System.err.println("Can't find url: " +	path);
    	}
    	return url;
	}
	
	 private static String makeDocURL(Object obj)
     {
     	String tot = "";
     	if(obj instanceof Class)
     		return classURL((Class) obj);
     	if(obj instanceof Method)
     	{
     		Method m = (Method) obj;
     		tot += classURL(m.getDeclaringClass());
     		tot+= "#" + m.getName() + "(";
     		Class[] prs = m.getParameterTypes();
     		for(int i = 0; i< prs.length; i++)
     		{
     			tot += prs[i].getName();
     			if(i != prs.length-1) tot+= ", ";
     		}
     		tot+=")";
     	}else if(obj instanceof Field)
     	{
     		Field f = (Field)obj;
     		tot += classURL(f.getDeclaringClass());
     		tot += "#" + f.getName();
     	}else if(obj instanceof Constructor)
     	{
     		Constructor f = (Constructor)obj;
     		tot += classURL(f.getDeclaringClass());
     		tot += "#" + f.getName() + "(";
     		Class[] prs = f.getParameterTypes();
     		for(int i = 0; i< prs.length; i++)
     		{
     			tot += prs[i].getName();
     			if(i != prs.length-1) tot+= ",";
     		}
     		tot+=")";
     	}else if(obj instanceof PackageInfo)
     	{
     		PackageInfo f = (PackageInfo)obj;
     		tot += f.getFullName().replace(".", "/") + 
     		"/package-summary.html";
     		DocInfo info = ClassRepository.getInstance().getDocInfoForPackage(f.getFullName());
     		if(info != null)
         		return  "file:///" + info.getName() + File.separator + tot;
     	}
     	//System.out.println("JavaDocManager - makeDocURL: " + tot);
    	return tot;
     }
     
     private static String classURL(Class cls)
     {
     	String c = cls.getName();
     	c = c.replace(".", "/") + ".html";
     	DocInfo info = ClassRepository.getInstance().getDocInfoForClass(cls);
     	if(info != null)
     		return  "file:///" + info.getName() + File.separator + c;
     	return c;
     }
     
     private JavaDocManager()
 	 {}
}
