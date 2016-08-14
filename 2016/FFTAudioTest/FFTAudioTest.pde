import ddf.minim.*;
import ddf.minim.analysis.*;
import ddf.minim.effects.*;
import ddf.minim.signals.*;
import ddf.minim.spi.*;
import ddf.minim.ugens.*;

Minim minim;
AudioInput in;

FFT fft;

BeatDetect beat;
BeatListener bl;
float kickSize, snareSize, hatSize;
// The lowest frequency that will be displayed
int minOctave = 44;
// Number of parts to split each octave into (higher numbers display a smaller range overall)
int bandsPerOctave = 1;

class BeatListener implements AudioListener
{
  private BeatDetect beat;
  private AudioInput source;

  BeatListener(BeatDetect beat, AudioInput source)
  {
    this.source = source;
    this.source.addListener(this);
    this.beat = beat;
  }

  void samples(float[] samps)
  {
    beat.detect(source.mix);
  }

  void samples(float[] sampsL, float[] sampsR)
  {
    beat.detect(source.mix);
  }
}

void setup() {
  size(512, 200, P3D);

  minim = new Minim(this);

  // use the getLineIn method of the Minim object to get an AudioInput
  in = minim.getLineIn();

  // FFT SPECTROGRAM
  // create an FFT object that has a time-domain buffer
  // the same size as jingle's sample buffer
  // note that this needs to be a power of two
  // and that it means the size of the spectrum will be half as large.
  fft = new FFT( in.bufferSize(), in.sampleRate() );
  fft.logAverages(minOctave, bandsPerOctave*2);

  // BEAT DETECTION:
  // a beat detection object song FREQ_ENERGY mode:
  beat = new BeatDetect(in.bufferSize(), in.sampleRate());
  // set the sensitivity to 300 milliseconds
  // After a beat has been detected, the algorithm will wait for 300 milliseconds
  // before allowing another beat to be reported. You can use this to dampen the
  // algorithm if it is giving too many false-positives. The default value is 10,
  // which is essentially no damping. If you try to set the sensitivity to a negative value,
  // an error will be reported and it will be set to 10 instead.
  // note that what sensitivity you choose will depend a lot on what kind of audio
  // you are analyzing. in this example, we use the same BeatDetect object for
  // detecting kick, snare, and hat, but that this sensitivity is not especially great
  // for detecting snare reliably (though it's also possible that the range of frequencies
  // used by the isSnare method are not appropriate for the song).
  beat.setSensitivity(300);
  beat.detectMode(BeatDetect.FREQ_ENERGY);
  kickSize = snareSize = hatSize = 16;
  // make a new beat listener, so that we won't miss any buffers for the analysis
  bl = new BeatListener(beat, in);

  // Some drawing params:
  textFont(createFont("Helvetica", 16));
}

void draw()
{
  background(0);
  stroke(255);

  // Draw time domain/Waveforms:
  // draw the waveforms so we can see what we are monitoring
  for(int i = 0; i < in.bufferSize() - 1; i++)
  {
    line( i, 50 + in.left.get(i)*50, i+1, 50 + in.left.get(i+1)*50 );
    line( i, 150 + in.right.get(i)*50, i+1, 150 + in.right.get(i+1)*50 );
  }

  // Draw debug text:
  String monitoringState = in.isMonitoring() ? "enabled" : "disabled";
  text( "Input monitoring is currently " + monitoringState + ".", 5, 15 );

  // Draw frequency domain/FFT Spectrum/Spectrogram
  // perform a forward FFT on the samples in jingle's mix buffer,
  // which contains the mix of both the left and right channels of the file
  fft.forward( in.mix );
  for(int i = 0; i < fft.specSize(); i++)
  {
    // draw the line for frequency band i, scaling it up a bit so we can see it
    line( i, height, i, height - fft.getBand(i)/* *8*/ );
  }

  // Draw beat detection:
  // draw a green rectangle for every detect band
  // that had an onset this frame
  float rectW = width / beat.detectSize();
  for(int i = 0; i < beat.detectSize(); ++i)
  {
    // test one frequency band for an onset
    if ( beat.isOnset(i) )
    {
      fill(0,200,0);
      rect( i*rectW, 0, rectW, height);
    }
  }
  // draw an orange rectangle over the bands in
  // the range we are querying
  int lowBand = 10;
  int highBand = beat.detectSize()-1;
  // at least this many bands must have an onset
  // for isRange to return true
  int numberOfOnsetsThreshold = 4;
  if ( beat.isRange(lowBand, highBand, numberOfOnsetsThreshold) )
  {
    fill(232,179,2,200);
    rect(rectW*lowBand, 0, (highBand-lowBand)*rectW, height);
  }
  if ( beat.isKick() ) kickSize = 32;
  if ( beat.isSnare() ) snareSize = 32;
  if ( beat.isHat() ) hatSize = 32;
  fill(255);
  textAlign(CENTER);
  textSize(kickSize);
  text("KICK", width/4, height/2);
  textSize(snareSize);
  text("SNARE", width/2, height/2);
  textSize(hatSize);
  text("HAT", 3*width/4, height/2);
  textAlign(LEFT);
  textSize(16);
  kickSize = constrain(kickSize * 0.95, 16, 32);
  snareSize = constrain(snareSize * 0.95, 16, 32);
  hatSize = constrain(hatSize * 0.95, 16, 32);
  // end Beat detection


}

void keyPressed()
{
  // Enable audio passthrough from in to out (Monitoring mode)
  if ( key == 'm' || key == 'M' )
  {
    if ( in.isMonitoring() )
    {
      in.disableMonitoring();
    }
    else
    {
      in.enableMonitoring();
    }
  }
}
