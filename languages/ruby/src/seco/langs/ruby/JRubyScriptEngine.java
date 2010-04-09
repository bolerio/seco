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

package seco.langs.ruby;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.script.AbstractScriptEngine;
import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import org.jruby.Ruby;
import org.jruby.RubyException;
import org.jruby.RubyIO;
import org.jruby.RubyObject;
import org.jruby.ast.Node;
import org.jruby.exceptions.RaiseException;
import org.jruby.internal.runtime.GlobalVariable;
import org.jruby.internal.runtime.GlobalVariables;
import org.jruby.internal.runtime.ReadonlyAccessor;
import org.jruby.internal.runtime.ValueAccessor;
import org.jruby.javasupport.Java;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.javasupport.JavaObject;
import org.jruby.javasupport.JavaUtil;
import org.jruby.runtime.Block;
import org.jruby.runtime.IAccessor;
import org.jruby.runtime.builtin.IRubyObject;
import org.jruby.util.KCode;

public class JRubyScriptEngine extends AbstractScriptEngine implements
		Compilable, Invocable
{

	// my factory, may be null
	private ScriptEngineFactory factory;
	private Ruby runtime;
	private boolean autoTerminate = true;

	public JRubyScriptEngine()
	{
		this(System.getProperty("com.sun.script.jruby.loadpath"));
	}

	public JRubyScriptEngine(String loadPath)
	{
		init(loadPath);
	}

	// my implementation for CompiledScript
	private class JRubyCompiledScript extends CompiledScript
	{
		// my compiled code
		private Node node;

		JRubyCompiledScript(Node node)
		{
			this.node = node;
		}

		public ScriptEngine getEngine()
		{
			return JRubyScriptEngine.this;
		}

		public Object eval(ScriptContext ctx) throws ScriptException
		{
			return evalNode(node, ctx);
		}
	}

	// Compilable methods
	public CompiledScript compile(String script) throws ScriptException
	{
		Node node = compileScript(script, context);
		return new JRubyCompiledScript(node);
	}

	public CompiledScript compile(Reader reader) throws ScriptException
	{
		Node node = compileScript(reader, context);
		return new JRubyCompiledScript(node);
	}

	// Invocable methods
	public Object invokeFunction(String name, Object... args)
			throws ScriptException, NoSuchMethodException
	{
		return invokeImpl(null, name, args, Object.class);
	}

	public Object invokeMethod(Object obj, String name, Object... args)
			throws ScriptException, NoSuchMethodException
	{
		if (obj == null)
		{
			throw new IllegalArgumentException("script object is null");
		}
		return invokeImpl(obj, name, args, Object.class);
	}

	public <T> T getInterface(Object obj, Class<T> clazz)
	{
		if (obj == null)
		{
			throw new IllegalArgumentException("script object is null");
		}
		return makeInterface(obj, clazz);
	}

	public <T> T getInterface(Class<T> clazz)
	{
		return makeInterface(null, clazz);
	}

	private <T> T makeInterface(Object obj, Class<T> clazz)
	{
		if (clazz == null || !clazz.isInterface())
		{
			throw new IllegalArgumentException("interface Class expected");
		}
		final Object thiz = obj;
		return (T) Proxy.newProxyInstance(clazz.getClassLoader(),
				new Class[] { clazz }, new InvocationHandler()
				{
					public Object invoke(Object proxy, Method m, Object[] args)
							throws Throwable
					{
						return invokeImpl(thiz, m.getName(), args, m
								.getReturnType());
					}
				});
	}

	// ScriptEngine methods
	public synchronized Object eval(String str, ScriptContext ctx)
			throws ScriptException
	{
		Node node = compileScript(str, ctx);
		return evalNode(node, ctx);
	}

	public synchronized Object eval(Reader reader, ScriptContext ctx)
			throws ScriptException
	{
		Node node = compileScript(reader, ctx);
		return evalNode(node, ctx);
	}

	public ScriptEngineFactory getFactory()
	{
		synchronized (this)
		{
			if (factory == null)
			{
				factory = new JRubyScriptEngineFactory();
			}
		}
		return factory;
	}

	public Bindings createBindings()
	{
		return new SimpleBindings();
	}

	// package-private methods
	public void setFactory(ScriptEngineFactory factory)
	{
		this.factory = factory;
	}

	// internals only below this point

	private Object rubyToJava(IRubyObject value)
	{
		return rubyToJava(value, Object.class);
	}

	private Object rubyToJava(IRubyObject value, Class type)
	{
		return JavaUtil.convertArgument(runtime, Java.ruby_to_java(value,
				value, Block.NULL_BLOCK), type);
	}

	private IRubyObject javaToRuby(Object value)
	{
		if (value instanceof IRubyObject)
		{
			return (IRubyObject) value;
		}
		IRubyObject result = JavaUtil.convertJavaToRuby(runtime, value);
		if (result instanceof JavaObject)
		{
			return runtime.getModule("JavaUtilities").callMethod(
					runtime.getCurrentContext(), "wrap", result);
		}
		return result;
	}

	private synchronized Node compileScript(String script, ScriptContext ctx)
			throws ScriptException
	{
		GlobalVariables oldGlobals = runtime.getGlobalVariables();
		try
		{
			setErrorWriter(ctx.getErrorWriter());
			setGlobalVariables(ctx);
			String filename = (String) ctx.getAttribute(ScriptEngine.FILENAME);
			if (filename == null)
			{
				filename = "<unknown>";
			}
			return runtime.parseEval(script, filename, runtime
					.getCurrentContext().getCurrentScope(), 0);
		} catch (RaiseException e)
		{
			RubyException re = e.getException();
			runtime.printError(re);
			throw new ScriptException(e);
		} catch (Exception e)
		{
			throw new ScriptException(e);
		} finally
		{
			if (oldGlobals != null)
			{
				setGlobalVariables(oldGlobals);
			}
		}
	}

	private synchronized Node compileScript(Reader reader, ScriptContext ctx)
			throws ScriptException
	{
		GlobalVariables oldGlobals = runtime.getGlobalVariables();
		try
		{
			setErrorWriter(ctx.getErrorWriter());
			setGlobalVariables(ctx);
			String filename = (String) ctx.getAttribute(ScriptEngine.FILENAME);
			if (filename == null)
			{
				filename = "<unknown>";
				String script = getRubyScript(reader);
				return runtime.parseEval(script, filename, runtime
						.getCurrentContext().getCurrentScope(), 0);
			}
			InputStream inputStream = getRubyReader(filename);
			return runtime.parseFile(inputStream, filename, runtime
					.getCurrentContext().getCurrentScope());
		} 
		catch (RaiseException e)
		{
			RubyException re = e.getException();
			runtime.printError(re);
			throw new ScriptException(e);
		} 
		catch (Exception exp)
		{
			throw new ScriptException(exp);
		} 
		finally
		{
			if (oldGlobals != null)
			{
				setGlobalVariables(oldGlobals);
			}
		}
	}

	private String getRubyScript(Reader reader) throws IOException
	{
		StringBuffer sb = new StringBuffer();
		char[] cbuf;
		while (true)
		{
			cbuf = new char[8 * 1024];
			int chars = reader.read(cbuf, 0, cbuf.length);
			if (chars < 0)
			{
				break;
			}
			sb.append(cbuf, 0, chars);
		}
		cbuf = null;
		return (new String(sb)).trim();
	}

	private InputStream getRubyReader(String filename)
			throws FileNotFoundException
	{
		File file = new File(filename);
		return new FileInputStream(file);
	}

	private void setGlobalVariables(final ScriptContext ctx)
	{
		ctx.setAttribute("context", ctx, ScriptContext.ENGINE_SCOPE);
		setGlobalVariables(new GlobalVariables(runtime)
		{
			GlobalVariables parent = runtime.getGlobalVariables();

			@Override
			public void define(String name, IAccessor accessor)
			{
				assert name != null;
				assert accessor != null;
				assert name.startsWith("$");
				synchronized (ctx)
				{
					Bindings engineScope = ctx
							.getBindings(ScriptContext.ENGINE_SCOPE);
					engineScope.put(name, new GlobalVariable(accessor));
				}
			}

			@Override
			public void defineReadonly(String name, IAccessor accessor)
			{
				assert name != null;
				assert accessor != null;
				assert name.startsWith("$");
				synchronized (ctx)
				{
					Bindings engineScope = ctx
							.getBindings(ScriptContext.ENGINE_SCOPE);
					engineScope.put(name, new GlobalVariable(
							new ReadonlyAccessor(name, accessor)));
				}
			}

			@Override
			public boolean isDefined(String name)
			{
				assert name != null;
				assert name.startsWith("$");
				synchronized (ctx)
				{
					String modifiedName = name.substring(1);
					boolean defined = ctx.getAttributesScope(modifiedName) != -1;
					return defined ? true : parent.isDefined(name);
				}
			}

			@Override
			public void alias(String name, String oldName)
			{
				assert name != null;
				assert oldName != null;
				assert name.startsWith("$");
				assert oldName.startsWith("$");

				if (runtime.getSafeLevel() >= 4)
				{
					throw runtime
							.newSecurityError("Insecure: can't alias global variable");
				}

				synchronized (ctx)
				{
					int scope = ctx.getAttributesScope(name);
					if (scope == -1)
					{
						scope = ScriptContext.ENGINE_SCOPE;
					}

					IRubyObject value = get(oldName);
					ctx.setAttribute(name, rubyToJava(value), scope);
				}
			}

			@Override
			public IRubyObject get(String name)
			{
				assert name != null;
				assert name.startsWith("$");

				synchronized (ctx)
				{
					// skip '$' and try
					String modifiedName = name.substring(1);
					int scope = ctx.getAttributesScope(modifiedName);
					if (scope == -1)
					{
						return parent.get(name);
					}

					Object obj = ctx.getAttribute(modifiedName, scope);
					if (obj instanceof IAccessor)
					{
						return ((IAccessor) obj).getValue();
					} else
					{
						return javaToRuby(obj);
					}
				}
			}

			@Override
			public IRubyObject set(String name, IRubyObject value)
			{
				assert name != null;
				assert name.startsWith("$");

				if (runtime.getSafeLevel() >= 4)
				{
					throw runtime
							.newSecurityError("Insecure: can't change global variable value");
				}

				synchronized (ctx)
				{
					// skip '$' and try
					String modifiedName = name.substring(1);
					int scope = ctx.getAttributesScope(modifiedName);
					if (scope == -1)
					{
						scope = ScriptContext.ENGINE_SCOPE;
					}
					IRubyObject oldValue = get(name);
					Object obj = ctx.getAttribute(modifiedName, scope);
					if (obj instanceof IAccessor)
					{
						((IAccessor) obj).setValue(value);
					} else
					{
						ctx
								.setAttribute(modifiedName, rubyToJava(value),
										scope);
						if ("KCODE".equals(modifiedName))
						{
							setKCode((String) rubyToJava(value));
						} else if ("stdout".equals(modifiedName))
						{
							equalOutputs((RubyObject) value);
						}
					}
					return oldValue;
				}
			}

			@Override
			public Set<String> getNames()
			{
				HashSet set = new HashSet();
				synchronized (ctx)
				{
					for (int scope : ctx.getScopes())
					{
						Bindings b = ctx.getBindings(scope);
						if (b != null)
						{
							for (String key : b.keySet())
							{
								set.add(key);
							}
						}
					}
				}
				for (Iterator<String> names = parent.getNames().iterator(); names
						.hasNext();)
				{
					set.add(names.next());
				}
				return Collections.unmodifiableSet(set);
			}

			@Override
			public IRubyObject getDefaultSeparator()
			{
				return parent.getDefaultSeparator();
			}
		});
	}

	private void setGlobalVariables(GlobalVariables globals)
	{
		runtime.setGlobalVariables(globals);
	}

	private void checkAutoTermination()
	{
		String p = System.getProperty("com.sun.script.jruby.terminator");
		if (p != null)
		{
			if ("on".equals(p))
			{
				autoTerminate = true;
			} else if ("off".equals(p))
			{
				autoTerminate = false;
			} else
			{
				throw new IllegalArgumentException(
						"com.sun.script.jruby.terminator property should be on or off.");
			}
		}
	}

	private Object evalNodeWithAutoTermination(Node node) throws Exception
	{
		try
		{
			return rubyToJava(runtime.runNormally(node, false));
		} finally
		{
			try
			{
				JavaEmbedUtils.terminate(runtime);
			} catch (RaiseException e)
			{
				RubyException re = e.getException();
				runtime.printError(re);
				if (!runtime.fastGetClass("SystemExit").isInstance(re))
				{
					throw new ScriptException(e);
				}
			}
		}
	}

	private Object evalNodeWithoutAutoTermination(Node node)
	{
		IRubyObject result = node.interpret(runtime, runtime
				.getCurrentContext(), runtime.getCurrentContext()
				.getFrameSelf(), Block.NULL_BLOCK);
		return JavaEmbedUtils.rubyToJava(runtime, result, Object.class);
	}

    synchronized Object evalNode(Node node, ScriptContext ctx)
			throws ScriptException
	{
		GlobalVariables oldGlobals = runtime.getGlobalVariables();
		try
		{
			setWriterOutputStream(ctx.getWriter());
			setErrorWriter(ctx.getErrorWriter());
			setGlobalVariables(ctx);
			checkAutoTermination();
			if (autoTerminate)
			{
				return evalNodeWithAutoTermination(node);
			} else
			{
				return evalNodeWithoutAutoTermination(node);
			}
		} catch (Exception exp)
		{
			throw new ScriptException(exp);
		} finally
		{
			if (oldGlobals != null)
			{
				setGlobalVariables(oldGlobals);
			}
		}
	}

	private void init(String loadPath)
	{
		runtime = Ruby.newInstance();
		IAccessor d = new ValueAccessor(runtime.newString("<script>"));
		runtime.getGlobalVariables().define("$PROGRAM_NAME", d);
		runtime.getGlobalVariables().define("$0", d);
		if (loadPath == null)
		{
			loadPath = System.getProperty("java.class.path");
		}
		List list = Arrays.asList(loadPath.split(File.pathSeparator));
		runtime.getLoadService().init(list);
		runtime.getLoadService().require("java");
	}

	private synchronized Object invokeImpl(final Object obj, String method,
			Object[] args, Class returnType) throws ScriptException
	{
		if (method == null)
		{
			throw new NullPointerException("method name is null");
		}
		GlobalVariables oldGlobals = runtime.getGlobalVariables();
		try
		{
			setWriterOutputStream(context.getWriter());
			setErrorWriter(context.getErrorWriter());
			setGlobalVariables(context);
			IRubyObject rubyRecv = obj != null ? JavaUtil.convertJavaToRuby(
					runtime, obj) : runtime.getTopSelf();

			IRubyObject result;
			if (args != null && args.length > 0)
			{
				IRubyObject[] rubyArgs = JavaUtil.convertJavaArrayToRuby(
						runtime, args);
				// Create Ruby proxies for any input arguments that are not
				// primitives.
				IRubyObject javaUtilities = runtime.getObject().getConstant(
						"JavaUtilities");
				for (int i = 0; i < rubyArgs.length; i++)
				{
					IRubyObject tmp = rubyArgs[i];
					if (tmp instanceof JavaObject)
					{
						rubyArgs[i] = javaUtilities.callMethod(runtime
								.getCurrentContext(), "wrap", tmp);
					}
				}
				result = rubyRecv.callMethod(runtime.getCurrentContext(),
						method, rubyArgs);
			} else
			{
				result = rubyRecv.callMethod(runtime.getCurrentContext(),
						method);
			}
			return rubyToJava(result, returnType);
		} catch (Exception exp)
		{
			throw new ScriptException(exp);
		} finally
		{
			try
			{
				JavaEmbedUtils.terminate(runtime);
			} catch (RaiseException e)
			{
				RubyException re = e.getException();
				runtime.printError(re);
				if (!runtime.fastGetClass("SystemExit").isInstance(re))
				{
					throw new ScriptException(e);
				}
			} finally
			{
				if (oldGlobals != null)
				{
					setGlobalVariables(oldGlobals);
				}
			}
		}
	}

	private void setKCode(String encoding)
	{
		KCode kcode = KCode.create(runtime, encoding);
		runtime.setKCode(kcode);
	}

	private void equalOutputs(RubyObject value)
	{
		runtime.getGlobalVariables().set("$>", value);
		runtime.getGlobalVariables().set("$defout", value);
	}

	private void setWriterOutputStream(Writer writer)
	{
		try
		{
			RubyIO dummy_io = new RubyIO(runtime, new PrintStream(
					new WriterOutputStream(new StringWriter())));
			runtime.getGlobalVariables().set("$stderr", dummy_io); // discard
																	// unwanted
																	// warnings
			RubyIO io = new RubyIO(runtime, new PrintStream(
					new WriterOutputStream(writer)));
			io.getOpenFile().getMainStream().setSync(true);
			runtime.defineGlobalConstant("STDOUT", io);
			runtime.getGlobalVariables().set("$>", io);
			runtime.getGlobalVariables().set("$stdout", io);
			runtime.getGlobalVariables().set("$defout", io);
		} catch (UnsupportedEncodingException exp)
		{
			throw new IllegalArgumentException(exp);
		}
	}

	private void setErrorWriter(Writer writer)
	{
		try
		{
			RubyIO dummy_io = new RubyIO(runtime, new PrintStream(
					new WriterOutputStream(new StringWriter())));
			runtime.getGlobalVariables().set("$stderr", dummy_io); // discard
																	// unwanted
																	// warnings
			RubyIO io = new RubyIO(runtime, new PrintStream(
					new WriterOutputStream(writer)));
			io.getOpenFile().getMainStream().setSync(true);
			runtime.defineGlobalConstant("STDERR", io);
			runtime.getGlobalVariables().set("$stderr", io);
			runtime.getGlobalVariables().set("$deferr", io);
		} catch (UnsupportedEncodingException exp)
		{
			throw new IllegalArgumentException(exp);
		}
	}

	private String getEncoding(Writer writer)
	{
		/*
		 * commented out to fix [issue 39] String enc =
		 * System.getProperty("sun.jnu.encoding"); if (enc != null) { return
		 * enc; }
		 */
		if (writer instanceof OutputStreamWriter)
		{
			return ((OutputStreamWriter) writer).getEncoding();
		}
		String enc;
		return ((enc = System.getProperty("file.encoding")) == null) ? "UTF-8"
				: enc;
	}

	private class WriterOutputStream extends OutputStream
	{

		private Writer writer;
		private CharsetDecoder decoder;

		private WriterOutputStream(Writer writer)
				throws UnsupportedEncodingException
		{
			this(writer, getEncoding(writer));
		}

		private WriterOutputStream(Writer writer, String enc)
				throws UnsupportedEncodingException
		{
			this.writer = writer;
			if (enc == null)
			{
				throw new UnsupportedEncodingException("encoding is " + enc);
			}
			try
			{
				decoder = Charset.forName(enc).newDecoder();
			} catch (Exception e)
			{
				throw new UnsupportedEncodingException("Unsupported: " + enc);
			}
			decoder.onMalformedInput(CodingErrorAction.REPLACE);
			decoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
		}

		@Override
		public void close() throws IOException
		{
			synchronized (writer)
			{
				decoder = null;
				writer.close();
			}
		}

		@Override
		public void flush() throws IOException
		{
			synchronized (writer)
			{
				writer.flush();
			}
		}

		@Override
		public void write(int b) throws IOException
		{
			byte[] buffer = new byte[1];
			write(buffer, 0, 1);
		}

		@Override
		public void write(byte[] buffer) throws IOException
		{
			write(buffer, 0, buffer.length);
		}

		@Override
		public void write(byte[] buffer, int offset, int length)
				throws IOException
		{
			synchronized (writer)
			{
				if (offset < 0 || offset > buffer.length - length || length < 0)
				{
					throw new IndexOutOfBoundsException();
				}
				if (length == 0)
				{
					return;
				}
				ByteBuffer bytes = ByteBuffer.wrap(buffer, offset, length);
				CharBuffer chars = CharBuffer.allocate(length);
				convert(bytes, chars);
				char[] cbuf = new char[chars.length()];
				chars.get(cbuf, 0, chars.length());
				writer.write(cbuf);
				writer.flush();
			}
		}

		private void convert(ByteBuffer bytes, CharBuffer chars)
				throws IOException
		{
			decoder.reset();
			chars.clear();
			CoderResult result = decoder.decode(bytes, chars, true);
			if (result.isError() || result.isOverflow())
			{
				throw new IOException(result.toString());
			} else if (result.isUnderflow())
			{
				chars.flip();
			}
		}
	}
}