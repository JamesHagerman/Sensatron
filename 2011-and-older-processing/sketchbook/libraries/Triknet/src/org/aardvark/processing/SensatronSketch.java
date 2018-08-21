package org.aardvark.processing;

import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

import javax.swing.JFileChooser;
import javax.swing.UIManager;

import org.aardvark.processing.delegate.ActionDelegate;
import org.aardvark.processing.sensatron.BeatDetector;
import org.aardvark.processing.sensatron.ColorEqDisplay;
import org.aardvark.processing.sensatron.DisplayControlWidget;
import org.aardvark.processing.sensatron.DisplayControlWidgetFactory;
import org.aardvark.processing.sensatron.Equalizer;
import org.aardvark.processing.sensatron.EqualizerDisplay;
import org.aardvark.processing.sensatron.LightDisplay;
import org.aardvark.processing.sensatron.OverlayDisplay;
import org.aardvark.processing.sensatron.RainDisplay;
import org.magiclamp.Triknet;

import processing.core.PApplet;
import ddf.minim.AudioInput;
import ddf.minim.AudioSource;
import ddf.minim.Minim;
import ddf.minim.analysis.BeatDetect;
import ddf.minim.analysis.FFT;

public class SensatronSketch extends PApplet {


	// Set this to false if the light controller is not connected
	boolean CONNECT_TO_TRIKLITS = false;

	// Number of light strands
	int STRANDS = 12;

	// Number of lights per strand
	int STRAND_LENGTH = 24;

	// The lowest frequency that will be displayed
	int minOctave = 44;

	// Number of parts to split each octave into (higher numbers display a smaller range overall)
	int bandsPerOctave = 1;














	// Here there be dragons! (Don't mess around down here unless you know a little Java)






	// on-screen light sim
	int SIZE = 500;
	int SPACING = 10;
	double dRad = (Math.PI*2)/STRANDS;
	int[][] lights = new int[STRANDS][STRAND_LENGTH];

	// controls
	int COLOR_PANEL_X = 170;
	int COLOR_PANEL_Y = 100;
	int WIDGET_1_X = SIZE;
	int WIDGET_1_Y = COLOR_PANEL_Y + 150;
	int WIDGET_2_X = SIZE + DisplayControlWidget.WIDTH;
	int WIDGET_2_Y = WIDGET_1_Y;
	PButton resetButton = new PButton(this, SIZE, 20, 40, 20, "[r]eset", 
	    new ActionDelegate(){ 
	      public void doAction(InputEvent event){ 
	    	  Equalizer[] eqs = getEqualizers();
	    	  for (int i = 0; i < eqs.length; i++) {
	    		  eqs[i].resetMaxLevels();
	    	  }
	      }
	    }, 'r');
	Slider bandSliderFull = new Slider(this, SIZE+60, 20, 256, STRANDS);
	Slider bandSliderHalf = new Slider(this, SIZE+60, 20, 256, STRANDS/2);
	Slider upRateSlider = new Slider(this, SIZE+60, 20, 100, 1);
	Slider downRateSlider = new Slider(this, SIZE+180, 20, 100, 1);

//	ACheckbox randomizeCheckbox = new ACheckbox(this, SIZE + COLOR_PANEL_X, COLOR_PANEL_Y + 125, 10, 10, "Randomi[z]e Colors", 'z');
//	Slider volatilitySlider = new Slider(this, SIZE + COLOR_PANEL_X + 130, COLOR_PANEL_Y + 130, 50, 1);
	
