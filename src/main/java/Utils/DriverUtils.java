package Utils;

import java.io.IOException;
import java.net.ServerSocket;

public final class DriverUtils {
    public static int getFreePort() {
        int port = 0;

        try (ServerSocket socket = new ServerSocket(0)) {
            socket.setReuseAddress(true);
            port = socket.getLocalPort();
        } catch (IOException ex) { throw new RuntimeException(ex.getMessage());}

        if (port > 0) return port;

        throw new RuntimeException("Couldn't find free port.");
    }
}
