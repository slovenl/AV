//
// Created by yangw on 2019-4-11.
//

#ifndef NATIVEOPENGLDEMO_WLFILTERYUV_H
#define NATIVEOPENGLDEMO_WLFILTERYUV_H

#include "WlBaseOpengl.h"
#include "../matrix/MatrixUtil.h"
#include "../shaderutil/WlShaderUtil.h"

class WlFilterYUV : public WlBaseOpengl {

public:
    GLint vPosition;
    GLint fPosition;
    GLint u_matrix;

    GLint sampler_y;
    GLint sampler_u;
    GLint sampler_v;

    GLuint samplers[3];


    float matrix[16];
    void *y = NULL;
    void *u = NULL;
    void *v = NULL;
    int yuv_wdith = 0;
    int yuv_height = 0;

public:
    WlFilterYUV();

    ~WlFilterYUV();

    void onCreate();

    void onChange(int w, int h);

    void draw();

    void destroy();

    void destorySorce();

    void setMatrix(int width, int height);

    void setYuvData(void *y, void *u, void *v, int width, int height);

};


#endif //NATIVEOPENGLDEMO_WLFILTERYUV_H
