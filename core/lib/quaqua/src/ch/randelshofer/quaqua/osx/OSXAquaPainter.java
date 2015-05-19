/*
 * @(#)OSXAquaPainter.java  
 * 
 * Copyright (c) 2011-2013 Werner Randelshofer, Switzerland.
 * All rights reserved.
 * 
 * You may not use, copy or modify this file, except in compliance with the
 * license agreement you entered into with Werner Randelshofer.
 * For details see accompanying license terms.
 */
package ch.randelshofer.quaqua.osx;

import ch.randelshofer.quaqua.QuaquaManager;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.Raster;
import java.security.AccessControlException;

/**
 * Renders Aqua user interface controls using the JavaRuntimeSupport framework
 * API which is present in OS X 10.6 and 10.7.
 * <p>
 * References:<br>
 * <a 
 * href="http://hg.openjdk.java.net/macosx-port/macosx-port/jdk/file/tip/src/macosx/classes/com/apple/laf/"
 * >OpenJDK LaF classes</a><br>
 *
 * <a href="http://hg.openjdk.java.net/macosx-port/macosx-port/jdk/file/tip/src/macosx/classes/apple/laf/"
 * >OpenJDK JRSUIControl classes</a><br>
 * 
 * <a href="http://hg.openjdk.java.net/macosx-port/macosx-port/jdk/file/tip/src/macosx/native/com/apple/laf/"
 * >
 * OpenJDK native code.
 * </a>
 *</p> 
 * @author Werner Randelshofer
 * @version $Id: OSXAquaPainter.java 458 2013-05-29 15:44:33Z wrandelshofer $
 */
public class OSXAquaPainter {

    /**
     * This variable is set to true, if native code is available.
     */
    private static volatile Boolean isNativeCodeAvailable;
    /** The handle to the native control. */
    private long handle;
    private Widget widget;
    /**
     * Version of the native code library.
     */
    private final static int EXPECTED_NATIVE_CODE_VERSION = 1;

    /**
     * Load the native code.
     */
    public static boolean isNativeCodeAvailable() {
        if (isNativeCodeAvailable == null) {
            synchronized (OSXApplication.class) {
                if (isNativeCodeAvailable == null) {
                    boolean success = false;
                    try {
                        // Note: The following line ensures that AWT is started,
                        // and has initialized NSApplication, before we attempt
                        // to access it.
                        Toolkit.getDefaultToolkit().getSystemEventQueue();

                        String value = QuaquaManager.getProperty("Quaqua.jniIsPreloaded");
                        if (value == null) {
                            value = QuaquaManager.getProperty("Quaqua.JNI.isPreloaded");
                        }
                        if (value != null && value.equals("true")) {
                            success = true;
                        } else {
                            // Try to load 64-bit libraries if possible
                            String[] libraryNames;
                            String osArch = System.getProperty("os.arch");
                            if (osArch.equals("x86_64")) {
                                libraryNames = new String[]{"quaqua64"};
                            } else {
                                libraryNames = new String[]{"quaqua64", "quaqua"};
                            }
                            for (String libraryName : libraryNames) {
                                try {
                                    JNILoader.loadLibrary(libraryName);
                                    success = true;
                                    break;
                                } catch (UnsatisfiedLinkError e) {
                                    System.err.println("Warning: " + OSXApplication.class + " couldn't load library \"" + System.mapLibraryName(libraryName) + "\". " + e);
                                    success = false;
                                } catch (AccessControlException e) {
                                    System.err.println("Warning: " + OSXApplication.class + " access controller denied loading library \"" + System.mapLibraryName(libraryName) + "\". " + e);
                                    success = false;
                                } catch (Throwable e) {
                                    e.printStackTrace();
                                    System.err.println("Warning: " + OSXApplication.class + " couldn't load library \"" + System.mapLibraryName(libraryName) + "\". " + e);
                                    success = false;
                                }
                            }
                        }

                        if (success) {
                            int nativeCodeVersion = nativeGetNativeCodeVersion();
                            if (nativeCodeVersion != EXPECTED_NATIVE_CODE_VERSION) {
                                System.err.println("Warning: " + OSXApplication.class + " can't use library libquaqua.jnilib. It has version " + nativeCodeVersion + " instead of " + EXPECTED_NATIVE_CODE_VERSION);
                                success = false;
                            }
                        }

                    } finally {
                        isNativeCodeAvailable = Boolean.valueOf(success);
                    }
                }
            }
        }
        return isNativeCodeAvailable == Boolean.TRUE;
    }

