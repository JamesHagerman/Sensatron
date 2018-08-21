package org.aardvark.sensatron.lights;

import org.aardvark.sensatron.model.LightParams;

public interface LightDisplay {
	
	/**
	 * Configure the number of strands and lights.
	 * 
	 * @param numStrands Number of strands
	 * @param numLights Number of lights per strand
	 */
	void setup(int numStrands, int numLights);

	/**
	 * Get the color to use for the given light.
	 * 
	 * @param strandNum
	 * @param lightNum
	 * @return RGB
	 */
	int getColor(int strandNum, int lightNum);
	
	/**
	 * Update the display.
	 * 
	 * @param params
	 */
	void update(LightParams params);
	
}
