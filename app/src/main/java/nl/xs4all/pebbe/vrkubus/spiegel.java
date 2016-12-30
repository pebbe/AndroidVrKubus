package nl.xs4all.pebbe.vrkubus;


public class spiegel implements MainActivity.Provider {
    @Override
    public boolean forward(float[] out, float[] in) {
        out[0] = -in[0];
        out[1] = in[1];
        out[2] = in[2];
        return true;
    }
}