    public void dispose() {
        releaseControl();
    }

    public Widget getWidget() {
        return widget;
    }

    /** Property keys. */
    public enum Key {

        widget(1),
        state(2),
        size(3),
        direction(4),
        orientation(5),
        verticalAlignment(6),
        horizontalAlignment(7),
        position(8),
        pressedPart(9),
        variant(10),
        windowType(11),
        focused(12),
        indicatorOnly(13),
        noIndicator(14),
        nothingToScroll(15),
        arrowsOnly(16),
        frameOnly(17),
        segmentTrailingSeparator(18),
        maximumValue(19),
        value(20),
        animationStartTime(21),
        animationTime(22),
        animationFrame(23),
        thumbProportion(24),
        thumbStart(25),
        windowFrameDrawClipped(26),
        windowFrameDrawTitleSeparator(27),
        windowTitleBarHeight(28);
        //
        private int id;

        private Key(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    };

    public enum Widget {

        background(1),
        buttonBevel(2),
        buttonBevelInset(3),
        buttonBevelRound(4),
        buttonCheckBox(5),
        buttonComboBox(6),
        buttonComboBoxInset(7),
        buttonDisclosure(8),
        buttonListHeader(9),
        buttonLittleArrows(10),
        buttonPopDown(11),
        buttonPopDownInset(12),
        buttonPopDownSquare(13),
        buttonPopUp(14),
        buttonPopUpInset(15),
        buttonPopUpSquare(16),
        buttonPush(17),
        buttonPushScope(18),
        buttonPushScope2(19),
        buttonPushTextured(20),
        buttonPushInset(21),
        buttonPushInset2(22),
        buttonRadio(23),
        buttonRound(24),
        buttonRoundHelp(25),
        buttonRoundInset(26),
        buttonRoundInset2(27),
        buttonSearchFieldCancel(28),
        buttonSearchFieldFind(29),
        buttonSegmented(30),
        buttonSegmentedInset(31),
        buttonSegmentedInset2(32),
        buttonSegmentedSCurve(33),
        buttonSegmentedTextured(34),
        buttonSegmentedToolbar(35),
        dial(36),
        disclosureTriangle(37),
        dividerGrabber(38),
        dividerSeparatorBar(39),
        dividerSplitter(40),
        focus(41),
        frameGroupBox(42),
        frameGroupBoxSecondary(43),
        frameListBox(44),
        framePlacard(45),
        frameTextField(46),
        frameTextFieldRound(47),
        frameWell(48),
        growBox(49),
        growBoxTextured(50),
        gradient(51),
        menu(52),
        menuItem(53),
        menuBar(54),
        menuTitle(55),
        progressBar(56),
        progressIndeterminateBar(57),
        progressRelevance(58),
        progressSpinner(59),
        scrollBar(60),
        scrollColumnSizer(61),
        slider(62),
        sliderThumb(63),
        synchronization(64),
        tab(65),
        titleBarCloseBox(66),
        titleBarCollapseBox(67),
        titleBarZoomBox(68),
        titleBarToolbarButton(69),
        toolbarItemWell(70),
        windowFrame(71);
        //
        private int id;

        private Widget(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }

    public enum State {

        active(1),
        inactive(2),
        disabled(3),
        pressed(4),
        pulsed(5),
        rollover(6),
        drag(7);
        //
        private int id;

        private State(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }

    public enum Size {

        mini(1),
        small(2),
        regular(3),
        large(4);
        //
        private int id;

        private Size(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }

    public enum Direction {

        none(1),
        up(2),
        down(3),
        left(4),
        right(5),
        north(6),
        south(7),
        east(8),
        west(9);
        //
        private int id;

        private Direction(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }

    public enum Orientation {

        horizontal(1),
        vertical(2);
        //
        private int id;

        private Orientation(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }

    public enum HorizontalAlignment {

        left(1),
        center(2),
        right(3);
        //
        private int id;

