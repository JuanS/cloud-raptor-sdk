Session Event Logger sample
===========================

The Session Event Logger sample application uses the Cloud Raptor SDK to monitor all events in a set of OpenTok sessions.
These events include:

* Cloud Raptor connecting to and disconnecting from a session (or failing to connect)
* Clients connecting and disconnecting from a session
* Streams being added and removed from a session
* Stream properties (such as whether the stream has audio or video) changing

Testing the app
---------------

1. Open the SessionManagement.java file and set the values for the following
static variables declared at the top of the class:

   * `API_KEY` — Set this to your OpenTok API key. See the [https://dashboard.tokbox.com](OpenTok dashboard).
   * `API_SECRET` — Set this to your OpenTok API secret. See the [https://dashboard.tokbox.com](OpenTok dashboard).
   * `SESSION_IDS` — Populate this with session IDs for the sessions you want the app to monitor. For example, this monitors
      two sessions:
      
           <pre>public static final String[] SESSION\_IDS =
                   {
                       "1\_MX4xMTI3fjEyNy4wLjAuMX5UaHUgTWF5IDMwIDE2OjI5OjM1IFBEVCAyMDEzfjAuMzExNjk2MX4",
                       "1\_MX4xMTI3fjEyNy4wLjAuMX5UaHUgTWF5IDMwIDE2OjMyOjMyIFBEVCAyMDEzfjAuMDM3OTMzNzA3fg"
                   };</pre>
    * `MAX_CONNECTION_COUNT` — The maximum number of clients you want to connect to the session at any one time.

2. Make sure that all of the required libraries (from the lib directory) are in the build path. Also, this app uses 
[Log4j](http://logging.apache.org/log4j/2.x/) to log information. Make sure that you have configured Log4j to work with
this app. For example, you can add a simple log4j.properties file (to log to the console):

        log4j.rootLogger=INFO, A1
        log4j.appender.A1=org.apache.log4j.ConsoleAppender
        log4j.appender.A1.layout=org.apache.log4j.PatternLayout

    Include the properties file as an argument for your configuration:

        -log4 log4j.properties

3. Run the SessionEventLogger app in debug mode.

   The Cloud Raptor app will log an event when it successfully connects to a session (or when it fails to connect).

4. Edit browser_demo.html located in the samples directory, and set the variables `apiKey` and `sessionId`
to your OpenTok API key and session ID Cloud Raptor is monitoring. Set the `token` variable to an OpenTok token for the session.
You can obtain a test token at the [https://dashboard.tokbox.com/projects](OpenTok dashboard)). (Set the role to "publisher" when
you generate the token.) Add the browser_demo.html to a web server. (WebRTC requires pages to be loaded from a web server.)

5. In a web browser, open the browser_demo.html file from your web server. Connect to the session in the page, and publish a stream.

   The Cloud Raptor app will log an event when it successfully connects to a session and when the stream is published.

6. In the web page, click the mute button in the published stream's display.

   The Cloud Raptor app will log an event when the stream stops publishing audio.

7. In the web page, stop publishing the stream, and disconnect from the session.

   The Cloud Raptor app will log an event when the client stops publishing a stream and when it disconnects from a session.


Understanding the code
----------------------

The sample app uses the OpenTok Cloud Raptor SDK to monitor all events related to OpenTok sessions.

### Instantiating a Cloud Raptor client and connecting to sessions

When the SessionEventLogger class loads, the `main()` method  instantiates a new CloudController object, passing in your OpenTok
API key and API secret to the constructor:

    CloudController controller = new CloudController(API_KEY, API_SECRET);

The OpenTok Cloud Raptor SDK defines the CloudController class. The static `CloudController.initSession()` instantiates
Session objects, which correspond to OpenTok sessions. The sample code creates Session objects for each session ID in the
`SESSION_IDS` array. It also sets up event listeners for events (described below), and it connects to the session:

    for (String sessionId : SESSION_IDS) {
        Session session = controller.initSession(sessionId);
        SessionEventLogger logger = new SessionEventLogger();
        session.addSessionListener(logger);
        session.addConnectionListener(logger);
        session.addStreamListener(logger);
        session.connect();
    }

The SessionEventLogger class implements the SessionListener, ConnectionListener, and StreamListener interfaces. These are
defined by the OpenTok Cloud Raptor SDK. This means that the instance of the SessionEventLogger class implements methods
that are called when session-related, connection-related, and stream-related events occur in the session. A unique instance
of the the SessionEventLogger class (`logger`) is added for each session.

### Listening for the Cloud Raptor app connecting to and disconnecting from the session

The SessionEventLogger class implements the SessionListener interface, defined by the OpenTok Cloud Raptor
SDK. It listens for events related to Cloud Raptor connecting to and disconnecting from a session. The `main()`
method of the class added an instance of the SessionEventLogger class as a SessionListener:

    SessionEventLogger logger = new SessionEventLogger();
    // ...
    session.addSessionListener(logger);

Because the SessionEventLogger class implements the SessionListener interface, it has an `onSessionConnected()`
method. This method is called when connections are added to a session:

    @Override
        public void onSessionConnected(Session session) {
            log.info("Cloud Raptor connected to session " + session.getSessionId());
        }

The Session object passed into the `onConnectionCreated()` method has a `getSessionId()` method, which returns
the unique session ID for the session that Cloud Raptor connected to.

The `onSessionConnected()` method uses [http://logging.apache.org/log4j/2.x/](Log4j) to log information on the
newly created Cloud Raptor connection. (Other methods in the app also use Log4j to log information.)

Because the SessionEventLogger class implements the SessionListener interface, it has an `onSessionDisconnected()`
method. This method is called when clients disconnect from a session:

    @Override
        public void onSessionDisconnected(Session session, Connection.Reason reason) {
            log.info("Cloud Raptor disconnected from session " + session.getSessionId());
            log.info("- reason: " + reason);
        }

In addition to the session parameter, the `onConnectionDestroyed()` method has a `reason` parameter. This is
a string defining the reason for which the connection ended.

The `onConnectionCreated()` method logs information on the Cloud Raptor connection ending.

In the event the Cloud Raptor app fails to connect to a session, the `onSessionConnectFailed()` (defined in the
SessionListener interface) is called:

    @Override
        public void onSessionConnectFailed(Session session, Exception exception) {
            log.info("Cloud Raptor failed to connect to session " + session.getSessionId());
            log.info("- exception: " + exception.toString());
        }

### Listening for clients connecting and disconnecting from sessions

The SessionEventLogger class implements the ConnectionListener interface, defined by the OpenTok Cloud Raptor
SDK. It listens for connection-related events for OpenTok sessions. Remember that the `main()` method of the class
added an instance of the SessionEventLogger class as a  ConnectionListener:

    SessionEventLogger logger = new SessionEventLogger();
    session.addConnectionListener(logger);

Because the SessionEventLogger class implements the ConnectionListener interface, it has an `onConnectionCreated()`
method. This method is called when connections are added to a session:

    @Override
    public void onConnectionCreated(ConnectionEvent event) {
        for (Connection connection : event.getConnections()) {
            log.info("New connection created");
            log.info("- session ID: " + ((Session)event.getSource()).getSessionId());
            log.info("- connection ID: " + connection.getId());
            log.info("- connection data: " + connection.getData());
        }
    }

The ConnectionEvent object passed into the `onConnectionCreated()` method includes these methods:

* The `getConnections()` method returns a list of Connection objects corresponding to the created connections. The Connection
class is defined in the OpenTok Cloud Raptor SDK. The `getId()` method of the Connection object returns the unique ID for
the connection. The `getData()` method of the Connection object returns the data string defined for the Connection. (You
can, optionally, define a data string when creating an OpenTok token. When a client connects using this token, this is the
data associated with the connection. You can use this to include identifying information or other metadata about the client.)
* The `getSource()` method returns the Session object to which the event pertains, corresponding to the OpenTok session.
The Session class is defined in the OpenTok Cloud Raptor SDK. It includes a `getSessionId()` method, which returns the
unique session ID for the session.

Because the SessionEventLogger class implements the ConnectionListener interface, it has an `onConnectionDestroyed()`
method. This method is called when clients disconnect from a session:

    @Override
    public void onConnectionDestroyed(ConnectionEvent event,
            Connection.Reason reason) {
        for (Connection connection : event.getConnections()) {
            log.info("Connection destroyed - reason: " + reason);
            log.info("- session ID: " +((Session)event.getSource()).getSessionId());
            log.info("- connection ID: " + connection.getId());
        }
    }

In addition to the `event` parameter (defined by the ConnectionEvent class), the `onConnectionDestroyed()` method has a
`reason` parameter. This is a string defining the reason for which the connection was destroyed.

The `onConnectionCreated()` method uses [http://logging.apache.org/log4j/2.x/](Log4j) to log information on the
destroyed connections.


### Listening for streams added to and removed from the session

The SessionEventLogger class implements the StreamListener interface, defined by the OpenTok Cloud Raptor
SDK. It listens for stream-related events for OpenTok sessions. Remember that the `main()` method of the class
added an instance of the SessionEventLogger class as a  ConnectionListener:

    SessionEventLogger logger = new SessionEventLogger();
    // ...
    session.addStreamListener(logger);

Because the SessionEventLogger class implements the StreamListener interface, it has an `onStreamCreated()`
method. This method is called when streams are added to a session:

    @Override
    public void onStreamCreated(StreamEvent event) {
        for (Stream stream : event.getStreams()) {
            log.info("New stream created");
            log.info("- session ID: " + ((Session)event.getSource()).getSessionId());
            log.info("- stream ID: " + stream.getId());
            log.info("- stream has audio: " + stream.hasAudio());
            log.info("- stream has video: " + stream.hasVideo());
        }
    }

The StreamEvent object passed into the `onConnectionCreated()` method includes these methods:

* The `getStreams()` method returns a list of Stream objects corresponding to the created streams. The Stream
class is defined in the OpenTok Cloud Raptor SDK. The `getId()` method of the Stream object returns the unique ID for
the stream. The `getHasAudio()` method of the Connection object returns a boolean which is `true` if the stream
has audio and `false` if it does not. The `getHasVideo()` method of the Connection object returns a boolean which is
`true` if the stream has video and `false` if it does not.
* The `getSource()` method returns the Session object to which the event pertains, corresponding to the OpenTok session
in which the stream was added.

Because the SessionEventLogger class implements the StreamListener interface, it has an `onStreamDestroyed()`
method. This method is called when a stream ends in a session:

    @Override
    public void onStreamDestroyed(StreamEvent event, Stream.Reason reason) {
        for (Stream stream : event.getStreams()) {
            log.info("Stream destroyed - reason: " + reason);
            log.info("- session ID: " + ((Session)(event.getSource())).getSessionId());
            log.info("- stream ID: " + stream.getId());
        }
    }

In addition to the `event` parameter (defined by the StreamEvent class), the `onStreamDestroyed()` method has a
`reason` parameter. This is a string defining the reason for which the stream ended.

Finally, because the SessionEventLogger class implements the StreamListener interface, it has an `onStreamDestroyed()`
method. This method is called when a stream ends in a session:

    @Override
    public void onStreamModified(StreamEvent event, String changedProperty,
            Object oldValue, Object newValue) {
        // For onStreamModified, there is only one Stream
        Stream stream = event.getStreams().get(0);

        log.info("Stream property changed");
        log.info("- session ID: " + ((Session)(event.getSource())).getSessionId());
        log.info("- stream ID: " + stream.getId());
        log.info("- changed property: " + changedProperty);
        log.info("- old value: " + oldValue.toString());
        log.info("- new value: " + newValue.toString());
    }

In addition to the `event` parameter (defined by the StreamEvent class), the `onStreamDestroyed()` method has 
the following parameters:

* `changedProperty` is the name of the property of the Stream that changed (such as `"hasAudio"` or `"hasVideo"`).
* `oldValue` is old value of the property. For example, for the `hasAudio` property, the value can be `true` or `false`.
* `newValue` is the new value of the property.
