import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import processing.video.*; 
import processing.serial.*; 
import TotalControl.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class Sensatron_2013_Lighting_Rig_v2 extends PApplet {

/*
  Sensatron 2013 Lighting Rig - v2.0
  by James Hagerman
  on August 18,2013 at 1:13 am
 */
 
// Physical configuration settings:
int STRANDS = 12; // Number of physical wands
int STRAND_LENGTH = 50; // Number of lights per strand
int LED_COUNT = STRAND_LENGTH; // Total number of lights

// We kinda need to use multiple definitions of the same data... for now...
int strandCount = 6;
int pixelsOnStrand = 100;
int totalPixels = strandCount * pixelsOnStrand;

// Animation settings:
ArrayList<SensatronRoutine> allAnimations; // Place to hold all known animations
ACircle aCircle; // A single circle controlled by the mouse
CircleAnimation originalCircles;
Spin spin;
MultiSpin multiSpin;

// Lighting class instances:
TCLControl tclControl;
RadialControl radialControl;
RawConversion rawConversion;

// Onscreen lighting display:
LightDisplay lightDisplay;

// Gyro input:
GyroInput gyroInput;

// Camera input class:
CameraInput cameraInput;



// Fake mouse movement variables:
int fakeMouseX;
int fakeMouseY;
int direction = 1;
int direction2 = 1;

// Pattern control:
int patternIndex = 3; // Start running on pattern 0
int patternIndexMax = 3;
boolean changePattern; // We need a way for classes to tell us it's time to change patterns.
int patternChangeTimer = 0;
int patternChangeTimeout = 1000;

public void setup() {
  size(500,500, P3D);
  frameRate(60);
  
  // Do hardware init first:
  tclControl = new TCLControl();
  radialControl = new RadialControl();
  rawConversion = new RawConversion();
  lightDisplay = new LightDisplay();
  gyroInput = new GyroInput(this);
  
  // Set up the webcam:
  // cameraInput = new CameraInput(this);
  
  changePattern = false;
  
  aCircle = new ACircle(100);
  originalCircles = new CircleAnimation();
  spin = new Spin(10);
  multiSpin = new MultiSpin(10);
  
  fakeMouseX = 0;
  fakeMouseY = 0;
  
}

public void draw() {

   // Keep the gyro data up to date and connected:
   gyroInput.draw();
   
   if (!gyroOkay) {
     patternChangeTimer += 1;
     if (patternChangeTimer > patternChangeTimeout) {
       println("Automatically changing patterns");
       patternChangeTimer = 0;
       changePattern = true;
     }
   }
  
  // If changePattern is true, one of the classes is asking us to change the animation pattern
  if (changePattern) {
    println("Changing patterns!");
    patternIndex += 1;
    if (patternIndex > patternIndexMax) {
      println("Starting from the first pattern.");
      patternIndex = 0;
    }
     if (patternIndex == 0) {
       patternIndex = 1; // This is skipping the first bullshit pattern.
     }
     if (patternIndex == 2) {
       patternIndex = 3; // This is skipping the second bullshit pattern.
     }
    changePattern = false;
  }
  
  
  
  // These draws the actual animation to the screen:
  if (patternIndex == 0) {
    if (gyroOkay) {
      aCircle.draw(gyroInput.rawX, gyroInput.rawY);
    } else {
      
     fakeMouseX += 10;
     if (fakeMouseX >= width) {
       fakeMouseX = 0;
     }
     fakeMouseY += (1 * direction);
     if (fakeMouseY >= 360 || fakeMouseY <= 0) {
       direction = direction * -1;
       fakeMouseY += (1 * direction);
     }
     
      aCircle.draw(fakeMouseX, fakeMouseY);
    }
    aCircle.updateScreen();
    rawConversion.stripRawColors(aCircle.pg); // Move the animation data directly to the lights
    
    
  } if (patternIndex == 1) {
     // Update the the fake mouse movement
     
     fakeMouseX += (5 * direction2);
     if (fakeMouseX >= width || fakeMouseX <= 0) {
       direction2 = direction2 * -1;
       fakeMouseX += (1 * direction2);
     }
     fakeMouseY += (1 * direction);
     if (fakeMouseY >= 360 || fakeMouseY <= 0) {
       direction = direction * -1;
       fakeMouseY += (1 * direction);
     }
     
    if (gyroOkay) {
      originalCircles.draw(gyroInput.rawX, gyroInput.rawY);
    } else {
      originalCircles.draw(fakeMouseX, fakeMouseY);
//      originalCircles.draw(mouseX, mouseY);
    }
    originalCircles.updateScreen();
    rawConversion.stripRawColors(originalCircles.pg); // Move the animation data directly to the lights
    
    
    
  } if (patternIndex == 2) {
    if (gyroOkay) {
      // Update the the fake mouse movement
       fakeMouseY += 1 + PApplet.parseInt(map(gyroInput.rawY, 0, width, 0, 5));
       if (fakeMouseY >= 180) {
         fakeMouseY = 0;
       }
     
      spin.draw(fakeMouseY, fakeMouseY);
    } else {
      
      // Update the the fake mouse movement
     fakeMouseX += 10;
     if (fakeMouseX >= width) {
       fakeMouseX = 0;
     }
     fakeMouseY += 10;
     if (fakeMouseY >= 180) {
       fakeMouseY = -0;
     }
     
      spin.draw(fakeMouseY, fakeMouseX);
    }
    
    spin.updateScreen();
    rawConversion.stripRawColors(spin.pg);
    
    
  } if (patternIndex == 3) {
    if (gyroOkay) {
      // Update the the fake mouse movement
       fakeMouseY += 1+ PApplet.parseInt(map(gyroInput.rawY, 0, width, 0, 5));
       if (fakeMouseY >= 180) {
         fakeMouseY = 0;
       }
     
      multiSpin.draw(fakeMouseY, fakeMouseY);
    } else {
      
      // Update the the fake mouse movement
//     fakeMouseX += 10;
//     if (fakeMouseX >= width) {
//       fakeMouseX = 0;
//     }
     fakeMouseY += 1;
     if (fakeMouseY >= 180) {
       fakeMouseY = -0;
     }
     
      multiSpin.draw(fakeMouseY, mouseY);
    }
    
    multiSpin.updateScreen();
    rawConversion.stripRawColors(multiSpin.pg);
  }


  // We need to automatically change or set the animation we're running if there is no gyro attached:
//  if (!gyroOkay) {
//    patternIndex = 1; // Hard code the damn thing to use the COOL animation if we don't have the gyro attached.
//  }
  


  // This draws the camera data to the screen...:
  // cameraInput.drawCameraData();
  // rawConversion.stripRawColors(cam); // and then directly to the lights: 
  
  
//  lightDisplay.drawLights(); // Draw 3D Lighting display

  // Shift radial light array to hardware:
  tclControl.tclArray = radialControl.mapRadialArrayToLights();
  tclControl.sendLights();
  
}
class ACircle extends SensatronRoutine {
//  int w = 0;
//  int h = 0;
//  
//  void reinit() {
//    w = pg.width;
//    h = pg.height;
//  }
  
  int diameter;
  ACircle() {
     diameter = 5;
  }
  
  ACircle(int circleSize) {
     diameter = circleSize;
  }

  public void draw() {
    draw(mouseX, mouseY);
  }
  
  public void draw(int inputX, int inputY) {
    pg.beginDraw();
    pg.colorMode(HSB, 255);
    pg.background(inputY, inputX, 255);
    pg.noStroke();
    pg.fill(0, 255, 0);
//    pg.ellipse(inputX,inputY, diameter, diameter);
    pg.endDraw();
  }
}
// Camera setup:


Capture cam;

class CameraInput {
  CameraInput(PApplet parent) {
    try {
  //    cam = new Capture(parent, 400, 300, "Logitech Camera");
      cam = new Capture(parent, 320, 180, "FaceTime HD Camera (Built-in)"); // retina machine
  //    cam = new Capture(parent, 320, 180, "Built-in iSight"); // original 13"
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
  
  public void drawCameraData() {
    cam.loadPixels();
    image(cam, 0, 0); 
  }
  
}

public void captureEvent(Capture c) {
  c.read();
}
class CircleAnimation extends SensatronRoutine {
  int diameter; // Size of the largest single circle ever drawn
  int[] xpos;
  int[] ypos;
  
  CircleAnimation() {
     diameter = 100;
     reinit();
  }
  
  CircleAnimation(int circleSize) {
     diameter = circleSize;
     reinit();
  }

  public void reinit() {
    // Declare two arrays with 50 elements.
    xpos = new int[100]; 
    ypos = new int[100];
    
    // Initialize all elements of each array to zero.
    for (int i = 0; i < xpos.length; i ++ ) {
      xpos[i] = 0; 
      ypos[i] = 0;
    }
  }

  public void draw() {
    draw(mouseX, mouseY);
  }
  
  public void draw(int inputX, int inputY) {
    pg.beginDraw();
    pg.colorMode(HSB, 255);
    pg.background(inputY, 255, 100);
    
    // Shift array values
    for (int i = 0; i < xpos.length-1; i ++ ) {
      // Shift all elements down one spot. 
      // xpos[0] = xpos[1], xpos[1] = xpos = [2], and so on. Stop at the second to last element.
      xpos[i] = xpos[i+1];
      ypos[i] = ypos[i+1];
    }
  //  delay(mouseY);
    
    // New location
    xpos[xpos.length-1] = inputX; // Update the last spot in the array with the mouse location.
    ypos[ypos.length-1] = 0;
    
    // Draw everything
    pg.pushMatrix();
//    rotateX(radians(-45));
//    rotateY(radians(45));
    pg.translate(pg.width/2,pg.height/2); // for P3D renderer
//    translate(width/2,height/2);
  
//    for (int i = 0; i < xpos.length; i ++ ) {
//       // Draw an ellipse for each element in the arrays. 
//       // Color and size are tied to the loop's counter: i.
//      
//  //    rotateZ(radians(i*90)); // for P3D renderer
//      rotate(radians(30));
//      noStroke();
//  //    translate(0,0,-10);
//      fill(255-i*2, 0+i*2.5, 255);
//      ellipse(xpos[i],ypos[i],i,i);
//    }
    for (int i = xpos.length -1; i > 0 ; i-- ) {
       // Draw an ellipse for each element in the arrays. 
       // Color and size are tied to the loop's counter: i.
      
//      rotateZ(radians(i*90)); // for P3D renderer
      pg.rotate(radians(30));
      pg.noStroke();
//      translate(0,0,-10);
      
      pg.fill(255-i*2, 255, 255, inputY); // HSB colors
      
//      fill(255-i*2, 0+i*2.5, 255-i*2, mouseY); // rgb colors
      pg.ellipse(xpos[i],ypos[i],i,i);
    }
    pg.popMatrix();
    pg.endDraw();
  }
}
/*
  Serial ports are a bitch in processing/java.
  Make sure you use this command to set your /var/lock directory to the correct permissions:
  sudo chmod go+rwx /var/lock
  sudo chmod 777 /var/spool/uucp/

  Still, it isn't flawless. It will probably crash the computer sometimes if there is unexpected
  device disconnects (i.e. if the battery dies on the gyro box).

  There doesn't seem to be a "real" fix anywhere...
 */



// The variables need to stay here so the global event hooks can use them
Serial myPort;
boolean gyroOkay = false;
String inString = ""; // Holds the string captured from the serial event

class GyroInput {

	int halfWidth, halfHeight;
	
	PApplet parent; // Needed for any serial init code

	int serialKeepAlive;
	int serialTimeOut = 120; // How many frames to wait before rechecking serial status
	String keepAliveTest = "";
	
	int lf = 10; // ASCII linefeed

	String[] splitSerial;
	int ax, ay, az;

	int rawX, rawY;

	GyroInput(PApplet parent_) {
		parent = parent_;

		halfWidth = width/2;
		halfHeight = height/2;

		// Set up the software serial port for comms over bluetooth:
		gyroOkay = initBlueToothSerial(parent);
		if (!gyroOkay) {
			println("Couldn't open serial port! Continuing anyways...");
			// exit();
		}

		// Set up shutdown functions:
		prepareExitHandler();

		println("Waiting until we settle... DERP");
		delay(1000);
	}

	// This method should be called every frame to keep the serial device connected and updated:
	public void draw() {
		processSerialInputString();
		checkSerialStatus();
	}

	public void checkSerialStatus() {
	  serialKeepAlive++;
	  if (serialKeepAlive >= serialTimeOut) {
	    println("Checking serial port status...");
	    if (keepAliveTest.equals(inString)) {
	     	gyroOkay = false;
	      println("We lost our connection! Reconnecting...");
	      reconnectSerial();
	    } else {
	//      println("Still connected. Updating test string and carrying on...");
	      keepAliveTest = inString;
	      serialKeepAlive = 0;
	    }
	  }
	}

	public void reconnectSerial() {
		if (gyroOkay) {
			myPort.stop();
		}
		if (!initBlueToothSerial(parent)) {
			println("We couldn't reconnect...");
			serialKeepAlive = 0; // Wait for another timeout before trying to reconnect...
		} else {
			println("Reconnected!");
		}
	}

	public boolean initBlueToothSerial(PApplet parent_) {
	  try {
	    // Set up the serial port:
	    String[] availableSerialPorts = Serial.list();
	    for (int i=0; i<availableSerialPorts.length; i++) {
	      println(availableSerialPorts[i]); // Print the available serial ports
	  
	        // Bluetooth: tty.FireFly-57DF-RNI-SPP  runs at 9600
	        // FTDI: tty.usbserial                  runs at 38400
	        // Hacked bluetooth makeymate: tty.FireFly-57DF-RNI-SPP runs at 115200
	  //    if (availableSerialPorts[i].indexOf("tty.usbserial") > 0) {
	      if (availableSerialPorts[i].indexOf("tty.FireFly-57DF-RNI-SPP") > 0) {
	        myPort = new Serial(parent_, availableSerialPorts[i], 115200); 
	        println("Using serial port " + availableSerialPorts[i]);
	        serialKeepAlive = 0;
	        // Set up the serial buffer to only call the serialEvent method when a linefeed is received:
	    	myPort.bufferUntil(lf);
	    	return true;
	      }
	    }
	    println("Can't find serial port!");
	    return false;
	  } catch (Exception e) {
	    println("Serial error!");
	  }
	  return false;
	}

	public void reconnectSerial(PApplet parent_) {
	  myPort.stop();
	  if (!initBlueToothSerial(parent_)) {
	    println("We couldn't reconnect...");
	  } else {
	    println("Reconnected!");
	  }
	}

	public void processSerialInputString() {
  
	  splitSerial = split(inString, ':');
	//  print(inString + " - " + splitSerial.length);
	  if (splitSerial.length==5) {
	//    println(splitSerial[0]);
//	    println(splitSerial[1]);
//	    println(splitSerial[2]);
//	    println(splitSerial[3]);
	    
	    ax = PApplet.parseInt(splitSerial[1]);
	    ay = PApplet.parseInt(splitSerial[2]);
	    az = PApplet.parseInt(splitSerial[3]);
	//    println("Here: "+ az+ ":" + map(az,-17000,17000,-90,90));

            if (ax==5 && ay==5 && az==5) {
              // The gyro boards arduino just told us someone is holding down the button...
              // Time to change the pattern!
//              println("WHAT!??");
              changePattern = true;
              
//              println("ax: " + ax);
//              println("ay: " + ay);
//              println("az: " + az);
              
            } else {
              // Move the gyro data into a "useable" variable:
              rawX = PApplet.parseInt(map(ax,-18000,18000,0,width));
              rawY = PApplet.parseInt(map(ay,-18000,18000,0,height));
            }
	  } else {
	    print(".");
	  }
	  
	  // drawMyBox(halfWidth, halfHeight, 40, -int(map(ax,-18000,18000,-90,90)), 0, int(map(ay,-18000,18000,-90,90)));

	}

}

public void serialEvent(Serial p) {
  inString = (myPort.readString());
//  println(inString); // Debug the serial data coming from the gyro board
}

// Try to clean up the serial port correctly every time the app closes:
private void prepareExitHandler() {
  Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
    public void run () { 
      if (gyroOkay) {
        println("Stopping gyro's serial port");
//        myPort.stop();
      }
    }
  }));
}
class LightDisplay {
  PGraphics lightDisplay;
  int SPACING = 5;
  
