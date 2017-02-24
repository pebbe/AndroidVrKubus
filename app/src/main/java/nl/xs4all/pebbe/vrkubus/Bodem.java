package nl.xs4all.pebbe.vrkubus;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import static java.lang.Math.PI;

public class Bodem {

    private final static int ARRAY_SIZE = 6 * 2;

    private FloatBuffer vertexBuffer;
    private final int mProgram;
    private int mPositionHandle;
    private int mMatrixHandle;
    private int mModusHandle;
    private int texture;

    private final String vertexShaderCode = "" +
            "uniform mat4 uMVPMatrix;" +
            "attribute vec2 position;" +
            "varying vec2 pos;" +
            "void main() {" +
            "    gl_Position = uMVPMatrix * vec4(40.0 * position[0] - 20.0, -4.0, 40.0 * position[1] - 20.0, 1.0);" +
            "    pos = position;" +
            "}";

    private final String fragmentShaderCode = "" +
            "precision mediump float;" +
            "uniform sampler2D texture;" +
            "uniform int modus;" +
            "varying vec2 pos;" +
            "void main() {" +
            "    gl_FragColor = texture2D(texture, pos);" +
            "    if (modus == 0) {" +
            "      gl_FragColor[0] = min(1.4 * gl_FragColor[0], 1.0);" +
            "    } else if (modus == 1) {" +
            "      gl_FragColor[0] = 0.6 * gl_FragColor[0];" +
            "      gl_FragColor[1] = min(1.2 * gl_FragColor[1], 1.0);" +
            "    } else if (modus == 2) {" +
            "      gl_FragColor[0] = 0.6 * gl_FragColor[0];" +
            "      gl_FragColor[2] = min(1.4 * gl_FragColor[2], 1.0);" +
            "    }" +
            "}";

    static final int COORDS_PER_VERTEX = 2;
    static float Coords[] = new float[ARRAY_SIZE];
    private int vertexCount;
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    private void punt(float x, float z) {
        Coords[COORDS_PER_VERTEX * vertexCount + 0] = x;
        Coords[COORDS_PER_VERTEX * vertexCount + 1] = z;
        vertexCount += 1;
    }

    public Bodem(Context context, int texturename) {
        texture = texturename;
        vertexCount = 0;

        punt(0, 0);
        punt(1, 0);
        punt(0, 1);
        punt(0, 1);
        punt(1, 0);
        punt(1, 1);

        //Log.i("MYTAG", "vertexCount: " + vertexCount);
        //Log.i("MYTAG", "ARRAY_SIZE: " + ARRAY_SIZE);

        ByteBuffer bb = ByteBuffer.allocateDirect(ARRAY_SIZE * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(Coords);
        vertexBuffer.position(0);

        int vertexShader = Util.loadShader(
                GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = Util.loadShader(
                GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        mProgram = GLES20.glCreateProgram();             // create empty OpenGL Program
        GLES20.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
        Util.checkGlError("glAttachShader vertexShader");
        GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
        Util.checkGlError("glAttachShader fragmentShader");
        GLES20.glLinkProgram(mProgram);                  // create OpenGL program executables
        Util.checkGlError("glLinkProgram");

        // Temporary create a bitmap
        Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), R.raw.wereld);

        // Bind texture to texturename
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        Util.checkGlError("glActiveTexture");
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture);
        Util.checkGlError("glBindTexture");

        // Load the bitmap into the bound texture.
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bmp, 0);
        Util.checkGlError("texImage2D");

        // We are done using the bitmap so we should recycle it.
        bmp.recycle();
    }

    public void draw(float[] mvpMatrix, int modus) {
        // Add program to OpenGL environment
        GLES20.glUseProgram(mProgram);
        Util.checkGlError("glUseProgram");

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        Util.checkGlError("glActiveTexture");

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture);
        Util.checkGlError("glBindTexture");

        // Set filtering
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        Util.checkGlError("glTexParameteri");

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        Util.checkGlError("glTexParameteri");

        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "position");
        Util.checkGlError("glGetAttribLocation vPosition");
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        Util.checkGlError("glEnableVertexAttribArray position");
        GLES20.glVertexAttribPointer(
                mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);
        Util.checkGlError("glVertexAttribPointer position");

        mMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        Util.checkGlError("glGetUniformLocation uMVPMatrix");
        GLES20.glUniformMatrix4fv(mMatrixHandle, 1, false, mvpMatrix, 0);
        Util.checkGlError("glUniformMatrix4fv uMVPMatrix");

        mModusHandle = GLES20.glGetUniformLocation(mProgram, "modus");
        Util.checkGlError("glGetUniformLocation modus");
        GLES20.glUniform1i(mModusHandle, modus);
        Util.checkGlError("glUniformMatrix4fv modus");

        // Get handle to textures locations
        int mSamplerLoc = GLES20.glGetUniformLocation(mProgram, "texture");
        Util.checkGlError("glGetUniformLocation texture");
        // Set the sampler texture unit to 0, where we have saved the texture.
        GLES20.glUniform1i(mSamplerLoc, 0);
        Util.checkGlError("glUniform1i mSamplerLoc");

        // Draw
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);
        Util.checkGlError("glDrawArrays");

        // Disable vertex arrays
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        Util.checkGlError("glDisableVertexAttribArray mPositionHandle");
    }
}
