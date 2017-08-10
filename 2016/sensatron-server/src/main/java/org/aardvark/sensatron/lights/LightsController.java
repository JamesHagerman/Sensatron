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

// Added by james as a hack for various things:
import java.io.OutputStream;
import java.util.*;
import java.util.stream.*;
import java.lang.Float.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;


public class LightsController implements Runnable {
	
	public static final int RED   = 0xff0000;
	public static final int GREEN = 0x00ff00;
	public static final int BLUE  = 0x0000ff;

	private final Thread renderThread = new Thread(this);
	public Minim minim;
	private String mediaPath = "";
	public AudioInput in;
	private FFT fft;
	private FFT fftLog;
	private BeatDetect beat;
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
	int[][] directInput = new int[STRANDS][STRAND_LENGTH];
	int SPACING = 5;

	// ANIMATION REQUIRED:
	int globalTime = 0;
	int ledCount = 0;
	float goodFFTBuckets[] = new float[STRAND_LENGTH];
	float goodFFTLog[] = new float[STRAND_LENGTH];
	int fftPeaks[] = new int[1]; // redefined in setup()
	int fftLogPeaks[] = new int[1]; // redefined in setup()
	float currentPeak = 0.0f;
	float currentAverage = 0.0f;

	LightDisplay spectrumDisplay;

	// NETWORK TO LIGHTS:
	Socket socket = null;
	int sockBufferSize = 600*3;
	byte[] bytes = new byte[sockBufferSize];
	OutputStream socketOut = null;
	boolean attemptNetworkLights = true;
	boolean networkLightsConnected = false;

	// Some Envelopes:
	// TODO: Build some curves?
	// TODO: Build some attack curves?
	void updateEnv(int t) {
		updateEnv1(t);
		updateEnv2(t);
		updateAR(t);
	}

	int envMax = 255;
	int env1Value = 0;
	int env1Rate = 4;
	int env2Value = 0;
	int env2Rate = 10;
	public void trig1() {
		log.info("env1 triggered!");
		env1Value = 255;
	}
	public void trig2() {
		log.info("env2 triggered!");
		env2Value = 255;
	}
	void updateEnv1(int t) {
		if (env1Value > 0) {
			env1Value = env1Value - env1Rate;
			if (env1Value < 0) {
				env1Value = 0;
			}
			if (env1Value > envMax) {
				env1Value = envMax;
			}
			log.info(" env1 value: " + env1Value);
		}
	}
	void updateEnv2(int t) {
		if (env2Value > 0) {
			env2Value = env2Value - env2Rate;
			if (env2Value < 0) {
				env2Value = 0;
			}
			if (env2Value > envMax) {
				env2Value = envMax;
			}
			log.info(" env2 value: " + env2Value);
		}
	}

	// AR Envelope
	int arValue = 0;
	int arState = -1; //-1 = settled, 0=A, 1=R
	int aRate = 10;
	int rRate = 5;
	public void trigAR() {
		log.info("arTriggered!");
		arState = 0; // attack mode
	}
	void updateAR(int t) {

		if (arState == 0) {
			arValue = arValue + aRate;
		}
		if (arState == 1) {
			arValue = arValue - rRate;
		}
		if (arValue < 0) {
			arValue = 0;
		}
		if (arValue > envMax) {
			arValue = envMax;
		}

		if (arState >= 0) {
				log.info(" ar envelope value: " + arValue);
		}

		// Change state if needed:
		if (arState == 0 && arValue >= envMax) {
			arState = 1; // release mode
		}
		if (arState == 1 && arValue <= 0) {
			arState = -1; //settled
		}
	}

	// AnimationBlob states:
	int blobCount = STRAND_LENGTH;
	int blobParamCount = 2;
	int blobs[][] = new int[blobCount][blobParamCount];
	int maxBlobSize = STRAND_LENGTH/2;
	void initBlobs() {
		for (int i = 0; i < blobCount; i++) {
			blobs[i][0] = 0; // 0 holds the size of the blob
			blobs[i][1] = Color.HSBtoRGB((i/(float)blobCount)*255, 1.0f, 1.0f); // 1 holds the color
		}
	}

	void calculateBlobSize(int blobIndex) {

		int newSize = Math.round(goodFFTLog[blobIndex]);
		if (newSize > maxBlobSize) {
			newSize = maxBlobSize;
		}
		blobs[blobIndex][0] = newSize;
	}

	void updateBlobs() {
		for (int i = 0; i < blobCount; i++) {
			calculateBlobSize(i);
		}
	}


