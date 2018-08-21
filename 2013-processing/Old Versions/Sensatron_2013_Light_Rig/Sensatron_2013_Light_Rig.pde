
/* 
 Sensatron 2013 Light Rig
 James Hagerman
 
 Make sure you've loaded the correct FTDI driver:
 retina machine:
   cd /Users/jhagerman/dev/processing/other peopels stuff/p9813/processing
 original 13"
   cd /Volumes/Keket/Users/jamis/dev/Circuits_MPUs/FTDI\ Hacks/TCL\ Lights/p9813/processing
 for arduino: make load
 for bitbanging: make unload
 
 My wiring rig pinout:
   g = blue   = ground
   c = yellow = clock
   + = red/nc = positive 5 volts
   d = green  = data
  nc = not connected
  nc = not connected
 
 */

// TCL Library setup
import TotalControl.*;
// The TotalControl processing library doesn't define the FTDI pins so we do:
short TC_FTDI_TX  = 0x01;  /* Avail on all FTDI adapters,  strand 0 default */
short TC_FTDI_RX  = 0x02;  /* Avail on all FTDI adapters,  strand 1 default */
short TC_FTDI_RTS = 0x04;  /* Avail on FTDI-branded cable, strand 2 default */
short TC_FTDI_CTS = 0x08;  /* Avail on all FTDI adapters,  clock default    */
short TC_FTDI_DTR = 0x10;  /* Avail on third-party cables, strand 2 default */
short TC_FTDI_DSR = 0x20;  /* Avail on full breakout board */
short TC_FTDI_DCD = 0x40;  /* Avail on full breakout board */
short TC_FTDI_RI  = 0x80;  /* Avail on full breakout board */

// Camera setup:
import processing.video.*;
Capture cam;

// These variables are 
int STRANDS = 12; // Number of physical wands
int STRAND_LENGTH = 50; // Number of lights per strand
int LED_COUNT = STRAND_LENGTH; // Total number of lights

int strandCount = 6;
int pixelsOnStrand = 100;
int totalPixels = strandCount * pixelsOnStrand;

// Onscreen display:
int screenWidth = 1280;
int screenHeight = 600;

PGraphics lightDisplay;
double dRad = (Math.PI*2)/STRANDS;
int[][] lights = new int[STRANDS][STRAND_LENGTH];
int SPACING = 5;
PFont font;

// TCL Stuff:
TotalControl tc;
int[] tclArray = new int[totalPixels];
int[] remap = new int[strandCount * pixelsOnStrand];

// Status output dots
int statusDotRow = 0;


// Circle Animation setup:
// Declare two arrays with 50 elements.
int[] xpos = new int[100]; 
int[] ypos = new int[100];
PGraphics circleAnimation;

void setup() {
  size(screenWidth, screenHeight, P3D);
  frameRate(60);
  
  lightDisplay = createGraphics(500, 600, P3D);
  lightDisplay.smooth();
  lightDisplay.lights();
  
  // Circle Animation setup:
  circleAnimation = createGraphics(300,300,P3D);
  circleAnimation.smooth();
  // Initialize all elements of each array to zero.
  for (int i = 0; i < xpos.length; i ++ ) {
    xpos[i] = 0; 
    ypos[i] = 0;
  }
  
  font = createFont("Arial Bold", 10);
  
  // Override the default pin outs:
  // This is clock. We don't want to override it:
  //tc.setStrandPin(x,TC_FTDI_CTS);
  tc.setStrandPin(0,TC_FTDI_TX); // default
  tc.setStrandPin(1,TC_FTDI_RX); // default
  tc.setStrandPin(2,TC_FTDI_DTR); // spliting dtr and rts
  
  // Custom lines for the ftdi breakout
  tc.setStrandPin(3,TC_FTDI_RTS);
  tc.setStrandPin(4,TC_FTDI_RI);
  tc.setStrandPin(5,TC_FTDI_DSR);
  tc.setStrandPin(6,TC_FTDI_DCD);
  int status = tc.open(strandCount, pixelsOnStrand);
  if (status != 0) {
    tc.printError(status);
    exit();
  }

  buildRemapArray();
  
  try {
//    cam = new Capture(this, 400, 300, "Logitech Camera");
    cam = new Capture(this, 320, 180, "FaceTime HD Camera (Built-in)"); // retina machine
//    cam = new Capture(this, 320, 180, "Built-in iSight"); // original 13"
  } 
  catch (Exception e) {
    println("Something's wrong with the camera settings:");
    String[] cameras = Capture.list();
    if (cameras.length == 0) {
      println("There are no cameras available for capture.");
      exit();
    } else {
      println("Available cameras:");
      for (int i = 0; i < cameras.length; i++) {
        println(cameras[i]);
      }   
    }
  }

  cam.start(); 
}

void draw() {
  background(150);
  displayFramerate();
//  printStatusDots();

  updateRaw();
  drawCircleAnimation();
  
  updateLights();
  drawLights();
  
//  delay(100);
}

void drawCircleAnimation() {
  updateCircles();
  image(circleAnimation, 500, 100); 
}

