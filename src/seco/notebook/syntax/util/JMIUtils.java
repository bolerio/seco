/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.notebook.syntax.util;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.lang.*;



public class JMIUtils
{
	private static boolean caseSensitive = true;
	
	public static boolean startsWith(String theString, String prefix){
        return caseSensitive ? theString.startsWith(prefix) :
            theString.toLowerCase().startsWith(prefix.toLowerCase());
    }
    
    public static boolean matchesCamelCase(String simpleName, String prefix) {
        int sni;
        int pi;
        for (pi = 0, sni = 0; sni < simpleName.length() && pi < prefix.length(); sni++) {
            char ch = simpleName.charAt(sni);
            if (Character.isUpperCase(ch)) {
                if (ch != prefix.charAt(pi++)) {
                    return false;
                }
            }
        }
        return pi == prefix.length();
    }
    
    public static String getTypeName(Object typ, boolean displayFQN, boolean substituteTypeParams) {
        if (typ instanceof Array)
            return getTypeName(typ.getClass().getName(), displayFQN, false) + "[]"; // NOI18N
        if (typ instanceof Class)
        	if(((Class) typ).isPrimitive())
        		return primitiveToStrMap.get((Class) typ);
        	else
              return displayFQN ? ((Class)typ).getName() : ((Class)typ).getSimpleName();
        if (typ instanceof Method)
        	return ((Method) typ).getName();
        return typ != null ? typ.toString() : ""; //NOI18N
    }
    
    
     private static final Map<Class, String> primitiveToStrMap = 
    	 new HashMap<Class, String>(13);

     static
     {
    	 primitiveToStrMap.put(Boolean.TYPE, "boolean");
    	 primitiveToStrMap.put(Byte.TYPE, "byte");
    	 primitiveToStrMap.put(Character.TYPE, "char");
    	 primitiveToStrMap.put(Double.TYPE, "double");
    	 primitiveToStrMap.put(Float.TYPE, "float");
    	 primitiveToStrMap.put(Integer.TYPE, "int");
    	 primitiveToStrMap.put(Long.TYPE, "long");
    	 primitiveToStrMap.put(Short.TYPE, "short");
    	 primitiveToStrMap.put(Void.TYPE, "void");
     }
    
    
    public static Class getExactClass(String name, String pkgName) {
        return getExactClass((pkgName != null && pkgName.length() != 0) ? (pkgName + "." + name) : name); // NOI18N
    }

    public static Class getExactClass(String classFullName) {
        /*
    	Type cls = resolveType(classFullName);
        if (cls instanceof UnresolvedClass)
            return null;
        if (cls instanceof Class)
            return (Class)cls;*/
    	try{
    		return Class.forName(classFullName);
    	}catch(Exception ex)
    	{
    		ex.printStackTrace();
    	}
        return null;
    }
    
    public static boolean isAssignable(Class from, Class to) {
        
        return from.isAssignableFrom(to) || to.isAssignableFrom(from);
    }

   


}
