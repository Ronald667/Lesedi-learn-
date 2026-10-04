package com.example.data

import kotlinx.coroutines.flow.Flow

class LesediRepository(private val dao: LesediDao) {
    val courses: Flow<List<CourseEntity>> = dao.getAllCourses()
    val userProfile: Flow<UserProfileEntity?> = dao.getUserProfile()
    val attendance: Flow<List<AttendanceEntity>> = dao.getAllAttendance()

    fun getLessonsForCourse(courseId: Int): Flow<List<LessonEntity>> {
        return dao.getLessonsForCourse(courseId)
    }

    suspend fun updateLesson(lesson: LessonEntity) {
        dao.updateLesson(lesson)
    }

    suspend fun updateCourse(course: CourseEntity) {
        dao.updateCourse(course)
    }

    suspend fun addAttendance(attendance: AttendanceEntity) {
        dao.insertAttendance(attendance)
    }
}
