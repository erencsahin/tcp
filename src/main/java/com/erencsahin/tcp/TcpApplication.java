package com.erencsahin.tcp;


import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

public class TcpApplication {

    private final Map<String, RateGenerator> rateGenerators = new ConcurrentHashMap<>();
    private final Map<String, List<PrintWriter>> subscribers = new ConcurrentHashMap<>();

    private final long publishInterval;
    private final int port;

    private final boolean running = true;

    public TcpApplication(int port, long publishInterval, Map<String, Double> initialRates) {
        this.port = port;
        this.publishInterval = publishInterval;
        for (Map.Entry<String, Double> entry : initialRates.entrySet()) {
            rateGenerators.put(entry.getKey(), new RateGenerator(entry.getValue(),entry.getValue()));
            subscribers.put(entry.getKey(), new CopyOnWriteArrayList<>());
        }
    }

    public void start() {
        Thread publisherAskThread = new Thread(this::publishRates);
        publisherAskThread.start();

        Thread serverThread = new Thread(this::handleIncomingConnections);
        serverThread.start();
    }

    private void publishRates() {
        try {
            Thread.sleep(publishInterval);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }

        while (running) {
            for (String symbol : rateGenerators.keySet()) {
                RateGenerator generator = rateGenerators.get(symbol);
                double ask = generator.getNextAskValue();
                double bid = generator.getNextBidValue();
                String timestamp = LocalDateTime.now().toString();
                String msg = String.format("%s|bid:%.5f|ask:%.5f|timestamp:%s",
                        symbol, ask, bid, timestamp);
                for (PrintWriter out : subscribers.get(symbol)) {
                    out.println(msg);
                    out.flush();
                }
            }
            try {
                Thread.sleep(publishInterval);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }


    private void handleIncomingConnections() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("TCP Server is running on port " + port);

            while (running) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket);
                new Thread(() -> handleClient(clientSocket)).start();
            }

        } catch (IOException e) {
            e.getCause();
        }
    }

    private void handleClient(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

            out.println("Connected to TCP Server. Use commands like 'subscribe-PF1_USDTRY'.");
            String line;
            while ((line = in.readLine()) != null) {
                processCommand(line, out);
            }
        } catch (IOException e) {
            e.getCause();
        }
    }

    private void processCommand(String command, PrintWriter out) {
        String[] parts = command.split("-");

        if (parts.length < 2) {
            out.println("ERROR|Invalid request format");
            return;
        }

        String action = parts[0].trim();
        String currency = parts[1].trim();

        switch (action) {
            case "subscribe":
                if (!rateGenerators.containsKey(currency)) {
                    out.println("ERROR|Rate data not found for " + currency);
                } else {
                    subscribers.get(currency).add(out);
                    out.println("Subscribed to " + currency);
                }
                break;
            case "unsubscribe":
                if (!rateGenerators.containsKey(currency)) {
                    out.println("ERROR|Rate data not found for " + currency);
                } else {
                    subscribers.get(currency).remove(out);
                    out.println("Unsubscribed from " + currency);
                }
                break;
            default:
                out.println("ERROR|Invalid request format");
        }
    }


    public static void main(String[] args) {
        int port = 8081;
        long interval = 2000L;

        Map<String, Double> initialRates = new HashMap<>();
        initialRates.put("PF1_USDTRY", 38.41904);
        initialRates.put("PF1_EURUSD", 1.13538);
        initialRates.put("PF1_GBPUSD", 1.33068);

        TcpApplication server = new TcpApplication(port, interval, initialRates);
        server.start();

    }
}
