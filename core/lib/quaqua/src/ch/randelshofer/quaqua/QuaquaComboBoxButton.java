/*
 * @(#)QuaquaComboBoxButton.java	
 *
 * Copyright (c) 2004-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package ch.randelshofer.quaqua;

import ch.randelshofer.quaqua.border.ButtonStateBorder;
import java.awt.*;

import javax.swing.CellRendererPane;
import javax.swing.DefaultButtonModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.border.Border;

import ch.randelshofer.quaqua.util.Images;

/**
 * JButton subclass to help out QuaquaComboBoxUI.
 *
 * @author  Werner Randelshofer
 * @version $Id: QuaquaComboBoxButton.java 462 2014-03-22 09:23:12Z wrandelshofer $
 */
public class QuaquaComboBoxButton extends JButton {

    protected JComboBox comboBox;
    protected JList listBox;
    protected CellRendererPane rendererPane;
    protected Icon comboIcon;
    protected boolean iconOnly = false;
    /**
     * This is the focus border painted around the button when it has focus.
     */
    private static Border focusRing;

    private static Border getFocusRing() {
        if (focusRing == null) {
            focusRing = QuaquaBorderFactory.create(
                    Images.createImage(QuaquaComboBoxButton.class.getResource("images/ComboBox.focusRing.png")),
                    new Insets(4, 6, 4, 6),
                    new Insets(0, 0, 0, 0),
                    false);
        }
        return focusRing;
    }

    /**
     * This is the border painted around the cell area.
     */
    private static Border getCellBorder() {
        return UIManager.getBorder("ComboBox.cellBorder");
    }

    /**
     * This is the border painted around the button area.
     */
    private Border getButtonBorder() {
        switch (QuaquaUtilities.getSizeVariant(comboBox)) {
            default:
                return UIManager.getBorder("ComboBox.buttonBorder");
            case SMALL:
                return UIManager.getBorder("ComboBox.smallButtonBorder");
            case MINI:
                return UIManager.getBorder("ComboBox.miniButtonBorder");

        }
    }

    /**
     * This is the border painted around the cell area.
     */
    private static Border getSmallCellBorder() {
        return UIManager.getBorder("ComboBox.smallCellBorder");
    }

    /**
     * This is the border painted around the button area.
     */
    private static Border getSmallButtonBorder() {
        return UIManager.getBorder("ComboBox.smallButtonBorder");
    }

    public final JComboBox getComboBox() {
        return comboBox;
    }

    public final void setComboBox(JComboBox cb) {
        comboBox = cb;
    }

    public final Icon getComboIcon() {
        return comboIcon;
    }

    public final void setComboIcon(Icon i) {
        comboIcon = i;
    }

    public final boolean isIconOnly() {
        return iconOnly;
    }

    public final void setIconOnly(boolean isIconOnly) {
        iconOnly = isIconOnly;
    }

    //QuaquaComboBoxButton() {
    public QuaquaComboBoxButton(QuaquaComboBoxUI ui, JComboBox cb, Icon i, boolean onlyIcon, CellRendererPane pane, JList list) {
        super("");

        DefaultButtonModel model = new DefaultButtonModel() {

            @Override
            public void setArmed(boolean armed) {
                super.setArmed(isPressed() ? true : armed);
            }
        };
        setModel(model);
        setBorder(null); // We do all the border handling in QuaquaComboBoxUI
        comboBox = cb;
        comboIcon = i;
        rendererPane = pane;
        listBox = list;
        setEnabled(comboBox.isEnabled());
        iconOnly = onlyIcon;
    }

    @Override
    public boolean isFocusTraversable() {
        return false;
    }

