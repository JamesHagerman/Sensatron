import processing.net.*; 
Client myClient;
String colors = "";
int led_count = 50;
float angle = 0;
color the_color;

void setup() {
  myClient = new Client(this, "192.168.25.169", 3333); 
  
}

void draw() {
  colorMode(HSB, 1.0);
  colors = "";
  angle += 1;
  if (angle > 360.0) {
    angle = 0;
  }
  
  the_color = color(angle/360.0, 1.0, 1.0);
  String color_string = hex(the_color, 6);
  
  for (int i = 0; i < led_count; i += 1) {
   colors = colors + color_string + " ";
  } 
 
  //print("angle:" + angle);
  //println(" color: " + color_string);
  background(the_color); 
  myClient.write(colors);
  delay(1); 
}

