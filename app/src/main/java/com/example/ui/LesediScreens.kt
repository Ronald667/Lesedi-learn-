package com.example.ui

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as GColor
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.CourseEntity
import com.example.data.LessonEntity
import com.example.ui.theme.LesediGold
import com.example.ui.theme.LesediGreen
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: LesediViewModel) {
    val language by viewModel.language.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val selectedCourse by viewModel.selectedCourse.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val showDsdDialog by viewModel.showDsdReportDialog.collectAsStateWithLifecycle()
    val showCertDialog by viewModel.showCertificateDialog.collectAsStateWithLifecycle()
    val snackbarMessage by viewModel.snackbarMessage.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackbar()
        }
    }

    if (selectedCourse != null) {
        BackHandler {
            viewModel.selectCourse(null)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column(modifier = Modifier.background(LesediGreen).fillMaxWidth().padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "LESEDI LEARN",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        modifier = Modifier.testTag("app_title")
                    )
                    Button(
                        onClick = { viewModel.toggleLanguage() },
                        colors = ButtonDefaults.buttonColors(containerColor = LesediGold, contentColor = Color.Black),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.height(36.dp).testTag("lang_toggle_button")
                    ) {
                        Text(text = if (language == "en") "Sepedi" else "English", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "MPO Programme • Ga-Sekororo • NPO: Lesedi Family Guard • ID: ${userProfile?.learnerId ?: "L-0893"}",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = Color.Black.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = if (language == "en") "Attendance" else "Tšhomisano", color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp)
                            Text(text = "${userProfile?.attendanceDays ?: 12} days", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = if (language == "en") "Data Mode" else "Mokgwa wa Data", color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp)
                            Text(text = userProfile?.dataMode ?: "Zero-rated ✓", color = LesediGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        },
        bottomBar = {
            if (selectedCourse == null) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { viewModel.setSelectedTab(0) },
                        icon = { Icon(Icons.Default.School, contentDescription = "Courses") },
                        label = { Text(if (language == "en") "Courses" else "Dithuto") },
                        modifier = Modifier.testTag("nav_courses")
                    )
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { viewModel.setSelectedTab(1) },
                        icon = { Icon(Icons.Default.Assessment, contentDescription = "DSD Report") },
                        label = { Text(if (language == "en") "DSD Report" else "Pego ya DSD") },
                        modifier = Modifier.testTag("nav_report")
                    )
                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { viewModel.setSelectedTab(2) },
                        icon = { Icon(Icons.Default.Verified, contentDescription = "Certificate") },
                        label = { Text(if (language == "en") "Certificate" else "Setifikeiti") },
                        modifier = Modifier.testTag("nav_certificate")
                    )
                }
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding).background(MaterialTheme.colorScheme.background)) {
            if (selectedCourse != null) {
                CourseDetailScreen(
                    course = selectedCourse!!,
                    viewModel = viewModel,
                    language = language,
                    onBack = { viewModel.selectCourse(null) }
                )
            } else {
                when (selectedTab) {
                    0 -> CoursesTab(viewModel = viewModel, language = language)
                    1 -> DsdReportTab(viewModel = viewModel, language = language)
                    2 -> CertificateTab(viewModel = viewModel, language = language)
                }
            }

            if (showDsdDialog) {
                DsdReportDialog(viewModel = viewModel, language = language)
            }

            if (showCertDialog) {
                CertificateDialog(viewModel = viewModel, language = language)
            }
        }
    }
}

