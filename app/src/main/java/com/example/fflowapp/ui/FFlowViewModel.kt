package com.example.fflowapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fflowapp.data.Task
import com.example.fflowapp.data.TaskRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

/**
 * ViewModel for the FFlow screen.
 *
 * Manages:
 * - Current date/time for the timeline display
 * - Task list loaded from Room via [TaskRepository]
 * - Vertical scroll offset (hour-based navigation)
 * - Horizontal scroll offset (date navigation, ±35% width constraint)
 * - Bottom sheet ("あとでやる") visibility
 * - Pin/unpin, done toggle, insert
 */
class FFlowViewModel(
    private val repository: TaskRepository
) : ViewModel() {

    /** Currently selected date */
    private val _currentDate = MutableStateFlow(LocalDate.now())
    val currentDate: StateFlow<LocalDate> = _currentDate.asStateFlow()

    /** Current time, updated every minute to drive the "now" line */
    private val _currentTime = MutableStateFlow(LocalTime.now())
    val currentTime: StateFlow<LocalTime> = _currentTime.asStateFlow()

    /** Active tasks observed from Room — reactive updates on insert/update/delete */
    val tasks: StateFlow<List<Task>> = repository.activeTasks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    /** Tasks sorted with pinned items first — used by the bottom sheet */
    val sortedTasks: StateFlow<List<Task>> = repository.sortedTasks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    /** Whether the "あとでやる" bottom sheet is visible */
    private val _showBottomSheet = MutableStateFlow(false)
    val showBottomSheet: StateFlow<Boolean> = _showBottomSheet.asStateFlow()

    /** Vertical scroll offset (hour rows); starts at current hour - 1 */
    private val _hourOffset = MutableStateFlow(
        (LocalTime.now().hour - 1).coerceAtLeast(0).toFloat()
    )
    val hourOffset: StateFlow<Float> = _hourOffset.asStateFlow()

    /** Horizontal scroll offset for date navigation (clamped to ±35%) */
    private val _dateOffset = MutableStateFlow(0f)
    val dateOffset: StateFlow<Float> = _dateOffset.asStateFlow()

    init {
        // Tick the clock every minute so the "now" line stays current
        viewModelScope.launch {
            while (true) {
                _currentTime.value = LocalTime.now()
                delay(60_000L)
            }
        }
    }

    /**
     * Update vertical hour offset from user scroll.
     * Clamped to non-negative (can't scroll above hour 0).
     */
    fun onHourScroll(offset: Float) {
        _hourOffset.value = offset.coerceAtLeast(0f)
    }

    /**
     * Update horizontal date offset from user scroll.
     * Clamped to ±35% of screen width as specified.
     */
    fun onDateScroll(offset: Float) {
        _dateOffset.value = offset.coerceIn(-0.35f, 0.35f)
    }

    /** Navigate directly to a specific date and reset horizontal offset. */
    fun setDate(date: LocalDate) {
        _currentDate.value = date
        _dateOffset.value = 0f
    }

    /** Jump back to today and reset both offsets (scroll to current hour). */
    fun goToToday() {
        _currentDate.value = LocalDate.now()
        _hourOffset.value = (LocalTime.now().hour - 1).coerceAtLeast(0).toFloat()
        _dateOffset.value = 0f
    }

    /** Toggle the bottom sheet visibility. */
    fun toggleBottomSheet() {
        _showBottomSheet.value = !_showBottomSheet.value
    }

    /** Pin/unpin a task and persist via Repository. */
    fun togglePin(task: Task) {
        viewModelScope.launch {
            repository.setPinned(task.id, !task.isPinned)
        }
    }

    /** Toggle done/un-done a task. */
    fun toggleDone(task: Task) {
        viewModelScope.launch {
            repository.toggleDone(task.id, !task.isDone)
        }
    }

    /** Insert a new task. */
    fun addTask(title: String, memo: String? = null, startTime: Long = System.currentTimeMillis()) {
        viewModelScope.launch {
            repository.insert(
                Task(title = title, memo = memo, startTime = startTime)
            )
        }
    }
}
