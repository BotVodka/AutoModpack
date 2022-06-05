package pl.skidam.automodpack.server;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import pl.skidam.automodpack.AutoModpackMain;

import java.io.*;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static pl.skidam.automodpack.AutoModpackMain.*;

public class HostModpack implements HttpHandler {

    private static final Path MODPACK_FILE = Path.of(FabricLoader.getInstance().getGameDir().toFile() + "/AutoModpack/modpack.zip");

    private static HttpServer server = null;
    private static ExecutorService threadPool = null;

    public static String modpackHostIp;

    public static void stop() {
        server.stop(1);
        threadPool.shutdownNow();
    }

    public static void start(MinecraftServer minecraftServer) {
        threadPool = Executors.newFixedThreadPool(host_thread_count, new ThreadFactoryBuilder().setNameFormat("AutoModpack-Modpack-Host-%d").build());

        CompletableFuture.runAsync(() -> {
            try {
                LOGGER.info("Starting modpack server...");

                // String serverIp = InetAddress.getLocalHost().getHostAddress();
                String subUrl = "modpack";

                // get local ip

                String serverIp = "0.0.0.0";
                try (java.util.Scanner s = new java.util.Scanner(new java.net.URL("https://api.ipify.org").openStream(), "UTF-8").useDelimiter("\\A")) {
                    System.out.println("My current IP address is " + s.next());
                    serverIp = s.next();
                } catch (java.io.IOException e) {
                    e.printStackTrace();
                }

                server = HttpServer.create(new InetSocketAddress(serverIp, host_port), 0);
                server.createContext("/" + subUrl, new HostModpack());
                server.setExecutor(threadPool);
                server.start();

                modpackHostIp = String.format("http://%s:%s/%s", serverIp, host_port, subUrl);

                String hash = String.format("%040x", new BigInteger(1, MessageDigest
                        .getInstance("SHA-1")
                        .digest(new FileInputStream(MODPACK_FILE.toString()).readAllBytes()))
                );

                AutoModpackMain.link = modpackHostIp;

                LOGGER.info("Modpack host started at {} (Hash: {})", modpackHostIp, hash);
            } catch (Exception e) {
                LOGGER.error("Failed to start the modpack server!", e);
            }
        }, HostModpack.threadPool);

    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (Objects.equals(exchange.getRequestMethod(), "GET")) {
//            LOGGER.error(exchange.getRequestURI().toString());
//            LOGGER.error(exchange.getRequestHeaders().toString());
//            if (exchange.getRequestHeaders().getFirst("X-Minecraft-Username") != null) {
//                LOGGER.info("Supplying modpack for Minecraft player: {}", exchange.getRequestHeaders().getFirst("X-Minecraft-Username"));
//            } else {
//                LOGGER.info("Supplying modpack to a non-Minecraft client");
//            }

            LOGGER.info("Supplying modpack to the client");

            OutputStream outputStream = exchange.getResponseBody();
            File pack = MODPACK_FILE.toFile();

            exchange.getResponseHeaders().add("User-Agent", "Java/modpack-host");
            exchange.sendResponseHeaders(200, pack.length());

            FileInputStream fis = new FileInputStream(pack);
            BufferedInputStream bis = new BufferedInputStream(fis);
            bis.transferTo(outputStream);
            bis.close();
            fis.close();

            outputStream.flush();
            outputStream.close();
        } else {
            exchange.sendResponseHeaders(400, 0);
        }
    }
}
