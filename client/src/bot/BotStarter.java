package bot;

import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class BotStarter {
    protected static int PoolSize = 5;
    protected static int AmountOfConnections = 10;
    protected static String ServerUriString = "ws://localhost:8080/saturn";

    public static void main(String[] args) throws URISyntaxException {
        ExecutorService executor = Executors.newFixedThreadPool(PoolSize);

        for (int i = 0; i < AmountOfConnections; i++) {
            Thread thread = new Thread() {
                public void run() {
                    URI serverUri = null;
                    try {
                        serverUri = new URI(ServerUriString);
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }

                    if (serverUri != null) {
                        WebSocketClient client = new WebSocketClient();
                        SocketClient socket = new SocketClient();
                        try {
                            client.start();
                            ClientUpgradeRequest request = new ClientUpgradeRequest();
                            client.connect(socket, serverUri, request);
                            System.out.printf("Connecting to : %s%n", serverUri);
                            socket.awaitClose(5, TimeUnit.SECONDS);
                        } catch (Throwable t) {
                            t.printStackTrace();
                        } finally {
                            try {
                                client.stop();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            };
            executor.execute(thread);
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
        }
        System.out.println("Finished all threads");
    }
}
