void setup() {
  prepareExitHandler();
  println("setup");
}

private void prepareExitHandler () {
  Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
    public void run () {
      System.out.println("SHUTDOWN HOOK");      // application exit code here
      println("stop");
    }
  }));
}

