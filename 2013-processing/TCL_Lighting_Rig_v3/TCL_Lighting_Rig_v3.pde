/*
  TCL Lighting Rig v3
  by James Hagerman
  on December 5, 2013

  This app started off as the control program for the Sensatron art car owned and opperated
  by Jason Siadek

  I, James, have been working on this code and have been turning it into an overall lighting 
  rig for use with TCL lights. 
 */
 
//============================
// Hardware definitions:

// Lighting class instances:
TCLControl tclControl;

// Gyro input:
GyroInput gyroInput; // not used in this project but wont compile right now without this

// Kinect management is done by the kinect manager:
KinectManager kinectManager;

// Camera input class:
CameraInput cameraInput; // We aren't using the camera either... yet...

// Fake Mouse input definition:
// Note: This is not hardware. This just fakes someone moving a mouse around:
int fakeMouseX;
int fakeMouseY;
int direction = 1;
int direction2 = 1;

// End hardware definitions
//============================


//============================
// Hardware enabling:

// These variables will let us define which of the above hardware we are actually using:
// If any of these are false, the code will not try to initialize the related hardware.
boolean lightsEnabled = true;
boolean cameraEnabled = false;
boolean gyroEnabled   = false;
boolean kinectEnabled = true;
boolean fakeMouseEnabled = false;

// End hardware enabling
//============================


//============================
// Display and Data management classes:
boolean debugOn = true;

// Radial data and display control:
// RadialControl radialControl;

// Spiral data and display control:
// SpiralControl spiralControl;

// Clickable data and display control:
ClickControl clickControl;

// End Display and Data management clases
//============================


//============================
// Animation related definitions:

// Animation related variables:
AnimationRoutine[] allAnimations; // Place to hold all known animations
AnimationRoutine currentAnimation; // a reference to the current animation object

// The animations themselves:
// ACircle aCircle; // A single circle controlled by the mouse
// CircleAnimation originalCircles;
// Spin spin;
// MultiSpin multiSpin;
// KinectTree kTree;

// How many animations, in total, the system knows about:
int animationCount = 5;

// Animation control:
int animationIndex = 0;
int animationIndexMax;

// Automatic animation changing:
boolean changePattern; // We need a way for classes to tell us it's time to change patterns.
int patternChangeTimer = 0;
int patternChangeTimeout = 1000;


// Animation morph inputs:
//
// All animations should use the same variable names for input so we can better separate physical 
// input devices into their own abstraction. These variables should end up replacing fakeMouseX 
// and fakeMouseY at some point in the future:
int inputX, inputY, inputZ, inputU, inputV, inputW;

// End animation related definitions
//============================

void setup() {
  size(500,500, P3D);
  frameRate(120);

  // Init some data management, mapping, and display stuff:
  // radialControl = new RadialControl(); // Radial mapping tools
  // spiralControl = new SpiralControl(); // Radial mapping tools
  clickControl = new ClickControl(); // Click mapping tools
  
  // Init all of the hardware:

  // Init TCL lights. This will try to connect to the hardware!!
  if (lightsEnabled) {
    // Hand the mapping calculated by the control system into the TCL control class:
    // tclControl = new TCLControl(radialControl.radialMap);
    tclControl = new TCLControl(clickControl.remapArray); 
  }

  // Try to init the bluetooth gyro. 
  if (gyroEnabled) {
    gyroInput = new GyroInput(this); // We aren't using the bluetooth gyro in this project
  }
  
  // Set up the webcam:
  if (cameraEnabled) {
    cameraInput = new CameraInput(this);
  }

  // Init any attached Kinect devices:
  if (kinectEnabled) {
    kinectManager = new KinectManager(this);
  }
  

  // Init the pattern management:
  changePattern = false;
  
  // Load animations into the animation list:
  loadAnimations();
  
  // Init the input variables if they need to be set:
  fakeMouseX = 0;
  fakeMouseY = 0;
}

void loadAnimations() {

  println("Loading " + animationCount + " animations...");
  allAnimations = new AnimationRoutine[animationCount];

  allAnimations[0] = new ACircle(100);
  allAnimations[1] = new CircleAnimation();
  allAnimations[2] = new Spin(10);
  allAnimations[3] = new MultiSpin(10);
  allAnimations[4] = new KinectTree(100);

  // aCircle = new ACircle(100);
  // originalCircles = new CircleAnimation();
  // spin = new Spin(10);
  // multiSpin = new MultiSpin(10);

  // Set the maximum pattern index count so the auto changer doesn't freak out:
  animationIndexMax = animationCount - 1;

  // Call change animation so we can set bounds and make sure we're ready to run:
  changeAnimation();

}

