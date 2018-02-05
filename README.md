# Pokemon Go: Dartmouth Cats Edition

Zack Johnson, Brian Tomasco

Our project's goal was to create similar functionality to Pokemon Go from scratch, instead using cats placed around Dartmouth College's campus. 

Via our Android app, users can create a profile and sign in. They are brought to a tabbed layout where they can either play the game, view and change settings, or view a list of the cats they need to find. Upon entering the game, users interact with a Google Maps object and physically move to the locations of the cats guided by a tracking service. When they find the cat, they can choose to pet the cat, which brings them to an overlay of the cat image onto the camera. Upon clicking the cat, users are brought to a success page and the list of cats is updated offscreen.

Behind the scenes, we use JSON requests to handle sign in and profile creation. We used the Google Maps API to form the base of our play tab. The camera overlay was integrated using a library developed by our TA, Varun Mishra. 

## Overview of files:

To view the code files, click on app -> src -> main. From there, java files are located in the java/com/... folder and layout files are located in the res folder. Layout files are written in xml. The functions of most files should be self-explanatory. Feel free to contact me at zacharyjohnsonri@gmail.com if there are any questions. Those that require additional explanation: 

 * `ForegroundService.java` creates a service that runs separate from the app in the notification bar that tracks a specified cat and the user's distance from it.
 * `HistoryLayoutHelper`  creates a container with which Brian and I can control the layout in the History (list of cats) tab. Allows updating from outside layout file and for more succinct files.
 * `Game.java` contains the tab that houses the play button where users can enter the game. When the users click on that button, they are directed to Play.java. 
 * `StopServiceReceiver.java` controls behavior to stop the tracking service.
 * `TabViewPageAdapter.java` helps TabLayout.java in the creation of the base Tabbed Layout.

## User Usage:

To run the game, download the APK contained within build/outputs/apk/debug.