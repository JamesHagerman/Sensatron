package org.aardvark.processing.sensatron;

import java.awt.Color;

import ddf.minim.analysis.BeatDetect;
import ddf.minim.analysis.FFT;

public class OverlayDisplay implements LightDisplay, Equalizer, BeatDetector {

	private LightDisplay base;
	private String baseName = "None";
	private boolean invertBase = false;
	
	private LightDisplay overlay;
	private String overlayName = "None";
	private boolean invertOverlay = false;
	
	private transient FFT fft;
	private transient BeatDetect beat;
	
	private OverlayMode mode;
	
	public OverlayDisplay(OverlayMode mode) {
		this.mode = mode;
	}
	
	public int getColor(double strandNum, double lightNum) {
		int baseColor = 0;
		if (base != null) {
			baseColor = base.getColor(strandNum, invertBase ? 1 - lightNum : lightNum);
		}
		int overlayColor = 0;
		if (overlay != null) {
			overlayColor = overlay.getColor(strandNum, invertOverlay ? 1 - lightNum : lightNum);
		}
		switch (mode) {
		case ADD:
			int blue = Math.min(baseColor & 0x0000ff + overlayColor & 0x0000ff, 0x0000ff);
			int green = Math.min(baseColor & 0x00ff00 + overlayColor & 0x00ff00, 0x00ff00);
			int red = Math.min(baseColor & 0xff0000 + overlayColor & 0xff0000, 0xff0000);
			return 0xff000000 | red | green | blue;
		case SUBTRACT:
			blue = Math.max(baseColor & 0x0000ff - overlayColor & 0x0000ff, 0);
			green = Math.max(baseColor & 0x00ff00 - overlayColor & 0x00ff00, 0);
			red = Math.max(baseColor & 0xff0000 - overlayColor & 0xff0000, 0);
			return 0xff000000 | red | green | blue;
		case TWEEN:
			blue = (baseColor & 0x0000ff + overlayColor & 0x0000ff) / 2;
			green = (baseColor & 0x00ff00 + overlayColor & 0x00ff00) / 2;
			red = (baseColor & 0xff0000 + overlayColor & 0xff0000) / 2;
			return 0xff000000 | red | green | blue;
		case OPAQUE:
			if (Color.RGBtoHSB(overlayColor & 0xff0000, overlayColor & 0xff00, overlayColor & 0xff, null)[2] > .05f) return overlayColor;
			else return baseColor;
		case BRIGHTNESS:
			// Use the Hue and Saturation of the base display, but add the overlay's Brightness to the base's
			blue = overlayColor & 0x0000ff;
			green = (overlayColor & 0x00ff00) >> 8;
			red = (overlayColor & 0xff0000) >> 16;
			float[] overlayHSB = Color.RGBtoHSB(red, green, blue, null);
			blue = baseColor & 0x0000ff;
			green = (baseColor & 0x00ff00) >> 8;
			red = (baseColor & 0xff0000) >> 16;
			float[] baseHSB = Color.RGBtoHSB(red, green, blue, null);
			float brightness = Math.min(baseHSB[2] + overlayHSB[2], 1f);
//			return 0xff000000 | Color.HSBtoRGB(baseHSB[0], baseHSB[1], brightness);
			return 0xff000000 | Color.HSBtoRGB(baseHSB[0], baseHSB[1], overlayHSB[2]);
		default:
			return baseColor;
		}
	}

	public void update() {
		if (base != null) {
			base.update();
		}
		if (overlay != null) {
			overlay.update();
		}
	}

	public LightDisplay getBase() {
		return base;
	}

	public void setBase(LightDisplay base) {
		this.base = base;
		setDisplayBeat(base);
		setDisplayEq(base);
	}

	public LightDisplay getOverlay() {
		return overlay;
	}

	public void setOverlay(LightDisplay overlay) {
		this.overlay = overlay;
		setDisplayBeat(overlay);
		setDisplayEq(overlay);
	}

	public void setBeat(BeatDetect beatDetect) {
		this.beat = beatDetect;
		setDisplayBeat(base);
		setDisplayBeat(overlay);
	}

	public void setEq(FFT fft) {
		this.fft = fft;
		setDisplayEq(base);
		setDisplayEq(overlay);
	}
	
	private void setDisplayEq(LightDisplay display) {
		if (display != null && display instanceof Equalizer) {
			((Equalizer) display).setEq(fft);
		}
	}
	
	private void setDisplayBeat(LightDisplay display) {
		if (display != null && display instanceof BeatDetector) {
			((BeatDetector) display).setBeat(beat);
		}
	}
	
	public void resetMaxLevels() {
		if (base != null && base instanceof Equalizer) {
			((Equalizer) base).resetMaxLevels();
		}
		if (overlay != null && overlay instanceof Equalizer) {
			((Equalizer) overlay).resetMaxLevels();
		}
	}
	
	public OverlayMode getMode() {
		return mode;
	}

	public void setMode(OverlayMode mode) {
		this.mode = mode;
	}

	public enum OverlayMode {
		/** Use the Base color whenever the Overlay color is black (0,0,0)*/
		OPAQUE,
		
		/** Add each of the red, green, and blue values */
		ADD,
		
		/** Subtract the red, green, and blue values of the Overlay
		 * from those of the Base 
		 */
		SUBTRACT,
		
		/** Take the average of each of the red, green, and blue values */
		TWEEN,
		
		/**Use the Hue and Saturation of the base display, 
		 * and the Brightness of the overlay
		 */
		BRIGHTNESS
		
	}

	public String getBaseName() {
		return baseName;
	}

	public void setBaseName(String baseName) {
		this.baseName = baseName;
	}

	public String getOverlayName() {
		return overlayName;
	}

	public void setOverlayName(String overlayName) {
		this.overlayName = overlayName;
	}

	public boolean isInvertBase() {
		return invertBase;
	}

	public void setInvertBase(boolean invertBase) {
		this.invertBase = invertBase;
	}

	public boolean isInvertOverlay() {
		return invertOverlay;
	}

	public void setInvertOverlay(boolean invertOverlay) {
		this.invertOverlay = invertOverlay;
	}

}
