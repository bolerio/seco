/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package seco.notebook.syntax.completion;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.StringTokenizer;
import javax.swing.text.ChangedCharSetException;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;

/**
 *  HTML Parser. It retrieves sections of the javadoc HTML file.
 *
 * @author  Martin Roskanin
 */
public class HTMLJavadocParser {
    

    /** Gets the javadoc text from the given URL
     *  @param url nbfs protocol URL
     *  @param pkg true if URL should be retrieved for a package
     */
    public static String getJavadocText(URL url, boolean pkg) 
    {
        if (url == null) return null;
        
        HTMLEditorKit.Parser parser = null;
        InputStream is = null;
        
        String charset = null;
        for (;;) {
            try{
                is = url.openStream();
                parser = new ParserDelegator();
                String urlStr = url.toString();
                int offsets[] = new int[2];
                Reader reader = charset == null?new InputStreamReader(is): new InputStreamReader(is, charset);
                
                if (pkg){
                    // package description
                    offsets = parsePackage(reader, parser, charset != null);
                }else if (urlStr.indexOf('#')>0){
                    // member javadoc info
                    String memberName = urlStr.substring(urlStr.indexOf('#')+1);
                    if (memberName.length()>0) offsets = parseMember(reader, memberName, parser, charset != null);
                }else{
                    // class javadoc info
                    offsets = parseClass(reader, parser, charset != null);
                }
                
                if (offsets !=null && offsets[0]!=-1 && offsets[1]>offsets[0]){
                    return getTextFromURLStream(url, offsets[0], offsets[1], charset);
                }
                break;
            } catch (ChangedCharSetException e) {
                if (charset == null) {
                    charset = getCharSet(e);
                    //restart with valid charset
                } else {
                    e.printStackTrace();
                    break;
                }
            } catch(IOException ioe){
            	//stay quiet 
                //ioe.printStackTrace();
                break;
            }finally{
                parser = null;
                if (is!=null) {
                    try{
                        is.close();
                    }catch(IOException ioe){
                        ioe.printStackTrace();
                    }
                }
            }
        }
        return null;
    }
    
    private static String getCharSet(ChangedCharSetException e) {
        String spec = e.getCharSetSpec();
        if (e.keyEqualsCharSet()) {
            //charsetspec contains only charset
            return spec;
        }
        
        //charsetspec is in form "text/html; charset=UTF-8"
                
        int index = spec.indexOf(";"); // NOI18N
        if (index != -1) {
            spec = spec.substring(index + 1);
        }
        
        spec = spec.toLowerCase();
        
        StringTokenizer st = new StringTokenizer(spec, " \t=", true); //NOI18N
        boolean foundCharSet = false;
        boolean foundEquals = false;
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if (token.equals(" ") || token.equals("\t")) { //NOI18N
                continue;
            }
            if (foundCharSet == false && foundEquals == false
                    && token.equals("charset")) { //NOI18N
                foundCharSet = true;
                continue;
            } else if (foundEquals == false && token.equals("=")) {//NOI18N
                foundEquals = true;
                continue;
            } else if (foundEquals == true && foundCharSet == true) {
                return token;
            }
            
            foundCharSet = false;
            foundEquals = false;
        }
        
