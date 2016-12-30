package nl.xs4all.pebbe.vrkubus;


public class dubbel implements MainActivity.Provider {
    @Override
    public boolean forward(float[] out, float[] in) {
        float roth = 2 * (float) Math.atan2((double) in[0], (double) in[2]);
        float rotv = (float) Math.atan2(in[1], Math.sqrt(in[0] * in[0] + in[2] * in[2]));

        out[0] = (float) (Math.sin(roth) * Math.cos(rotv));
        out[1] = (float) Math.sin(rotv);
        out[2] = (float) (Math.cos(roth) * Math.cos(rotv));

        return true;
    }
}
