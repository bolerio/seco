/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.util;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.Segment;


/**
 * Various utility methods related to swing text documents.
 *
 * @author Miloslav Metelka
 * @since 1.4
 */

public final class DocumentUtilities {
    
    private static final Object TYPING_MODIFICATION_DOCUMENT_PROPERTY = new Object();
    
    private DocumentUtilities() {
        // No instances
    }

   
   
    /**
     * Mark that the ongoing document modification(s) will be caused
     * by user's typing.
     * It should be used by default-key-typed-action and the actions
     * for backspace and delete keys.
     * <br/>
     * The document listeners being fired may
     * query it by using {@link #isTypingModification(Document)}.
     * This method should always be used in the following pattern:
     * <pre>
     * DocumentUtilities.setTypingModification(doc, true);
     * try {
     *     doc.insertString(offset, typedText, null);
     * } finally {
     *    DocumentUtilities.setTypingModification(doc, false);
     * }
     * </pre>
     *
     * @see #isTypingModification(Document)
     */
    public static void setTypingModification(Document doc, boolean typingModification) {
        doc.putProperty(TYPING_MODIFICATION_DOCUMENT_PROPERTY, Boolean.valueOf(typingModification));
    }
    
    /**
     * @deprecated
     * @see #isTypingModification(Document)
     */
    public static boolean isTypingModification(DocumentEvent evt) {
        Boolean b = (Boolean)evt.getDocument().getProperty(TYPING_MODIFICATION_DOCUMENT_PROPERTY);
        return (b != null) ? b.booleanValue() : false;
    }

    /**
     * This method should be used to check whether
     * the lastly performed document modification was caused by user's typing.
     * <br/>
     * Certain functionality such as code completion or code templates
     * may benefit from that information. For example the java code completion
     * should only react to the typed "." but not if the same string was e.g.
     * pasted from the clipboard.
     *
     * @see #setTypingModification(Document, boolean)
     */
    public static boolean isTypingModification(Document doc) {
        Boolean b = (Boolean)doc.getProperty(TYPING_MODIFICATION_DOCUMENT_PROPERTY);
        return (b != null) ? b.booleanValue() : false;
    }

    /**
     * Get text of the given document as char sequence.
     * <br>
     *
     * @param doc document for which the charsequence is being obtained.
     * @return non-null character sequence.
     *  <br>
     *  The returned character sequence should only be accessed under
     *  document's readlock (or writelock).
     */
    public static CharSequence getText(Document doc) {
        CharSequence text = (CharSequence)doc.getProperty(CharSequence.class);
        if (text == null) {
            text = new DocumentCharSequence(doc);
            doc.putProperty(CharSequence.class, text);
        }
        return text;
    }
    
    /**
     * Get a portion of text of the given document as char sequence.
     * <br>
     *
     * @param doc document for which the charsequence is being obtained.
     * @param offset starting offset of the charsequence to obtain.
     * @param length length of the charsequence to obtain
     * @return non-null character sequence.
     * @exception BadLocationException  some portion of the given range
     *   was not a valid part of the document.  The location in the exception
     *   is the first bad position encountered.
     *  <br>
     *  The returned character sequence should only be accessed under
     *  document's readlock (or writelock).
     */
    public static CharSequence getText(Document doc, int offset, int length) throws BadLocationException {
        CharSequence text = (CharSequence)doc.getProperty(CharSequence.class);
        if (text == null) {
            text = new DocumentCharSequence(doc);
            doc.putProperty(CharSequence.class, text);
        }
        try {
            return text.subSequence(offset, offset + length);
        } catch (IndexOutOfBoundsException e) {
            int badOffset = offset;
            if (offset >= 0 && offset + length > text.length()) {
                badOffset = length;
            }
            throw new BadLocationException(e.getMessage(), badOffset);
        }
    }
    
   
    /**
     * Implementation of the character sequence for a generic document
     * that does not provide its own implementation of character sequence.
     */
    private static final class DocumentCharSequence extends AbstractCharSequence.StringLike {
        
        private final Segment segment = new Segment();
        
        private final Document doc;
        
        DocumentCharSequence(Document doc) {
            this.doc = doc;
        }

        public int length() {
            return doc.getLength();
        }

        public synchronized char charAt(int index) {
            try {
                doc.getText(index, 1, segment);
            } catch (BadLocationException e) {
                throw new IndexOutOfBoundsException(e.getMessage()
                    + " at offset=" + e.offsetRequested()); // NOI18N
            }
            return segment.array[segment.offset];
        }

    }
    
   
}

