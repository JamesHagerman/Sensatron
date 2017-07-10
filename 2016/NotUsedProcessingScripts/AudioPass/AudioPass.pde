import ddf.minim.*;
import ddf.minim.analysis.*;

Minim minim;
AudioInput in;
AudioOutput out;
int bufferSize = 2048;

void setup() {
  size(1024, 480, P3D);
  minim = new Minim(this);
  in = minim.getLineIn(Minim.STEREO, bufferSize);
  in.enableMonitoring();

  out = minim.getLineOut(Minim.STEREO, bufferSize);
}

void draw()
{
  background(0);
}
