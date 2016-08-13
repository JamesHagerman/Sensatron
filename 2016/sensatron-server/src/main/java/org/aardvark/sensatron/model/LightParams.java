package org.aardvark.sensatron.model;

public class LightParams implements Cloneable {
	
	private boolean on;

	public boolean isOn() {
		return on;
	}

	public void setOn(boolean on) {
		this.on = on;
	}

	@Override
	public String toString() {
		return super.toString() + " [on=" + on + "]";
	}

	@Override
	public LightParams clone() throws CloneNotSupportedException {
		return (LightParams) super.clone();
	}
}