  LightDisplay() {
    lightDisplay = createGraphics(width, height, P3D);
    lightDisplay.smooth();
    lightDisplay.lights();
  }
 
  public void drawLights() {
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
    
    // THIS NEEDS TO BE MOVED ELSEWHERE:
//    // Move this information to the physical lights:
//    mapDrawingToLights();
//    sendLights();
  } 
  
}
class MultiSpin extends SensatronRoutine {
//  int w = 0;
//  int h = 0;
//  
//  void reinit() {
//    w = pg.width;
//    h = pg.height;
//  }
  
  int size;
  int changeTimeoutCounter = 0;
  int changeTimeout = 50;
  int backgroundColor;
  
  MultiSpin() {
     size = 5;
  }
  
  MultiSpin(int circleSize) {
     size = circleSize;
  }

  public void draw() {
    draw(mouseX, mouseY);
  }
  
  public void draw(int inputX, int inputY) {
    pg.beginDraw();
    pg.noStroke();
    pg.colorMode(HSB, 255);
    pg.background(backgroundColor);
    pg.pushMatrix();
    pg.imageMode(CENTER);
    pg.translate(width/2, height/2);
    
    pg.rotate(radians(inputX));

    drawBar();
    pg.rotate(radians(30));
    drawBar();
    pg.rotate(radians(30));
    drawBar();
    pg.rotate(radians(30));
    drawBar();
    pg.rotate(radians(30));
    drawBar();
    pg.rotate(radians(30));
    drawBar();
    pg.rotate(radians(30));
    drawBar();
    pg.rotate(radians(30));
    drawBar();
    pg.rotate(radians(30));
    drawBar();
    pg.rotate(radians(30));
    drawBar();
    pg.rotate(radians(30));
    drawBar();
    pg.rotate(radians(30));
    drawBar();
    
    pg.popMatrix();
    pg.endDraw();
    
//    changeTimeoutCounter += 1;
//    if (changeTimeoutCounter > changeTimeout) {
//      changeColors();
//      changeTimeoutCounter = 0;
//    }
    // change every 30 degrees
    if (inputX%30 == 1) {
      changeColors();
//      changeTimeoutCounter = 0;
    }
  }
  
