package cs4750final.plannerapp

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.sql.Time
import java.util.*

@Entity
data class Task(@PrimaryKey
                val id: UUID = UUID.randomUUID(),
                var title: String = "",
                var date: Date = Date(),
                var dueTime: String = "",
                var isSolved: Boolean = false,
                var suspect: String = ""
) {
    val photoFileName
        get() = "IMG_$id.jpg"
}