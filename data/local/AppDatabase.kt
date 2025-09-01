package com.babetech.ucb_admin_access.data.local


import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [StudentEntity::class, AttendanceEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun studentDao(): StudentDao
    abstract fun attendanceDao(): AttendanceDao
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS attendance (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                studentMatricule TEXT NOT NULL,
                attendanceDate TEXT NOT NULL,
                attendanceTime TEXT NOT NULL,
                timestamp INTEGER NOT NULL,
                promotion TEXT,
                faculte TEXT,
                isPresent INTEGER NOT NULL DEFAULT 1,
                FOREIGN KEY(studentMatricule) REFERENCES students(matricule) ON DELETE CASCADE
            )
        """)
        
        database.execSQL("CREATE INDEX IF NOT EXISTS index_attendance_studentMatricule ON attendance(studentMatricule)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_attendance_attendanceDate ON attendance(attendanceDate)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_attendance_promotion ON attendance(promotion)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_attendance_faculte ON attendance(faculte)")
    }
}
