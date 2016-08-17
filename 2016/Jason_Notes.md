
# Notes from Jason: Sensatron: sound reactive

## Goal
Connect light Animation to various modifiers such as BPM, pitch, volume, (if time then also accelerometer, mic, x/y color field) and then modulate the sensitivity and/or range of such modifiers for the user to create unique expressions of "sound as light".

- x/y color field
- Tap Tempo (BPM automatically is hard)
- slider for speed as well(?)
- pitch
- volume
- accelerometer
- mic (?)
- camera

James question:  
`What other, perhaps generic, modulators are needed for the animations?`

## Dials/Touch strip/Stripe
The top and bottom values of each dial will need to be determined by testing them, and then when those edge case values can be baked into each dial to define that dial's range.

## Cycling Animation
The lights can show a constant flow of the full spectrum of color, all high value, all high saturation.
The speed of the cycle can be affected manually by a touch area, but it can also be set to be cycling as a relative speed of the BPM of the music that's passing through it.

## Listeners
### Beat Detection
Beats Per Minute affects the speed of the cycling animation of the spectrum flowing through lights, from the base to the tip.
DIAL: affect the speed of the flow with this dial to make the BPM fractional, for example: if a 60 bpm song would result in animations that are cycling 1/second that that is probably too fast for a chill 60bpm song, so the dial can be used to make the animation cycle 1/2 seconds, 1/4 seconds, 1/8 seconds, etc. or slid the other way to make it 2/second, 4/second, 8/second, etc. But these numbers will need to be played with visually to know what the ranges and multipliers should be. There is probably something mathematically elegant, but for now we can use an arithmetic progression.

Tap tempo is WAAAAAAAAAAAAAAAAAAAAAAAAAAAAY easier than automatic beat detection.

### Pitch Detection
The range of colors cycling on the TCL lights can be manually set with the UI to a narrower band all the way down to a single color, or all the way out to the full spectrum of the rainbow, from red to violet. This range can be set by the user or it can instead be affected by the pitch. Silence holds on the last color that the pitch determined, then as more sound is detected the spectrum of light is wider. On a pitch meter, a single tone's pitch is represented as a peak followed by a series of smaller and smaller peaks. I'm suggesting we use those values to display an area of the spectrum by dividing the RGB color space value (which should probably always restricted to a CLUT that results in the lights always being bright) by the Pitch value and then displaying the result.

The range that the pitch is affecting perhaps could be adjusted with the UI, so that the cycle can be forced to a narrower or wider band, greater or lesser sensitivity (e.g. 0% sensitivity = total UI control, 50% sensitivity = a composited mix of UI control with a layer of sound control flowing through it, 100% sensitivity = total sound control). We'd have to play with this to make that determination if it looks cool, and how to affect it.

#### Pitch UI

- Top Range Dial
- Bottom Range Dial
- Sensitivity

#### Pitch Notes

We'd like to adjust both the color bands being displayed (width of spectrum) as well as the width of the audio filter determining pitches (from subwoofer to tweeters and adjust between these ranges).

### Volume
The volume value determines the dynamism, so that lots of consistent sound provides little change in the lights, silence cycles

Spectrum:
From the low frequency to the high frequency, roygbiv. As a note hits, anywhere along that frequency, it dominates its color on the spectrum, so that as the frequencies dance, the lights along the pole shift through colors in that area.

(Accelerometer) (on/off)
Angle of animation (360° rotation)

## Animation notes

### DO NOT LET THE LIGHTS GO DARK!

We have LIGHTS on the car, not DARKS. Make 'em friggin glow and move even if they see silence...

### FFT = Cool! (like bow ties...)

Grabbing spikes from FFT is always possible. Biggest spikes win.

Making each of the spikes a frequency (which they are) of color (which they are not) in a way that means the height of the spike translates to "width" of a given color band. Not sure how many color bands we need but maybe that'd be adjustable. Spectrum width/50 might work... but there are probably about 1024 frequency buckets to pull spikes from...

Having these "fat" areas of color either shift or "occlude" the colors nearby... Are they mushing them away for them to slowly fall back into place? Another parameter to change?

Is this taffy or what!? Experimentation baby!

Oh, and color pallets are a thing to keep in mind.

The best option is for any given pixel to "be related" to the color of any of it's neighbors. Best is neighbors on the same strand/wand/pole but in closer to the hub, neighbors on other wands can be delicious too.



### Pitch

Pitch competitions to allow songs to "be" a color/collection of colors. "Hold period" for any given competition...

Peaks are held longer because we want the VU "peak" to "dance" instead of just peg or fall fast. Watching pitch peaks... and maybe having a velocity..... Peak size could be acceleration? Maybe that could be controlled by attack and decay/ADSR envelope...

Those collection of colors are sliding down the pole at the speed of the tap tempo or whatever speed control we have.

Not sure how this gets integrated with the other FFT but perhaps a mix would work.

#### Pitch issues

Because pitch various ALL OF THE TIME we need a way to allow the aliens to pick and chose their "space" in the frequency band the pitches may be coming from.

This also includes, perhaps, the determination of "what, exactly, IS a peak anyways?"

Bass music has all sorts of issues if the thing is tuned to classic jam rock.


### Color determination

We can use Color Look up tables if we have some source materials...

We can also use shifting palettes.

We can also use HSV RAINBOWS because rainbows are also like bowties (COOL!).

DO NOT USE SIN/COS/TAN a lot. That's a performance hit we might not be able to handle all that much of.
