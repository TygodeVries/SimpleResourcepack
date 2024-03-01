package dev.thesheep.simpleresourcepack.networking;

import dev.thesheep.simpleresourcepack.SimpleResourcepack;
import org.bukkit.Bukkit;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileHoster {

    private static FileHoster instance;
    public static FileHoster getInstance()
    {
        return instance;
    }

    public FileHoster(String ip, int port)
    {
        // Make sure only one instance is running
        if(instance != null)
        {
            Bukkit.getLogger().severe("Cannot create another instance of the filehoster class!");
            return;
        }

        instance = this;

        this.port = port;
        this.ip = ip;

        try {
            Start();
        } catch (Exception e)
        {
            Bukkit.getLogger().severe("Failed to start resoucepack server: " + e);
        }
    }

    ServerSocket serverSocket;
    private void Start() throws Exception
    {
        serverSocket = new ServerSocket(port);
        serverSocket.setSoTimeout(0);

        // A sync task we use to host the server
        Runnable runnable = new Runnable()
        {
            @Override
            public void run()
            {
                while(true)
                {
                    try
                    {
                        tick();
                    }
                    catch (Exception exception)
                    {
                        System.out.println(exception);
                    }
                }
            }

            public void tick() throws Exception
            {
                Socket socket = serverSocket.accept();

                ExecutorService executorService = Executors.newSingleThreadExecutor();

                executorService.execute(() -> {
                    try {
                        String path = extractPath(socket);

                        String completeFilePath = SimpleResourcepack.getInstance().getCacheFolder().getPath() + path + ".zip";

                        byte[] fileBytes = Files.readAllBytes(new File(completeFilePath).toPath());
                        HttpDataResponse dataResponse = new HttpDataResponse(fileBytes);
                        dataResponse.Send(socket);
                    } catch (Exception exception)
                    {
                        System.out.println(exception);
                    }
                });
            }
        };

        Bukkit.getScheduler().runTaskAsynchronously(SimpleResourcepack.getInstance(), runnable);
    }

    private String extractPath(Socket socket)
    {
        String path = "fallback";

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            StringBuilder request = new StringBuilder();

            String line;
            while ((line = reader.readLine()) != null && !line.isEmpty()) {
                request.append(line).append("\r\n");
            }

            String[] lines = request.toString().split("\r\n");

            if (lines.length > 0 && lines[0].startsWith("GET")) {
                String[] parts = lines[0].split("\\s+");
                if (parts.length > 1) {
                    path = parts[1];
                }
            }
        }
        catch (Exception exception)
        {
            System.out.println(exception);
        }

        return path;
    }

    int port;
    public int getPort() { return port; }
    public void setPort(int port)
    {
        this.port = port;
    }

    String ip;
    public String getIp() {return ip; }
    public void setIp(String ip) { this.ip = ip; }
}
