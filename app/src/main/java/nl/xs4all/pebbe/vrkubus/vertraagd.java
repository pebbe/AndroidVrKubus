package nl.xs4all.pebbe.vrkubus;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.DelayQueue;

public class vertraagd implements MainActivity.Provider {

    private long delay = 1000;
    private BlockingQueue queue;

    public vertraagd(long d) {
        delay = d;
        queue = new DelayQueue();
    }

    @Override
    public boolean forward(float[] out, float[] in) {
        DelayObject object = new DelayObject(in[0], in[1], in[2], delay);
        try {
            queue.put(object);
        } catch (Exception ex) {
        }

        object = (DelayObject) queue.poll();
        if (object == null) {
            return false;
        }

        while (queue.poll() != null) {

        }

        out[0] = object.x;
        out[1] = object.y;
        out[2] = object.z;
        return true;
    }
}
