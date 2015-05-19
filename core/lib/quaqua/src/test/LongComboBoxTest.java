/*
 * @(#)LongComboBoxTest.java  1.0  2010-11-20
 * 
 * Copyright © 2010 Werner Randelshofer, Switzerland.
 * All rights reserved.
 * 
 * You may not use, copy or modify this file, except in compliance with the
 * license agreement you entered into with Werner Randelshofer.
 * For details see accompanying license terms.
 */

package test;
import javax.swing.*;

/**
 * {@code LongComboBoxTest}.
 *
 * @author Werner Randelshofer
 * @version 1.0 2010-11-20 Created.
 */
public class LongComboBoxTest {
    public static void main(String[] args) {
        try {

UIManager.setLookAndFeel("ch.randelshofer.quaqua.leopard.Quaqua16LeopardLookAndFeel");

            JFrame frame = new JFrame();
            JPanel panel = new JPanel();
            frame.setContentPane(panel);
            JComboBox comboBox = new JComboBox();
            comboBox.setModel(new DefaultComboBoxModel(new
String [] {
                 "1",   "2",  "3",  "4",  "5",  "6",  "7",
"8",  "9", "10",
                "11", "12", "13", "14", "15", "16", "17",
"18", "19", "20",
                "21", "22", "23", "24", "25", "26", "27",
"28", "29", "30",
                "31", "32", "33", "34", "35", "36", "37",
"38", "39", "40",
                "41", "42", "43", "44", "45", "46", "47",
"48", "49", "50",
                "51", "52", "53", "54", "55", "56", "57",
"58", "59", "60",
                "61", "62", "63", "64", "65", "66", "67",
"68", "69", "70",
                "71", "72", "73", "74", "75", "76", "77",
"78", "79", "80",
                "91", "92", "93", "94", "95", "96", "97",
"98", "99", "100",
              }));
              panel.add(comboBox);

              frame.setSize(100,60);
              frame.setVisible(true);
        }
        catch (Exception qe) {
            System.out.println(qe.getMessage());
        }
    }
}
