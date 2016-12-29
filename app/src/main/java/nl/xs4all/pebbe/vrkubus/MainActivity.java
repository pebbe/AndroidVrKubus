package nl.xs4all.pebbe.vrkubus;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.Bundle;
import android.util.Log;

import com.google.vr.sdk.base.AndroidCompat;
import com.google.vr.sdk.base.Eye;
import com.google.vr.sdk.base.GvrActivity;
import com.google.vr.sdk.base.GvrView;
import com.google.vr.sdk.base.HeadTransform;
import com.google.vr.sdk.base.Viewport;

import javax.microedition.khronos.egl.EGLConfig;

public class MainActivity extends GvrActivity implements GvrView.StereoRenderer {

    private Kubus kubus;

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
        Matrix.translateM(modelCube, 0, 0, 0, -3.0f); // TODO ???
        Log.i("MYTAG", "[ [ " + modelCube[0] + " " + modelCube[4] + " " + modelCube[8] + " " + modelCube[12] + " ]");
        Log.i("MYTAG", "  [ " + modelCube[1] + " " + modelCube[5] + " " + modelCube[9] + " " + modelCube[13] + " ]");
        Log.i("MYTAG", "  [ " + modelCube[2] + " " + modelCube[6] + " " + modelCube[10] + " " + modelCube[14] + " ]");
        Log.i("MYTAG", "  [ " + modelCube[3] + " " + modelCube[7] + " " + modelCube[11] + " " + modelCube[15] + " ] ]");

        Matrix.setLookAtM(camera, 0,
                0.0f, 0.0f, /*5.0f */ 0.01f,
                0.0f, 0.0f, 0.0f,
                0.0f, 1.0f, 0.0f);
        Log.i("MYTAG", "[ [ " + camera[0] + " " + camera[4] + " " + camera[8] + " " + camera[12] + " ]");
        Log.i("MYTAG", "  [ " + camera[1] + " " + camera[5] + " " + camera[9] + " " + camera[13] + " ]");
        Log.i("MYTAG", "  [ " + camera[2] + " " + camera[6] + " " + camera[10] + " " + camera[14] + " ]");
        Log.i("MYTAG", "  [ " + camera[3] + " " + camera[7] + " " + camera[11] + " " + camera[15] + " ] ]");


        kubus = new Kubus(this);
    }

    @Override
    public void onNewFrame(HeadTransform headTransform) {
    }

    @Override
    public void onDrawEye(Eye eye) {
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // Apply the eye transformation to the camera.
        Matrix.multiplyMM(view, 0, eye.getEyeView(), 0, camera, 0);

        // Build the ModelView and ModelViewProjection matrices
        // for calculating cube position and light.
        float[] perspective = eye.getPerspective(0.1f, 10.0f);
        Matrix.multiplyMM(modelView, 0, view, 0, modelCube, 0);
        Matrix.multiplyMM(modelViewProjection, 0, perspective, 0, modelView, 0);
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
        gvrView.setTransitionViewEnabled(true);

        // Enable Cardboard-trigger feedback with Daydream headsets. This is a simple way of supporting
        // Daydream controller input for basic interactions using the existing Cardboard trigger API.
        gvrView.enableCardboardTriggerEmulation();

        if (gvrView.setAsyncReprojectionEnabled(true)) {
            // Async reprojection decouples the app framerate from the display framerate,
            // allowing immersive interaction even at the throttled clockrates set by
            // sustained performance mode.
            AndroidCompat.setSustainedPerformanceMode(this, true);
        }

        setGvrView(gvrView);
    }
}
