import com.opentok.cloud.controller.CloudController;
import com.opentok.cloud.controller.Session;
import com.opentok.cloud.controller.SessionListener;
import com.opentok.cloud.controller.connection.Connection;
import com.opentok.cloud.controller.connection.ConnectionEvent;
import com.opentok.cloud.controller.connection.ConnectionListener;
import com.opentok.cloud.controller.stream.Stream;
import com.opentok.cloud.controller.stream.StreamEvent;
import com.opentok.cloud.controller.stream.StreamListener;

import org.apache.log4j.Logger;

public class SessionEventLogger implements SessionListener, ConnectionListener,
		StreamListener {
	
	private static final Logger log = Logger.getLogger(SessionEventLogger.class);

    // Set API_KEY and API_SECRET to your OpenTok API key and secret. See https://dashboard.tokbox.com
    public static final String API_KEY = "Replace this with your OpenTok API key";
    public static final String API_SECRET = "Replace this with your OpenTok API secret";

    // Populate this array with session IDs for sessions you want to manage
    public static final String[] SESSION_IDS =
                                    {
                                        "Replace this with an OpenTok session ID",
                                        "Replace this with another session ID (or remove it from the list)"
                                    };
    public static void main(String[] args) {
        CloudController controller = new CloudController(API_KEY, API_SECRET);
        for (String sessionId : SESSION_IDS) {
            Session session = controller.initSession(sessionId);
            SessionEventLogger logger = new SessionEventLogger();
            session.addSessionListener(logger);
            session.addConnectionListener(logger);
            session.addStreamListener(logger);
            session.connect();
        }
    }

	@Override
	public void onSessionConnected(Session session) {
		log.info("Cloud Raptor connected to session " + session.getSessionId());
	}

	@Override
	public void onSessionDisconnected(Session session, Connection.Reason reason) {
		log.info("Cloud Raptor disconnected from session " + session.getSessionId());
		log.info("- reason: " + reason);
	}

	@Override
	public void onSessionConnectFailed(Session session, Exception exception) {
		log.info("Cloud Raptor failed to connect to session " + session.getSessionId());
		log.info("- exception: " + exception.toString());
	}
    

	@Override
	public void onConnectionCreated(ConnectionEvent event) {
		for (Connection connection : event.getConnections()) {
			log.info("New connection created");
			log.info("- session ID: " + ((Session)event.getSource()).getSessionId());
			log.info("- connection ID: " + connection.getId());
			log.info("- connection data: " + connection.getData());
		}
	}

	@Override
	public void onConnectionDestroyed(ConnectionEvent event,
			Connection.Reason reason) {
		for (Connection connection : event.getConnections()) {
			log.info("Connection destroyed - reason: " + reason);
			log.info("- session ID: " +((Session)event.getSource()).getSessionId());
			log.info("- connection ID: " + connection.getId());
		}
	}

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

	@Override
	public void onStreamDestroyed(StreamEvent event, Stream.Reason reason) {
		for (Stream stream : event.getStreams()) {
			log.info("Stream destroyed - reason: " + reason);
			log.info("- session ID: " + ((Session)(event.getSource())).getSessionId());
			log.info("- stream ID: " + stream.getId());
		}
	}

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
}
