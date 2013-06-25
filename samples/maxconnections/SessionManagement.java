import com.opentok.cloud.controller.CloudController;
import com.opentok.cloud.controller.Session;

/**
 * This sample application limits the number of clients connected to a session.
 * Be sure to add values for the static variables declared at the top of the class.
 * For a complete description, see the README.md file in this source directory.
 */
public class SessionManagement {
    // Set API_KEY and API_SECRET to your OpenTok API key and secret. See https://dashboard.tokbox.com
    public static final String API_KEY = "Replace with your OpenTok API key.";
    public static final String API_SECRET = "Replace with your OpenTok API secret.";

    // Populate this array with session IDs for sessions you want to manage
    public static final String[] SESSION_IDS =
                                    {
    									"Replace this with an OpenTok session ID",
        								"Replace this with another session ID (or remove it from the list)"
                                    };
    public static final int MAX_CONNECTION_COUNT = 2;
    
    public static void main(String[] args) {
        CloudController controller = new CloudController(API_KEY, API_SECRET);
        for (String sessionId : SESSION_IDS) {
            Session session = controller.initSession(sessionId);
            session.addConnectionListener(new MaxCountConnectionListener(MAX_CONNECTION_COUNT));
            session.connect();
        }
    }
}
