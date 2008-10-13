/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.classloader;

/**
 * 
 * <p>
 * This interface represents an entity that is capable of retrieving a Java class
 * by its name.
 * </p>
 *
 * @author Borislav Iordanov
 *
 */
public interface ClassFinder
{
	ClassInfo findClassInfo();
}
