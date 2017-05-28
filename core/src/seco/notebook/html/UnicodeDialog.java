/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.notebook.html;

/*
GNU Lesser General Public License

UnicodeDialog
Copyright (C) 2004 Howard Kistler & Michael Pearce

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

*/

/*
Unicode block names and character value ranges taken from data sheets at http://www.unicode.org/charts/
Copyright 1991-2004 Unicode, Inc. All rights reserved. Distributed under the Terms of Use in http://www.unicode.org/copyright.html.
*/

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

import seco.gui.GUIHelper;
import seco.util.GUIUtil;

public class UnicodeDialog extends JDialog implements ActionListener
{
	public static final int UNICODE_BASE = 0;
	public static final int UNICODE_SIGS = 47;
	public static final int UNICODE_SPEC = 48;
	public static final int UNICODE_MATH = 49;
	public static final int UNICODE_DRAW = 54;
	public static final int UNICODE_DING = 56;

	private static final int    UNICODEBLOCKSIZE  = 256;
	private static final String CMDCHANGEBLOCK    = "changeblock";

	private static final String[] unicodeBlocks =
	{
		"Basic Latin & Latin-1 Supplement",		"Latin Extended-A",		"Latin Extended-B",		"IPA Extensions",		"Spacing Modifier Letters",		"Combining Diacritical Marks",		"Greek and Coptic",		"Cyrillic",		"Cyrillic Supplement",		"Armenian",		"Hebrew",		"Arabic",		"Syriac",		"Thaana",		"Devanagari",		"Bengali",		"Gurmukhi",		"Gujarati",		"Oriya",		"Tamil",		"Telugu",		"Kannada",		"Malayalam",		"Sinhala",		"Thai",		"Lao",		"Tibetan",		"Myanmar",		"Georgian",		"Hanjul Jamo",		"Ethiopic",		"Cherokee",		"Unified Canadian Aboriginal Syllabics",		"Ogham",		"Runic",		"Tagalog",		"Hanunoo",		"Buhid",		"Tagbanwa",		"Khmer",		"Mongolian",		"Limbu",		"Tai Le",		"Khmer Symbols",		"Phonetic Extensions",		"Latin Extended Additional",		"Greek Extended",		"Punctuation / Scripts / Currency / Diacriticals",
		"Letterlike Symbols / Number Forms / Arrows",
		"Mathematical Operators",
		"Miscellaneous Technical",
		"Control Pictures",
		"Optical Character Recognition",
		"Enclosed Alphanumerics",
		"Box Drawing / Block Elements / Geometric Shapes",
		"Miscellaneous Symbols",
		"Dingbats / Math-A / Arrows-A",
		"Braille Patterns",
		"Arrows-B / Math-B",
		"Supplemental Mathematical Operators",
		"Miscellaneous Symbols and Arrows",
		"CJK Radicals Supplement",
		"Kangxi Radicals",
		"Ideographic Description Characters",
		"CJK Symbols and Punctuation",
		"Hiragana",
		"Katakana",
		"Bopomofo",
		"Hangul Compatibility Jamo",
		"Kanbun",
		"Bopomofo Extended",
		"Katakana Phonetic Extensions",
		"Enclosed CJK Letters and Months",
		"CJK Compatibility",
		"CJK Unified Ideographs Extension A",
		"Yijing Hexagram Symbols",
		"CJK Unified Ideographs",
		"Yi Syllables",
		"Yi Radicals",
		"Hangul Symbols",
		"RESERVED AREA: High Surrogates",
		"RESERVED AREA: Low Surrogates",
		"RESERVED AREA: Private Use",
		"CJK Compatibility Ideographs",
		"Alphabetic Presentation Forms",
		"Arabic Presentation Forms-A",
		"Variation Selectors",
		"Combining Half Marks",
		"CJK Compatibility Forms",
		"Small Form Variants",
		"Arabic Presentation Forms-B",
		"Halfwidth and Fullwidth Forms",
		"Specials"
	};

