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

void setup() {
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

void draw() {

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
       fakeMouseY += 1 + int(map(gyroInput.rawY, 0, width, 0, 5));
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
       fakeMouseY += 1+ int(map(gyroInput.rawY, 0, width, 0, 5));
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
