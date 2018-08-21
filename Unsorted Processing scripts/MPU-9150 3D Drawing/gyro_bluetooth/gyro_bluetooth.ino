// I2C device class (I2Cdev) demonstration Arduino sketch for MPU9150
// 1/4/2013 original by Jeff Rowberg <jeff@rowberg.net> at https://github.com/jrowberg/i2cdevlib
//          modified by Aaron Weiss <aaron@sparkfun.com>
//
// Changelog:
//     2011-10-07 - initial release
//     2013-1-4 - added raw magnetometer output

/* ============================================
 I2Cdev device library code is placed under the MIT license
 
 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:
 
 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.
 
 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.
 ===============================================
 */

// Arduino Wire library is required if I2Cdev I2CDEV_ARDUINO_WIRE implementation
// is used in I2Cdev.h
#include "Wire.h"

// I2Cdev and MPU6050 must be installed as libraries, or else the .cpp/.h files
// for both classes must be in the include path of your project
#include "I2Cdev.h"
#include "MPU6050.h"
#include "HardwareSerial.h"

#include <SoftwareSerial.h>

// class default I2C address is 0x68
// specific I2C addresses may be passed as a parameter here
// AD0 low = 0x68 (default for InvenSense evaluation board)
// AD0 high = 0x69
MPU6050 accelgyro;

int16_t ax, ay, az;
int16_t gx, gy, gz;
int16_t mx, my, mz;

#define LED_PIN 13
bool blinkState = false;

#define rxPin 10
#define txPin 11

// Set up a new software serial port:
SoftwareSerial bluetooth(rxPin, txPin); // RX, TX
char dataResponse[512];
String inputString = "";
boolean stringComplete = false; 

int buttonTimeout;
int buttonTimeoutMax;
int buttonPin = 2;

void setup() {
  // join I2C bus (I2Cdev library doesn't do this automatically)

  Wire.begin();

  // initialize serial communication
  // (38400 chosen because it works as well at 8MHz as it does at 16MHz, but
  // it's really up to you depending on your project)
  Serial.begin(38400);

  // Set pin modes for software serial port pins, 10 and ll (rx and tx)
  pinMode(rxPin, INPUT);
  pinMode(txPin, OUTPUT);
  bluetooth.begin(115200);

  // Init bluetooth:
  initBluetooth();

  // initialize device
  Serial.println("Initializing I2C devices...");
  accelgyro.initialize();

  // verify connection
  Serial.println("Testing device connections...");
  Serial.println(accelgyro.testConnection() ? "MPU6050 connection successful" : "MPU6050 connection failed");

  // configure Arduino LED for
  pinMode(LED_PIN, OUTPUT);

  buttonTimeout = 0;
  buttonTimeoutMax = 100;
  pinMode(buttonPin, INPUT);
}

void loop() {
  buttonTimeout += 1;
  if (buttonTimeout >= buttonTimeoutMax) {
    blinkState = !blinkState;
    digitalWrite(LED_PIN, blinkState);

    if (digitalRead(buttonPin) == HIGH) {
      delay(50);
      blinkState = !blinkState;
      digitalWrite(LED_PIN, blinkState);
      delay(50);
      blinkState = !blinkState;
      digitalWrite(LED_PIN, blinkState);
      delay(50);
      blinkState = !blinkState;
      digitalWrite(LED_PIN, blinkState);
      delay(50);
      blinkState = !blinkState;
      digitalWrite(LED_PIN, blinkState);
      delay(50);
      blinkState = !blinkState;
      digitalWrite(LED_PIN, blinkState);
      delay(50);
      blinkState = !blinkState;
      digitalWrite(LED_PIN, blinkState);
      delay(50);
      blinkState = !blinkState;
      digitalWrite(LED_PIN, blinkState);
      bluetooth.print(":");
      bluetooth.print(0x05); 
      bluetooth.print(":");
      bluetooth.print(0x05); 
      bluetooth.print(":");
      bluetooth.print(0x05); 
      bluetooth.println(":");
    }
    buttonTimeout = 0;
  } 
  else {
    // read raw accel/gyro measurements from device
    accelgyro.getMotion9(&ax, &ay, &az, &gx, &gy, &gz, &mx, &my, &mz);

    // these methods (and a few others) are also available
    //    accelgyro.getAcceleration(&ax, &ay, &az);
    //    accelgyro.getRotation(&gx, &gy, &gz);

    // display tab-separated accel/gyro x/y/z values
    //    Serial.print("a/g/m:");
    //    Serial.print(ax); Serial.print(":");
    //    Serial.print(ay); Serial.print(":");
    //    Serial.print(az); Serial.println(":");
    //    Serial.print(gx); Serial.print(":");
    //    Serial.print(gy); Serial.print(":");
    //    Serial.println(gz); //Serial.print("\t");
    //    Serial.print(mx); Serial.print("\t");
    //    Serial.print(my); Serial.print("\t");
    //    Serial.println(mz);

    // Send the data over the bluetooth serial port for processing to pick up:
      bluetooth.print(":");
      bluetooth.print(ax); bluetooth.print(":");
      bluetooth.print(ay); bluetooth.print(":");
      bluetooth.print(az); bluetooth.println(":");  
  }

  // blink LED to indicate activity
  //    blinkState = !blinkState;
  //    digitalWrite(LED_PIN, blinkState);

//  softwareSerialReceive();
//  Serial.print("Response from RN-42: '");
//  Serial.print(inputString);
//  Serial.println("'");
//  
//  // Slow down our log rate:
//  delay(5000);

  // clear the string:
  inputString = "";
  stringComplete = false;
}


