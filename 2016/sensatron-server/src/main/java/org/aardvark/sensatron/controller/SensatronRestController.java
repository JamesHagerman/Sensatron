package org.aardvark.sensatron.controller;

import java.io.IOException;
import java.util.Base64;

import org.aardvark.sensatron.lights.LightsController;
import org.aardvark.sensatron.model.FluidSimRequest;
import org.aardvark.sensatron.model.LightParams;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;

@RestController
@RequestMapping("/")
public class SensatronRestController {

	Gson gson = new Gson();

	@Autowired
	LightsController lightsController;

	// Logger instance
	private static final Logger logger = Logger.getLogger(SensatronRestController.class);

	@RequestMapping(value = "/lights", method = RequestMethod.GET)
	public String getLightParams(@RequestParam(value = "version", required = false, defaultValue = "1") int version) {
		// Return the current params:
		return gson.toJson(lightsController.getParams());
	}

	@RequestMapping(value = "/lights", method = RequestMethod.POST)
	public String setLightParams(@RequestParam(value = "toggle", required = false) Boolean toggle,
								@RequestParam(value = "flashlight", required = false) Boolean flashlight,
								@RequestParam(value = "directInput", required = false) Boolean directInput,
								@RequestParam(value = "beat", required = false) Boolean beat,
								@RequestParam(value = "mode", required = false) Integer mode,
								@RequestParam(value = "blendMode", required = false) Integer blendMode,
								@RequestParam(value = "saturation", required = false) Integer saturation,
								@RequestParam(value = "slider4", required = false) Integer slider4,
								@RequestParam(value = "slider5", required = false) Integer slider5,
								@RequestParam(value = "hue1", required = false) Integer hue1,
								@RequestParam(value = "hue2", required = false) Integer hue2) {
		LightParams lightParams = lightsController.getParams();
		if (toggle != null && toggle) {
			if (lightParams.isOn()) {
				lightParams.setOn(false);
			} else {
				lightParams.setOn(true);
			}
		}
		if (flashlight != null && flashlight) {
			if (lightParams.isFlashlight()) {
				lightParams.setFlashlight(false);
			} else {
				lightParams.setFlashlight(true);
			}
		}
		if (directInput != null && directInput) {
			if (lightParams.isDirectInput()) {
				lightParams.setDirectInput(false);
			} else {
				lightParams.setDirectInput(true);
			}
		}
		if (hue1 != null) {
			lightParams.setHue1(hue1);
		}
		if (hue2 != null) {
			lightParams.setHue2(hue2);
		}
		if (saturation != null) {
			lightParams.setSaturation(saturation);
		}
		if (slider4 != null) {
			lightParams.setSlider4(slider4);
		}
		if (slider5 != null) {
			lightParams.setSlider5(slider5);
		}
		if (mode != null) {
			lightParams.setMode(mode);
		}
		if (blendMode != null) {
			lightParams.setBlendMode(blendMode);
		}
		if (beat != null) {
			lightParams.setVirtualBeat(beat);
		}
		lightsController.setParams(lightParams);

		return gson.toJson(lightParams);
	}

	@RequestMapping(value = "/trigger", method = RequestMethod.POST)
	public String trigger(@RequestParam(value = "which", required = true) Integer which) {
		switch (which) {
		case 1:
			lightsController.trig1();
			break;
		case 2:
			lightsController.trig2();
			break;
		case 3:
			lightsController.trigAR();
			break;
		}
		return "";
	}

	@RequestMapping(value = "/lights", method = RequestMethod.PUT)
	public String putSomething(@RequestBody String request, @RequestParam(value = "version", required = false, defaultValue = "1") int version) {

		// if (logger.isDebugEnabled()) {
		// 	logger.debug("Start putSomething");
		// 	logger.debug("data: '" + request + "'");
		// }

		LightParams params = gson.fromJson(request, LightParams.class);
		lightsController.setParams(params);
		return gson.toJson(params);

		// String response = null;
		//
		// try {
		// 	switch (version) {
		// 	case 1:
		// 		if (logger.isDebugEnabled())
		// 			logger.debug("in version 1");
		// 		// TODO: add your business logic here
		// 		response = "Response from Spring RESTful Webservice : "+ request;
		//
		// 		break;
		// 	default:
		// 		throw new Exception("Unsupported version: " + version);
		// 	}
		// } catch (Exception e) {
		// 	response = e.getMessage().toString();
		// }
		//
		// if (logger.isDebugEnabled()) {
		// 	logger.debug("result: '" + response + "'");
		// 	logger.debug("End putSomething");
		// }
		// return response;
	}

