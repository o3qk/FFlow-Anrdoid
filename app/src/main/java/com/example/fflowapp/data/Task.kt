package com.example.fflowapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Entity representing a single task in the FFlow timeline.
 *
 * Matches the simple entity spec:
 *   - [id]       : Auto-generated primary key
 *   - [title]    : Task title (required)
 *   - [memo]     : Optional additional notes (displayed as second line)
 *   - [completed]: Whether the task has been marked done
 *   - [pinned]   : Whether the task is pinned to the top of the timeline
 *   - [timestamp]: Epoch millis for ordering / timeline placement
 */
@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val memo: String? = null,
    val completed: Boolean = false,
    val pinned: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
