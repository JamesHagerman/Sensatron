class AnimationRoutine {
  PGraphics pg;
  AnimationRoutine() {
    pg = createGraphics(width, height, P3D);
    pg.smooth();
  }
  
  void draw() { }

  void draw(int xIn, int yIn) {}
  
  // All this does is draw the current frame to the master document. It could be done outside of the class:
  void updateScreen() {
    image(pg, 0, 0); 
  }
  
  void updateLights() {
    println("Updating lights");
//    image(pg, 0, 0); 
  }
}
