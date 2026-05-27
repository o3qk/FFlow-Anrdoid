package com.example.fflowapp.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Room DAO for [Task] CRUD operations.
 *
 * Exposes a reactive [Flow] of all tasks so the UI
 * automatically updates on any insert / update / delete.
 */
@Dao
interface TaskDao {

    /** Observe all tasks ordered by timestamp descending (newest first). */
    @Query("SELECT * FROM tasks ORDER BY timestamp DESC")
    fun getAllTasks(): Flow<List<Task>>

    /** Insert a single task; replaces on conflict by primary key. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: Task): Long

    /** Update an existing task (matched by id). */
    @Update
    suspend fun update(task: Task)

    /** Delete a specific task. */
    @Delete
    suspend fun delete(task: Task)

    /** Retrieve a single task by its id (for detail screens). */
    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: Long): Task?

    /** Observe all tasks sorted: pinned first, then by timestamp descending. */
    @Query("SELECT * FROM tasks ORDER BY pinned DESC, timestamp DESC")
    fun getTasksSorted(): Flow<List<Task>>

    /** Toggle the pinned flag on a task. */
    @Query("UPDATE tasks SET pinned = :pinned WHERE id = :id")
    suspend fun setPinned(id: Long, pinned: Boolean)
}
