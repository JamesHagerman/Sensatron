import processing.serial.*;

int halfWidth, halfHeight;

Serial myPort;
int serialKeepAlive;
int serialTimeOut = 120;
String keepAliveTest = "";
boolean serialOkay = false;

int lf = 10; // ASCII linefeed
String inString = ""; // Holds the string captured from the serial event

String[] splitSerial;
int ax, ay, az;

void setup() {
  background(80);
  size(500,500,P3D);
  frameRate(60);
  smooth();
//  noStroke();
  halfWidth = width/2;
  halfHeight = height/2;
  
  // Set up the software serial port for comms over bluetooth:
  serialOkay = initBlueToothSerial();
  if (!serialOkay) {
    println("Couldn't open serial port! Exiting...");
    exit();
  }
  
  // Set up shutdown functions:
  prepareExitHandler();
  
  println("Waiting until we settle... DERP");
  delay(1000);
}

private void prepareExitHandler() {
  Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
    public void run () { 
      if (serialOkay) {
        println("Stopping serial port");
        myPort.stop();
      }
    }
  }));
}

void draw() {
  processSerialInputString();
  checkSerialStatus();
}

void checkSerialStatus() {
  serialKeepAlive++;
  if (serialKeepAlive >= serialTimeOut) {
    println("Checking serial port status...");
    if (keepAliveTest.equals(inString)) {
      println("We lost our connection! Reconnecting...");
      reconnectSerial();
    } else {
//      println("Still connected. Updating test string and carrying on...");
      keepAliveTest = inString;
      serialKeepAlive = 0;
    }
  }
}

void reconnectSerial() {
  myPort.stop();
  if (!initBlueToothSerial()) {
    println("We couldn't reconnect...");
  } else {
    println("Reconnected!");
  }
}

boolean initBlueToothSerial() {
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
         myPort = new Serial(this, availableSerialPorts[i], 115200); 
         println("Using serial port " + availableSerialPorts[i]);
         serialKeepAlive = 0;
         break;
      }
    } 
    
    // Set up the serial buffer to only call the serialEvent method when a linefeed is received:
    myPort.bufferUntil(lf);
    return true;
  } catch (Exception e) {
    println("Serial error!");
  }
  return false;
}

//==============
// MY FUNCTIONS:

void drawMyBox(int x, int y, int size, int rotationX, int rotationY, int rotationZ) {
  
  // Instead of this:
//  fill(192);
//  int posX = x-(size/2);
//  int posY = y-(size/2); 
//  rect(posX, posY, size, size); 

  // We use this to make drawing multiple objects way easier:
  pushMatrix();
  fill(192);
  translate(x, y);
  rotateX(radians(rotationX));
  rotateY(radians(rotationY));
  rotateZ(radians(rotationZ));
//  rect(0-(size/2), 0-(size/2), size, size);
  box(100);
  translate(0, -100);
  box(20);
  popMatrix();
}

void respondToMouse() {
//  println("Mouse X " + mouseX);
//  println("Mouse Y " + mouseY);
//  background(80);
//  drawSquare(halfWidth, halfHeight, 40, -(mouseY-halfHeight), mouseX-halfWidth, 0); 
}

void processSerialInputString() {
  
  splitSerial = split(inString, ':');
//  print(inString + " - " + splitSerial.length);
  if (splitSerial.length==5) {
//    println(splitSerial[0]);
//    println(splitSerial[1]);
//    println(splitSerial[2]);
//    println(splitSerial[3]);
    
    ax = int(splitSerial[1]);
    ay = int(splitSerial[2]);
    az = int(splitSerial[3]);
//    println("Here: "+ az+ ":" + map(az,-17000,17000,-90,90));
  } else {
    println("No data received...");
  }
  background(80);
  drawMyBox(halfWidth, halfHeight, 40, -int(map(ax,-18000,18000,-90,90)), 0, int(map(ay,-18000,18000,-90,90)));

}

//========
// EVENTS:
void mousePressed() {
  respondToMouse(); 
}

void mouseDragged() {
  respondToMouse();
}

void mouseMoved() {
  respondToMouse();
}

void serialEvent(Serial p) {
  inString = (myPort.readString());
//  println(inString);
}
