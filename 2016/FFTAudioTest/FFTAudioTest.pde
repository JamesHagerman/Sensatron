import ddf.minim.*;
import ddf.minim.analysis.*;
import ddf.minim.effects.*;
import ddf.minim.signals.*;
import ddf.minim.spi.*;
import ddf.minim.ugens.*;

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

  minim = new Minim(this);

  // use the getLineIn method of the Minim object to get an AudioInput
  in = minim.getLineIn(Minim.STEREO, bufferSize);

  // create an FFT object that has a time-domain buffer
  // the same size as jingle's sample buffer
  // note that this needs to be a power of two
  // and that it means the size of the spectrum will be half as large.
  fft = new FFT( in.bufferSize(), in.sampleRate() );

  fftLog = new FFT( in.bufferSize(), in.sampleRate() );
  fftLog.logAverages( 22, 3 );
  height2 = height/2;
  rectMode(CORNERS);
}

void draw()
{
  background(0);
  stroke(255);

  // draw the waveforms so we can see what we are monitoring
  for(int i = 0; i < in.bufferSize() - 1; i++)
  {
    line( i, 50 + in.left.get(i)*50, i+1, 50 + in.left.get(i+1)*50 );
    line( i, 150 + in.right.get(i)*50, i+1, 150 + in.right.get(i+1)*50 );
  }

  String monitoringState = in.isMonitoring() ? "enabled" : "disabled";
  text( "Input monitoring is currently " + monitoringState + ".", 5, 15 );

  // perform a forward FFT on the samples in jingle's mix buffer,
  // which contains the mix of both the left and right channels of the file
  fft.forward( in.mix );
  fftLog.forward( in.mix );

  noFill();
  for(int i = 0; i < fft.specSize(); i++)
  {
    // if the mouse is over the spectrum value we're about to draw
    // set the stroke color to red
    int barWidth = (width/(bufferSize/2))/2;
    // if ( mouseX <= i *(width*2)/bufferSize && mouseX > i *(width*2)/bufferSize)
    if ( mouseX == i)
    {
      centerFrequency = fft.indexToFreq(i);
      stroke(255, 0, 0);
    }
    else
    {
        stroke(255);
    }
    // line(i*(width*2)/bufferSize, height2, i*(width*2)/bufferSize, height2 - fft.getBand(i)*spectrumScale);

    // 102 frequiency bands across, basically the range of the piano (up to C7)
    line(i*10, height2, i*10, height2 - fft.getBand(i)*spectrumScale);


    // line(i, height2, i, height2 - fft.getBand(i)*spectrumScale);
  }
  fill(255, 128);
  text("Spectrum Center Frequency: " + centerFrequency, 5, height2 - 25);

  // for(int i = 0; i < fft.specSize(); i++)
  // {
  //   // draw the line for frequency band i, scaling it up a bit so we can see it
  //   line( i*(width*2)/bufferSize, height, i*(width*2)/bufferSize, height - fft.getBand(i)*spectrumScale );
  // }

  // draw the logarithmic averages
  {
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
      if ( mouseX >= xl && mouseX < xr )
      {
        fill(255, 128);
        text("Logarithmic Average Center Frequency: " + centerFrequency, 5, height - 25);
        fill(255, 0, 0);
      }
      else
      {
          fill(255);
      }
      // draw a rectangle for each average, multiply the value by spectrumScale so we can see it better
      rect( xl, height, xr, height - fftLog.getAvg(i)*spectrumScale );
    }
  }

}

void keyPressed()
{
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
