package org.aardvark.processing.sensatron;

import java.io.Serializable;

public interface LightDisplay extends Serializable {

	int getColor(double strandNum, double lightNum);
	
	void update();
	
}
