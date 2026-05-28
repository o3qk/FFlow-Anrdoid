package com.example.fflowapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Entity representing a single task in the FFlow timeline.
 *
 * Fields:
 *   - [id]         : Auto-generated primary key
 *   - [title]      : Task title (required)
 *   - [memo]       : Optional additional notes
 *   - [startTime]  : Epoch millis for timeline placement
 *   - [isDone]     : Whether the task has been marked done
 *   - [isPinned]   : Whether the task is pinned to the top of the list
 *   - [isDeleted]  : Soft-delete flag (excluded from default queries)
 *   - [updatedAt]  : Last update timestamp
 */
@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val memo: String? = null,
    val startTime: Long = System.currentTimeMillis(),
    val isDone: Boolean = false,
    val isPinned: Boolean = false,
    val isDeleted: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)
