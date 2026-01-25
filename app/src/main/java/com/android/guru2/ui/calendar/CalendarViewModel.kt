package com.android.guru2.ui.calendar

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.guru2.data.model.DiaryEntry
import com.android.guru2.data.model.HealthRecord
import com.android.guru2.data.model.ImportantSchedule
import com.android.guru2.data.repository.CalendarRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

class CalendarViewModel : ViewModel() {
    private val repository = CalendarRepository()

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _currentMonth = MutableStateFlow(YearMonth.now())
    val currentMonth: StateFlow<YearMonth> = _currentMonth.asStateFlow()

    private val _diaries = MutableStateFlow<List<DiaryEntry>>(emptyList())
    val diaries: StateFlow<List<DiaryEntry>> = _diaries.asStateFlow()

    private val _healthRecords = MutableStateFlow<List<HealthRecord>>(emptyList())
    val healthRecords: StateFlow<List<HealthRecord>> = _healthRecords.asStateFlow()

    private val _schedules = MutableStateFlow<List<ImportantSchedule>>(emptyList())
    val schedules: StateFlow<List<ImportantSchedule>> = _schedules.asStateFlow()

    private val _datesWithDiary = MutableStateFlow<Set<LocalDate>>(emptySet())
    val datesWithDiary: StateFlow<Set<LocalDate>> = _datesWithDiary.asStateFlow()

    private val _datesWithHealth = MutableStateFlow<Set<LocalDate>>(emptySet())
    val datesWithHealth: StateFlow<Set<LocalDate>> = _datesWithHealth.asStateFlow()

    private val _datesWithSchedule = MutableStateFlow<Set<LocalDate>>(emptySet())
    val datesWithSchedule: StateFlow<Set<LocalDate>> = _datesWithSchedule.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
        loadDataForDate(date)
    }

    fun changeMonth(yearMonth: YearMonth) {
        _currentMonth.value = yearMonth
        loadMonthData(yearMonth)
    }

    private fun loadDataForDate(date: LocalDate) {
        val userId = "test_user"
        val dateString = date.toString()

        Log.d("CalendarViewModel", "loadDataForDate - date: $dateString, userId: $userId")

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val diaries = repository.getDiariesByDate(userId, dateString)
                val healthRecords = repository.getHealthRecordsByDate(userId, dateString)
                val schedules = repository.getSchedulesByDate(userId, dateString)

                Log.d("CalendarViewModel", "로드 완료 - 일기:${diaries.size}, 건강:${healthRecords.size}, 일정:${schedules.size}")

                _diaries.value = diaries
                _healthRecords.value = healthRecords
                _schedules.value = schedules
            } catch (e: Exception) {
                Log.e("CalendarViewModel", "loadDataForDate 에러", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadMonthData(yearMonth: YearMonth) {
        val userId = "test_user"
        val yearMonthString = yearMonth.toString()

        Log.d("CalendarViewModel", "loadMonthData - yearMonth: $yearMonthString, userId: $userId")

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val diaries = repository.getDiariesByMonth(userId, yearMonthString)
                val healthRecords = repository.getHealthRecordsByMonth(userId, yearMonthString)
                val schedules = repository.getSchedulesByMonth(userId, yearMonthString)

                Log.d("CalendarViewModel", "이번 달 데이터 - 일기:${diaries.size}개, 건강:${healthRecords.size}개, 일정:${schedules.size}개")

                diaries.forEach { Log.d("CalendarViewModel", "  📝 일기 날짜: ${it.date}") }
                healthRecords.forEach { Log.d("CalendarViewModel", "  🏥 건강 날짜: ${it.date}") }
                schedules.forEach { Log.d("CalendarViewModel", "  📅 일정 날짜: ${it.date}") }

                _datesWithDiary.value = diaries.map { LocalDate.parse(it.date) }.toSet()
                _datesWithHealth.value = healthRecords.map { LocalDate.parse(it.date) }.toSet()
                _datesWithSchedule.value = schedules.map { LocalDate.parse(it.date) }.toSet()

                Log.d("CalendarViewModel", "발바닥 표시될 날짜들:")
                Log.d("CalendarViewModel", "  일기: ${_datesWithDiary.value}")
                Log.d("CalendarViewModel", "  건강: ${_datesWithHealth.value}")
                Log.d("CalendarViewModel", "  일정: ${_datesWithSchedule.value}")
            } catch (e: Exception) {
                Log.e("CalendarViewModel", "loadMonthData 에러", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun saveDiary(title: String, content: String, onSuccess: () -> Unit, onError: () -> Unit) {
        val userId = "test_user"
        val diary = DiaryEntry(
            userId = userId,
            date = _selectedDate.value.toString(),
            title = title,
            content = content
        )

        Log.d("CalendarViewModel", "일기 저장 - date: ${diary.date}, title: ${diary.title}")

        viewModelScope.launch {
            _isLoading.value = true
            val success = repository.insertDiary(diary)
            _isLoading.value = false

            Log.d("CalendarViewModel", if (success) "✅ 저장 성공!" else "❌ 저장 실패!")

            if (success) {
                loadDataForDate(_selectedDate.value)
                loadMonthData(_currentMonth.value)
                onSuccess()
            } else {
                onError()
            }
        }
    }

    fun saveHealthRecord(
        symptom: String,
        treatment: String,
        foodIntake: String,
        weight: Double,
        onSuccess: () -> Unit,
        onError: () -> Unit
    ) {
        val userId = "test_user"
        val record = HealthRecord(
            userId = userId,
            date = _selectedDate.value.toString(),
            symptom = symptom,
            treatment = treatment,
            foodIntake = foodIntake,
            weight = weight
        )

        Log.d("CalendarViewModel", "건강 기록 저장 - date: ${record.date}, symptom: ${record.symptom}")

        viewModelScope.launch {
            _isLoading.value = true
            val success = repository.insertHealthRecord(record)
            _isLoading.value = false

            Log.d("CalendarViewModel", if (success) "✅ 저장 성공!" else "❌ 저장 실패!")

            if (success) {
                loadDataForDate(_selectedDate.value)
                loadMonthData(_currentMonth.value)
                onSuccess()
            } else {
                onError()
            }
        }
    }

    fun saveSchedule(
        title: String,
        location: String,
        notes: String,
        onSuccess: () -> Unit,
        onError: () -> Unit
    ) {
        val userId = "test_user"
        val schedule = ImportantSchedule(
            userId = userId,
            date = _selectedDate.value.toString(),
            title = title,
            location = location.ifEmpty { null },
            notes = notes.ifEmpty { null }
        )

        Log.d("CalendarViewModel", "일정 저장 - date: ${schedule.date}, title: ${schedule.title}")

        viewModelScope.launch {
            _isLoading.value = true
            val success = repository.insertSchedule(schedule)
            _isLoading.value = false

            Log.d("CalendarViewModel", if (success) "저장 성공!" else "❌ 저장 실패!")

            if (success) {
                loadDataForDate(_selectedDate.value)
                loadMonthData(_currentMonth.value)
                onSuccess()
            } else {
                onError()
            }
        }
    }

    init {
        Log.d("CalendarViewModel", "ViewModel 초기화")
        loadDataForDate(_selectedDate.value)
        loadMonthData(_currentMonth.value)
    }
}