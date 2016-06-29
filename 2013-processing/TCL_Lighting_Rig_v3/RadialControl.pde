/*
  RadialControl.pde
  by James Hagerman
  on December 9, 2013

  This class is a combination of a few different classes. Because all of the original classes were written to handle TCL
  lights in a radial "spoke" pattern for the Sensatron art car, they were all pulled into a single class.

  Really, it ends up being a proto-class for being able to implement different physically mapping patterns in an way that
  disconnects the animations away from the physical distrobution of the lights.

  The class handles the following features:
  - internal tracking of color arrays (Not really... but it could probably do so in the future... with lights[][])

  - mapping a radial, multidimensional light array to the linear light array that the TCL library expects. (using the 
    mapRadialArrayToLights() method)

  - pulling color data from a provided PImage (using the stripRawColors(PImage) method)

  - 3D display of the colors in the radial pattern. This is best used to display what a given animation will actually look 
  like in real life

  In the future, this may be disected and be turned into a real "MappingControl" inheritable class...
 */

// Physical configuration settings:
int STRANDS = 12; // Number of physical wands
int STRAND_LENGTH = 50; // Number of lights per wand
int LED_COUNT = STRANDS * STRAND_LENGTH; // Total number of lights

double dRad;
int[][] lights;

class RadialControl {
  PGraphics lightDisplay;
  int grabSpacing = 5; // This spacing is for PULLING data from images not putting lights on the screen
  int displaySpacing = 5;
  int[] radialMap;
  
  RadialControl() {
    dRad = (Math.PI*2)/STRANDS;
    lights = new int[STRANDS][STRAND_LENGTH];

    lightDisplay = createGraphics(width, height, P3D);
    lightDisplay.smooth();
    lightDisplay.lights();

    // Build the radial remapping array we'll need for the spoke pattern:
    buildRemapArray();
  }

  void buildRemapArray() {
    println("Building RadialControl remap array...");
    radialMap = new int[LED_COUNT];

    // No map:
    // for (int i = 0; i < LED_COUNT; i++) {
    //   radialMap[i] = i;
    // }

    // Only the colors of the first light:
    // for (int i = 0; i < LED_COUNT; i++) {
    //   radialMap[i] = 0;
    // }

    // Working radial remap:
    int index = 0;
    for(int i=0; i<STRANDS; i++) {
      // println("Setting wand: " + i);
      for(int j=0;j<STRAND_LENGTH;j++) {
        if(j%2==0) { // even led's (0,2,4,6...)
          radialMap[j-(j/2) + (STRAND_LENGTH * i)] = index;
          // if (i == 1) {
          //   println("index " + index + " is: " + radialMap[index]);
          // }
        } else { // odd led's (1,3,5,7...)
          radialMap[(STRAND_LENGTH * (i+1)) - (j-(j/2))] = index;
          // if (i == 1) {
          //   println("index " + index + " is: " + radialMap[index]);
          // }
        }
        index += 1;
      }
    }
    
    println("Done building RadialControl remap array.");
  }

  // This method strips raw color data from a given PImage and plops it DIRECTLY into the radial lights array:
  void stripRawColors(PImage toLoad) {
    toLoad.loadPixels();
    int centerX = toLoad.width/2;
    int centerY = toLoad.height/2;
    for (int strand = 0; strand < STRANDS; strand++) {
      double theta = strand * dRad - (PI/2) + PI;
      for (int lightNum = 0; lightNum < STRAND_LENGTH; lightNum++) {
        int y = (int) ((lightNum+3) * grabSpacing * Math.sin(theta));
        int x = (int) ((lightNum+3) * grabSpacing * Math.cos(theta));
        
        x = (int)map(x, 0, 600, 0, toLoad.width);
        y = (int)map(y, 0, 600, 0, toLoad.height);
        x = centerX - x;
        y = centerY - y;
        fill(toLoad.pixels[y*toLoad.width+x]);
        ellipse(500+x, 0+toLoad.height+y, 5, 5); // 
        lights[strand][lightNum] = toLoad.pixels[y*toLoad.width+x];
      }
    }
  }

  // This method draws the raw data to the screen:
  // void drawRawData() {
  //   if (cam.available()) {
  //     cam.read();
  //     cam.loadPixels();
  //   }
  //   image(cam, width-cam.width, 100); 
  //   color c = cam.pixels[1*cam.width+1]; //pixels[y*cam.width+x]
  //   // color c = cam.get(60,90);
  //   noStroke();
  //   fill(c);
  //   rect(width-30, height-30, 20, 20);  
  // }
  
  int[] mapArrayToLights() {
    int[] toRet = new int[totalPixels];
    int lightIndex = 0;
    for (int strand = 0; strand < STRANDS; strand++) {
      for (int lightNum = 0; lightNum < STRAND_LENGTH; lightNum++) {
        toRet[lightIndex] = lights[strand][lightNum];
        lightIndex++;
      }
    }
    return toRet;
  }

  void drawLights() {
    int centerX = lightDisplay.width/2;
    int centerY = lightDisplay.height/2;
    
    lightDisplay.beginDraw();
    lightDisplay.background(100);
    lightDisplay.pushMatrix();
    // rotateZ(radians(180));
    lightDisplay.translate(0, 0, -100);
    lightDisplay.rotateX(radians(45));

    for (int strand = 0; strand < STRANDS; strand++) {
      double theta = strand * dRad - (PI/2) + PI;
      for (int lightNum = 0; lightNum < STRAND_LENGTH; lightNum++) {
        int c = lights[strand][lightNum];
        // c = 255;
        lightDisplay.fill(c);
        // noStroke();
        int y = (int) ((lightNum+3) * displaySpacing * Math.sin(theta));
        int x = (int) ((lightNum+3) * displaySpacing * Math.cos(theta));
        x = centerX - x;
        y = centerY - y;
        lightDisplay.ellipse(x, y, 5, 5);
      }
      // Draw the wand labels
      lightDisplay.fill(255);
      int y = (int) ((STRAND_LENGTH+3) * displaySpacing * Math.sin(theta));
      int x = (int) ((STRAND_LENGTH+3) * displaySpacing * Math.cos(theta));
      x = centerX - x;
      y = centerY - y;
      lightDisplay.text(strand, x, y, 10);
    }

    lightDisplay.popMatrix();
    lightDisplay.endDraw();
    image(lightDisplay, 0, 0);
  }
  
  
  
  // These are really more like animations but maybe we can use them as a tool to help us write animations later:
  void randomizeAllLights() {
    for (int strand = 0; strand < STRANDS; strand++) {
      for (int lightNum = 0; lightNum < STRAND_LENGTH; lightNum++) {
        lights[strand][lightNum] = getRandomColor();
      }
    }
  }
  
  void setAllLights(color c) {
    for (int strand = 0; strand < STRANDS; strand++) {
      for (int lightNum = 0; lightNum < STRAND_LENGTH; lightNum++) {
        lights[strand][lightNum] = c;
      }
    }
  }
  
  void setOneLight(int strand, int lightNum, color c) {
    lights[strand][lightNum] = c;
  }
  
  color getRandomColor() {
    return color((int)random(255), (int)random(255), (int)random(255));
  }
}