// This method handles any animation change triggers from:
//  - input hardware
//  - the auto changer
// And also bounds any hard animationIndex values set anywhere else in the code.
void changeAnimation() {
  // If changePattern is true, one of the classes is asking us to increment the animation pattern.
  if (changePattern) {
    println("Changing patterns!");
    animationIndex += 1;
    
    // Since some animations suck, we can skip them with a remap:
    // ToDo: Make an easier to use remap function. Lookup array maybe?
    if (animationIndex == 0) {
      animationIndex = 1; // This is skipping the first bullshit pattern.
    }
    if (animationIndex == 2) {
      animationIndex = 3; // This is skipping the second bullshit pattern.
    }
    
    // Wrap the pattern index back around to zero so auto incrementing works correctly:
    if (animationIndex > animationIndexMax) {
      println("Starting from the first pattern.");
      animationIndex = 0;
    }
    changePattern = false;
  }

  // The animationIndex can be set arbitrarily. We must continuely bounds check the current animationIndex to 
  // ensure it is a "real" value so we don't overflow the animation array by accident:
  if (animationIndex > animationIndexMax) {
    animationIndex = animationIndexMax;
  }
  if (animationIndex < 0) {
    animationIndex = 0;
  }

  // Set the current animation to be one of the ones in the allAnimations array:
  currentAnimation = allAnimations[animationIndex];

}

// This method will take data from any enabled hardware devices and map that data to one or more of the
// input variables. Mouse is always mapped to inputX and inputY but hardware data is allowed to override
// the mouse values.
//
// ToDo: Think about moving this mapping into the hardware classes themselves...
void updateHardwareInputs() {

  // Always map mouse input into the variables so we can always be sure of some input method:

  inputX = mouseX;
  inputY = mouseY;

  if (gyroEnabled) {
    // Keep the gyro data up to date and connected:
    gyroInput.keepAlive();

    if (!gyroOkay) {
      patternChangeTimer += 1;
      if (patternChangeTimer > patternChangeTimeout) {
        println("Automatically changing patterns");
        patternChangeTimer = 0;
        changePattern = true;
      }
    } else {
      inputX = gyroInput.rawX;
      inputY = gyroInput.rawY;
    }

    // We need to automatically change or set the animation we're running if there is no gyro attached:
    if (!gyroOkay) {
      animationIndex = 1; // Hard code the damn thing to use the COOL animation if we don't have the gyro attached.
    }
  }

  if (kinectEnabled) {
    kinectManager.parse();

    PVector kB = kinectManager.getBlob(0, 0); // 0,0: first kinect, first blob
    if (kB != null) {
      inputX = (int) kB.x;
      inputY = (int) kB.y;
    }

  }


  // ToDo: Figure out how to implament fakeMouse movements:
  if (fakeMouseEnabled) {

    // Clean this shit up:
    if (animationIndex == 0) {
      fakeMouseX += 10;
      if (fakeMouseX >= width) {
        fakeMouseX = 0;
      }
      fakeMouseY += (1 * direction);
      if (fakeMouseY >= 360 || fakeMouseY <= 0) {
        direction = direction * -1;
        fakeMouseY += (1 * direction);
      }
    } else if (animationIndex == 1) {
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
    } else if (animationIndex == 2) {
      fakeMouseX += 10;
      if (fakeMouseX >= width) {
        fakeMouseX = 0;
      }
      fakeMouseY += 10;
      if (fakeMouseY >= 180) {
        fakeMouseY = -0;
      }
    } else if (animationIndex == 3) {
      fakeMouseY += 1;
      if (fakeMouseY >= 180) {
        fakeMouseY = -0;
      }
    }

    inputX = fakeMouseX;
    inputY = fakeMouseY;
  }


}

