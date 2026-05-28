package com.example.fflowapp.data

import kotlinx.coroutines.flow.Flow

/**
 * Repository that wraps [TaskDao] and provides a clean API
 * for the ViewModel layer.
 */
class TaskRepository(private val taskDao: TaskDao) {

    /** Reactive stream of active (non-deleted) tasks, newest first. */
    val activeTasks: Flow<List<Task>> = taskDao.getAllTasks()

    /** Reactive stream of active tasks sorted: pinned first, then by startTime. */
    val sortedTasks: Flow<List<Task>> = taskDao.getTasksSorted()

    suspend fun insert(task: Task): Long = taskDao.insert(task)

    suspend fun update(task: Task) = taskDao.update(task)

    suspend fun softDelete(task: Task) =
        taskDao.softDelete(task.id, System.currentTimeMillis())

    suspend fun setPinned(id: Long, pinned: Boolean) =
        taskDao.setPinned(id, pinned)

    suspend fun toggleDone(id: Long, done: Boolean) =
        taskDao.setDone(id, done)
}
