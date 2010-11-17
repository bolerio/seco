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

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileFilter;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.html.HTML;


/**
 * A dialog providing an image repository and a way to edit display options for
 * images from the repository.
 * 
 * @author Ulrich Hilger
 * @author Light Development
 * @author <a href="http://www.lightdev.com">http://www.lightdev.com</a>
 * @author <a href="mailto:info@lightdev.com">info@lightdev.com</a>
 * @author published under the terms and conditions of the GNU General Public
 * License, for details see file gpl.txt in the distribution package of this
 * software
 * 
 * @version stage 11, April 27, 2003
 */
public class ImageDialog extends DialogShell implements ActionListener,
		ListSelectionListener, ChangeListener
{
	/** directory this ImageDialog maintains */
	private File imgDir;
	/** KeyListener for watching changes in the scale text field */
	private KeyHandler keyHandler = new KeyHandler();
	/** FocusListener for watching changes in the scale text field */
	private FocusHandler focusHandler = new FocusHandler();
	private SimpleAttributeSet originalAttributes = new SimpleAttributeSet();
	/**
	 * indicates whether or not changes in a SizeSelectorPanel are to be
	 * processed. Usually, changes caused by a method of this class are to be
	 * ignored
	 */
	private boolean ignoreChangeEvents = false;
	/** list with images in this image repository */
	private JList imgFileList;
	/** button to add an image file to the repository */
	private JButton addImgBtn;
	/** button to delete an image file from the repository */
	private JButton delImgBtn;
	/** text field for manipulating the scale of an image */
	private JTextField scale;
	/** component to manipulate the image width */
	private SizeSelectorPanel imgWidth;
	/** component to manipulate the image height */
	private SizeSelectorPanel imgHeight;
	/** component to display the original width of an image */
	private JLabel oWidth;
	/** component to display the original height of an image */
	private JLabel oHeight;
	/** component to preview an image */
	private ImagePreview preview;
	/** component to scroll an image inside the preview */
	private JScrollPane scPrev;
	/**
	 * contains all components having attributes for the image represented in
	 * this <code>ImageDialog</code>
	 */
	private Vector attributeComponents = new Vector();
	/** the document the image came from, if any */
	private MyHTMLDocument doc;

	/**
	 * construct a new ImageDialog
	 * 
	 * @param parent the parent frame of this ImageDialog
	 * @param title the title of this ImageDialog
	 * @param imgDir the directory of the image repository
	 */
	public ImageDialog(Dialog parent, String title, File imgDir)
	{
		super(parent, title);
		initDialog(title, imgDir);
	}

	/**
	 * construct a new ImageDialog
	 * 
	 * @param parent the parent frame of this ImageDialog
	 * @param title the title of this ImageDialog
	 * @param imgDir the directory of the image repository
	 */
	public ImageDialog(Frame parent, String title, File imgDir)
	{
		super(parent, title);
		initDialog(title, imgDir);
	}

	public ImageDialog(Frame parent, String title, File imgDir,
			MyHTMLDocument sourceDoc)
	{
		super(parent, title);
		this.doc = sourceDoc;
		initDialog(title, imgDir);
	}

	/**
	 * build the dialog contents after construction
	 * 
	 * @param title the title of this ImageDialog
	 * @param imgDir the directory of the image repository
	 */
	private void initDialog(String title, File imgDir)
	{
		// System.out.println("ImageDialog.initDialog imgDir=" +
		// imgDir.getAbsolutePath());
		this.imgDir = imgDir;
		Dimension dim;
		// create an image directory panel
		JPanel dirPanel = new JPanel(new BorderLayout());
		dirPanel.setBorder(new TitledBorder(new EtchedBorder(
				EtchedBorder.LOWERED), "Image files"));
		// create a list to display image files in
		imgFileList = new JList();
		JScrollPane imageScroll = new JScrollPane(imgFileList);
		dim = new Dimension(200, 100);
		imageScroll.setMinimumSize(dim);
		imageScroll.setPreferredSize(dim);
		imgFileList.addListSelectionListener(this);
		updateFileList();
		// create a panel with action buttons for image files
		JPanel dirBtnPanel = new JPanel();
		// create image directory action buttons
		addImgBtn = new JButton("Add...");
		addImgBtn.addActionListener(this);
		delImgBtn = new JButton("Delete");
		delImgBtn.addActionListener(this);
		// add action buttons to button panel
		dirBtnPanel.add(addImgBtn);
		dirBtnPanel.add(delImgBtn);
		// add components to image directory panel
		dirPanel.add(imageScroll, BorderLayout.CENTER);
		//dirPanel.add(imgFileList, BorderLayout.CENTER);
		dirPanel.add(dirBtnPanel, BorderLayout.SOUTH);
		// create an image preview panel
		JPanel previewPanel = new JPanel(new BorderLayout());
		previewPanel.setBorder(new TitledBorder(new EtchedBorder(
				EtchedBorder.LOWERED), "Preview"));
		// add a new ImagePreview object to the preview panel
		preview = new ImagePreview();
		dim = new Dimension(250, 250);
		preview.setMinimumSize(dim);
		preview.setPreferredSize(dim);
		scPrev = new JScrollPane(preview);
		previewPanel.add(scPrev, BorderLayout.CENTER);
		// layout and constraints to use later on
		GridBagLayout g = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		// create an image properties panel
		JPanel eastPanel = new JPanel(new BorderLayout());
		JPanel propertiesPanel = new JPanel(g);
		eastPanel.add(propertiesPanel, BorderLayout.NORTH);
		eastPanel.setBorder(new TitledBorder(new EtchedBorder(
				EtchedBorder.LOWERED), "Properties"));
		// add scale component
		Util.addGridBagComponent(propertiesPanel, new JLabel("Scale"), g, c, 0,
				0, GridBagConstraints.EAST);
		scale = new JTextField();
		scale.addKeyListener(keyHandler);
		scale.addFocusListener(focusHandler);
		dim = new Dimension(50, 20);
		scale.setMinimumSize(dim);
		scale.setPreferredSize(dim);
		JPanel helperPanel = new JPanel();
		helperPanel.add(scale);
		helperPanel.add(new JLabel(SizeSelectorPanel.UNIT_PERCENT,
				SwingConstants.LEFT));
		Util.addGridBagComponent(propertiesPanel, helperPanel, g, c, 1, 0,
				GridBagConstraints.WEST);
		// add width component
		Util.addGridBagComponent(propertiesPanel, new JLabel("Width"), g, c, 0,
				1, GridBagConstraints.EAST);
		imgWidth = new SizeSelectorPanel(HTML.Attribute.WIDTH, null, false,
				SizeSelectorPanel.TYPE_LABEL);
		attributeComponents.addElement(imgWidth);
		imgWidth.getValueSelector().addChangeListener(this);
		Util.addGridBagComponent(propertiesPanel, imgWidth, g, c, 1, 1,
				GridBagConstraints.WEST);
		// add height component
		Util.addGridBagComponent(propertiesPanel, new JLabel("Height"), g, c,
				0, 2, GridBagConstraints.EAST);
		imgHeight = new SizeSelectorPanel(HTML.Attribute.HEIGHT, null, false,
				SizeSelectorPanel.TYPE_LABEL);
		attributeComponents.addElement(imgHeight);
		imgHeight.getValueSelector().addChangeListener(this);
		Util.addGridBagComponent(propertiesPanel, imgHeight, g, c, 1, 2,
				GridBagConstraints.WEST);
		// add hspace component
		Util.addGridBagComponent(propertiesPanel, new JLabel("horiz. space"),
				g, c, 0, 3, GridBagConstraints.EAST);
		SizeSelectorPanel hSpace = new SizeSelectorPanel(HTML.Attribute.HSPACE,
				null, false, SizeSelectorPanel.TYPE_LABEL);
		attributeComponents.addElement(hSpace);
		Util.addGridBagComponent(propertiesPanel, hSpace, g, c, 1, 3,
				GridBagConstraints.WEST);
		// add vspace component
		Util.addGridBagComponent(propertiesPanel, new JLabel("vert. space"), g,
				c, 0, 4, GridBagConstraints.EAST);
		SizeSelectorPanel vSpace = new SizeSelectorPanel(HTML.Attribute.VSPACE,
				null, false, SizeSelectorPanel.TYPE_LABEL);
		attributeComponents.addElement(vSpace);
		Util.addGridBagComponent(propertiesPanel, vSpace, g, c, 1, 4,
				GridBagConstraints.WEST);
		// add alignment component
		Util.addGridBagComponent(propertiesPanel, new JLabel("Alignment"), g,
				c, 0, 5, GridBagConstraints.EAST);
		String[] items = new String[] { "top", "middle", "bottom", "left",
				"center", "right" };
		String[] names = new String[] { "top", "middle", "bottom", "left",
				"center", "right" };
		AttributeComboBox imgAlign = new AttributeComboBox(items, names, null,
				HTML.Attribute.ALIGN);
		attributeComponents.addElement(imgAlign);
		Util.addGridBagComponent(propertiesPanel, imgAlign, g, c, 1, 5,
				GridBagConstraints.WEST);
		// add original width component
		Util.addGridBagComponent(propertiesPanel, new JLabel("orig. width:"),
				g, c, 0, 6, GridBagConstraints.EAST);
		oWidth = new JLabel("");
		Util.addGridBagComponent(propertiesPanel, oWidth, g, c, 1, 6,
				GridBagConstraints.WEST);
		// add original height component
		Util.addGridBagComponent(propertiesPanel, new JLabel("orig. height:"),
				g, c, 0, 7, GridBagConstraints.EAST);
		oHeight = new JLabel("");
		Util.addGridBagComponent(propertiesPanel, oHeight, g, c, 1, 7,
				GridBagConstraints.WEST);
		// add border component
		Util.addGridBagComponent(propertiesPanel, new JLabel("Border:"), g, c,
				0, 8, GridBagConstraints.EAST);
		SizeSelectorPanel imgBorder = new SizeSelectorPanel(
				HTML.Attribute.BORDER, null, false,
				SizeSelectorPanel.TYPE_LABEL);
		attributeComponents.addElement(imgBorder);
		Util.addGridBagComponent(propertiesPanel, imgBorder, g, c, 1, 8,
				GridBagConstraints.WEST);
		// add to content pane of DialogShell
		Container contentPane = super.getContentPane();
		contentPane.add(dirPanel, BorderLayout.WEST);
		contentPane.add(previewPanel, BorderLayout.CENTER);
		contentPane.add(eastPanel, BorderLayout.EAST);
		// cause optimal placement of all elements
		pack();
		scPrev.addComponentListener(new ResizeListener());
	}

	public Integer getImgWidth()
	{
		return imgWidth.getIntValue();
	}

	public Integer getImgHeight()
	{
		return imgHeight.getIntValue();
	}

	/**
	 * set dialog content from a given set of image attributes
	 * 
	 * @param a the set of attributes to set dialog contents from
	 */
	public void setImageAttributes(AttributeSet a)
	{
		// System.out.println("ImageDialog.setImageAttributes");
		ignoreChangeEvents = true;
		originalAttributes.addAttributes(a);
		if (a.isDefined(HTML.Attribute.SRC))
		{
			File imgFile = null;
			if (doc != null)
			{
				imgFile = new File(Util
						.resolveRelativePath(a.getAttribute(HTML.Attribute.SRC)
								.toString(), doc.getBase().getFile()));
			} else
			{
				imgFile = new File(a.getAttribute(HTML.Attribute.SRC)
						.toString());
			}
			//System.out.println("ImageDialog.setImageAttribute imgFile=" +
			 //imgFile.getAbsolutePath());
			imgFileList.setSelectedValue(strip(imgFile), true);
		}
		for (int i = 0; i < attributeComponents.size(); i++)
		{
			((AttributeComponent) attributeComponents.get(i)).setValue(a);
		}
		if (a.isDefined(HTML.Attribute.WIDTH))
		{
			preview.setPreviewWidth(Integer.parseInt(a.getAttribute(
					HTML.Attribute.WIDTH).toString()));
		}
		if (a.isDefined(HTML.Attribute.HEIGHT))
		{
			preview.setPreviewHeight(Integer.parseInt(a.getAttribute(
					HTML.Attribute.HEIGHT).toString()));
		}
		int scalePct = preview.getScale();
		scale.setText(Integer.toString(scalePct));
		ignoreChangeEvents = false;
	}

	public void setImage(String fName, String w, String h)
	{
		// System.out.println("ImageDialog.setImage fName=" + fName);
		imgFileList.setSelectedValue(new File(fName).getName(), true);
		preview.setImage(new ImageIcon(fName));
		try
		{
			if (w != null && w.length() > 0)
			{
				preview.setPreviewWidth(Integer.parseInt(w));
			}
			if (h != null && h.length() > 0)
			{
				preview.setPreviewHeight(Integer.parseInt(h));
			}
		}
		catch (Exception e)
		{
			Util.errMsg(this, null, e);
		}
	}

	/**
	 * get the HTML representing the image selected in this
	 * <code>ImageDialog</code>
	 */
	public String getImageHTML()
	{
		SimpleAttributeSet set = new SimpleAttributeSet(originalAttributes);
		StringWriter sw = new StringWriter();
		MyHTMLWriter w = new MyHTMLWriter(sw);
		for (int i = 0; i < attributeComponents.size(); i++)
		{
			set.addAttributes(((AttributeComponent) attributeComponents.get(i))
					.getValue());
		}
		set.addAttribute(HTML.Attribute.SRC, getImageSrc());
		try
		{
			w.startTag(HTML.Tag.IMG, set);
		}
		catch (Exception e)
		{
			Util.errMsg(this, e.getMessage(), e);
		}
		return sw.getBuffer().toString();
	}

	/**
	 * get the value for the SRC attribute of an image tag
	 * 
	 * @return the value of the SRC attribute of an image tag
	 */
	public String getImageSrc()
	{
		StringBuffer buf = new StringBuffer();
		Object value = imgFileList.getSelectedValue();
		if (value != null)
		{
			//buf.append("images");// TODO:
			//buf.append(Util.URL_SEPARATOR);
			buf.append(value.toString());
		}
		return buf.toString();
	}
	private static ExampleFileFilter filter;

	private ExampleFileFilter getFilter()
	{
		if (filter == null)
		{
			filter = new ExampleFileFilter();
			filter.addExtension("gif");
			filter.addExtension("jpg");
			filter.addExtension("jpeg");
			filter.setDescription("Image files");
		}
		return filter;
	}

	/**
	 * handle the event when the user pressed the 'Add...' button to add a new
	 * image to the repository
	 */
	private void handleAddImage()
	{
		try
		{
			JFileChooser chooser = new JFileChooser();
			chooser.setMultiSelectionEnabled(true);
			chooser.setFileFilter(getFilter());
			if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
			{
				File[] sFiles = chooser.getSelectedFiles();
				if (!imgDir.exists())
					imgDir.mkdirs();
				
				String imgDirName = imgDir.getAbsolutePath();
				for (int i = 0; i < sFiles.length; i++)
				{
					// System.out.println("file selected: " + sFiles[i] + " new
					// name= " + imgDirName + File.separator +
					// sFiles[i].getName());
					Util.copyFile(sFiles[i], new File(imgDirName
							+ File.separator + sFiles[i].getName()));
					updateFileList();
				}
			}
		}
		catch (Exception e)
		{
			Util.errMsg(this, e.getMessage(), e);
		}
	}

	/**
	 * handle the event occurring when the user pressed the 'Delete' button to
	 * remove an image from the repository
	 */
	private void handleDeleteImage()
	{
		String fName = imgFileList.getSelectedValue().toString();
		if (Util.msg(JOptionPane.YES_NO_OPTION, "confirmDelete",
				"deleteFileQuery", fName, "\r\n"))
		{
			File delFile = new File(imgDir.getAbsolutePath() + File.separator
					+ fName);
			delFile.delete();
			updateFileList();
		}
	}

	/**
	 * display all files found in the image directory
	 */
	private void updateFileList()
	{
		if (imgDir != null && imgFileList != null)
		{
			List out = new LinkedList();
			populate(imgDir, out);
			imgFileList.setListData(out.toArray());
		}
	}
	
	private void populate(File f, List list){
		if(f.isDirectory())
			for(File inner: f.listFiles())
				populate(inner, list);
		String fn = f.getName();
		if(fn.endsWith(".gif") || fn.endsWith(".jpg")
				|| fn.endsWith(".jpeg"))
			list.add(strip(f));
	}
	
	private String strip(File f){
		String dir = imgDir.getAbsolutePath();
		String fn = f.getAbsolutePath();
		//strip separator too
		return fn.substring(dir.length() + 1);
	}

	/**
	 * update all image property displays to the current setting
	 */
	private void updateControls()
	{
		ignoreChangeEvents = true;
		int scalePct = preview.getScale();
		SimpleAttributeSet set = new SimpleAttributeSet();
		oWidth.setText(Integer.toString(preview.getOriginalWidth()));
		oHeight.setText(Integer.toString(preview.getOriginalHeight()));
		// System.out.println("updateControls origW=" +
		// preview.getOriginalWidth());
		// System.out.println("updateControls add WIDTH attr as " +
		// Integer.toString(
		// preview.getOriginalWidth() * scalePct / 100) +
		// SizeSelectorPanel.UNIT_PT);
		set.addAttribute(HTML.Attribute.WIDTH, Integer.toString(preview
				.getOriginalWidth()
				* scalePct / 100)
				+ SizeSelectorPanel.UNIT_PT);
		set.addAttribute(HTML.Attribute.HEIGHT, Integer.toString(preview
				.getOriginalHeight()
				* scalePct / 100)
				+ SizeSelectorPanel.UNIT_PT);
		imgWidth.setValue(set);
		imgHeight.setValue(set);
		scale.setText(Integer.toString(scalePct));
		ignoreChangeEvents = false;
	}

	/**
	 * apply a scale set by the user through respective text field and update
	 * all related image property displays
	 */
	private void applyPreviewScale()
	{
		// System.out.println("applyPreviewScale scale=" + scale.getText());
		ignoreChangeEvents = true;
		try
		{
			preview.setScale(Integer.parseInt(scale.getText()));
			updateControls();
		}
		catch (Exception e)
		{
		}
		ignoreChangeEvents = false;
	}

	/**
	 * apply a new width set by the user and update all related image property
	 * displays
	 */
	private void applyPreviewWidth()
	{
		// System.out.println("applyPreviewWidth width=" +
		// imgWidth.getIntValue().intValue());
		ignoreChangeEvents = true;
		preview.setPreviewWidth(imgWidth.getIntValue().intValue());
		int scalePct = preview.getScale();
		// System.out.println("applyPreviewWidth scale now " + scalePct);
		SimpleAttributeSet set = new SimpleAttributeSet();
		scale.setText(Integer.toString(scalePct));
		set.addAttribute(HTML.Attribute.HEIGHT, Integer.toString(preview
				.getOriginalHeight()
				* scalePct / 100)
				+ SizeSelectorPanel.UNIT_PT);
		// System.out.println("applyPreviewWidth, changing height to " +
		// Integer.toString(
		// preview.getOriginalHeight() * scalePct / 100) +
		// SizeSelectorPanel.UNIT_PT);
		imgHeight.setValue(set);
		ignoreChangeEvents = false;
	}

	/**
	 * apply a new height set by the user and update all related image property
	 * displays
	 */
	private void applyPreviewHeight()
	{
		// System.out.println("applyPreviewHeight height=" +
		// imgHeight.getIntValue().intValue());
		ignoreChangeEvents = true;
		preview.setPreviewHeight(imgHeight.getIntValue().intValue());
		int scalePct = preview.getScale();
		// System.out.println("applyPreviewHeight scale now " + scalePct);
		SimpleAttributeSet set = new SimpleAttributeSet();
		scale.setText(Integer.toString(scalePct));
		set.addAttribute(HTML.Attribute.WIDTH, Integer.toString(preview
				.getOriginalWidth()
				* scalePct / 100)
				+ SizeSelectorPanel.UNIT_PT);
		// System.out.println("applyPreviewHeight, changing width to " +
		// Integer.toString(
		// preview.getOriginalWidth() * scalePct / 100) +
		// SizeSelectorPanel.UNIT_PT);
		imgWidth.setValue(set);
		ignoreChangeEvents = false;
	}

	/* ---------------- event handling start ------------------------- */
	/**
	 * implements the ActionListener interface to be notified of clicks onto the
	 * file repository buttons.
	 */
	public void actionPerformed(ActionEvent e)
	{
		Object src = e.getSource();
		if (src == addImgBtn)
		{
			handleAddImage();
		} else if (src == delImgBtn)
		{
			handleDeleteImage();
		} else
		{
			super.actionPerformed(e);
		}
	}

	/**
	 * Listener for changes in the image list.
	 * 
	 * <p>
	 * updates the image preview and property displays according to the current
	 * selection (if any)
	 * </p>
	 */
	public void valueChanged(ListSelectionEvent e)
	{
		if (!imgFileList.isSelectionEmpty())
		{
			/*
			 * System.out.println("ImageDialog.valueChanged setting preview
			 * image to " + imgDir.getAbsolutePath() + File.separator +
			 * imgFileList.getSelectedValue().toString());
			 */
			preview.setImage(new ImageIcon(imgDir.getAbsolutePath()
					+ File.separator
					+ imgFileList.getSelectedValue().toString()));
			updateControls();
		} else
		{
			preview.setImage(null);
			int vWidth = scPrev.getWidth() - 5;
			int vHeight = scPrev.getHeight() - 5;
			preview.setPreferredSize(new Dimension(vWidth, vHeight));
			preview.revalidate();
		}
	}

	/**
	 * Listener for resize events.
	 * 
	 * <p>
	 * used on the JScrollPane holding the image preview to adjust the preview
	 * to size changes and to synchronize property displays accordingly.
	 * </p>
	 */
	private class ResizeListener extends ComponentAdapter
	{
		public void componentResized(ComponentEvent e)
		{
			int vWidth = scPrev.getWidth() - 5;
			int vHeight = scPrev.getHeight() - 5;
			preview.setPreferredSize(new Dimension(vWidth, vHeight));
			preview.revalidate();
			updateControls();
		}
	}

	/**
	 * Listener for key events
	 * 
	 * <p>
	 * Used to adjust preview properties according to user settings in the scale
	 * text field
	 * </p>
	 */
	private class KeyHandler extends KeyAdapter
	{
		public void keyReleased(KeyEvent e)
		{
			Object source = e.getSource();
			int keyCode = e.getKeyCode();
			if (source.equals(scale))
			{
				if (keyCode == KeyEvent.VK_ENTER)
				{
					applyPreviewScale();
				}
			}
		}
	}

	/**
	 * Listener for focus events
	 * 
	 * <p>
	 * Used to adjust preview properties according to user settings in the scale
	 * text field
	 * </p>
	 */
	private class FocusHandler extends FocusAdapter
	{
		public void focusLost(FocusEvent e)
		{
			Object source = e.getSource();
			if (source.equals(scale))
			{
				applyPreviewScale();
			}
		}
	}

	/**
	 * Listener for change events
	 * 
	 * <p>
	 * Used to adjust preview properties according to user settings in
	 * SizeSelectorPanels
	 * </p>
	 */
	public void stateChanged(ChangeEvent e)
	{
		if (!ignoreChangeEvents)
		{
			Object source = e.getSource();
			if (source.equals(imgWidth.getValueSelector()))
			{
				applyPreviewWidth();
			} else if (source.equals(imgHeight.getValueSelector()))
			{
				applyPreviewHeight();
			}
		}
	}
	/* ---------------- event handling end ------------------------- */
	
	static /**
	 * A convenience implementation of FileFilter that filters out
	 * all files except for those type extensions that it knows about.
	 *
	 * Extensions are of the type ".foo", which is typically found on
	 * Windows and Unix boxes, but not on Macinthosh. Case is ignored.
	 *
	 * Example - create a new filter that filerts out all files
	 * but gif and jpg image files:
	 *
	 *     JFileChooser chooser = new JFileChooser();
	 *     ExampleFileFilter filter = new ExampleFileFilter(
	 *                   new String{"gif", "jpg"}, "JPEG & GIF Images")
	 *     chooser.addChoosableFileFilter(filter);
	 *     chooser.showOpenDialog(this);
	 *
	 * @version 1.12 12/03/01
	 * @author Jeff Dinkins
	 */
	public class ExampleFileFilter extends javax.swing.filechooser.FileFilter
	{

	    private Hashtable filters = null;
	    private String description = null;
	    private String fullDescription = null;
	    private boolean useExtensionsInDescription = true;

	    /**
	     * Creates a file filter. If no filters are added, then all
	     * files are accepted.
	     *
	     * @see #addExtension
	     */
	    public ExampleFileFilter() {
	    this.filters = new Hashtable();
	    }

	    /**
	     * Creates a file filter that accepts files with the given extension.
	     * Example: new ExampleFileFilter("jpg");
	     *
	     * @see #addExtension
	     */
	    public ExampleFileFilter(String extension) {
	    this(extension,null);
	    }

	    /**
	     * Creates a file filter that accepts the given file type.
	     * Example: new ExampleFileFilter("jpg", "JPEG Image Images");
	     *
	     * Note that the "." before the extension is not needed. If
	     * provided, it will be ignored.
	     *
	     * @see #addExtension
	     */
	    public ExampleFileFilter(String extension, String description) {
	    this();
	    if(extension!=null) addExtension(extension);
	    if(description!=null) setDescription(description);
	    }

	    /**
	     * Creates a file filter from the given string array.
	     * Example: new ExampleFileFilter(String {"gif", "jpg"});
	     *
	     * Note that the "." before the extension is not needed adn
	     * will be ignored.
	     *
	     * @see #addExtension
	     */
	    public ExampleFileFilter(String[] filters) {
	    this(filters, null);
	    }

	    /**
	     * Creates a file filter from the given string array and description.
	     * Example: new ExampleFileFilter(String {"gif", "jpg"}, "Gif and JPG Images");
	     *
	     * Note that the "." before the extension is not needed and will be ignored.
	     *
	     * @see #addExtension
	     */
	    public ExampleFileFilter(String[] filters, String description) {
	    this();
	    for (int i = 0; i < filters.length; i++) {
	        // add filters one by one
	        addExtension(filters[i]);
	    }
	    if(description!=null) setDescription(description);
	    }

	    /**
	     * Return true if this file should be shown in the directory pane,
	     * false if it shouldn't.
	     *
	     * Files that begin with "." are ignored.
	     *
	     * @see #getExtension
	     * @see FileFilter#accepts
	     */
	    public boolean accept(File f) {
	    if(f != null) {
	        if(f.isDirectory()) {
	        return true;
	        }
	        String extension = getExtension(f);
	        if(extension != null && filters.get(getExtension(f)) != null) {
	        return true;
	        };
	    }
	    return false;
	    }

	    /**
	     * Return the extension portion of the file's name .
	     *
	     * @see #getExtension
	     * @see FileFilter#accept
	     */
	     public String getExtension(File f) {
	    if(f != null) {
	        String filename = f.getName();
	        int i = filename.lastIndexOf('.');
	        if(i>0 && i<filename.length()-1) {
	        return filename.substring(i+1).toLowerCase();
	        };
	    }
	    return null;
	    }

	    /**
	     * Adds a filetype "dot" extension to filter against.
	     *
	     * For example: the following code will create a filter that filters
	     * out all files except those that end in ".jpg" and ".tif":
	     *
	     *   ExampleFileFilter filter = new ExampleFileFilter();
	     *   filter.addExtension("jpg");
	     *   filter.addExtension("tif");
	     *
	     * Note that the "." before the extension is not needed and will be ignored.
	     */
	    public void addExtension(String extension) {
	    if(filters == null) {
	        filters = new Hashtable(5);
	    }
	    filters.put(extension.toLowerCase(), this);
	    fullDescription = null;
	    }


	    /**
	     * Returns the human readable description of this filter. For
	     * example: "JPEG and GIF Image Files (*.jpg, *.gif)"
	     *
	     * @see setDescription
	     * @see setExtensionListInDescription
	     * @see isExtensionListInDescription
	     * @see FileFilter#getDescription
	     */
	    public String getDescription() {
	    if(fullDescription == null) {
	        if(description == null || isExtensionListInDescription()) {
	        fullDescription = description==null ? "(" : description + " (";
	        // build the description from the extension list
	        Enumeration extensions = filters.keys();
	        if(extensions != null) {
	            fullDescription += "." + (String) extensions.nextElement();
	            while (extensions.hasMoreElements()) {
	            fullDescription += ", ." + (String) extensions.nextElement();
	            }
	        }
	        fullDescription += ")";
	        } else {
	        fullDescription = description;
	        }
	    }
	    return fullDescription;
	    }

	    /**
	     * Sets the human readable description of this filter. For
	     * example: filter.setDescription("Gif and JPG Images");
	     *
	     * @see setDescription
	     * @see setExtensionListInDescription
	     * @see isExtensionListInDescription
	     */
	    public void setDescription(String description) {
	    this.description = description;
	    fullDescription = null;
	    }

	    /**
	     * Determines whether the extension list (.jpg, .gif, etc) should
	     * show up in the human readable description.
	     *
	     * Only relevent if a description was provided in the constructor
	     * or using setDescription();
	     *
	     * @see getDescription
	     * @see setDescription
	     * @see isExtensionListInDescription
	     */
	    public void setExtensionListInDescription(boolean b) {
	    useExtensionsInDescription = b;
	    fullDescription = null;
	    }

	    /**
	     * Returns whether the extension list (.jpg, .gif, etc) should
	     * show up in the human readable description.
	     *
	     * Only relevent if a description was provided in the constructor
	     * or using setDescription();
	     *
	     * @see getDescription
	     * @see setDescription
	     * @see setExtensionListInDescription
	     */
	    public boolean isExtensionListInDescription() {
	    return useExtensionsInDescription;
	    }
	}
}
