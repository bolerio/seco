package seco.notebook.ruby;

/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 *
 * Redistribution and use in source and binary forms, with or without modification, are 
 * permitted provided that the following conditions are met: Redistributions of source code 
 * must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of 
 * conditions and the following disclaimer in the documentation and/or other materials 
 * provided with the distribution. Neither the name of the Sun Microsystems nor the names of 
 * is contributors may be used to endorse or promote products derived from this software 
 * without specific prior written permission. 

 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY 
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER 
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON 
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

/*
 * JRubyScriptEngine.java
 * @author A. Sundararajan
 * @author Roberto Chinnici
 */

import javax.script.*;

import java.lang.reflect.*;
import java.io.*;
import java.util.*;

import org.jruby.*;
import org.jruby.ast.*;
import org.jruby.internal.runtime.*;
import org.jruby.javasupport.*;
import org.jruby.runtime.*;
import org.jruby.runtime.builtin.*;
import org.jruby.internal.runtime.GlobalVariable;

public class JRubyScriptEngine extends AbstractScriptEngine 
        implements Compilable//, Invocable 
{ 

    // my factory, may be null
    private ScriptEngineFactory factory;
    private Ruby runtime;
   
    public JRubyScriptEngine() {
        init(System.getProperty("com.sun.script.jruby.loadpath"));
    }

    public JRubyScriptEngine(String loadPath) {
        init(loadPath);
    }

    // my implementation for CompiledScript
    private class JRubyCompiledScript extends CompiledScript {
        // my compiled code
        private Node node;     

        JRubyCompiledScript (Node node) {
            this.node = node;
        }

        public ScriptEngine getEngine() {
            return JRubyScriptEngine.this;
        }

        public Object eval(ScriptContext ctx) throws ScriptException {
            return evalNode(node, ctx);
        }
    }

    // Compilable methods
    public CompiledScript compile(String script) 
                                  throws ScriptException {  
        Node node = compileScript(script, context);
        return new JRubyCompiledScript(node);
    }

    public CompiledScript compile (Reader reader) 
                                  throws ScriptException {  
        Node node = compileScript(reader, context);
        return new JRubyCompiledScript(node);
    }

    // Invocable methods
    public Object invokeFunction(String name, Object... args) 
                         throws ScriptException, NoSuchMethodException {       
        return invokeImpl(null, name, args, Object.class);
    }

    public Object invokeMethod(Object obj, String name, Object... args) 
                         throws ScriptException, NoSuchMethodException {       
        if (obj == null) {
            throw new IllegalArgumentException("script object is null");
        }
        return invokeImpl(obj, name, args, Object.class);
    }

    public <T> T getInterface(Object obj, Class<T> clazz) {
        if (obj == null) {
            throw new IllegalArgumentException("script object is null");
        }
        return makeInterface(obj, clazz);
    }

    public <T> T getInterface(Class<T> clazz) {
        return makeInterface(null, clazz);
    }

    private <T> T makeInterface(Object obj, Class<T> clazz) {
        if (clazz == null || !clazz.isInterface()) {
            throw new IllegalArgumentException("interface Class expected");
        }
        final Object thiz = obj;
        return (T) Proxy.newProxyInstance(
              clazz.getClassLoader(),
              new Class[] { clazz },
              new InvocationHandler() {
                  public Object invoke(Object proxy, Method m, Object[] args)
                                       throws Throwable {
                      return invokeImpl(thiz, m.getName(),
                                        args, m.getReturnType());
                  }
              });
    }

    // ScriptEngine methods
    public synchronized Object eval(String str, ScriptContext ctx) 
                       throws ScriptException {	
        Node node = compileScript(str, ctx);
        return evalNode(node, ctx);
    }

    public synchronized Object eval(Reader reader, ScriptContext ctx)
                       throws ScriptException { 
        Node node = compileScript(reader, ctx);
        return evalNode(node, ctx);
    }

    public ScriptEngineFactory getFactory() {
	synchronized (this) {
	    if (factory == null) {
	    	factory = new JRubyScriptEngineFactory();
	    }
        }
	return factory;
    }

    public Bindings createBindings() {
        return new SimpleBindings();
    }

    // package-private methods
    public void setFactory(ScriptEngineFactory factory) {
        this.factory = factory;
    } 

    // internals only below this point    

    private Object rubyToJava(IRubyObject value) {
        return rubyToJava(value, Object.class);
    }

    private Object rubyToJava(IRubyObject value, Class type) {
        return JavaUtil.convertArgument(
                 Java.ruby_to_java(value, value, Block.NULL_BLOCK), 
                 type);
    }

    private IRubyObject javaToRuby(Object value) {
        if (value instanceof IRubyObject) {
            return (IRubyObject) value;
        }
        IRubyObject result = JavaUtil.convertJavaToRuby(runtime, value);
        if (result instanceof JavaObject) {
            return runtime.getModule("JavaUtilities").callMethod(runtime.getCurrentContext(), "wrap", result);
        }
        return result;
    }   

    private synchronized Node compileScript(String script, ScriptContext ctx) 
                                 throws ScriptException {        
        GlobalVariables oldGlobals = runtime.getGlobalVariables();  
        try {
            setGlobalVariables(ctx);
            String filename = (String) ctx.getAttribute(ScriptEngine.FILENAME);
            if (filename == null) {
                filename = "<unknown>";
            }
            return runtime.parse(script, filename, null, 0);
        } catch (Exception exp) {
            throw new ScriptException(exp);
        } finally {
            if (oldGlobals != null) {
                setGlobalVariables(oldGlobals);
            }
        }
    }

    private synchronized Node compileScript(Reader reader, ScriptContext ctx) 
                                 throws ScriptException {        
        GlobalVariables oldGlobals = runtime.getGlobalVariables();  
        try {
            setGlobalVariables(ctx);
            String filename = (String) ctx.getAttribute(ScriptEngine.FILENAME);
            if (filename == null) {
                filename = "<unknown>";
            }
            return runtime.parse(reader, filename, null, 0);
        } catch (Exception exp) {
            throw new ScriptException(exp);
        } finally {
            if (oldGlobals != null) {
                setGlobalVariables(oldGlobals);
            }
        }
    }

    private void setGlobalVariables(final ScriptContext ctx) {
        ctx.setAttribute("context", ctx, ScriptContext.ENGINE_SCOPE);
        setGlobalVariables(new GlobalVariables(runtime) {
                GlobalVariables parent = runtime.getGlobalVariables();
                
                public void define(String name, IAccessor accessor) {
                    assert name != null;
                    assert accessor != null;
                    assert name.startsWith("$");
                    synchronized (ctx) {
                        Bindings engineScope = ctx.getBindings(ScriptContext.ENGINE_SCOPE);                  
                        engineScope.put(name, new GlobalVariable(accessor)); 
                    }
                }


                public void defineReadonly(String name, IAccessor accessor) {
                    assert name != null;
                    assert accessor != null;
                    assert name.startsWith("$");
                    synchronized (ctx) {
                        Bindings engineScope = ctx.getBindings(ScriptContext.ENGINE_SCOPE);
                        engineScope.put(name, new GlobalVariable(new 
                                             ReadonlyAccessor(name, accessor)));
                    }
                } 

                public boolean isDefined(String name) {
                    assert name != null;
                    assert name.startsWith("$");
                    synchronized (ctx) {
                        String modifiedName = name.substring(1);
                        boolean defined = ctx.getAttributesScope(modifiedName) != -1;
                        return defined ? true : parent.isDefined(name);
                    }
                }

                public void alias(String name, String oldName) {
                    assert name != null;
                    assert oldName != null;
                    assert name.startsWith("$");
                    assert oldName.startsWith("$");

                    if (runtime.getSafeLevel() >= 4) {
                        throw runtime.newSecurityError("Insecure: can't alias global variable");
                    }

                    synchronized (ctx) {
                        int scope = ctx.getAttributesScope(name);
                        if (scope == -1) {
                            scope = ScriptContext.ENGINE_SCOPE;
                        }

                        IRubyObject value = get(oldName);
                        ctx.setAttribute(name, rubyToJava(value), scope);
                    }
                }

                public IRubyObject get(String name) {
                    assert name != null;
                    assert name.startsWith("$");

                    synchronized (ctx) {
                        // skip '$' and try
                        String modifiedName = name.substring(1);
                        int scope = ctx.getAttributesScope(modifiedName);
                        if (scope == -1) {
                            return parent.get(name);
                        }

                        Object obj = ctx.getAttribute(modifiedName, scope);
                        if (obj instanceof IAccessor) {
                            return ((IAccessor)obj).getValue();
                        } else {
                            return javaToRuby(obj);
                        }
                    }                    
                }

                public IRubyObject set(String name, IRubyObject value) {
                    assert name != null;
                    assert name.startsWith("$");

                    if (runtime.getSafeLevel() >= 4) {
                        throw runtime.newSecurityError("Insecure: can't change global variable value");
                    }

                    synchronized (ctx) {
                        // skip '$' and try
                        String modifiedName = name.substring(1);
                        int scope = ctx.getAttributesScope(modifiedName);
                        if (scope == -1) {
                            scope = ScriptContext.ENGINE_SCOPE;
                        }
                        IRubyObject oldValue = get(name);
                        Object obj = ctx.getAttribute(modifiedName, scope);
                        if (obj instanceof IAccessor) {
                            ((IAccessor)obj).setValue(value);
                        } else {                        
                            ctx.setAttribute(modifiedName, rubyToJava(value), scope);
                        }
                        return oldValue;
                    }
                }

                public Iterator getNames() {                    
                    List list = new ArrayList();
                    synchronized (ctx) {
                        for (int scope : ctx.getScopes()) {
                            Bindings b = ctx.getBindings(scope);
                            if (b != null) {
                                for (String key: b.keySet()) {
                                    list.add(key);
                                }
                            }
                        }
                    }
                    for (Iterator names = parent.getNames(); names.hasNext();) {
                        list.add(names.next());
                    }
                    return Collections.unmodifiableList(list).iterator();
                }
            });
    }

    private void setGlobalVariables(GlobalVariables globals) {
        runtime.setGlobalVariables(globals);
    }

    synchronized Object evalNode(Node node, ScriptContext ctx) 
                            throws ScriptException {
        GlobalVariables oldGlobals = runtime.getGlobalVariables();
        try {
            setGlobalVariables(ctx);
            return rubyToJava(runtime.eval(node));
        } catch (Exception exp) {
            throw new ScriptException(exp);
        } finally {
            if (oldGlobals != null) {
                setGlobalVariables(oldGlobals);
            }
        }
    }

    private void init(String loadPath) {        
        runtime = Ruby.getDefaultInstance();
        if (loadPath == null) {
            loadPath = System.getProperty("java.class.path");
        }
        List list = Arrays.asList(loadPath.split(File.pathSeparator));
        runtime.getLoadService().init(list);                
        runtime.getLoadService().require("java");
    }

    private Object invokeImpl(final Object obj, String method, 
                        Object[] args, Class returnType)
                        throws ScriptException {
        if (method == null) {
            throw new NullPointerException("method name is null");
        }

        try {
            IRubyObject rubyRecv = obj != null ? 
                  JavaUtil.convertJavaToRuby(runtime, obj) : runtime.getTopSelf();

            IRubyObject[] rubyArgs = JavaUtil.convertJavaArrayToRuby(runtime, args);

            // Create Ruby proxies for any input arguments that are not primitives.
            IRubyObject javaUtilities = runtime.getObject().getConstant("JavaUtilities");
            for (int i = 0; i < rubyArgs.length; i++) {
                IRubyObject tmp = rubyArgs[i];
                if (tmp instanceof JavaObject) {
                    rubyArgs[i] = javaUtilities.callMethod(runtime.getCurrentContext(), "wrap", tmp);
                }
            }

            IRubyObject result = rubyRecv.callMethod(runtime.getCurrentContext(), method, rubyArgs);
            return rubyToJava(result, returnType);
        } catch (Exception exp) {
            throw new ScriptException(exp);
        }
    }
}

