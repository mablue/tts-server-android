package com.github.jing332.tts_server_android.help.audio.exo

import android.net.Uri
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.upstream.BaseDataSource
import com.google.android.exoplayer2.upstream.DataSpec
import java.io.EOFException
import java.io.IOException
import java.io.InputStream


class InputStreamDataSource(
    private val inputStream: InputStream,
) : BaseDataSource(/* isNetwork = */ false) {
    private var dataSpec: DataSpec? = null
    private var bytesRemaining: Long = 0
    private var opened = false

    @Throws(IOException::class)
    override fun open(dataSpec: DataSpec): Long {
        this.dataSpec = dataSpec
        try {
            val skipped: Long = inputStream.skip(dataSpec.position)
            if (skipped < dataSpec.position) throw EOFException()
            if (dataSpec.length != C.LENGTH_UNSET.toLong()) {
                bytesRemaining = dataSpec.length
            } else {
                bytesRemaining = inputStream.available().toLong()
                if (bytesRemaining == 0L) bytesRemaining = C.LENGTH_UNSET.toLong()
            }
        } catch (e: IOException) {
            throw IOException(e)
        }
        opened = true
        return bytesRemaining
    }

    override fun getUri(): Uri? = dataSpec?.uri

    @Throws(IOException::class)
    override fun read(buffer: ByteArray, offset: Int, readLength: Int): Int {
        if (readLength == 0) {
            return 0
        } else if (bytesRemaining == 0L) {
            return C.RESULT_END_OF_INPUT
        }
        val bytesRead: Int = try {
            val bytesToRead =
                if (bytesRemaining == C.LENGTH_UNSET.toLong()) readLength else Math.min(
                    bytesRemaining,
                    readLength.toLong()
                ).toInt()
            inputStream.read(buffer, offset, bytesToRead)
        } catch (e: IOException) {
            throw IOException(e)
        }
        if (bytesRead == -1) {
            if (bytesRemaining != C.LENGTH_UNSET.toLong()) {
                // End of stream reached having not read sufficient data.
                throw IOException(EOFException())
            }
            return C.RESULT_END_OF_INPUT
        }
        if (bytesRemaining != C.LENGTH_UNSET.toLong()) {
            bytesRemaining -= bytesRead.toLong()
        }
        return bytesRead
    }


    @Throws(IOException::class)
    override fun close() {
        try {
            inputStream.close()
        } catch (e: IOException) {
            throw e
        } finally {
            if (opened) {
                opened = false
            }
        }
    }
}