package cs4750final.plannerapp

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import java.util.*
import androidx.core.content.FileProvider
import java.io.File

private const val TAG = "TaskFragment"
private const val ARG_TASK_ID = "task_id"
private const val DIALOG_DATE = "DialogDate"
private const val REQUEST_DATE = 0
private const val REQUEST_CONTACT = 1
private const val REQUEST_PHOTO = 2
private const val DATE_FORMAT = "EEE, MMM, dd"

class TaskFragment : Fragment(), DatePickerFragment.Callbacks {

    private lateinit var task: Task
    private lateinit var photoFile: File
    private lateinit var photoUri: Uri
    private lateinit var titleField: EditText
    private lateinit var detailsField: EditText
    private lateinit var dateButton: Button
    private lateinit var solvedCheckBox: CheckBox
    private lateinit var priorityCheckBox: CheckBox
    private lateinit var reportButton: Button
//    private lateinit var suspectButton: Button
    private lateinit var photoButton: ImageButton
    private lateinit var photoView: ImageView
    private val taskDetailViewModel: TaskDetailViewModel by lazy {
        ViewModelProviders.of(this).get(TaskDetailViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        task = Task()
        val taskId: UUID = arguments?.getSerializable(ARG_TASK_ID) as UUID
        taskDetailViewModel.loadTask(taskId)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_task, container, false)

        titleField = view.findViewById(R.id.task_title) as EditText
        detailsField = view.findViewById(R.id.task_detail) as EditText
        dateButton = view.findViewById(R.id.task_date) as Button
        solvedCheckBox = view.findViewById(R.id.task_solved) as CheckBox
        priorityCheckBox = view.findViewById(R.id.priority_task) as CheckBox
        reportButton = view.findViewById(R.id.task_report) as Button
//        suspectButton = view.findViewById(R.id.task_suspect) as Button
        photoButton = view.findViewById(R.id.task_camera) as ImageButton
        photoView = view.findViewById(R.id.task_photo) as ImageView

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val taskId = arguments?.getSerializable(ARG_TASK_ID) as UUID
        taskDetailViewModel.loadTask(taskId)
        taskDetailViewModel.taskLiveData.observe(
                viewLifecycleOwner,
                Observer { task ->
                    task?.let {
                        this.task = task
                        photoFile = taskDetailViewModel.getPhotoFile(task)
                        photoUri = FileProvider.getUriForFile(requireActivity(),
                                "cs4750final.plannerapp.fileprovider",
                                photoFile)
                        updateUI()
                    }
                })
    }

    override fun onStart() {
        super.onStart()

        val titleWatcher = object : TextWatcher {

            override fun beforeTextChanged(
                    sequence: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
            ) {
                // This space intentionally left blank
            }

            override fun onTextChanged(
                    sequence: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
            ) {
                task.title = sequence.toString()
            }

            override fun afterTextChanged(sequence: Editable?) {
                // This one too
            }
        }
        titleField.addTextChangedListener(titleWatcher)

        val detailWatcher = object : TextWatcher {
            override fun beforeTextChanged(
                    sequence: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
            ) {
                // This space intentionally left blank
            }

            override fun onTextChanged(
                    sequence: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
            ) {
                task.details = sequence.toString()
            }

            override fun afterTextChanged(sequence: Editable?) {
                // This one too
            }
        }

        detailsField.addTextChangedListener(detailWatcher)

        solvedCheckBox.apply {
            setOnCheckedChangeListener { _, isChecked ->
                task.isCompleted = isChecked
            }
        }

        priorityCheckBox.apply {
            setOnCheckedChangeListener { _, isChecked ->
                task.isPriority = isChecked
            }
        }

        dateButton.setOnClickListener {
            DatePickerFragment.newInstance(task.date).apply {
                setTargetFragment(this@TaskFragment, REQUEST_DATE)
                show(this@TaskFragment.requireFragmentManager(), DIALOG_DATE)
            }
        }

        reportButton.setOnClickListener {
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, getTaskReport())
                putExtra(
                        Intent.EXTRA_SUBJECT,
                        getString(R.string.task_report_subject))
            }.also { intent ->
                val chooserIntent =
                        Intent.createChooser(intent, getString(R.string.send_report))
                startActivity(chooserIntent)
            }
        }

