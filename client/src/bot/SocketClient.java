package bot;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.json.simple.JSONObject;

import java.util.Random;
import java.util.concurrent.*;


@WebSocket(maxTextMessageSize = 64 * 1024)
public class SocketClient {

    private static String[] emails = new String[] {"email_1@mail.ru", "email_2@mail.ru", "email_3@mail.ru", "email_4@mail.ru"};
    private static String[] passwords = new String[] {"pass1", "pass2", "pass3", "pass4"};

    private final CountDownLatch closeLatch;

    @SuppressWarnings("unused")
    private Session session;

    public SocketClient() {
        this.closeLatch = new CountDownLatch(1);
    }

    public boolean awaitClose(int duration, TimeUnit unit) throws InterruptedException {
        return this.closeLatch.await(duration, unit);
    }

    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        System.out.printf("Connection closed: %d - %s%n", statusCode, reason);
        this.session = null;
        this.closeLatch.countDown();
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        System.out.printf("Got connect: %s%n", session);
        this.session = session;
        sendInitializationRequest();
    }

    @OnWebSocketMessage
    public void onMessage(String msg) {
        System.out.println("message: " + msg);
        try {
            Thread.sleep(1000);
            session.close(StatusCode.NORMAL, "I'm done");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void sendInitializationRequest() {
        /**
         * {
         "type":"LOGIN_CUSTOMER",
         "sequence_id":"715c13b3-881a-9c97-b853-10be585a9747”,
         "data”:{
         "email":"123@gmail.com”,
         "password":”newPassword"
         }
         */

        Random random = new Random();

        int userI = random.nextInt(emails.length);

        String req = "request from client";

        JSONObject data = new JSONObject();
        data.put("email", emails[userI]);
        data.put("password", passwords[userI]);

        JSONObject obj = new JSONObject();
        obj.put("type", "LOGIN_CUSTOMER");
        obj.put("sequence_id", "715c13b3-881a-9c97-b853-10be585a9747");
        obj.put("data", data);

        Future<Void> fut;
        fut = session.getRemote().sendStringByFuture(obj.toJSONString());
        try {
            fut.get(2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }
}
