package br.com.ascend.aetheris

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlin.concurrent.thread
import kotlin.math.PI
import kotlin.math.sin

/**
 * ASCEND 1.7.5 — A Forja do Tempo
 * Gerador procedural 100% offline de 3 ambientes sonoros nórdicos (chuva no salão,
 * lareira da forja, vento boreal) sem arquivos de áudio externos e com controle de volume/parada.
 */
object AmbientAudioEngine {
    @Volatile private var running = false
    @Volatile private var currentSceneId: String = "off"
    @Volatile private var currentVolume: Float = 0.45f
    private var worker: Thread? = null

    val activeScene: String get() = if (running) currentSceneId else "off"
    val volume: Float get() = currentVolume

    fun setVolume(v: Float) {
        currentVolume = v.coerceIn(0f, 1f)
    }

    fun start(sceneId: String, vol: Float = currentVolume) {
        currentVolume = vol.coerceIn(0f, 1f)
        if (sceneId == "off") {
            stop()
            return
        }
        if (running && currentSceneId == sceneId) return
        stop()
        currentSceneId = sceneId
        running = true
        worker = thread(start = true, isDaemon = true, name = "AscendAmbientAudio") {
            playProceduralLoop(sceneId)
        }
    }

    fun stop() {
        running = false
        currentSceneId = "off"
        worker?.interrupt()
        worker = null
    }

    /**
     * Gera um buffer PCM 16-bit mono determinístico para a cena solicitada (também testável em JVM).
     */
    fun synthesizeFrame(
        sceneId: String,
        sampleCount: Int,
        sampleRate: Int = 22050,
        seedStart: Long = 1337L,
        phaseOffset: Double = 0.0,
        vol: Float = 0.45f
    ): ShortArray {
        val out = ShortArray(sampleCount)
        var rng = seedStart
        var lp1 = 0.0
        var lp2 = 0.0
        var crackleEnv = 0.0
        val gain = vol.coerceIn(0f, 1f) * 14000.0
        for (i in 0 until sampleCount) {
            // xorshift64 determinístico para ruído suave
            rng = rng xor (rng shl 13)
            rng = rng xor (rng ushr 7)
            rng = rng xor (rng shl 17)
            val white = ((rng and 0xFFFFL).toDouble() / 32768.0) - 1.0
            val t = (phaseOffset + i) / sampleRate.toDouble()
            val sample = when (sceneId) {
                "rain" -> {
                    // Ruído rosa suave + ondulação lenta de telhado de madeira
                    lp1 = lp1 * 0.86 + white * 0.14
                    lp2 = lp2 * 0.96 + lp1 * 0.04
                    val sway = 0.85 + 0.15 * sin(2.0 * PI * 0.18 * t)
                    (lp1 * 0.55 + lp2 * 0.45) * sway
                }
                "hearth" -> {
                    // Grave contínuo de brasa + estalos breves raros
                    lp1 = lp1 * 0.94 + white * 0.06
                    lp2 = lp2 * 0.985 + lp1 * 0.015
                    if ((rng and 0x3FFFL) == 7L) crackleEnv = 0.85
                    crackleEnv *= 0.93
                    val ember = lp2 * 1.15 + white * crackleEnv * 0.35
                    ember.coerceIn(-1.0, 1.0)
                }
                "wind" -> {
                    // Vento boreal filtrado com modulação de baixa frequência
                    lp1 = lp1 * 0.95 + white * 0.05
                    lp2 = lp2 * 0.98 + lp1 * 0.02
                    val gust = 0.55 + 0.45 * sin(2.0 * PI * 0.11 * t)
                    val howl = 0.12 * sin(2.0 * PI * (135.0 + 18.0 * sin(2.0 * PI * 0.07 * t)) * t)
                    (lp2 * 1.45 * gust + howl).coerceIn(-1.0, 1.0)
                }
                else -> 0.0
            }
            out[i] = (sample * gain).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return out
    }

    private fun playProceduralLoop(sceneId: String) {
        val sampleRate = 22050
        val minBuf = runCatching {
            AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT)
        }.getOrElse { -1 }
        if (minBuf <= 0) {
            running = false
            return
        }
        val frameSize = (sampleRate / 4).coerceAtLeast(minBuf / 2)
        val track = runCatching {
            AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setSampleRate(sampleRate)
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(frameSize * 4)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()
        }.getOrNull() ?: run {
            running = false
            return
        }

        try {
            track.play()
            var seed = 918273645L
            var phase = 0.0
            while (running && currentSceneId == sceneId && !Thread.currentThread().isInterrupted) {
                val pcm = synthesizeFrame(sceneId, frameSize, sampleRate, seed, phase, currentVolume)
                seed += frameSize * 31L
                phase += frameSize
                track.write(pcm, 0, pcm.size)
            }
        } catch (_: Throwable) {
            // Falha silenciosa em ambientes sem hardware de áudio
        } finally {
            runCatching { track.stop() }
            runCatching { track.release() }
        }
    }
}
