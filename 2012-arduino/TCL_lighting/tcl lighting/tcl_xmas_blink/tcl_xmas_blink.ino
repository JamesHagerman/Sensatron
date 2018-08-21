#include <SPI.h>
#include <TCL.h>
#include <math.h>

const int LEDS = 600; // 50 LED Total Control Lighting Strand
//const int COLORS = 1; // 5 colors and black.
//const int BLACK = 0; // Define the colors for your code

//byte current_color[LEDS][3]; // This will store the RGB values of various colors

//double angle = 0.0;
//unsigned long phase_time = 0; // between 0 and 5 seconds...

int btn = 9;
int button_state;
int went_low = 0;

int program = 0;


void setup() {
//  int i;
//  unsigned long time;
  
  Serial.begin(9600);
  Serial.println("Hello Computer");
  TCL.begin();
  
  pinMode(btn, INPUT);
  
  // Set up RGB values for each color we have defined above
//  color_values[BLACK][0] = 0x00;
//  color_values[BLACK][1] = 0x00;
//  color_values[BLACK][2] = 0x00;

  // time = millis(); // Find the current time
  // for(i=0;i<=LEDS;i++) {
  //   change_time[i] = time+random(700,1300); // Set up the time each LED should blink on in 700-1300 ms
  //   current_color[i]=BLACK; // Set each LED to black
  // }
  update_strand(); // Update the colors along the strand of LEDs
}

void update_strand() {
  int i;
//  int color;
  
  TCL.sendEmptyFrame();
  for(i=0;i<=LEDS;i++) {
//    color = current_color[i];
    delay(10);
    TCL.sendColor(0x00,0xff,0x00);
  }
  TCL.sendEmptyFrame();
}

void loop() {
//   int i;
////   unsigned long time;
////   time=millis();
////   
////   int ledCut = 10 * (analogRead(TCL_POT1)/1023.0);
////   double blink_speed = analogRead(TCL_POT2)/1023.0; // between 0.0 and 1.0
////   double phaseOffset = 255 * (analogRead(TCL_POT3)/1023.0); // between 0.0 and 60.0
////   double phaseOffset2 = 255 * (analogRead(TCL_POT4)/1023.0); // between 0.0 and 60.0
////   
////   double r, g, b;
//   // Update base phase angle
// 
// 
////   angle += blink_speed * 100.0;
////   if(angle > 360) {
////     angle = angle - 360;
////   }
////   if(angle < 0) {
////     angle = angle + 360; 
////   }
////   //Serial.println(angle);
////  
////   r = ((sin((angle + 120)*(M_PI/180)) + 1)/2.0)*phaseOffset;
////   g = ((sin((angle )*(M_PI/180)) + 1)/2.0)*phaseOffset;
////   b = ((sin((angle + 240)*(M_PI/180)) + 1)/2.0)*phaseOffset;
// //  Serial.print(angle);
// //  Serial.print(", ");
// //  Serial.print(r);
// //  Serial.print(", ");
// //  Serial.print(g);
// //  Serial.print(", ");
// //  Serial.print(b);
// //  Serial.print("\n");
// 
//   color_values[RED][0]=0x00;
//   color_values[RED][1]=0xff;
//   color_values[RED][2]=0x00;  
//   
//   // All led's 
//   for(i=1; i<LEDS; i++) {
// //    if(change_time[i]<time) {
// //      change_time[i]=time+random(1,blink_speed);
// //      if(current_color[i]==BLACK) {
// //        current_color[i]=random(1,RED);
// //      }
// //      else {
// //        current_color[i]=BLACK;
// //      }
// //    }
// //    Serial.print(i);
// //    Serial.print(": ");
// //    Serial.println(i%2);
// 
//     // (i+1) % 10 == 0 || (i+1) % 10 == 1
//     
//     // if(i%10==ledCut || i%10==9-ledCut) { // if i%10
//     //   current_color[i]=RED;
//     // } else {
//     //   current_color[i]=BLACK; 
//     // } 
// 
//     current_color[i]=RED;
//   }
//   
//   update_strand();

  
  buttonRead();
  
}


void buttonRead() {
   // read the state of the switch into a local variable:
  int button_state = digitalRead(btn);
  
  if (button_state == LOW && went_low != 2) {
    went_low = 1;
  }
  
  if (button_state == HIGH) {
    went_low = 0;
  }
  
  if (went_low == 1) {
    Serial.println("Went low");
    
    went_low = 2;
  } 
}
