/*
 * @(#)FilenameDocument.java  
 * 
 * Copyright (c) 2010 Werner Randelshofer, Switzerland.
 * All rights reserved.
 * 
 * You may not use, copy or modify this file, except in compliance with the
 * license agreement you entered into with Werner Randelshofer.
 * For details see accompanying license terms.
 */
package ch.randelshofer.quaqua.filechooser;

import ch.randelshofer.quaqua.QuaquaManager;
import java.util.HashSet;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 * A document model which silently converts forbidden filename characters
 * into dashes.
 * <p>
 * On Mac OS X, only the colon character is forbidden: {@code : }.
 * <p>
 * On Windows, the following characters are forbidden: {@code \ / : * ? " < > | }
 * <p>
 * On other operating systems, no characters are converted.
 *
 * @author Werner Randelshofer
 * @version $Id: FilenameDocument.java 458 2013-05-29 15:44:33Z wrandelshofer $
 */
public class FilenameDocument extends PlainDocument {

    private HashSet<Character> forbidden;

    public FilenameDocument() {
        forbidden = new HashSet<Character>();
        int os = QuaquaManager.getOS();
        if (os >= QuaquaManager.CHEETAH) {
            forbidden.add(':');
        } else if (os == QuaquaManager.WINDOWS) {
            forbidden.add('\\');
            forbidden.add('/');
            forbidden.add(':');
            forbidden.add('*');
            forbidden.add('?');
            forbidden.add('"');
            forbidden.add('<');
            forbidden.add('>');
        }
    }

    @Override
    public void insertString(int offs, String str, AttributeSet a)
            throws BadLocationException {
        char[] chars = str.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (forbidden.contains(chars[i])) {
                chars[i] = '-';
            }
        }

        super.insertString(offs, new String(chars), a);
    }
}
