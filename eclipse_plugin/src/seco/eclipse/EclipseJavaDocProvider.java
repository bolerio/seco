package seco.eclipse;

import java.lang.reflect.Method;
import java.util.ArrayList;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;

import seco.notebook.syntax.completion.JavaDocManager.JavaDocProvider;

public class EclipseJavaDocProvider implements JavaDocProvider
{

    @Override
    public String getHTML(Object content)
    {
        if(content instanceof Method)
            return resolveMethod((Method) content);
        return null;
    }

    private String resolveMethod(Method m)
    {
        IType cl = PluginU.getClassIType(m.getDeclaringClass());
        System.out.println("EclipseJavaDocProvider: " + cl);
        if(cl == null) return null;
        String[] params = new String[m.getParameterTypes().length];
        for(int i = 0; i < params.length; i++)
        {
            params[i] = PluginU.getClassIType(m.getParameterTypes()[i]).getElementName();
        }
        
        IMethod im = cl.getMethod(m.getName(), params);
        System.out.println("EclipseJavaDocProvider1: " + im);
        try{
        return org.eclipse.jdt.internal.ui.text.javadoc.JavadocContentAccess2.getHTMLContent(im, true, true);
        }catch(Exception ex)
        {
            ex.printStackTrace();
            return null;
        }
    }
}
