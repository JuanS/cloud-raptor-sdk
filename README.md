OpenTok Cloud Raptor SDK README
===============================

The OpenTok Cloud Raptor SDK is a Java library that lets you moderate and monitor events in OpenTok sessions.

This is a beta version of the SDK. Some APIs may change. We encourage you to send us feedback and learn more at
the [OpenTok Cloud Raptor forum](http://www.tokbox.com/forums/cloud-raptor-sdk).

Using the Cloud Raptor SDK
--------------------------

In your Java project, add the contents of the lib directory to your project's build path. This directory
includes the opentok-cloud-raptor.jar file and other required jar files.

The OpenTok Cloud Raptor SDK requires a number of other libraries, which are included in the lib directory.
Add these to your project's build path.

Sample apps
-----------

Look at the samples directory for sample Cloud Raptor apps. Be sure to read the README files for each app

Using CloudRaptor
-----------------

###Initializing a CloudController object and connecting to a session

The com.opentok.cloud.controller.CloudController class is the root class for any Cloud Raptor app. Initialize a CloudRaptor object by 
passing your OpenTok API key and API secret into the constructor:

    CloudController controller = new CloudController(API_KEY, API_SECRET);

You then call the `initSession()` method of the CloudController object, passing in an OpenTok session ID:

    Session session = controller.initSession(sessionId);

The session ID must be for a session that was generated with the API key used in instantiating the CloudController object.
You can instantiate multiple Session objects to work with different OpenTok sessions within a Cloud Raptor app.

The Session object has methods for moderating OpenTok sessions and for listening to events in sessions. (Details are
provided below.)

Before you can use most methods of the Session object, the app must successfully connect to the session. Call the
`addSessionListener()` method of the Session object, passing in an object that implements the SessionListener class.
Then call the `connect()` method of the session.

The `onSessionConnected()` method of the SessionListener object is called when the session connects.

*Note:* You *can* (and should) call the following methods of the Session object before the `onSessionConnected()` method of the
SessionListener object is called: `addSessionListener()`, `addConnectionListener()`, and `addStreamListener()`. Call these
methods to set event listeners. (See the SessionEventLogger demo app for an example.)

**Important:** When a Cloud Raptor client connects to a session, it shows up as a new connection on all other clients.
The connection ID of a Cloud Raptor client begins with with `"CR_"`. For example, in the Cloud Raptor SDK, check the 
`getId()` method of a Connection object to see if it is a Cloud Raptor client. In the OpenTok on WebRTC JavaScript library,
checkt the `id` property of a Connection object. A connection with an ID that does not start with `"CR_"` represent an OpenTok
client connecting to the session on a browser or mobile app. See the demo apps for code that checks this ID string. Also,
be aware of this fact when building JavaScript and mobile OpenTok apps.

###Listening for events in a session

The Cloud Raptor SDK lets you monitor events in OpenTok session. These events include the following:

* Cloud Raptor connecting to and disconnecting from a session (or failing to connect)
* Clients connecting and disconnecting from a session
* Streams being added and removed from a session
* Stream properties (such as whether the stream has audio or video) changing

For a demo app, with a full description, see the SessionEventLogger sample app in the demos directory.

###Moderating a session

The Cloud Raptor SDK lets you moderate a session, forcing clients to disconnect from a session or to
stop publishing streams.

* Use the `forceDisconnect()` method of a Session object to force a client to disconnect.
* Use the `forceUnpublish()` method of a Session object to force a client to stop publishing a stream.

More information
----------------

See the docs directory for the OpenTok CloudRaptor SDK API reference.