void updateCircles() {
  circleAnimation.beginDraw();
  circleAnimation.colorMode(HSB, 255);
  circleAnimation.background(mouseY, 255, 255);
  
  // Shift array values
  for (int i = 0; i < xpos.length-1; i ++ ) {
    // Shift all elements down one spot. 
    // xpos[0] = xpos[1], xpos[1] = xpos = [2], and so on. Stop at the second to last element.
    xpos[i] = xpos[i+1];
    ypos[i] = ypos[i+1];
  }
//  delay(mouseY);
  
  // New location
  xpos[xpos.length-1] = mouseX; // Update the last spot in the array with the mouse location.
  ypos[ypos.length-1] = 0;
  
  // Draw everything
  circleAnimation.pushMatrix();
//  rotateX(radians(-45));
//  rotateY(radians(45));
  circleAnimation.translate(circleAnimation.width/2,circleAnimation.height/2); // for P3D renderer
//  translate(width/2,height/2);
  
//  for (int i = 0; i < xpos.length; i ++ ) {
//     // Draw an ellipse for each element in the arrays. 
//     // Color and size are tied to the loop's counter: i.
//    
////    rotateZ(radians(i*90)); // for P3D renderer
//    rotate(radians(30));
//    noStroke();
////    translate(0,0,-10);
//    fill(255-i*2, 0+i*2.5, 255);
//    ellipse(xpos[i],ypos[i],i,i);
//  }
  for (int i = xpos.length -1; i > 0 ; i-- ) {
     // Draw an ellipse for each element in the arrays. 
     // Color and size are tied to the loop's counter: i.
    
//    rotateZ(radians(i*90)); // for P3D renderer
    circleAnimation.rotate(radians(30));
    circleAnimation.noStroke();
//    translate(0,0,-10);
    
    circleAnimation.fill(255-i*2, 255, 255, mouseY); // HSB colors
    
//    fill(255-i*2, 0+i*2.5, 255-i*2, mouseY); // rgb colors
    circleAnimation.ellipse(xpos[i],ypos[i],i,i);
  }
  circleAnimation.popMatrix();
  circleAnimation.endDraw();
}

void updateRaw() {
  if (cam.available()) {
    cam.read();
    cam.loadPixels();
  }
  image(cam, width-cam.width, 100); 
  color c = cam.pixels[1*cam.width+1]; //pixels[y*cam.width+x]
//  color c = cam.get(60,90);
  noStroke();
  fill(c);
  rect(width-30, height-30, 20, 20);  
}

void updateLights() {
//  color thisRandomColor = getRandomColor();
//  for (int i = 0; i < totalPixels; i++) {
//    tclArray[i]  = getRandomColor();
//  }

//  randomizeAllLights();
//  setAllLights(color(255, 0, 0));
//  setOneLight(0, 5, color(255));
//  cycleOneColor();
  
  useRawColors(circleAnimation);
}

void useRawColors(PImage toLoad) {
  toLoad.loadPixels();
  int centerX = toLoad.width/2;
  int centerY = toLoad.height/2;
  for (int strand = 0; strand < STRANDS; strand++) {
    double theta = strand * dRad - (PI/2) + PI;
    for (int lightNum = 0; lightNum < STRAND_LENGTH; lightNum++) {
      int y = (int) ((lightNum+3) * SPACING * Math.sin(theta));
      int x = (int) ((lightNum+3) * SPACING * Math.cos(theta));
      
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

void cycleOneColor() {
  for (int strand = 0; strand < STRANDS; strand++) {
    for (int lightNum = 0; lightNum < STRAND_LENGTH; lightNum++) {
//      lights[strand][lightNum] = getRandomColor();
      setAllLights(color(255, 0, 0));
      setOneLight(strand, lightNum, color(255));
    }
  }
}

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

void mapDrawingToLights() {
  int lightIndex = 0;
  for (int strand = 0; strand < STRANDS; strand++) {
    for (int lightNum = 0; lightNum < STRAND_LENGTH; lightNum++) {
      tclArray[lightIndex] = lights[strand][lightNum];
      lightIndex++;
    }
  }
}

color getRandomColor() {
  return color((int)random(255), (int)random(255), (int)random(255));
}

void sendLights() {
  tc.refresh(tclArray, remap);
}

void buildRemapArray() {
  println("Building remap array");
  for (int i = 0; i < STRANDS * STRAND_LENGTH; i++) {
    remap[i] = i;
  }
  println("Done building remap array");
}

void drawLights() {
  int centerX = lightDisplay.width/2;
  int centerY = lightDisplay.height/2;
  
  lightDisplay.beginDraw();
  lightDisplay.background(100);
  lightDisplay.pushMatrix();
  //  rotateZ(radians(180));
  lightDisplay.translate(0, 0, -100);
  lightDisplay.rotateX(radians(45));

  for (int strand = 0; strand < STRANDS; strand++) {
    double theta = strand * dRad - (PI/2) + PI;
    for (int lightNum = 0; lightNum < STRAND_LENGTH; lightNum++) {
      int c = lights[strand][lightNum];
//      c = 255;
      lightDisplay.fill(c);
//      noStroke();
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
  
  // Move this information to the physical lights:
  mapDrawingToLights();
  sendLights();
}

void colorTest() {
  // This isn't used. It's just here for reference...
  println("Color test");

//  int rBlue = (int)random(255);
  int rRed = 0xf1;
  int rBlue = 0xf2;
  int rGreen = 0xf3;

  int randomColor = rRed;
  randomColor = rRed << 16 | rBlue << 8 | rGreen;
  println("Custom color hex: " + hex(randomColor, 8));

  color testColor = color(rRed, rBlue, rGreen, 5);
  println("Test color hex: " + hex(randomColor, 8));
}

void printStatusDots() {
  if (statusDotRow <= 50) {
    print(".");
    statusDotRow++;
  } 
  else {
    println(".");
    statusDotRow = 0;
  }
}

void displayFramerate() {
  textFont(font, 10);
  fill(255);
  text("FPS: " + int(frameRate), 720, 30);
}

void exit() {
  tc.close();
}

