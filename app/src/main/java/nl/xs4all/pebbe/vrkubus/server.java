package nl.xs4all.pebbe.vrkubus;

import java.io.DataInputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Locale;

public class server implements MainActivity.Provider {

    private Thread myThread;
    private Socket socket;
    private DataInputStream input;
    private PrintStream output;

    private float x;
    private float y;
    private float z;
    private boolean ok = false;
    private boolean running = false;
    private Object okLock = new Object();
    private Object runningLock = new Object();

    public server() {
        running = true;
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    socket = new Socket("192.168.178.24", 8448);
                    input = new DataInputStream(socket.getInputStream());
                    output = new PrintStream(socket.getOutputStream());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                output.format("VRC1.0\n");
                synchronized (runningLock) {
                    running = false;
                }
            }
        };
        myThread = new Thread(runnable);
        myThread.start();
    }

    @Override
    public boolean forward(float[] out, float[] in) {
        final float xi = in[0];
        final float yi = in[1];
        final float zi = in[2];
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                synchronized (runningLock) {
                    if (running) {
                        return;
                    }
                    running = true;
                }
                output.format(Locale.US, "%f/%f/%f\n", xi, yi, zi);

                String response;
                try {
                    response = input.readLine();
                } catch (Exception e) {
                    synchronized (runningLock) {
                        running = false;
                    }
                    return;
                }

                synchronized (okLock) {
                    ok = false;
                    if (response != null) {
                        String[] parts = response.split("/");
                        if (parts.length > 2) {
                            try {
                                x = Float.parseFloat(parts[0]);
                                y = Float.parseFloat(parts[1]);
                                z = Float.parseFloat(parts[2]);
                                ok = true;
                            } catch (Exception e) {
                            }
                        }
                    }
                }
                synchronized (runningLock) {
                    running = false;
                }

            };
        };
        Thread t = new Thread(runnable);
        t.start();

        boolean retval;
        synchronized (okLock) {
            retval = ok;
            if (ok) {
                out[0] = x;
                out[1] = y;
                out[2] = z;
                ok = false;
            }
        }
        return retval;
    }
}
