import processing.net.*;

class ColorServer {
  Server rawColorServer;
  int port = 3001;
  int bufferSize = 600*3;
  ColorServer () {
  } // end ColorServer

  void init(PApplet parent) {
    // Start the Raw Color Server:
    rawColorServer = new Server(parent, port);
  } // end init

  void getColors(int[] p) {
    Client thisClient = rawColorServer.available();
    // If the client is not null, and says something, display what it said
    if (thisClient !=null) {
      byte[] colorBytes = new byte[bufferSize];
      int byteCount = thisClient.readBytes(colorBytes);
      // println("byteCount: " + byteCount + " led count: " + byteCount/3);
      if (byteCount == 1800) {
        int j = 0;
        int red = 0, green = 0, blue = 0;
        for (int i = 0; i < bufferSize; i+=3) {
          // print("Index: " + i/3 + " i: " + i );
          red = colorBytes[i] & 0xff;
          green = colorBytes[i+1] & 0xff;
          blue = colorBytes[i+2] & 0xff;

          // println(" r: "+ hex(red)+" g: "+hex(green)+" b: "+hex(blue));
          color theColor = color((int)red, (int)green, (int)blue);
          p[j] = theColor;
          j++;
        }
        // println(" r: "+(int)red+" g: "+(int)green+" b: "+(int)blue);
      }

    }
  } // end getColor

} // end class
