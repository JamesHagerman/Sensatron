package org.aardvark.sensatron.model;

public class FluidSimRequest {
	private int NumLightProbeRadials;
	private int NumLightProbeLightsPerRadial;
	private String LightProbeData;
	
	public int getNumLightProbeRadials() {
		return NumLightProbeRadials;
	}
	public void setNumLightProbeRadials(int numLightProbeRadials) {
		NumLightProbeRadials = numLightProbeRadials;
	}
	public int getNumLightProbeLightsPerRadial() {
		return NumLightProbeLightsPerRadial;
	}
	public void setNumLightProbeLightsPerRadial(int numLightProbeLightsPerRadial) {
		NumLightProbeLightsPerRadial = numLightProbeLightsPerRadial;
	}
	public String getLightProbeData() {
		return LightProbeData;
	}
	public void setLightProbeData(String lightProbeData) {
		LightProbeData = lightProbeData;
	}
	
	
}
