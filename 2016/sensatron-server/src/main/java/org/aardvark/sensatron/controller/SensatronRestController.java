package org.aardvark.sensatron.controller;

import java.io.IOException;
import java.util.Base64;

import org.aardvark.sensatron.lights.LightsController;
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
								@RequestParam(value = "beat", required = false) Boolean beat,
								@RequestParam(value = "mode", required = false) Integer mode,
								@RequestParam(value = "saturation", required = false) Integer saturation,
								@RequestParam(value = "slider4", required = false) Integer slider4,
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
		if (mode != null) {
			lightParams.setMode(mode);
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
		LightParams p = lightsController.getParams();
		p.setDirectInput(true);
		lightsController.setParams(p);
		int strand = 0;
		int light = 0;
		byte[] decoded = Base64.getDecoder().decode(request);
		for (int i = 0; i < decoded.length - 2; i += 3) {
			lightsController.setOneLight(strand, light, lightsController.color(decoded[i], decoded[i+1], decoded[i+2]));
			light++;
			if (light > lightsController.getStrandLength()) {
				light = 0;
				strand++;
			}
			if (strand > lightsController.getNumStrands()) {
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
