package com.muedsa.compose.tv.widget.player.gl

import android.content.Context
import android.opengl.GLES20
import androidx.media3.common.util.GlProgram
import androidx.media3.common.util.GlUtil
import androidx.media3.common.util.GlUtil.GlException
import androidx.media3.common.util.UnstableApi
import timber.log.Timber

@UnstableApi
class FsrVideoProcessor(
    private val context: Context,
) : VideoProcessingGLSurfaceView.VideoProcessor {

    private var needInputSize: Boolean = true

    private val framebuffers = IntArray(1)
    private val textures = IntArray(1)
    private var easuProgram: GlProgram? = null
    private var rcasProgram: GlProgram? = null

    private val sharpness: Float = 0.2f

    private var outputWidth: Int = -1
    private var outputHeight: Int = -1
    private var outputSize: FloatArray = FloatArray(2)

    override fun initialize(glMajorVersion: Int, glMinorVersion: Int, extensions: String) {
        var shaderFileDir = "shaders/fsr/2.0/"
        if (glMajorVersion > 3 || (glMajorVersion == 3 && glMinorVersion >= 1)) {
            shaderFileDir = "shaders/fsr/3.1/"
            needInputSize = false
        } else if (glMajorVersion == 3 && glMinorVersion == 0) {
            shaderFileDir = "shaders/fsr/3.0/"
            needInputSize = false
        }
        Timber.i("fsr shader use: $shaderFileDir")
        easuProgram = GlProgram(
            context,
            "${shaderFileDir}fsr_easu_vertex.glsl",
            "${shaderFileDir}fsr_easu_fragment.glsl"
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
        rcasProgram = GlProgram(
            context,
            "${shaderFileDir}fsr_rcas_vertex.glsl",
            "${shaderFileDir}fsr_rcas_fragment.glsl"
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

    private fun createFrameBuffer() {
        GLES20.glGenFramebuffers(1, framebuffers, 0)
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, framebuffers[0])
        GLES20.glGenTextures(1, textures, 0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0])
        GLES20.glTexImage2D(
            GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, outputWidth, outputHeight,
            0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null
        )
        GLES20.glFramebufferTexture2D(
            GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
            GLES20.GL_TEXTURE_2D, textures[0], 0
        )
        val status = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER)
        if (status != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            Timber.e("Framebuffer is not complete: $status")
        }
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)
    }

    override fun setSurfaceSize(width: Int, height: Int) {
        Timber.i("setSurfaceSize($width,$height)")
        this.outputWidth = width
        this.outputHeight = height
        this.outputSize = floatArrayOf(width.toFloat(), height.toFloat())
        deleteFramebuffer()
        createFrameBuffer()
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
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, framebuffers[0])
        val easuProgram = checkNotNull(this.easuProgram)
        easuProgram.use()
        easuProgram.setSamplerTexIdUniform("inputTexture", frameTexture, 0)
        inputTextureSize?.let {
            easuProgram.setFloatsUniform("inputTextureSize", inputTextureSize)
        }
        easuProgram.setFloatsUniform("outputTextureSize", outputSize)
        easuProgram.setFloatsUniform("uTexTransform", transformMatrix)
        try {
            easuProgram.bindAttributesAndUniforms()
        } catch (e: GlException) {
            Timber.e(e, "Failed to update the easu shader program")
        }
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        checkGlError("Failed to easu")

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)
        val rcasProgram = checkNotNull(this.rcasProgram)
        rcasProgram.use()
        rcasProgram.setSamplerTexIdUniform("inputTexture", textures[0], 0)
        inputTextureSize?.let {
            rcasProgram.setFloatsUniform("inputTextureSize", outputSize)
        }
        rcasProgram.setFloatUniform("sharpness", sharpness)
        try {
            rcasProgram.bindAttributesAndUniforms()
        } catch (e: GlException) {
            Timber.e(e, "Failed to update the rcas shader program")
        }
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP,  /* first= */0,  /* count= */4)
        checkGlError("Failed to rcas")
    }

    private fun deleteFramebuffer() {
        if (framebuffers[0] != 0) {
            GLES20.glDeleteFramebuffers(1, framebuffers, 0)
            framebuffers[0] = 0
        }
        if (textures[0] != 0) {
            GLES20.glDeleteTextures(1, textures, 0)
            textures[0] = 0
        }
    }

    override fun release() {
        try {
            easuProgram?.delete()
        } catch (e: GlException) {
            Timber.e(e, "Failed to delete the easu shader program")
        }
        try {
            rcasProgram?.delete()
        } catch (e: GlException) {
            Timber.e(e, "Failed to delete the rcas shader program")
        }
        deleteFramebuffer()
    }

    private fun checkGlError(message: String) {
        try {
            GlUtil.checkGlError()
        } catch (e: GlException) {
            Timber.e(e, message)
        }
    }
}