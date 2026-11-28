package com.example.android.memoization.data.anki

import com.example.android.memoization.data.model.Shown
import com.example.android.memoization.data.model.SideState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AnkiFormatTest {

    @Test
    fun plainText_dropsFormattingAndMediaAndKeepsLines() {
        assertEquals("hablar", AnkiText.toPlain("<b>hablar</b>"))
        assertEquals("говорить\nразговаривать", AnkiText.toPlain("говорить&nbsp;<br>разговаривать"))
        assertEquals("есть", AnkiText.toPlain("есть [sound:hablar.mp3]"))
        assertEquals("el gato", AnkiText.toPlain("el gato <img src=\"gato.png\">"))
        assertEquals("", AnkiText.toPlain("<img src='gato.png'>"))
        assertEquals("a & b <c>", AnkiText.toPlain("a &amp; b &lt;c&gt;"))
        assertEquals("да", AnkiText.toPlain("&#1076;&#x430;"))
        assertEquals("один\nдва", AnkiText.toPlain("<div>один</div><div>  два </div><div><br></div>"))
        assertEquals("x", AnkiText.toPlain("<style>.a{}</style>x"))
        assertEquals("&unknown;", AnkiText.toPlain("&unknown;"))
    }

    @Test
    fun cloze_isTheDoubleBraceGap() {
        assertTrue(AnkiText.hasCloze("Столица — {{c1::Мадрид}}"))
        assertTrue(AnkiText.hasCloze("{{c12::x::hint}}"))
        assertFalse(AnkiText.hasCloze("{{Front}}"))
    }

    @Test
    fun mediaNames_fromSoundsAndImages() {
        assertEquals(listOf("a.mp3", "b.png", "c d.jpg"), AnkiText.mediaNames("[sound:a.mp3] <img src=b.png> <img alt=\"\" src=\"c d.jpg\">"))
    }

    @Test
    fun mediaMap_readsBothFormats() {
        assertEquals(mapOf("1" to "hablar.mp3", "0" to "gato.png"), AnkiFormat.mediaMap("""{"1":"hablar.mp3","0":"gato.png"}""".toByteArray()))
        // MediaEntries exactly as Anki 25.9 wrote it, once unpacked.
        val proto = ("0a250a0a6861626c61722e6d7033108f011a1488f48cf95e8c4c0a89c5ee72cff8d92c1f7657320a220a08676174" +
            "6f2e706e67104e1a14c61020f4abc03eece6a7175707908abd827a31c3").chunked(2).map { it.toInt(16).toByte() }.toByteArray()
        assertEquals(mapOf("0" to "hablar.mp3", "1" to "gato.png"), AnkiFormat.mediaMap(proto))
    }

    @Test
    fun noteTypeKind_isFieldOneOfTheConfig() {
        assertEquals(AnkiFormat.KIND_CLOZE, AnkiFormat.noteTypeKind(byteArrayOf(0x08, 0x01, 0x1A, 0x00)))
        assertEquals(AnkiFormat.KIND_NORMAL, AnkiFormat.noteTypeKind(byteArrayOf(0x1A, 0x00, 0x48, 0x01)))
        assertEquals(AnkiFormat.KIND_NORMAL, AnkiFormat.noteTypeKind(null))
        assertEquals(AnkiFormat.KIND_NORMAL, AnkiFormat.noteTypeKind(byteArrayOf(0x0F)))
    }

    @Test
    fun deckNames() {
        assertEquals("Испанский::Глаголы", AnkiFormat.deckName("Испанский\u001fГлаголы"))
        assertEquals("Испанский · Глаголы", AnkiFormat.stackName("Испанский::Глаголы"))
        assertEquals("jezik", AnkiFormat.stackName("jezik"))
    }

    @Test
    fun legacyJson() {
        val types = AnkiFormat.legacyNoteTypes(
            """{"5":{"name":"Basic (and reversed card)","type":0,"flds":[{"name":"Back","ord":1},{"name":"Front","ord":0}]},"6":{"name":"Cloze","type":1,"flds":[]}}"""
        )
        assertEquals(listOf("Front", "Back"), types[5]!!.fieldNames)
        assertEquals(AnkiFormat.KIND_CLOZE, types[6]!!.kind)
        assertEquals(mapOf(1L to "Default", 2L to "A::B"), AnkiFormat.legacyDecks("""{"1":{"name":"Default"},"2":{"name":"A::B"}}"""))
    }

    @Test
    fun fsrsMemory_onlyWhenThere() {
        assertEquals(14.25 to 6.5, AnkiFormat.fsrsMemory("""{"s":14.25,"d":6.5,"dr":0.9}"""))
        assertNull(AnkiFormat.fsrsMemory("{}"))
        assertNull(AnkiFormat.fsrsMemory(""))
        assertNull(AnkiFormat.fsrsMemory("not json"))
    }

    private fun card(type: Int, queue: Int = 2, due: Long = 5, ivl: Int = 10, factor: Int = 2500, data: String? = "{}", ord: Int = 0) =
        AnkiCard(1, 1, 1, ord, type, queue, due, ivl, factor, 4, 1, 0, 0, data)

    private val created = 1_700_000_000L
    private val now = created * 1000 + 3_600_000L

    @Test
    fun schedule_newCardsAndLearningWithoutMemoryStartNew() {
        assertEquals(SideState.New, AnkiSchedule.side(card(type = 0), 1, Shown.WORD, created, null, now).state)
        assertEquals(SideState.New, AnkiSchedule.side(card(type = 1, queue = 1, due = created + 60, ivl = 0), 1, Shown.WORD, created, null, now).state)
    }

    @Test
    fun schedule_reviewCardFromSm2() {
        val side = AnkiSchedule.side(card(type = 2), 7, Shown.MEANING, created, null, now)
        assertEquals(SideState.Review, side.state)
        assertEquals(10.0, side.stability!!, 1e-9)
        assertEquals(5.0, side.difficulty!!, 1e-9)
        assertEquals((created + 5 * 86_400) * 1000, side.due)
        assertEquals(side.due!! - 10 * 86_400_000L, side.lastReview)
        assertEquals(4, side.reps)
        assertEquals(Shown.MEANING, side.shown)
    }

    @Test
    fun schedule_fsrsMemoryWins() {
        val side = AnkiSchedule.side(card(type = 1, queue = 1, due = created + 600, ivl = 0, data = """{"s":0.8,"d":7.1}"""), 1, Shown.WORD, created, 123L, now)
        assertEquals(0.8, side.stability!!, 1e-9)
        assertEquals(7.1, side.difficulty!!, 1e-9)
        assertEquals((created + 600) * 1000, side.due)
        assertEquals(123L, side.lastReview)
    }

    @Test
    fun difficulty_followsTheEase() {
        assertEquals(5.0, AnkiSchedule.difficultyOf(2500), 1e-9)
        assertEquals(10.0, AnkiSchedule.difficultyOf(1300), 1e-9)
        assertEquals(1.0, AnkiSchedule.difficultyOf(4000), 1e-9)
        assertEquals(5.0, AnkiSchedule.difficultyOf(0), 1e-9)
    }

    @Test
    fun templates_frontIsTheWordSide() {
        assertEquals(Shown.WORD, AnkiSchedule.shownOf(0))
        assertEquals(Shown.MEANING, AnkiSchedule.shownOf(1))
        assertNull(AnkiSchedule.shownOf(2))
    }
}
