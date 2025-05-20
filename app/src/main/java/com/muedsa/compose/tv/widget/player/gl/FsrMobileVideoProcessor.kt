package com.muedsa.compose.tv.widget.player.gl

import android.content.Context
import android.opengl.GLES20
import androidx.media3.common.util.GlProgram
import androidx.media3.common.util.GlUtil
import androidx.media3.common.util.GlUtil.GlException
import androidx.media3.common.util.UnstableApi
import timber.log.Timber

@UnstableApi
class FsrMobileVideoProcessor(
    private val context: Context,
) : VideoProcessingGLSurfaceView.VideoProcessor {

    private var needInputSize: Boolean = true

    private var program: GlProgram? = null

    private var outputWidth: Int = -1
    private var outputHeight: Int = -1
    private var outputSize: FloatArray = FloatArray(2)

    override fun initialize(glMajorVersion: Int, glMinorVersion: Int, extensions: String) {
        var shaderFileDir = "shaders/fsr/mobile/2.0/"
        needInputSize = true
        if (glMajorVersion > 3 || (glMajorVersion == 3 && glMinorVersion >= 1)) {
            shaderFileDir = "shaders/fsr/mobile/3.1/"
            needInputSize = false
        } else if (glMajorVersion == 3 && glMinorVersion == 0) {
            shaderFileDir = "shaders/fsr/mobile/3.0/"
            needInputSize = false
        }
        Timber.i("fsr shader use: $shaderFileDir")
        Timber.i("opengl extensions: $extensions")
        program = GlProgram(
            context,
            "${shaderFileDir}opt_fsr_vertex.glsl",
            "${shaderFileDir}opt_fsr_fragment.glsl"
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
        // createFrameBuffer() // size not been initialized
        GLES20.glTexParameterf(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_MIN_FILTER,
            GLES20.GL_NEAREST.toFloat()
        )
        GLES20.glTexParameterf(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_MAG_FILTER,
            GLES20.GL_LINEAR.toFloat()
        )
        GLES20.glTexParameterf(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_WRAP_S,
            GLES20.GL_REPEAT.toFloat()
        )
        GLES20.glTexParameterf(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_WRAP_T,
            GLES20.GL_REPEAT.toFloat()
        )
    }

    override fun setSurfaceSize(width: Int, height: Int) {
        Timber.i("setSurfaceSize($width,$height)")
        this.outputWidth = width
        this.outputHeight = height
        this.outputSize = floatArrayOf(width.toFloat(), height.toFloat())
    }

    override fun draw(
        frameTexture: Int,
        frameTimestampUs: Long,
        frameWidth: Int,
        frameHeight: Int,
        transformMatrix: FloatArray
    ) {
        val inputTextureSize : FloatArray? = if (needInputSize) {
            if (frameWidth > 0 && frameHeight > 0) {
              floatArrayOf(frameWidth.toFloat(), frameHeight.toFloat())
            } else floatArrayOf(0f, 0f)
        } else null
        val program = checkNotNull(this.program)
        program.setSamplerTexIdUniform("inputTexture", frameTexture, 0)
        inputTextureSize?.let {
            program.setFloatsUniform("inputTextureSize", inputTextureSize)
        }
        program.setFloatsUniform("outputTextureSize", outputSize)
        program.setFloatsUniform("uTexTransform", transformMatrix)
        try {
            program.bindAttributesAndUniforms()
        } catch (e: GlException) {
            Timber.e(e, "Failed to update the fsr shader program")
        }
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        checkGlError("Failed to fsr shader program")
    }

    override fun release() {
        try {
            program?.delete()
        } catch (e: GlException) {
            Timber.e(e, "Failed to delete the easu shader program")
        }
    }

    private fun checkGlError(message: String) {
        try {
            GlUtil.checkGlError()
        } catch (e: GlException) {
            Timber.e(e, message)
        }
    }
}