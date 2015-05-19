/*
 * @(#)ScriptSystem.java  
 *
 * Copyright (c) 2005-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */

package ch.randelshofer.quaqua.util;

import java.awt.*;
/**
 * Constants for the script systems supported by Unicode.
 *
 * @author  Werner Randelshofer
 * @version $Id: ScriptSystem.java 462 2014-03-22 09:23:12Z wrandelshofer $
 */
public class ScriptSystem {
    /**
     * Script system specification.
     */
    private static class Spec {
        /**
         *
         * @param baseline java.awt.Font.ROMAN_BASELINE, CENTER_BASELINE or
         * HANGING_BASELINE.
         */
        public Spec(int system, int[] ranges, int measurementChar, int baseline) {
            this.system = system;
            this.ranges = ranges;
            this.measurementChar = (char) measurementChar;
            this.baseline = baseline;
        }
        int[] ranges;
        int system;
        char measurementChar;
        int baseline;
    }
    
    /**
     * Script systems. The ID of the script system is the lowest
     * Unicode character code of the script system.
     */
    public final static int
    // EUROPEAN ALPHABETS
    ARMENIAN = 0x530,
    COPTIC = 0x2c80,
    CYRILLIC = 0x400,
    GEORGIAN = 0x10a0,
    GREEK = 0x370,
    LATIN = 0x0,
    // AFRICAN SCRIPTS
    ETHIOPIC = 0x1200,
    // NKO = { 0X, 0X, 0X , Font.ROMAN_BASELINE), //
    TIFINAGH = 0x2d30,
    // MIDDLE EASTERN SCRIPTS
    ARABIC = 0x600,
    HEBREW = 0x590,
    SYRIAC = 0x700,
    THAANA = 0x780,
    // AMERICAN SCRIPTS
    CANADIAN_SYLLABICS = 0x1400,
    CHEROKEE = 0x13a0,
    DESERET = 0x10400,
    // OTHER SCRIPTS
    SHAVIAN = 0x10450,
    OSMANYA = 0x10480,
    // GLAGOLITIC =
    // INDIC SCRIPTS
    BENGALI = 0x980,
    DEVANAGARI = 0x900,
    GUJARATI = 0xa80,
    GURMUKHI = 0xa00,
    KANNADA = 0xc80,
    LIMBU = 0x1900,
    MALAYALAM = 0xd00,
    ORIYA = 0xb00,
    SINHALA = 0xd80,
    //SYLOTI_NAGRI =
    TAMIL = 0xb80,
    TELUGU = 0xc00,
    // PHILIPPINE SCRIPTS
    //BUHID =
    // HANUNOO =
    //TAGALOG =
    //TAGBANWA =
    // SOUTH EAST ASIAN SCRIPTS
    //BUGINESE =
    // BALINESE =
    KHMER = 0x1780,
    LAO = 0xe80,
    MYANMAR = 0x1000,
    //NEW_TAI_LUE =
    TAI_LE = 0x1950,
    THAI = 0xe00,
    // EAST ASIAN SCRIPTS
    HAN = 0x2e80,
    BOPOMOFO = 0x3100,
    HIRAGANA = 0x3040,
    KATAKANA = 0x30a0,
    HANGUL = 0x1100,
    YI = 0xa000,
    // CENTRAL ASIAN
    // KHAROSHTHI =
    MONGOLIAN = 0x1800,
    //PHAG_SPA =
    TIBETAN = 0xf00,
    // ANCIENT SCRIPTS
    //ANCIENT_GREEK =
    //CUNEIFORM =
    //OLD_PERSIAN =
    //UGARITIC =
    //LINEARB =
    //AEGEANNUMBERS =
    //COUNTING_ROD_NUMBERS =
    //CYPRIOT_SYLLABARY =
    //GOTHIC =
    //OLDITALIC =
    //OGHAM =
    RUNIC = 0x16a0;
    //PHOENICIAN = 0;
    