        return null;
    }
    
    
    /** Gets the part from URLStream as a String
     *  @param startOffset start offset from where to retrieve text
     *  @param endOffset end offset to where the text reach
     *  @throws IOException if the startOffset>endOffset
     */
    public static String getTextFromURLStream(URL url, int startOffset, int endOffset) throws IOException{
        return getTextFromURLStream(url, startOffset, endOffset, null);
    }

    private static String getTextFromURLStream(URL url, int startOffset, int endOffset, String charset) throws IOException{
        
        if (url == null) return null;
        
        if (startOffset>endOffset) throw new IOException();
        InputStream fis = url.openStream();
        InputStreamReader fisreader = charset == null ? new InputStreamReader(fis) : new InputStreamReader(fis, charset);
        int len = endOffset - startOffset;
        int bytesAlreadyRead = 0;
        char buffer[] = new char[len];
        int bytesToSkip = startOffset;
        long bytesSkipped = 0;
        do {
            bytesSkipped = fisreader.skip(bytesToSkip);
            bytesToSkip -= bytesSkipped;
        } while ((bytesToSkip > 0) && (bytesSkipped > 0));

        do {
            int count = fisreader.read(buffer, bytesAlreadyRead, len - bytesAlreadyRead);
            if (count < 0){
                break;
            }
            bytesAlreadyRead += count;
        } while (bytesAlreadyRead < len);
        fisreader.close();
        return new String(buffer);
    }

    
    
    /** Retrieves the position (start offset and end offset) of class javadoc info
      * in the raw html file */
    private static int[] parseClass(Reader reader, final HTMLEditorKit.Parser parser, boolean ignoreCharset) throws IOException {
        final int INIT = 0;
        // javadoc HTML comment '======== START OF CLASS DATA ========'
        final int CLASS_DATA_START = 1;
        // start of the text we need. Located just after first P.
        final int TEXT_START = 2;

        final int state[] = new int[1];
        final int offset[] = new int[2];

        offset[0] = -1; //start offset
        offset[1] = -1; //end offset
        state[0] = INIT;

        HTMLEditorKit.ParserCallback callback = new HTMLEditorKit.ParserCallback() {

            int nextHRPos = -1;
            int lastHRPos = -1;

            public void handleSimpleTag(HTML.Tag t, MutableAttributeSet a, int pos) {
                if (t == HTML.Tag.HR){
                    if (state[0] == TEXT_START){
                        nextHRPos = pos;
                    }
                    lastHRPos = pos;
                }
            }

            public void handleStartTag(HTML.Tag t, MutableAttributeSet a, int pos) {
                if (t == HTML.Tag.P && state[0] == CLASS_DATA_START){
                    state[0] = TEXT_START;
                    offset[0] = pos;
                }
                if (t == HTML.Tag.A && state[0] == TEXT_START) {
                    String attrName = (String)a.getAttribute(HTML.Attribute.NAME);
                    if (attrName!=null && attrName.length()>0){
                        if (nextHRPos!=-1){
                            offset[1] = nextHRPos;
                        }else{
                            offset[1] = pos;
                        }
                        state[0] = INIT;
                    }
                }
            }

            public void handleComment(char[] data, int pos){
                String comment = String.valueOf(data);
                if (comment!=null){
                    if (comment.indexOf("START OF CLASS DATA")>0){ //NOI18N
                        state[0] = CLASS_DATA_START;
                    } else if (comment.indexOf("NESTED CLASS SUMMARY")>0){ //NOI18N
                        if (lastHRPos!=-1){
                            offset[1] = lastHRPos;
                        }else{
                            offset[1] = pos;
                        }
                    }
                }
            }
        };

        parser.parse(reader, callback, ignoreCharset);
        callback = null;
        return offset;
    }

    /** Retrieves the position (start offset and end offset) of member javadoc info
      * in the raw html file */
    private static int[] parseMember(Reader reader, final String name, final HTMLEditorKit.Parser parser, boolean ignoreCharset) throws IOException {
        final int INIT = 0;
        // 'A' tag with the name we are looking for.
        final int A_OPEN = 1;
        // close tag of 'A'
        final int A_CLOSE = 2;
        // PRE tag after the A_CLOSE
        final int PRE_OPEN = 3;

        final int state[] = new int[1];
        final int offset[] = new int[2];

        offset[0] = -1; //start offset
        offset[1] = -1; //end offset
        state[0] = INIT;

        HTMLEditorKit.ParserCallback callback = new HTMLEditorKit.ParserCallback() {

            int hrPos = -1;

            public void handleSimpleTag(HTML.Tag t, MutableAttributeSet a, int pos) {
                if (t == HTML.Tag.HR && state[0]!=INIT){
                    if (state[0] == PRE_OPEN){
                        hrPos = pos;
                    }
                }
            }

            public void handleStartTag(HTML.Tag t, MutableAttributeSet a, int pos) {

                if (t == HTML.Tag.A) {
                    String attrName = (String)a.getAttribute(HTML.Attribute.NAME);
                    if (name.equals(attrName)){
                        // we have found desired javadoc member info anchor
                        state[0] = A_OPEN;
                    } else {
                        if (state[0] == PRE_OPEN && attrName!=null){
                            // reach the end of retrieved javadoc info
                            state[0] = INIT;
                            offset[1] = (hrPos!=-1) ? hrPos : pos;
                        }
                    }
                } else if (t == HTML.Tag.PRE && state[0] == A_CLOSE){
                    state[0] = PRE_OPEN;
                    offset[0] = pos;
                }

            }

            public void handleEndTag(HTML.Tag t, int pos){
                if (t == HTML.Tag.A && state[0] == A_OPEN){
                    state[0] = A_CLOSE;
                }
            }

        };

        parser.parse(reader, callback, ignoreCharset);
        callback = null;
        return offset;
    }

    /** Retrieves the position (start offset and end offset) of member javadoc info
      * in the raw html file */
    private static int[] parsePackage(Reader reader, final HTMLEditorKit.Parser parser, boolean ignoreCharset) throws IOException {
        final String name = "package_description"; //NOI18N
        final int INIT = 0;
        // 'A' tag with the name we are looking for.
        final int A_OPEN = 1;

        final int state[] = new int[1];
        final int offset[] = new int[2];

        offset[0] = -1; //start offset
        offset[1] = -1; //end offset
        state[0] = INIT;

        HTMLEditorKit.ParserCallback callback = new HTMLEditorKit.ParserCallback() {

            int hrPos = -1;

            public void handleSimpleTag(HTML.Tag t, MutableAttributeSet a, int pos) {
                if (t == HTML.Tag.HR && state[0]!=INIT){
                    if (state[0] == A_OPEN){
                        hrPos = pos;
                        offset[1] = pos;
                    }
                }
            }

            public void handleStartTag(HTML.Tag t, MutableAttributeSet a, int pos) {

                if (t == HTML.Tag.A) {
                    String attrName = (String)a.getAttribute(HTML.Attribute.NAME);
                    if (name.equals(attrName)){
                        // we have found desired javadoc member info anchor
                        state[0] = A_OPEN;
                        offset[0] = pos;
                    } else {
                        if (state[0] == A_OPEN && attrName!=null){
                            // reach the end of retrieved javadoc info
                            state[0] = INIT;
                            offset[1] = (hrPos!=-1) ? hrPos : pos;
                        }
                    }
                } 
            }
        };

        parser.parse(reader, callback, ignoreCharset);
        callback = null;
        return offset;
    }
    
}

