package com.example.fflowapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fflowapp.data.Task
import com.example.fflowapp.data.TaskDao
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
 * - Task list loaded from Room DAO
 * - Vertical scroll offset (hour-based navigation)
 * - Horizontal scroll offset (date navigation, ±35% width constraint)
 * - Bottom sheet ("あとでやる") visibility
 * - Pin/unpin task toggling
 */
class FFlowViewModel(
    private val taskDao: TaskDao
) : ViewModel() {

    /** Currently selected date */
    private val _currentDate = MutableStateFlow(LocalDate.now())
    val currentDate: StateFlow<LocalDate> = _currentDate.asStateFlow()

    /** Current time, updated every minute to drive the "now" line */
    private val _currentTime = MutableStateFlow(LocalTime.now())
    val currentTime: StateFlow<LocalTime> = _currentTime.asStateFlow()

    /** Task list observed from Room DAO — reactive updates on insert/update/delete */
    val tasks: StateFlow<List<Task>> = taskDao.getAllTasks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    /** Task list sorted with pinned tasks first — used by the bottom sheet */
    val sortedTasks: StateFlow<List<Task>> = taskDao.getTasksSorted()
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

    /** Pin/unpin a task and persist via DAO. */
    fun togglePin(task: Task) {
        viewModelScope.launch {
            taskDao.setPinned(task.id, !task.pinned)
        }
    }
}
