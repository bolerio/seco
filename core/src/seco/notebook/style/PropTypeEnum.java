/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.notebook.style;

import java.awt.Color;
import java.util.StringTokenizer;


/**
 * Simple enum defining the property types supported by <code>NBStyle</code> 
 */
public enum PropTypeEnum
{

    COLOR, FONT, NUMBER, STRING;

    public String toString(Object o)
    {
        switch (this)
        {
        case COLOR:
            return getRGBText((Color) o);
        case FONT:
            return ((FontEx) o).toString();
        case NUMBER:
            return o.toString();
        case STRING:
            return "" + o;
        default:
            throw new AssertionError();
        }
    }

    public Object fromString(String str)
    {
        switch (this)
        {
        case COLOR:
            return parseRGBText(str);
        case FONT:
            return FontEx.fromString(str);
        case NUMBER:
            return Double.parseDouble(str);
        case STRING:
            return str;
        default:
            throw new AssertionError();
        }
    }

    static Color parseRGBText(String text)
    {
        StringTokenizer strtok = new StringTokenizer(text, ",");
        if (strtok.countTokens() != 3)
        {
            System.err.println("illegal RGB string: " + text);
            return Color.black;
        }

        String red = strtok.nextToken().trim();
        String green = strtok.nextToken().trim();
        String blue = strtok.nextToken().trim();

        try
        {
            int r = Integer.parseInt(red);
            int g = Integer.parseInt(green);
            int b = Integer.parseInt(blue);
            return new Color(r, g, b);
        }
        catch (NumberFormatException e)
        {
            return Color.black;
        }

    }

    static String getRGBText(Color color)
    {
        Integer red = new Integer(color.getRed());
        Integer green = new Integer(color.getGreen());
        Integer blue = new Integer(color.getBlue());
        return new String(red.toString() + "," + green.toString() + ","
                + blue.toString());
    }
}
