package dev.thesheep.simpleresourcepack.networking;

import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class HttpDataResponse {

    private byte[] data;
    public HttpDataResponse(byte[] data)
    {
        this.data = data;
    }

    public void Send(Socket socket) throws Exception
    {
        String httpResponse = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: application/zip\r\n" +
                "Content-Disposition: attachment; filename=\"pack.zip\"\r\n" +
                "Content-Length: " + data.length + "\r\n" +
                "\r\n";

        byte[] httpResponseBytes = httpResponse.getBytes(StandardCharsets.UTF_8);
        socket.getOutputStream().write(httpResponseBytes);
        socket.getOutputStream().write(data);
        socket.getOutputStream().flush();
    }

}
