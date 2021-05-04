package cs4750final.plannerapp

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class Task(@PrimaryKey
                 val id: UUID = UUID.randomUUID(),
                 var title: String = "",
                 var details: String = "",
                 var date: Date = Date(),
                 var isSolved: Boolean = false,
                 var suspect: String = ""
//    var requiresPolice: Boolean = false
) {
    val photoFileName
        get() = "IMG_$id.jpg"
}