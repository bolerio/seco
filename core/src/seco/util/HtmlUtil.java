package seco.util;

import java.awt.Color;
import java.awt.Font;

import seco.notebook.syntax.Chunk;

public class HtmlUtil
{
    public final static String toCSS(Font font)
    {
        return String.format("font-family: \"%s\"; %s; %s;", font.getFamily(),
                toSizeCSS(font), toStyleCSS(font));
    }

    public final static String toSizeCSS(Font font)
    {
        return String.format("font-size: %s px", (int) (font.getSize() * .75)); // converts
                                                                                // to
                                                                                // pixels
                                                                                // with
                                                                                // standard
                                                                                // DPI
    }

    public final static String toStyleCSS(Font font)
    {
        switch (font.getStyle())
        {
        case Font.ITALIC:
            return "font-style : italic";
        case Font.BOLD:
            return "font-weight: bold";
        default:
            return "font-weight: normal";
        }
    }

    public final static String toCSS(Color color)
    {
        return String.format("color: #%s;", new Object[] { toHex(color) });
    }

    public static final String toHex(Color color)
    {
        color = color == null ? Color.BLACK : color;
        String rgb = Integer.toHexString(color.getRGB());
        return rgb.substring(2, rgb.length());
    }

    public static String escapeToHTMLString(String string,
            boolean handle_newlines)
    {
        StringBuffer sb = new StringBuffer(string.length());
        // true if last char was blank
        boolean lastWasBlankChar = false;
        int len = string.length();
        char c;

        for (int i = 0; i < len; i++)
        {
            c = string.charAt(i);
            if (c == ' ')
            {
                // blank gets extra work,
                // this solves the problem you get if you replace all
                // blanks with &nbsp;, if you do that you loss
                // word breaking
                if (lastWasBlankChar)
                {
                    lastWasBlankChar = false;
                    sb.append("&nbsp;");
                }
                else
                {
                    lastWasBlankChar = true;
                    sb.append(' ');
                }
            }
            else
            {
                lastWasBlankChar = false;
                //
                // HTML Special Chars
                if (c == '"') sb.append("&quot;");
                else if (c == '&') sb.append("&amp;");
                else if (c == '<') sb.append("&lt;");
                else if (c == '>') sb.append("&gt;");
                else if (handle_newlines && c == '\n')
                // Handle Newline
                sb.append("&lt;br/&gt;");
                else
                {
                    int ci = 0xffff & c;
                    if (ci < 160)
                    // nothing special only 7 Bit
                    sb.append(c);
                    else
                    {
                        // Not 7 Bit use the unicode system
                        sb.append("&#");
                        sb.append(new Integer(ci).toString());
                        sb.append(';');
                    }
                }
            }
        }
        return sb.toString();
    }

    public static String span(Chunk chunks)
    {
        return "<span style=' " + toCSS(chunks.style.getFont()) + " "
                + toCSS(chunks.style.getForegroundColor()) + "'>"
                + escapeToHTMLString(chunks.str, false) + "</span>";
    }

    private HtmlUtil()
    {
    }
}
