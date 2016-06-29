// Camera setup:
import processing.video.*;

Capture cam;

class CameraInput {
  CameraInput(PApplet parent) {
    try {
  //    cam = new Capture(parent, 400, 300, "Logitech Camera");
      cam = new Capture(parent, 320, 180, "FaceTime HD Camera (Built-in)"); // retina machine
  //    cam = new Capture(parent, 320, 180, "Built-in iSight"); // original 13"
    } 
    catch (Exception e) {
      println("Something's wrong with the camera settings:");
      String[] cameras = Capture.list();
      if (cameras.length == 0) {
        println("There are no cameras available for capture.");
        exit();
      } else {
        println("Available cameras:");
        for (int i = 0; i < cameras.length; i++) {
          println(cameras[i]);
        }   
      }
    }
  
    cam.start();
  }
  
  void drawCameraData() {
    cam.loadPixels();
    image(cam, 0, 0); 
  }
  
}

void captureEvent(Capture c) {
  c.read();
}
