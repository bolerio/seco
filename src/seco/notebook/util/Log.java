/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.notebook.util;

import java.io.*;
import java.util.Date;
import java.text.SimpleDateFormat;

/**
 * This is a simple logging class.
 */
public final class Log
{
	private static final int TRACE_LEVEL = 1;
	private static final boolean  ENABLED = false;
	private final static String filename = null;
		//"nb_log" + new SimpleDateFormat("ddMMyy").format(new Date());

	public static void start()
	{
		Info();
		Trace("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%Begin session");
	}
	
	public static void end()
	{
		Trace("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%End session");
	}
	
	public static void Info()
	{
		System.out.println("Logging in: " + ((filename != null) ?
				        new File(filename).getAbsolutePath()
						: "console"));
	}

	private static void write(final String s)
	{
		if (filename == null)
		{
			System.out.println(s);
			return;
		}
		final String fn = filename;
		try
		{
			RandomAccessFile f = new RandomAccessFile(fn, "rw");
			f.seek(f.length());
			f.writeBytes("\r\n" + s + "\r\n");
			f.close();
		}
		catch (IOException ioe)
		{
			System.out.println("TICL error, while trying to write to log file:"
					+ ioe.toString());
		}
	}

	/**
	 * Trace a specified string regardles of the current tracing level.
	 * 
	 * @param s the <code>String</code> to trace.
	 */
	public static void Trace(String s)
	{
		Trace(1, s);
	}

	/**
	 * Trace a specified string only if the current tracing level is greater
	 * than or equal to the specified level.
	 * 
	 * @param level The tracing level of this "trace".
	 * @param s The string to trace.
	 */
	public static void Trace(int level, String s)
	{
		if(ENABLED && level <= TRACE_LEVEL)
		   write(s);
	}

	/**
	 * Write a warning. A warning is always written, regardless of the current
	 * tracing level.
	 * 
	 * @param s The warning's text.
	 */
	public static void Warning(String s)
	{
		write("NB Warning: " + s);
	}

	/**
	 * Write an error. An error is always written, regardless of the current
	 * tracing level.
	 * 
	 * @param s The error's text.
	 */
	public static void Error(String s)
	{
		write("NB Error: " + s);
	}

	public static void writeTime()
	{
		write((new SimpleDateFormat("MM/dd HH:mm:ss")).format(new Date()));
	}
}
