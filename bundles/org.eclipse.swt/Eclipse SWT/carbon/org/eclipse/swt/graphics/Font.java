package org.eclipse.swt.graphics;

/*
 * Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
 * This file is made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */

import org.eclipse.swt.internal.carbon.*;
import org.eclipse.swt.*;

/**
 * Instances of this class manage operating system resources that
 * define how text looks when it is displayed. Fonts may be constructed
 * by providing a device and either name, size and style information
 * or a <code>FontData</code> object which encapsulates this data.
 * <p>
 * Application code must explicitly invoke the <code>Font.dispose()</code> 
 * method to release the operating system resources managed by each instance
 * when those instances are no longer required.
 * </p>
 *
 * @see FontData
 */
public final class Font {

	/**
	 * the handle to the OS font (a FMFont)
	 * (Warning: This field is platform dependent)
	 */
	public int handle;
	
	/**
	 * the id to the OS font (a FMFontFamily)
	 * (Warning: This field is platform dependent)
	 */
	public short id;
	
	/**
	 * the style to the OS font (a FMFontStyle)
	 * (Warning: This field is platform dependent)
	 */
	public short style;

	/**
	 * the size to the OS font
	 * (Warning: This field is platform dependent)
	 */
	public short size;

	/**
	 * The device where this font was created.
	 */
	Device device;
	
