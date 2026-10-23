package com.sony.braviaremote.network

import java.io.ByteArrayOutputStream
import java.io.EOFException
import java.io.InputStream
import java.io.OutputStream

/**
 * Lightweight Protocol Buffer binary writer and reader.
 * Implements wire types: 0 (Varint) and 2 (Length-delimited).
 */
class ProtoWriter {
    private val buffer = ByteArrayOutputStream()

    fun writeVarint(value: Long) {
        var v = value
        while (v and 0x7FL.inv() != 0L) {
            buffer.write(((v and 0x7F) or 0x80).toInt())
            v = v ushr 7
        }
        buffer.write(v.toInt())
    }

    private fun writeTag(fieldNumber: Int, wireType: Int) {
        writeVarint(((fieldNumber shl 3) or wireType).toLong())
    }

    fun writeInt32(fieldNumber: Int, value: Int) {
        writeTag(fieldNumber, 0)
        writeVarint(value.toLong())
    }

    fun writeBool(fieldNumber: Int, value: Boolean) {
        writeInt32(fieldNumber, if (value) 1 else 0)
    }

    fun writeString(fieldNumber: Int, value: String) {
        val bytes = value.toByteArray(Charsets.UTF_8)
        writeTag(fieldNumber, 2)
        writeVarint(bytes.size.toLong())
        buffer.write(bytes)
    }

    fun writeBytes(fieldNumber: Int, value: ByteArray) {
        writeTag(fieldNumber, 2)
        writeVarint(value.size.toLong())
        buffer.write(value)
    }

    fun writeMessage(fieldNumber: Int, message: ProtoWriter) {
        val bytes = message.toByteArray()
        writeTag(fieldNumber, 2)
        writeVarint(bytes.size.toLong())
        buffer.write(bytes)
    }

    fun toByteArray(): ByteArray = buffer.toByteArray()

    /**
     * Prepends the serialized message with its length as a varint (length-delimited framing).
     */
    fun toDelimitedByteArray(): ByteArray {
        val raw = buffer.toByteArray()
        val out = ByteArrayOutputStream()
        var v = raw.size.toLong()
        while (v and 0x7FL.inv() != 0L) {
            out.write(((v and 0x7F) or 0x80).toInt())
            v = v ushr 7
        }
        out.write(v.toInt())
        out.write(raw)
        return out.toByteArray()
    }
}

class ProtoReader(private val bytes: ByteArray) {
    private var pos = 0

    fun hasMore(): Boolean = pos < bytes.size

    fun readVarint(): Long {
        var result: Long = 0
        var shift = 0
        while (pos < bytes.size) {
            val b = bytes[pos++].toInt()
            result = result or ((b and 0x7F).toLong() shl shift)
            if ((b and 0x80) == 0) return result
            shift += 7
        }
        return result
    }

    fun readTag(): Pair<Int, Int>? {
        if (!hasMore()) return null
        val tag = readVarint().toInt()
        val fieldNumber = tag ushr 3
        val wireType = tag and 0x07
        return Pair(fieldNumber, wireType)
    }

    fun readInt32(): Int = readVarint().toInt()

    fun readBytes(): ByteArray {
        val length = readVarint().toInt()
        val end = minOf(pos + length, bytes.size)
        val result = bytes.copyOfRange(pos, end)
        pos = end
        return result
    }

    fun readString(): String {
        return String(readBytes(), Charsets.UTF_8)
    }

    fun skipField(wireType: Int) {
        when (wireType) {
            0 -> readVarint()
            1 -> pos = minOf(pos + 8, bytes.size)
            2 -> {
                val len = readVarint().toInt()
                pos = minOf(pos + len, bytes.size)
            }
            5 -> pos = minOf(pos + 4, bytes.size)
            else -> pos = bytes.size
        }
    }
}

object ProtoStreamHelper {
    /**
     * Reads a varint-length-prefixed message payload from an InputStream.
     */
    fun readDelimitedMessage(input: InputStream): ByteArray {
        var length = 0
        var shift = 0
        while (true) {
            val b = input.read()
            if (b == -1) throw EOFException("Socket closed by remote peer")
            length = length or ((b and 0x7F) shl shift)
            if ((b and 0x80) == 0) break
            shift += 7
            if (shift > 35) throw IllegalArgumentException("Varint length prefix too large")
        }
        val buffer = ByteArray(length)
        var totalRead = 0
        while (totalRead < length) {
            val count = input.read(buffer, totalRead, length - totalRead)
            if (count == -1) throw EOFException("Unexpected EOF while reading message payload ($totalRead/$length bytes read)")
            totalRead += count
        }
        return buffer
    }

    fun writeDelimitedMessage(output: OutputStream, writer: ProtoWriter) {
        output.write(writer.toDelimitedByteArray())
        output.flush()
    }
}
