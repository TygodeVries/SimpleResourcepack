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
        if (!isPubliclyReachable(1000)) {
            Bukkit.getLogger().severe("Failed to start resourcepack server: Server is not publicly reachable on " + ip + ":" + port);
            return;
        }

        disabled = false;

        CompletableFuture.runAsync(FileHoster::runServer, executorService);
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
                SimpleResourcepack.getInstance().getLogger().severe("A network socket exception occurred. This may be due to a network issue, try restarting!");
                SimpleResourcepack.getInstance().getLogger().severe("Exception: " + e);
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
            socket.close();
            return;
        }

        CompletableFuture.runAsync(() -> handleClient(socket), executorService);
    }

    private static void handleClient(Socket socket) {
        try {
            if (socket.isClosed()) {
                return;
            }

            socket.setSoTimeout(READ_TIMEOUT_MS);
            String response = extractPath(socket);

            if (response == null) {
                Bukkit.getLogger().warning("A fallback has been requested.");
                socket.getOutputStream().write(HttpDataResponse.get404());
                return;
            }

            String path = response.split("/")[2];
            Path filePath = Paths.get(SimpleResourcepack.getInstance().getCacheFolder().getPath(), path + ".zip");

            if (!Files.exists(filePath)) {
                return;
            }

            byte[] fileBytes = Files.readAllBytes(filePath);
            sendResponse(socket, new HttpDataResponse(fileBytes));
        } catch (Exception e) {
            SimpleResourcepack.getInstance().getLogger().log(Level.WARNING, "Failed to handle client request", e);
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                SimpleResourcepack.getInstance().getLogger().log(Level.WARNING, "Failed to close client socket", e);
            }
        }
    }

    private static void sendResponse(Socket socket, HttpDataResponse response) throws Exception {
        response.Send(socket);
    }

    private static String extractPath(Socket socket) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        try {
            String line;
            int lineCount = 0;
            while ((line = reader.readLine()) != null && !line.isEmpty() && lineCount < MAX_REQUEST_LINES) {
                if (line.startsWith("GET")) {
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

        return null;
    }

    public static boolean isPubliclyReachable(int timeoutMs) {
        if (ip == null) {
            Bukkit.getLogger().warning("Could not determine external IP address.");
            return false;
        }

        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(ip, port), timeoutMs);
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
