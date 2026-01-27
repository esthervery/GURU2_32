package com.android.guru2.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.guru2.R
import com.android.guru2.data.model.DiaryEntry
import com.android.guru2.data.model.HealthRecord
import com.android.guru2.data.model.ImportantSchedule
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel = viewModel(),
    onNavigateToHome: () -> Unit,
    onNavigateToCommunity: () -> Unit = {}
) {
    val selectedDate by viewModel.selectedDate.collectAsState()
    val currentMonth by viewModel.currentMonth.collectAsState()
    val diaries by viewModel.diaries.collectAsState()
    val healthRecords by viewModel.healthRecords.collectAsState()
    val schedules by viewModel.schedules.collectAsState()
    val datesWithDiary by viewModel.datesWithDiary.collectAsState()
    val datesWithHealth by viewModel.datesWithHealth.collectAsState()
    val datesWithSchedule by viewModel.datesWithSchedule.collectAsState()

    var showAddMenu by remember { mutableStateOf(false) }

    //  “추가” 다이얼로그용
    var showDiaryDialog by remember { mutableStateOf(false) }
    var showHealthDialog by remember { mutableStateOf(false) }
    var showScheduleDialog by remember { mutableStateOf(false) }

    //  상세보기 다이얼로그용
    var showDiaryDetail by remember { mutableStateOf<DiaryEntry?>(null) }
    var showHealthDetail by remember { mutableStateOf<HealthRecord?>(null) }
    var showScheduleDetail by remember { mutableStateOf<ImportantSchedule?>(null) }

    //  “수정”은 따로 다이얼로그를 만들지 않고, 기존 다이얼로그를 prefill로 재사용
    var editingDiary by remember { mutableStateOf<DiaryEntry?>(null) }
    var editingHealth by remember { mutableStateOf<HealthRecord?>(null) }
    var editingSchedule by remember { mutableStateOf<ImportantSchedule?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "캘린더", style = MaterialTheme.typography.titleLarge)
                    }
                },
                actions = { Spacer(modifier = Modifier.width(48.dp)) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddMenu = !showAddMenu },
                containerColor = Color(0xFFF0724A),
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "추가",
                    tint = Color.White
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                CalendarView(
                    currentMonth = currentMonth,
                    selectedDate = selectedDate,
                    datesWithDiary = datesWithDiary,
                    datesWithHealth = datesWithHealth,
                    datesWithSchedule = datesWithSchedule,
                    onDateSelected = { viewModel.selectDate(it) },
                    onMonthChanged = { viewModel.changeMonth(it) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "${selectedDate.year}년 ${selectedDate.monthValue}월 ${selectedDate.dayOfMonth}일(${
                        selectedDate.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.KOREAN)
                    })",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 20.dp),
                    color = Color(0xFFF0724A)
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(schedules) { schedule ->
                        ScheduleItem(schedule = schedule, onClick = { showScheduleDetail = schedule })
                    }
                    items(diaries) { diary ->
                        DiaryItem(diary = diary, onClick = { showDiaryDetail = diary })
                    }
                    items(healthRecords) { record ->
                        HealthRecordItem(record = record, onClick = { showHealthDetail = record })
                    }
                }
            }

            // + 버튼 메뉴
            if (showAddMenu) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 20.dp, bottom = 90.dp)
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AddMenuItem(
                        icon = R.drawable.ic_schedule,
                        text = "중요 일정",
                        onClick = {
                            showScheduleDialog = true
                            showAddMenu = false
                        }
                    )
                    AddMenuItem(
                        icon = R.drawable.ic_diary,
                        text = "하루 일기",
                        onClick = {
                            showDiaryDialog = true
                            showAddMenu = false
                        }
                    )
                    AddMenuItem(
                        icon = R.drawable.ic_health_record,
                        text = "건강 기록",
                        onClick = {
                            showHealthDialog = true
                            showAddMenu = false
                        }
                    )
                }
            }
        }
    }

    //  추가(빈 값) 다이얼로그들
    if (showDiaryDialog) {
        DiaryDialog(
            onDismiss = { showDiaryDialog = false },
            onSave = { title, content ->
                viewModel.saveDiary(
                    title = title,
                    content = content,
                    onSuccess = { showDiaryDialog = false },
                    onError = { }
                )
            },
            initialTitle = "",
            initialContent = "",
            headerTitle = "하루 일기"
        )
    }

    if (showHealthDialog) {
        HealthRecordDialog(
            onDismiss = { showHealthDialog = false },
            onSave = { symptom, treatment, foodIntake, weight ->
                viewModel.saveHealthRecord(
                    symptom = symptom,
                    treatment = treatment,
                    foodIntake = foodIntake,
                    weight = weight,
                    onSuccess = { showHealthDialog = false },
                    onError = { }
                )
            },
            initialSymptom = "",
            initialTreatment = "",
            initialFoodIntake = "",
            initialWeight = 0.0,
            headerTitle = "건강 기록"
        )
    }

    if (showScheduleDialog) {
        ScheduleDialog(
            onDismiss = { showScheduleDialog = false },
            onSave = { title, location, notes ->
                viewModel.saveSchedule(
                    title = title,
                    location = location,
                    notes = notes,
                    onSuccess = { showScheduleDialog = false },
                    onError = { }
                )
            },
            initialTitle = "",
            initialLocation = "",
            initialNotes = "",
            headerTitle = "새 일정"
        )
    }

    //  수정(prefill) 다이얼로그들
    editingDiary?.let { diary ->
        DiaryDialog(
            onDismiss = { editingDiary = null },
            onSave = { title, content ->
                viewModel.updateDiary(
                    diary = diary.copy(title = title, content = content),
                    onSuccess = { editingDiary = null },
                    onError = { }
                )
            },
            initialTitle = diary.title,
            initialContent = diary.content,
            headerTitle = "일기 수정"
        )
    }

    editingHealth?.let { record ->
        HealthRecordDialog(
            onDismiss = { editingHealth = null },
            onSave = { symptom, treatment, foodIntake, weight ->
                viewModel.updateHealthRecord(
                    record = record.copy(
                        symptom = symptom,
                        treatment = treatment,
                        foodIntake = foodIntake,
                        weight = weight
                    ),
                    onSuccess = { editingHealth = null },
                    onError = { }
                )
            },
            initialSymptom = record.symptom,
            initialTreatment = record.treatment,
            initialFoodIntake = record.foodIntake,
            initialWeight = record.weight,
            headerTitle = "건강 기록 수정"
        )
    }

    editingSchedule?.let { schedule ->
        ScheduleDialog(
            onDismiss = { editingSchedule = null },
            onSave = { title, location, notes ->
                viewModel.updateSchedule(
                    schedule = schedule.copy(
                        title = title,
                        location = location,
                        notes = notes
                    ),
                    onSuccess = { editingSchedule = null },
                    onError = { }
                )
            },
            initialTitle = schedule.title,
            initialLocation = schedule.location ?: "",
            initialNotes = schedule.notes ?: "",
            headerTitle = "일정 수정"
        )
    }

    // 상세 팝업들 (삭제/수정/닫기)
    showDiaryDetail?.let { diary ->
        var showDeleteConfirm by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showDiaryDetail = null },
            title = { Text(text = diary.title, style = MaterialTheme.typography.titleMedium) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = diary.date, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Divider()
                    Text(text = diary.content, style = MaterialTheme.typography.bodyMedium)
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { showDeleteConfirm = true }) {
                        Text("삭제", color = Color.Red)
                    }
                    TextButton(
                        onClick = {
                            editingDiary = diary
                            showDiaryDetail = null
                        }
                    ) { Text("수정", color = Color(0xFFF0724A)) }
                    TextButton(onClick = { showDiaryDetail = null }) {
                        Text("닫기", color = Color(0xFFF0724A))
                    }
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )

        if (showDeleteConfirm) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirm = false },
                title = { Text("삭제 확인") },
                text = { Text("정말 이 일기를 삭제하시겠습니까?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteDiary(
                                diary = diary,
                                onSuccess = {
                                    showDeleteConfirm = false
                                    showDiaryDetail = null
                                },
                                onError = { }
                            )
                        }
                    ) { Text("삭제", color = Color.Red) }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirm = false }) {
                        Text("취소", color = Color.Gray)
                    }
                },
                containerColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            )
        }
    }

    showHealthDetail?.let { record ->
        var showDeleteConfirm by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showHealthDetail = null },
            title = { Text(text = "건강 기록", style = MaterialTheme.typography.titleMedium) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = record.date, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Divider()
                    Text("이상증상: ${record.symptom}", style = MaterialTheme.typography.bodyMedium)
                    Text("대소변: ${record.treatment}", style = MaterialTheme.typography.bodyMedium)
                    Text("식사량: ${record.foodIntake}", style = MaterialTheme.typography.bodyMedium)
                    Text("몸무게: ${record.weight}kg", style = MaterialTheme.typography.bodyMedium)
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { showDeleteConfirm = true }) {
                        Text("삭제", color = Color.Red)
                    }
                    TextButton(
                        onClick = {
                            editingHealth = record
                            showHealthDetail = null
                        }
                    ) { Text("수정", color = Color(0xFFF0724A)) }
                    TextButton(onClick = { showHealthDetail = null }) {
                        Text("닫기", color = Color(0xFFF0724A))
                    }
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )

        if (showDeleteConfirm) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirm = false },
                title = { Text("삭제 확인") },
                text = { Text("정말 이 건강 기록을 삭제하시겠습니까?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteHealthRecord(
                                record = record,
                                onSuccess = {
                                    showDeleteConfirm = false
                                    showHealthDetail = null
                                },
                                onError = { }
                            )
                        }
                    ) { Text("삭제", color = Color.Red) }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirm = false }) {
                        Text("취소", color = Color.Gray)
                    }
                },
                containerColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            )
        }
    }

    showScheduleDetail?.let { schedule ->
        var showDeleteConfirm by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showScheduleDetail = null },
            title = { Text(text = schedule.title, style = MaterialTheme.typography.titleMedium) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = schedule.date, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Divider()
                    schedule.location?.let { Text("장소: $it", style = MaterialTheme.typography.bodyMedium) }
                    schedule.notes?.let { Text("노트: $it", style = MaterialTheme.typography.bodyMedium) }
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { showDeleteConfirm = true }) {
                        Text("삭제", color = Color.Red)
                    }
                    TextButton(
                        onClick = {
                            editingSchedule = schedule
                            showScheduleDetail = null
                        }
                    ) { Text("수정", color = Color(0xFFF0724A)) }
                    TextButton(onClick = { showScheduleDetail = null }) {
                        Text("닫기", color = Color(0xFFF0724A))
                    }
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )

        if (showDeleteConfirm) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirm = false },
                title = { Text("삭제 확인") },
                text = { Text("정말 이 일정을 삭제하시겠습니까?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteSchedule(
                                schedule = schedule,
                                onSuccess = {
                                    showDeleteConfirm = false
                                    showScheduleDetail = null
                                },
                                onError = { }
                            )
                        }
                    ) { Text("삭제", color = Color.Red) }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirm = false }) {
                        Text("취소", color = Color.Gray)
                    }
                },
                containerColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

