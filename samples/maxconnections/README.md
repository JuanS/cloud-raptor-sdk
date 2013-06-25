Session Management sample
=========================

The Session Management sample application uses the Cloud Raptor SDK to monitor sessions and limits the number of clients connected
to each session.

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

2. Locate the browser_demo.html file, found in the root of the demos directory. Copy this file to a web server. Edit the values of the
`API_KEY` and `SESSION_ID` variables to match the OpenTok API key and one of the session IDs used in the Java code for the sample app.
Also, set the `TOKEN` variable to a token generated for the session. (You can obtain a test token at the [https://dashboard.tokbox.com/projects](OpenTok dashboard)).

3. Run the SessionManagement app in debug mode.

4. Edit browser_demo.html located in the samples directory, and set the variables `apiKey` and `sessionId`
to your OpenTok API key and session ID Cloud Raptor is monitoring. Set the `token` variable to an OpenTok token for the session.
You can obtain a test token at the [https://dashboard.tokbox.com/projects](OpenTok dashboard)). (Set the role to "publisher" when
you generate the token.) Add the browser_demo.html to a web server. (WebRTC requires pages to be loaded from a web server.)

5. In a web browser, open the browser_demo.html file from your web server. Connect to the session in the page. Repeat this in separate
browser windows until you surpass the maximum number of connections in the session. The Cloud Raptor sample app will disconnect a client
if the maximum number of connections in the session has already been reached.

Understanding the code
----------------------

The sample app uses the OpenTok Cloud Raptor SDK to monitor connections created and destroyed in a session and to force
clients to disconnect (when the maximum connection count is reached).

### Instantiating a Cloud Raptor client and connecting to sessions

When the SessionManagement class loads, the `main()` method  instantiates a new CloudController object, passing in your OpenTok
API key and API secret to the constructor:

    CloudController controller = new CloudController(API_KEY, API_SECRET);

The OpenTok Cloud Raptor SDK defines the CloudController class. The static `CloudController.initSession()` instantiates
Session objects, which correspond to OpenTok sessions. The sample code creates Session objects for each session ID in the
`SESSION_IDS` array. It also sets up a ConnectionListener object for each session, and it connects
to the session:

    for (String sessionId : SESSION_IDS) {
        Session session = controller.initSession(sessionId);
        session.addConnectionListener(new MaxCountConnectionListener(session, MAX_CONNECTION_COUNT));
        session.connect();
    }

### Forcing clients to disconnect

The sample app's MaxCountConnectionListener class implements the ConnectionListener interface, defined by the OpenTok Cloud Raptor
SDK. It listens for connection-related events for an OpenTok session.

The constructor function for the MaxCountConnectionListener class takes one parameters: the number of maximum connections
to allow in the session.

The MaxCountConnectionListener class also has a `connections` property, which is a list of active connections that we intend
to keep in the session (until they are destroyed, such as when a client disconnects from the session).

Because the MaxCountConnectionListener class implements the ConnectionListener interface, it has an `onConnectionCreated()`
method. This method is called when connections are added to the session:

    @Override
    public void onConnectionCreated(ConnectionEvent event) {
        for (Connection connection : event.getConnections()) {
            System.out.println("Connection created: " + connection.toString());
            // Ignore connections for Cloud Raptor clients. 
            if (!connection.getId().startsWith("CR_")) {
                // Add non-Cloud Raptor connections to the connections list
                // or force disconnect once the maximum connection count is reached. 
                if (connections.size() < maxConnectionCount) {
                    connections.add(connection);
                } else {
                    Session session = (Session) event.getSource();
                    session.forceDisconnect(connection.getId());
                }
            }
        }
    }

The `ConnectionEvent` object passed into the `onConnectionCreated()` method has two methods:

* The `getConnections()` method returns a list of Connection objects corresponding to the created connections. The Connection
class is defined in the OpenTok Cloud Raptor SDK.
* The `getSource()` method returns the Session object to which the event pertains, corresponding to the OpenTok session.
The Session class is defined in the OpenTok Cloud Raptor SDK.

The implementation of the `onConnectionCreated()` method in the sample app ignores connections from Cloud Raptor.
(We do not want to remove these from the session or include these in the connections array).
The `getId()` method of a Connection object returns a unique ID for the connection, and for Cloud Raptor this ID starts with
<code>"CR_"</code>.

For created connections that are not Cloud Raptor clients (such as OpenTok clients in the browser), we check to see if
the number of maximum connections has been reached. If so, it disconnects a connection by calling the `forceDisconnect()`
method of the Session object.

### Listening for clients that disconnect from the session

Because the MaxCountConnectionListener class implements the ConnectionListener interface, it has an `onConnectionDestroyed()`
method. This method is called when connections are destroyed in the session:

    @Override
    public void onConnectionDestroyed(ConnectionEvent event, Connection.Reason reason) {
        for (Connection connection : event.getConnections()) {
            System.out.println("Connection destroyed: " + connection.toString());
            connections.remove(connection);
        }
    }

The destroyed connection is removed from the `connections` list (if it is contained in the list). This way, the `connections`
list contains only active connections that are added up to the maximum number of connections. (It does not contain connections
that have been forced to disconnect by Cloud Raptor.)