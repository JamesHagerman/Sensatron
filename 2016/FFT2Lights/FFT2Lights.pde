// Audio imports:
import ddf.minim.*;
import ddf.minim.analysis.*;
// Audio stuff:
Minim minim;
AudioInput in;
FFT fft;
FFT fftLog;
int bufferSize = 2048;
float centerFrequency = 0;
float height2;
float spectrumScale = 2;

// Already in APP:
// Car Lighting layout:
int STRANDS = 12; // Number of physical wands
int STRAND_LENGTH = 50; // Number of lights per strand
int LED_COUNT = STRAND_LENGTH; // Total number of lights
int strandCount = STRANDS/2; // two wands per "strand" of output
int pixelsOnStrand = STRAND_LENGTH*2; // twice as many pixels per "strand"
int totalPixels = strandCount * pixelsOnStrand;
int[] p = new int[totalPixels];
int[] remap = new int[totalPixels];
/* Could also use PImage and loadPixels()/updatePixels()...many options! */
int[][] lights = new int[STRANDS][STRAND_LENGTH];

// DO NOT WANT IN APP:
// A place to display the lights:
PGraphics lightDisplay;
int SPACING = 5;
double dRad = (Math.PI*2)/STRANDS;

// ANIMATION REQUIRED:
int globalTime = 0;
int ledCount = 0;
float goodFFTBuckets[] = new float[STRAND_LENGTH];
float goodFFTLog[] = new float[STRAND_LENGTH];

void setup() {
  // Just for display:
  size(1024, 600, P3D); // make it a little smaller...
  frameRate(60);
  height2 = height/2;
  rectMode(CORNERS);
  // This will build the Remapping array that will
  // wrap the LED strands back on themselves for
  // the higher density the Sensatron loves so much:
  buildRemapArray();
  // Build the visualizer:
  lightDisplay = createGraphics(800, 600, P3D);
  lightDisplay.smooth();
  lightDisplay.lights();

  // Audio stuff:
  minim = new Minim(this);
  in = minim.getLineIn(Minim.STEREO, bufferSize);
  in.enableMonitoring();
  fft = new FFT( in.bufferSize(), in.sampleRate() );
  fftLog = new FFT( in.bufferSize(), in.sampleRate() );
  fftLog.logAverages( 10, 8 );

  println("fftLog.avgSize(): " + fftLog.avgSize());
}

void draw() {
  background(0);
  stroke(255);

  globalTime++;

  // Do whatever with the audio to get it into a format we can use:
  processAudio();

  // DISPLAY:
  // setAllLights(getRandomColor());

  // Convert the Audio into meaningful ART
  doArt();

  // Actually draw the colors to the screen:
  // mapDrawingToLights(); // p[] -> lights[][]
  // mapLightsToDrawing(); // lights[][] -> p[]
  drawLights(); // draws lights[][]
  // drawLEDs(); // draws p[] to the LEDs.
}


void processAudio() {
  // AUDIO DRAW:
  fft.forward( in.mix );
  fftLog.forward( in.mix );

  ledCount = 0; // Reset ledCount
  for(int i = 0; i < fft.specSize(); i++)
  {
    if ( mouseX == i) {
      centerFrequency = fft.indexToFreq(i);
      stroke(255, 0, 0);
    } else {
        stroke(255);
    }

    // 102 frequiency bands across, basically the range of the piano (up to C7)
    // line(i*10, height2, i*10, height2 - fft.getBand(i)*spectrumScale);
    // line(i, height2, i, height2 - fft.getBand(i)*spectrumScale);

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
    centerFrequency    = fftLog.getAverageCenterFrequency(i);
    // how wide is this average in Hz?
    float averageWidth = fftLog.getAverageBandWidth(i);

    // we calculate the lowest and highest frequencies
    // contained in this average using the center frequency
    // and bandwidth of this average.
    float lowFreq  = centerFrequency - averageWidth/2;
    float highFreq = centerFrequency + averageWidth/2;

    // freqToIndex converts a frequency in Hz to a spectrum band index
    // that can be passed to getBand. in this case, we simply use the
    // index as coordinates for the rectangle we draw to represent
    // the average.
    int xl = (int)fftLog.freqToIndex(lowFreq);
    int xr = (int)fftLog.freqToIndex(highFreq);

    // if the mouse is inside of this average's rectangle
    // print the center frequency and set the fill color to red
    if ( mouseX >= xl && mouseX < xr ) {
      fill(255, 0, 0);
    } else {
      fill(255);
    }
    // draw a rectangle for each average, multiply the value by spectrumScale so we can see it better
    rect( xl, height, xr, height - fftLog.getAvg(i)*spectrumScale );

    // Save the values into the goodFFTBuckets:
    if (ledCount<STRAND_LENGTH) {
      goodFFTLog[ledCount] = fftLog.getAvg(i);
    }
    ledCount++; // increment for the next pass
  }
}


