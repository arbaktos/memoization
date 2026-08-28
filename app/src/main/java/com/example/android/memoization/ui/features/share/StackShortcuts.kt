package com.example.android.memoization.ui.features.share

import android.content.Context
import android.content.Intent
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.example.android.memoization.MainActivity
import com.example.android.memoization.R
import com.example.android.memoization.data.model.MemoStack
import com.example.android.memoization.data.repository.ReviewLogRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.min

/**
 * The stacks the system offers as targets of their own when a word is shared - the row of
 * suggestions above the app list, where a chat app puts the people written to most.
 *
 * A target is a dynamic shortcut, published and kept in step with the library; the system
 * decides the order it shows them in from how often each is used, so [reportUsed] is what
 * really teaches it. The ranking published here is only the first guess.
 */
@Singleton
class StackShortcuts @Inject constructor(
    @ApplicationContext private val context: Context,
    private val reviewLog: ReviewLogRepository,
) {

    /**
     * Replaces the published targets with the stacks that best deserve them: pinned first, then
     * the ones practised most recently, then the biggest.
     */
    suspend fun publish(stacks: List<MemoStack>) {
        val lastSession = reviewLog.lastSessionPerStack()
        val ranked = stacks
            .filter { it.isVisible }
            .sortedWith(
                compareByDescending<MemoStack> { it.pinnedTime ?: 0L }
                    .thenByDescending { lastSession[it.stackId] ?: 0L }
                    .thenByDescending { it.words.size }
            )
            .take(min(MAX_TARGETS, ShortcutManagerCompat.getMaxShortcutCountPerActivity(context)))

        ShortcutManagerCompat.setDynamicShortcuts(
            context,
            ranked.mapIndexed { rank, stack -> shortcutOf(stack, rank) }
        )
    }

    /** A word went into this stack: the system ranks the targets on what it is told here. */
    fun reportUsed(stackId: Long) {
        ShortcutManagerCompat.reportShortcutUsed(context, StackShortcutId.of(stackId))
    }

    private fun shortcutOf(stack: MemoStack, rank: Int): ShortcutInfoCompat {
        val name = stack.name.ifBlank { context.getString(R.string.untitled_stack) }
        // Tapped from the launcher instead of a share sheet, the shortcut opens the stack.
        val open = Intent(context, MainActivity::class.java)
            .setAction(Intent.ACTION_VIEW)
            .putExtra(EXTRA_STACK_ID, stack.stackId)
        return ShortcutInfoCompat.Builder(context, StackShortcutId.of(stack.stackId))
            .setShortLabel(name)
            .setLongLabel(name)
            .setIcon(IconCompat.createWithResource(context, R.drawable.ic_shortcut_stack))
            .setCategories(setOf(SHARE_CATEGORY))
            // Without this the system drops the target as soon as it is unpublished, and with
            // it the share sheet can keep offering a stack it has learnt the learner uses.
            .setLongLived(true)
            .setRank(rank)
            .setIntent(open)
            .build()
    }

    companion object {
        /** More would not be shown: the share sheet has room for a handful of targets. */
        const val MAX_TARGETS = 4

        /** Must match the category in res/xml/shortcuts.xml, or no target is ever offered. */
        const val SHARE_CATEGORY = "com.example.android.memoization.category.SHARE_INTO_STACK"

        /** Which stack a shortcut opened, when it was tapped in the launcher. */
        const val EXTRA_STACK_ID = "com.example.android.memoization.extra.STACK_ID"
    }
}

/** The id a stack's share target is published under, and the way back from it. */
object StackShortcutId {

    private const val PREFIX = "stack-"

    fun of(stackId: Long): String = PREFIX + stackId

    /** The stack a shortcut id names, or null if the id is not one of ours. */
    fun stackIdOf(shortcutId: String?): Long? =
        shortcutId?.takeIf { it.startsWith(PREFIX) }?.drop(PREFIX.length)?.toLongOrNull()
}

/**
 * The stack this intent already names: the target the learner picked in the share sheet, or the
 * shortcut they tapped in the launcher. Null when the app was opened any other way, and then
 * the stack is asked for on screen.
 */
fun Intent.shortcutStackId(): Long? {
    val shared = StackShortcutId.stackIdOf(getStringExtra(Intent.EXTRA_SHORTCUT_ID))
    if (shared != null) return shared
    val opened = getLongExtra(StackShortcuts.EXTRA_STACK_ID, NO_STACK)
    return if (opened == NO_STACK) null else opened
}

private const val NO_STACK = -1L
