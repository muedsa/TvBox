package com.muedsa.compose.tv.widget.player.gl

import android.content.Context
import android.opengl.GLES20
import androidx.media3.common.util.GlProgram
import androidx.media3.common.util.GlUtil
import androidx.media3.common.util.GlUtil.GlException
import androidx.media3.common.util.UnstableApi
import timber.log.Timber
import javax.microedition.khronos.opengles.GL10

@UnstableApi
class TestVideoProcessor(
    private val context: Context
) : VideoProcessingGLSurfaceView.VideoProcessor {

    private var program: GlProgram? = null

    override fun initialize(glMajorVersion: Int, glMinorVersion: Int) {
        program = GlProgram(
            context,
            /* vertexShaderFilePath= */ "shaders/test/test_vertex.glsl",
            /* fragmentShaderFilePath= */ "shaders/test/test_fragment.glsl"
        ).apply {
            setBufferAttribute(
                "aPosition",
                GlUtil.getNormalizedCoordinateBounds(),
                GlUtil.HOMOGENEOUS_COORDINATE_VECTOR_SIZE
            )
            setBufferAttribute(
                "aTexCoords",
                GlUtil.getTextureCoordinateBounds(),
                GlUtil.HOMOGENEOUS_COORDINATE_VECTOR_SIZE
            )
        }
        GLES20.glTexParameterf(
            GL10.GL_TEXTURE_2D,
            GL10.GL_TEXTURE_MIN_FILTER,
            GL10.GL_NEAREST.toFloat()
        )
        GLES20.glTexParameterf(
            GL10.GL_TEXTURE_2D,
            GL10.GL_TEXTURE_MAG_FILTER,
            GL10.GL_LINEAR.toFloat()
        )
        GLES20.glTexParameterf(
            GL10.GL_TEXTURE_2D,
            GL10.GL_TEXTURE_WRAP_S,
            GL10.GL_REPEAT.toFloat()
        )
        GLES20.glTexParameterf(
            GL10.GL_TEXTURE_2D,
            GL10.GL_TEXTURE_WRAP_T,
            GL10.GL_REPEAT.toFloat()
        )
    }

    override fun setSurfaceSize(width: Int, height: Int) {

    }

    override fun draw(
        frameTexture: Int,
        frameTimestampUs: Long,
        frameWidth: Int,
        frameHeight: Int,
        transformMatrix: FloatArray
    ) {
        // Run the shader program.
        val program = checkNotNull(this.program)
        program.setSamplerTexIdUniform("inputTexture", frameTexture,  /* texUnitIndex= */0)
        program.setFloatsUniform("uTexTransform", transformMatrix)
        try {
            program.bindAttributesAndUniforms()
        } catch (e: GlException) {
            Timber.e(e, "Failed to update the shader program")
        }
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP,  /* first= */0,  /* count= */4)
        try {
            GlUtil.checkGlError()
        } catch (e: GlException) {
            Timber.e(e, "Failed to draw a frame")
        }
    }

    override fun release() {
        try {
            program?.delete()
        } catch (e: GlException) {
            Timber.e(e, "Failed to delete the shader program")
        }
    }
}