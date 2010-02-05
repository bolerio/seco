/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
/*
 * SimplyHTML, a word processor based on Java, HTML and CSS
 * Copyright (C) 2002 Ulrich Hilger
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package seco.notebook.html;

import javax.swing.*;
import java.awt.*;

/**
 * An <code>ImagePreview</code> is a component to preview
 * GIF and JPEG images.
 *
 * <p>The preview adapts (shrinks) images to its size upon their
 * assignment. When the preview is resized, its image is adapted again.</p>
 *
 * <p>Alternately, when altering the image width, height and scale properties
 * with respective setters, the image is resized within the preview without
 * the preview itself adapting in size.</p>
 *
 * <p>Scroll bars are displayed as appropriate.</p>
 *
 * @author Ulrich Hilger
 * @author Light Development
 * @author <a href="http://www.lightdev.com">http://www.lightdev.com</a>
 * @author <a href="mailto:info@lightdev.com">info@lightdev.com</a>
 * @author published under the terms and conditions of the
 *      GNU General Public License,
 *      for details see file gpl.txt in the distribution
 *      package of this software
 *
 * @version stage 11, April 27, 2003
 */
public class ImagePreview extends JComponent implements Scrollable {

  /**
   * scroll increment (for Scrollable implementation)
   */
  private int maxUnitIncrement = 1;

  /**
   * the image to be previewed
   */
  private ImageIcon pic;

  /**
   * Construct an <CODE>ImagePreview</CODE>.
   *
   * @param  pic - the image to be previewed
   */
  public ImagePreview(ImageIcon pic) {
    setImage(pic);
  }

  /**
   * Construct an <CODE>ImagePreview</CODE> without an image
   * associated.
   */
  public ImagePreview() {
    this(null);
  }

  /**
   * Get the original width of the image previewed in this component
   *
   * @return  the original width of the image previewed in this component
   *             or -1 if no image is assigned
   */
  public int getOriginalWidth() {
    if(pic != null) {
      return pic.getIconWidth();
    }
    else {
      return -1;
    }
  }

  /**
   * Get the original height of the image previewed in this component
   *
   * @return  the original height of the image previewed in this component
   *             or -1 if no image is assigned
   */
  public int getOriginalHeight() {
    if(pic != null) {
      return pic.getIconHeight();
    }
    else {
      return -1;
    }
  }

  /**
   * Set the image to be previewed.
   */
  public void setImage(ImageIcon pic) {
    this.pic = pic;
    if(pic != null) {
      this.getGraphics().clearRect(0, 0, getWidth(), getHeight());
      this.paint(this.getGraphics());
    }
  }

  /**
   * Paints this component.
   * If the image associated with this component is smaller than the size
   * of the component, the image is painted in its original size. Otherwise,
   * the image is scaled down to the size of this component.
   *
   * @param   g - The graphics context to use for painting.
   */
  public void paint(Graphics g) {
    if(pic != null) {
      int dWidth = pic.getIconWidth();
      int dHeight = pic.getIconHeight();
      int scale = getScale();
      dWidth = dWidth * scale / 100;
      dHeight = dHeight * scale/ 100;
      g.drawImage(  pic.getImage(),
                    0,
                    0,
                    dWidth,
                    dHeight,
                    this);
    }
  }

  /**
   * Gets the size adjustment necessary for the image to fit into this
   * component and returns the resulting scale percentage.
   *
   * @return  the scale percentage of the image
   */
  public int getScale() {
    int scale = 100;
    if(pic != null) {
      int vPct = 100;
      int hPct = 100;
        Dimension ps = getPreferredSize();
        hPct = (int) ((double) ps.getWidth() /
                      ((double) pic.getIconWidth() / (double) 100));
        //System.out.println("ImagePreview getScale ps.getWidth " + ps.getWidth());
        //System.out.println("ImagePreview getScale pic.getIconWidth() " + pic.getIconWidth());
        //System.out.println("ImagePreview getScale hPct " + hPct + "\r\n\r\n");
        vPct = (int) ((double) ps.getHeight() /
                      ((double) pic.getIconHeight() / (double) 100));
        //System.out.println("ImagePreview getScale ps.getHeight() " + ps.getHeight());
        //System.out.println("ImagePreview getScale pic.getIconHeight() " + pic.getIconHeight());
        //System.out.println("ImagePreview getScale vPct " + vPct + "\r\n\r\n");
        if(hPct < vPct) {
          scale = hPct;
        }
        else {
          scale = vPct;
        }
    }
    //System.out.println("ImagePreview getScale=" + scale + "\r\n\r\n");
    return scale;
  }

