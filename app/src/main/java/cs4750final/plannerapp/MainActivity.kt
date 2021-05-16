package cs4750final.plannerapp

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import java.util.*

private const val TAG = "MainActivity"
private const val SELECTED_DATE = "cs4750final.plannerapp.selected_date"

class MainActivity : AppCompatActivity(),
    TaskListFragment.Callbacks {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val date=intent.getStringExtra("date")
        Log.d(TAG, "got date $date")

        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (currentFragment == null) {
            val fragment = TaskListFragment.newInstance(date)
            supportFragmentManager
                .beginTransaction()
                .add(R.id.fragment_container, fragment)
                .commit()
        }
    }

    override fun onTaskSelected(taskId: UUID) {
        val fragment = TaskFragment.newInstance(taskId)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    //edited by Linda
//    companion object {
//        fun newIntent(packageContext: Context, selectedDate: String):
//                Intent {
//            return Intent(packageContext,
//                    MainActivity::class.java).apply {
//                putExtra(SELECTED_DATE, selectedDate)
//            }
//        }
//    }
}