        private HorizontalAlignment(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }

    public enum VerticalAlignment {

        top(1),
        center(2),
        bottom(3);
        //
        private int id;

        private VerticalAlignment(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }

    public enum SegmentPosition {

        first(1),
        middle(2),
        last(3),
        only(4);
        //
        private int id;

        private SegmentPosition(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }

    public enum ScrollBarPart {

        none(1),
        thumb(2),
        arrowMin(3),
        arrowMax(4),
        arrowMaxInside(5),
        arrowMinInside(6),
        trackMin(7),
        trackMax(8);
        //
        private int id;

        private ScrollBarPart(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }

    public enum Variant {

        menuGlyph(1),
        menuPopup(2),
        menuPulldown(3),
        menuHierarchical(4),
        gradientListBackgroundEven(5),
        gradientListBackgroundOdd(6),
        gradientSideBar(7),
        gradientSideBarSelection(8),
        gradientSideBarFocusedSelection(9);
        //
        private int id;

        private Variant(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }

    public enum WindowType {

        document(1),
        utility(2),
        titlelessUtility(3);
        //
        private int id;

        private WindowType(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }

    private boolean createControl() {
        if (handle == 0 && isNativeCodeAvailable()) {
            handle = nativeCreateControl(false);
        }
        return handle != 0;
    }

