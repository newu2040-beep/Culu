package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [WaterEntry::class, ReminderItem::class, ReminderLog::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun waterDao(): WaterDao
    abstract fun reminderDao(): ReminderDao
    abstract fun reminderLogDao(): ReminderLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "culu_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }

            private suspend fun populateInitialData(database: AppDatabase) {
                val reminderDao = database.reminderDao()
                // Default starter wellness routines for instant delight
                reminderDao.insertReminder(
                    ReminderItem(
                        title = "Morning Hydration",
                        type = ReminderItem.TYPE_WATER,
                        dosage = "300 ml",
                        timeMinutes = 480, // 8:00 AM
                        daysOfWeek = "1,2,3,4,5,6,7",
                        isNotificationEnabled = true,
                        isActive = true,
                        iconName = "water",
                        categoryName = "Hydration"
                    )
                )
                reminderDao.insertReminder(
                    ReminderItem(
                        title = "Vitamin D & Omega-3",
                        type = ReminderItem.TYPE_MEDICINE,
                        dosage = "1 capsule with meal",
                        timeMinutes = 540, // 9:00 AM
                        daysOfWeek = "1,2,3,4,5,6,7",
                        isNotificationEnabled = true,
                        isActive = true,
                        iconName = "pill",
                        categoryName = "Supplements"
                    )
                )
                reminderDao.insertReminder(
                    ReminderItem(
                        title = "Afternoon Reset Walk",
                        type = ReminderItem.TYPE_CUSTOM,
                        dosage = "15 min stretch",
                        timeMinutes = 870, // 2:30 PM
                        daysOfWeek = "1,2,3,4,5",
                        isNotificationEnabled = true,
                        isActive = true,
                        iconName = "walk",
                        categoryName = "Routine"
                    )
                )
                reminderDao.insertReminder(
                    ReminderItem(
                        title = "Evening Wind-Down",
                        type = ReminderItem.TYPE_CUSTOM,
                        dosage = "Herbal tea & screen off",
                        timeMinutes = 1320, // 10:00 PM
                        daysOfWeek = "1,2,3,4,5,6,7",
                        isNotificationEnabled = true,
                        isActive = true,
                        iconName = "bed",
                        categoryName = "Sleep"
                    )
                )
            }
        }
    }
}