  /**
   * set the preview to a new width maintaining the image proportions
   *
   * @param  newWidth   the new width for the image preview
   */
  public void setPreviewWidth(int newWidth) {
    //System.out.println("ImagePreview setPreviewWidth newWidth=" + newWidth);
    if(pic != null) {
      try {
        int hPct = (int) ((double) newWidth / (double) ((double) getOriginalWidth() / (double) 100));
        int newHeight = getOriginalHeight() * hPct / 100;
        setPreferredSize(new Dimension(newWidth, newHeight));
      }
      catch(Exception e) {
        e.printStackTrace();
        setPreferredSize(new Dimension(20, 20));
      }
      revalidate();
    }
  }

  /**
   * set the preview to a new height maintaining the image proportions
   *
   * @param  newHeight   the new height for the image preview
   */
  public void setPreviewHeight(int newHeight) {
    if( pic != null) {
      try {
        int vPct = (int) ((double) newHeight / ((double) getOriginalHeight() / (double) 100));
        int newWidth = getOriginalWidth() * vPct / 100;
        setPreferredSize(new Dimension(newWidth, newHeight));
      }
      catch(Exception e) {
        e.printStackTrace();
        setPreferredSize(new Dimension(20, 20));
      }
      revalidate();
    }
  }

  /**
   * Adapt the size of the image previewed by this component to a new
   * scale.
   *
   * @param  newScale   the new scale the image shall adapt to in size
   */
  public void setScale(int newScale) {
    int newWidth;
    int newHeight;
    newWidth = getOriginalWidth() * newScale / 100 ;
    newHeight = getOriginalHeight() * newScale / 100;
    setPreferredSize(new Dimension(newWidth, newHeight));
    revalidate();
  }

  /*
    ------------ Scrollable implementation start ----------------------
  */
  public Dimension getPreferredScrollableViewportSize() {
      return getPreferredSize();
  }

  public int getScrollableUnitIncrement(Rectangle visibleRect,
                                        int orientation,
                                        int direction) {
      //Get the current position.
      int currentPosition = 0;
      if (orientation == SwingConstants.HORIZONTAL)
          currentPosition = visibleRect.x;
      else
          currentPosition = visibleRect.y;

      //Return the number of pixels between currentPosition
      //and the nearest tick mark in the indicated direction.
      if (direction < 0) {
          int newPosition = currentPosition -
                           (currentPosition / maxUnitIncrement) *
                            maxUnitIncrement;
          return (newPosition == 0) ? maxUnitIncrement : newPosition;
      } else {
          return ((currentPosition / maxUnitIncrement) + 1) *
                 maxUnitIncrement - currentPosition;
      }
  }

  public int getScrollableBlockIncrement(Rectangle visibleRect,
                                         int orientation,
                                         int direction) {
      if (orientation == SwingConstants.HORIZONTAL)
          return visibleRect.width - maxUnitIncrement;
      else
          return visibleRect.height - maxUnitIncrement;
  }

  public boolean getScrollableTracksViewportWidth() {
      return false;
  }

  public boolean getScrollableTracksViewportHeight() {
      return false;
  }

  public void setMaxUnitIncrement(int pixels) {
      maxUnitIncrement = pixels;
  }
  /*
    --------- Scrollable implementation end ---------------------------
  */
}
