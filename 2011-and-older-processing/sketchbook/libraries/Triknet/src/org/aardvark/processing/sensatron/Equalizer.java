package org.aardvark.processing.sensatron;

import ddf.minim.analysis.FFT;

public interface Equalizer {

	void setEq(FFT fft);
	void resetMaxLevels();
	
}
