package dev.thesheep.simpleresourcepack.networking;

import dev.thesheep.simpleresourcepack.SimpleResourcepack;

import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class HttpDataResponse {

    private byte[] data;
    public HttpDataResponse(byte[] data)
    {
        this.data = data;
    }

    /**
     * Returns a simple 404 responds
     * @return
     */
    public static byte[] get404()
    {
        String httpResponse = "HTTP/1.1 404 Not found.\r\n\r\n";
        return httpResponse.getBytes();
    }

    /**
     * Respond to a socket
     * @param socket
     * @throws Exception
     */
    public void send(Socket socket) throws Exception
    {


        SimpleResourcepack.debugLog("Sending response...");
        String fileName = "simplerp-" + System.currentTimeMillis();

        String httpResponse = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: application/zip\r\n" +
                "Content-Disposition: attachment; filename=\"" + fileName + ".zip\"\r\n" +
                "Content-Length: " + data.length + "\r\n" +
                "\r\n";

        byte[] httpResponseBytes = httpResponse.getBytes(StandardCharsets.UTF_8);

        SimpleResourcepack.debugLog("Sending headers...");
        socket.getOutputStream().write(httpResponseBytes);

        SimpleResourcepack.debugLog("Sending data...");

        socket.getOutputStream().write(data);
        SimpleResourcepack.debugLog("Flushing...");
        socket.getOutputStream().flush();
    }

}
