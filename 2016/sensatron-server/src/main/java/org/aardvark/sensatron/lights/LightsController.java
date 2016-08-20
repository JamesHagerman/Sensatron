package org.aardvark.sensatron.lights;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import java.awt.Color;

import org.aardvark.sensatron.model.LightParams;
import org.apache.log4j.Logger;

import TotalControl.TotalControl;
import ddf.minim.*;
import ddf.minim.analysis.*;

public class LightsController implements Runnable {

	private final Thread renderThread = new Thread(this);
	public Minim minim;
	private String mediaPath = "";
	public AudioInput in;
	private FFT fft;
	private FFT fftLog;
	public int bufferSize = 2048;

	private boolean tcl = false; // Are we actually controlling the lights?
	private boolean stopping = false;
	private LightParams params = new LightParams();
	private static Logger log = Logger.getLogger(LightsController.class);

	// The TotalControl processing library doesn't define the FTDI pins so we do:
	short TC_FTDI_TX  = 0x01;  /* Avail on all FTDI adapters,  strand 0 default */
	short TC_FTDI_RX  = 0x02;  /* Avail on all FTDI adapters,  strand 1 default */
	short TC_FTDI_RTS = 0x04;  /* Avail on FTDI-branded cable, strand 2 default */
	short TC_FTDI_CTS = 0x08;  /* Avail on all FTDI adapters,  clock default    */
	short TC_FTDI_DTR = 0x10;  /* Avail on third-party cables, strand 2 default */
	short TC_FTDI_DSR = 0x20;  /* Avail on full breakout board */
	short TC_FTDI_DCD = 0x40;  /* Avail on full breakout board */
	short TC_FTDI_RI  = 0x80;  /* Avail on full breakout board */

	int STRANDS = 12; // Number of physical wands
	int STRAND_LENGTH = 50; // Number of lights per strand
	int LED_COUNT = STRANDS*STRAND_LENGTH; // Total number of lights

	int strandCount = STRANDS/2; // two wands per "strand" of output
	int pixelsOnStrand = STRAND_LENGTH*2; // twice as many pixels per "strand"
	int totalPixels = strandCount * pixelsOnStrand;

	int[] pixels = new int[totalPixels];
	int[] remap = new int[totalPixels];
	/* Could also use PImage and loadPixels()/updatePixels()...many options! */

	int[][] lights = new int[STRANDS][STRAND_LENGTH];
	int SPACING = 5;

	// ANIMATION REQUIRED:
	int globalTime = 0;
	int ledCount = 0;
	float goodFFTBuckets[] = new float[STRAND_LENGTH];
	float goodFFTLog[] = new float[STRAND_LENGTH];

	// DEBUG:
	boolean derp = false;

	void setup() {
		log.info("SETUP!");
		try {
			minim = new Minim(this);
		  in = minim.getLineIn(Minim.STEREO, bufferSize);
		  in.enableMonitoring();
			fft = new FFT( in.bufferSize(), in.sampleRate() );
		  fftLog = new FFT( in.bufferSize(), in.sampleRate() );
		  fftLog.logAverages( 10, 8 );
			log.info("fftLog.avgSize(): " + fftLog.avgSize());
			if (in.isMonitoring()) {
				log.info("Audio should be monitoring...");
			}
		} catch(Exception e) {
			log.error("Couldn't open audio? " + e);
		}

		try {
		  // This will build the Remapping array that will
		  // wrap the LED strands back on themselves for
		  // the higher density the Sensatron loves so much:
		  buildRemapArray();

		  // Override the default pin outs:
		  // This is clock. We don't want to override it:
		  //tc.setStrandPin(x,TC_FTDI_CTS);
		  TotalControl.setStrandPin(0,TC_FTDI_TX); // default
		  TotalControl.setStrandPin(1,TC_FTDI_RX); // default
		  TotalControl.setStrandPin(2,TC_FTDI_DTR); // spliting dtr and rts

		  // Custom lines for the ftdi breakout
		  TotalControl.setStrandPin(3,TC_FTDI_RTS);
		  TotalControl.setStrandPin(4,TC_FTDI_RI);
		  TotalControl.setStrandPin(5,TC_FTDI_DSR);
		  TotalControl.setStrandPin(6,TC_FTDI_DCD);

		  int status = TotalControl.open(strandCount, pixelsOnStrand);
		  if(status == 0) {
			  tcl = true;
		  } else {
				TotalControl.printError(status);
				log.error("Couldn't open connection to TCL light array.");
		  }
		} catch (UnsatisfiedLinkError e) {
			log.error("Couldn't find TCL native library. " + e);
		}
		renderThread.start();
	}

