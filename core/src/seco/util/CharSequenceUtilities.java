/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.util;

/**
 * Utility methods related to character sequences.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class CharSequenceUtilities {
    
    private CharSequenceUtilities() {
        // no instances
    }

    /**
     * Compute {@link String}-like hashcode over given {@link CharSequence}.
     *
     * @param text character sequence for which the hashcode is being computed.
     * @return hashcode of the given character sequence.
     */
    public static int stringLikeHashCode(CharSequence text) {
        int len = text.length();

        int h = 0;
        for (int i = 0; i < len; i++) {
            h = 31 * h + text.charAt(i);
        }
        return h;
    }

    /**
     * Method that compares a given character sequence to another object.
     * The match is successful if the other object is a character sequence as well
     * and both character sequences contain the same characters.
     *
     * @param text character sequence being compared to the given object.
     *  It must not be <code>null</code>.
     * @param o object to be compared to the character sequence.
     *  It can be <code>null</code>.
     * @return true if both parameters are non-null
     *  and they are equal in String-like manner.
     */
    public static boolean equals(CharSequence text, Object o) {
        if (text == o) {
            return true;
        }

        if (o instanceof CharSequence) { // both non-null
            CharSequence text2 = (CharSequence)o;
            int len = text.length();
            if (len == text2.length()) {
                for (int i = len - 1; i >= 0; i--) {
                    if (text.charAt(i) != text2.charAt(i)) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }
    
    /**
     * Test whether whether the given character sequences
     * represent the same text.
     * <br>
     * The match is successful if the contained characters
     * of the two character sequences are the same.
     *
     * @param text1 first character sequence being compared.
     *  It must not be <code>null</code>.
     * @param o object to be compared to the character sequence.
     *  It must not be <code>null</code>.
     * @return true if both parameters are equal in String-like manner.
     */
    public static boolean textEquals(CharSequence text1, CharSequence text2) {
        if (text1 == text2) {
            return true;
        }
        int len = text1.length();
        if (len == text2.length()) {
            for (int i = len - 1; i >= 0; i--) {
                if (text1.charAt(i) != text2.charAt(i)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
    
    /**
     * Create a string from the given character sequence by first creating
     * a <code>StringBuffer</code> and appending the whole character sequence
     * char-by-char.
     * <br>
     * The method does not call <code>toString()</code> on the given character
     * sequence.
     *
     * @param text character sequence for which the <code>String</code> form
     *  should be created.
     * @return string representation of the character sequence.
     */
    public static String toString(CharSequence text) {
        StringBuffer sb = new StringBuffer(text.length());
        append(sb, text);
        return sb.toString();
    }

    public static String toString(CharSequence text, int start, int end) {
        if (start < 0) {
            throw new IndexOutOfBoundsException("start=" + start + " < 0"); // NOI18N
        }
        if (end < start) {
            throw new IndexOutOfBoundsException("end=" + end + " < start=" + start); // NOI18N
        }
        if (end > text.length()) {
            throw new IndexOutOfBoundsException("end=" + end + " > length()=" + text.length()); // NOI18N
        }

        StringBuffer sb = new StringBuffer(end - start);
        while (start < end) {
            sb.append(text.charAt(start++));
        }
        return sb.toString();
    }
    
    public static void append(StringBuffer sb, CharSequence text) {
        int textLength = text.length();
        for (int i = 0; i < textLength; i++) {
            sb.append(text.charAt(i));
        }
    }
    
    public static void append(StringBuffer sb, CharSequence text, int start, int end) {
        if (start < 0) {
            throw new IndexOutOfBoundsException("start=" + start + " < 0"); // NOI18N
        }
        if (end < start) {
            throw new IndexOutOfBoundsException("end=" + end + " < start=" + start); // NOI18N
        }
        if (end > text.length()) {
            throw new IndexOutOfBoundsException("end=" + end + " > length()=" + text.length()); // NOI18N
        }

        while (start < end) {
            sb.append(text.charAt(start++));
        }
    }

    public static int indexOf(CharSequence text, int ch) {
	return indexOf(text, ch, 0);
    }

    public static int indexOf(CharSequence text, int ch, int fromIndex) {
	int length = text.length();
	while (fromIndex < length) {
	    if (text.charAt(fromIndex) == ch) {
		return fromIndex;
	    }
            fromIndex++;
	}
	return -1;
    }
    
    public static int indexOf(CharSequence text, CharSequence seq) {
        return indexOf(text, seq, 0);
    }
    
    public static int indexOf(CharSequence text, CharSequence seq, int fromIndex) {
        int textLength = text.length();
        int seqLength = seq.length();
	if (fromIndex >= textLength) {
            return (seqLength == 0 ? textLength : -1);
	}
    	if (fromIndex < 0) {
    	    fromIndex = 0;
    	}
	if (seqLength == 0) {
	    return fromIndex;
	}

        char first = seq.charAt(0);
        int max = textLength - seqLength;

        for (int i = fromIndex; i <= max; i++) {
            // look for first character
            if (text.charAt(i) != first) {
                while (++i <= max && text.charAt(i) != first);
            }

            // found first character, now look at the rest of seq
            if (i <= max) {
                int j = i + 1;
                int end = j + seqLength - 1;
                for (int k = 1; j < end && text.charAt(j) == seq.charAt(k); j++, k++);
                if (j == end) {
                    // found whole sequence
                    return i;
                }
            }
        }
        return -1;
    }
    
    public static int lastIndexOf(CharSequence text, CharSequence seq) {
        return lastIndexOf(text, seq, text.length());
    }
    
    public static int lastIndexOf(CharSequence text, CharSequence seq, int fromIndex) {
        int textLength = text.length();
        int seqLength = seq.length();
        int rightIndex = textLength - seqLength;
	if (fromIndex < 0) {
	    return -1;
	}
	if (fromIndex > rightIndex) {
	    fromIndex = rightIndex;
	}
	// empty string always matches
	if (seqLength == 0) {
	    return fromIndex;
	}

        int strLastIndex = seqLength - 1;
	char strLastChar = seq.charAt(strLastIndex);
	int min = seqLength - 1;
	int i = min + fromIndex;

    startSearchForLastChar:
	while (true) {
	    while (i >= min && text.charAt(i) != strLastChar) {
		i--;
	    }
            
	    if (i < min) {
		return -1;
	    }
	    int j = i - 1;
	    int start = j - (seqLength - 1);
	    int k = strLastIndex - 1;

	    while (j > start) {
	        if (text.charAt(j--) != seq.charAt(k--)) {
		    i--;
		    continue startSearchForLastChar;
		}
	    }
	    return start + 1;
	}
    }
    
    public static int lastIndexOf(CharSequence text, int ch) {
	return lastIndexOf(text, ch, text.length() - 1);
    }

    public static int lastIndexOf(CharSequence text, int ch, int fromIndex) {
        if (fromIndex > text.length() - 1) {
            fromIndex = text.length() - 1;
        }
	while (fromIndex >= 0) {
	    if (text.charAt(fromIndex) == ch) {
		return fromIndex;
	    }
            fromIndex--;
	}
	return -1;
    }

    public static boolean startsWith(CharSequence text, CharSequence prefix) {
        int p_length = prefix.length();
        if (p_length > text.length()) {
            return false;
        }
        for (int x = 0; x < p_length; x++) {
            if (text.charAt(x) != prefix.charAt(x))
                return false;
        }
        return true;
    }
    
    public static boolean endsWith(CharSequence text, CharSequence suffix) {
        int s_length = suffix.length();
        int text_length = text.length();
        if (s_length > text_length) {
            return false;
        }
        for (int x = 0; x < s_length; x++) {
            if (text.charAt(text_length - s_length + x) != suffix.charAt(x))
                return false;
        }
        return true;
    }
    
    public static CharSequence trim(CharSequence text) {
        int length = text.length();
        if (length == 0)
            return text;
        int start = 0;
        int end = length - 1;
        while (start < length && text.charAt(start) <= ' ') {
            start++;
        }
        if (start == length)
            return text.subSequence(0, 0);
        while (end > start && text.charAt(end) <= ' ') {
            end--;
        }
        return text.subSequence(start, end + 1);
    }
    
    public static void debugChar(StringBuffer sb, char ch) {
        switch (ch) {
            case '\n':
                sb.append("\n");
                break;
            case '\t':
                sb.append("\t");
                break;
            case '\r':
                sb.append("\r");
                break;
            case '\\':
                sb.append("\\");
                break;
            default:
                sb.append(ch);
                break;
        }
    }
    
    public static void debugText(StringBuffer sb, CharSequence text) {
        for (int i = 0; i < text.length(); i++) {
            debugChar(sb, text.charAt(i));
        }
    }
    
    public static String debugText(CharSequence text) {
        StringBuffer sb = new StringBuffer();
        debugText(sb, text);
        return sb.toString();
    }

}
