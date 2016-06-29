class ACircle extends SensatronRoutine {
//  int w = 0;
//  int h = 0;
//  
//  void reinit() {
//    w = pg.width;
//    h = pg.height;
//  }
  
  int diameter;
  ACircle() {
     diameter = 5;
  }
  
  ACircle(int circleSize) {
     diameter = circleSize;
  }
  
  void draw() {
    pg.beginDraw();
    pg.background(255,0,255);
    pg.fill(255);
    pg.ellipse(mouseX,mouseY, diameter, diameter);
    pg.endDraw();
  }
}
