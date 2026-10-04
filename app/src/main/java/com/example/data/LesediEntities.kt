package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "courses")
data class CourseEntity(
    @PrimaryKey val id: Int,
    val grade: String,
    val title: String,
    val sepediTitle: String,
    val icon: String,
    val lessonsCount: Int,
    val progress: Int,
    val type: String
)

@Entity(tableName = "lessons")
data class LessonEntity(
    @PrimaryKey val id: Int,
    val courseId: Int,
    val title: String,
    val sepediTitle: String,
    val videoInfo: String,
    val quizQuestion: String,
    val isCompleted: Boolean = false
)

@Entity(tableName = "attendance")
data class AttendanceEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String,
    val note: String
)

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val name: String,
    val learnerId: String,
    val village: String,
    val npo: String,
    val attendanceDays: Int,
    val dataMode: String
)
