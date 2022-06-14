# cam-watcher
The purpose of this project is to provide a lean and simple way to do recording and motion detection in IP or any cameras 
that can work with the video4linux interface. The project is built following the principle of the Linux toolbox - to do 
one thing well but to be open to integration with other tools. Currently it is meant to be managed via systemd and file 
drop-ins. There will be no GUI added ever. There will be an (optional) REST API probably added later.

The project is written using the Java programming language. Although Java as a language is portable, there are no guarantees
that the application will work on anything other than a Linux-based OS. This is simply because it has not been tested. It also
presents one area where contributions would be welcome.

## Recording technologies

The project uses ffmpeg as the recommended way to record footage from video cameras with an optonal fallback to the OpenCV
library. Bare in mind that OpenCV offers no audio recording capabilities.

### FFmpeg recording

At the low level, it works by creating processes using the Java process builder that start ffmpeg recording threads in the 
OS in which the application is running. The prerequisite is, of course, an installed, working version of ffmpeg in that OS.
The recording is configured to work in 1-minute chunks. This is because error recovery and immediate playback are easier to
guarantee in this way. A prerequisite for releasing v1 is to a feature, like a periodic job, that will consolidate these
chunks into a video comprised of all clips for that camera within that day.

### Opencv recording

Opencv is a fallback option and exists more out of respect for legacy and given that it needs to be present in the project
in order for the monitoring functionality to work. It will also record in 1-minute chunks with no audio. Instead of creating
a process, it will use a Java wrapper to call C++ code from the OpenCV library. This means that OpenCV needs to be installed
in that OS and that the Java binary and JAR must also be generated for that system.

## Monitoring technology

The application offers the ability, as of yet rudimentary, to detect motion in the feeds being monitored. It uses OpenCV to
construct a simple change detection algorithm that will hopefully be improved in the near future. It looks for changes between
captured frames with a configurable minimum area threshold that the system needs to observe in order to declare that motion has
been detected. Such an approach has numerous drawbacks but is relatively easy on system resources compared to more sophisticated
algorithms. A common false positive is day/night changes.

## Notification

The application currently supports notifiications on motion events by providing a slack webhook url. There is the possibility to extend it
with other notification providers. The project might also create other types of events in the future for which notifications could 
be triggered.

## Adding cameras

Camers that can be monitored or recorded must either be IP cameras that expose an RTSP video stream (with or w/o authentication) or devices
that are visible through the video4linux interface.

The application monitors a configurable path and when a json configuration file is dropped in, it will read the configuration and delete the file.
The json file specifies details such as the url / device id, any potential auth, monitoring area site and such. To be certain of all properties
please check the javadoc.

## System properties and defaults

There are also system-wide default properties, some of which need to be specified or else the application will not start. These can also be seen
in the code or in the javadoc. More details will be provided in the project documentation. This is also a task where collaborators would be welcome.

## Non-java dependencies

- OpenCV
- FFmpeg
- A slack webhook

## Deployment

The application is meant to be run as a systemd service. There is a service template provided and the `build.gradle` file contains
a task that allows these templates to be filled based on configurable properties.
