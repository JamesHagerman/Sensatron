// Car Lighting layout:
int STRANDS = 12; // Number of physical wands
int STRAND_LENGTH = 50; // Number of lights per strand
int LED_COUNT = STRANDS * STRAND_LENGTH; // Total number of lights

// TCL Variables:
int strandCount = STRANDS/2; // two wands per "strand" of output
int pixelsOnStrand = STRAND_LENGTH*2; // twice as many pixels per "strand"
int totalPixels = LED_COUNT;

int[] p = new int[totalPixels];
int[] remap = new int[totalPixels];
/* Could also use PImage and loadPixels()/updatePixels()...many options! */

ColorServer colorServer = new ColorServer();

// A place to display the lights:
PGraphics lightDisplay;
double dRad = (Math.PI*2)/STRANDS;
int[][] lights = new int[STRANDS][STRAND_LENGTH];
int SPACING = 5;

void setup()
{
  size(800, 600, P3D); // make it a little smaller...
  frameRate(60);

  // Start the Raw Color Server:
  colorServer.init(this);

  // This will build the Remapping array that will
  // wrap the LED strands back on themselves for
  // the higher density the Sensatron loves so much:
  buildRemapArray();

  // Build the visualizer:
  lightDisplay = createGraphics(800, 600, P3D);
  lightDisplay.smooth();
  lightDisplay.lights();
}

void draw()
{
  background(150);
  // Get color data from a connected client:
  colorServer.getColors(p);
  mapLightsToDrawing();
  drawLights();
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
    println("Setting wand: " + i);
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

// Method to move colors from the lights[][] multi-dimensional array
// and to the lights array: p[]
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

// A method for actually drawing the lights:
void drawLights() {
  int centerX = lightDisplay.width/2;
  int centerY = lightDisplay.height/2;

  lightDisplay.beginDraw();
  lightDisplay.background(100);
  lightDisplay.pushMatrix();
  //  rotateZ(radians(180));
  lightDisplay.translate(0, 0, -100);
  // lightDisplay.rotateX(radians(45));

  for (int strand = 0; strand < STRANDS; strand++) {
    double theta = strand * dRad - (PI/2) + PI;
    for (int lightNum = 0; lightNum < STRAND_LENGTH; lightNum++) {
      int c = lights[strand][lightNum];
//      c = 255;
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
