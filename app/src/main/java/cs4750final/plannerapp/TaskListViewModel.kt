package cs4750final.plannerapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import java.util.*

class TaskListViewModel : ViewModel() {

    private val taskRepository = TaskRepository.get()

    //Shows all the tasks
    //val taskListLiveData = taskRepository.getTasks()

    //Shows all the events on the date defined.
    val d1 : Date = Date(2021, 0, 23)
    val taskListLiveData : LiveData<List<Task>> = taskRepository.getTaskByDate(d1)

    fun addTask(task: Task) {
        taskRepository.addTask(task)
    }
}