@Composable
fun CoursesTab(viewModel: LesediViewModel, language: String) {
    val courses by viewModel.courses.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(LesediGreen.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = LesediGreen)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (language == "en") "Welcome, ${userProfile?.name ?: "Learner"}" else "Re a le amogela, ${userProfile?.name ?: "Learner"}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (language == "en") "Learn free, even offline. DSD approved tracking built-in." else "Ithute mahala, le ge go sena data. E dumelletšwe ke DSD.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        items(courses) { course ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.selectCourse(course) }
                    .testTag("course_card_${course.id}")
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = course.icon, fontSize = 32.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Surface(
                            color = if (course.type.contains("MPO")) LesediGreen else MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = course.type,
                                color = if (course.type.contains("MPO")) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (language == "en") course.title else course.sepediTitle,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${course.grade} • ${course.lessonsCount} ${if (language == "en") "lessons" else "dithuto"}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { course.progress / 100f },
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                            color = LesediGreen,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = if (language == "en") "DSD MPO Report (Auto-generated)" else "Pego ya DSD MPO",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (language == "en")
                            "Learner ID: ${userProfile?.learnerId} • Village: ${userProfile?.village} • Programme: Family Safety • Attendance exported for DSD monthly report ✓"
                        else
                            "Nomoro ya Morutwana: ${userProfile?.learnerId} • Motse: ${userProfile?.village} • Lenaneo: Polokego ya Lapa • E rometšwe go DSD ✓",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { viewModel.setShowDsdDialog(true) },
                        colors = ButtonDefaults.buttonColors(containerColor = LesediGreen),
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("download_dsd_button")
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = if (language == "en") "Download Attendance PDF for DSD" else "Khoasolla Pego ya DSD", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun DsdReportTab(viewModel: LesediViewModel, language: String) {
    val attendance by viewModel.attendance.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = if (language == "en") "Ga-Sekororo MPO Compliance" else "Tshepediso ya MPO ya Ga-Sekororo", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "NPO: ${userProfile?.npo}", fontSize = 13.sp)
                    Text(text = "Learner ID: ${userProfile?.learnerId}", fontSize = 13.sp)
                    Text(text = "Total Verified Attendance: ${userProfile?.attendanceDays} Days", fontSize = 13.sp, color = LesediGreen, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { viewModel.setShowDsdDialog(true) },
                        colors = ButtonDefaults.buttonColors(containerColor = LesediGreen),
                        modifier = Modifier.fillMaxWidth().testTag("export_pdf_btn")
                    ) {
                        Text(if (language == "en") "Export Monthly DSD Report PDF" else "Romela Pego ya Kgwedi ya DSD")
                    }
                }
            }
        }

        item {
            Text(text = if (language == "en") "Recent Attendance Logs" else "Dintlha tša Kgafetša", fontWeight = FontWeight.Bold, fontSize = 15.sp, modifier = Modifier.padding(top = 8.dp))
        }

        items(attendance) { att ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = att.date, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(text = att.note, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Surface(color = LesediGreen.copy(alpha = 0.1f), shape = RoundedCornerShape(6.dp)) {
                        Text(text = "Verified ✓", color = LesediGreen, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                }
            }
        }
    }
}

fun downloadCertificatePdf(context: Context, name: String, course: String) {
    val pdfDocument = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
    val page = pdfDocument.startPage(pageInfo)
    val canvas = page.canvas
    val paint = Paint()

    // Background white
    paint.color = GColor.WHITE
    canvas.drawRect(0f, 0f, 595f, 842f, paint)

    // Header green banner
    paint.color = GColor.parseColor("#007A33")
    canvas.drawRect(0f, 0f, 595f, 120f, paint)

    // Header text
    paint.color = GColor.WHITE
    paint.textSize = 22f
    paint.isFakeBoldText = true
    canvas.drawText("LESEDI FAMILY GUARD NPO", 40f, 60f, paint)
    paint.textSize = 14f
    canvas.drawText("LESEDI LEARN • GA-SEKORORO", 40f, 90f, paint)

    // Title
    paint.color = GColor.BLACK
    paint.textSize = 26f
    paint.isFakeBoldText = true
    canvas.drawText("Certificate of Completion", 40f, 190f, paint)

    // Content
    paint.textSize = 16f
    paint.isFakeBoldText = false
    canvas.drawText("This proudly certifies that:", 40f, 250f, paint)

    paint.textSize = 22f
    paint.isFakeBoldText = true
    paint.color = GColor.parseColor("#007A33")
    canvas.drawText(name, 40f, 290f, paint)

    paint.color = GColor.BLACK
    paint.textSize = 16f
    paint.isFakeBoldText = false
    canvas.drawText("Completed Programme: $course", 40f, 350f, paint)
    canvas.drawText("Village: Ga-Sekororo • Date: ${LocalDate.now()}", 40f, 390f, paint)
    canvas.drawText("Learner ID: L-0893 • NPO No: 294-...", 40f, 430f, paint)
    canvas.drawText("Verification: lesedilearn.co.za/verify", 40f, 470f, paint)

    paint.color = GColor.parseColor("#555555")
    paint.textSize = 13f
    canvas.drawText("This certificate is officially recognized for Department of Social Development (DSD) MPO reporting.", 40f, 540f, paint)

    pdfDocument.finishPage(page)

    val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Lesedi_Certificate_$name.pdf")
    try {
        pdfDocument.writeTo(FileOutputStream(file))
        pdfDocument.close()

        val intent = Intent(Intent.ACTION_VIEW).apply {
            val uri = Uri.fromFile(file)
            setDataAndType(uri, "application/pdf")
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file))
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share Certificate PDF"))
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

@Composable
fun CertificateTab(viewModel: LesediViewModel, language: String) {
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = LesediGold, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = "LESEDI LEARN CERTIFICATE", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = LesediGreen)
                    Text(text = "MPO Programme & Family Safety", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = if (language == "en") "This is proudly presented to" else "Sengwalo se se abelwa", fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = userProfile?.name ?: "Learner Sekororo", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Text(text = "ID: ${userProfile?.learnerId} • Village: ${userProfile?.village}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (language == "en")
                            "Has successfully completed required modules in Family Safety, Digital Literacy, and Youth Empowerment under Ga-Sekororo MPO / DSD Guidelines."
                        else
                            "O phadile dithuto tša Polokego ya Lapa le Bokgoni bja Digital.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = {
                            downloadCertificatePdf(context, userProfile?.name ?: "Learner Sekororo", "GBV Awareness & Family Safety")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LesediGreen, contentColor = Color.White),
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("download_cert_pdf_btn")
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "🎓 Download Real Certificate PDF", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { viewModel.setShowCertificateDialog(true) },
                        colors = ButtonDefaults.buttonColors(containerColor = LesediGold, contentColor = Color.Black),
                        modifier = Modifier.fillMaxWidth().height(44.dp).testTag("view_full_cert_btn")
                    ) {
                        Icon(Icons.Default.Visibility, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = if (language == "en") "View Official Certificate" else "Bona Setifikeiti", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9C4)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "🚨 Need emergency help? Download Lesedi Guard App on Play Store",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Click here to open Play Store",
                        color = LesediGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier
                            .clickable {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps"))
                                context.startActivity(intent)
                            }
                            .testTag("playstore_link")
                    )
                }
            }
        }
    }
}

