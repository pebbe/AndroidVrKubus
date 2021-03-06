package nl.xs4all.pebbe.vrkubus;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class Kubus {

    private final static int ARRAY_SIZE = 6 * 6 * 3;

    private FloatBuffer coordsBuffer;
    private FloatBuffer colorsBuffer;
    private final int mProgram;
    private int mPositionHandle;
    private int mColorHandle;
    private int texture;

    private final String vertexShaderCode = "" +
            "uniform mat4 uMVPMatrix;" +
            "attribute vec3 position;" +
            "attribute vec3 color;" +
            "varying vec3 col;" +
            "void main() {" +
            "    gl_Position = uMVPMatrix * vec4(position, 1);" +
            "    col = color;" +
            "}";

    private final String fragmentShaderCode = "" +
            "precision mediump float;" +
            "uniform sampler2D texture;" +
            "varying vec3 col;" +
            "void main() {" +
            "    gl_FragColor = col[2] * texture2D(texture, vec2(col[0], col[1]));" +
            "}";

    static final int COORDS_PER_VERTEX = 3;
    static float Coords[] = new float[ARRAY_SIZE];
    private final int coordStride = COORDS_PER_VERTEX * 4; // 4 bytes per float

    static final int COLORS_PER_VERTEX = 3;
    static float Colors[] = new float[ARRAY_SIZE];
    private final int colorStride = COLORS_PER_VERTEX * 4; // 4 bytes per float

    private int vertexCount;

    private void punt(float x, float y, float z, float xi, float yi, float c) {
        Coords[COORDS_PER_VERTEX * vertexCount + 0] = x;
        Coords[COORDS_PER_VERTEX * vertexCount + 1] = y;
        Coords[COORDS_PER_VERTEX * vertexCount + 2] = z;
        Colors[COLORS_PER_VERTEX * vertexCount + 0] = xi;
        Colors[COLORS_PER_VERTEX * vertexCount + 1] = yi;
        Colors[COLORS_PER_VERTEX * vertexCount + 2] = c;
        vertexCount ++;
    }

    public Kubus(Context context, int texturename) {
        texture = texturename;
        vertexCount = 0;

        // boven 6
        punt(-1, 1, -1, 0, 0, 1);
        punt(-1, 1, 1, 0, 1, 1);
        punt(1, 1, 1, 1, 1, 1);
        punt(-1, 1, -1, 0, 0, 1);
        punt(1, 1, 1, 1, 1, 1);
        punt(1, 1, -1, 1, 0, 1);

        // links 2
        punt(-1, 1, -1, 0, 0, .85f);
        punt(-1, -1, -1, 0, 1, .85f);
        punt(-1, -1, 1, 1, 1, .85f);
        punt(-1, 1, -1, 0, 0, .85f);
        punt(-1, -1, 1, 1, 1, .85f);
        punt(-1, 1, 1, 1, 0, .85f);

        // voor 4
        punt(-1, 1, 1, 0, 0, .7f);
        punt(-1, -1, 1, 0, 1, .7f);
        punt(1, -1, 1, 1, 1, .7f);
        punt(-1, 1, 1, 0, 0, .7f);
        punt(1, -1, 1, 1, 1, .7f);
        punt(1, 1, 1, 1, 0, .7f);

        // rechts 5
        punt(1, 1, 1, 0, 0, .4f);
        punt(1, -1, 1, 0, 1, .4f);
        punt(1, -1, -1, 1, 1, .4f);
        punt(1, 1, 1, 0, 0, .4f);
        punt(1, -1, -1, 1, 1, .4f);
        punt(1, 1, -1, 1, 0, .4f);

        // achter 3
        punt(1, 1, -1, 0, 0, .55f);
        punt(1, -1, -1, 0, 1, .55f);
        punt(-1, -1, -1, 1, 1, .55f);
        punt(1, 1, -1, 0, 0, .55f);
        punt(-1, -1, -1, 1, 1, .55f);
        punt(-1, 1, -1, 1, 0, .55f);

        // onder 1
        punt(-1, -1, 1, 0, 0, .25f);
        punt(-1, -1, -1, 0, 1, .25f);
        punt(1, -1, -1, 1, 1, .25f);
        punt(-1, -1, 1, 0, 0, .25f);
        punt(1, -1, -1, 1, 1, .25f);
        punt(1, -1, 1, 1, 0, .25f);


        ByteBuffer b1 = ByteBuffer.allocateDirect(ARRAY_SIZE * 4);
        b1.order(ByteOrder.nativeOrder());
        coordsBuffer = b1.asFloatBuffer();
        coordsBuffer.put(Coords);
        coordsBuffer.position(0);

        ByteBuffer b2 = ByteBuffer.allocateDirect(ARRAY_SIZE * 4);
        b2.order(ByteOrder.nativeOrder());
        colorsBuffer = b2.asFloatBuffer();
        colorsBuffer.put(Colors);
        colorsBuffer.position(0);

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
        Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), R.raw.beton);

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

    public void draw(float[] mvpMatrix) {
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

        GLES20.glDisable(GLES20.GL_BLEND);

        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "position");
        Util.checkGlError("glGetAttribLocation position");
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        Util.checkGlError("glEnableVertexAttribArray position");
        GLES20.glVertexAttribPointer(
                mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                coordStride, coordsBuffer);
        Util.checkGlError("glVertexAttribPointer position");

        mColorHandle = GLES20.glGetAttribLocation(mProgram, "color");
        Util.checkGlError("glGetAttribLocation color");
        GLES20.glEnableVertexAttribArray(mColorHandle);
        Util.checkGlError("glEnableVertexAttribArray color");
        GLES20.glVertexAttribPointer(
                mColorHandle, COLORS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                colorStride, colorsBuffer);
        Util.checkGlError("glVertexAttribPointer color");

        int mMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        Util.checkGlError("glGetUniformLocation uMVPMatrix");
        GLES20.glUniformMatrix4fv(mMatrixHandle, 1, false, mvpMatrix, 0);
        Util.checkGlError("glUniformMatrix4fv uMVPMatrix");

        // Get handle to textures locations
        int mSamplerLoc = GLES20.glGetUniformLocation (mProgram, "texture" );
        Util.checkGlError("glGetUniformLocation texture");
        // Set the sampler texture unit to 0, where we have saved the texture.
        GLES20.glUniform1i(mSamplerLoc, 0);
        Util.checkGlError("glUniform1i mSamplerLoc");

        // Draw
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);
        Util.checkGlError("glDrawArrays");

        // Disable vertex arrays
        GLES20.glDisableVertexAttribArray(mColorHandle);
        Util.checkGlError("glDisableVertexAttribArray colorHandle");
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        Util.checkGlError("glDisableVertexAttribArray positionHandle");
    }
}
