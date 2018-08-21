class SensatronRoutine {
  PGraphics pg;
  SensatronRoutine() {
    pg = createGraphics(width, height, P3D);
  }
  
  void draw() { }
  
  void update() {
    image(pg, 0, 0); 
  }
}