//        suspectButton.apply {
//            val pickContactIntent =
//                    Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
//
//            setOnClickListener {
//                startActivityForResult(pickContactIntent, REQUEST_CONTACT)
//            }
//
//            val packageManager: PackageManager = requireActivity().packageManager
//            val resolvedActivity: ResolveInfo? =
//                    packageManager.resolveActivity(pickContactIntent,
//                            PackageManager.MATCH_DEFAULT_ONLY)
//            if (resolvedActivity == null) {
//                isEnabled = false
//            }
//        }

        photoButton.apply {
            val packageManager: PackageManager = requireActivity().packageManager

            val captureImage = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val resolvedActivity: ResolveInfo? =
                    packageManager.resolveActivity(captureImage,
                            PackageManager.MATCH_DEFAULT_ONLY)
            if (resolvedActivity == null) {
                isEnabled = false
            }

            setOnClickListener {
                captureImage.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)

                val cameraActivities: List<ResolveInfo> =
                        packageManager.queryIntentActivities(captureImage,
                                PackageManager.MATCH_DEFAULT_ONLY)

                for (cameraActivity in cameraActivities) {
                    requireActivity().grantUriPermission(
                            cameraActivity.activityInfo.packageName,
                            photoUri,
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )
                }

                startActivityForResult(captureImage, REQUEST_PHOTO)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        taskDetailViewModel.saveTask(task)
    }

    override fun onDetach() {
        super.onDetach()
        // Revoke photo permissions if the user leaves without taking a photo
        requireActivity().revokeUriPermission(photoUri,
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
    }

    override fun onDateSelected(date: Date) {
        task.date = date
        updateUI()
    }

    private fun updateUI() {
        titleField.setText(task.title)
        detailsField.setText(task.details)
        dateButton.text = task.date.toString()
        solvedCheckBox.apply {
            isChecked = task.isCompleted
            jumpDrawablesToCurrentState()
        }
        priorityCheckBox.apply {
            isChecked = task.isPriority
            jumpDrawablesToCurrentState()
        }
//        if (task.suspect.isNotEmpty()) {
//            suspectButton.text = task.suspect
//        }
        updatePhotoView()
    }

    private fun updatePhotoView() {
        if (photoFile.exists()) {
            val bitmap = getScaledBitmap(photoFile.path, requireActivity())
            photoView.setImageBitmap(bitmap)
        } else {
            photoView.setImageDrawable(null)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when {
            resultCode != Activity.RESULT_OK -> return

            requestCode == REQUEST_CONTACT && data != null -> {
                val contactUri: Uri? = data.data
                // Specify which fields you want your query to return values for.
                val queryFields = arrayOf(ContactsContract.Contacts.DISPLAY_NAME)
                // Perform your query - the contactUri is like a "where" clause here
                val cursor = contactUri?.let {
                    requireActivity().contentResolver
                            .query(it, queryFields, null, null, null)
                }
                cursor?.use {
                    // Double-check that you actually got results
                    if (it.count == 0) {
                        return
                    }

                    // Pull out the first column of the first row of data -
                    // that is your suspect's name.
                    it.moveToFirst()
//                    val suspect = it.getString(0)
//                    task.suspect = suspect
//                    taskDetailViewModel.saveTask(task)
//                    suspectButton.text = suspect
                }
            }

            requestCode == REQUEST_PHOTO -> {
                requireActivity().revokeUriPermission(photoUri,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION)

                updatePhotoView()
            }
        }
    }

    private fun getTaskReport(): String {
        val solvedString = if (task.isCompleted) {
            getString(R.string.task_report_solved)
        } else {
            getString(R.string.task_report_unsolved)
        }

        val dateString = DateFormat.format(DATE_FORMAT, task.date).toString()
        val suspect = if (task.suspect.isBlank()) {
            getString(R.string.task_report_no_suspect)
        } else {
            getString(R.string.task_report_suspect, task.suspect)
        }

        return getString(R.string.task_report,
                task.title, dateString, solvedString, suspect)
    }

    companion object {

        fun newInstance(taskId: UUID): TaskFragment {
            val args = Bundle().apply {
                putSerializable(ARG_TASK_ID, taskId)
            }
            return TaskFragment().apply {
                arguments = args
            }
        }
    }
}