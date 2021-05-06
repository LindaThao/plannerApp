package cs4750final.plannerapp

import android.content.Intent
import android.os.Bundle
import android.widget.CalendarView
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

private const val REQUEST_CODE_CHEAT = 0

class CalendarActivity : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        val calendarView = findViewById<CalendarView>(R.id.calendarView)
        calendarView?.setOnDateChangeListener { view, year, month, dayOfMonth ->
            val temp = month + 1
            val date = "$temp/$dayOfMonth/$year"
            val intent = Intent(this@CalendarActivity, MainActivity::class.java)
            intent.putExtra("date", date)
            startActivityForResult(intent, REQUEST_CODE_CHEAT)
        }
    }
}
