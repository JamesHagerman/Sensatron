package org.aardvark.sensatron.lights;

import java.awt.Color;

import org.aardvark.sensatron.model.LightParams;

import ddf.minim.analysis.BeatDetect;

public class SpectrumDisplay implements LightDisplay {
	
	private static final int PULSE_DURATION_MILLIS = 100;

	int strands;
	int lights;
	BeatDetect beat;
	long now;
	long lastFrame;
	long lastBeat;
	long extraTime;
	LightParams params;
	
	public SpectrumDisplay(BeatDetect beat) {
		this.beat = beat;
	}
	
	@Override
	public void setup(int numStrands, int numLights) {
		this.strands = numStrands;
		this.lights = numLights;
	}

	@Override
	public int getColor(int strandNum, int lightNum) {
		int wavelength = (params.getSlider4() + 2); // lights per wave
		float speed = 10; // lights per second
		float frequency = speed / wavelength; // waves per second
		long time = now + extraTime;
		float saturation = params.getSaturation() / 100f;
		float start = params.getHue1() / 255f;
		float end = params.getHue2() / 255f;
		if (end < start) end += 1;
		float span = end - start;
		int millisPerLight = (int) (1000 / speed);
		float phase = ((time + lightNum * millisPerLight) % (int) (1000/frequency)) / (500.0f/frequency);
		if (phase > 1) {
			phase = 2 - phase;
		}
		float hue = (start + (span * phase));
		return Color.HSBtoRGB(hue, saturation, 1f);

	}

	@Override
	public void update(LightParams params) {
		lastFrame = now;
		now = System.currentTimeMillis();
		this.params = params;
		if (beat.isOnset() || params.isVirtualBeat()) {
			lastBeat = now;
		}
		if (lastBeat + PULSE_DURATION_MILLIS > now) {
			extraTime += (now - lastFrame) * 3; // 4x speed
		}
	}

}
