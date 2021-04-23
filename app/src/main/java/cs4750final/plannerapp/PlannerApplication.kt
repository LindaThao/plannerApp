package cs4750final.plannerapp

import android.app.Application

class PlannerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        TaskRepository.initialize(this)
    }
}