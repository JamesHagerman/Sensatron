class SensatronRoutine {
  PGraphics pg;
  SensatronRoutine() {
    pg = createGraphics(width, height, P3D);
    pg.smooth();
  }
  
  void draw() { }
  
  void updateScreen() {
    image(pg, 0, 0); 
  }
  
  void updateLights() {
    println("Updating lights");
//    image(pg, 0, 0); 
  }
}
