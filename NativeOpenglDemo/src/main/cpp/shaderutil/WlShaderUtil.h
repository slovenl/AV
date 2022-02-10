//
// Created by yangw on 2019-2-23.
//

#ifndef NATIVEOPENGLDEMO_WLSHADERUTIL_H
#define NATIVEOPENGLDEMO_WLSHADERUTIL_H

#include <GLES2/gl2.h>

static int loadShaders(int shaderType, const char *code) {
    GLuint shader = glCreateShader(shaderType);
    glShaderSource(shader, 1, &code, 0);
    glCompileShader(shader);
    return shader;
}

static int
createProgrm(const char *vertex, const char *fragment, GLuint *v_shader, GLuint *f_shader) {
    GLuint vertexShader = loadShaders(GL_VERTEX_SHADER, vertex);
    GLuint fragmentShader = loadShaders(GL_FRAGMENT_SHADER, fragment);
    int program = glCreateProgram();
    glAttachShader(program, vertexShader);
    glAttachShader(program, fragmentShader);
    glLinkProgram(program);

    *v_shader = vertexShader;
    *f_shader = fragmentShader;

    return program;
}


#endif //NATIVEOPENGLDEMO_WLSHADERUTIL_H
