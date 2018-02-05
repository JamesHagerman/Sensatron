#!/usr/bin/env python3

import time
import requests
import json
import base64

# Animation/color tooling:
import colorsys
import os     # for os.random() that does random bytes
import random # for random.random() that does random float ranged 0-1

sensatronHost = '192.168.1.210'
sensatronPort = '8080'

def main():
    
    print 'Running the Pythonic Sensatron Rainbow Generator...'

    # drawAllBlue()
    # drawAllWhite()
    # drawRandomGrays()
    # drawRandomRainbow()

    runGameLoop()

def runGameLoop():
    # Build a "game loop" to fire off a chunk of frames to the sensatron...
    currentFrame = 0
    maxAnimationTime = 2 # seconds
    fps = 30.
    maxFrames = fps*maxAnimationTime
    print 'We will animate for {} seconds, at {} fps, thus, send {} frames to the sensatron'.format(maxAnimationTime, fps, maxFrames)

    while 1:
    # while currentFrame < maxFrames:
        currentFrame += 1 # Keep track of our frames

        # Draw some stuff:
        # drawRandomGrays()
        drawRandomRainbow()

        # TODO: We should really only call sendColorData() once per game loop...

        time.sleep(1./fps) # Timestep our game loop

def drawRandomRainbow():
    byteString = ""
    for x in range(0, 600):
        # Random Hue:
        randomHue = random.random() # between 0 and 1
        saturation = 1.
        value = 1.
        r,g,b = colorsys.hsv_to_rgb(randomHue, saturation, value)
        # print '{},{},{}'.format(r, g, b)
        roundedR = int(round(r*255))
        roundedG = int(round(g*255))
        roundedB = int(round(b*255))
        # print '{},{},{}'.format(roundedR,roundedG,roundedB)
        byteString += chr(roundedR)
        byteString += chr(roundedG)
        byteString += chr(roundedB)
    # print(byteString)

    sendColorData(byteString)
    
def drawRandomGrays():
    # Random grays:
    byteString = ""
    for x in range(0, 600):
        # Random RGB bytes:
        pixelColor = os.urandom(3)
        byteString += pixelColor
    # print(byteString)

    sendColorData(byteString)

def drawAllBlue():
    byteString = ""
    for x in range(0, 600):
        byteString += chr(0x00)
        byteString += chr(0x00)
        byteString += chr(0xff)
    sendColorData(byteString)

def drawAllWhite():
    # All LEDs set white:
    colorBytes = [0xff]*3*600
    byteString = "".join(map(chr, colorBytes))
    sendColorData(byteString)

def sendColorData(byteString):
    encodedColors = base64.b64encode(byteString)
    data = {'NumLightProbeRadials': 12, 'NumLightProbeLightsPerRadial': 50, 'LightProbeData': encodedColors}
    r = requests.put('http://'+sensatronHost+':'+sensatronPort+'/rs/lightData',data=json.dumps(data))


# Do this last so we can define anything anywhere:
if __name__ == '__main__':
    main()