// 캘린더 UI 컴포저블들
@Composable
fun CalendarView(
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    datesWithDiary: Set<LocalDate>,
    datesWithHealth: Set<LocalDate>,
    datesWithSchedule: Set<LocalDate>,
    onDateSelected: (LocalDate) -> Unit,
    onMonthChanged: (YearMonth) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onMonthChanged(currentMonth.minusMonths(1)) }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_chevron_left),
                    contentDescription = "이전 달"
                )
            }

            Text(
                text = "${currentMonth.year}.${String.format("%02d", currentMonth.monthValue)}",
                style = MaterialTheme.typography.titleMedium
            )

            IconButton(onClick = { onMonthChanged(currentMonth.plusMonths(1)) }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_chevron_right),
                    contentDescription = "다음 달"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("일", "월", "화", "수", "목", "금", "토").forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall,
                    color = when (day) {
                        "일" -> Color.Red
                        "토" -> Color.Blue
                        else -> Color.Black
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        val firstDayOfMonth = currentMonth.atDay(1)
        val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7
        val daysInMonth = currentMonth.lengthOfMonth()

        Column {
            var dayCounter = 1
            repeat(6) { week ->
                if (dayCounter <= daysInMonth) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        repeat(7) { dayOfWeek ->
                            val dayIndex = week * 7 + dayOfWeek
                            if (dayIndex >= firstDayOfWeek && dayCounter <= daysInMonth) {
                                val date = currentMonth.atDay(dayCounter)
                                DayCell(
                                    date = date,
                                    isSelected = date == selectedDate,
                                    hasDiary = datesWithDiary.contains(date),
                                    hasHealth = datesWithHealth.contains(date),
                                    hasSchedule = datesWithSchedule.contains(date),
                                    onClick = { onDateSelected(date) }
                                )
                                dayCounter++
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RowScope.DayCell(
    date: LocalDate,
    isSelected: Boolean,
    hasDiary: Boolean,
    hasHealth: Boolean,
    hasSchedule: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .weight(1f)
            .aspectRatio(1f)
            .padding(2.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodySmall,
                color = when {
                    isSelected -> Color(0xFFF0724A)
                    date.dayOfWeek == DayOfWeek.SUNDAY -> Color(0xFFFF0000)
                    date.dayOfWeek == DayOfWeek.SATURDAY -> Color(0xFF0000FF)
                    else -> Color.Black
                }
            )

            if (hasDiary || hasHealth || hasSchedule) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_paw),
                    contentDescription = "기록 있음",
                    modifier = Modifier.size(16.dp),
                    tint = Color(0xFF999999)
                )
            }
        }
    }
}

@Composable
fun AddMenuItem(
    icon: Int,
    text: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = text,
            tint = Color(0xFFF0724A),
            modifier = Modifier.size(24.dp)
        )
        Text(text = text, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun ScheduleItem(
    schedule: ImportantSchedule,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFFF9F5), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_schedule),
            contentDescription = null,
            tint = Color(0xFFF0724A),
            modifier = Modifier.size(24.dp)
        )
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = schedule.title, style = MaterialTheme.typography.bodyLarge)
            schedule.location?.let {
                Text(text = it, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
    }
}

@Composable
fun DiaryItem(
    diary: DiaryEntry,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFFF9F5), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_diary),
            contentDescription = null,
            tint = Color(0xFFF0724A),
            modifier = Modifier.size(24.dp)
        )
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = diary.title, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = diary.content,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                maxLines = 2
            )
        }
    }
}

@Composable
fun HealthRecordItem(
    record: HealthRecord,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFFF9F5), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_health_record),
            contentDescription = null,
            tint = Color(0xFFF0724A),
            modifier = Modifier.size(24.dp)
        )
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = record.symptom, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = "몸무게: ${record.weight}kg",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}