	public void run() {
		log.info("Render thread started.");
		while (!stopping) {
			// Use the same LightParams throughout the rendering process, even if a new one is passed in
			LightParams p = params;
			draw(p);

			try {
				Thread.sleep(33);
			} catch (InterruptedException ignore) {}
		}
		log.info("Render thread stopped.");
	}

	public void stop() {
		log.info("Stopping render thread...");
		stopping = true;
	}

	void draw(LightParams p) {
		globalTime++;
		processAudio();

		// Light debug:
		// if (derp) {
		// if (p.isOn()) {
		// 	log.trace("Turning lights on");
		// 	setAllLights(color(255,255,255));
		// } else {
		// 	log.trace("Turning lights off");
		// 	setAllLights(0);
		// }
		// derp = !derp;


		doArt(p);

		// REQUIRED CODE:
		mapDrawingToLights(); // lights[][] -> pixel[]
		drawLEDs(); // draws pixel[] to the LEDs.
		// YUP.
	}

	void exit()
	{
		stop();
		TotalControl.close();
	}


	// Audio handler:
	void processAudio() {
	  // AUDIO DRAW:
	  fft.forward( in.mix );
	  fftLog.forward( in.mix );

	  ledCount = 0; // Reset ledCount
	  for(int i = 0; i < fft.specSize(); i++)
	  {
	    // Save the values into the goodFFTBuckets:
	    if (ledCount<STRAND_LENGTH) {
	      goodFFTBuckets[ledCount] = fft.getBand(i);
	    }
	    ledCount++; // increment for the next pass goodFFTLog
	  }


	  ledCount = 0; // Reset ledCount
	  // since logarithmically spaced averages are not equally spaced
	  // we can't precompute the width for all averages
	  for(int i = 0; i < fftLog.avgSize(); i++)
	  {
	    // Save the values into the goodFFTBuckets:
	    if (ledCount<STRAND_LENGTH) {
	      goodFFTLog[ledCount] = fftLog.getAvg(i);
	    }
	    ledCount++; // increment for the next pass
	  }
	}


	void doArt(LightParams p) {
	  // int paramValue = 12; //((mouseY*255)/height); // convert to 0-255;
	  // int signedValue = paramValue-(255/2);

	  // if (paramValue == 0) paramValue = 1;

	  // println("derp: " + paramValue);
	  int shiftedTime = Math.abs((int)Math.sin(globalTime)*p.getHue1());

	  // for(int i = 0; i < STRAND_LENGTH; i++) {
		for(int i = 0; i < STRAND_LENGTH; i++) {

	    // int roundedVal = Math.round(goodFFTBuckets[i])*10;
	    int roundedVal = Math.round(goodFFTLog[i]); // *shiftedTime

	    // colorMode(HSB, 255);
	    // color theColor = color(roundedVal, 255, 255);

			int theColor = Color.HSBtoRGB(roundedVal/255.0f, 1.0f, 1.0f);
	    // color theColor = color(globalTime%255, 255, 255);
	    // color theColor = color(roundedVal, roundedVal, roundedVal);
	    setOneRing(i, theColor);
	    // setOneSpiral(0, i, 1, theColor);
	    // colorMode(RGB, 255);
	  }

		// setOneStrand(0, Color.HSBtoRGB(64/255.0f, 1.0f, 1.0f) );
	}

