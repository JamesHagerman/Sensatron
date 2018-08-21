#include <SPI.h>
#include <TCL.h>

int i, j;

const int WAND_COUNT = 1;
const int WAND_LENGTH = 50 ; // 50 LEDs per strand
const int LEDS = WAND_COUNT * WAND_LENGTH; // total led count on all strands

// Analog inputs:
int programPick;    		// TCL_POT1 - AN1 - between 0-10
int blink_speed;    		// TCL_POT2 - AN2 - between 0-1024
int brightness;     		// TCL_POT3 - AN3 - between 0-255
double programControl; 	// TCL_POT4 - AN4 - between 0-1023.0

// Audio detection stuff:
// Spectrum analyzer shield pins
int spectrumStrobe = 8;
int spectrumReset = 9;
int spectrumAnalogL = 4;  //4 for left channel, 5 for right.
int spectrumAnalogR = 5;  //4 for left channel, 5 for right.
int spectrumL[7]; 	// array to hold spectrum values for the left channel
int spectrumR[7]; 	// array to hold spectrum values for the right channel
byte band;	// variable to hold which band we're looking at
// int audioPinL = A4;
// int audioPinR = A5;
int audioValueL; // Left audio value
int audioValueR; // Right audio value
int divisor; // divide single to adjust for low volumes


byte color_value_red = 0xff;
byte color_value_green = 0xff;
byte color_value_blue = 0xff;


void setup() {
  // Turn on Serial port:
  Serial.begin(115200);
//  Serial.println("");
//  Serial.println("LED sequencing written by James Hagerman");
  
    // Turn on TCL library:
  TCL.begin();
  set_red();
  
  // Initialize the spectrum spread board:
  setupSpectrumBoard();
  
}

// Spectrum board setup:
void setupSpectrumBoard() {
	//Setup pins to drive the spectrum analyzer. 
  pinMode(spectrumReset, OUTPUT);
  pinMode(spectrumStrobe, OUTPUT);

  //Init spectrum analyzer
  digitalWrite(spectrumStrobe,LOW);
    delay(1);
  digitalWrite(spectrumReset,HIGH);
    delay(1);
  digitalWrite(spectrumStrobe,HIGH);
    delay(1);
  digitalWrite(spectrumStrobe,LOW);
    delay(1);
  digitalWrite(spectrumReset,LOW);
    delay(5);
  // Reading the analyzer now will read the lowest frequency.
	
}

// Read 7 band equalizer on each channel
void readSpectrum() {
  // band 0 = Lowest Frequencies.
  for(band=0;band <7; band++) {
    // spectrumL[band] = (analogRead(spectrumAnalogL) + analogRead(spectrumAnalogL) ) >>1; //Read twice and take the average by dividing by 2
    // spectrumR[band] = (analogRead(spectrumAnalogR) + analogRead(spectrumAnalogR) ) >>1; //Read twice and take the average by dividing by 2
    spectrumL[band] = analogRead(spectrumAnalogL);
    spectrumR[band] = analogRead(spectrumAnalogR);

    digitalWrite(spectrumStrobe,HIGH);
    digitalWrite(spectrumStrobe,LOW);     
  }
}

void update_strand_real() {
  int i;
  int color;
  //setDoubleBack(); // Double back will use coord[][][] and build the real array into double_back[]
  
  TCL.sendEmptyFrame();
  for(i=0;i<LEDS;i++) {
    //TCL.sendColor(0xff,0x00,0xff);
    TCL.sendColor(color_value_red,color_value_green,color_value_blue);
    //TCL.sendColor(double_back_red[i],double_back_green[i],double_back_blue[i]);
		
    // old:  
    // TCL.sendColor(current_color[i][0],current_color[i][1],current_color[i][2]);
  }
  TCL.sendEmptyFrame();  
}

void set_white() {
  int i;
  TCL.sendEmptyFrame();
  for(i=0;i<LEDS;i++) {
    TCL.sendColor(0xff,0xff,0xff);
  }
  TCL.sendEmptyFrame();  
}

void set_red() {
  int i;
  TCL.sendEmptyFrame();
  for(i=0;i<LEDS;i++) {
    TCL.sendColor(0xff,0x00,0x00);
  }
  TCL.sendEmptyFrame();  
}

void loop() {
  readSpectrum();
  
//  Serial.write((byte)0xdef0);
//  for(band=0;band <7; band++) {
//    spectrumL[band] = analogRead(spectrumAnalogL);
//    spectrumR[band] = analogRead(spectrumAnalogR);
//    Serial.write((byte)spectrumR[band]);
//    Serial.write((byte)spectrumL[band]);
//  }
//  Serial.write((byte)0xfed0);
  Serial.write((byte)0x10);
  
//  if (digitalRead(switchPin) == HIGH) {  // If switch is ON,
//    Serial.write((byte)1);               // send 1 to Processing
//  } else {                               // If the switch is not ON,
//    Serial.write((byte)0);               // send 0 to Processing
//  }
  delay(100);                            // Wait 100 milliseconds
  
//  byte buffer[4];
//  byte command;
//  if (Serial.available() > 0) { 
//    while (Serial.available() > 0) {
//      command = Serial.read();
//      
//      if (command == "c") {
//        set_white();
//        color_value_red = (byte) Serial.read();
//        color_value_green = (byte) Serial.read();
//        color_value_blue = (byte) Serial.read();
//        Serial.print("Wow!");
//      }
//     // set_white();
////      color_value_red = (byte) Serial.read();
////      color_value_green = (byte) Serial.read();
////      color_value_blue = (byte) Serial.read();
//    }
//  }
  
  String inputString = "";
  boolean stringComplete = false;  // whether the string is complete
  
  if (Serial.available() > 0) { 
    while (Serial.available()) {
      // get the new byte:
      color_value_red = (byte)Serial.read(); 
      color_value_green = (byte)Serial.read(); 
      color_value_blue = (byte)Serial.read(); 
      
      // if the incoming character is a newline, set a flag
      // so the main loop can do something about it:
      if (inChar == '\n') {
        set_white();
        stringComplete = true;
      } 
    }
  }
  
  if (stringComplete) {
    update_strand_real();
  }
  
  delay(100);
}
