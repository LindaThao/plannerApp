package cs4750final.plannerapp

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.*
import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "TaskListFragment"
private const val SAVED_SUBTITLE_VISIBLE = "subtitle"
private const val ARG_DATE = "date"

class TaskListFragment : Fragment() {

    private lateinit var taskRecyclerView: RecyclerView
    private var adapter: TaskAdapter? = TaskAdapter(emptyList())


    private val taskListViewModel: TaskListViewModel by lazy {
        ViewModelProviders.of(this).get("date", TaskListViewModel::class.java)
    }
    private var callbacks
            : Callbacks? = null

    interface Callbacks {
        fun onTaskSelected(taskId: UUID)
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as Callbacks?
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val selectedDate = arguments?.getStringArrayList(ARG_NAME)
        val view = inflater.inflate(R.layout.fragment_task_list, container, false)

        taskRecyclerView =
                view.findViewById(R.id.task_recycler_view) as RecyclerView
        taskRecyclerView.layoutManager = LinearLayoutManager(context)
        taskRecyclerView.adapter = adapter

        return view
    }

    override fun onStart() {
        super.onStart()
        taskListViewModel.taskListLiveData.observe(
                viewLifecycleOwner,
                Observer { tasks ->
                    tasks?.let {
                        Log.i(TAG, "Got taskLiveData ${tasks.size}")
                        updateUI(tasks)
                    }
                }
        )

    }

    override fun onDetach() {
        super.onDetach()
        callbacks = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_task_list, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.new_task -> {
                val task = Task()
                taskListViewModel.addTask(task)
                callbacks?.onTaskSelected(task.id)
                true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun updateUI(tasks: List<Task>) {
        adapter?.let {
            it.tasks = tasks
        } ?: run {
            adapter = TaskAdapter(tasks)
        }
        taskRecyclerView.adapter = adapter
    }

    private inner class TaskHolder(view: View)
        : RecyclerView.ViewHolder(view), View.OnClickListener {

        private lateinit var task: Task

        private val titleTextView: TextView = itemView.findViewById(R.id.task_title)
        private val dateTextView: TextView = itemView.findViewById(R.id.task_date)
        private val priorityImageView: ImageView = itemView.findViewById(R.id.priority_task)
//        private val solvedCheckbox: CheckBox = itemView.findViewById(R.id.task_done)
//        private val solvedImageView: ImageView = itemView.findViewById(R.id.task_solved)

        init {
            itemView.setOnClickListener(this)
//            solvedCheckbox.setOnCheckedChangeListener { _, isChecked ->
//                task.isSolved = isChecked
//                Log.i(TAG, "Task: $task.isSolved")

//                taskListViewModel.taskListLiveData.observe(
//                    viewLifecycleOwner,
//                    Observer { tasks ->
//                        tasks?.let {
//                            updateUI(tasks)
//                        }
//                    }
//                )
//            }
        }

        fun bind(task: Task) {
            this.task = task
            titleTextView.text = this.task.title
            val timeString = SimpleDateFormat("hh:mm a")
                    .format(this.task.date)
            dateTextView.text = timeString
//            solvedCheckbox.isChecked  = false
//            solvedImageView.visibility =
            if (task.isCompleted) {
//                solvedCheckbox.isChecked = true
                titleTextView.paintFlags = titleTextView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                dateTextView.paintFlags = dateTextView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                View.VISIBLE
            } else {
                View.GONE
            }

            priorityImageView.visibility =
            if (task.isPriority) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }

        override fun onClick(v: View) {
            callbacks?.onTaskSelected(task.id)
//            solvedCheckbox.setOnCheckedChangeListener { _, isChecked ->
//                task.isSolved = isChecked
//                Log.i(TAG, "Task: $task.isSolved")
//
////                updateUI(tasks)
//            }
        }
    }

    private inner class TaskAdapter(var tasks: List<Task>)
        : RecyclerView.Adapter<TaskHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
                : TaskHolder {
            val layoutInflater = LayoutInflater.from(context)
            val view = layoutInflater.inflate(R.layout.list_item_task, parent, false)
            return TaskHolder(view)
        }

        override fun onBindViewHolder(holder: TaskHolder, position: Int) {
            val task = tasks[position]
            holder.bind(task)
        }

        override fun getItemCount() = tasks.size
    }

//    companion object {
//        const val ARG_NAME = "selectedDate"
//
//        fun newInstance(): TaskListFragment {
//            return TaskListFragment()
//        }


//   Testing this code to see if it can connect two sides (calendar and selected date) - Pending
    companion object {
        const val ARG_NAME = "selectedDate"

        fun newInstance(): TaskListFragment {
            return TaskListFragment()
        }

//    fun newInstance(date: Date): TaskListFragment {
//
//        val fragment = TaskListFragment()
//
//        val bundle = Bundle().apply {
//            putSerializable(ARG_DATE, date)
//        }
//
//        fragment.arguments = bundle
//
//        return fragment
//        }
//    }

        fun newInstance(selectedDate: String?): TaskListFragment {
            val fragment = TaskListFragment()

            val bundle = Bundle().apply {
                putString(ARG_NAME, selectedDate)
            }

            fragment.arguments = bundle

            return fragment
        }
    }
}