	//=============
	// HELPERS:
	// Random color generator:
	int getRandomColor() {
	  return color((int)random(255), (int)random(255), (int)random(255));
	}
	public int color(int red, int green, int blue) {
		return red << 16 | green << 8 | blue;
	}
	public int random(int max) {
		return (int) (Math.random() * (max + 1));
	}
	// end helpers
	//============
	void setOneLight(int strand, int lightNum, int c) {
	  strand = strand%STRANDS;
	  lightNum = lightNum%STRAND_LENGTH;
	  lights[strand][lightNum] = c;
	}
	void setAllLights(int c) {
	  for (int strand = 0; strand < STRANDS; strand++) {
	    for (int lightNum = 0; lightNum < STRAND_LENGTH; lightNum++) {
	      setOneLight(strand, lightNum, c);
	    }
	  }
	}
	void setOneRing(int lightNum, int c) {
	  for (int i = 0; i < STRANDS; i++) {
	    setOneLight(i, lightNum, c);
	  }
	}
	void setOneStrand(int strand, int c) {
	  for (int i = 0; i < STRAND_LENGTH; i++) {
	    setOneLight(strand, i, c);
	  }
	}
	void setOneSpiral(int strand, int lightNum, int direction, int c) {
	  for (int i = 0; i < STRANDS; i++) {
	    int lightOffset = lightNum + (i * direction);
	    setOneLight(strand + i, lightOffset, c);
	  }
	}

	public void buildRemapArray() {
	  log.debug("Building remap array...");
	  // Linear mapping - No fold back:
	  //for (int i = 0; i < STRANDS * STRAND_LENGTH; i++) {
	  //  remap[i] = i;
	  //}

	  // All 0 mapping - Only touch the... uh... first led? I think?
	  //for (int i = 0; i < STRANDS * STRAND_LENGTH; i++) {
	  //  remap[i] = 0;
	  //}

	  // Working radial remap:
	  int index = 0;
	  for(int i=0; i<STRANDS; i++) {
	    log.trace("Setting wand: " + i);
	    for(int j=0;j<STRAND_LENGTH;j++) {
	      if(j%2==0) { // even led's (0,2,4,6...)
	        remap[j-(j/2) + (STRAND_LENGTH * i)] = index;
	      } else { // odd led's (1,3,5,7...)
	         remap[(STRAND_LENGTH * (i+1)) - (j-(j/2))] = index;
	      }
	      index += 1;
	    }
	  }

	  log.debug("Done building remap array.");
	}
		// Move colors from lights[][] to pixel[] structure
	void mapDrawingToLights() {
	  int lightIndex = 0;
	  for (int strand = 0; strand < STRANDS; strand++) {
	    for (int lightNum = 0; lightNum < STRAND_LENGTH; lightNum++) {
	      pixels[lightIndex] = lights[strand][lightNum];
	      lightIndex++;
	    }
	  }
	}
	// Draw color from pixel[] structure to the LEDs themselves:
	void drawLEDs() {
		if (tcl) {
		  TotalControl.refresh(pixels, remap);
	  }
	}


	public LightParams getParams() {
		try {
			return params.clone();
		} catch (CloneNotSupportedException e) {
			log.error(e.toString() + " -- returning our own params object");
			return params;
		}
	}

	public void setParams(LightParams params) {
		log.trace("Params set to: " + params);
		this.params = params;
	}

	public boolean isTCLConnected() {
		return tcl;
	}


	// Minim helper methods

	/**
	 * The sketchPath method is expected to transform a filename into an absolute path and is used when attempting to create an AudioRecorder.
	 *
	 * @param fileName
	 * @return
	 */
	public String sketchPath( String fileName ) {
		return new File(mediaPath, fileName).getAbsolutePath();
	}

	/**
	 * The createInput method is used when loading files and is expected to take a filename, which is not necessarily an absolute path,
	 * and return an InputStream that can be used to read the file. For example, in Processing, the createInput method will search in
	 * the data folder, the sketch folder, handle URLs, and absolute paths. If you are using Minim outside of Processing, you can handle
	 * whatever cases are appropriate for your project.
	 *
	 * @param fileName
	 * @return
	 * @throws FileNotFoundException
	 */
	public InputStream createInput( String fileName ) throws FileNotFoundException {
		try {
			return new FileInputStream(new File(fileName));
		} catch (FileNotFoundException e) {
			log.debug(fileName + " doesn't seem to be an absolute path...");
		}
		return new FileInputStream(new File(mediaPath, fileName));
	}

	public String getMediaPath() {
		return mediaPath;
	}

	public void setMediaPath(String mediaPath) {
		this.mediaPath = mediaPath;
	}
}