    /**
     * Lookup table for determining the script system of a
     * given character.
     *
     * The first item in the subarray is the id of the scripts system.
     * Each array consists of unicode code intervals (from, to) that
     * contains characters of the script system. The intervals start from
     * the first item until the second last item fo the subarray.
     * The last item of the subarray specifies the most useful character in
     * the script system to determine perceived ascent and perceived descent
     * of a font using this script system.
     */
    private final static Spec[] systems = {
        // European Alphabets
        new Spec(ARMENIAN, new int[] {ARMENIAN, 0x058f}, 0x552, Font.ROMAN_BASELINE), // ARMENIAN CAPITAL LETTER YIWN
        new Spec(COPTIC, new int[] {COPTIC, 0x2cff}, 0x2ca0, Font.ROMAN_BASELINE),  // COPTIC CAPITAL LETTER PI
        new Spec(CYRILLIC, new int[] {CYRILLIC, 0x52f}, 0x41d, Font.ROMAN_BASELINE),  // CYRILLIC CAPITAL LETTER EN
        new Spec(GEORGIAN, new int[] {GEORGIAN, 0x10ff}, 0x10a4 , Font.ROMAN_BASELINE), // GEORGIAN CAPITAL LETTER EN
        new Spec(GREEK, new int[] {GREEK, 0x3f0, 0x1f00, 0x1fff}, 0x39e , Font.ROMAN_BASELINE), // GREEK CAPITAL LETTER XI
        new Spec(LATIN, new int[] {LATIN, 0x2af, 0x1d00, 0x1d7f, 0x1e00, 0x1eff, 0xfb00, 0xfb4f}, 0x49 , Font.ROMAN_BASELINE), // LATIN CAPITAL LETTER I
        // African Scripts
        new Spec(ETHIOPIC, new int[] {ETHIOPIC, 0x139f, 0x2d80, 0x2ddf}, 0x1260 , Font.ROMAN_BASELINE), // ETHIOPIC SYLLABLE BA
        // NKo = { 0x, 0x, 0x , Font.ROMAN_BASELINE), //
        new Spec(TIFINAGH, new int[] {TIFINAGH, 0x2d7f}, 0x2d4a , Font.ROMAN_BASELINE), // TIFINAGH LETTER YAZH
        // Middle Eastern Scripts
        new Spec(ARABIC, new int[] {ARABIC, 0x6ff, 0xfb50, 0xfdff, 0xfe70, 0xfeff}, 0x6a1 , Font.ROMAN_BASELINE), // ARABIC LETTER DOTLESS FEH
        new Spec(HEBREW, new int[] {HEBREW, 0x5ff}, 0x5d3 , Font.ROMAN_BASELINE), // HEBREW LETTER DALET
        new Spec(SYRIAC, new int[] {SYRIAC, 0x74f}, 0x716 , Font.ROMAN_BASELINE), // SYRIAC LETTER DOTLESS DALATH RISH
        new Spec(THAANA, new int[] {THAANA, 0x7bf}, 0x782 , Font.ROMAN_BASELINE), // THAANA LETTER NOONU
        // American Scripts
     /*
     CanadianSyllabics = { 0x, 0x, 0x , Font.ROMAN_BASELINE), //
     Cherokee = { 0x, 0x, 0x , Font.ROMAN_BASELINE), //
     Deseret = { 0x, 0x, 0x , Font.ROMAN_BASELINE), //
    // Other Scripts
     Shavian = { 0x, 0x, 0x , Font.ROMAN_BASELINE), //
     Osmanya = { 0x, 0x, 0x , Font.ROMAN_BASELINE), //
     Glagolitic = { 0x, 0x, 0x , Font.ROMAN_BASELINE), //
    // Indic Scripts
     Bengali = { 0x, 0x, 0x , Font.ROMAN_BASELINE), //
      */
        new Spec(DEVANAGARI, new int[] {DEVANAGARI, 0x97f}, 0x917 , Font.ROMAN_BASELINE), // DEVANAGARI LETTER GA
        new Spec(GUJARATI, new int[] {GUJARATI, 0xaff}, 0xabe , Font.ROMAN_BASELINE), // GUJARATI VOWEL SIGN AA
        new Spec(GURMUKHI, new int[] {GURMUKHI, 0xa7f}, 0xa38 , Font.ROMAN_BASELINE), // GURMUKHI LETTER SA
        //Kannada = { 0x, 0x, 0x , Font.ROMAN_BASELINE), //
        //Limbu = { 0x, 0x, 0x , Font.ROMAN_BASELINE), //
        //Malayalam = { 0x, 0x, 0x , Font.ROMAN_BASELINE), //
        //Oriya = { 0x, 0x, 0x , Font.ROMAN_BASELINE), //
        //Sinhala = { 0x, 0x, 0x , Font.ROMAN_BASELINE), //
        //SylotiNagri = { 0x, 0x, 0x , Font.ROMAN_BASELINE), //
        new Spec(TAMIL, new int[] {TAMIL, 0xb8f}, 0xbaa , Font.ROMAN_BASELINE), // TAMIL LETTER PA
        //Telugu = { 0x, 0x, 0x , Font.ROMAN_BASELINE), //
        // Philippine Scripts
        //Buhid = { 0x, 0x, 0x , Font.ROMAN_BASELINE), //
        // Hanunoo = { 0x, 0x, 0x , Font.ROMAN_BASELINE), //
        // Tagalog = { 0x, 0x, 0x , Font.ROMAN_BASELINE), //
        // Tagbanwa = { 0x, 0x, 0x , Font.ROMAN_BASELINE), //
        // South East Asian Scripts
        //Buginese = { 0x, 0x, 0x , Font.ROMAN_BASELINE), //
        //Balinese = { 0x, 0x, 0x , Font.ROMAN_BASELINE), //
        //Khmer = { 0x, 0x, 0x , Font.ROMAN_BASELINE), //
        //Lao = { 0x, 0x, 0x , Font.ROMAN_BASELINE), //
        //Myanmar = { 0x, 0x, 0x , Font.ROMAN_BASELINE), //
        //NewTaiLue = { 0x, 0x, 0x , Font.ROMAN_BASELINE), //
        // TaiLe = { 0x, 0x, 0x , Font.ROMAN_BASELINE), //
        new Spec(THAI, new int[] {THAI, 0xe70}, 0xe1a , Font.ROMAN_BASELINE), // THAI CHARACTER BO BAIMAI
        // East Asian Scripts
        new Spec(HAN, new int[] {HAN,0xfff, 0x3190,0x319f, 0x4e00,0x9fff, 0xf900,0xfaff, 0x3400,0x4dbf, 0x20000,0x2a6df, 0x2f800,0x2fa10}, 0x2f01 , Font.ROMAN_BASELINE), // KANGXI RADICAL LINE
        new Spec(BOPOMOFO, new int[] {BOPOMOFO,0x312f, 0x31a0,0x31bf}, 0x3107 , Font.ROMAN_BASELINE), // BOPOMOFO LETTER M
        new Spec(HIRAGANA, new int[] {HIRAGANA,0x309f}, 0x305b , Font.ROMAN_BASELINE), // HRAGANA LETTER SE
        new Spec(KATAKANA, new int[] {KATAKANA,0x30ff, 0xff00,0xffe0, 0x31f0,0x31ff}, 0x30f1 , Font.ROMAN_BASELINE), // KATAKANA LETTER WE
        new Spec(HANGUL, new int[] {HANGUL, 0x11f0}, 0x1100 , Font.ROMAN_BASELINE), // HANGUL CHOSEON KIYEOK
        new Spec(YI, new int[] {YI, 0xA4cf}, 0xa490 , Font.ROMAN_BASELINE), // YI RADICAL QOT
        // Central Asian
        //Kharoshthi = { 0x, 0x, 0x , Font.ROMAN_BASELINE), //
        new Spec(MONGOLIAN, new int[] {MONGOLIAN, 0x18a0}, 0x1882 , Font.ROMAN_BASELINE), // MONGOLIAN LETTER ALI GALI DAMARU
        //PhagsPa = { 0x, 0x, 0x , Font.ROMAN_BASELINE), //
        new Spec(TIBETAN, new int[] {TIBETAN, 0xfff}, 0xf46 , Font.ROMAN_BASELINE), // TIBETAN LETTER CHA
        // Ancient Scripts
     /*
     AncientGreek = { 0x, 0x, 0x , Font.ROMAN_BASELINE), //
     Cuneiform = { 0x, 0x, 0x , Font.ROMAN_BASELINE), //
     OldPersian = { 0x, 0x, 0x , Font.ROMAN_BASELINE), //
     Ugaritic = { 0x, 0x, 0x , Font.ROMAN_BASELINE), //
     LinearB = { 0x, 0x, 0x , Font.ROMAN_BASELINE), //
     AegeanNumbers = { 0x, 0x, 0x , Font.ROMAN_BASELINE), //
     CountingRodNumbers = { 0x, 0x, 0x , Font.ROMAN_BASELINE), //
     CypriotSyllabary = { 0x, 0x, 0x , Font.ROMAN_BASELINE), //
     Gothic = { 0x, 0x, 0x , Font.ROMAN_BASELINE), //
     OldItalic = { 0x, 0x, 0x , Font.ROMAN_BASELINE), //
     Ogham = { 0x, 0x, 0x , Font.ROMAN_BASELINE), //*/
        new Spec(RUNIC, new int[] { RUNIC, 0x16ff}, 0x16c1, Font.ROMAN_BASELINE),  // RUNIC LETTER ISAZ IS ISS I
        //Phoenician = { 0x, 0x, 0x , Font.ROMAN_BASELINE), //
    }
    ;
    
