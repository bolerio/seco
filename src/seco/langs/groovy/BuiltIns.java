package seco.langs.groovy;

import groovy.inspect.Inspector;
import groovy.lang.MetaClass;
import groovy.lang.MetaMethod;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.codehaus.groovy.reflection.CachedClass;
import org.codehaus.groovy.runtime.InvokerHelper;

import seco.notebook.syntax.completion.CompletionItem;
import seco.notebook.syntax.java.JavaResultItem;

public class BuiltIns
{
   static Set<CompletionItem> groovyMetas;
   
   public static Set<CompletionItem> getGroovyMetas(){
       if(groovyMetas != null) return groovyMetas;
       groovyMetas = new HashSet<CompletionItem>();
       MetaClass metaClass = InvokerHelper.getMetaClass(new Object());
       List<MetaMethod> metaMethods = metaClass.getMetaMethods();
       for(MetaMethod m: metaMethods)
       {
           CachedClass[] cc = m.getParameterTypes(); 
           Class<?>[] types = new Class<?>[cc.length];
           for(int i = 0; i < cc.length; i++)
               types[i] = cc[i].getTheClass();
           groovyMetas.add(new JavaResultItem.MethodItem(m.getName(),
                   m.getReturnType(), types, null));
       }
       return groovyMetas;
   }
}
