package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.AttendanceEntity
import com.example.data.CourseEntity
import com.example.data.LesediRepository
import com.example.data.LessonEntity
import com.example.data.UserProfileEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LesediViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: LesediRepository

    val courses: StateFlow<List<CourseEntity>>
    val userProfile: StateFlow<UserProfileEntity?>
    val attendance: StateFlow<List<AttendanceEntity>>

    private val _language = MutableStateFlow("en")
    val language: StateFlow<String> = _language.asStateFlow()

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _selectedCourse = MutableStateFlow<CourseEntity?>(null)
    val selectedCourse: StateFlow<CourseEntity?> = _selectedCourse.asStateFlow()

    private val _currentLessons = MutableStateFlow<List<LessonEntity>>(emptyList())
    val currentLessons: StateFlow<List<LessonEntity>> = _currentLessons.asStateFlow()

    private val _showDsdReportDialog = MutableStateFlow(false)
    val showDsdReportDialog: StateFlow<Boolean> = _showDsdReportDialog.asStateFlow()

    private val _showCertificateDialog = MutableStateFlow(false)
    val showCertificateDialog: StateFlow<Boolean> = _showCertificateDialog.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    init {
        val dao = AppDatabase.getDatabase(application).lesediDao()
        repository = LesediRepository(dao)

        courses = repository.courses.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        userProfile = repository.userProfile.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

        attendance = repository.attendance.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    fun toggleLanguage() {
        _language.value = if (_language.value == "en") "st" else "en"
    }

    fun setSelectedTab(tab: Int) {
        _selectedTab.value = tab
    }

    fun selectCourse(course: CourseEntity?) {
        _selectedCourse.value = course
        if (course != null) {
            viewModelScope.launch {
                repository.getLessonsForCourse(course.id).collect { lessons ->
                    _currentLessons.value = lessons
                }
            }
        } else {
            _currentLessons.value = emptyList()
        }
    }

    fun markLessonComplete(lesson: LessonEntity) {
        viewModelScope.launch {
            val updatedLesson = lesson.copy(isCompleted = true)
            repository.updateLesson(updatedLesson)

            val course = _selectedCourse.value
            if (course != null) {
                val lessons = _currentLessons.value
                val completedCount = lessons.count { it.id == lesson.id || it.isCompleted }
                val newProgress = if (lessons.isNotEmpty()) (completedCount * 100) / lessons.size else 0
                val updatedCourse = course.copy(progress = newProgress)
                repository.updateCourse(updatedCourse)
                _selectedCourse.value = updatedCourse
            }

            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val today = dateFormat.format(Date())
            repository.addAttendance(AttendanceEntity(date = today, note = "Completed lesson: ${lesson.title}"))

            _snackbarMessage.value = if (_language.value == "en") {
                "Lesson completed! Attendance logged for DSD ✓"
            } else {
                "Lesono le phethilwe! Tshedimosetso e bolokilwe ✓"
            }
        }
    }

    fun setShowDsdDialog(show: Boolean) {
        _showDsdReportDialog.value = show
    }

    fun setShowCertificateDialog(show: Boolean) {
        _showCertificateDialog.value = show
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }
}