	private final int[] unicodeBlockStart =
	{
		0,
		256,
		384,
		592,
		688,
		768,
		880,
		1024,
		1280,
		1328,
		1424,
		1536,
		1792,
		1920,
		2304,
		2432,
		2560,
		2688,
		2816,
		2944,
		3072,
		3200,
		3328,
		3456,
		3584,
		3712,
		3840,
		4096,
		4256,
		4352,
		4608,
		5024,
		5120,
		5760,
		5792,
		5888,
		5920,
		5952,
		5984,
		6016,
		6144,
		6400,
		6480,
		6624,
		7424,
		7680,
		7936,
		8192,
		8448,
		8704,
		8960,
		9216,
		9280,
		9312,
		9472,
		9728,
		9984,
		10240,
		10496,
		10752,
		11008,
		11904,
		12032,
		12272,
		12288,
		12352,
		12448,
		12544,
		12592,
		12688,
		12704,
		12784,
		12800,
		13056,
		13312,
		19904,
		19968,
		40960,
		42128,
		44032,
		55296,
		56320,
		57344,
		63744,
		64256,
		64336,
		65024,
		65056,
		65072,
		65104,
		65136,
		65280,
		65520
	};

	private final int[] unicodeBlockEnd =
	{
		255,
		383,
		591,
		687,
		767,
		879,
		1023,
		1279,
		1327,
		1423,
		1535,
		1791,
		1871,
		1983,
		2431,
		2559,
		2687,
		2815,
		2943,
		3071,
		3199,
		3327,
		3455,
		3583,
		3711,
		3839,
		4095,
		4255,
		4351,
		4607,
		4991,
		5119,
		5759,
		5791,
		5887,
		5919,
		5951,
		5983,
		6015,
		6143,
		6319,
		6479,
		6527,
		6655,
		7551,
		7935,
		8191,
		8447,
		8703,
		8959,
		9215,
		9279,
		9311,
		9471,
		9727,
		9983,
		10239,
		10495,
		10751,
		11007,
		11263,
		12031,
		12255,
		12287,
		12351,
		12447,
		12543,
		12591,
		12687,
		12703,
		12735,
		12799,
		13055,
		13311,
		19903,
		19967,
		40879,
		42127,
		42191,
		55215,
		56319,
		57343,
		63743,
		64255,
		64335,
		65023,
		65039,
		65071,
		65103,
		65135,
		65279,
		65519,
		65535
	};

/*
	Original Sets (some are combined to make the dialog more usable)
	"Basic Latin & Latin-1 Supplement"
		"Basic Latin"								-> 0-127
		"Latin-1 Supplement"						-> 128-255
	"Punctuation / Scripts / Currency / Diacriticals"
		"General Punctuation"						-> 8192-8303
		"Superscripts and Subscripts"				-> 8304-8351
		"Currency Symbols"							-> 8352-8399
		"Combining Diacritical Marks for Symbols"	-> 8400-8447
	"Letterlike Symbols / Number Forms / Arrows"
		"Letterlike Symbols"						-> 8448-8527
		"Number Forms"								-> 8528-8591
		"Arrows"									-> 8592-8703
	"Box Drawing / Block Elements / Geometric Shapes"
		"Box Drawing" 								-> 9472-9599
		"Block Elements"							-> 9600-9631
		"Geometric Shapes"							-> 9632-9727
	"Dingbats / Math-A / Arrows-A"
		"Dingbats"									-> 9984-10175
		"Miscellaneous Mathematical Symbols-A"		-> 10176-10223
		"Supplemental Arrows-A"						-> 10224-10239
	"Arrows-B / Math-B"
		"Supplemental Arrows-B"						-> 10496-10623
		"Miscellaneous Mathematical Symbols-B"		-> 10624-10751
*/

/*
	Unicode high characters (these are out of range for Java char, which cuts off at 65536)
	"Linear B Syllabary"						-> 65536-65663
	"Linear B Ideograms"						-> 65664-65791
	"Aegean Numbers"							-> 65792-65855
	"Old Italic"								-> 66304-66351
	"Gothic"									-> 66352-66383
	"Ugaritic"									-> 66432-66463
	"Deseret"									-> 66560-66639
	"Shavian"									-> 66640-66687
	"Osmanya"									-> 66688-66735
	"Cypriot Syllabary"							-> 67584-67647
	"Byzantine Musical Symbols"					-> 118784-119039
	"Musical Symbols"							-> 119040-119295
	"Tai Xuan Jing Symbols"						-> 119552-119647
	"Mathematical Alphanumeric Symbols"			-> 119808-120831
	"CJK Unified Ideographic Extension B"		-> 131072-173791
	"CJK Compatibility Ideographs Supplement"	-> 194560-195103
	"Tags"										-> 917504-917631
	"Variations Selector Supplement"			-> 917760-917999
	"Supplementary Private Use Area-A"			-> 983040-1048573
	"Supplementary Private Use Area-B"			-> 1048576-1114109
*/

