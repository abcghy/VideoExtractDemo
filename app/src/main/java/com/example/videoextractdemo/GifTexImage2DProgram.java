package com.example.videoextractdemo;

import android.graphics.RectF;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.util.Log;

import org.intellij.lang.annotations.Language;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

class GifTexImage2DProgram {
    private int program = 0;
    private int positionLocation = -1;
    private int textureLocation = -1;
//    private int maskTextureLocation = -1;
    private int coordinateLocation = -1;

    @Language("GLSL")
    private String vertexShaderCode =
            "precision mediump float;" +
                    "attribute vec4 position;" +
                    "attribute vec4 coordinate;" +
                    "varying vec2 textureCoordinate;" +
                    "void main() {" +
                    "    gl_Position = position;" +
                    "    textureCoordinate = vec2(coordinate);" +
                    "}";

    @Language("GLSL")
    private String fragmentShaderCode =
            "#extension GL_OES_EGL_image_external : require\n" +
            "precision mediump float;" +
                    "varying mediump vec2 textureCoordinate;" +
                    "uniform sampler2D texture;" +
                    "void main() {" +
                    "     vec4 baseColor = texture2D(texture, textureCoordinate);" +
                    "     gl_FragColor = vec4(baseColor.rgb, baseColor.a);" +
                    "}";

    private float[] texturePoints = {0f, 0f, 1f, 0f, 0f, 1f, 1f, 1f};
    private FloatBuffer textureBuffer;
    private float[] verticesPoints = {-1f, -1f, 1f, -1f, -1f, 1f, 1f, 1f};
    private FloatBuffer verticesBuffer;

    private int bgWidth;
    private int bgHeight;

    public GifTexImage2DProgram() {
        textureBuffer = ByteBuffer.allocateDirect(texturePoints.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        textureBuffer.put(texturePoints)
                .rewind();

        verticesBuffer = ByteBuffer.allocateDirect(verticesPoints.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        verticesBuffer.put(verticesPoints)
                .rewind();
    }

    public void initialize() {
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int pixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);
        program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, vertexShader);
        GLES20.glAttachShader(program, pixelShader);
        GLES20.glLinkProgram(program);

        int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] == GLES20.GL_FALSE) {
            String result = GLES20.glGetProgramInfoLog(program);
            Log.e("TESTTT", "result: " + result);
        }

        GLES20.glDeleteShader(pixelShader);
        GLES20.glDeleteShader(vertexShader);
        positionLocation = GLES20.glGetAttribLocation(program, "position");
        textureLocation = GLES20.glGetUniformLocation(program, "texture");
//        maskTextureLocation = GLES20.glGetUniformLocation(program, "maskTexture");
        coordinateLocation = GLES20.glGetAttribLocation(program, "coordinate");

        GLES20.glUseProgram(program);
        // base texture
        GLES20.glUniform1i(textureLocation, 0);
        // mask texture
//        GLES20.glUniform1i(maskTextureLocation, 1);
    }

    private int mTexId;

    public int getTexId() {
        return mTexId;
    }

    public void onRecordChanged() {
//        // rebind the textures
//        if (texNames != null) {
//            GLES20.glDeleteTextures(texNames.length, texNames, 0);
//            texNames = null;
//        }
//        ImageEditRecord imageEditRecord = iImageRenderView.getLastRecord();
//        this.stickers = imageEditRecord.getStickers();
//        this.bgWidth = imageEditRecord.getBgWidth();
//        this.bgHeight = imageEditRecord.getBgHeight();
//
//        if (stickers.size() != 0) {
//            texNames = new int[stickers.size() * 2];
//            GLES20.glGenTextures(stickers.size() * 2, texNames, 0);
//        }
//
//        for (int index = 0; index < stickers.size(); index++) {
//            StickerDefault sticker = stickers.get(index);
//            sticker.setTextureOrder(index);
//

        int[] texNames = new int[1];
        GLES20.glGenTextures(1, texNames, 0);
        mTexId = texNames[0];

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTexId);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
//        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA,
//                sticker.getImageCache().getWidth(), sticker.getImageCache().getHeight(),
//                0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
    }

    private RectF bgRect = new RectF();

    private int viewWidth;
    private int viewHeight;

    public void setDimensions(int width, int height) {
        this.viewWidth = width;
        this.viewHeight = height;
    }

    public void draw() {
        GLES20.glClearColor(0, 0, 0, 0);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        try {

            GLES20.glViewport((int) bgRect.left, viewHeight - (int) bgRect.bottom,
                    (int) bgRect.width(), (int) bgRect.height());

            GLES20.glEnableVertexAttribArray(coordinateLocation);
            GLES20.glEnableVertexAttribArray(positionLocation);
            GLES20.glVertexAttribPointer(coordinateLocation, 2, GLES20.GL_FLOAT, false, 0, textureBuffer);

            verticesBuffer.rewind();
            verticesBuffer.put(verticesPoints);
            verticesBuffer.rewind();

            GLES20.glVertexAttribPointer(positionLocation, 2, GLES20.GL_FLOAT, false, 0, verticesBuffer);

            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTexId);
//            if (sticker.getImageCache().isDynamic()) {
//                ((WebpImageCache) sticker.getImageCache()).getWebpPlayer().glTexSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, stickerWidth, stickerHeight);
//            } else {
//                GLUtils.texSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, sticker.getContentBitmap());
//            }

//            GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
//            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texNames[2 * sticker.getTextureOrder() + 1]);
//            GLUtils.texSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, sticker.getMaskBitmap());

            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void destroy() {
        // delete textures
//        if (texNames != null) {
//            GLES20.glDeleteTextures(texNames.length, texNames, 0);
//            texNames = null;
//        }
        GLES20.glDeleteTextures(1, new int[mTexId], 0);

        GLES20.glDeleteProgram(program);
        textureBuffer = null;
        verticesBuffer = null;
    }

    public static int loadShader(int shaderType, String source) {
        int shader = GLES20.glCreateShader(shaderType);
        GLES20.glShaderSource(shader, source);
        GLES20.glCompileShader(shader);

        int[] compileStatus = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0);
        if (compileStatus[0] == GLES20.GL_FALSE) {
            String result = GLES20.glGetShaderInfoLog(shader);
            Log.e("TESTTT", "type: " + shaderType + "result: " + result);
        }
        return shader;
    }
}