    @Override
    public void setBorder(Border b) {
        // Empty. We do all border handling in QuaquaComboBoxUI
    }
    /*
    public boolean isOpaque() {
    return false;
    }*/

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        // Set the background and foreground to the combobox colors.
        if (enabled) {
            setBackground(comboBox.getBackground());
            setForeground(comboBox.getForeground());
        } else {
            setBackground(UIManager.getColor("ComboBox.disabledBackground"));
            setForeground(UIManager.getColor("ComboBox.disabledForeground"));
        }
    }

    @Override
    public void paintBorder(Graphics g) {
        // Empty: We paint the border in paintComponent.
    }

    @Override
    public void paintComponent(Graphics g) {
        Object savedHints = QuaquaUtilities.beginGraphics((Graphics2D) g);
        QuaquaComboBoxUI ui = (QuaquaComboBoxUI) comboBox.getUI();
        int buttonWidth = ui.getArrowWidth();
        boolean isTableCellEditor = ui.isTableCellEditor();
        QuaquaUtilities.SizeVariant sizeVariant = QuaquaUtilities.getSizeVariant(comboBox);
        boolean isSmall = sizeVariant == QuaquaUtilities.SizeVariant.SMALL;
        Insets insets = getInsets();
        // Paint background and borders
        int x, y, width, height;
        x = insets.left;
        y = insets.top;
        width = getWidth() - insets.left - insets.right;
        height = getHeight() - insets.top - insets.bottom;

        if (comboBox.isOpaque()) {
            g.setColor(comboBox.getBackground());
            g.fillRect(0, 0, width, height);
        }
        if (!isTableCellEditor) {
            if (iconOnly) {
                Border border = getButtonBorder();
                if (border != null) {
                    border.paintBorder(this, g, x, y, width, height);
                }
            } else {
                Border border;
                switch (sizeVariant) {
                    case SMALL:
                        border = UIManager.getBorder("ComboBox.smallCellAndButtonBorder");
                        break;
                    case MINI:
                        border = UIManager.getBorder("ComboBox.miniCellAndButtonBorder");
                        break;
                    default:
                        border = UIManager.getBorder("ComboBox.cellAndButtonBorder");
                        break;
                }
                if (border != null) {
                    border.paintBorder(this, g, x, y, width, height);
                } else {
                    // this code is used by the non-native versions of Quaqua.
                    border = (isSmall) ? getSmallCellBorder() : getCellBorder();
                    if (border != null) {
                        border.paintBorder(this, g,
                                x, y, width - buttonWidth, height);
                    }
                    border = (isSmall) ? getSmallButtonBorder() : getButtonBorder();
                    if (border != null) {
                        border.paintBorder(this, g,
                                width - buttonWidth, y, buttonWidth, height);
                    }
                }
            }
        }


        boolean leftToRight = QuaquaUtilities.isLeftToRight(comboBox);


        // Paint the icon
        comboIcon = ui.getArrowIcon();
        if (comboIcon != null) {
            int iconWidth = comboIcon.getIconWidth();
            int iconHeight = comboIcon.getIconHeight();
            int iconTop = 0;
            int iconLeft = 0;

            if (iconOnly) {
                iconLeft = x + (width - buttonWidth) / 2 + (buttonWidth - iconWidth) / 2 - 2;
                iconTop = y + (height - iconHeight) / 2;
            } else {
                if (leftToRight) {
                    iconLeft = x + width - buttonWidth + (buttonWidth - iconWidth) / 2 - 1;
                } else {
                    iconLeft = 0;
                }
                iconTop = y + (height - iconHeight) / 2;
                //if (isSmallSizeVariant) iconTop--;
            }
            comboIcon.paintIcon(this, g, iconLeft, iconTop);

            // Paint the focus
            if (QuaquaUtilities.isFocused(comboBox) && !isTableCellEditor) {
                Border border = null;
                border = getFocusRing();
                if (border != null) {
                    border.paintBorder(this, g, x, y, width, height);
                }
            }
        }

        // Let the renderer paint
        if (!iconOnly && comboBox != null) {
            ListCellRenderer renderer = comboBox.getRenderer();

            Component c;
            boolean renderPressed = getModel().isPressed();
            c = renderer.getListCellRendererComponent(listBox,
                    comboBox.getSelectedItem(),
                    -1,
                    renderPressed,
                    false);
            c.setFont(comboBox.getFont());
            c.setEnabled(comboBox.isEnabled());

            Rectangle cellBounds = ((QuaquaComboBoxUI) comboBox.getUI()).rectangleForCurrentValue();

            // Fix for 4238829: should lay out the JPanel.
            boolean shouldValidate = false;
            if (c instanceof JPanel) {
                shouldValidate = true;
            }


            boolean wasOpaque = c.isOpaque();

            if (c instanceof JComponent) {
                ((JComponent) c).setOpaque(false);
            }
            if (leftToRight) {
                rendererPane.paintComponent(g, c, this,
                        cellBounds.x - getX(), cellBounds.y - getY(), cellBounds.width, cellBounds.height, shouldValidate);
            } else {
                rendererPane.paintComponent(g, c, this,
                        cellBounds.x, cellBounds.y, cellBounds.width, cellBounds.height, shouldValidate);
            }
            if (c instanceof JComponent) {
                ((JComponent) c).setOpaque(wasOpaque);
            }
        }
        QuaquaUtilities.endGraphics((Graphics2D) g, savedHints);
    }
}
