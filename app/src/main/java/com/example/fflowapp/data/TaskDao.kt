package com.example.fflowapp.data

import androidx.room.Dao
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

    /** Observe active (non-deleted) tasks ordered by startTime descending. */
    @Query("SELECT * FROM tasks WHERE isDeleted = 0 ORDER BY startTime DESC")
    fun getAllTasks(): Flow<List<Task>>

    /** Insert a single task; replaces on conflict by primary key. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: Task): Long

    /** Update an existing task (matched by id). */
    @Update
    suspend fun update(task: Task)

    /** Soft-delete: set isDeleted flag. */
    @Query("UPDATE tasks SET isDeleted = 1, updatedAt = :now WHERE id = :id")
    suspend fun softDelete(id: Long, now: Long = System.currentTimeMillis())

    /** Retrieve a single task by its id. */
    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: Long): Task?

    /** Observe active tasks sorted: pinned first, then by startTime descending. */
    @Query("SELECT * FROM tasks WHERE isDeleted = 0 ORDER BY isPinned DESC, startTime DESC")
    fun getTasksSorted(): Flow<List<Task>>

    /** Toggle the pinned flag on a task. */
    @Query("UPDATE tasks SET isPinned = :pinned WHERE id = :id")
    suspend fun setPinned(id: Long, pinned: Boolean)

    /** Set the done flag on a task. */
    @Query("UPDATE tasks SET isDone = :done, updatedAt = :now WHERE id = :id")
    suspend fun setDone(id: Long, done: Boolean, now: Long = System.currentTimeMillis())
}