	private HTMLEditor editor;
	private Font buttonFont;
	private JToggleButton[] buttonArray = new JToggleButton[UNICODEBLOCKSIZE];
	private ButtonGroup buttonGroup;
	private JComboBox jcmbBlockSelector;
	private JComboBox jcmbPageSelector;

	public UnicodeDialog(HTMLEditor parent, String title, boolean bModal, int index)
	{
		super(GUIUtil.getFrame(parent), title, bModal);
		if(GUIUtil.getFrame(parent) == null) setIconImage(GUIHelper.LOGO_IMAGE);
		editor = parent;
		init(index);
	}

	public void actionPerformed(ActionEvent ae)
	{
		if(ae.getActionCommand().equals(CMDCHANGEBLOCK))
		{
			populateButtons(jcmbBlockSelector.getSelectedIndex(), jcmbPageSelector.getSelectedIndex());
		}
		else if(ae.getActionCommand().equals("close"))
		{
			setVisible(false);
			dispose();
		}
		else if(ae.getActionCommand().equals(""))
		{
			// ignore
		}
		else
		{
			try
			{
				editor.insertUnicodeChar(ae.getActionCommand());
			}
			catch(java.io.IOException ioe) { System.out.println("IOException during character insertion : " + ioe.getMessage()); }
			catch(javax.swing.text.BadLocationException ble) { System.out.println("BadLocationException during character insertion : " + ble.getMessage()); }
		}
	}