  public void changeColors() {
    colorMode(HSB, 255);
    backgroundColor = color((int)random(255), 255, 150); 
  }
  
  
  public void drawBar() {
    int colors = 100;
    for (int i = 0; i < colors; i++) {
      pg.fill((255/colors)*i, 255, 255); // HSB colors
      pg.rect(((width/2)/colors)*i, -size/2, (width/2)/colors, size);
    }
  }
}
double dRad;
int[][] lights;

class RadialControl {
  
  RadialControl() {
    dRad = (Math.PI*2)/STRANDS;
    lights = new int[STRANDS][STRAND_LENGTH];
  }
  
  public int[] mapRadialArrayToLights() {
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
  
  
  
  // These are really more like animations but maybe we can use them as a tool to help us write animations later:
  public void randomizeAllLights() {
    for (int strand = 0; strand < STRANDS; strand++) {
      for (int lightNum = 0; lightNum < STRAND_LENGTH; lightNum++) {
        lights[strand][lightNum] = getRandomColor();
      }
    }
  }
  
  public void setAllLights(int c) {
    for (int strand = 0; strand < STRANDS; strand++) {
      for (int lightNum = 0; lightNum < STRAND_LENGTH; lightNum++) {
        lights[strand][lightNum] = c;
      }
    }
  }
  
  public void setOneLight(int strand, int lightNum, int c) {
    lights[strand][lightNum] = c;
  }
  
  public int getRandomColor() {
    return color((int)random(255), (int)random(255), (int)random(255));
  }
}
class RawConversion {
  int SPACING = 5; // This spacing is for PULLING data from images not putting lights on the screen
  
