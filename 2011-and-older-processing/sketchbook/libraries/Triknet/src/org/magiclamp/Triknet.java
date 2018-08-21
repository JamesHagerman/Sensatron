/*
  you can put a one sentence description of your library here.

  (c) copyright

  This library is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License as published by the Free Software Foundation; either
  version 2.1 of the License, or (at your option) any later version.

  This library is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General
  Public License along with this library; if not, write to the
  Free Software Foundation, Inc., 59 Temple Place, Suite 330,
  Boston, MA  02111-1307  USA
 */

package org.magiclamp;


import processing.core.PApplet;
import processing.net.*;

/**
 * This class negotiates output to the triknet light controller.
 * 
 * @author Mark Lottor
 * @author Jacob Finn
 * 
 */
public class Triknet {

	PApplet myParent;

	// constants
	public final String VERSION = "0.1";
	public final int DEFAULT_OUTPUT_STRINGS = 16;
	public final int DEFAULT_SLOTS = 24;
	public final int OUTPUT_INTERLEAVE = 2;
	public final boolean DEFAULT_FOUR_BIT_OUTPUT = false;
	public final String DEFAULT_CONTROLLER_IP = "10.0.0.200";

	/**
	 * a Constructor, usually called in the setup() method in your sketch to
	 * initialize and start the library.
	 * 
	 * @example Hello
	 * @param theParent
	 * @param controllerIp the IP address of the triknet light controller
	 */
	public Triknet(PApplet theParent, String controllerIp) {
		myParent = theParent;
		this.controllerIp = controllerIp;
	}

	public Triknet(PApplet theParent, String controllerIp, int numStrings, int slotsPerString) {
		myParent = theParent;
		this.controllerIp = controllerIp;
		this.numStrings = numStrings;
		this.slotsPerString = slotsPerString;
	}

	/**
	 * return the version of the library.
	 * 
	 * @return String
	 */
	public String version() {
		return VERSION;
	}

	void p_ftx()
	{
		int i;

		for (i = 0; i < 32; i++)
			PApplet.println("ftx[" + i + "]= " + PApplet.hex(ftx[i]));
	}

	/* generate frame(s) using current rgb array values */
	void frame_gen(int v[][])
	{
		int b, d, i, slot, string, bit, ebit, s, xi;
		int w;

		/* output the frame header bits (0010) */
		fc = 0;
		for (b = 0; b < OUTPUT_INTERLEAVE; b++)
			frame[fc++] = 0;
		for (b = 0; b < OUTPUT_INTERLEAVE; b++)
			frame[fc++] = 0;
		for (b = 0; b < OUTPUT_INTERLEAVE; b++)
			frame[fc++] = (byte) 0xff;
		for (b = 0; b < OUTPUT_INTERLEAVE; b++)
			frame[fc++] = 0;

		for (slot = 0; slot < slotsPerString; slot++)  /* for each light on the string */
		{
			xi = 0;
			for (string = 0; string < numStrings; string++)  /* for each string */
			{
				w = v[string][slot];  /* get rgb value for string/slot location via map */
				ftx[xi] = (w << 8);          /* add to next transposition array slot */
				xi++;
			}

			frame_transpose();

			/* now ouput the bits for this light */
			for (bit = 7; bit >= 0; bit--)
			{
				xi = 0;
				for (b = 0; b < OUTPUT_INTERLEAVE; b++)
				{
					/* R */
					if (xi == 0)
						frame[fc++] =  (byte) ((ftx[bit] >> 24) & 0xff);
					if (xi == 1)
						frame[fc++] = (byte) ((ftx[bit] & 0x00ff0000) >>> 16);
					if (xi == 2)
						frame[fc++] = (byte) ((ftx[bit] & 0x0000ff00) >>> 8);
					if (xi == 3)
						frame[fc++] = (byte) ((ftx[bit] & 0x000000ff));
					xi++;
				}
				xi = 0;
				for (b = 0; b < OUTPUT_INTERLEAVE; b++)
				{
					/* G */
					if (xi == 0)
						frame[fc++] =  (byte) ((ftx[bit+8] >> 24) & 0xff);
					if (xi == 1)
						frame[fc++] = (byte) ((ftx[bit+8] & 0x00ff0000) >>> 16);
					if (xi == 2)
						frame[fc++] = (byte) ((ftx[bit+8] & 0x0000ff00) >>> 8);
					if (xi == 3)
						frame[fc++] = (byte) ((ftx[bit+8] & 0x000000ff));
					xi++;
				}
				xi = 0;
				for (b = 0; b < OUTPUT_INTERLEAVE; b++)
				{
					/* B */
					if (xi == 0)
						frame[fc++] =  (byte) ((ftx[bit+16] >> 24) & 0xff);
					if (xi == 1)
						frame[fc++] = (byte) ((ftx[bit+16] & 0x00ff0000) >>> 16);
					if (xi == 2)
						frame[fc++] = (byte) ((ftx[bit+16] & 0x0000ff00) >>> 8);
					if (xi == 3)
						frame[fc++] = (byte) ((ftx[bit+16] & 0x000000ff));
					xi++;
					if (xi > 3) { xi = 0; }
				}
				xi = 0;
				for (b = 0; b < OUTPUT_INTERLEAVE; b++)
				{
					/* P */
					if (xi == 0)
						frame[fc++] =  (byte) ~((ftx[bit+16] >> 24) & 0xff);
					if (xi == 1)
						frame[fc++] = (byte) ~((ftx[bit+16] & 0x00ff0000) >>> 16);
					if (xi == 2)
						frame[fc++] = (byte) ~((ftx[bit+16] & 0x0000ff00) >>> 8);
					if (xi == 3)
						frame[fc++] = (byte) ~((ftx[bit+16] & 0x000000ff));
					xi++;
				}
			}
		}
		/* output the frame trailer bits (0000) */
		for (b = 0; b < OUTPUT_INTERLEAVE; b++)
			frame[fc++] = 0;
		for (b = 0; b < OUTPUT_INTERLEAVE; b++)
			frame[fc++] = 0;
		for (b = 0; b < OUTPUT_INTERLEAVE; b++)
			frame[fc++] = 0;
		for (b = 0; b < OUTPUT_INTERLEAVE; b++)
			frame[fc++] = 0;

		if (fourBitOutput)
			pack_frames();
	}

