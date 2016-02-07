package com.saturn.websocket;

import com.saturn.db.DbConnection;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by ivan on 1/28/16.
 *
 */

@WebSocket
public class SaturnWebSocket {
    private Session session;
    public static final SimpleDateFormat dateFormat = new SimpleDateFormat("yy-MM-dd'T'HH:mm:ssZ", Locale.ENGLISH);

    // called when the socket connection with the browser is established
    @OnWebSocketConnect
    public void handleConnect(Session session) {
        this.session = session;
        System.out.println("Connection opened " + this.session);
    }

    // called when the connection closed
    @OnWebSocketClose
    public void handleClose(int statusCode, String reason) {
        System.out.println("Connection closed with statusCode="
                + statusCode + ", reason=" + reason);
    }

    /**
     * Приложение должно обеспечивать следующие процессы:
     — аутентификацию пользователя
     — приложение должно принимать запрос LOGIN_CUSTOMER проверять наличие данного пользователя в базе с указанным паролем и возвращать либо сообщение об успешно аутентификации с токеном и датой его истечения, либо сообщение об ошибке
     — хранение токенов пользователей
     — приложение должно вести базу выданных пользователям токенов.
     — приложение должно вести историю токенов пользователей.
     — приложение должно сбрасывать токен пользователя если он запрашивает повторную аутентификацию
     */
    @OnWebSocketMessage
    public void handleMessage(String message) {
        JSONParser parser = new JSONParser();

        Object messageParser = null;
        try {
            messageParser = parser.parse(message);
        } catch (ParseException e) {
            send(errorMessage("wrong.request", "Wrong input request", "null").toJSONString());
        }

        JSONObject jsonRequest = (JSONObject) messageParser;

        String seqId = (String) jsonRequest.get("sequence_id");

        JSONObject dataReq = (JSONObject) jsonRequest.get("data");
        String inputEmail = (String) dataReq.get("email");
        String inputPassword = (String) dataReq.get("password");

        try (Connection connection = DbConnection.GetAccess().getConnect();)
        {
            if (connection == null) {
                throw new SQLException();
            }

            String query = "SELECT * FROM users where email='" + inputEmail + "'";
            ResultSet rs = connection.createStatement().executeQuery(query);

            if (rs.next()) {
                if (inputPassword.equals(rs.getString("password"))) {
                    int userId = rs.getInt("id");

                    connection.setAutoCommit(false); // enable transaction

                    query = "WITH updateUser AS (" +
                            "  UPDATE users SET token = gen_random_uuid(), token_expiration_date = CURRENT_TIMESTAMP + INTERVAL '5 days' where id = " + userId +
                            "  RETURNING id, token, token_expiration_date" +
                            ")," +
                            "insertHistory AS (" +
                            "  INSERT INTO token_history (user_id, token) SELECT id, token FROM updateUser" +
                            ")" +
                            "" +
                            "SELECT token, token_expiration_date FROM updateUser;";
                    ResultSet ans = connection.createStatement().executeQuery(query);

                    connection.commit();

                    String token = null;
                    Date token_expiration_date = null;
                    if (ans.next()) {
                        token = ans.getString("token");
                        token_expiration_date = new Date(ans.getTimestamp("token_expiration_date").getTime());
                    }

                    if (token != null && token_expiration_date != null) {
                        //2015-07-15T11:14:30Z

                        JSONObject data = new JSONObject();
                        data.put("api_token", token);
                        data.put("api_token_expiration_date", dateFormat.format(token_expiration_date));

                        JSONObject obj = new JSONObject();
                        obj.put("type", "CUSTOMER_API_TOKEN");
                        obj.put("sequence_id", seqId);
                        obj.put("data", data);

                        send(obj.toJSONString());
                    } else {
                        send(errorMessage("db.error", "Getting token error", seqId).toJSONString());
                    }
                }
            } else {
                send(errorMessage("customer.notFound", "Customer not found", seqId).toJSONString());
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            send(errorMessage("db.error", "Db error", seqId).toJSONString());
        }
    }

    private JSONObject errorMessage(String errorCode, String errorMsg, String seqId) {
        JSONObject data = new JSONObject();
        data.put("error_description", errorMsg);
        data.put("error_code", errorCode);

        JSONObject obj = new JSONObject();
        obj.put("type", "CUSTOMER_ERROR");
        obj.put("sequence_id", seqId);
        obj.put("data", data);

        return obj;
    }

    // called in case of an error
    @OnWebSocketError
    public void handleError(Throwable error) {
        error.printStackTrace();
    }

    // sends message to browser
    private void send(String message) {
        try {
            if (session.isOpen()) {
                session.getRemote().sendString(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // closes the socket
    private void stop() {
        try {
            session.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
