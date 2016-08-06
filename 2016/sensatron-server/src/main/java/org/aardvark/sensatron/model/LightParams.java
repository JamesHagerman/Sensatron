package org.aardvark.sensatron.model;

public class LightParams {
	
	private boolean on;

	public boolean isOn() {
		return on;
	}

	public void setOn(boolean on) {
		this.on = on;
	}

	@Override
	public String toString() {
		return "LightParams [on=" + on + "]";
	}

}