	/**
	 * The ATSUI style for the font.
	 */
	int atsuiStyle;
	
Font() {
}

/**	 
 * Constructs a new font given a device and font data
 * which describes the desired font's appearance.
 * <p>
 * You must dispose the font when it is no longer required. 
 * </p>
 *
 * @param device the device to create the font on
 * @param fd the FontData that describes the desired font (must not be null)
 * 
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if device is null and there is no current device</li>
 *    <li>ERROR_NULL_ARGUMENT - if the fd argument is null</li>
 * </ul>
 * @exception SWTError <ul>
 *    <li>ERROR_NO_HANDLES - if a font could not be created from the given font data</li>
 * </ul>
 */
public Font(Device display, FontData fd) {
	if (device == null) device = Device.getDevice();
	if (device == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
	if (fd == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
	init(device, fd.getName(), fd.getHeight(), fd.getStyle());
}

/**	 
 * Constructs a new font given a device and font datas
 * which describes the desired font's appearance.
 * <p>
 * You must dispose the font when it is no longer required. 
 * </p>
 *
 * @param device the device to create the font on
 * @param fds the array of FontData that describes the desired font (must not be null)
 * 
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if device is null and there is no current device</li>
 *    <li>ERROR_NULL_ARGUMENT - if the fds argument is null</li>
 *    <li>ERROR_INVALID_ARGUMENT - if the length of fds is zero</li>
 *    <li>ERROR_NULL_ARGUMENT - if any fd in the array is null</li>
 * </ul>
 * @exception SWTError <ul>
 *    <li>ERROR_NO_HANDLES - if a font could not be created from the given font data</li>
 * </ul>
 */
public Font(Device device, FontData[] fds) {
	if (device == null) device = Device.getDevice();
	if (device == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
	if (fds == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
	if (fds.length == 0) SWT.error(SWT.ERROR_INVALID_ARGUMENT);
	FontData fd = fds[0];
	if (fd == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
	init(device,fd.getName(), fd.getHeight(), fd.getStyle());
}

/**	 
 * Constructs a new font given a device, a font name,
 * the height of the desired font in points, and a font
 * style.
 * <p>
 * You must dispose the font when it is no longer required. 
 * </p>
 *
 * @param device the device to create the font on
 * @param name the name of the font (must not be null)
 * @param height the font height in points
 * @param style a bit or combination of NORMAL, BOLD, ITALIC
 * 
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if device is null and there is no current device</li>
 *    <li>ERROR_NULL_ARGUMENT - if the name argument is null</li>
 *    <li>ERROR_INVALID_ARGUMENT - if the height is negative</li>
 * </ul>
 * @exception SWTError <ul>
 *    <li>ERROR_NO_HANDLES - if a font could not be created from the given arguments</li>
 * </ul>
 */
public Font(Device display, String name, int height, int style) {
	if (device == null) device = Device.getDevice();
	if (device == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
	init(device, name, height, style);
}

int createStyle () {
	int[] buffer = new int[1];
	OS.ATSUCreateStyle(buffer);
	if (buffer[0] == 0) SWT.error(SWT.ERROR_NO_HANDLES);
	int ptr = OS.NewPtr(16);
	OS.memcpy(ptr, new int[]{handle}, 4); 
	OS.memcpy(ptr + 4, new int[]{size << 16}, 4); 
	OS.memcpy(ptr + 8, new byte[]{(style & OS.bold) != 0 ? (byte)1 : 0}, 1); 
	OS.memcpy(ptr + 9, new byte[]{(style & OS.italic) != 0 ? (byte)1 : 0}, 1); 
	int[] tags = new int[]{OS.kATSUFontTag, OS.kATSUSizeTag, OS.kATSUQDBoldfaceTag, OS.kATSUQDItalicTag};
	int[] sizes = new int[]{4, 4, 1, 1};
	int[] values = new int[]{ptr, ptr + 4, ptr + 8, ptr + 9};
	OS.ATSUSetAttributes(buffer[0], tags.length, tags, sizes, values);
	OS.DisposePtr(ptr);
	return buffer [0];
}

/**
 * Disposes of the operating system resources associated with
 * the font. Applications must dispose of all fonts which
 * they allocate.
 */
public void dispose() {
	if (handle == 0) return;
	handle = 0;
	id = -1;
	if (atsuiStyle != 0) OS.ATSUDisposeStyle(atsuiStyle);
	atsuiStyle = 0;
	device = null;
}

/**
 * Compares the argument to the receiver, and returns true
 * if they represent the <em>same</em> object using a class
 * specific comparison.
 *
 * @param object the object to compare with this object
 * @return <code>true</code> if the object is the same as this object and <code>false</code> otherwise
 *
 * @see #hashCode
 */
public boolean equals(Object object) {
	if (object == this) return true;
	if (!(object instanceof Font)) return false;
	return handle == ((Font)object).handle;
}

/**
 * Returns an array of <code>FontData</code>s representing the receiver.
 * On Windows, only one FontData will be returned per font. On X however, 
 * a <code>Font</code> object <em>may</em> be composed of multiple X 
 * fonts. To support this case, we return an array of font data objects.
 *
 * @return an array of font data objects describing the receiver
 *
 * @exception SWTException <ul>
 *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 */
public FontData[] getFontData() {
	if (isDisposed()) SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
	byte[] buffer = new byte[256];
	OS.FMGetFontFamilyName(id, buffer);
	int length = buffer[0] & 0xFF;
	char[] chars = new char[length];
	for (int i=0; i<length; i++) {
		chars[i]= (char)buffer[i+1];
	}
	String name = new String(chars);
	int style = SWT.NORMAL;
	if ((this.style & OS.italic) != 0) style |= SWT.ITALIC;
	if ((this.style & OS.bold) != 0) style |= SWT.BOLD;
	FontData data = new FontData(name, size, style);
	return new FontData[]{data};
}

/**	 
 * Invokes platform specific functionality to allocate a new font.
 * <p>
 * <b>IMPORTANT:</b> This method is <em>not</em> part of the public
 * API for <code>Font</code>. It is marked public only so that it
 * can be shared within the packages provided by SWT. It is not
 * available on all platforms, and should never be called from
 * application code.
 * </p>
 *
 * @param device the device on which to allocate the color
 * @param handle the handle for the font
 * @param size the size for the font
 * 
 * @private
 */
public static Font carbon_new(Device device, int handle, short id, short style, short size) {
	if (device == null) device = Device.getDevice();
	Font font = new Font();
	font.handle = handle;
	font.id = id;
	font.style = style;
	font.size = size;
	font.device = device;
	return font;
}

/**
 * Returns an integer hash code for the receiver. Any two 
 * objects which return <code>true</code> when passed to 
 * <code>equals</code> must return the same value for this
 * method.
 *
 * @return the receiver's hash
 *
 * @see #equals
 */
public int hashCode() {
	return handle;
}

void init(Device device, String name, int height, int style) {
	if (name == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
	if (height < 0) SWT.error(SWT.ERROR_INVALID_ARGUMENT);
	byte[] buffer = new byte[256];
	int length = name.length();
	if (length > 255) length = 255;
	buffer[0] = (byte)length;
	for (int i=0; i<length; i++) {
		buffer[i+1]= (byte)name.charAt(i);
	}
	this.id = OS.FMGetFontFamilyFromName(buffer);
	if (this.id == OS.kInvalidFontFamily) this.id = OS.GetAppFont();
	if ((style & SWT.ITALIC) != 0) this.style |= OS.italic;
	if ((style & SWT.BOLD) != 0) this.style |= OS.bold;
	this.size = (short)height;
	int[] font = new int[1];
	if (OS.FMGetFontFromFontFamilyInstance(id, this.style, font, null) != 0) {
		SWT.error(SWT.ERROR_NO_HANDLES);
	}
	if (font[0] == 0) SWT.error(SWT.ERROR_NO_HANDLES);
	this.handle = font[0];
	this.atsuiStyle = createStyle();
}

/**
 * Returns <code>true</code> if the font has been disposed,
 * and <code>false</code> otherwise.
 * <p>
 * This method gets the dispose state for the font.
 * When a font has been disposed, it is an error to
 * invoke any other method using the font.
 *
 * @return <code>true</code> when the font is disposed and <code>false</code> otherwise
 */
public boolean isDisposed() {
	return handle == 0;
}

/**
 * Returns a string containing a concise, human-readable
 * description of the receiver.
 *
 * @return a string representation of the receiver
 */
public String toString () {
	if (isDisposed()) return "Font {*DISPOSED*}";
	return "Font {" + handle + "}";
}

}
