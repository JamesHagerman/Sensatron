package org.aardvark.sensatron.lights;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

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
	int LED_COUNT = STRAND_LENGTH; // Total number of lights

	int strandCount = STRANDS/2; // two wands per "strand" of output
	int pixelsOnStrand = STRAND_LENGTH*2; // twice as many pixels per "strand"
	int totalPixels = strandCount * pixelsOnStrand;

	int[] pixels = new int[totalPixels];
	int[] remap = new int[totalPixels];
	/* Could also use PImage and loadPixels()/updatePixels()...many options! */

	int[][] lights = new int[STRANDS][STRAND_LENGTH];
	int SPACING = 5;

	void setup()
	{
		log.info("SETUP!");
		try {
			minim = new Minim(this);
		  in = minim.getLineIn(Minim.STEREO, bufferSize);
		  in.enableMonitoring();
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

	void draw(LightParams p)
	{
		if (p.isOn()) {
			log.trace("Turning lights on");
			setAllLights(color(255,255,255));
		} else {
			log.trace("Turning lights off");
			setAllLights(0);
		}
	  mapDrawingToLights();

	  // Set some random pixel to full white:
	  //int x = (int)random(totalPixels);
	  //p[x]  = 0x00ffffff;

	  // Draw the p array to the lights using the remap function:

	  if (tcl) {
		  TotalControl.refresh(pixels, remap);
	  }

	  // Set the random pixel back to black for the next pass:
	  //p[x]  = 0;
	}

	void exit()
	{
		stop();
		TotalControl.close();
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
//	          if (i == 1) {
//	            log.debug("index " + index + " is: " + remap[index]);
//	          }
	      } else { // odd led's (1,3,5,7...)
	         remap[(STRAND_LENGTH * (i+1)) - (j-(j/2))] = index;
//	          if (i == 1) {
//	            log.debug("index " + index + " is: " + remap[index]);
//	          }
	      }
	      index += 1;
	    }
	  }

	  log.debug("Done building remap array.");
	}

	// Random color generator:
	int getRandomColor() {
	  return color((int)random(255), (int)random(255), (int)random(255));
	}

	// Setting all lights to some color:
	void setAllLights(int c) {
	  for (int strand = 0; strand < STRANDS; strand++) {
	    for (int lightNum = 0; lightNum < STRAND_LENGTH; lightNum++) {
	      lights[strand][lightNum] = c;
	    }
	  }
	}

	// Method to move colors from the lights[][] multi-dimensional array
	// and to the lights array: p[]
	void mapDrawingToLights() {
	  int lightIndex = 0;
	  for (int strand = 0; strand < STRANDS; strand++) {
	    for (int lightNum = 0; lightNum < STRAND_LENGTH; lightNum++) {
	      pixels[lightIndex] = lights[strand][lightNum];
	      lightIndex++;
	    }
	  }
	}

	public int color(int red, int green, int blue) {
		return red << 16 | green << 8 | blue;
	}

	public int random(int max) {
		return (int) (Math.random() * (max + 1));
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
		log.debug("Params set to: " + params);
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