    private void releaseControl() {
        if (handle != 0 && isNativeCodeAvailable()) {
            nativeReleaseControl(handle);
            handle = 0;
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        releaseControl();
    }

    /** Sets the widget type of the JRSUIControl. */
    public void setWidget(Widget widget) {
        this.widget=widget;
        if (createControl()) {
            nativeSetWidget(handle, widget.getId());
        }
    }

    /** Sets the state of the JRSUIControl. */
    public void setState(State state) {
        if (createControl()) {
            nativeSetState(handle, state.getId());
        }
    }

    /** Sets a key value of the JRSUIControl. */
    public void setValueByKey(Key key, double value) {
        if (createControl()) {
            nativeSetValueByKey(handle, key.getId(), value);
        }
    }

    /** Sets the size variant of the JRSUIControl. */
    public void setSize(Size size) {
        if (createControl()) {
            nativeSetSize(handle, size.getId());
        }
    }

    /** Sets the direction of the JRSUIControl. */
    public void setDirection(Direction direction) {
        if (createControl()) {
            nativeSetDirection(handle, direction.getId());
        }
    }

    /** Sets the orientation of the JRSUIControl. */
    public void setOrientation(Orientation orientation) {
        if (createControl()) {
            nativeSetOrientation(handle, orientation.getId());
        }
    }

    /** Sets the horizontal alignment of the JRSUIControl. */
    public void setHorizontalAlignment(HorizontalAlignment halignment) {
        if (createControl()) {
            nativeSetHorizontalAlignment(handle, halignment.getId());
        }
    }

    /** Sets the vertical alignment of the JRSUIControl. */
    public void setVerticalAlignment(VerticalAlignment valignment) {
        if (createControl()) {
            nativeSetVerticalAlignment(handle, valignment.getId());
        }
    }

    /** Sets the segment position of the JRSUIControl. */
    public void setSegmentPosition(SegmentPosition segpos) {
        if (createControl()) {
            nativeSetSegmentPosition(handle, segpos.getId());
        }
    }

    /** Specifies the desired scroll bar part of the JRSUIControl. */
    public void setScrollBarPart(ScrollBarPart sbpart) {
        if (createControl()) {
            nativeSetScrollBarPart(handle, sbpart.getId());
        }
    }

    /** Specifies the desired variant of the JRSUIControl. */
    public void setVariant(Variant variant) {
        if (createControl()) {
            nativeSetVariant(handle, variant.getId());
        }
    }

    /** Specifies the desired window type of the JRSUIControl. */
    public void setWindowType(WindowType wtype) {
        if (createControl()) {
            nativeSetWindowType(handle, wtype.getId());
        }
    }

    /** Specifies whether to show arrows on a JRSUIControl. */
    public void setShowArrows(boolean b) {
        if (createControl()) {
            nativeSetShowArrows(handle, b);
        }
    }

    /** Specifies whether to animate a JRSUIControl. */
    public void setAnimating(boolean b) {
        if (createControl()) {
            nativeSetAnimating(handle, b);
        }
    }

    /** Paints the widget on the specified image. 
     * The image data must be of type {@code BufferedImage.TYPE_INT_ARGB_PRE}.
     */
    public void paint(int[] imageData, int imgWidth, int imgHeight,//
            double x, double y, double width, double height) {
        if (createControl()) {
            nativePaint(imageData, imgWidth, imgHeight, handle, x, imgHeight - y - height, width, height);
        }
    }

    /** Paints the widget on the specified image. 
     * The image data must be of type {@code BufferedImage.TYPE_INT_ARGB_PRE}.
     * 
     * @throws IllegalArgumentException if the image type is not {@code BufferedImage.TYPE_INT_ARGB_PRE}. 
     */
    public void paint(BufferedImage image,//
            double x, double y, double width, double height) {
        if (image.getType() != BufferedImage.TYPE_INT_ARGB_PRE) {
            throw new IllegalArgumentException("Unsupported image type=" + image.getType());
        }
        if (createControl()) {
            int[] imgData = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

            int imgWidth = image.getWidth(), imgHeight = image.getHeight(), imgX = 0, imgY = 0;
            Raster raster = image.getRaster();
            while (raster.getParent() != null) {
                imgX -= raster.getMinX();
                imgY -= raster.getMinY();
                imgWidth = raster.getWidth();
                imgHeight = raster.getHeight();
            }

            nativePaint(imgData, imgWidth, imgHeight, handle, x, imgHeight - y - height, width, height);
        }
    }

    /**
     * Returns the version of the native code library. If the version
     * does not match with the version that we expect, we can not use
     * it.
     * @return The version number of the native code.
     */
    private static native int nativeGetNativeCodeVersion();

    /** Creates a JRSUIControl and returns a handle to it. */
    private static native long nativeCreateControl(boolean isFlipped);

    /** Disposes of the JRSUIControl. */
    private static native void nativeReleaseControl(long handle);

    /** Sets a property value on a JRSUIControl. */
    private static native void nativeSetValueByKey(long ctrlHandle, int key, double value);

    /** Sets the widget type on a JRSUIControl. */
    private static native void nativeSetWidget(long ctrlHandle, int widget);

    /** Sets the state of the JRSUIControl. */
    private static native void nativeSetState(long ctrlHandle, int state);

    /** Sets the size variant of the JRSUIControl. */
    private static native void nativeSetSize(long ctrlHandle, int size);

    /** Sets the direction of the JRSUIControl. */
    private static native void nativeSetDirection(long ctrlHandle, int direction);

    /** Sets the orientation of the JRSUIControl. */
    private static native void nativeSetOrientation(long ctrlHandle, int orientation);

    /** Sets the horizontal alignment of the JRSUIControl. */
    private static native void nativeSetHorizontalAlignment(long ctrlHandle, int halignment);

    /** Sets the vertical alignment of the JRSUIControl. */
    private static native void nativeSetVerticalAlignment(long ctrlHandle, int valignment);

    /** Sets the segment position of the JRSUIControl. */
    private static native void nativeSetSegmentPosition(long ctrlHandle, int segpos);

    /** Specifies the desired scroll bar part of the JRSUIControl. */
    private static native void nativeSetScrollBarPart(long ctrlHandle, int sbpart);

    /** Specifies the desired variant of the JRSUIControl. */
    private static native void nativeSetVariant(long ctrlHandle, int variant);

    /** Specifies the desired window type of the JRSUIControl. */
    private static native void nativeSetWindowType(long ctrlHandle, int wtype);

    /** Specifies whether to show arrows on a JRSUIControl. */
    private static native void nativeSetShowArrows(long ctrlHandle, boolean b);

    /** Specifies whether to animate a JRSUIControl. */
    private static native void nativeSetAnimating(long ctrlHandle, boolean b);

    /** Paints the widget on the specified image. 
     * Note: The coordinate system of the native paint method has its origin
     * at the lower left corner. (Java has the origin at the top left corner.
     */
    private static native void nativePaint(int[] imgData, int imgWidth, int imgHeight,//
            long ctrlHandle, double x, double y, double width, double height);
}