	// for all the output data in a frame buffer, pack 4-bit nibbles into bytes
	// like so:  byte1, byte2  =>  byte1-high-4-bits | (byte2-high-4-bits >> 4)
	// then adjust frame counts
	// note: framecount assumed to be even.
	void pack_frames()
	{
		int i, d;
		byte p;

		for (i = 0; i < fc; i += 2)
		{
			p = (byte) ((frame[i] & 0xf0) |  ((frame[i+1] >>> 4) & 0x0f));
			frame[i/2] = p;
		}
		fc = fc / 2;
	}

	//  bit transpose a 32x32 bit array
	void frame_transpose()
	{
		int t;
		int i;

		for (i = 0; i < 16; i++)
		{
			t         = (ftx[i] << 16) & 0xffff0000;
			ftx[i]    = (ftx[i]    & 0xffff0000) | ((ftx[i+16] >> 16) & 0x0000ffff);
			ftx[i+16] = (ftx[i+16] & 0x0000ffff) | t; 
		}


		for (i = 0; i < 8; i++)
		{
			t         = (ftx[i] << 8) & 0xff00ff00;
			ftx[i]    = (ftx[i]    & 0xff00ff00) | ((ftx[i+8] >> 8) & 0x00ff00ff);
			ftx[i+8]  = (ftx[i+8]  & 0x00ff00ff) | t;

			t         = (ftx[i+16] << 8) & 0xff00ff00;
			ftx[i+16] = (ftx[i+16] & 0xff00ff00) | ((ftx[i+24] >> 8) & 0x00ff00ff);
			ftx[i+24] = (ftx[i+24] & 0x00ff00ff) | t;
		}


		for (i = 0; i < 4; i++)
		{
			t         = (ftx[i]  << 4)  & 0xf0f0f0f0;
			ftx[i]    = (ftx[i]    & 0xf0f0f0f0) | ((ftx[i+4] >> 4) & 0x0f0f0f0f);
			ftx[i+4]  = (ftx[i+4]  & 0x0f0f0f0f) | t;

			t         = (ftx[i+8]  << 4) & 0xf0f0f0f0;
			ftx[i+8]  = (ftx[i+8]  & 0xf0f0f0f0) | ((ftx[i+12] >> 4) & 0x0f0f0f0f);
			ftx[i+12] = (ftx[i+12] & 0x0f0f0f0f) | t;

			t         = (ftx[i+16] << 4) & 0xf0f0f0f0;
			ftx[i+16] = (ftx[i+16] & 0xf0f0f0f0) | ((ftx[i+20] >> 4) & 0x0f0f0f0f);
			ftx[i+20] = (ftx[i+20] & 0x0f0f0f0f) | t;

			t         = (ftx[i+24] << 4) & 0xf0f0f0f0;
			ftx[i+24] = (ftx[i+24] & 0xf0f0f0f0) | ((ftx[i+28] >> 4) & 0x0f0f0f0f);
			ftx[i+28] = (ftx[i+28] & 0x0f0f0f0f) | t;
		}


		for (i = 0; i < 30; i += 4)
		{
			t         = (ftx[i] << 2) & 0xcccccccc;
			ftx[i]    = (ftx[i]    & 0xcccccccc) | ((ftx[i+2] >> 2) & 0x33333333);
			ftx[i+2]  = (ftx[i+2]  & 0x33333333) | t;

			t         = (ftx[i+1] << 2) & 0xcccccccc;
			ftx[i+1]  = (ftx[i+1]  & 0xcccccccc) | ((ftx[i+3] >> 2) & 0x33333333);
			ftx[i+3]  = (ftx[i+3]  & 0x33333333) | t;
		}

		for (i = 0; i < 32; i += 2)
		{
			t         = (ftx[i] << 1) & 0xaaaaaaaa;
			ftx[i]    = (ftx[i]    & 0xaaaaaaaa) | ((ftx[i+1] >> 1) & 0x55555555);
			ftx[i+1]  = (ftx[i+1]  & 0x55555555) | t;  
		}
	}

