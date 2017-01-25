package nl.xs4all.pebbe.vrkubus;

import android.content.Context;

import java.io.DataInputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Locale;

// TODO: check for wifi
// TODO: error handling

public class server2 implements MainActivity.Provider {

    private static final String address = "192.168.178.24";
    private static final int port = 8448;

    private static final int NR_OF_CONNECTIONS = 8;
    private int current = 0;

    private Socket[] sockets;
    private DataInputStream[] inputs;
    private PrintStream[] outputs;

    private float x;
    private float y;
    private float z;
    private boolean ok = false;
    final private Object xyzokLock = new Object();

    private boolean[] runnings;
    final private Object runningLock = new Object();

    public server2(Context context) {
        sockets = new Socket[NR_OF_CONNECTIONS];
        inputs = new DataInputStream[NR_OF_CONNECTIONS];
        outputs = new PrintStream[NR_OF_CONNECTIONS];
        runnings = new boolean[NR_OF_CONNECTIONS];
        for (int i = 0; i < NR_OF_CONNECTIONS; i++) {
            runnings[i] = true;
        }

        MyDBHandler handler = new MyDBHandler(context, null, null, 1);
        String value = handler.findSetting("uid");
        if (value.equals("")) {
            value = "" + System.currentTimeMillis();
            handler.addSetting("uid", value);
        }
        final String uid = value;

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < NR_OF_CONNECTIONS; i++) {
                    try {
                        sockets[i] = new Socket(address, port);
                        inputs[i] = new DataInputStream(sockets[i].getInputStream());
                        outputs[i] = new PrintStream(sockets[i].getOutputStream());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    outputs[i].format("VRC1.0 %s\n", uid);
                    synchronized (runningLock) {
                        runnings[i] = false;
                    }
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
        final int index = current;
        current = (current + 1) % NR_OF_CONNECTIONS;
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                synchronized (runningLock) {
                    if (runnings[index]) {
                        return;
                    }
                    runnings[index] = true;
                }
                outputs[index].format(Locale.US, "%f/%f/%f\n", xi, yi, zi);

                String response;
                try {
                    response = inputs[index].readLine(); // TODO deprecated
                } catch (Exception e) {
                    synchronized (runningLock) {
                        runnings[index] = false;
                    }
                    return;
                }

                synchronized (xyzokLock) {
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
                    runnings[index] = false;
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
