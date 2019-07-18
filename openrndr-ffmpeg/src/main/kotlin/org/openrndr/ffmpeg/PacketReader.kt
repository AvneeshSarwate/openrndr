package org.openrndr.ffmpeg

import org.bytedeco.ffmpeg.avcodec.AVPacket
import org.bytedeco.ffmpeg.avformat.AVFormatContext
import org.bytedeco.ffmpeg.global.avcodec
import org.bytedeco.ffmpeg.global.avformat
import org.bytedeco.ffmpeg.global.avutil.AVERROR_EOF

internal class PacketReader(val formatContext: AVFormatContext, val statistics: VideoStatistics) {

    val queue = Queue<AVPacket>(2500)
    var disposed = false

    var endOfFile = false
    var ready = true
    fun isQueueEmpty():Boolean {
        return queue.size() == 0
    }

    fun start() {
//        println("sleeping..")
//        Thread.sleep(5000)
//        println("go")
        while (!disposed) {

            if (queue.size() > 500) {
                ready = true
            }

            if (queue.size() < 2400) {

                if (!endOfFile) {
                    val packet = avcodec.av_packet_alloc()
                    //println("reading packet")
                    val res = avformat.av_read_frame(formatContext, packet)
                    //println("done")

                    if (res == 0) {
                        //println("got packet ${queue.size()}")
                        queue.push(packet)
                        statistics.packetQueueSize = queue.size()
                    } else {
                        println("no packet (error)  ${queue.size()}")
                        if (res == AVERROR_EOF) {
                            println("packet reader; end of file")
                            endOfFile = true
                        }
                    }
                } else {
                    Thread.sleep(100)
                }
            } else {
                println("queue full")
                Thread.sleep(500)
            }
        }
        println("packet reader ended for some reason?")
    }

    fun nextPacket(): AVPacket? {
        if (queue.size() > 0)
            statistics.packetQueueSize = queue.size() - 1
        else {
            statistics.packetQueueSize = 0
        }

        return if (ready) queue.popOrNull() else null
    }

    fun dispose() {
        disposed = true
    }

    fun flushQueue() {

        while (!queue.isEmpty())  {
            val packet = queue.pop()
            avcodec.av_packet_unref(packet)
        }
        endOfFile = false
        println("flushed packet reader queue")
    }

}