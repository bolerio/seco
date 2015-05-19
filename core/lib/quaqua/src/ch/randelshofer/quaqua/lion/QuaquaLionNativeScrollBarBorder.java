/*
 * @(#)QuaquaLionNativeScrollBarBorder.java  
 * 
 * Copyright (c) 2011-2013 Werner Randelshofer, Switzerland.
 * All rights reserved.
 * 
 * You may not use, copy or modify this file, except in compliance with the
 * license agreement you entered into with Werner Randelshofer.
 * For details see accompanying license terms.
 */
package ch.randelshofer.quaqua.lion;

import javax.swing.JScrollBar;
import java.awt.Graphics;
import java.awt.Component;
import ch.randelshofer.quaqua.QuaquaUtilities;
import ch.randelshofer.quaqua.border.QuaquaNativeBorder;
import java.awt.Insets;
import javax.swing.SwingConstants;
import static ch.randelshofer.quaqua.osx.OSXAquaPainter.*;

/**
 * {@code QuaquaLionNativeScrollBarBorder}.
 *
 * @author Werner Randelshofer
 * @version $Id: QuaquaLionNativeScrollBarBorder.java 458 2013-05-29 15:44:33Z wrandelshofer $
 */
public class QuaquaLionNativeScrollBarBorder extends QuaquaNativeBorder {

    public QuaquaLionNativeScrollBarBorder(Widget widget, Insets imageInsets, Insets borderInsets) {
        super(0,widget, imageInsets, borderInsets);
    }

    public QuaquaLionNativeScrollBarBorder(Widget widget) {
        super(0,widget);
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        JScrollBar sb = (JScrollBar) c;

        Insets vm = getVisualMargin(c);
        x += vm.left;
        y += vm.top;
        width -= vm.left + vm.right;
        height -= vm.top + vm.bottom;

        int args = 0;
        State state;
        if (QuaquaUtilities.isOnActiveWindow(c)) {
            state = State.active;
            args |= 1 << ARG_ACTIVE;
        } else {
            state = State.inactive;
        }
        if (!c.isEnabled()) {
            state = State.disabled;
            args |= 1 << ARG_DISABLED;
        }
        painter.setState(state);

        boolean isFocused = QuaquaUtilities.isFocused(c);
        args |= (isFocused) ? 1 << ARG_FOCUSED : 0;
        painter.setValueByKey(Key.focused, isFocused ? 1 : 0);

        Size size;

        switch (QuaquaUtilities.getSizeVariant(c)) {
            case REGULAR:
            default:
                size = Size.regular;
                break;
            case SMALL:
                size = Size.small;
                break;
            case MINI:
                size = Size.mini;
                break;

        }
        painter.setSize(size);
        args |= size.getId() << ARG_SIZE_VARIANT;


        if (sb.getOrientation() == SwingConstants.HORIZONTAL) {
            painter.setOrientation(Orientation.horizontal);
            args |= 1 << ARG_ORIENTATION;
        } else {
            painter.setOrientation(Orientation.vertical);
        }

        if (sb.getMaximum() != sb.getMinimum()) {
            double totalSize = (double) (sb.getMaximum() - sb.getMinimum());
            painter.setValueByKey(Key.thumbProportion, sb.getVisibleAmount() / totalSize);
            painter.setValueByKey(Key.value, (sb.getValue()- sb.getMinimum())/(totalSize));
        } else {
            painter.setValueByKey(Key.thumbProportion, 0);
        }

        paint(c, g, x, y, width, height, args);
    }
}
