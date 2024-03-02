package dev.thesheep.simpleresourcepack.networking;

import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class HttpDataResponse {

    private byte[] data;
    public HttpDataResponse(byte[] data)
    {
        this.data = data;
    }

    public static byte[] get404()
    {
        String httpResponse = "HTTP/1.1 404 Not found.\r\n\r\n";
        return httpResponse.getBytes();
    }

    public void Send(Socket socket) throws Exception
    {
        String fileName = "simplerp-" + System.currentTimeMillis();

        String httpResponse = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: application/zip\r\n" +
                "Content-Disposition: attachment; filename=\"" + fileName + ".zip\"\r\n" +
                "Content-Length: " + data.length + "\r\n" +
                "\r\n";

        byte[] httpResponseBytes = httpResponse.getBytes(StandardCharsets.UTF_8);
        socket.getOutputStream().write(httpResponseBytes);
        socket.getOutputStream().write(data);
        socket.getOutputStream().flush();
    }

}
