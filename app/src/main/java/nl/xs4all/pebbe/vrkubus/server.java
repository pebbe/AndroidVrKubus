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
    private boolean err = false;
    private String ErrStr = "";
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
                    socket.setSoTimeout(1000);
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
    public String getError() {
        return ErrStr;
    }

    @Override
    public int forward(float[] out, float[] in) {
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
                    synchronized (xyzokLock) {
                        err = true;
                        ErrStr = e.toString();
                    }
                    return;
                }

                synchronized (xyzokLock) {
                    ok = false;
                    if (response == null) {
                        err = true;
                        ErrStr = "No response from remote server";
                    } else {
                        String[] parts = response.trim().split("[ \t]+");
                        if (parts.length == 3) {
                            try {
                                x = Float.parseFloat(parts[0]);
                                y = Float.parseFloat(parts[1]);
                                z = Float.parseFloat(parts[2]);
                                ok = true;
                            } catch (Exception e) {
                                // shouldn't happen
                                err = true;
                                ErrStr = e.toString();
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

        int retval = Util.stNIL;
        synchronized (xyzokLock) {
            if (err) {
                retval = Util.stERROR;
                err = false;
            } else if (ok) {
                out[0] = x;
                out[1] = y;
                out[2] = z;
                retval = Util.stOK;
            }
            ok = false;
        }
        return retval;
    }
}
