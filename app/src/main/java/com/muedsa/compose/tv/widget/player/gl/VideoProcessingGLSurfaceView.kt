package com.muedsa.compose.tv.widget.player.gl

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.SurfaceTexture
import android.media.MediaFormat
import android.opengl.EGL14
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.os.Handler
import android.os.Looper
import android.view.Surface
import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.util.Assertions
import androidx.media3.common.util.GlUtil
import androidx.media3.common.util.GlUtil.GlException
import androidx.media3.common.util.TimedValueQueue
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.video.VideoFrameMetadataListener
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.egl.EGLContext
import javax.microedition.khronos.egl.EGLDisplay
import javax.microedition.khronos.egl.EGLSurface
import javax.microedition.khronos.opengles.GL10


@SuppressLint("ViewConstructor")
@UnstableApi
class VideoProcessingGLSurfaceView(
    context: Context,
    requireSecureContext: Boolean,
    videoProcessor: VideoProcessor,
) : GLSurfaceView(context) {

    private val renderer: VideoRenderer = VideoRenderer(videoProcessor)
    private val mainHandler: Handler = Handler(Looper.getMainLooper())

    private var surfaceTexture: SurfaceTexture? = null
    private var surface: Surface? = null
    private var player: ExoPlayer? = null

    init {
        setEGLContextClientVersion(2)
        setEGLConfigChooser(
            /* redSize= */ 8,
            /* greenSize= */ 8,
            /* blueSize= */ 8,
            /* alphaSize= */ 8,
            /* depthSize= */ 0,
            /* stencilSize= */ 0
        )
        setEGLContextFactory(object : EGLContextFactory {
            override fun createContext(
                egl: EGL10,
                display: EGLDisplay,
                eglConfig: EGLConfig
            ): EGLContext {
                val glAttributes = if (requireSecureContext) intArrayOf(
                    EGL14.EGL_CONTEXT_CLIENT_VERSION,
                    2,
                    EGL_PROTECTED_CONTENT_EXT,
                    EGL14.EGL_TRUE,
                    EGL14.EGL_NONE
                ) else intArrayOf(EGL14.EGL_CONTEXT_CLIENT_VERSION, 2, EGL14.EGL_NONE)
                return egl.eglCreateContext(
                    display,
                    eglConfig,
                    /* share_context= */ EGL10.EGL_NO_CONTEXT,
                    glAttributes
                )
            }

            override fun destroyContext(
                egl: EGL10,
                display: EGLDisplay,
                context: EGLContext
            ) {
                egl.eglDestroyContext(display, context)
            }
        })
        setEGLWindowSurfaceFactory(object : EGLWindowSurfaceFactory {
            override fun createWindowSurface(
                egl: EGL10,
                display: EGLDisplay,
                config: EGLConfig,
                nativeWindow: Any
            ): EGLSurface {
                val attribList = if (requireSecureContext) intArrayOf(
                    EGL_PROTECTED_CONTENT_EXT, EGL14.EGL_TRUE, EGL10.EGL_NONE
                ) else intArrayOf(EGL10.EGL_NONE)
                return egl.eglCreateWindowSurface(display, config, nativeWindow, attribList)
            }

            override fun destroySurface(
                egl: EGL10,
                display: EGLDisplay,
                surface: EGLSurface
            ) {
                egl.eglDestroySurface(display, surface)
            }
        })
        setRenderer(renderer)
        renderMode = RENDERMODE_WHEN_DIRTY
    }

    fun setPlayer(player: ExoPlayer?) {
        if (player == this.player) {
            return
        }
        this.player?.clearVideoSurface(surface)
        this.player?.clearVideoFrameMetadataListener(renderer)
        this.player = player
        this.player?.setVideoFrameMetadataListener(renderer)
        this.player?.setVideoSurface(surface)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        // Post to make sure we occur in order with any onSurfaceTextureAvailable calls.
        mainHandler.post {
            surface?.let {
                player?.setVideoSurface(null)
                releaseSurface(surfaceTexture, it)
                surfaceTexture = null
                surface = null
            }
        }
    }

    private fun onSurfaceTextureAvailable(surfaceTexture: SurfaceTexture) {
        mainHandler.post {
            val oldSurfaceTexture = this.surfaceTexture
            val oldSurface = this.surface
            this.surfaceTexture = surfaceTexture
            this.surface = Surface(surfaceTexture)
            releaseSurface(oldSurfaceTexture, oldSurface)
            player?.setVideoSurface(surface)
        }
    }

    private fun releaseSurface(
        oldSurfaceTexture: SurfaceTexture?,
        oldSurface: Surface?
    ) {
        oldSurfaceTexture?.release()
        oldSurface?.release()
    }

    inner class VideoRenderer(
        private val videoProcessor: VideoProcessor,
    ) : Renderer, VideoFrameMetadataListener {

        private val frameAvailable: AtomicBoolean = AtomicBoolean()
        private val videoFrameTimestampQueue: TimedValueQueue<Long> = TimedValueQueue()
        private val transformMatrix: FloatArray = FloatArray(16)

        private var texture = 0
        private var surfaceTexture: SurfaceTexture? = null

        private var initialized = false
        private var width = -1
        private var height = -1
        private var frameTimestampUs: Long = C.TIME_UNSET

        override fun onSurfaceCreated(
            gl: GL10,
            config: EGLConfig
        ) {
            try {
                texture = GlUtil.createExternalTexture()
            } catch (e: GlException) {
                Timber.e(e, "Failed to create an external texture")
            }
            surfaceTexture = SurfaceTexture(texture).apply {
                setOnFrameAvailableListener {
                    frameAvailable.set(true)
                    requestRender()
                }
                onSurfaceTextureAvailable(this@apply)

            }
        }

        override fun onSurfaceChanged(
            gl: GL10?,
            width: Int,
            height: Int
        ) {
            GLES20.glViewport(0, 0, width, height)
            this.width = width
            this.height = height
        }

        override fun onDrawFrame(gl: GL10) {

            if (!initialized) {
                videoProcessor.initialize()
                initialized = true
            }

            if (width != -1 && height != -1) {
                videoProcessor.setSurfaceSize(width, height)
                width = -1
                height = -1
            }

            if (frameAvailable.compareAndSet(true, false)) {
                val surfaceTexture = Assertions.checkNotNull(this.surfaceTexture)
                surfaceTexture.updateTexImage()
                val lastFrameTimestampNs = surfaceTexture.timestamp
                videoFrameTimestampQueue.poll(lastFrameTimestampNs)?.let {
                    this.frameTimestampUs = it
                }
                surfaceTexture.getTransformMatrix(transformMatrix)
            }

            videoProcessor.draw(
                texture,
                frameTimestampUs,
                transformMatrix,
            )
        }

        override fun onVideoFrameAboutToBeRendered(
            presentationTimeUs: Long,
            releaseTimeNs: Long,
            format: Format,
            mediaFormat: MediaFormat?
        ) {
            videoFrameTimestampQueue.add(releaseTimeNs, presentationTimeUs)
        }
    }

    interface VideoProcessor {

        /** Performs any required GL initialization.  */
        fun initialize()

        /** Sets the size of the output surface in pixels.  */
        fun setSurfaceSize(width: Int, height: Int)

        /**
         * Draws using GL operations.
         *
         * @param frameTexture The ID of a GL texture containing a video frame.
         * @param frameTimestampUs The presentation timestamp of the frame, in microseconds.
         * @param transformMatrix The 4 * 4 transform matrix to be applied to the texture.
         */
        fun draw(
            frameTexture: Int,
            frameTimestampUs: Long,
            transformMatrix: FloatArray
        )

        /** Releases any resources associated with this [VideoProcessor].  */
        fun release()
    }

    companion object {
        const val EGL_PROTECTED_CONTENT_EXT: Int = 0x32C0
    }
}