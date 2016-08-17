import ddf.minim.*;
import ddf.minim.analysis.*;

Minim minim;
AudioInput in;
FFT fft;
FFT fftLog;
int bufferSize = 2048;

float centerFrequency = 0;
float height2;
float spectrumScale = 2;

void setup() {
  size(1024, 480, P3D);
  height2 = height/2;
  rectMode(CORNERS);


  minim = new Minim(this);

  in = minim.getLineIn(Minim.STEREO, bufferSize);
  in.enableMonitoring();

  fft = new FFT( in.bufferSize(), in.sampleRate() );
  fftLog = new FFT( in.bufferSize(), in.sampleRate() );
  fftLog.logAverages( 22, 3 );

}

void draw()
{
  background(0);
  stroke(255);

  fft.forward( in.mix );
  fftLog.forward( in.mix );

  for(int i = 0; i < fft.specSize(); i++)
  {
    if ( mouseX == i) {
      centerFrequency = fft.indexToFreq(i);
      stroke(255, 0, 0);
    } else {
        stroke(255);
    }

    // 102 frequiency bands across, basically the range of the piano (up to C7)
    // line(i*10, height2, i*10, height2 - fft.getBand(i)*spectrumScale);
    line(i, height2, i, height2 - fft.getBand(i)*spectrumScale);
  }


  // since logarithmically spaced averages are not equally spaced
  // we can't precompute the width for all averages
  for(int i = 0; i < fftLog.avgSize(); i++)
  {
    centerFrequency    = fftLog.getAverageCenterFrequency(i);
    // how wide is this average in Hz?
    float averageWidth = fftLog.getAverageBandWidth(i);

    // we calculate the lowest and highest frequencies
    // contained in this average using the center frequency
    // and bandwidth of this average.
    float lowFreq  = centerFrequency - averageWidth/2;
    float highFreq = centerFrequency + averageWidth/2;

    // freqToIndex converts a frequency in Hz to a spectrum band index
    // that can be passed to getBand. in this case, we simply use the
    // index as coordinates for the rectangle we draw to represent
    // the average.
    int xl = (int)fftLog.freqToIndex(lowFreq);
    int xr = (int)fftLog.freqToIndex(highFreq);

    // if the mouse is inside of this average's rectangle
    // print the center frequency and set the fill color to red
    if ( mouseX >= xl && mouseX < xr ) {
      fill(255, 0, 0);
    } else {
      fill(255);
    }
    // draw a rectangle for each average, multiply the value by spectrumScale so we can see it better
    rect( xl, height, xr, height - fftLog.getAvg(i)*spectrumScale );
  }

}
