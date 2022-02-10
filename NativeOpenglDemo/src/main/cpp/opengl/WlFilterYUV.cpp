//
// Created by yangw on 2019-4-11.
//

#include "WlFilterYUV.h"

WlFilterYUV::WlFilterYUV() {

}

WlFilterYUV::~WlFilterYUV() {

}

void WlFilterYUV::onCreate() {

    vertex = "attribute vec4 v_Position;\n"
             "attribute vec2 f_Position;\n"
             "varying vec2 ft_Position;\n"
             "uniform mat4 u_Matrix;\n"
             "void main() {\n"
             "    ft_Position = f_Position;\n"
             "    gl_Position = v_Position * u_Matrix;\n"
             "}";

    fragment = "precision mediump float;\n"
               "varying vec2 ft_Position;\n"
               "uniform sampler2D sampler_y;\n"
               "uniform sampler2D sampler_u;\n"
               "uniform sampler2D sampler_v;\n"
               "void main() {\n"
               "   float y,u,v;\n"
               "   y = texture2D(sampler_y,ft_Position).r;\n"
               "   u = texture2D(sampler_u,ft_Position).r - 0.5;\n"
               "   v = texture2D(sampler_v,ft_Position).r - 0.5;\n"
               "\n"
               "   vec3 rgb;\n"
               "   rgb.r = y + 1.403 * v;\n"
               "   rgb.g = y - 0.344 * u - 0.714 * v;\n"
               "   rgb.b = y + 1.770 * u;\n"
               "\n"
               "   gl_FragColor = vec4(rgb,1);\n"
               "}";

    program = createProgrm(vertex, fragment, &vShader, &fShader);
    LOGD("opengl program is %d %d %d", program, vShader, fShader);
    vPosition = glGetAttribLocation(program, "v_Position");//顶点坐标
    fPosition = glGetAttribLocation(program, "f_Position");//纹理坐标
    sampler_y = glGetUniformLocation(program, "sampler_y");
    sampler_u = glGetUniformLocation(program, "sampler_u");
    sampler_v = glGetUniformLocation(program, "sampler_v");
    u_matrix = glGetUniformLocation(program, "u_Matrix");

    glGenTextures(3, samplers);

    for (int i = 0; i < 3; i++) {
        glBindTexture(GL_TEXTURE_2D, samplers[i]);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glBindTexture(GL_TEXTURE_2D, 0);
    }

}

void WlFilterYUV::onChange(int width, int height) {

    surface_width = width;
    surface_height = height;
    glViewport(0, 0, width, height);
    setMatrix(width, height);

}

void WlFilterYUV::draw() {

    glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
    glClear(GL_COLOR_BUFFER_BIT);
    glUseProgram(program);

    glUniformMatrix4fv(u_matrix, 1, GL_FALSE, matrix);

    glEnableVertexAttribArray(vPosition);
    glVertexAttribPointer(vPosition, 2, GL_FLOAT, false, 8, vertexs);
    glEnableVertexAttribArray(fPosition);
    glVertexAttribPointer(fPosition, 2, GL_FLOAT, false, 8, fragments);


    if (yuv_wdith > 0 && yuv_height > 0) {
        if (y != NULL) {
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, samplers[0]);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE, yuv_wdith, yuv_height, 0, GL_LUMINANCE,
                         GL_UNSIGNED_BYTE, y);
            glUniform1i(sampler_y, 0);
        }
        if (u != NULL) {
            glActiveTexture(GL_TEXTURE1);
            glBindTexture(GL_TEXTURE_2D, samplers[1]);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE, yuv_wdith / 2, yuv_height / 2, 0,
                         GL_LUMINANCE, GL_UNSIGNED_BYTE, u);
            glUniform1i(sampler_u, 1);
        }

        if (v != NULL) {
            glActiveTexture(GL_TEXTURE2);
            glBindTexture(GL_TEXTURE_2D, samplers[2]);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE, yuv_wdith / 2, yuv_height / 2, 0,
                         GL_LUMINANCE, GL_UNSIGNED_BYTE, v);
            glUniform1i(sampler_v, 2);
        }
        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
        glBindTexture(GL_TEXTURE_2D, 0);
    }
}

void WlFilterYUV::destroy() {

    glDeleteTextures(3, samplers);
    glDetachShader(program, vShader);
    glDetachShader(program, fShader);
    glDeleteShader(vShader);
    glDeleteShader(fShader);
    glDeleteProgram(program);
}

void WlFilterYUV::destorySorce() {

    yuv_wdith = 0;
    yuv_height = 0;

    if (y != NULL) {
        free(y);
        y = NULL;
    }
    if (u != NULL) {
        free(u);
        u = NULL;
    }
    if (v != NULL) {
        free(v);
        v = NULL;
    }

}

void WlFilterYUV::setMatrix(int width, int height) {

    initMatrix(matrix);

    if (yuv_wdith > 0 && yuv_height > 0) {
        float screen_r = 1.0 * width / height;
        float picture_r = 1.0 * yuv_wdith / yuv_height;

        if (screen_r > picture_r) //图片宽度缩放
        {

            float r = width / (1.0 * height / yuv_height * yuv_wdith);
            orthoM(-r, r, -1, 1, matrix);

        } else {//图片高度缩放

            float r = height / (1.0 * width / yuv_wdith * yuv_height);
            orthoM(-1, 1, -r, r, matrix);
        }
    }
}

void WlFilterYUV::setYuvData(void *Y, void *U, void *V, int width, int height) {

    if (width > 0 && height > 0) {
        if (yuv_wdith != width || yuv_height != height) {
            yuv_wdith = width;
            yuv_height = height;

            if (y != NULL) {
                free(y);
                y = NULL;
            }
            if (u != NULL) {
                free(u);
                u = NULL;
            }
            if (v != NULL) {
                free(v);
                v = NULL;
            }
            y = malloc(yuv_wdith * yuv_height);
            u = malloc(yuv_wdith * yuv_height / 4);
            v = malloc(yuv_wdith * yuv_height / 4);
            setMatrix(surface_width, surface_height);
        }
        memcpy(y, Y, yuv_wdith * yuv_height);
        memcpy(u, U, yuv_wdith * yuv_height / 4);
        memcpy(v, V, yuv_wdith * yuv_height / 4);

    }


}