void initBluetooth() {
  Serial.println("Starting bluetooth init");
  delay(320); // delay to let software serial and bluetooth module to pick up from reset

  // Make sure we aren't in command mode before we start (when someone presses the arduino reset button):
  exitCommandMode();
  delay(500);

  // Now, reenter command mode so we can try to auto connect:
  enterCommandMode();
  delay(500);
  
  // Fix the serial settings so we can actually see what the hell the responses are:
//  setSerialSettings();
//  delay(1000);

  // Make sure we're disconnected first!
  disconnect();
  delay(5000);

  // Try connecting to a host:
  connectToHost();
  delay(500);

  // Exit command mode so we can get back to sending data:
  exitCommandMode();
  delay(200);



  Serial.println("Done trying.");
}

void setSerialSettings() {
  Serial.println("Fixing serial parity...");
  bluetooth.println("U,115K,N");
  
  softwareSerialReceive();
  Serial.print("Response from RN-42: '");
  Serial.print(inputString);
  Serial.println("'");
  
  inputString = "";
  stringComplete = false;
}

char enterCommandMode() {
  Serial.println("Entering command mode...");
  // The stupid bluetooth module is SLOW at reading stuff out of it's serial buffer. So we need a delay between characters...
  bluetooth.print("$");
  delay(100);
  bluetooth.print("$");
  delay(100);
  bluetooth.print("$");
  //   delay(100);
  
  softwareSerialReceive();
  Serial.print("Response from RN-42: '");
  Serial.print(inputString);
  Serial.println("'");
  
  inputString = "";
  stringComplete = false;
}

void exitCommandMode() {
  Serial.println("Exiting command mode...");
  //  bluetooth.print("-");
  //   delay(100);
  //   bluetooth.print("-");
  //   delay(100);
  //   bluetooth.print("-");
  //   delay(100);
  //   bluetooth.print("\r\n");
  bluetooth.println("---");

  softwareSerialReceive();
  Serial.print("Response from RN-42: '");
  Serial.print(inputString);
  Serial.println("'");
  
  inputString = "";
  stringComplete = false;
}

void connectToHost() {
  Serial.println("Trying to connect to host...");
  // The stupid bluetooth module is SLOW at reading stuff out of it's serial buffer. So we need a delay between characters...
  bluetooth.println("C,68A86D57EEB4");
  //   delay(100);
  //   bluetooth.print("$");
  //   delay(100);
  //   bluetooth.print("$");
  //   delay(100);

  softwareSerialReceive();
  Serial.print("Response from RN-42: '");
  Serial.print(inputString);
  Serial.println("'");
  
//  delay(2000);
//
//  // Loop until we're actually connected
//  int testCount = 0;
//  while(connectionStatus() == false) {
//    Serial.print("Checking connection status...");
//    testCount += 1;
//    if (testCount >= 100) {
//      Serial.print("Can't connect!");
//      return;
//    }
//  }
//  Serial.println("Looks like we're connected!");
//  inputString = "";
//  stringComplete = false;
}

boolean connectionStatus() {
  Serial.print("Checking connection status...");
  bluetooth.println("GK");
  softwareSerialReceive();
  Serial.print("Response from RN-42: '");
  Serial.print(inputString);
  Serial.println("'");
  Serial.print("WHAT:");
  Serial.println(inputString.charAt(3));
  if (inputString.charAt(3) == 'K') {
    return true;
  } else {
    return false;
  }
}

void disconnect() {
  Serial.println("Disconnecting from host...");
  bluetooth.println("K");

  softwareSerialReceive();
  Serial.print("Response from RN-42: '");
  Serial.print(inputString);
  Serial.println("'");
  inputString = "";
  stringComplete = false;
}

//void serialEvent() {
//  String hardwareInput = "";
//  while (Serial.available()) {
//    // get the new byte:
//    char inChar = (char)Serial.read(); 
//    // add it to the inputString:
//    hardwareInput += inChar;
//    // if the incoming character is a newline, set a flag
//    // so the main loop can do something about it:
//    if (inChar == '\n') {
//      Serial.print("Sending command '");
//      Serial.print(hardwareInput);
//      Serial.println("' to the bluetooth module");
//      bluetooth.println(hardwareInput);
//    } 
//  }
//}


void softwareSerialReceive() {
  inputString = "";
  stringComplete = false;
  
  // We need to wait for bytes to return. We don't know if we even will get a response...
  for (int i = 0; i < 100; i++) {
    // If .available is greater then 0 we have bytes to read in, one at a time.
    if (bluetooth.available()>0) {
      Serial.print("Got ");
      Serial.print(bluetooth.available());
      Serial.println(" bytes!:");
      
      // Read the bytes one by one:
      while (bluetooth.available()) {
        int raw = bluetooth.read();
        Serial.print("Character ");
        Serial.print(bluetooth.available());
        Serial.print(" looks like: ");
        Serial.println(raw, BIN);
        char c = (char)raw;
        inputString += c;
//        Serial.print("Character: '");
//        Serial.print(c);
//        Serial.println("'");
//        Serial.print(bluetooth.available());
//        Serial.println(" bytes left to read...");
      }
    } else {
      Serial.print(".");
    }
    delay(10);
  }
  
  Serial.println("No newline received.");
  
}

