package com.example.videoextractdemo;

import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;

class OffscreenEGLConnection {
    private EGLDisplay eglDisplay = EGL14.EGL_NO_DISPLAY;
    private EGLSurface eglSurface = EGL14.EGL_NO_SURFACE;
    private EGLContext eglContext = EGL14.EGL_NO_CONTEXT;

    public void initialize(Object window) {
        eglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
        if (eglDisplay == EGL14.EGL_NO_DISPLAY) {
            throw new IllegalStateException("Unable to obtain EGL14 display");
        }
        int[] eglVersion = new int[1];
        if (!EGL14.eglInitialize(eglDisplay, eglVersion, 0, eglVersion, 0)) {
            throw new IllegalStateException("Unable to initialize EGL14: $eglError");
        }

        EGLConfig[] eglConfigs = new EGLConfig[1];
        int[] numConfigs = new int[1];
        int[] configAttributes = {
                EGL14.EGL_RED_SIZE,
                8,
                EGL14.EGL_GREEN_SIZE,
                8,
                EGL14.EGL_BLUE_SIZE,
                8,
                EGL14.EGL_ALPHA_SIZE,
                8,
                EGL14.EGL_RENDERABLE_TYPE,
                EGL14.EGL_OPENGL_ES2_BIT,
                EGL14.EGL_NONE
        };

        if (!EGL14.eglChooseConfig(eglDisplay, configAttributes, 0, eglConfigs, 0, eglConfigs.length, numConfigs, 0)) {
            throw new IllegalStateException("Unable to find RGB888 ES2 EGL config: $eglError");
        }

        int[] contextAttributes = {EGL14.EGL_CONTEXT_CLIENT_VERSION, 2, EGL14.EGL_NONE};
        eglContext = EGL14.eglCreateContext(eglDisplay, eglConfigs[0], EGL14.EGL_NO_CONTEXT, contextAttributes, 0);
        if (eglContext == EGL14.EGL_NO_CONTEXT) {
            throw new IllegalStateException("Unable to create EGL context: $eglError");
        }

        int[] surfaceAttributes = {EGL14.EGL_NONE};
        eglSurface = EGL14.eglCreateWindowSurface(eglDisplay, eglConfigs[0], window, surfaceAttributes, 0);
        if (!EGL14.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext)) {
            throw new IllegalStateException("Unable to initialize EGL: $eglError");
        }
    }

    public void draw() {
        EGL14.eglSwapBuffers(eglDisplay, eglSurface);
    }

    public void destroy() {
        if (eglDisplay!= EGL14.EGL_NO_DISPLAY) {
            EGL14.eglMakeCurrent(eglDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT);
            EGL14.eglDestroySurface(eglDisplay, eglSurface);
            eglSurface = EGL14.EGL_NO_SURFACE;
            EGL14.eglDestroyContext(eglDisplay, eglContext);
            eglContext = EGL14.EGL_NO_CONTEXT;
            EGL14.eglTerminate(eglDisplay);
            eglDisplay = EGL14.EGL_NO_DISPLAY;
            EGL14.eglReleaseThread();
        }
    }

//    private val eglError: String
//        get() = GLUtils.getEGLErrorString(eglGetError())
}