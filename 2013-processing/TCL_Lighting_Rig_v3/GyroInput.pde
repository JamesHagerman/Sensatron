/*
  Serial ports are a bitch in processing/java.
  Make sure you use this command to set your /var/lock directory to the correct permissions:
  sudo chmod go+rwx /var/lock
  sudo chmod 777 /var/spool/uucp/

  Still, it isn't flawless. It will probably crash the computer sometimes if there is unexpected
  device disconnects (i.e. if the battery dies on the gyro box).

  There doesn't seem to be a "real" fix anywhere...
 */

import processing.serial.*;

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
	void keepAlive() {
		processSerialInputString();
		checkSerialStatus();
	}

	void checkSerialStatus() {
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

	void reconnectSerial() {
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

	boolean initBlueToothSerial(PApplet parent_) {
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

	void reconnectSerial(PApplet parent_) {
	  myPort.stop();
	  if (!initBlueToothSerial(parent_)) {
	    println("We couldn't reconnect...");
	  } else {
	    println("Reconnected!");
	  }
	}

	void processSerialInputString() {
  
	  splitSerial = split(inString, ':');
	//  print(inString + " - " + splitSerial.length);
	  if (splitSerial.length==5) {
	//    println(splitSerial[0]);
//	    println(splitSerial[1]);
//	    println(splitSerial[2]);
//	    println(splitSerial[3]);
	    
	    ax = int(splitSerial[1]);
	    ay = int(splitSerial[2]);
	    az = int(splitSerial[3]);
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
              rawX = int(map(ax,-18000,18000,0,width));
              rawY = int(map(ay,-18000,18000,0,height));
            }
	  } else {
	    print(".");
	  }
	  
	  // drawMyBox(halfWidth, halfHeight, 40, -int(map(ax,-18000,18000,-90,90)), 0, int(map(ay,-18000,18000,-90,90)));

	}

}

void serialEvent(Serial p) {
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
