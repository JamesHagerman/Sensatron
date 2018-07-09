# Sensatron Server and LEDDisplay run notes

This document explains how to run the `sensatron-server` and `LEDDisplay` on Windows 10.

Get the code by downloading the repo from here:

(https://github.com/JamesHagerman/Sensatron)[https://github.com/JamesHagerman/Sensatron]

## Software I'm using to run this:

This is the software I'm using. If you don't like it, you're on your own:

- Git client: Any should work. (GitHub Desktop)[https://desktop.github.com/], or the git client built into the editors, or the official command line (client)[https://git-scm.com/downloads])

- JDK 8: (http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)[http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html]

- For `LEDDisplay`:
    - Processing
        - To run the LEDDisplay
        - (https://processing.org/download/)[https://processing.org/download/] 

- For the `sensatron-server` itself:
    - IntelliJ
        - I like IntelliJ; it comes with Maven and git tools.
        - I have saved my project settings to this repo so you can just open the project directly in IntelliJ!
        - Do this instead of Eclipse... 
        - (https://www.jetbrains.com/idea/download/)[https://www.jetbrains.com/idea/download/]
    - OR
    - Eclipse
        - I have no idea how this works! Ask Jacob!
        - You'll probably need Maven on it's own:
            - Maven binary disribution: (https://maven.apache.org/download.cgi)[https://maven.apache.org/download.cgi]

## Starting with Processing...

For development, the `LEDDisplay` lets you see what the Sensatron lights should look like (from above the car, btw). It runs in Processing.

- Install Processing by downloading the zip file and dragging the `processing-3.3.6` directory to something like `C:\Program Files`
- Then double click `processing.exe` inside that directory

*Hint: right click the `processing.exe` and select `Pin to Start`. That'll let you start it more easily.*

- Once Processing starts (there will be a nag screen at some point, just close it), select `File` -> `Open...`
- Navigate to the `LEDDisplay` directory, and select the file `LEDDisplay.pde`
- Click `Open`
- Click the play button, and the `LEDDisplay` screen should pop up. If windows asks for network access, allow it!

Woo! Now you should see black LED pixels on the car. We need something to send it colors. That's what the `sensatron-server` does...

## Then moving on to IntelliJ and the JDK...

The easy way to run `sensatron-server` is running it via the IntelliJ IDE. Just open the project in IntelliJ and click the run button.

That involves installing IntelliJ, and installing the Java Development Kit.

### Install IntelliJ
- Download the community version installer and run it
- Pick all of the defaults. (maybe pick the Darcula theme if you want lol)

After it's installed, select "Open" and chose the `sensatron-server` directory from this repo.

*Note: I'd suggest using the `View` menu to enable the "Toolbar" and "Tool buttons" UI...*

Once the project is opened, you should be able to run the `sensatron-server` by either:
- Hitting the green play button
- OR
- using `Run` menu -> `Run 'sensatron-server'`

If you see output that looks like this, you can navigate to (http://127.0.0.1:8080/)[http://127.0.0.1:8080/] and see the interface!

```
[INFO] Started SelectChannelConnector@0.0.0.0:8080
[INFO] Started Jetty Server
[INFO] Starting scanner at interval of 30 seconds.
```

If, however, you get an error about no JDK being defined, you'll need to install the JDK 8 and configure the `Project Structure` to use that JDK.

#### Install JDK 8

You may already have a version of the JDK. Java is a pain that way. 

- Go download the Java SE Development Kit 8 (JDK) installer from here. It should be something like `jdk-8u162-windows-x64.exe`:
(http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)[http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html]
- Run the installer and *REMEMBER WHERE IT INSTALLS THINGS!* By default, this is: `C:\Program File\Java\jre1.8.0_162`
    - Note that we will actually be using the JDK, not the JRE. Not sure why the installer even bothers telling us the jre path. whatever.

#### Then configure IntelliJ Project Structure with that JDK...

Now that you've got a JDK installed, go back to IntelliJ and configure it to use that JDK:

- `File` menu -> `Project Structure...` -> `Project` pane
- Click the `New...` button next to `<No SDK>` and select `JDK`
- In the browser that comes up navigate to the JDK path: `C:\Program File\Java\jdk1.8.0_162`
- Click `OK` in the file browser window, and the Project pane should show something like `1.8 (java version "1.8.0_162")`

Click `OK

THEN Try running the application again and hope for the best

## Trouble shooting

Oh boy. Just ask James or Jacob in the gitter for help...
