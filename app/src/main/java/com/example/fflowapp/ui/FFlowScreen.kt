package com.example.fflowapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fflowapp.data.Task
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

/** Height allocated to each one-hour slot in the timeline. */
private val HOUR_HEIGHT_DP = 60.dp

/** Width of the left-side hour-number column. */
private val HOUR_COLUMN_WIDTH_DP = 48.dp

/** Colour used for the "now" indicator line. */
private val NOW_LINE_COLOR = Color(0xFFE53935)

/**
 * Top-level FFlow screen composable.
 *
 * Houses the top bar (date + action icons) and the scrollable
 * hour-column / task timeline.
 *
 * @param viewModel Provides reactive state for date, time, tasks and scroll offsets.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FFlowScreen(viewModel: FFlowViewModel = viewModel()) {
    val currentDate by viewModel.currentDate.collectAsState()
    val currentTime by viewModel.currentTime.collectAsState()
    val tasks by viewModel.tasks.collectAsState()
    val sortedTasks by viewModel.sortedTasks.collectAsState()
    val hourOffset by viewModel.hourOffset.collectAsState()
    val dateOffset by viewModel.dateOffset.collectAsState()
    val showBottomSheet by viewModel.showBottomSheet.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        FFlowTopBar(
            currentDate = currentDate,
            onTodayClick = { viewModel.goToToday() },
            onToggleBottomSheet = { viewModel.toggleBottomSheet() }
        )

        FFlowTimeline(
            currentTime = currentTime,
            tasks = tasks,
            hourOffset = hourOffset,
            dateOffset = dateOffset,
            onHourScroll = viewModel::onHourScroll,
            onDateScroll = viewModel::onDateScroll
        )
    }

    // "あとでやる" BottomSheet
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.toggleBottomSheet() },
            sheetState = sheetState
        ) {
            FFlowBottomSheetContent(
                tasks = sortedTasks,
                onTogglePin = { task -> viewModel.togglePin(task) }
            )
        }
    }
}

// ---------------------------------------------------------------------------
//  Top bar
// ---------------------------------------------------------------------------

/**
 * Top bar showing the selected date (day month year) and three action buttons.
 *
 * Layout:
 *   [Day (large)]                  [⚙] [ℹ] [📅]
 *   [Month Year]
 */
@Composable
private fun FFlowTopBar(
    currentDate: LocalDate,
    onTodayClick: () -> Unit = {},
    onToggleBottomSheet: () -> Unit = {}
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            // Left side: day number + month / year (tap to jump to today)
            Column(
                modifier = Modifier.clickable { onTodayClick() }
            ) {
                Text(
                    text = currentDate.dayOfMonth.toString(),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = buildString {
                        append(currentDate.month.getDisplayName(TextStyle.SHORT, Locale.getDefault()))
                        append(" ")
                        append(currentDate.year)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }

            // Right side: action icon buttons
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                IconButton(onClick = { onToggleBottomSheet() }) {
                    Icon(
                        imageVector = Icons.Default.PushPin,
                        contentDescription = "あとでやる",
                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                }
                IconButton(onClick = { /* Open settings */ }) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                }
                IconButton(onClick = { /* Open stats */ }) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Stats",
                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                }
                IconButton(onClick = { /* Open month view */ }) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Month",
                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    }
}

// ---------------------------------------------------------------------------
//  Timeline
// ---------------------------------------------------------------------------

/**
 * Main timeline area consisting of an hour-labels column, a vertical divider,
 * and a task area — all scrolling vertically as one unit.
 *
 * Gesture handling:
 * - Vertical drags modify [hourOffset] (scrolling through hours).
 * - Horizontal drags modify [dateOffset] (peek into adjacent dates, clamped
 *   to ±35 % of screen width).
 */
