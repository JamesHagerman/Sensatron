import processing.net.*;

class ColorServer {
  Server rawColorServer;
  int port = 3001;
  ColorServer () {
  } // end ColorServer

  void init(PApplet parent) {
    // Start the Raw Color Server:
    rawColorServer = new Server(parent, port);
  } // end init

  void getColors() {
    Client thisClient = rawColorServer.available();
    // If the client is not null, and says something, display what it said
    if (thisClient !=null) {
      String whatClientSaid = thisClient.readString();
      if (whatClientSaid != null) {
        println(trim(whatClientSaid));
      }
    }
  } // end getColor

} // end class