	PButton saveDisplay1Button = new PButton(this, WIDGET_1_X + 20, WIDGET_1_Y + DisplayControlWidget.HEIGHT + 20, 50, 20, "Save", 
			new ActionDelegate() {
		public void doAction(InputEvent event) {
			saveDisplay1();
		}
	});
	PButton loadDisplay1Button = new PButton(this, WIDGET_1_X + 120, WIDGET_1_Y + DisplayControlWidget.HEIGHT + 20, 50, 20, "Load", 
			new ActionDelegate() {
			public void doAction(InputEvent event) {
				loadDisplay1();
			}
		});
	PButton saveDisplay2Button = new PButton(this, WIDGET_2_X + 20, WIDGET_2_Y + DisplayControlWidget.HEIGHT + 20, 50, 20, "Save", 
			new ActionDelegate() {
		public void doAction(InputEvent event) {
			saveDisplay2();
		}
	});
	PButton loadDisplay2Button = new PButton(this, WIDGET_2_X + 120, WIDGET_2_Y + DisplayControlWidget.HEIGHT + 20, 50, 20, "Load", 
			new ActionDelegate() {
			public void doAction(InputEvent event) {
				loadDisplay2();
			}
		});
	ACheckbox fullModeCheckbox = new ACheckbox(this, SIZE, 60, 10, 10, "Mode: [f]ull/split", 'f');
	ACheckbox invertFullCheckbox = new ACheckbox(this, SIZE, 80, 10, 10, "[i]nvert Full/Side", 'i');
	ACheckbox invertTopCheckbox = new ACheckbox(this, SIZE, 100, 10, 10, "[I]nvert Top", 'I');
	ACheckbox symmetryCheckbox = new ACheckbox(this, SIZE, 120, 10, 10, "[s]ymmetry on/off", 's');
	ACheckbox radialSymmetryCheckbox = new ACheckbox(this, SIZE, 140, 10, 10, "[S]ymmetry Type", 'S');
	ACheckbox logPowerCheckbox = new ACheckbox(this, SIZE, 160, 10, 10, "[L]ogarithmic bands", 'L');


	// actual light controller (Triknet)
	Triknet triknet = null;
	String CONTROLLER_IP = "192.168.1.200";

	// visualization
//	EqualizerDisplay eqDisplay = null;
//	ColorEqDisplay colorEqDisplay1 = null;
//	ColorEqDisplay colorEqDisplay2 = null;
	LightDisplay topDisplay = null;
	LightDisplay sideDisplay = null;
//	RainDisplay rainDisplay = null;
//	OverlayDisplay rainEqDisplay = null;
	
	HashMap<String, LightDisplay> savedDisplays = 
			new HashMap<String, LightDisplay>(); 
	
	DisplayControlWidget<? extends LightDisplay> controlWidget1;
	DisplayControlWidget<? extends LightDisplay> controlWidget2;

	// Average amount (out of 255) each rgb value will move per frame, in random mode
	float randomizerVolatility = 2.5f;

	// audio processing (Minim)
	Minim minim;
	AudioInput input;
	AudioSource source;
	FFT fft;
	BeatDetect beat;
	int bufferSize = 1024;
	
	JFileChooser fileChooser;
	public boolean choosingFile = false;
	
	public void setup() {

	  // Triknet
	  if (CONNECT_TO_TRIKLITS) {
	    triknet = new Triknet(this, CONTROLLER_IP, STRANDS, STRAND_LENGTH);
	    triknet.TL_open();
	  }
	  
	  // Minim
	  minim = new Minim(this);
	  input = minim.getLineIn();
//	  input = minim.getLineIn(Minim.STEREO, 1024, 48000, 16);
	  source = input;
	  fft = new FFT(source.bufferSize(), source.sampleRate());
	  beat = new BeatDetect(source.bufferSize(), source.sampleRate());
	  beat.detectMode(BeatDetect.SOUND_ENERGY);
	  fft.logAverages(minOctave, bandsPerOctave*2);
	  println("Buffer size is " + source.bufferSize() + 
	      ", sample rate is " + source.sampleRate());
	  println(fft.avgSize() + " averages");
	  
	  try { 
		  UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); 
	  } catch (Exception e) { 
		  e.printStackTrace();  
	  }
	  fileChooser = new JFileChooser("/");

	  size(SIZE*2, SIZE);
	  setAllLights(0, 0, 0);

	  bandSliderFull.setShortcutKey('b');
	  bandSliderFull.setLabel("Equalizer [b]ands");
	  bandSliderHalf.setShortcutKey('b');
	  bandSliderHalf.setLabel("Equalizer [b]ands");
	  upRateSlider.setShortcutKey('u');
	  upRateSlider.setLabel("Tween [u]p Rate");
	  downRateSlider.setShortcutKey('d');
	  downRateSlider.setLabel("Tween [d]own Rate");
//	  volatilitySlider.setLabel("[v]olatility");
//	  volatilitySlider.setShortcutKey('v');
//	  volatilitySlider.setShowValues(true);
	  
