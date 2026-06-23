package dev.thesheep.simpleresourcepack.networking;

import dev.thesheep.simpleresourcepack.SimpleResourcepack;
import org.bukkit.Bukkit;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

public class FileHoster {
    private static final int SLEEP_INTERVAL_MS = 20;
    private static final int READ_TIMEOUT_MS = 5000;
    private static final int MAX_REQUEST_LINES = 100;

    private static String ip;
    private static int port;
    private static ServerSocket serverSocket;
    private static ExecutorService executorService;
    private static boolean disabled = true;

    public static void initialize(String ip, int port) {
        FileHoster.ip = ip;
        FileHoster.port = port;
        executorService = Executors.newCachedThreadPool();

        try {
            start();
        } catch (Exception e) {
            disabled = true;
            Bukkit.getLogger().severe("Failed to start resourcepack server: " + e);
        }
    }

    private static void start() throws IOException {
        serverSocket = new ServerSocket(port);
        serverSocket.setSoTimeout(0);

        SimpleResourcepack.debugLog("Checking if server is publicly available.");
        if (!isPubliclyReachable(1000)) {
            Bukkit.getLogger().severe("Failed to start resourcepack server: Server is not publicly reachable on " + ip + ":" + port);
            return;
        }
        SimpleResourcepack.debugLog("Check done!");

        disabled = false;

        CompletableFuture.runAsync(FileHoster::runServer, executorService);

        SimpleResourcepack.debugLog("Server is now running on port " + port);
    }

    private static void runServer() {

        while (!Thread.currentThread().isInterrupted()) {
            try {
                tick();
                Thread.sleep(SLEEP_INTERVAL_MS);
            } catch (InterruptedException e) {
                disabled = true;
                SimpleResourcepack.getInstance().getLogger().info("Server thread interrupted");
                break;
            } catch (SocketException | SocketTimeoutException e) {
                disabled = true;
                SimpleResourcepack.getInstance().getLogger().severe("The file hosting sever crashed! " + e);
                break;
            } catch (Exception e) {
                disabled = true;
                SimpleResourcepack.getInstance().getLogger().severe("Failed to handle tick: " + e);
                break;
            }
        }
    }

    private static void tick() throws IOException {
        Socket socket = serverSocket.accept();

        if (disabled) {
            try {
                socket.close();
            } catch (Exception e)
            {
                SimpleResourcepack.debugLog("The file server has shutdown, rejecting connections.");
            }
            return;
        }

        CompletableFuture.runAsync(() -> handleClient(socket), executorService);
    }

    private static void handleClient(Socket socket) {
        String response = "none";
        String path = "none";

        try {

            SimpleResourcepack.debugLog("Starting to handle client...");

            SimpleResourcepack.debugLog(
                    "Connection from " + socket.getRemoteSocketAddress()
            );

            if (socket.isClosed()) {
                return;
            }

            socket.setSoTimeout(READ_TIMEOUT_MS);

            SimpleResourcepack.debugLog("Extracting file path from client.");
            response = extractPath(socket);

            if (response.equals("fallback")) {
                SimpleResourcepack.debugLog("Using fallback 404!");
                sendResponse(socket, new HttpDataResponse(HttpDataResponse.get404()));
                return;
            }

            if(response.equalsIgnoreCase("/health"))
            {
                SimpleResourcepack.debugLog("Got the health check, nice!");
                return;
            }

            String[] parts = response.split("/");
            if(parts.length != 3)
            {
                SimpleResourcepack.debugLog("Got an invalid request format. (" + response + ") ignoring it.");
                socket.close();
                return;
            }

            path = parts[2];

            SimpleResourcepack.debugLog("Path is " + path);

            Path filePath = Paths.get(SimpleResourcepack.getInstance().getCacheFolder().getPath(), path + ".zip");

            SimpleResourcepack.debugLog("Filepath is " + filePath);

            if (!Files.exists(filePath)) {
                // Try a backup path
                filePath = Paths.get(SimpleResourcepack.getInstance().getCacheFolder().getPath(), path);
                if(!Files.exists(filePath)) {

                    SimpleResourcepack.debugLog("File does not exist!");
                    return;
                }
            }


            SimpleResourcepack.debugLog("Reading file data...");
            byte[] fileBytes = Files.readAllBytes(filePath);


            SimpleResourcepack.debugLog("Sending response");
            sendResponse(socket, new HttpDataResponse(fileBytes));
        }
        catch (SocketException e) {
            if ("Broken pipe".equalsIgnoreCase(e.getMessage())) {
                SimpleResourcepack.debugLog(
                        "Client disconnected during transfer. " + e
                );
                return;
            }
            else {
                SimpleResourcepack.getInstance().getLogger().log(Level.SEVERE, "SocketException: " + e);
            }
        }
        catch (Exception e) {
            SimpleResourcepack.getInstance().getLogger().log(Level.WARNING, "Failed to handle client request: " + response + " on path " + path + " because: ", e);
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                SimpleResourcepack.getInstance().getLogger().log(Level.WARNING, "Failed to close client socket", e);
            }
        }
    }

    private static void sendResponse(Socket socket, HttpDataResponse response) throws Exception {
        response.send(socket);
    }

    private static String extractPath(Socket socket) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        try {
            String line;
            int lineCount = 0;
            while ((line = reader.readLine()) != null && !line.isEmpty() && lineCount < MAX_REQUEST_LINES) {
                SimpleResourcepack.debugLog("Request line: " + line);
                if (line.startsWith("GET") || line.startsWith("HEAD")) {
                    String[] parts = line.split("\\s+");
                    if (parts.length > 1) {
                        return parts[1];
                    }
                }
                lineCount++;
            }
        } catch (IOException e) {
            Bukkit.getLogger().log(Level.WARNING, "Error reading from socket", e);
        }

        SimpleResourcepack.debugLog("Could not get correct path.");

        return "fallback";
    }

    public static boolean isPubliclyReachable(int timeoutMs) {
        if (ip == null) {
            Bukkit.getLogger().warning("Could not determine external IP address.");
            return false;
        }

        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(ip, port), timeoutMs);

            OutputStream out = socket.getOutputStream();
            out.write("GET /health HTTP/1.1\r\n\r\n".getBytes());
            out.flush();

            return true;
        } catch (IOException e) {
            Bukkit.getLogger().warning("Server is not publicly reachable: " + e.getMessage());
            return false;
        }
    }

    public static int getPort() {
        return port;
    }

    public static String getIp() {
        return ip;
    }

    public static boolean isDisabled() {
        return disabled;
    }

    public static void shutdown() {

        SimpleResourcepack.debugLog("Shutting down server...");
        try {
            disabled = true;

            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            if (executorService != null) {
                executorService.shutdownNow();
            }
        } catch (IOException e) {
            SimpleResourcepack.getInstance().getLogger().log(Level.SEVERE, "Error shutting down FileHoster", e);
        }
    }
}
