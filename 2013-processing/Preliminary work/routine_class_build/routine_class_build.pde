/*
  Sensatron 2013 Lighting Rig Rebuild
  by James Hagerman
  on August 18,2013 at 1:13 am
 */

ArrayList<SensatronRoutine> allAnimations; // Place to hold all known animations
ACircle aCircle; // A single circle controlled by the mouse
CircleAnimation originalCircles;

void setup() {
  size(500,500, P3D);
  frameRate(60);
  
//  aCircle = new ACircle(100);
  originalCircles = new CircleAnimation();
  
}

void draw() {
//  aCircle.draw();
//  aCircle.updateScreen();
  
  originalCircles.draw();
  originalCircles.updateScreen();
}
