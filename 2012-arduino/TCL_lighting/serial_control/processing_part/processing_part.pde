/**
 * Simple Read
 * 
 * Read data from the serial port and change the color of a rectangle
 * when a switch connected to a Wiring or Arduino board is pressed and released.
 * This example works with the Wiring / Arduino program that follows below.
 */


import processing.serial.*;

Serial myPort;  // Create object from Serial class
int val;      // Data received from the serial port

void setup() 
{
  size(200, 200);
  // I know that the first port in the serial list on my mac
  // is always my  FTDI adaptor, so I open Serial.list()[0].
  // On Windows machines, this generally opens COM1.
  // Open whatever port is the one you're using.
  String portName = Serial.list()[0];
  myPort = new Serial(this, portName, 115200);
  println(Serial.list());
}

void draw()
{
  
 if ( myPort.available() > 0) {  // If data is available,
   while ( myPort.available() > 0) {
     val = myPort.read();         // read it and store it in val
   }
   println(val);
 }

  
//  if ( myPort.available() > 0) {  // If data is available,
//    val = myPort.read();         // read it and store it in val
//  }
//  background(255);             // Set background to white
//  //println(val);
//  if (val == 0) {              // If the serial value is 0,
//    fill(0);                   // set fill to black
//  } 
//  else {                       // If the serial value is not 0,
//    fill(255);                 // set fill to light gray
//  }
//  rect(50, 50, 100, 100);
}



/*

// Wiring / Arduino Code
// Code for sensing a switch status and writing the value to the serial port.

int switchPin = 4;                       // Switch connected to pin 4

void setup() {
  pinMode(switchPin, INPUT);             // Set pin 0 as an input
  Serial.begin(9600);                    // Start serial communication at 9600 bps
}

void loop() {
  if (digitalRead(switchPin) == HIGH) {  // If switch is ON,
    Serial.print(1, BYTE);               // send 1 to Processing
  } else {                               // If the switch is not ON,
    Serial.print(0, BYTE);               // send 0 to Processing
  }
  delay(100);                            // Wait 100 milliseconds
}

*/
