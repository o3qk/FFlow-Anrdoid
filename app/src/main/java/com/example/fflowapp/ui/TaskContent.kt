package com.example.fflowapp.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.minHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.fflowapp.data.Task

/**
 * The visible content of a task item.
 *
 * - Title: 1 line max, ellipsis overflow.
 * - Memo (if present): up to 2 lines, ellipsis overflow.
 * - Completed styling: gray text + [TextDecoration.LineThrough].
 *
 * Total textual lines = max 3 (1 title + 2 memo).
 */
@Composable
fun TaskContent(task: Task) {
    val contentAlpha = if (task.completed) 0.5f else 1f
    val titleColor = MaterialTheme.colorScheme.onSurface.copy(alpha = contentAlpha)
    val metaColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = contentAlpha)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .minHeight(32.dp)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            // Title line (line 1 of 3)
            Text(
                text = task.title,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = titleColor,
                textDecoration = if (task.completed) {
                    TextDecoration.LineThrough
                } else {
                    TextDecoration.None
                }
            )

            // Memo lines (lines 2–3 of 3, only if present)
            if (!task.memo.isNullOrBlank()) {
                Text(
                    text = task.memo,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = metaColor
                )
            }
        }
    }
}
