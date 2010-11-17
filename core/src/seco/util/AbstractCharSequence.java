/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.util;


/**
 * Abstract implementation of character sequence
 * with {@link String}-like implementation
 * of <CODE>hashCode()</CODE> and <CODE>equals()</CODE>.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public abstract class AbstractCharSequence implements CharSequence {
    
    /**
     * Returns the length of this character sequence.  The length is the number
     * of 16-bit Unicode characters in the sequence. </p>
     *
     * @return  the number of characters in this sequence
     */
    public abstract int length();

    /**
     * Returns the character at the specified index.  An index ranges from zero
     * to <tt>length() - 1</tt>.  The first character of the sequence is at
     * index zero, the next at index one, and so on, as for array
     * indexing. </p>
     *
     * @param   index   the index of the character to be returned
     *
     * @return  the specified character
     *
     * @throws  IndexOutOfBoundsException
     *          if the <tt>index</tt> argument is negative or not less than
     *          <tt>length()</tt>
     */
    public abstract char charAt(int index);


    private String toString(int start, int end) {
        return CharSequenceUtilities.toString(this, start, end);
    }

    public CharSequence subSequence(int start, int end) {
        return toString(start, end);
    }

    public String toString() {
        return toString(0, length());
    }
    
    /**
     * Subclass providing string-like implementation
     * of <code>hashCode()</code> and <code>equals()</code>
     * method accepting strings with the same content
     * like charsequence has.
     * <br>
     * This makes the class suitable for matching to strings
     * e.g. in maps.
     * <br>
     * <b>NOTE</b>: Matching is just uni-directional
     * i.e. charsequence.equals(string) works
     * but string.equals(charsequence) does not.
     */
    public static abstract class StringLike extends AbstractCharSequence {

        public int hashCode() {
            return CharSequenceUtilities.stringLikeHashCode(this);
        }

        public boolean equals(Object o) {
            return CharSequenceUtilities.equals(this, o);
        }
        
    }

}
