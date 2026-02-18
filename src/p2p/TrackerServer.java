package p2p;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TrackerServer {
    private final int port;
    private final Map<String, PeerInfo> peers = new ConcurrentHashMap<>();

    public TrackerServer(int port) {
        this.port = port;
    }

    public void start() throws IOException {
        try (ServerSocket ss = new ServerSocket(port)) {
            System.out.println("[TRACKER] Listening on " + port);
            while (true) {
                Socket client = ss.accept();
                new Thread(() -> handle(client)).start();
            }
        }
    }

    private void handle(Socket client) {
        try (client;
                BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()))) {

            String line = in.readLine();
            if (line == null)
                return;

            String[] args = P2PProtocol.parseArgs(line);

            if (P2PProtocol.isCommand(line, P2PProtocol.REGISTER)) {
                // REGISTER peerId port
                if (args.length != 2) {
                    out.write(P2PProtocol.buildError("Invalid REGISTER"));
                } else {
                    String peerId = args[0];
                    int peerPort = Integer.parseInt(args[1]);
                    String ip = client.getInetAddress().getHostAddress();
                    peers.put(peerId, new PeerInfo(peerId, ip, peerPort));
                    out.write(P2PProtocol.buildOk());
                }
                out.newLine();
                out.flush();
                return;
            }

            if (P2PProtocol.isCommand(line, P2PProtocol.LIST)) {
                out.write(buildPeerList());
                out.newLine();
                out.flush();
                return;
            }

            out.write(P2PProtocol.buildError("Unknown command"));
            out.newLine();
            out.flush();

        } catch (Exception e) {
            System.out.println("[TRACKER] Error: " + e.getMessage());
        }
    }

    private String buildPeerList() {
        // Construye la lista: peer1@ip:port,peer2@ip:port
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (PeerInfo p : peers.values()) {
            if (!first)
                sb.append(",");
            sb.append(p.peerId).append("@").append(p.ip).append(":").append(p.port);
            first = false;
        }
        return P2PProtocol.buildPeers(sb.toString());
    }

    public static void main(String[] args) throws Exception {
        new TrackerServer(6000).start();
    }

    private static class PeerInfo {
        final String peerId;
        final String ip;
        final int port;

        PeerInfo(String peerId, String ip, int port) {
            this.peerId = peerId;
            this.ip = ip;
            this.port = port;
        }
    }
}