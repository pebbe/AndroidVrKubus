package nl.xs4all.pebbe.vrkubus;

import android.content.Context;

import java.io.DataInputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Locale;

public class server implements MainActivity.Provider {

    private static final String address = "192.168.178.24";
    private static final int port = 8448;

    private Socket socket;
    private DataInputStream input;
    private PrintStream output;

    private float x;
    private float y;
    private float z;
    private boolean ok = false;
    final private Object xyzokLock = new Object();

    private boolean running = false;
    final private Object runningLock = new Object();

    public server(Context context) {
        running = true;

        MyDBHandler handler = new MyDBHandler(context);
        String value = handler.findSetting(Util.kUid);
        if (value.equals("")) {
            value = "" + System.currentTimeMillis();
            handler.addSetting(Util.kUid, value);
        }
        final String uid = value;

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    socket = new Socket(address, port);
                    input = new DataInputStream(socket.getInputStream());
                    output = new PrintStream(socket.getOutputStream());
                    output.format("VRC1.0 %s\n", uid);
                    input.readLine();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                synchronized (runningLock) {
                    running = false;
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
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
                output.format(Locale.US, "%f %f %f\n", xi, yi, zi);

                String response;
                try {
                    response = input.readLine();
                } catch (Exception e) {
                    synchronized (runningLock) {
                        running = false;
                    }
                    return;
                }

                synchronized (xyzokLock) {
                    ok = false;
                    if (response != null) {
                        String[] parts = response.trim().split("[ \t]+");
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
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();

        boolean retval;
        synchronized (xyzokLock) {
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
