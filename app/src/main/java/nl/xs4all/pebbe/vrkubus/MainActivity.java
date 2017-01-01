package nl.xs4all.pebbe.vrkubus;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.Bundle;

import com.google.vr.sdk.base.Eye;
import com.google.vr.sdk.base.GvrActivity;
import com.google.vr.sdk.base.GvrView;
import com.google.vr.sdk.base.HeadTransform;
import com.google.vr.sdk.base.Viewport;

import javax.microedition.khronos.egl.EGLConfig;

public class MainActivity extends GvrActivity implements GvrView.StereoRenderer {

    private Kubus kubus;
    private Wereld wereld;

    protected float[] modelCube;
    protected float[] modelWorld;
    private float[] camera;
    private float[] view;
    private float[] modelViewProjection;
    private float[] modelView;
    private float[] forward;
    private float[] other;
    private Provider provider;
    private float ox;
    private float oy;
    private float oz;
    private int modus;

    public interface Provider {
        public boolean forward(float[] out, float[] in);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initializeGvrView();

        // Initialize other objects here.
        modelCube = new float[16];
        modelWorld = new float[16];
        camera = new float[16];
        view = new float[16];
        modelViewProjection = new float[16];
        modelView = new float[16];
        forward = new float[3];
        other = new float[3];
        provider = new vertraagd();
    }

    @Override
    public void onSurfaceCreated(EGLConfig config) {
        Matrix.setLookAtM(camera, 0,
                0.0f, 0.0f, 0.01f,  // 0.01f
                0.0f, 0.0f, 0.0f,
                0.0f, 1.0f, 0.0f);

        kubus = new Kubus(this);
        wereld = new Wereld(this);

        ox = 0;
        oy = 0;
        oz = -1;
        modus = 0;
    }

    @Override
    public void onNewFrame(HeadTransform headTransform) {
        Matrix.setIdentityM(modelCube, 0);
        Matrix.setIdentityM(modelWorld, 0);

        headTransform.getForwardVector(forward, 0);
        Matrix.translateM(modelWorld, 0, 7.0f * forward[0], 7.0f * forward[1], 7.0f * forward[2]);

        // is dit nodig?
        float f = (float) Math.sqrt((double) (forward[0] * forward[0] + forward[1] * forward[1] + forward[2] * forward[2]));
        forward[0] = forward[0] / f;
        forward[1] = forward[1] / f;
        forward[2] = forward[2] / f;

        if (provider.forward(other, forward)) {
            ox = other[0];
            oy = other[1];
            oz = other[2];
        }

        if (modus == 0) {

            Matrix.translateM(modelCube, 0, 7.0f * forward[0], 7.0f * forward[1], 7.0f * forward[2]);

            float roth = (float) Math.atan2((double) forward[0], (double) forward[2]);
            Matrix.rotateM(modelCube, 0, roth / (float) Math.PI * 180.0f, 0, 1, 0);
            float rotv = (float) Math.atan2(forward[1], Math.sqrt(forward[0] * forward[0] + forward[2] * forward[2]));
            Matrix.rotateM(modelCube, 0, -rotv / (float) Math.PI * 180.0f, 1, 0, 0);

            rotv = (float) Math.atan2(oy, Math.sqrt(ox * ox + oz * oz));
            Matrix.rotateM(modelCube, 0, rotv / (float) Math.PI * 180.0f, 1, 0, 0);
            roth = (float) Math.atan2(ox, oz);
            Matrix.rotateM(modelCube, 0, -roth / (float) Math.PI * 180.0f, 0, 1, 0);

        } else if (modus == 1) {

            Matrix.translateM(modelCube, 0, 7.0f * forward[0], 7.0f * forward[1], 7.0f * forward[2]);

            float roth = (float) Math.atan2(ox, oz);
            Matrix.rotateM(modelCube, 0, roth / (float) Math.PI * 180.0f, 0, 1, 0);
            float rotv = (float) Math.atan2(oy, Math.sqrt(ox * ox + oz * oz));
            Matrix.rotateM(modelCube, 0, -rotv / (float) Math.PI * 180.0f, 1, 0, 0);

        } else {

            Matrix.translateM(modelCube, 0, 7.0f * ox, 7.0f * oy, 7.0f * oz);

        }
    }

    @Override
    public void onDrawEye(Eye eye) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // Apply the eye transformation to the camera.
        Matrix.multiplyMM(view, 0, eye.getEyeView(), 0, camera, 0);

        // Build the ModelView and ModelViewProjection matrices
        // for calculating cube position and light.
        float[] perspective = eye.getPerspective(0.1f, 100.0f);

        Matrix.multiplyMM(modelView, 0, view, 0, modelWorld, 0);
        Matrix.multiplyMM(modelViewProjection, 0, perspective, 0, modelView, 0);

        GLES20.glDisable(GLES20.GL_CULL_FACE);
        wereld.draw(modelViewProjection);

        Matrix.multiplyMM(modelView, 0, view, 0, modelCube, 0);
        Matrix.multiplyMM(modelViewProjection, 0, perspective, 0, modelView, 0);

        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glCullFace(GLES20.GL_BACK);
        kubus.draw(modelViewProjection);
    }

    @Override
    public void onRendererShutdown() {

    }

    @Override
    public void onSurfaceChanged(int i, int i1) {

    }

    @Override
    public void onFinishFrame(Viewport viewport) {

    }

    public void initializeGvrView() {
        setContentView(R.layout.common_ui);

        GvrView gvrView = (GvrView) findViewById(R.id.gvr_view);
        gvrView.setEGLConfigChooser(8, 8, 8, 8, 16, 8);

        gvrView.setRenderer(this);
        //gvrView.setTransitionViewEnabled(true);

        // Enable Cardboard-trigger feedback with Daydream headsets. This is a simple way of supporting
        // Daydream controller input for basic interactions using the existing Cardboard trigger API.
        gvrView.enableCardboardTriggerEmulation();

        /*
        if (gvrView.setAsyncReprojectionEnabled(true)) {
            // Async reprojection decouples the app framerate from the display framerate,
            // allowing immersive interaction even at the throttled clockrates set by
            // sustained performance mode.
            AndroidCompat.setSustainedPerformanceMode(this, true);
        }
        */

        setGvrView(gvrView);
    }

    @Override
    public void onCardboardTrigger() {
        modus = (modus + 1) % 3;
    }

    private void log(String s, float[] m) {
        /*
        Log.i("MYTAG", s + " [ [ " + m[0] + " " + m[4] + " " + m[8] + " " + m[12] + " ]");
        Log.i("MYTAG", s + "   [ " + m[1] + " " + m[5] + " " + m[9] + " " + m[13] + " ]");
        Log.i("MYTAG", s + "   [ " + m[2] + " " + m[6] + " " + m[10] + " " + m[14] + " ]");
        Log.i("MYTAG", s + "   [ " + m[3] + " " + m[7] + " " + m[11] + " " + m[15] + " ] ]");
        */
    }

}