	@RequestMapping(value = "/lightData", method = RequestMethod.PUT)
	public String putLightData(@RequestBody String request) {
		// logger.debug("Start lightData");
		// logger.debug("data: '" + request +"'");
		FluidSimRequest req = new Gson().fromJson(request, FluidSimRequest.class);
		int numStrands = req.getNumLightProbeRadials();
		int numLights = req.getNumLightProbeLightsPerRadial();
		if (numStrands > lightsController.getNumStrands()) {
			logger.warn("Light array configuration does not match; FluidSim expects " + numStrands + " strands");
			numStrands = lightsController.getNumStrands();
		}
		if (numLights > lightsController.getStrandLength()) {
			logger.warn("Light array configuration does not match; FluidSim expects " + numLights + " lights");
			numLights = lightsController.getStrandLength();
		}
		int strand = numStrands - 1;
		int light = 0;
		byte[] decoded = Base64.getDecoder().decode(req.getLightProbeData().trim());
		// logger.debug("data length: " + decoded.length + " pixel count: " + decoded.length/3 + " num lights: " + numLights + " num strands: " + numStrands);
		for (int i = 0; i < decoded.length - 2; i += 3) {
			lightsController.setDirectInput(strand, light, lightsController.color(decoded[i], decoded[i+1], decoded[i+2]));
			light++;
			if (light >= numLights) {
				light = 0;
				strand--;
			}
			if (strand < 0) { // strand starts at numStrands-1 and counts down
				logger.warn("Color data longer than expected: " + decoded.length);
				break;
			}
		}
		return "{ response: 'Sensatron ok!'}";
	}

	@RequestMapping(value = "/lights", method = RequestMethod.DELETE)
	public void deleteSomething(@RequestBody String request,@RequestParam(value = "version", required = false, defaultValue = "1") int version) {

		if (logger.isDebugEnabled()) {
			logger.debug("Start putSomething");
			logger.debug("data: '" + request + "'");
		}

		String response = null;

		try {
			switch (version) {
			case 1:
				if (logger.isDebugEnabled())
					logger.debug("in version 1");
				// TODO: add your business logic here

				break;
			default:
				throw new Exception("Unsupported version: " + version);
			}
		} catch (Exception e) {
			logger.info(e.getMessage());
		}

		if (logger.isDebugEnabled()) {
			logger.debug("result: '" + response + "'");
			logger.debug("End putSomething");
		}
	}

	@RequestMapping(value = "/shutdown", method = RequestMethod.POST)
	public String shutdown() {
		try {
			Runtime.getRuntime().exec("sudo shutdown -h now");  // I don't know if 'shutdown' works in cygwin, but I don't want to find out
	//			Runtime.getRuntime().exec("shutdown -r now");  // The real shutdown command (or something like it)

			// Probably don't actually have time to return anything... but we might as well try.

			// System.exit(0);
			return "Shutting down...";
		} catch (IOException e) {
			logger.error("Couldn't shutdown...", e);
			return e.toString();
		}
	}

	@RequestMapping(value = "/reboot", method = RequestMethod.POST)
	public String reboot() {
		try {
			Runtime.getRuntime().exec("sudo shutdown -r now");  // I don't know if 'shutdown' works in cygwin, but I don't want to find out
//			Runtime.getRuntime().exec("shutdown -r now");  // The real shutdown command (or something like it)

			// Probably don't actually have time to return anything... but we might as well try.

			// System.exit(0);
			return "Rebooting...";
		} catch (IOException e) {
			logger.error("Couldn't reboot...", e);
			return e.toString();
		}
	}

	public LightsController getLightsController() {
		return lightsController;
	}

	public void setLightsController(LightsController lightsController) {
		this.lightsController = lightsController;
	}
}
