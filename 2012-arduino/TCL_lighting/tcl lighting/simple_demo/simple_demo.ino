#include <SPI.h>
#include <TCL.h>
#include <math.h>

int i;
const int LEDS = 50; // 50 LED Total Control Lighting Strand
byte colors[LEDS][3]; // This will store the RGB values of various colors

// Pins for TCL connections:
// data   = pin 11
// clock  = pin 13

void setup() {

  Serial.begin(9600);
  Serial.println("Simple TCL LED demo");
  TCL.begin();
 
  for(i=0;i<LEDS;i++) {
    colors[i][0]=0x00;
    colors[i][1]=0x00;
    colors[i][2]=0x00;
  }
  update_strand(); // Update the colors along the strand of LEDs
}

void update_strand() {
  TCL.sendEmptyFrame();
  for(i=0;i<LEDS;i++) {
    TCL.sendColor(colors[i][0],colors[i][1],colors[i][2]);
  }
  TCL.sendEmptyFrame();
}

void loop() {
  
  // All led's 
  for(i=0; i<LEDS; i++) {
    colors[i][0]=0x00;
    colors[i][1]=0xff;
    colors[i][2]=0x00;
  }
  
  update_strand();
}