	public void init(int startIndex)
	{
		buttonFont = new Font("Monospaced", Font.PLAIN, 12);
	
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());
		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);

		JPanel centerPanel = new JPanel();
		centerPanel.setLayout(new GridLayout(0, 17, 0, 0));
		buttonGroup = new ButtonGroup();

		int prefButtonWidth  = 32;
		int prefButtonHeight = 32;

		centerPanel.add(new JLabel(""));
		for(int labelLoop = 0; labelLoop < 16; labelLoop++)
		{
			JLabel jlblMarker = new JLabel("x" + (labelLoop > 9 ? "" + (char)(65 + (labelLoop - 10)) : "" + labelLoop));
			jlblMarker.setHorizontalAlignment(SwingConstants.CENTER);
			jlblMarker.setVerticalAlignment(SwingConstants.CENTER);
			jlblMarker.setForeground(new Color(0.5f, 0.5f, 0.75f));
			centerPanel.add(jlblMarker);
		}

		int labelcount  = 0;
		for(int counter = 0; counter < UNICODEBLOCKSIZE; counter++)
		{
			if((counter % 16) == 0)
			{
				JLabel jlblMarker = new JLabel((labelcount > 9 ? "" + (char)(65 + (labelcount - 10)) : "" + labelcount) + "x");
				jlblMarker.setHorizontalAlignment(SwingConstants.CENTER);
				jlblMarker.setVerticalAlignment(SwingConstants.CENTER);
				jlblMarker.setForeground(new Color(0.5f, 0.5f, 0.75f));
				centerPanel.add(jlblMarker);
				labelcount++;
			}
			buttonArray[counter] = new JToggleButton(" ");
			buttonArray[counter].getModel().setActionCommand("");
			buttonArray[counter].setFont(buttonFont);
			buttonArray[counter].setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.LOWERED));
			buttonArray[counter].addActionListener(this);
			if(counter == 0)
			{
				FontRenderContext frcLocal = ((java.awt.Graphics2D)(editor.getGraphics())).getFontRenderContext();
				Rectangle2D fontBounds = buttonFont.getMaxCharBounds(frcLocal);
				int maxCharWidth  = (int)(Math.abs(fontBounds.getX())) + (int)(Math.abs(fontBounds.getWidth()));
				int maxCharHeight = (int)(Math.abs(fontBounds.getY())) + (int)(Math.abs(fontBounds.getHeight()));
				Insets buttonInsets = buttonArray[counter].getBorder().getBorderInsets(buttonArray[counter]);
				prefButtonWidth  = maxCharWidth + buttonInsets.left + buttonInsets.right;
				prefButtonHeight = maxCharHeight + buttonInsets.top + buttonInsets.bottom;
			}
			buttonArray[counter].setPreferredSize(new Dimension(prefButtonWidth, prefButtonHeight));
			centerPanel.add(buttonArray[counter]);
			buttonGroup.add(buttonArray[counter]);
		}

		JPanel selectorPanel = new JPanel();

		jcmbBlockSelector = new JComboBox(unicodeBlocks);
		jcmbBlockSelector.setSelectedIndex(startIndex);
		jcmbBlockSelector.setActionCommand(CMDCHANGEBLOCK);
		jcmbBlockSelector.addActionListener(this);

		String[] sPages = { "1" };
		jcmbPageSelector = new JComboBox(sPages);
		jcmbPageSelector.setSelectedIndex(0);
		jcmbPageSelector.setActionCommand(CMDCHANGEBLOCK);
		jcmbPageSelector.addActionListener(this);

		selectorPanel.add(new JLabel("Unicode Set"));
		selectorPanel.add(jcmbBlockSelector);
		selectorPanel.add(new JLabel("Page"));
		selectorPanel.add(jcmbPageSelector);

		JPanel buttonPanel = new JPanel();

		JButton closeButton = new JButton("Close");
		closeButton.setActionCommand("close");
		closeButton.addActionListener(this);
		buttonPanel.add(closeButton);

		contentPane.add(centerPanel, BorderLayout.CENTER);
		contentPane.add(selectorPanel, BorderLayout.NORTH);
		contentPane.add(buttonPanel, BorderLayout.SOUTH);

		this.pack();

		populateButtons(startIndex, 0);

		this.setVisible(true);
	}

	private void populateButtons(int index, int page)
	{
		int blockPages = ((unicodeBlockEnd[index] / UNICODEBLOCKSIZE) - (unicodeBlockStart[index] / UNICODEBLOCKSIZE)) + 1;
		if(blockPages != jcmbPageSelector.getItemCount())
		{
			jcmbPageSelector.setActionCommand("");
			jcmbPageSelector.setEnabled(false);
			jcmbPageSelector.removeAllItems();
			for(int i = 0; i < blockPages; i++)
			{
				jcmbPageSelector.addItem("" + (i + 1));
			}
			jcmbPageSelector.setEnabled(true);
			jcmbPageSelector.update(this.getGraphics());
			jcmbPageSelector.setActionCommand(CMDCHANGEBLOCK);
		}
		if(page > (jcmbPageSelector.getItemCount() - 1))
		{
			page = 0;
		}

		int firstInt = ((unicodeBlockStart[index] / UNICODEBLOCKSIZE) * UNICODEBLOCKSIZE) + (page * UNICODEBLOCKSIZE);
		int currInt = firstInt;
		for(int charInt = 0; charInt < UNICODEBLOCKSIZE; charInt++)
		{
			currInt = firstInt + charInt;
			buttonArray[charInt].setSelected(false);
			if(currInt < unicodeBlockStart[index] || currInt > unicodeBlockEnd[index])
			{
				buttonArray[charInt].setText(" ");
				buttonArray[charInt].getModel().setActionCommand(" ");
				buttonArray[charInt].setEnabled(false);
				buttonArray[charInt].setVisible(false);
			}
			else
			{
				char unichar = (char)currInt;
				String symbol = Character.toString(unichar);
				if(buttonFont.canDisplay(unichar))
				{
					buttonArray[charInt].setText(symbol);
				}
				else
				{
					buttonArray[charInt].setText(" ");
				}
				buttonArray[charInt].getModel().setActionCommand(symbol);
				buttonArray[charInt].setEnabled(true);
				buttonArray[charInt].setVisible(true);
				buttonArray[charInt].update(this.getGraphics());
			}
		}
	}
}
