package com.example.android.memoization.data.anki

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/** The parts of an Anki package's format that are plain data, readable without Android. */
object AnkiFormat {

    /** Anki's separator between a note's fields, and in schema 18 between deck name parts. */
    const val FIELD_SEPARATOR = '\u001f'

    /** Note type kinds, as Anki numbers them. */
    const val KIND_NORMAL = 0
    const val KIND_CLOZE = 1

    private val ZSTD_MAGIC = byteArrayOf(0x28, 0xB5.toByte(), 0x2F, 0xFD.toByte())

    fun isZstd(head: ByteArray): Boolean =
        head.size >= 4 && (0 until 4).all { head[it] == ZSTD_MAGIC[it] }

    /**
     * Which zip entry holds which media file. Older packages say it as JSON, {"0": "cat.jpg"};
     * newer ones as a protobuf list, where an entry's place in the list is its zip name unless
     * it says otherwise.
     */
    fun mediaMap(bytes: ByteArray): Map<String, String> {
        val text = bytes.toString(Charsets.UTF_8).trim()
        if (text.startsWith("{")) {
            return Json.parseToJsonElement(text).jsonObject.mapValues { it.value.jsonPrimitive.content }
        }
        val out = linkedMapOf<String, String>()
        ProtoReader(bytes).fields()
            .filter { it.first == 1 }
            .mapNotNull { it.second as? ProtoReader.Value.Bytes }
            .forEachIndexed { index, entry ->
                val fields = ProtoReader(entry.value).fields()
                val name = (fields.firstOrNull { it.first == 1 }?.second as? ProtoReader.Value.Bytes)
                    ?.value?.toString(Charsets.UTF_8) ?: return@forEachIndexed
                val zipName = (fields.firstOrNull { it.first == 255 }?.second as? ProtoReader.Value.Varint)
                    ?.value ?: index.toLong()
                out[zipName.toString()] = name
            }
        return out
    }

    /** A note type's kind from its schema 18 config blob: field 1, absent for a normal one. */
    fun noteTypeKind(config: ByteArray?): Int {
        if (config == null) return KIND_NORMAL
        return runCatching {
            (ProtoReader(config).fields().firstOrNull { it.first == 1 }?.second as? ProtoReader.Value.Varint)
                ?.value?.toInt()
        }.getOrNull() ?: KIND_NORMAL
    }

    /** "Испанский::Глаголы" whichever separator the schema used. */
    fun deckName(stored: String): String = stored.replace(FIELD_SEPARATOR.toString(), "::")

    /** How a deck reads as a stack's name: "Испанский · Глаголы". */
    fun stackName(deckName: String): String =
        deckName.split("::").map { it.trim() }.filter { it.isNotEmpty() }.joinToString(" · ").ifEmpty { deckName }

    /** Schema 11 keeps note types in one JSON column: id to (name, kind, field names by ord). */
    fun legacyNoteTypes(json: String): Map<Long, AnkiNoteType> =
        Json.parseToJsonElement(json).jsonObject.mapNotNull { (id, value) ->
            val model = value.jsonObject
            val fields = model["flds"]?.jsonArray?.map { it.jsonObject }
                ?.sortedBy { it["ord"]?.jsonPrimitive?.intOrNull ?: 0 }
                ?.map { it["name"]?.jsonPrimitive?.content.orEmpty() }
                .orEmpty()
            id.toLongOrNull()?.let {
                it to AnkiNoteType(
                    id = it,
                    name = model["name"]?.jsonPrimitive?.content.orEmpty(),
                    kind = model["type"]?.jsonPrimitive?.intOrNull ?: KIND_NORMAL,
                    fieldNames = fields,
                )
            }
        }.toMap()

    /** Schema 11 keeps decks in one JSON column too: id to name. */
    fun legacyDecks(json: String): Map<Long, String> =
        Json.parseToJsonElement(json).jsonObject.mapNotNull { (id, value) ->
            val name = value.jsonObject["name"]?.jsonPrimitive?.content ?: return@mapNotNull null
            id.toLongOrNull()?.let { it to deckName(name) }
        }.toMap()

    /** The FSRS memory Anki 23.10+ keeps in a card's data, {"s": 14.2, "d": 6.5}, if it has one. */
    fun fsrsMemory(data: String?): Pair<Double, Double>? {
        if (data.isNullOrBlank()) return null
        val json = runCatching { Json.parseToJsonElement(data) as? JsonObject }.getOrNull() ?: return null
        val s = (json["s"] as? JsonPrimitive)?.doubleOrNull ?: return null
        val d = (json["d"] as? JsonPrimitive)?.doubleOrNull ?: return null
        return if (s > 0) s to d else null
    }
}