@Composable
private fun FFlowTimeline(
    currentTime: LocalTime,
    tasks: List<Task>,
    hourOffset: Float,
    dateOffset: Float,
    onHourScroll: (Float) -> Unit,
    onDateScroll: (Float) -> Unit
) {
    val density = LocalDensity.current
    val hourHeightPx = with(density) { HOUR_HEIGHT_DP.toPx() }
    val verticalScrollState = rememberScrollState()

    // One-time initial scroll to the hour offset from the ViewModel.
    LaunchedEffect(Unit) {
        val targetPx = (hourOffset * hourHeightPx).toInt()
        verticalScrollState.scrollTo(targetPx)
    }

    // Sync ViewModel hourOffset -> scroll state.
    // Used when the ViewModel jumps to a date (e.g. goToToday()).
    // A 1-hour threshold prevents feedback when the user scrolls.
    LaunchedEffect(hourOffset) {
        val targetPx = (hourOffset * hourHeightPx).toInt()
        if (abs(verticalScrollState.value - targetPx) > hourHeightPx.toInt()) {
            verticalScrollState.scrollTo(targetPx)
        }
    }

    // Observe scroll changes and push back to ViewModel.
    // SnapshotFlow re-reads both dependencies on each emission so the
    // hourOffset check always uses the latest value, preventing feedback loops.
    LaunchedEffect(Unit) {
        snapshotFlow { verticalScrollState.value to hourOffset }
            .collect { (px, currentHourOffset) ->
                val newOffset = px.toFloat() / hourHeightPx
                if (abs(newOffset - currentHourOffset) > 0.5f) {
                    onHourScroll(newOffset)
                }
            }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            // Horizontal drag -> date offset
            .pointerInput(Unit) {
                detectHorizontalDragGestures { _, dragAmount ->
                    val screenWidth = size.width.toFloat()
                    val newOffset = dateOffset + dragAmount / screenWidth
                    onDateScroll(newOffset)
                }
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(x = (dateOffset * size.width).roundToInt(), y = 0) }
                .verticalScroll(verticalScrollState)
        ) {
            // ----- Hour labels column -----
            Column(
                modifier = Modifier
                    .width(HOUR_COLUMN_WIDTH_DP)
                    .padding(end = 4.dp)
            ) {
                // Top padding so the first hour isn't flush with the top edge
                Spacer(modifier = Modifier.height(HOUR_HEIGHT_DP * 0.5f))

                for (hour in 0..23) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(HOUR_HEIGHT_DP),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Text(
                            text = String.format("%02d", hour),
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.45f)
                        )
                    }
                }

                // Bottom padding
                Spacer(modifier = Modifier.height(HOUR_HEIGHT_DP * 0.5f))
            }

            // ----- Vertical timeline line -----
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            )

            // ----- Task area -----
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp)
            ) {
                // Top padding (must match hours column)
                Spacer(modifier = Modifier.height(HOUR_HEIGHT_DP * 0.5f))

                for (hour in 0..23) {
                    val tasksForHour = tasks.filter { task ->
                        val taskHourOfDay = (task.startTime % 86_400_000L) / 3_600_000L
                        taskHourOfDay.toInt() == hour
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(HOUR_HEIGHT_DP)
                    ) {
                        // Semi-transparent hour-divider line at the top
                        HorizontalDivider(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.TopCenter),
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f)
                        )

                        // Tasks stacked vertically inside the hour slot
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(start = 8.dp, end = 4.dp, top = 2.dp)
                        ) {
                            tasksForHour.forEach { task ->
                                TaskContent(task = task)
                            }
                        }

                        // "Now" red line at the top of the current hour slot
                        if (hour == currentTime.hour) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(2.dp)
                                    .background(NOW_LINE_COLOR)
                                    .align(Alignment.TopCenter)
                            )

                            // Small dot aligned with the vertical timeline line
                            Box(
                                modifier = Modifier
                                    .offset(x = (-4).dp)
                                    .width(8.dp)
                                    .height(8.dp)
                                    .background(NOW_LINE_COLOR, shape = MaterialTheme.shapes.extraLarge)
                                    .align(Alignment.TopCenter)
                            )
                        }
                    }
                }

                // Bottom padding
                Spacer(modifier = Modifier.height(HOUR_HEIGHT_DP * 0.5f))
            }
        }
    }
}

// ---------------------------------------------------------------------------
//  "あとでやる" Bottom Sheet content
// ---------------------------------------------------------------------------

/**
 * Content for the "あとでやる" bottom sheet.
 *
 * Displays all tasks sorted with pinned items first.
 * Each row shows [TaskContent] + a Pin toggle button.
 *
 * The [ModalBottomSheet] handles peek(1-line) / expand via its sheet state.
 */
@Composable
private fun FFlowBottomSheetContent(
    tasks: List<Task>,
    onTogglePin: (Task) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        // Drag handle
        HorizontalDivider(
            modifier = Modifier
                .padding(horizontal = 120.dp, vertical = 8.dp),
            thickness = 3.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        )

        Text(
            text = "あとでやる",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 80.dp, max = 400.dp)
        ) {
            items(tasks, key = { it.id }) { task ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Task content (takes remaining space)
                    Box(modifier = Modifier.weight(1f)) {
                        TaskContent(task = task)
                    }

                    // Pin toggle button
                    IconButton(onClick = { onTogglePin(task) }) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = if (task.isPinned) "Unpin" else "Pin",
                            tint = if (task.isPinned) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}


