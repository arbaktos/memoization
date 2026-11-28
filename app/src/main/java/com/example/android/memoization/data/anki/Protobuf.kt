package com.example.android.memoization.data.anki

/**
 * Just enough of the protobuf wire format to read the two messages an Anki package needs: the
 * media list (MediaEntries) and a note type's kind (NotetypeConfig). Pulling in a protobuf
 * runtime and Anki's .proto files for that would be a lot more than the twenty lines it takes.
 */
internal class ProtoReader(private val bytes: ByteArray, private var pos: Int = 0, private val end: Int = bytes.size) {

    sealed interface Value {
        data class Varint(val value: Long) : Value
        class Bytes(val value: ByteArray) : Value
        object Other : Value
    }

    /** Every field of the message in order, as (field number, value). */
    fun fields(): List<Pair<Int, Value>> {
        val out = mutableListOf<Pair<Int, Value>>()
        while (pos < end) {
            val key = varint()
            val number = (key ushr 3).toInt()
            val value = when ((key and 7).toInt()) {
                0 -> Value.Varint(varint())
                1 -> { skip(8); Value.Other }
                2 -> {
                    val length = varint().toInt()
                    require(length >= 0 && pos + length <= end) { "Broken protobuf length" }
                    Value.Bytes(bytes.copyOfRange(pos, pos + length)).also { pos += length }
                }
                5 -> { skip(4); Value.Other }
                else -> throw IllegalArgumentException("Unsupported protobuf wire type")
            }
            out += number to value
        }
        return out
    }

    private fun skip(n: Int) {
        require(pos + n <= end) { "Broken protobuf" }
        pos += n
    }

    private fun varint(): Long {
        var result = 0L
        var shift = 0
        while (true) {
            require(pos < end && shift < 64) { "Broken protobuf varint" }
            val b = bytes[pos++].toInt()
            result = result or ((b and 0x7F).toLong() shl shift)
            if (b and 0x80 == 0) return result
            shift += 7
        }
    }
}
