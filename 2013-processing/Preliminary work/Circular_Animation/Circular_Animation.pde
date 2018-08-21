// Learning Processing
// Daniel Shiffman
// http://www.learningprocessing.com

// Example 9-8: A snake following the mouse

// Declare two arrays with 50 elements.
int[] xpos = new int[100]; 
int[] ypos = new int[100];

PGraphics circleAnimation;

void setup() {
  size(800,600, P3D);
  frameRate(60);
  smooth();
  
  // Initialize all elements of each array to zero.
  circleAnimation = createGraphics(width,height,P3D);
  circleAnimation.smooth();
  for (int i = 0; i < xpos.length; i ++ ) {
    xpos[i] = 0; 
    ypos[i] = 0;
  }
}

void draw() {
  updateCircles();
  image(circleAnimation, 0, 0);
}

void updateCircles() {
  circleAnimation.beginDraw();
  circleAnimation.colorMode(HSB, 255);
  circleAnimation.background(mouseY, 255, 255);
  
  // Shift array values
  for (int i = 0; i < xpos.length-1; i ++ ) {
    // Shift all elements down one spot. 
    // xpos[0] = xpos[1], xpos[1] = xpos = [2], and so on. Stop at the second to last element.
    xpos[i] = xpos[i+1];
    ypos[i] = ypos[i+1];
  }
//  delay(mouseY);
  
  // New location
  xpos[xpos.length-1] = mouseX; // Update the last spot in the array with the mouse location.
  ypos[ypos.length-1] = 0;
  
  // Draw everything
  circleAnimation.pushMatrix();
//  rotateX(radians(-45));
//  rotateY(radians(45));
  circleAnimation.translate(width/2,height/2); // for P3D renderer
//  translate(width/2,height/2);
  
//  for (int i = 0; i < xpos.length; i ++ ) {
//     // Draw an ellipse for each element in the arrays. 
//     // Color and size are tied to the loop's counter: i.
//    
////    rotateZ(radians(i*90)); // for P3D renderer
//    rotate(radians(30));
//    noStroke();
////    translate(0,0,-10);
//    fill(255-i*2, 0+i*2.5, 255);
//    ellipse(xpos[i],ypos[i],i,i);
//  }
  for (int i = xpos.length -1; i > 0 ; i-- ) {
     // Draw an ellipse for each element in the arrays. 
     // Color and size are tied to the loop's counter: i.
    
//    rotateZ(radians(i*90)); // for P3D renderer
    circleAnimation.rotate(radians(30));
//    noStroke();
//    translate(0,0,-10);
    
    circleAnimation.fill(255-i*2, 255, 255, mouseY); // HSB colors
    
//    fill(255-i*2, 0+i*2.5, 255-i*2, mouseY); // rgb colors
    circleAnimation.ellipse(xpos[i],ypos[i],i,i);
  }
  circleAnimation.popMatrix();
  circleAnimation.endDraw();
}
