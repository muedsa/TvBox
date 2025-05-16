package com.muedsa.compose.tv.widget.player.gl

import android.content.Context
import android.opengl.GLES31
import androidx.media3.common.util.GlProgram
import androidx.media3.common.util.GlUtil
import androidx.media3.common.util.GlUtil.GlException
import androidx.media3.common.util.UnstableApi
import com.badlogic.gdx.graphics.GL30
import timber.log.Timber

@UnstableApi
class FsrVideoProcessor(
    private val context: Context,
) : VideoProcessingGLSurfaceView.VideoProcessor {

    private val framebuffers = IntArray(1)
    private val textures = IntArray(1)
    private var easuProgram: GlProgram? = null
    private var rcasProgram: GlProgram? = null

    private val sharpness: Float = 0.2f

    private var width: Int = -1
    private var height: Int = -1
    private var size: FloatArray = FloatArray(2)

    override fun initialize() {
        easuProgram = GlProgram(
            context,
            "shaders/fsr/fsr_easu_vertex.glsl",
            "shaders/fsr/fsr_easu_fragment.glsl"
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
            "shaders/fsr/fsr_rcas_vertex.glsl",
            "shaders/fsr/fsr_rcas_fragment.glsl"
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

        GLES31.glTexParameterf(
            GLES31.GL_TEXTURE_2D,
            GLES31.GL_TEXTURE_MIN_FILTER,
            GLES31.GL_NEAREST.toFloat()
        )
        GLES31.glTexParameterf(
            GLES31.GL_TEXTURE_2D,
            GLES31.GL_TEXTURE_MAG_FILTER,
            GLES31.GL_LINEAR.toFloat()
        )
        GLES31.glTexParameterf(
            GLES31.GL_TEXTURE_2D,
            GLES31.GL_TEXTURE_WRAP_S,
            GLES31.GL_REPEAT.toFloat()
        )
        GLES31.glTexParameterf(
            GLES31.GL_TEXTURE_2D,
            GLES31.GL_TEXTURE_WRAP_T,
            GLES31.GL_REPEAT.toFloat()
        )
    }

    private fun createFrameBuffer() {
        GLES31.glGenFramebuffers(1, framebuffers, 0)
        GLES31.glBindFramebuffer(GL30.GL_FRAMEBUFFER, framebuffers[0])
        GLES31.glGenTextures(1, textures, 0)
        GLES31.glBindTexture(GL30.GL_TEXTURE_2D, textures[0])
        GLES31.glTexImage2D(
            GLES31.GL_TEXTURE_2D, 0, GLES31.GL_RGBA, width, height,
            0, GLES31.GL_RGBA, GLES31.GL_UNSIGNED_BYTE, null
        )
        GLES31.glFramebufferTexture2D(
            GLES31.GL_FRAMEBUFFER, GLES31.GL_COLOR_ATTACHMENT0,
            GLES31.GL_TEXTURE_2D, textures[0], 0
        )
        val status = GLES31.glCheckFramebufferStatus(GLES31.GL_FRAMEBUFFER)
        if (status != GLES31.GL_FRAMEBUFFER_COMPLETE) {
            Timber.e("Framebuffer is not complete: $status")
        }
        GLES31.glBindFramebuffer(GLES31.GL_FRAMEBUFFER, 0)
    }

    override fun setSurfaceSize(width: Int, height: Int) {
        Timber.i("setSurfaceSize($width,$height)")
        this.width = width
        this.height = height
        this.size = floatArrayOf(width.toFloat(), height.toFloat())
        deleteFramebuffer()
        createFrameBuffer()
    }

    override fun draw(
        frameTexture: Int,
        frameTimestampUs: Long,
        transformMatrix: FloatArray
    ) {
        GLES31.glBindFramebuffer(GLES31.GL_FRAMEBUFFER, framebuffers[0])
        val easuProgram = checkNotNull(this.easuProgram)
        easuProgram.use()
        easuProgram.setSamplerTexIdUniform("inputTexture", frameTexture, 0)
        easuProgram.setFloatsUniform("outputTextureSize", size)
        easuProgram.setFloatsUniform("uTexTransform", transformMatrix)
        try {
            easuProgram.bindAttributesAndUniforms()
        } catch (e: GlException) {
            Timber.e(e, "Failed to update the easu shader program")
        }
        GLES31.glClear(GLES31.GL_COLOR_BUFFER_BIT)
        GLES31.glDrawArrays(GLES31.GL_TRIANGLE_STRIP, 0, 4)
        checkGlError("Failed to easu")

        GLES31.glBindFramebuffer(GLES31.GL_FRAMEBUFFER, 0)
        val rcasProgram = checkNotNull(this.rcasProgram)
        rcasProgram.use()
        rcasProgram.setSamplerTexIdUniform("inputTexture", textures[0], 0)
        rcasProgram.setFloatUniform("sharpness", sharpness)
        try {
            rcasProgram.bindAttributesAndUniforms()
        } catch (e: GlException) {
            Timber.e(e, "Failed to update the rcas shader program")
        }
        GLES31.glClear(GLES31.GL_COLOR_BUFFER_BIT)
        GLES31.glDrawArrays(GLES31.GL_TRIANGLE_STRIP,  /* first= */0,  /* count= */4)
        checkGlError("Failed to rcas")
    }

    private fun deleteFramebuffer() {
        if (framebuffers[0] != 0) {
            GLES31.glDeleteFramebuffers(1, framebuffers, 0)
            framebuffers[0] = 0
        }
        if (textures[0] != 0) {
            GLES31.glDeleteTextures(1, textures, 0)
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