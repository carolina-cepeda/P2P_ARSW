package p2p;

import java.io.*;
import java.net.Socket;
import java.util.*;

public class TrackerClient {
    private final String host;
    private final int port;

    public TrackerClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void register(String peerId, int peerPort) {
        String resp = request(P2PProtocol.buildRegister(peerId, peerPort));
        if (!resp.startsWith(P2PProtocol.OK))
            throw new RuntimeException("Register failed: " + resp);
    }

    public Map<String, HostPort> listPeers() {
        String resp = request(P2PProtocol.buildList());
        if (!P2PProtocol.isCommand(resp, P2PProtocol.PEERS))
            throw new RuntimeException("LIST failed: " + resp);

        String payload = resp.substring("PEERS".length()).trim();
        Map<String, HostPort> out = new HashMap<>();
        if (payload.isBlank())
            return out;

        String[] entries = payload.split(",");
        for (String e : entries) {
            // peerId@ip:port
            String[] a = e.split("@");
            String peerId = a[0];
            String[] hp = a[1].split(":");
            out.put(peerId, new HostPort(hp[0], Integer.parseInt(hp[1])));
        }
        return out;
    }

    private String request(String line) {
        try (Socket s = new Socket(host, port);
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
                BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()))) {
            out.write(line);
            out.newLine();
            out.flush();
            return in.readLine();
        } catch (IOException e) {
            throw new RuntimeException("Tracker unreachable: " + e.getMessage(), e);
        }
    }

    public static class HostPort {
        public final String host;
        public final int port;

        public HostPort(String host, int port) {
            this.host = host;
            this.port = port;
        }
    }
}