@Composable
fun CourseDetailScreen(course: CourseEntity, viewModel: LesediViewModel, language: String, onBack: () -> Unit) {
    val lessons by viewModel.currentLessons.collectAsStateWithLifecycle()
    var quizAnswers by remember { mutableStateOf(mapOf<Int, String>()) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("back_button")) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(text = course.type, fontSize = 11.sp, color = LesediGreen, fontWeight = FontWeight.Bold)
                    Text(text = if (language == "en") course.title else course.sepediTitle, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }
        }

        items(lessons) { lesson ->
            var answerText by remember(lesson.id) { mutableStateOf(quizAnswers[lesson.id] ?: "") }

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth().testTag("lesson_card_${lesson.id}")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(text = "${lesson.id}. ${if (language == "en") lesson.title else lesson.sepediTitle}", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Play Video", tint = Color.White, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = lesson.videoInfo, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(text = "Sepedi AI Voice • Zero-rated streaming", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(text = "Quiz: ${lesson.quizQuestion}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = answerText,
                                onValueChange = {
                                    answerText = it
                                    quizAnswers = quizAnswers + (lesson.id to it)
                                },
                                placeholder = { Text(if (language == "en") "Type your answer..." else "Ngwala karabo ya gago...") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    viewModel.markLessonComplete(lesson)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (lesson.isCompleted) Color.Gray else LesediGreen
                                ),
                                modifier = Modifier.fillMaxWidth().height(44.dp).testTag("complete_lesson_${lesson.id}")
                            ) {
                                Text(
                                    text = if (lesson.isCompleted) {
                                        if (language == "en") "Completed ✓" else "E Fihlile ✓"
                                    } else {
                                        if (language == "en") "Mark Complete + Attendance" else "Phetha + Tšhomisano"
                                    },
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DsdReportDialog(viewModel: LesediViewModel, language: String) {
    Dialog(onDismissRequest = { viewModel.setShowDsdDialog(false) }) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Verified, contentDescription = null, tint = LesediGreen, modifier = Modifier.size(56.dp))
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = if (language == "en") "DSD Attendance Export" else "Pego ya DSD E Rolilwe", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (language == "en")
                        "Official Department of Social Development (DSD) MPO Monthly Attendance Report generated successfully for Ga-Sekororo Centre."
                    else
                        "Pego ya kgwedi ya DSD e dirilwe ka katlego bakeng sa Ga-Sekororo.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { viewModel.setShowDsdDialog(false) },
                    colors = ButtonDefaults.buttonColors(containerColor = LesediGreen),
                    modifier = Modifier.fillMaxWidth().testTag("close_dsd_dialog")
                ) {
                    Text(if (language == "en") "Done" else "Go Botse", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun CertificateDialog(viewModel: LesediViewModel, language: String) {
    Dialog(onDismissRequest = { viewModel.setShowCertificateDialog(false) }) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = LesediGold, modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = "Official Certificate Verified", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = LesediGreen)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "ID: L-0893 • Ga-Sekororo MPO Programme\nReady for School & DSD submission with cryptographic verification badge.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { viewModel.setShowCertificateDialog(false) },
                    colors = ButtonDefaults.buttonColors(containerColor = LesediGreen),
                    modifier = Modifier.fillMaxWidth().testTag("close_cert_dialog")
                ) {
                    Text(if (language == "en") "Close" else "Vala", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
