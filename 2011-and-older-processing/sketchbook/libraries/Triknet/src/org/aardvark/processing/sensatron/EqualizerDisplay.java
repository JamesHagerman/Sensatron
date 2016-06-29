package org.aardvark.processing.sensatron;

import ddf.minim.analysis.FFT;

public class EqualizerDisplay implements LightDisplay, Equalizer {
	
	private transient FFT fft;
	private int[] bandSettings = new int[0];
	private int maxBandSetting;
	private double[] levels = new double[0];
	private int[] levelMultipliers = new int[0];
	private int globalLevelMultiplier = 10;
	private boolean useGlobalMultiplier = true;
	private double multiplierStep = .1;
	private double[] maxLevels = new double[0];
	private int inactiveBrightness = 20;
	private int activeBrightness = 255;

	public EqualizerDisplay(FFT fft, int[] bandSettings, int maxBandSetting) {
		this.fft = fft;
		setBandSettings(bandSettings);
		this.maxBandSetting = maxBandSetting;
	}
	
	public int getColor(double strandNum, double lightNum) {
		int strand = (int) Math.floor(levels.length * strandNum);
		if (strand >= levels.length) {
			strand = levels.length - 1;
		}
		if (lightNum < levels[strand]) {
			return getActiveColor(lightNum);
		} else {
			return getInactiveColor(lightNum);
		}
	}

	protected int getInactiveColor(double lightNum) {
		if (lightNum < .4) {
			return color(0, inactiveBrightness, 0);
		} else if (lightNum < .8) {
			return color(inactiveBrightness, inactiveBrightness, 0);
		} else {
			return color(inactiveBrightness, 0, 0);
		}
	}
	
	public static int color(int x, int y, int z) {
		return 0xff000000 | (x << 16) | (y << 8) | z;
	}

	protected int getActiveColor(double lightNum) {
		if (lightNum < .4) {
			return color(0, activeBrightness, 0);
		} else if (lightNum < .8) {
			return color(activeBrightness, activeBrightness, 0);
		} else {
			return color(activeBrightness, 0, 0);
		}
	}

	public void update() {
		for (int band = 0; band+1 < fft.avgSize(); band++) {
			displayLevel(band, fft.getAvg(band+1));
		}
		
		/*
		int band = 0;
		double totalLevel = 0;
		int specSize = fft.specSize();
//		println("specSize is " + specSize);
		int bandSize = 0;
		int bandMax = (int) (bandSettings[band] * (specSize/maxBandSetting));
//		println("Band " + band + " starts at i=0, max is " + bandMax);
		for (int i = 0; i < specSize && band < bandSettings.length; i++) {
			if (i > bandMax) {
//				println("Band " + band + " has " + bandSize + " segments");
				double avgLevel = totalLevel / bandSize;
				double levelMultiplier = 1;
				if (useGlobalMultiplier) {
					levelMultiplier = Math.pow(globalLevelMultiplier*multiplierStep, band);
				} else {
					if (band < levelMultipliers.length) {
						levelMultiplier = levelMultipliers[band] * multiplierStep;
					}
				}
				avgLevel *= levelMultiplier;
				displayLevel(band, avgLevel);
				totalLevel = 0;
				bandSize = 0;
				band++;
				if (band < bandSettings.length) {
					bandMax = (int) (bandSettings[band] * (specSize/maxBandSetting));
//					println("Slider " + band + " is at position " + bandSettings[band]);
//					println("Band " + band + " starts at i=" + i + ", max is " + bandMax);
				}
			}
			float level = fft.getBand(i);
			totalLevel += level;
			bandSize++;

		}
		if (band < bandSettings.length) {
//			println("Band " + band + " has " + bandSize + " segments");
			double avgLevel = totalLevel / bandSize;
			displayLevel(band, avgLevel);
		}
		*/
	}

	void displayLevel(int bandNum, double level) {
		if (bandNum >= levels.length) {
//			System.out.println("band out of range: " + bandNum);
			return;
		}
		if (level > maxLevels[bandNum]) {
			maxLevels[bandNum] = level;
		} else {
			maxLevels[bandNum] *= .9999;
		}
//		if (bandNum == 0)
//			System.out.println("New max level: " + maxLevels[bandNum]);

		levels[bandNum] = level/(maxLevels[bandNum]/1.2);

	}
	
	public void setEq(FFT fft) {
		this.fft = fft;
	}

	public int[] getBandSettings() {
		return bandSettings;
	}

	public void setBandSettings(int[] bandSettings) {
		this.bandSettings = bandSettings;
		if (levels.length != bandSettings.length){ 
			this.levels = new double[bandSettings.length];
		}
		if (maxLevels.length != bandSettings.length){ 
			this.maxLevels = new double[bandSettings.length];
		}
	}
	
	public void resetMaxLevels() {
		this.maxLevels = new double[bandSettings.length];
	}
	
	public int getMaxBandSetting() {
		return maxBandSetting;
	}

	public void setMaxBandSetting(int maxBandSetting) {
		this.maxBandSetting = maxBandSetting;
	}

	public int getInactiveBrightness() {
		return inactiveBrightness;
	}

	public void setInactiveBrightness(int inactiveBrightness) {
		this.inactiveBrightness = inactiveBrightness;
	}

	public int getActiveBrightness() {
		return activeBrightness;
	}

	public void setActiveBrightness(int activeBrightness) {
		this.activeBrightness = activeBrightness;
	}

	public int getGlobalLevelMultiplier() {
		return globalLevelMultiplier;
	}

	public void setGlobalLevelMultiplier(int globalLevelMultiplier) {
		this.globalLevelMultiplier = globalLevelMultiplier;
	}

	public boolean isUseGlobalMultiplier() {
		return useGlobalMultiplier;
	}

	public void setUseGlobalMultiplier(boolean useGlobalMultiplier) {
		this.useGlobalMultiplier = useGlobalMultiplier;
	}

	public double getMultiplierStep() {
		return multiplierStep;
	}

	public void setMultiplierStep(double multiplierStep) {
		this.multiplierStep = multiplierStep;
	}

	public void setLevelMultipliers(int[] levelMultipliers) {
		this.levelMultipliers = levelMultipliers;
	}
}
