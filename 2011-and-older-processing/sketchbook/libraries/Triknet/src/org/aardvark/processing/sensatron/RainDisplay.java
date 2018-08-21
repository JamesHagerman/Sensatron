package org.aardvark.processing.sensatron;

import java.awt.Color;
import java.io.Serializable;
import java.util.Random;

import ddf.minim.analysis.BeatDetect;

public class RainDisplay implements LightDisplay, BeatDetector {
	
	RainDrop[][] pane;
	double viscosity;
	double precip;
	float hue;
	float saturation = 1f;
	float brightness = 1f;
	RainMode mode = RainMode.BRIGHTNESS;
	float minHue = 0f;
	float maxHue = 1f;
	boolean random;
	
	private transient BeatDetect beat;
	private boolean showBeats;
	
	int updateCounter = 0;
	
	public RainDisplay(int color, int size, double viscosity, double precip) {
		pane = new RainDrop[size][size];
		for (int x = 0; x < size; x++) {
			for (int y = 0; y < size; y++) {
				pane[x][y] = new RainDrop(getRandomHue(), 0f);
			}
		}
		this.viscosity = viscosity;
		this.precip = precip;
		Color c = new Color(color);
		float[] hsb = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
		this.hue = hsb[0];
		this.saturation = hsb[1];
	}

	public int getColor(double strandNum, double lightNum) {
		int col = (int) Math.floor(strandNum * (pane.length-1));
		int row = (int) Math.floor(lightNum * (pane[col].length-1));
		RainDrop drop = pane[col][row];
		float hue = drop.getHue();
		float brightness = this.brightness;
		float saturation = this.saturation;
		switch (mode) {
		case BRIGHTNESS:
			brightness = drop.getSize();
			break;
		case SATURATION:
			saturation = drop.getSize();
			break;
		}
		
		brightness = Math.min(brightness, 1);
		saturation = Math.min(saturation, 1);
		return Color.HSBtoRGB(hue, saturation, brightness);
	}

	public void update() {
//		if (updateCounter++ < 5) return;
//		updateCounter = 0;
		boolean onBeat = false;
		if (beat != null) {
			if (showBeats && beat.isOnset()) onBeat = true;
		}
		for (RainDrop[] column : pane) {
			int height = column.length;
			for (int i = height - 1; i >= 0; i--) {
				RainDrop drop = column[i];
				float size = drop.getSize();
				if (Math.random() < precip) {
					drop.setSize(size + .02f);
				}
				if (i == 0 && onBeat) {
					drop.setSize(size + 1f);
				}
//				if (column[i] > 1) column[i] = 1;
				if (Math.random() * 2 < size * viscosity) {
					if (i < height - 1) {
						column[i+1] = column[i+1].merge(drop);
					}
					drop.setSize(0f);
					drop.setHue(getRandomHue());
				}
			}
		}
	}
	
	private float getRandomHue() {
		if (!random) {
			return hue;
		} else {
			return new Random().nextFloat() * (maxHue - minHue) + minHue;
		}
	}

	public double getViscosity() {
		return viscosity;
	}

	public void setViscosity(double viscosity) {
		this.viscosity = viscosity;
	}

	public double getPrecip() {
		return precip;
	}

	public void setPrecip(double precip) {
		this.precip = precip;
	}

	public float getHue() {
		return hue;
	}

	public void setHue(float hue) {
		this.hue = hue;
	}

	public float getSaturation() {
		return saturation;
	}

	public void setSaturation(float saturation) {
		this.saturation = saturation;
	}

	public BeatDetect getBeat() {
		return beat;
	}

	public void setBeat(BeatDetect beat) {
		this.beat = beat;
	}

	public boolean isShowBeats() {
		return showBeats;
	}

	public void setShowBeats(boolean showBeats) {
		this.showBeats = showBeats;
	}

	private class RainDrop implements Serializable {
		float hue;
		float size;
		
		public RainDrop(float hue, float size) {
			this.hue = hue;
			this.size = size;
		}
		
		public RainDrop merge(RainDrop incoming) {
			if (this.size < incoming.size) {
				this.hue = incoming.hue;
			}
			this.size += incoming.size;
			return this;
		}

		public float getHue() {
			return hue;
		}

		public void setHue(float hue) {
			this.hue = hue;
		}

		public float getSize() {
			return size;
		}

		public void setSize(float size) {
			this.size = size;
		}
		
	}
	
	public enum RainMode {
		BRIGHTNESS,
		SATURATION
	}

	public float getBrightness() {
		return brightness;
	}

	public void setBrightness(float brightness) {
		this.brightness = brightness;
	}

	public RainMode getMode() {
		return mode;
	}

	public void setMode(RainMode mode) {
		this.mode = mode;
	}

	public float getMinHue() {
		return minHue;
	}

	public void setMinHue(float minHue) {
		this.minHue = minHue;
	}

	public float getMaxHue() {
		return maxHue;
	}

	public void setMaxHue(float maxHue) {
		this.maxHue = maxHue;
	}

	public boolean isRandom() {
		return random;
	}

	public void setRandom(boolean random) {
		this.random = random;
	}
}