	  registerAll(resetButton);
	//  registerAll(rotateButton);
	//  registerAll(bandSliderFull);
	  registerAll(upRateSlider);
	  registerAll(downRateSlider);
	  registerAll(fullModeCheckbox);
	  registerAll(invertFullCheckbox);
	  registerAll(invertTopCheckbox);
	  registerAll(symmetryCheckbox);
	  registerAll(radialSymmetryCheckbox);
	//  registerAll(logPowerCheckbox);
//	  registerAll(randomizeCheckbox);
//	  registerAll(volatilitySlider);
	  registerAll(controlWidget1);
	  registerAll(controlWidget2);
	  registerAll(saveDisplay1Button);
	  registerAll(loadDisplay1Button);
	  registerAll(saveDisplay2Button);
	  registerAll(loadDisplay2Button);
	  
	  symmetryCheckbox.setActionDelegate(
	    new ActionDelegate() { 
	      public void doAction(InputEvent event) {
	        if (symmetryCheckbox.isChecked()) {
//	          unregisterAll(bandSliderFull);
//	          registerAll(bandSliderHalf);
//	          eqDisplay.setBandSettings(bandSliderHalf.getPositions());
//	          colorEqDisplay1.setBandSettings(bandSliderHalf.getPositions());
//	          colorEqDisplay2.setBandSettings(bandSliderHalf.getPositions());
//	          fft.logAverages(minOctave, (int)(bandsPerOctave));
	        } else {
//	          unregisterAll(bandSliderHalf);
//	          registerAll(bandSliderFull);
//	          eqDisplay.setBandSettings(bandSliderFull.getPositions());
//	          colorEqDisplay1.setBandSettings(bandSliderFull.getPositions());
//	          colorEqDisplay2.setBandSettings(bandSliderFull.getPositions());
//	          fft.logAverages(minOctave, bandsPerOctave*2);
	        }
	      }
	    });
	  logPowerCheckbox.setActionDelegate(
	    new ActionDelegate() { 
	      public void doAction(InputEvent event) {
	        if (logPowerCheckbox.isChecked()) {
//	          eqDisplay.setGlobalLevelMultiplier(12);
//	          colorEqDisplay1.setGlobalLevelMultiplier(12);
//	          colorEqDisplay2.setGlobalLevelMultiplier(12);
	        } else {
//	          eqDisplay.setGlobalLevelMultiplier(10);
//	          colorEqDisplay1.setGlobalLevelMultiplier(10);
//	          colorEqDisplay2.setGlobalLevelMultiplier(10);
	        }
	      }
	    });
//	  volatilitySlider.setActionDelegate(
//	    new ActionDelegate() { 
//	      public void doAction(InputEvent event) {
//	        randomizerVolatility = volatilitySlider.getPositions()[0] / 10f;
//	      }
//	    });
	  
	  textFont(loadFont("ArialMT-12.vlw"), 12);
	  
//	  colorEqDisplay1 = new ColorEqDisplay(fft, bandSliderFull.getPositions(), bandSliderFull.getLength()*4, color(0, 50, 255), color(0, 255, 50));
//	  colorEqDisplay2 = new ColorEqDisplay(fft, bandSliderFull.getPositions(), bandSliderFull.getLength()*4, color(0, 255, 0), color(255, 0, 0));
//	  eqDisplay = new EqualizerDisplay(fft, bandSliderFull.getPositions(), bandSliderFull.getLength()*4);
//	  colorEqDisplay1.setInactiveBrightness(255);
//	  colorEqDisplay2.setInactiveBrightness(255);
//	  colorEqDisplay1.setMode(1);
//	  colorEqDisplay2.setMode(1);
//	  rainDisplay = new RainDisplay(color(0, 255, 255), 24, .5, .25);
//	  rainDisplay.setBeat(beat);
//	  ((DisplayControlWidget<RainDisplay>) controlWidget1).setDisplay(rainDisplay);
//	  rainEqDisplay = new OverlayDisplay(OverlayDisplay.OverlayMode.BRIGHTNESS);
//	  rainEqDisplay.setBase(rainDisplay);
//	  rainEqDisplay.setOverlay(colorEqDisplay1);
//	  eqDisplay.setInactiveBrightness(0);
//	//  eqDisplayHalf = new EqualizerDisplay(fft, bandSliderHalf.getPositions(), bandSliderHalf.getLength()*2);
//	  fullDisplay = rainDisplay;
//	  topDisplay = eqDisplay;
//	  sideDisplay = eqDisplay;
	  
