import java.util.ArrayList;
import java.util.List;

import com.opentok.cloud.controller.Session;
import com.opentok.cloud.controller.connection.Connection;
import com.opentok.cloud.controller.connection.ConnectionEvent;
import com.opentok.cloud.controller.connection.ConnectionListener;

/**
 * A connection listener used by the SessionManagement sample app. This class
 * monitors the number of connections in the session and forces newly connecting
 * clients to disconnect once the maximum connection count is reached.
 */
public class MaxCountConnectionListener implements ConnectionListener {

    private int maxConnectionCount = 2;
    List<Connection> connections = new ArrayList<Connection>();
    
    public MaxCountConnectionListener(int max) {
        this.maxConnectionCount = max;
    }

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

    @Override
    public void onConnectionDestroyed(ConnectionEvent event, Connection.Reason reason) {
        for (Connection connection : event.getConnections()) {
            System.out.println("Connection destroyed: " + connection.toString());
            connections.remove(connection);
        }
    }
}
