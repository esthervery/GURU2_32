package com.android.guru2.data.repository

import com.android.guru2.data.SupabaseClient
import com.android.guru2.data.model.DiaryEntry
import com.android.guru2.data.model.HealthRecord
import com.android.guru2.data.model.ImportantSchedule
import io.github.jan.supabase.postgrest.from

class CalendarRepository {
    private val client = SupabaseClient.client

    // 일기 관련 함수
    suspend fun insertDiary(diary: DiaryEntry): Boolean {
        return try {
            client.from("diary_entries").insert(diary)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun getDiariesByDate(userId: String, date: String): List<DiaryEntry> {
        return try {
            client.from("diary_entries")
                .select {
                    filter {
                        eq("user_id", userId)
                        eq("date", date)
                    }
                }.decodeList<DiaryEntry>()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getDiariesByMonth(userId: String, yearMonth: String): List<DiaryEntry> {
        return try {
            // 2026-01 → 모든 데이터 가져와서 필터링
            client.from("diary_entries")
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }.decodeList<DiaryEntry>()
                .filter { it.date.startsWith(yearMonth) }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // 건강 기록 관련 함수
    suspend fun insertHealthRecord(record: HealthRecord): Boolean {
        return try {
            client.from("health_records").insert(record)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun getHealthRecordsByDate(userId: String, date: String): List<HealthRecord> {
        return try {
            client.from("health_records")
                .select {
                    filter {
                        eq("user_id", userId)
                        eq("date", date)
                    }
                }.decodeList<HealthRecord>()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getHealthRecordsByMonth(userId: String, yearMonth: String): List<HealthRecord> {
        return try {
            client.from("health_records")
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }.decodeList<HealthRecord>()
                .filter { it.date.startsWith(yearMonth) }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // 일정 관련 함수
    suspend fun insertSchedule(schedule: ImportantSchedule): Boolean {
        return try {
            client.from("important_schedules").insert(schedule)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun getSchedulesByDate(userId: String, date: String): List<ImportantSchedule> {
        return try {
            client.from("important_schedules")
                .select {
                    filter {
                        eq("user_id", userId)
                        eq("date", date)
                    }
                }.decodeList<ImportantSchedule>()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getSchedulesByMonth(userId: String, yearMonth: String): List<ImportantSchedule> {
        return try {
            client.from("important_schedules")
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }.decodeList<ImportantSchedule>()
                .filter { it.date.startsWith(yearMonth) }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}