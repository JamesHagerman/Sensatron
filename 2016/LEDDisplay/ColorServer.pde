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
      println("byteCount: " + byteCount);
      if (byteCount > 0) {
        // String myString = new String(colorBytes);
        // println(myString);
        for (int i = 0, j = 0; i < bufferSize; i+=3, j++) {
          print("i: " + i +" j: " + j);
          byte red = colorBytes[i];
          byte green = colorBytes[i+1];
          byte blue = colorBytes[i+2];

          println(" r: "+(int)red+" g: "+(int)green+" b: "+(int)blue);

          color theColor = color((int)red, (int)green, (int)blue);
          p[j] = theColor;
        }
      }

    }
  } // end getColor

} // end class
