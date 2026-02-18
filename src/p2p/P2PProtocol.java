package p2p;

/**
 * Protocolo para la comunicación P2P.
 * Define las constantes de los comandos.
 *
 * Formato general de un mensaje: COMANDO arg1 arg2 ...
 */
public final class P2PProtocol {

    public static final String REGISTER = "REGISTER";
    public static final String LIST = "LIST";
    public static final String MSG = "MSG";
    public static final String OK = "OK";
    public static final String ERR = "ERR";
    public static final String PEERS = "PEERS";

    private P2PProtocol() {
    }

    public static String buildRegister(String peerId, int port) {
        return REGISTER + " " + peerId + " " + port;
    }

    public static String buildList() {
        return LIST;
    }

    public static String buildMsg(String fromPeerId, String text) {
        return MSG + " " + fromPeerId + " " + text;
    }

    public static String buildOk() {
        return OK;
    }

    public static String buildError(String reason) {
        return ERR + " " + reason;
    }

    public static String buildPeers(String peerList) {
        return PEERS + " " + peerList;
    }

    public static String parseCommand(String line) {
        if (line == null || line.isBlank())
            return "";
        return line.trim().split("\\s+", 2)[0].toUpperCase();
    }

    public static String[] parseArgs(String line) {
        if (line == null || line.isBlank())
            return new String[0];
        String[] parts = line.trim().split("\\s+");
        if (parts.length <= 1)
            return new String[0];

        String[] args = new String[parts.length - 1];
        System.arraycopy(parts, 1, args, 0, args.length);
        return args;
    }

    public static boolean isCommand(String line, String command) {
        if (line == null || command == null)
            return false;
        return parseCommand(line).equalsIgnoreCase(command);
    }
}