	  try {
		  new ObjectOutputStream(new FileOutputStream("/colorEqDisplay")).writeObject(new ColorEqDisplay(fft, bandSliderFull.getPositions(), bandSliderFull.getLength()*4, color(0, 255, 0), color(255, 0, 0)));
		  new ObjectOutputStream(new FileOutputStream("/rainDisplay")).writeObject(new RainDisplay(color(0, 255, 255), 24, .5, .25));
		  new ObjectOutputStream(new FileOutputStream("/overlayDisplay")).writeObject(new OverlayDisplay(OverlayDisplay.OverlayMode.OPAQUE));
	  } catch (FileNotFoundException e) {
		  e.printStackTrace();
	  } catch (IOException e) {
		  e.printStackTrace();
	  }
	  
	  try {
		  topDisplay = (LightDisplay) new ObjectInputStream(new FileInputStream("topDisplay")).readObject();
	  } catch (FileNotFoundException e) {
		  e.printStackTrace();
	  } catch (IOException e) {
		  e.printStackTrace();
	  } catch (ClassNotFoundException e) {
		  e.printStackTrace();
	  }
	  if (topDisplay == null) {
		  topDisplay = new EqualizerDisplay(fft, bandSliderFull.getPositions(), bandSliderFull.getLength()*4);
	  }
	  configureDisplay(topDisplay);
	  configureWidget2(topDisplay);
	  try {
		  sideDisplay = (LightDisplay) new ObjectInputStream(new FileInputStream("sideDisplay")).readObject();
	  } catch (FileNotFoundException e) {
		  e.printStackTrace();
	  } catch (IOException e) {
		  e.printStackTrace();
	  } catch (ClassNotFoundException e) {
		  e.printStackTrace();
	  }
	  if (sideDisplay == null) {
		  sideDisplay = new EqualizerDisplay(fft, bandSliderFull.getPositions(), bandSliderFull.getLength()*4);
	  }
	  configureDisplay(sideDisplay);
	  configureWidget1(sideDisplay);
	  
	}

	public void draw() {
	  background(153);
	  
	  // Randomization
//	  if (randomizeCheckbox.isChecked()) {
////	    int newColor = randomColor(setColor1Button.getFillColor());
////	    setColor1Button.setFillColor(newColor);
////	    colorEqDisplay1.setTopColor(newColor);
////	    newColor = randomColor(setColor2Button.getFillColor());
////	    setColor2Button.setFillColor(newColor);
////	    colorEqDisplay1.setBottomColor(newColor);
////	    newColor = randomColor(setColor3Button.getFillColor());
////	    setColor3Button.setFillColor(newColor);
////	    colorEqDisplay2.setTopColor(newColor);
////	    newColor = randomColor(setColor4Button.getFillColor());
////	    setColor4Button.setFillColor(newColor);
////	    colorEqDisplay2.setBottomColor(newColor);
//	  }
	  fft.forward(source.mix);
	  beat.detect(source.mix);
	  if(fullModeCheckbox.isChecked()) {
	    if (sideDisplay != null)
	    	sideDisplay.update();
	  } else {
	    if (topDisplay != null)
	      topDisplay.update();
	    if (sideDisplay != topDisplay && sideDisplay != null)
	      sideDisplay.update();
	  }
	  for (int strandNum = 0; strandNum < STRANDS; strandNum++) {
	      double strand = (strandNum + .5) / (STRANDS * 1.0);
	      if (symmetryCheckbox.isChecked()) {
	        if (radialSymmetryCheckbox.isChecked()) {
	          if (strand >= .5) strand -= .5;
	        } else {
	          if (strand >= .5) strand = 1.0 - strand;
	        }
	        strand *= 2;
	      }
	    for (int lightNum = 0; lightNum < STRAND_LENGTH; lightNum++) {
	      double light = lightNum / (STRAND_LENGTH * 1.0);
	      if (fullModeCheckbox.isChecked()) {
	        if (invertFullCheckbox.isChecked()) light = 1 - light;
	        tween(strandNum, lightNum, sideDisplay.getColor(strand, light));
	      } else {
	        if (light < .5) {
	          light *= 2;
	          if (invertTopCheckbox.isChecked()) light = 1 - light;
	          tween(strandNum, lightNum, topDisplay.getColor(strand, light));
	        } else {
	          light = (light - .5) * 2;
	          if (invertFullCheckbox.isChecked()) light = 1 - light;
	          tween(strandNum, lightNum, sideDisplay.getColor(strand, light));
	        }
	      }
	    }
	  }  

	  drawLights();
	  if (triknet != null)
	    triknet.TL_out(lights, 10);
	  try {Thread.sleep(17);}catch(InterruptedException e){}
	  
	}

	void tween(int strandNum, int lightNum, int targetColor) {
	  int orig = lights[strandNum][lightNum];
	  float origRed = red(orig);
	  float origGreen = green(orig);
	  float origBlue = blue(orig);
	  float targetRed = red(targetColor);
	  float targetGreen = green(targetColor);
	  float targetBlue = blue(targetColor);
	  float scale = (origRed + origGreen + origBlue) > (targetRed + targetGreen + targetBlue)
	                ? (downRateSlider.getPositions()[0] / 100f)
	                : (upRateSlider.getPositions()[0] / 100f);
	  float dRed = (targetRed - origRed) * scale;
	  float dGreen = (targetGreen - origGreen) * scale;
	  float dBlue = (targetBlue - origBlue) * scale;
	  float newRed = (dRed > 0) ? ceil(origRed + dRed) : floor(origRed + dRed);
	  float newGreen = (dGreen > 0) ? ceil(origGreen + dGreen) : floor(origGreen + dGreen);
	  float newBlue = (dBlue > 0) ? ceil(origBlue + dBlue) : floor(origBlue + dBlue);
	  lights[strandNum][lightNum] = normalize(color(newRed, newGreen, newBlue));
	}

	public void stop() {
	  // Triknet
	  if (triknet != null) 
	    triknet.TL_close();
	  
	  // Minim
	  input.close();
	  minim.stop();
	  
	  try {
//		  new ObjectOutputStream(new FileOutputStream("fullDisplay")).writeObject(fullDisplay);
		  if (topDisplay != null) new ObjectOutputStream(new FileOutputStream("topDisplay")).writeObject(topDisplay);
		  if (sideDisplay != null) new ObjectOutputStream(new FileOutputStream("sideDisplay")).writeObject(sideDisplay);
	  } catch (FileNotFoundException e) {
		  e.printStackTrace();
	  } catch (IOException e) {
		  e.printStackTrace();
	  }
	  
	  super.stop();
	}

	public int normalize(int c) {
	  int result = c;
	  int r = (int) red(c);
	  int g = (int) green(c);
	  int b = (int) blue(c);
	  int brightness = r + g + b;
	  if (brightness > 512) {
	    double factor = 512.0 / brightness;
	    r = (int) (r * factor);
	    b = (int) (b * factor);
	    g = (int) (g * factor);
	    
	    result = color(r, g, b);
	  }
	  return result;
	}

	void drawLights() {
	  int centerX = SIZE/2;
	  int centerY = SIZE/2;
	  for (int strand = 0; strand < STRANDS; strand++) {
	    double theta = strand * dRad - (PI/2);
	    for (int lightNum = 0; lightNum < STRAND_LENGTH; lightNum++) {
	      int c = lights[strand][lightNum];
	      fill(c);
	      noStroke();
	      int y = (int) ((lightNum+1) * SPACING * Math.sin(theta));
	      int x = (int) ((lightNum+1) * SPACING * Math.cos(theta));
	      x = centerX - x;
	      y = centerY - y;
	      rect(x, y, 4, 4);
	    }
//	    fill(0);
//	    int y = (int) ((STRAND_LENGTH+1) * SPACING * Math.sin(theta));
//	    int x = (int) ((STRAND_LENGTH+1) * SPACING * Math.cos(theta));
//	    x = centerX - x;
//	    y = centerY - y + 5;
//	    text(strand, x, y);
	  }
	}

	private void loadDisplay1() {
		choosingFile = true;
		int returnVal = fileChooser.showOpenDialog(this); 
		choosingFile = false;
		if (returnVal == JFileChooser.APPROVE_OPTION) { 
			File file = fileChooser.getSelectedFile();
			if (file.isFile()) {
				ObjectInputStream is = null;
				try {
					is = new ObjectInputStream(new FileInputStream(file));
					LightDisplay newDisplay = (LightDisplay) is.readObject();
					configureDisplay(newDisplay);
					configureWidget1(newDisplay);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} finally {
					if (is != null) {
						try {
							is.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}

	private void configureWidget1(LightDisplay newDisplay) {
		DisplayControlWidget<? extends LightDisplay> newWidget = DisplayControlWidgetFactory.createWidget(newDisplay, this, WIDGET_1_X, WIDGET_1_Y);
		if (newWidget != null) {
			unregisterAll(controlWidget1);
			registerAll(newWidget);
			sideDisplay = newWidget.getDisplay();
			controlWidget1 = newWidget;
		}
	}
	
	private void loadDisplay2() {
		choosingFile = true;
		int returnVal = fileChooser.showOpenDialog(this); 
		choosingFile = false;
		if (returnVal == JFileChooser.APPROVE_OPTION) { 
			File file = fileChooser.getSelectedFile();
			if (file.isFile()) {
				ObjectInputStream is = null;
				try {
					is = new ObjectInputStream(new FileInputStream(file));
					LightDisplay newDisplay = (LightDisplay) is.readObject();
					configureDisplay(newDisplay);
					configureWidget2(newDisplay);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} finally {
					if (is != null) {
						try {
							is.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}

	private void configureWidget2(LightDisplay newDisplay) {
		DisplayControlWidget<? extends LightDisplay> newWidget = DisplayControlWidgetFactory.createWidget(newDisplay, this, WIDGET_2_X, WIDGET_2_Y);
		if (newWidget != null) {
			unregisterAll(controlWidget2);
			registerAll(newWidget);
			topDisplay = newWidget.getDisplay();
			controlWidget2 = newWidget;
		}
	}
	
	private void saveDisplay1() {
		if (controlWidget1 == null) return;
		choosingFile = true;
		int returnVal = fileChooser.showSaveDialog(this);
		choosingFile = false;
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			ObjectOutputStream os = null;
			try {
				File file = fileChooser.getSelectedFile();
				os = new ObjectOutputStream(new FileOutputStream(file));
				os.writeObject(controlWidget1.getDisplay());
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (os != null) {
					try {
						os.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	private void saveDisplay2() {
		if (controlWidget2 == null) return;
		choosingFile = true;
		int returnVal = fileChooser.showSaveDialog(this);
		choosingFile = false;
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			ObjectOutputStream os = null;
			try {
				File file = fileChooser.getSelectedFile();
				os = new ObjectOutputStream(new FileOutputStream(file));
				os.writeObject(controlWidget2.getDisplay());
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (os != null) {
					try {
						os.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	private void configureDisplay(LightDisplay display) {
		if (display instanceof BeatDetector) {
			((BeatDetector) display).setBeat(beat);
		}
		if (display instanceof Equalizer) {
			((Equalizer) display).setEq(fft);
		}
	}
	
	private Equalizer[] getEqualizers() {
		Equalizer[] result;
		boolean sideIsEq = (sideDisplay != null && sideDisplay instanceof Equalizer);
		boolean topIsEq = (topDisplay != null && topDisplay instanceof Equalizer);
		if (sideIsEq && topIsEq) {
			result = new Equalizer[]{(Equalizer) topDisplay, (Equalizer) sideDisplay};
		} else if (sideIsEq) {
			result = new Equalizer[]{(Equalizer) sideDisplay};
		} else if (topIsEq) {
			result = new Equalizer[]{(Equalizer) topDisplay};
		} else {
			result = new Equalizer[0];
		}
		return result;
	}

	void setLight(int strand, int lightNum, int c) {
	  lightNum = lightNum % STRAND_LENGTH;
	  strand = strand % STRANDS;
	  lights[strand][lightNum] = normalize(c);
	}

	void setAllLights(int red, int green, int blue) {
	  for (int i = 0; i < STRANDS; i++) {
	    for (int j = 0; j < STRAND_LENGTH; j++) {
	      setLight(i, j, color(red, green, blue));
	    }
	  }
	}

	void setRow(int lightNum, int c) {
	  for (int strand = 0; strand < STRANDS; strand++) {
	    setLight(strand, lightNum, c);
	  }
	}

	void setStrand(int strand, int c) {
	  for (int lightNum = 0; lightNum < STRAND_LENGTH; lightNum++) {
	    setLight(strand, lightNum, c);
	  }
	}

	void rotateStrandsCCW() {
	  int[] tmp = lights[0].clone();
	  for (int strand = 1; strand < STRANDS; strand++) {
	    for (int lightNum = 0; lightNum < STRAND_LENGTH; lightNum++) {
	      setLight(strand-1, lightNum, lights[strand][lightNum]);
	    }
	  }
	  for (int lightNum = 0; lightNum < STRAND_LENGTH; lightNum++) {
	    setLight(STRANDS-1, lightNum, tmp[lightNum]);
	  }
	} 

	void rotateStrandsCW() {
	  int[] tmp = lights[STRANDS-1].clone();
	  for (int strand = STRANDS-1; strand > 0; strand--) {
	    for (int lightNum = 0; lightNum < STRAND_LENGTH; lightNum++) {
	      setLight(strand, lightNum, lights[strand-1][lightNum]);
	    }
	  }
	  for (int lightNum = 0; lightNum < STRAND_LENGTH; lightNum++) {
	    setLight(0, lightNum, tmp[lightNum]);
	  }
	}

	void rotateRowsUp() {
	  for (int strand = 0; strand < STRANDS; strand++) {
	    int tmp = lights[strand][0];
	    for (int row = 0; row < STRAND_LENGTH-1; row++) {
	      setLight(strand, row, lights[strand][row+1]);
	    }
	    setLight(strand, STRAND_LENGTH-1, tmp);
	  }
	}

	void rotateRowsDown() {
	  for (int strand = 0; strand < STRANDS; strand++) {
	    int tmp = lights[strand][STRAND_LENGTH-1];
	    for (int row = STRAND_LENGTH-1; row > 0; row--) {
	      setLight(strand, row, lights[strand][row-1]);
	    }
	    setLight(strand, 0, tmp);
	  }
	}

	int randomColor(int oldColor) {
	  int newRed = (int) red(oldColor);
	  int newGreen = (int) green(oldColor);
	  int newBlue = (int) blue(oldColor);
	  float rand;
	  
	  if (randomizerVolatility > 1) {
	    rand = random(1);
	    if (rand < 1/3f) newRed += (int) randomizerVolatility;
	    else if (rand < 2/3f) newRed-= (int) randomizerVolatility;
	    
	    rand = random(1);
	    if (rand < 1/3f) newGreen+= (int) randomizerVolatility;
	    else if (rand < 2/3f) newGreen-= (int) randomizerVolatility;
	    
	    rand = random(1);
	    if (rand < 1/3f) newBlue+= (int) randomizerVolatility;
	    else if (rand < 2/3f) newBlue-= (int) randomizerVolatility;
	  } else {
	    rand = random(1);
	    if (rand < 1/3f) newRed += random(1) < randomizerVolatility ? 1 : 0;
	    else if (rand < 2/3f) newRed-= random(1) < randomizerVolatility ? 1 : 0;
	    
	    rand = random(1);
	    if (rand < 1/3f) newGreen+= random(1) < randomizerVolatility ? 1 : 0;
	    else if (rand < 2/3f) newGreen-= random(1) < randomizerVolatility ? 1 : 0;
	    
	    rand = random(1);
	    if (rand < 1/3f) newBlue+= random(1) < randomizerVolatility ? 1 : 0;
	    else if (rand < 2/3f) newBlue-= random(1) < randomizerVolatility ? 1 : 0;
	  }
	  return color(newRed, newGreen, newBlue);
	}

	void registerAll(Object o) {
		if (o != null) {
			registerDraw(o);
			registerMouseEvent(o);
			registerKeyEvent(o);
		}
	}

	void unregisterAll(Object o) {
		if (o != null) {
			unregisterDraw(o);
			unregisterMouseEvent(o);
			unregisterKeyEvent(o);
		}
	}
	
	protected void checkMouseEvent(MouseEvent e) {
		if (!choosingFile) {
			super.checkMouseEvent(e);
		}
	}
	
}