  RawConversion() {
    
  }
  
  // This method strips raw color data from a given PImage and plops it DIRECTLY into the radial lights array:
  public void stripRawColors(PImage toLoad) {
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
  
//  // This method draws the raw data to the screen:
//  void drawRawData() {
//    if (cam.available()) {
//      cam.read();
//      cam.loadPixels();
//    }
//    image(cam, width-cam.width, 100); 
//    color c = cam.pixels[1*cam.width+1]; //pixels[y*cam.width+x]
//  //  color c = cam.get(60,90);
//    noStroke();
//    fill(c);
//    rect(width-30, height-30, 20, 20);  
//  }


}
class SensatronRoutine {
  PGraphics pg;
  SensatronRoutine() {
    pg = createGraphics(width, height, P3D);
    pg.smooth();
  }
  
  public void draw() { }
  
  public void updateScreen() {
    image(pg, 0, 0); 
  }
  
  public void updateLights() {
    println("Updating lights");
//    image(pg, 0, 0); 
  }
}
class Spin extends SensatronRoutine {
//  int w = 0;
//  int h = 0;
//  
//  void reinit() {
//    w = pg.width;
//    h = pg.height;
//  }
  
  int size;
  Spin() {
     size = 5;
  }
  
  Spin(int circleSize) {
     size = circleSize;
  }