	// DEBUG:
	boolean derp = false;

	void setup() {
		log.info("SETUP!");
		try {
			minim = new Minim(this);
		  in = minim.getLineIn(Minim.STEREO, bufferSize);
		  in.enableMonitoring();
			fft = new FFT( in.bufferSize(), in.sampleRate() );
		  beat = new BeatDetect(in.bufferSize(), in.sampleRate());
		  beat.detectMode(BeatDetect.SOUND_ENERGY);
		  fftLog = new FFT( in.bufferSize(), in.sampleRate() );

		  fftLog.logAverages( 10, 8 );
			log.info("fftLog.avgSize(): " + fftLog.avgSize());

			// Redefine our peaks array:
			fftPeaks = new int[fft.specSize()];
			fftLogPeaks = new int[fftLog.avgSize()];


			if (in.isMonitoring()) {
				log.info("Audio should be monitoring...");
			}

		} catch(Exception e) {
			log.error("Couldn't open audio? " + e);
		}

		spectrumDisplay = new SpectrumDisplay(beat);

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

		// Attempt to connect to network display
		openSocket();


		renderThread.start();
	}

	public void run() {
		log.info("Render thread started.");
		while (!stopping) {
			// Use the same LightParams throughout the rendering process, even if a new one is passed in
			LightParams p = getParams();
			// Reset the virtual beat detector right away, so we don't miss anything
			params.setVirtualBeat(false);

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
		globalTime++; // TODO base this on REAL time, not frames
		updateEnv(globalTime);
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

		if (!p.isOn()) {
			setAllLights(0);
		} else if (p.isFlashlight()) {
			setAllLights(0xffffff);
		}

		// REQUIRED CODE:
		mapDrawingToLights(p); // lights[][] -> pixel[]
		drawLEDs(); // draws pixel[] to the LEDs.
		// YUP.
	}

	void exit()
	{
		stop();
		log.info("Trying to stop everything gracefully...");
		closeSocket();
		in.close();
		TotalControl.close();
		log.info("Done! Exiting!");
	}


	// Audio handler:
	public int compare(int i, int j) {
		// This is inverted...  with *-1
		return Float.compare(goodFFTBuckets[i], goodFFTBuckets[j]) * -1;
	}

	void processAudio() {
	  // AUDIO DRAW:
	  fft.forward( in.mix );
	  fftLog.forward( in.mix );
	  beat.detect(in.mix);

	  ledCount = 0; // Reset ledCount
	  for(int i = 0; i < fft.specSize(); i++)
	  {
	    // Save the values into the goodFFTBuckets:
	    if (ledCount<STRAND_LENGTH) {
	      goodFFTBuckets[ledCount] = fft.getBand(i);
	    }
	    ledCount++; // increment for the next pass goodFFTLog
	  }

		// log.info("FFT Buckets: " + Arrays.toString(goodFFTBuckets));
		fftPeaks = IntStream.range(0, goodFFTBuckets.length )
                .boxed().sorted((i, j) -> compare(i, j) )
                .mapToInt(ele -> ele).toArray();
		// log.info("FFT SORTED: " + Arrays.toString(fftPeaks));

		// Calculate the current peak and the current total average:
		currentPeak = goodFFTBuckets[fftPeaks[0]];
		float sum = 0.0f;
		for (float d : goodFFTBuckets) {
			sum = sum + d;
		}
		currentAverage = 1.0f * sum / goodFFTBuckets.length;


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

		// log.info("FFT Log Buckets. " + Arrays.toString(goodFFTLog));
		fftLogPeaks = IntStream.range(0, goodFFTLog.length )
                .boxed().sorted((i, j) -> compare(i, j) )
                .mapToInt(ele -> ele).toArray();
		// log.info("FFT Log SORTED: " + Arrays.toString(fftLogPeaks));
	}


	void doArt(LightParams p) {
		// log.info("LightsController thinks: " + p);

		float saturation = p.getSaturation() / 100f;
		float rotate = p.getHue2()/255.0f;
		float rate = p.getSlider4()/100.0f;

		switch (p.getMode()) {
			case LightParams.MODE_SPECTRUM:
				spectrumDisplay.update(p);
				for (int strand = 0; strand < STRANDS; strand++) {
					for (int light = 0; light < STRAND_LENGTH; light++) {
						setOneLight(strand, light, spectrumDisplay.getColor(strand, light));
					}
				}
				return;
			case LightParams.MODE_BLOBS:
				int pickedColor = Color.HSBtoRGB(p.getHue1()/255.0f, saturation, 1.0f);
				shiftAllOut();
				for(int i = 0; i < STRANDS; i++) {
					float rotatedHue = ( goodFFTBuckets[i]/currentPeak ) + rotate;

				  int theColor = Color.HSBtoRGB(rotatedHue, 1.0f, 1.0f);
					int blended = blend(theColor, pickedColor, (env1Value+(env2Value*-1)+arValue)/255.0f);
					setOneLight(i, 0, blended);
				}
				return;
			case LightParams.MODE_FFT:
				shiftAllOut();

				// int cutPoint = (int) (((env2Value+arValue)/255.0f)*STRAND_LENGTH); // 0-STRAND_LENGTH
				// cutPoint = STRAND_LENGTH - cutPoint;


				int cutPoint = Math.round(rate*STRAND_LENGTH-1);
				// log.info("Cut point: "+ cutPoint);
				for(int i = 0; i < cutPoint; i++) {

					// Only care about spikes above the average
					// float roundedVal = 0;
					// if (goodFFTBuckets[i] > currentAverage+(currentAverage*0.1)) {
						// roundedVal = goodFFTBuckets[i];
						// roundedVal = Math.round(goodFFTLog[i]);

						float rotatedHue = (goodFFTBuckets[i]/currentPeak) + rotate;
						int theColor = Color.HSBtoRGB(rotatedHue, 1.0f, 1.0f);
						setOneRing(i, theColor);

						// setOneSpiral(0, i, 1, theColor);
						// setOneLight(currentStrand, i, Color.HSBtoRGB(shiftedTime/255.0f, 1.0f, 1.0f) );
					// }
				}

				// whitenAll(env1Value);

				int toMix = Color.HSBtoRGB(p.getHue1()/255.0f, saturation, 1.0f);
				blendAll(toMix, (env1Value+(env2Value*-1)+arValue)/255.0f);

				// debug
				// int theColor = Color.HSBtoRGB(0/255.0f, 1.0f, 1.0f);
				// setOneRing(currentLED, theColor);

				return;
		}
	}

	public void setDirectInput(int strand, int lightNum, int c) {
		  strand = strand%STRANDS;
		  lightNum = lightNum%STRAND_LENGTH;
		  directInput[strand][lightNum] = c;
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

	// LED DRAWING TOOLS:
	public void setOneLight(int strand, int lightNum, int c) {
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
	// These affect the pixels
	void shiftAllOut() {
		int lightIndex = 0;
		for (int strand = STRANDS-1; strand >= 0; strand--) {
	    for (int lightNum = STRAND_LENGTH-1; lightNum >= 1; lightNum--) {
	      lights[strand][lightNum] = lights[strand][lightNum-1];
	      lightIndex++;
	    }
	  }
	}

 	int blend( int i1, int i2, float ratio ) {
		int toReturn = 0;
    if ( ratio > 1f ) ratio = 1f;
    else if ( ratio < 0f ) ratio = 0f;
    float iRatio = 1.0f - ratio;

    int r1 = ((i1 & 0xff0000) >> 16);
    int g1 = ((i1 & 0xff00) >> 8);
    int b1 = (i1 & 0xff);

    int r2 = ((i2 & 0xff0000) >> 16);
    int g2 = ((i2 & 0xff00) >> 8);
    int b2 = (i2 & 0xff);

    int r = (int)((r1 * iRatio) + (r2 * ratio));
    int g = (int)((g1 * iRatio) + (g2 * ratio));
    int b = (int)((b1 * iRatio) + (b2 * ratio));
		toReturn =  r << 16 | g << 8 | b ;

    return toReturn;
	}

	void whitenAll(int amount) {
		int lightIndex = 0;
		for (int strand = 0; strand < STRANDS; strand++) {
	    for (int lightNum = 0; lightNum < STRAND_LENGTH; lightNum++) {
				int currentValue = lights[strand][lightNum];

				// unpack to bytes
				int r = (currentValue >> 16) & 0xff;
				int g = (currentValue >> 8) & 0xff;
				int b = (currentValue) & 0xff;

				// Do some processing:
				r = r + amount;
				g = g + amount;
				b = b + amount;

				// limit
				if (r > 255) r = 255;
				if (g > 255) g = 255;
				if (b > 255) b = 255;

				// pack
				currentValue = color(r, g, b);

	      lights[strand][lightNum] = currentValue;
	      lightIndex++;
	    }
	  }
	}

	void blendAll(int baseColor, float amount) {
		int lightIndex = 0;
		for (int strand = 0; strand < STRANDS; strand++) {
	    for (int lightNum = 0; lightNum < STRAND_LENGTH; lightNum++) {
				int currentValue = lights[strand][lightNum];
	      lights[strand][lightNum] = blend(currentValue, baseColor, amount);;
	      lightIndex++;
	    }
	  }
	}

	// END LED DRAWING TOOLS

	// TCL Stuff
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
	void mapDrawingToLights(LightParams p) {
	  int lightIndex = 0;
	  for (int strand = 0; strand < STRANDS; strand++) {
	    for (int lightNum = 0; lightNum < STRAND_LENGTH; lightNum++) {
	      pixels[lightIndex] = blend(lights[strand][lightNum], directInput[strand][lightNum], p);
	      lightIndex++;
	    }
	  }
	}

	// Handle blending between art and direct input
	int blend(int color1, int color2, LightParams p) {
		int result = color1;
		switch (p.getBlendMode()) {
		case LightParams.BLEND_MODE_1:
			// X-Fade
			result = blend(color1, color2, p.getSlider5()/100.0f);
			break;
		case LightParams.BLEND_MODE_2:
			int threshold = p.getSlider5();
			if ((color2 & RED) < threshold && (color2 & GREEN) < threshold && (color2 & BLUE) < threshold) {
				result = (int) (color1 * (threshold/255.0));
			}
			break;
		case LightParams.BLEND_MODE_3:
			break;
		case LightParams.BLEND_MODE_4:
			// Hacked Screen blend (results in brighter picture. annnnnd pastel land)
			// int r = (int)( ~((~r1 & 0xff)*(~r2 & 0xff)) & 0xff );
	    // int g = (int)( ~((~g1 & 0xff)*(~g2 & 0xff)) & 0xff );
	    // int b = (int)( ~((~b1 & 0xff)*(~b2 & 0xff)) & 0xff );

			// If Nuc is entirely white and there are splotches of blue coming from tablet
			// we really want to SUBTRACT the INVERTED TABLET colors from the White of the NUC
			// r1 - (~r2 & 0xff)

			// If Nuc is entirely Black for some reason,
			// We really want to ADD the NON-INVERTED tablet colors TO the black of the NUC
			// r1 + r2

			// float[] hsb1 = Color.RGBtoHSB(r1, g1, b1, null);
			// float[] hsb2 = Color.RGBtoHSB(r2, g2, b2, null);
			//
			// if (hsb1[2]>hsb2[2]) {
			// 	return color1;
			// } else {
			// 	return color2;
			// }

			// int mod1 = Color.HSBtoRGB(hsb1[0], hsb1[1], hsb1[2]);
			// int mod2 = Color.HSBtoRGB(hsb2[0], hsb2[1], hsb2[2]);



			// if (r2>r1 || g2>g1 || b2>b1) {
			// 	return color2;
			// } else {
			// 	return color1;
			// }
			break;
		}
		return result;

	}

	// Draw color from pixel[] structure to the LEDs themselves:
	void drawLEDs() {
		if (tcl) {
		  TotalControl.refresh(pixels, remap);
	  }
		if (networkLightsConnected) {
			socketLEDs();
		}
	}

	void openSocket() {
		if (attemptNetworkLights) {
			log.info("Trying to connect to lights server display...");
			try	{
	      Socket socket = new Socket("127.0.0.1", 3001);
				socketOut = socket.getOutputStream();
	    }
	    catch(IOException ex){
	      log.error("Could not connect to the server!");
				networkLightsConnected = false;
				return;
	    }
			networkLightsConnected = true;
		} else {
			log.info("Not trying to connect to lights server display...");
		}
	}
	void closeSocket() {
		if (networkLightsConnected) {
			try	{
				socketOut.close();
				socket.close();
	    }
	    catch(IOException ex){
	      log.error("Could not close connection to server!");
	    }
		}

	}
	void socketLEDs(){
		if (networkLightsConnected) {
			try	{
				int j = 0;
				for (int i = 0; i < sockBufferSize; i+=3) {
					bytes[i] = (byte)(pixels[j] >> 16 & 0xff);
					bytes[i+1] = (byte)((pixels[j] >> 8) & 0xff);
					bytes[i+2] = (byte)((pixels[j]) & 0xff);
					j++;
				}
				socketOut.write(bytes, 0, sockBufferSize);
	    }
	    catch(IOException ex){
	      log.error("Could not send to server!");
				networkLightsConnected = false;
				return;
	    }
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

	public int getNumStrands() {
		return STRANDS;
	}

	public int getStrandLength() {
		return STRAND_LENGTH;
	}
}
