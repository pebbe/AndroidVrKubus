package nl.xs4all.pebbe.vrkubus;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.Bundle;
import android.util.Log;

import com.google.vr.sdk.base.Eye;
import com.google.vr.sdk.base.GvrActivity;
import com.google.vr.sdk.base.GvrView;
import com.google.vr.sdk.base.HeadTransform;
import com.google.vr.sdk.base.Viewport;

import javax.microedition.khronos.egl.EGLConfig;

public class MainActivity extends GvrActivity implements GvrView.StereoRenderer {

    //private Globe1 globe;
    private Kubus1 kubus;

    protected float[] modelCube;
    private float[] camera;
    private float[] view;
    private float[] modelViewProjection;
    private float[] modelView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initializeGvrView();

        // Initialize other objects here.
        modelCube = new float[16];
        camera = new float[16];
        view = new float[16];
        modelViewProjection = new float[16];
        modelView = new float[16];
    }

    @Override
    public void onSurfaceCreated(EGLConfig config) {
        Matrix.setIdentityM(modelCube, 0);
        // Matrix.translateM(modelCube, 0, 0, 0, -3.0f);

        Matrix.setLookAtM(camera, 0,
                0.0f, 0.0f, 5.0f /* 0.01f */,
                0.0f, 0.0f, 0.0f,
                0.0f, 1.0f, 0.0f);

        kubus = new Kubus1(this);
        //globe = new Globe1();

    }

    @Override
    public void onNewFrame(HeadTransform headTransform) {
    }

    @Override
    public void onDrawEye(Eye eye) {
        //float c = 0.6f;
        //GLES20.glClearColor(c * 0.27f, c * 0.35f, c * 0.39f, 1.0f);
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glCullFace(GLES20.GL_BACK);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        log("camera", camera);
        log("modelCube", modelCube);

        // Apply the eye transformation to the camera.
        Matrix.multiplyMM(view, 0, eye.getEyeView(), 0, camera, 0);

        log("view", view);

        // Build the ModelView and ModelViewProjection matrices
        // for calculating cube position and light.
        float[] perspective = eye.getPerspective(0.1f, 10.0f);
        log("perspective", perspective);
        Matrix.multiplyMM(modelView, 0, view, 0, modelCube, 0);
        log("modelView", modelView);
        Matrix.multiplyMM(modelViewProjection, 0, perspective, 0, modelView, 0);
        log("modelViewProjection", modelViewProjection);

        //globe.draw(modelViewProjection);
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
        //gvrView.enableCardboardTriggerEmulation();

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

    private void log(String s, float[] m) {
        /*
        Log.i("MYTAG", s + " [ [ " + m[0] + " " + m[4] + " " + m[8] + " " + m[12] + " ]");
        Log.i("MYTAG", s + "   [ " + m[1] + " " + m[5] + " " + m[9] + " " + m[13] + " ]");
        Log.i("MYTAG", s + "   [ " + m[2] + " " + m[6] + " " + m[10] + " " + m[14] + " ]");
        Log.i("MYTAG", s + "   [ " + m[3] + " " + m[7] + " " + m[11] + " " + m[15] + " ] ]");
        */
    }

}