  public void draw() {
    draw(mouseX, mouseY);
  }
  
  public void draw(int inputX, int inputY) {
    pg.beginDraw();
    pg.noStroke();
    pg.colorMode(HSB, 255);
    pg.background(255,255,0);
    pg.pushMatrix();
    pg.imageMode(CENTER);
    pg.translate(width/2, height/2);
    
    pg.rotate(radians(inputX));
    pg.fill(inputY, 255, 255); // HSB colors
    pg.rect(-width/2, -size/2, width, size);
    
    pg.rotate(radians(-inputX*2));
    pg.fill(inputY, 255, 255); // HSB colors
    pg.rect(-width/2, -size/2, width, size);
    
    pg.popMatrix();
    pg.endDraw();
  }
}
/*
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
  
  orange box:
  1 ground  brown
  2 clock   orange
  3 5volts  red
  4 data    black
  
*/

// TCL Library setup


// The TotalControl processing library doesn't define the FTDI pins so we do:
short TC_FTDI_TX  = 0x01;  /* Avail on all FTDI adapters,  strand 0 default */
short TC_FTDI_RX  = 0x02;  /* Avail on all FTDI adapters,  strand 1 default */
short TC_FTDI_RTS = 0x04;  /* Avail on FTDI-branded cable, strand 2 default */
short TC_FTDI_CTS = 0x08;  /* Avail on all FTDI adapters,  clock default    */
short TC_FTDI_DTR = 0x10;  /* Avail on third-party cables, strand 2 default */
short TC_FTDI_DSR = 0x20;  /* Avail on full breakout board */
short TC_FTDI_DCD = 0x40;  /* Avail on full breakout board */
short TC_FTDI_RI  = 0x80;  /* Avail on full breakout board */

class TCLControl {
  
