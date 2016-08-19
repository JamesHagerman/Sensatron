package org.aardvark.sensatron.controller;

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
	public String getSomething(@RequestParam(value = "request") String request,	@RequestParam(value = "version", required = false, defaultValue = "1") int version) {
		
		return gson.toJson(lightsController.getParams());
	}

	@RequestMapping(value = "/lights", method = RequestMethod.POST)
	public String postSomething(@RequestParam(value = "toggle", required = false) Boolean toggle,
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
		if (hue1 != null) {
			lightParams.setHue1(hue1);
		}
		if (hue2 != null) {
			lightParams.setHue2(hue2);
		}
		lightsController.setParams(lightParams);
		
		return gson.toJson(lightParams);
	}

	@RequestMapping(value = "/<add method name here>", method = RequestMethod.PUT)
	public String putSomething(@RequestBody String request,@RequestParam(value = "version", required = false, defaultValue = "1") int version) {
		
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
				response = "Response from Spring RESTful Webservice : "+ request;

				break;
			default:
				throw new Exception("Unsupported version: " + version);
			}
		} catch (Exception e) {
			response = e.getMessage().toString();
		}

		if (logger.isDebugEnabled()) {
			logger.debug("result: '" + response + "'");
			logger.debug("End putSomething");
		}
		return response;
	}

	@RequestMapping(value = "/<add method name here>", method = RequestMethod.DELETE)
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

	public LightsController getLightsController() {
		return lightsController;
	}

	public void setLightsController(LightsController lightsController) {
		this.lightsController = lightsController;
	}
}
