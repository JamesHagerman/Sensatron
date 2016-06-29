package org.aardvark.processing.sensatron;

import java.awt.Color;

import ddf.minim.analysis.FFT;

public class ColorEqDisplay extends EqualizerDisplay {

	public static final int GRADIENT_MODE = 0;
	public static final int OVERLAP_MODE = 1;
	
	int red1 = 0;
	int green1 = 255;
	int blue1 = 0;
	int red2 = 0;
	int green2 = 0;
	int blue2 = 255;
	private int mode = 1;
	
	public ColorEqDisplay(FFT fft, int[] bandSettings, int maxBandSetting) {
		super(fft, bandSettings, maxBandSetting);
		setInactiveBrightness(0);
	}

	public ColorEqDisplay(FFT fft, int[] bandSettings, int maxBandSetting, int color) {
		this(fft, bandSettings, maxBandSetting, color, color);
	}

	public ColorEqDisplay(FFT fft, int[] bandSettings, int maxBandSetting, int bottomColor, int topColor) {
		this(fft, bandSettings, maxBandSetting);
		setTopColor(topColor);
		setBottomColor(bottomColor);
	}

	@Override
	protected int getActiveColor(double lightNum) {
		int result = 0x00ff0000;
		double red, green, blue, brightness;
		switch (mode) {
		case GRADIENT_MODE:
			red = red1 * lightNum + red2 * (1-lightNum);
			green = green1 * lightNum + green2 * (1-lightNum);
			blue = blue1 * lightNum + blue2 * (1-lightNum);
			brightness = getActiveBrightness() / 255.0;
			red *= brightness;
			green *= brightness;
			blue *= brightness;
			result = color((int)red,(int)green,(int)blue);
			break;
		case OVERLAP_MODE:
			brightness = getActiveBrightness() / 255.0;
			red = red1 * brightness;
			green = green1 * brightness;
			blue = blue1 * brightness;
			result = color((int)red,(int)green,(int)blue);
			break;
		}
		return result;
	}

	@Override
	protected int getInactiveColor(double lightNum) {
		int result = 0x00ff0000;
		double red, green, blue, brightness;
		switch (mode) {
		case GRADIENT_MODE:
			red = red1 * lightNum + red2 * (1-lightNum);
			green = green1 * lightNum + green2 * (1-lightNum);
			blue = blue1 * lightNum + blue2 * (1-lightNum);
			brightness = getInactiveBrightness() / 255.0;
			red *= brightness;
			green *= brightness;
			blue *= brightness;
			result = color((int)red,(int)green,(int)blue);
			break;
		case OVERLAP_MODE:
			brightness = getInactiveBrightness() / 255.0;
			red = red2 * brightness;
			green = green2 * brightness;
			blue = blue2 * brightness;
			result = color((int)red,(int)green,(int)blue);
			break;
		}
		return result;
	}
	
	public void setTopColor(int color) {
		red2 = color>>16 & 0xff;
		green2 = color>>8 & 0xff;
		blue2 = color & 0xff;
//		System.out.println(red2 + ", " + green2 + ", " + blue2);
	}
	
	public void setTopHue(float hue) {
		float[] hsb = Color.RGBtoHSB(red2, green2, blue2, null);
		setTopColor(Color.HSBtoRGB(hue, hsb[1], hsb[2]));
	}

	public void setTopSaturation(float saturation) {
		float[] hsb = Color.RGBtoHSB(red2, green2, blue2, null);
		setTopColor(Color.HSBtoRGB(hsb[0], saturation, hsb[2]));
	}

	public int getTopColor() {
		return color(red2, green2, blue2);
	}
	
	public void setBottomColor(int color) {
		red1 = color>>16 & 0xff;
		green1 = color>>8 & 0xff;
		blue1 = color & 0xff;
//		System.out.println(red1 + ", " + green1 + ", " + blue1);
	}
	
	public void setBottomHue(float hue) {
		float[] hsb = Color.RGBtoHSB(red1, green1, blue1, null);
		setBottomColor(Color.HSBtoRGB(hue, hsb[1], hsb[2]));
	}

	public void setBottomSaturation(float saturation) {
		float[] hsb = Color.RGBtoHSB(red1, green1, blue1, null);
		setBottomColor(Color.HSBtoRGB(hsb[0], saturation, hsb[2]));
	}

	public int getBottomColor() {
		return color(red1, green1, blue1);
	}

	public int getMode() {
		return mode;
	}

	public void setMode(int mode) {
		this.mode = mode;
	}

}
