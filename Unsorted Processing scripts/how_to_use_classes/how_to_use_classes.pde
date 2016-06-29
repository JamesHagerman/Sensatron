HLine h1 = new HLine(20, 2.0); 
HLine h2 = new HLine(50, 2.5);

void setup() {
  size(200, 200);
  frameRate(30);
}

void draw() {
  background(204);
  h1.update(); 
  h2.update();
}