void draw() {

  // Update any hardware input devices:
  // Note: This function lets hardware trigger animaion changes!
  updateHardwareInputs();

  // Manage any animation changes caused by hardware OR the auto switcher. And sets currentAnimation 
  // to one of the known animations:
  changeAnimation();

  // Process the current animation:
  currentAnimation.draw(inputX, inputY);

  // Dump the current animation frame into the light array:
  // radialControl.stripRawColors(currentAnimation.pg);
  clickControl.stripRawColors(currentAnimation.pg);

  if (cameraEnabled) {
    // Dump the current camera frame into the light array:
    // radialControl.stripRawColors(cam); 
  }

  // Only handle the TCL hardware output if it's enabled:
  if (lightsEnabled) {
    // Map the control systems array to the hardware output array:
    // tclControl.tclArray = radialControl.mapArrayToLights();
    tclControl.tclArray = clickControl.mapDataToLights();

    // Actually send the light array to the hardware lights:
    tclControl.sendLights();
  }

  // Update the on screen display:
  updateDisplay();
  
}

// This method will update the on screen display so we can see what is going on with the lights:
// ToDo: Figure out how to switch between the three onscreen display modes:
void updateDisplay() {

  if (!debugOn) {
    // Draw the current animation frame to the screen:
    // currentAnimation.updateScreen();

    // Draw 3D radial display
    // radialControl.drawLights();

    // Draw the 3D spiral display:
    // spiralControl.drawLights();

    // Draw the 2D Click display
    clickControl.drawLights(currentAnimation.pg);
  } else {
    if (kinectEnabled) {
      kinectManager.draw();
    }
  }

  if (kinectEnabled) {
    kinectManager.drawDebug(0);
  }



  if (cameraEnabled) {
    // Draw the camera data to the screen:
    cameraInput.drawCameraData();
  }
}


// Handle keyinput:
void keyPressed(){
  // q to quit gracefully so we don't break the driver:
  if (key=='q') {
    if (kinectEnabled) {
      kinectManager.quit();
    }
    exit();
  }

  if (key == ' ') {
    debugOn = !debugOn;
  }

  // The keys , and . allow us to change the pattern manually:
  if (key == '.') {
    animationIndex += 1;
    println("animationIndex is now: " + animationIndex);
  }
  if (key == ',') {
    animationIndex -= 1;
    println("animationIndex is now: " + animationIndex);
  }
  // The / key allows us to emulate the "changePattern" triggers sent by the gyro football:
  if (key == '/') {
    changePattern = true;
    println("Anaimtion change triggered...");
  }

  if (kinectEnabled) {
    float t = kinectManager.getThreshold();
    if (key==']') {
      t+=5;
      kinectManager.setThreshold(t);
    } else if (key=='[') {
      t-=5;
      kinectManager.setThreshold(t);
    }

    if (key == 'z') {
      int newTilt = kinectManager.getTilt() - 5;
      kinectManager.setTilt(newTilt);
    } else if (key == 'x') {
      int newTilt = kinectManager.getTilt() + 5;
      kinectManager.setTilt(newTilt);
    }

    if (key == 'r') {
      kinectManager.resetDepths();
    }

    if (key == 'f') {
      kinectManager.fixDepths();
    }

    int minSize = kinectManager.getMinBlob();
    int maxSize = kinectManager.getMaxBlob();
    if (key == CODED) {
      // Left and right change min size
      if (keyCode == LEFT) {
        if (minSize > 11000) {
          minSize -= 1000;
        } else {
          minSize -= 100;
        }
        kinectManager.setMinBlob(minSize);
        kinectManager.setMaxBlob(maxSize);
      } else if (keyCode == RIGHT) {
        if (minSize > 11000) {
          minSize += 1000;
        } else {
          minSize += 100;
        }
        kinectManager.setMinBlob(minSize);
        kinectManager.setMaxBlob(maxSize);
      }
      // Up and down change max size
      if (keyCode == DOWN) {
        if (maxSize > 11000) {
          maxSize -= 1000;
        } else {
          maxSize -= 100;
        }
        kinectManager.setMinBlob(minSize);
        kinectManager.setMaxBlob(maxSize);
      } else if (keyCode == UP) {
        if (maxSize > 11000) {
          maxSize += 1000;
        } else {
          maxSize += 100;
        }
        kinectManager.setMinBlob(minSize);
        kinectManager.setMaxBlob(maxSize);
      }
    }
  }

}

// Handle mouseinput:
void mousePressed() {
  clickControl.handleMouse();
}
