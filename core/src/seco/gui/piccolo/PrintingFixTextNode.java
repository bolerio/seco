/**********************************************************************
 * License information (the MIT license)
 *
 * Copyright (c) 2004 Ihab A.B. Awad; Stanford University
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation files
 * (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge,
 * publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT.  IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *********************************************************************/

package seco.gui.piccolo;

import java.awt.FontMetrics;
import java.awt.font.LineMetrics;

import edu.umd.cs.piccolo.nodes.PText;
import edu.umd.cs.piccolo.util.PPaintContext;

/**
 * This is a version of a Piccolo <code>PText</code> node that takes control
 * of its own rendering behavior when printing.</p>
 * 
 * <p>This class is necessary because the {@link java.awt.Graphics2D}implementation we use
 * for printing, contributed by the FreeHEP Java Library, does not implement
 * {@link java.awt.Graphics2D#drawGlyphVector(GlyphVector,float,float)}.</p>
 * 
 * <p>The special rendering behavior is such that instances of this class obtain
 * font metrics directly, and call the
 * {@link java.awt.Graphics2D#drawString(String,float,float)} method, rather than using
 * the default <code>PText</code> implementation, which calls
 * {@link java.awt.Graphics2D#drawGlyphVector(GlyphVector,float,float)}.</p>
 * 
 * <p>A programmer instructs instances of this class to use the "special" printing
 * behavior by setting the client property
 * {@link #PRINTING_CLIENT_PROPERTY_KEY} to some non- <code>null</code> value
 * on the {@link edu.umd.cs.piccolo.PCamera} associated with the current rendering operation.</p>
 * 
 * @author Ihab A.B. Awad
 * @version $Id: PrintingFixTextNode.java,v 1.4 2004/04/23 01:25:09 rowanxmas Exp $
 */

public class PrintingFixTextNode extends PText {

  public static final String PRINTING_CLIENT_PROPERTY_KEY =
    PrintingFixTextNode.class.getName() + ".isPrinting";
    
  public PrintingFixTextNode ( String text ) {
    super( text );
  }

  public void paint ( PPaintContext pc ) {
        
    if (  pc.getCamera() == null ) {
      // sometimes the camera is null when printing to a printer
      super.paint(pc);
    } else if ( pc.getCamera().getClientProperty(PRINTING_CLIENT_PROPERTY_KEY) != null ) {
      printingFixPaint(pc);
    } else {
      super.paint(pc);
    }
  }

  private void printingFixPaint(PPaintContext pc) {

    FontMetrics fm = pc.getGraphics().getFontMetrics(getFont());
    LineMetrics lm = fm.getLineMetrics(getText(), pc.getGraphics());
        
    double lineHeight = lm.getAscent() + lm.getDescent();
    //double verticalPad = (getHeight() - lineHeight) / 2.0;
    double yBaseline = getY() + (double)lm.getAscent();//+ (float)lineHeight;// + verticalPad + (double)lm.getAscent();

    pc.getGraphics().setPaint( getTextPaint() );

    // Fix for reckognizing NewLines "/n"
    String text = getText();
    String[] lines = text.split("\\n");
    for ( int i = 0; i < lines.length; ++i ) {
      pc.getGraphics().drawString( lines[i], (float)getX(), (float)yBaseline + ( (float)lineHeight * i ));
    }
    //pc.getGraphics().drawString(getText(), (float)getX(), (float)yBaseline);
  }
    
}
