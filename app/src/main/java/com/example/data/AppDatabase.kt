package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [CourseEntity::class, LessonEntity::class, AttendanceEntity::class, UserProfileEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun lesediDao(): LesediDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "lesedi_learn_database"
                )
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database.lesediDao())
                    }
                }
            }

            private suspend fun populateInitialData(dao: LesediDao) {
                dao.insertUserProfile(
                    UserProfileEntity(
                        id = 1,
                        name = "Learner Sekororo",
                        learnerId = "L-0893",
                        village = "Ga-Sekororo",
                        npo = "Lesedi Family Guard",
                        attendanceDays = 12,
                        dataMode = "Zero-rated ✓"
                    )
                )

                val courses = listOf(
                    CourseEntity(1, "Grade 10-12", "Life Orientation - Family Safety", "Polokego ya Lapa", "🛡️", 8, 40, "MPO Programme"),
                    CourseEntity(2, "Grade 10-12", "Digital Skills & Phone Safety", "Bokgoni bja Digital", "📱", 6, 10, "MPO Programme"),
                    CourseEntity(3, "Grade 10", "Mathematics - Sepedi Explained", "Dipalo", "🔢", 12, 0, "CAPS Support"),
                    CourseEntity(4, "Grade 11", "GBV Awareness for Youth", "Go Lwantsha Tlhekefetšo", "🤝", 5, 0, "MPO Programme")
                )
                dao.insertCourses(courses)

                val lessons = listOf(
                    // Course 1
                    LessonEntity(101, 1, "What is GBV?", "Na Tlhekefetšo ke eng?", "AI Video: 3 min - Sepedi voice", "What is GBV?", true),
                    LessonEntity(102, 1, "How to report at SAPS / Thuthuzela", "Tsela ya go begela SAPS", "AI Video: 4 min", "Where do you report?", true),
                    LessonEntity(103, 1, "Panic Button & Family Plan", "Konopo ya Tshhoganetso", "AI Video: 2 min", "Who are your 2 trusted contacts?", false),
                    LessonEntity(104, 1, "Safe Transport & Walking Routes", "Metsela e e Sireletsegilego", "AI Video: 3 min", "Name one safe route rule.", false),
                    LessonEntity(105, 1, "Digital Safety & Cyberbullying", "Polokego ya Inthanete", "AI Video: 4 min", "What is online privacy?", false),
                    LessonEntity(106, 1, "Community Support Networks", "Matlotlo a Setšhaba", "AI Video: 3 min", "Who is your local leader?", false),
                    LessonEntity(107, 1, "Legal Rights for Youth", "Ditshwanelo tša Molao", "AI Video: 5 min", "What is your right to safety?", false),
                    LessonEntity(108, 1, "Final Family Safety Pledge", "Kano ya Polokego ya Lapa", "AI Video: 2 min", "Complete your pledge", false),

                    // Course 2
                    LessonEntity(201, 2, "Introduction to Smartphones", "Ditiragalo tša Mohala", "AI Video: 3 min", "How to lock your screen?", true),
                    LessonEntity(202, 2, "WhatsApp Safety & Scams", "Polokego ya WhatsApp le Ditšitšili", "AI Video: 4 min", "Should you share OTP?", false),
                    LessonEntity(203, 2, "Finding Educational Apps", "Go hwetša Ditsela tša Thuto", "AI Video: 3 min", "Name a learning app.", false),
                    LessonEntity(204, 2, "Zero-Rated Data Access", "Data ya Mahala", "AI Video: 2 min", "How to use zero-rated services?", false),
                    LessonEntity(205, 2, "Creating Strong Passwords", "Mantšu a Sephiri a Matla", "AI Video: 3 min", "Why use symbols?", false),
                    LessonEntity(206, 2, "Digital Citizenship", "Boagi bja Digital", "AI Video: 3 min", "Respect online?", false),

                    // Course 3
                    LessonEntity(301, 3, "Algebraic Expressions in Sepedi", "Dipalo tša Algebra ka Sepedi", "AI Video: 5 min", "Solve x + 2 = 5", false),
                    LessonEntity(302, 3, "Geometry & Triangles", "Dikhutlo tša Khutlonne", "AI Video: 4 min", "Sum of angles in triangle?", false),
                    LessonEntity(303, 3, "Functions & Graphs", "Dikgorwana le Dipalo", "AI Video: 6 min", "What is gradient?", false),

                    // Course 4
                    LessonEntity(401, 4, "Understanding Consent", "Tumelelo", "AI Video: 3 min", "What does consent mean?", false),
                    LessonEntity(402, 4, "Bystander Intervention", "Go Thusa Ba Bangwe", "AI Video: 4 min", "How to intervene safely?", false)
                )
                dao.insertLessons(lessons)

                dao.insertAttendance(AttendanceEntity(date = "2026-10-01", note = "Attended Family Safety Module 1"))
                dao.insertAttendance(AttendanceEntity(date = "2026-10-02", note = "Attended Digital Skills Module 1"))
            }
        }
    }
}