	public void TL_open()
	{
		PApplet.println("Connecting to " + controllerIp + "...");
		triknet = new Client(myParent, controllerIp, 1000);
		PApplet.println("  OK");
	}

	public void TL_close()
	{
		triknet.stop();
	}

	// output array of rgb values to controller, display for msecs long
	public int TL_out(int v[][], int msecs)
	{
		int fcount, f;

		if (msecs < 1) msecs = 1;
		fcount = msecs/19;
		if (fcount < 1) fcount = 1;
		frame_gen(v);

//		PApplet.println("gen " + fcount + " frames, len " + fc);
		for (f = 0; f < fcount; f++)
			frame_tx();
		return(fcount*19);
	}

	public void print_frame()
	{
		int i;

		for (i = 0; i < fc; i++)
			PApplet.println(PApplet.hex(frame[i]));
	}

	void frame_tx()
	{
		int i;

		if (fourBitOutput)
		{
			for (i = 0; i < fc; i++) hframe[i] = frame[i];
			triknet.write(hframe);
		}
		else
		{
			triknet.write(frame);
		}
	}


	void test()
	{
		int t, i, j, x, y;

		myParent.colorMode(PApplet.RGB, 255);
		TL_open();

		for (t = 0; t < 300; t++) // 1 minute
		{
			for (j = 0; j < 24; j++)  // for each light
			{
				v[0][j] = myParent.color(255,0,0);
				v[1][j] = myParent.color(0,255,0);
				v[2][j] = myParent.color(0,0,255);
				v[3][j] = myParent.color(255,0,255);
				TL_out(v,200);        // output frame for .2 seconds
				v[0][j] = myParent.color(0,0,0);
				v[1][j] = myParent.color(0,0,0);
				v[2][j] = myParent.color(0,0,0);
				v[3][j] = myParent.color(0,0,0);
			}
		}

		TL_close();
		PApplet.println("done");
	}



	Client triknet;

	String controllerIp = DEFAULT_CONTROLLER_IP;
	boolean fourBitOutput = DEFAULT_FOUR_BIT_OUTPUT;     // set for USB interface
	int numStrings = DEFAULT_OUTPUT_STRINGS;
	int slotsPerString = DEFAULT_SLOTS;

	// array v holds color values for up to 32 strings of 24 lights
	int v[][] = new int[32][24];

	// used for bit frame_transpose function
	int ftx[] = new int[32];

	// used for output frame
	int fc;
	byte frame[] = new byte[776*OUTPUT_INTERLEAVE];
	byte hframe[] = new byte[388];

}
