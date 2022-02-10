//
// Created by yangw on 2019-3-31.
//

#ifndef NATIVEOPENGLDEMO_WLFILTERONE_H
#define NATIVEOPENGLDEMO_WLFILTERONE_H

#include "WlBaseOpengl.h"
#include "../matrix/MatrixUtil.h"
#include "../shaderutil/WlShaderUtil.h"

class WlFilterOne : public WlBaseOpengl {

public:
    GLint vPosition;
    GLint fPosition;
    GLint sampler;
    GLuint textureId;
    GLint u_matrix;

    int w;
    int h;
    void *pixels = NULL;

    float matrix[16];

public:
    WlFilterOne();

    ~WlFilterOne();

    void onCreate();

    void onChange(int w, int h);

    void draw();

    void destroy();

    void destorySorce();

    void setMatrix(int width, int height);

    void setPilex(void *data, int width, int height, int length);

};


#endif //NATIVEOPENGLDEMO_WLFILTERONE_H
