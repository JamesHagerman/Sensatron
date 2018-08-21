double dRad;
int[][] lights;

class RadialControl {
  
  RadialControl() {
    dRad = (Math.PI*2)/STRANDS;
    lights = new int[STRANDS][STRAND_LENGTH];
  }
  
  int[] mapRadialArrayToLights() {
    int[] toRet = new int[totalPixels];
    int lightIndex = 0;
    for (int strand = 0; strand < STRANDS; strand++) {
      for (int lightNum = 0; lightNum < STRAND_LENGTH; lightNum++) {
        toRet[lightIndex] = lights[strand][lightNum];
        lightIndex++;
      }
    }
    return toRet;
  }
  
  
  
  // These are really more like animations but maybe we can use them as a tool to help us write animations later:
  void randomizeAllLights() {
    for (int strand = 0; strand < STRANDS; strand++) {
      for (int lightNum = 0; lightNum < STRAND_LENGTH; lightNum++) {
        lights[strand][lightNum] = getRandomColor();
      }
    }
  }
  
  void setAllLights(color c) {
    for (int strand = 0; strand < STRANDS; strand++) {
      for (int lightNum = 0; lightNum < STRAND_LENGTH; lightNum++) {
        lights[strand][lightNum] = c;
      }
    }
  }
  
  void setOneLight(int strand, int lightNum, color c) {
    lights[strand][lightNum] = c;
  }
  
  color getRandomColor() {
    return color((int)random(255), (int)random(255), (int)random(255));
  }
}