    /**
     * Prevent instance creation
     */
    private ScriptSystem() {
    }
    
    /**
     * Returns the script system of the specified char.
     * If the script system can not be determined, LATIN is
     * returned.
     */
    public static int getScriptSystemOf(char ch) {
        // FIXME This takes linear time. Instead of this
        // algorithm, we could take advantage of the fact
        // that all Unicode script systems start at a 16 bit
        // interval (ch modulo 16).
        for (int i=0, n = systems.length; i < n; i++) {
            for (int j=0, m = systems[i].ranges.length; j < m; j+=2) {
                if (ch >= systems[i].ranges[j] && ch <= systems[i].ranges[j + 1]) {
                    return systems[i].system;
                }
            }
        }
        return LATIN;
    }
    /**
     * Returns the best character of the specified script system
     * for measuring perceived ascent and perceived descent of the
     * script.
     */
    public static char getMeasurementChar(int system) {
        // FIXME This takes linear time. Instead of this
        // algorithm, we should use a HashMap.
        for (int i=0, n = systems.length; i < n; i++) {
            if (systems[i].system == system) {
                return (char) systems[i].measurementChar;
            }
        }
        return 'X';
    }
    /**
     * Returns the baseline of the specified script system.
     * java.awt.Font.ROMAN_BASELINE, CENTER_BASELINE or HANGING_BASELINE
     */
    public static int getBaseline(int system) {
        // FIXME This takes linear time. Instead of this
        // algorithm, we should use a HashMap.
        for (int i=0, n = systems.length; i < n; i++) {
            if (systems[i].system == system) {
                return systems[i].baseline;
            }
        }
        return Font.ROMAN_BASELINE;
    }
}