void doArt() {

  int paramValue = 12; //((mouseY*255)/height); // convert to 0-255;
  int signedValue = paramValue-(255/2);

  if (paramValue == 0) paramValue = 1;

  // println("derp: " + paramValue);
  // int shiftedTime = (globalTime/paramValue)%STRANDS;

  for(int i = 0; i < STRAND_LENGTH; i++) {
    int roundedVal = Math.round(goodFFTBuckets[i])*10;
    // int roundedVal = Math.round(goodFFTLog[i])*10;

    colorMode(HSB, 255);
    color theColor = color(roundedVal, 255, 255);
    // color theColor = color(globalTime%255, 255, 255);
    // color theColor = color(roundedVal, roundedVal, roundedVal);
    // setOneRing(i, theColor);
    setOneSpiral(0, i, 1, theColor);
    colorMode(RGB, 255);

  }
}



// Color stuff:
color getRandomColor() {
  return color((int)random(255), (int)random(255), (int)random(255));
}
// int color(int red, int green, int blue) {
//   return red << 16 | green << 8 | blue;
// }
// int random(int max) {
//   return (int) (Math.random() * (max + 1));
// }
// LIGHTS STUFF:
void setOneLight(int strand, int lightNum, color c) {
  // println("Strand: " + strand + " lightnum: " + lightNum);
  // if (strand < 0) {
  //   strand += STRANDS;
  // }
  // if (strand >= STRANDS) {
  //   strand = strand%STRANDS;
  // }
  strand = strand%STRANDS;
  // if (lightNum < 0) {
  //   lightNum += STRAND_LENGTH;
  // }
  // if (lightNum >= STRAND_LENGTH) {
  //   lightNum = lightNum%STRAND_LENGTH;
  // }
  lightNum = lightNum%STRAND_LENGTH;

  // println("Fixed Strand: " + strand + " lightnum: " + lightNum);
  lights[strand][lightNum] = c;
}
void setOneRing(int lightNum, color c) {
  for (int i = 0; i < STRANDS; i++) {
    setOneLight(i, lightNum, c);
  }
}
void setOneSpiral(int strand, int lightNum, int direction, color c) {
  for (int i = 0; i < STRANDS; i++) {
    int lightOffset = lightNum + (i * direction);
    setOneLight(strand + i, lightOffset, c);
  }
}
// Setting all lights to some color:
void setAllLights(color c) {
  for (int strand = 0; strand < STRANDS; strand++) {
    for (int lightNum = 0; lightNum < STRAND_LENGTH; lightNum++) {
      lights[strand][lightNum] = c;
    }
  }
}
public void buildRemapArray() {
  println("Building remap array...");
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
    // println("Setting wand: " + i);
    for(int j=0;j<STRAND_LENGTH;j++) {
      if(j%2==0) { // even led's (0,2,4,6...)
        remap[j-(j/2) + (STRAND_LENGTH * i)] = index;
      } else { // odd led's (1,3,5,7...)
         remap[(STRAND_LENGTH * (i+1)) - (j-(j/2))] = index;
      }
      index += 1;
    }
  }
  println("Done building remap array.");
}
// Move colors from lights[][] to p[] structure
void mapDrawingToLights() {
  // Lights on each strand to one big array
  int lightIndex = 0;
  for (int strand = 0; strand < STRANDS; strand++) {
    for (int lightNum = 0; lightNum < STRAND_LENGTH; lightNum++) {
      p[lightIndex] = lights[strand][lightNum];
      lightIndex++;
    }
  }
}
// Move colors from p[] to lights[][] structure
void mapLightsToDrawing() {
  // One big array to lights on each strand
  int lightIndex = 0;
  for (int strand = 0; strand < STRANDS; strand++) {
    for (int lightNum = 0; lightNum < STRAND_LENGTH; lightNum++) {
      lights[strand][lightNum] = p[lightIndex];
      lightIndex++;
    }
  }
}
// Draw the lights[][] structure to the screen
void drawLights() {
  int centerX = lightDisplay.width/2;
  int centerY = lightDisplay.height/2;
  lightDisplay.beginDraw();
  // lightDisplay.background(100);
  lightDisplay.pushMatrix();
  //  rotateZ(radians(180));
  lightDisplay.translate(0, 0, -100);
  // lightDisplay.rotateX(radians(45));
  for (int strand = 0; strand < STRANDS; strand++) {
    double theta = strand * dRad - (PI/2) + PI;
    for (int lightNum = 0; lightNum < STRAND_LENGTH; lightNum++) {
      int c = lights[strand][lightNum];
      lightDisplay.fill(c);
      noStroke();
      int y = (int) ((lightNum+3) * SPACING * Math.sin(theta));
      int x = (int) ((lightNum+3) * SPACING * Math.cos(theta));
      x = centerX - x;
      y = centerY - y;
      lightDisplay.ellipse(x, y, 5, 5);
    }
    // Draw the wand labels
    lightDisplay.fill(255);
    int y = (int) ((STRAND_LENGTH+3) * SPACING * Math.sin(theta));
    int x = (int) ((STRAND_LENGTH+3) * SPACING * Math.cos(theta));
    x = centerX - x;
    y = centerY - y;
    lightDisplay.text(strand, x, y, 10);
  }
  lightDisplay.popMatrix();
  lightDisplay.endDraw();
  image(lightDisplay, 0, 0);
}