  // TCL Objects:
  TotalControl tc; // TCL data control object itself
  int[] tclArray; // Actual pixel array
  int[] remap; // Remap lookup table
  
  TCLControl() {
    println("Initalizing the TCL Library...");
    tclArray = new int[totalPixels];
    remap = new int[strandCount * pixelsOnStrand];
    
    // Override the default pin outs:
    // This is clock. We don't want to override it:
    println("Customizing pinouts for the Sensatron...");
    //tc.setStrandPin(x,TC_FTDI_CTS);
    tc.setStrandPin(0,TC_FTDI_TX); // default
    tc.setStrandPin(1,TC_FTDI_RX); // default
    tc.setStrandPin(2,TC_FTDI_DTR); // spliting dtr and rts
    
    // Custom lines for the ftdi breakout
    tc.setStrandPin(3,TC_FTDI_RTS);
    tc.setStrandPin(4,TC_FTDI_RI);
    tc.setStrandPin(5,TC_FTDI_DSR);
    tc.setStrandPin(6,TC_FTDI_DCD);
    println("Done customizing pinouts.");
    
    println("Trying to open the FTDI device for bitbanging...");
    int status = tc.open(strandCount, pixelsOnStrand);
    if (status != 0) {
      tc.printError(status);
//      exit();
      println("Continuing without light output!");
    } else {
      println("Device opened.");
    }
  
    buildRemapArray();
//    exit();
  }
  
  public void buildRemapArray() {
    println("Building remap array...");
//    for (int i = 0; i < STRANDS * STRAND_LENGTH; i++) {
//      remap[i] = i;
//    }
//    for (int i = 0; i < STRANDS * STRAND_LENGTH; i++) {
//      remap[i] = 0;
//    }

    // Working radial remap:
    int index = 0;
    for(int i=0; i<STRANDS; i++) {
      println("Setting wand: " + i);
      for(int j=0;j<STRAND_LENGTH;j++) {
        if(j%2==0) { // even led's (0,2,4,6...)
          remap[j-(j/2) + (STRAND_LENGTH * i)] = index;
//          if (i == 1) {
//            println("index " + index + " is: " + remap[index]);
//          }
        } else { // odd led's (1,3,5,7...)
           remap[(STRAND_LENGTH * (i+1)) - (j-(j/2))] = index;
//          if (i == 1) {
//            println("index " + index + " is: " + remap[index]);
//          }
        }
        index += 1;
      }
    }
    
    println("Done building remap array.");
  }
  
  // This will actually send the data in tclArray out to the lights using the remap table:
  public void sendLights() {
    tc.refresh(tclArray, remap);
  }
  
}
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "--full-screen", "--bgcolor=#666666", "--hide-stop", "Sensatron_2013_Lighting_Rig_v2" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
