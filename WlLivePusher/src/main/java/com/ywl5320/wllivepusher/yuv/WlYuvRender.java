package com.ywl5320.wllivepusher.yuv;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import com.ywl5320.wllivepusher.R;
import com.ywl5320.wllivepusher.egl.WLEGLSurfaceView;
import com.ywl5320.wllivepusher.egl.WlShaderUtil;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class WlYuvRender implements WLEGLSurfaceView.WlGLRender{


    private Context context;
    private FloatBuffer vertexBuffer;
    private final float[] vertexData = {
            1f,1f,
            -1f,1f,
            1f,-1f,
            -1f,-1f
    };

    private FloatBuffer textureBuffer;
    private final float[] textureVertexData = {
            1f,0f,
            0f,0f,
            1f,1f,
            0f,1f
    };

    private int program;
    private int vPosition;
    private int fPosition;

    private int sampler_y;
    private int sampler_u;
    private int sampler_v;

    private int [] texture_yuv;

    private int fboId;
    private int textureid;

    int w;
    int h;

    Buffer y;
    Buffer u;
    Buffer v;

    private WlYuvFboRender wlYuvFboRender;

    private float[] matrix = new float[16];
    private int u_matrix;

    public WlYuvRender(Context context) {
        this.context = context;
        wlYuvFboRender = new WlYuvFboRender(context);
        vertexBuffer = ByteBuffer.allocateDirect(vertexData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(vertexData);
        vertexBuffer.position(0);

        textureBuffer = ByteBuffer.allocateDirect(textureVertexData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(textureVertexData);
        textureBuffer.position(0);
        Matrix.setIdentityM(matrix, 0);

    }

    @Override
    public void onSurfaceCreated() {
        wlYuvFboRender.onCreate();
        String vertexShader = WlShaderUtil.getRawResource(context, R.raw.vertex_shader_yuv);
        String fragmentShader = WlShaderUtil.getRawResource(context, R.raw.fragment_shader_yuv);
        program = WlShaderUtil.createProgram(vertexShader, fragmentShader);
        vPosition = GLES20.glGetAttribLocation(program, "v_Position");
        fPosition = GLES20.glGetAttribLocation(program, "f_Position");
        u_matrix = GLES20.glGetUniformLocation(program, "u_Matrix");

        sampler_y = GLES20.glGetUniformLocation(program, "sampler_y");
        sampler_u = GLES20.glGetUniformLocation(program, "sampler_u");
        sampler_v = GLES20.glGetUniformLocation(program, "sampler_v");

        texture_yuv = new int[3];
        GLES20.glGenTextures(3, texture_yuv, 0);

        for(int i = 0; i < 3; i++)
        {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture_yuv[i]);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        }

        int[] fbos = new int[1];
        GLES20.glGenBuffers(1, fbos, 0);
        fboId = fbos[0];
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fboId);

        int []textureIds = new int[1];
        GLES20.glGenTextures(1, textureIds, 0);
        textureid = textureIds[0];
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureid);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, 720, 500, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, textureid, 0);
        if(GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER) != GLES20.GL_FRAMEBUFFER_COMPLETE)
        {
            Log.e("ywl5320", "fbo wrong");
        }
        else
        {
            Log.e("ywl5320", "fbo success");
        }

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);


    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        Matrix.rotateM(matrix, 0, 180f, 1, 0, 0);
        GLES20.glViewport(0, 0, width, height);
        wlYuvFboRender.onChange(width, height);
    }

    @Override
    public void onDrawFrame() {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glClearColor(1f, 0f, 0f, 1f);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fboId);

        if(w > 0 && h > 0 && y != null && u != null && v != null)
        {
            GLES20.glUseProgram(program);
            GLES20.glUniformMatrix4fv(u_matrix, 1, false, matrix, 0);
            GLES20.glEnableVertexAttribArray(vPosition);
            GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false,
                    8, vertexBuffer);
            GLES20.glEnableVertexAttribArray(fPosition);
            GLES20.glVertexAttribPointer(fPosition,2,GLES20.GL_FLOAT,false,8, textureBuffer);

            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture_yuv[0]);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE, w, h, 0, GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, y);
            GLES20.glUniform1i(sampler_y, 0);

            GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture_yuv[1]);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE, w / 2, h / 2, 0, GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE,
                    u);
            GLES20.glUniform1i(sampler_u, 1);

            GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture_yuv[2]);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE, w / 2, h / 2, 0, GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE,
                    v);
            GLES20.glUniform1i(sampler_v, 2);

            y.clear();
            u.clear();
            v.clear();

            y = null;
            u = null;
            v = null;
        }
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        wlYuvFboRender.onDraw(textureid);


    }

    public void setFrameData(int w, int h, byte[] by, byte[] bu, byte[] bv)
    {
        this.w = w;
        this.h = h;
        this.y = ByteBuffer.wrap(by);
        this.u = ByteBuffer.wrap(bu);
        this.v = ByteBuffer.wrap(bv);

    }
}
