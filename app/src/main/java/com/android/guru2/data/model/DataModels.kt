package com.android.guru2.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DiaryEntry(
    val id: Int? = null,
    @SerialName("user_id")
    val userId: String,
    val date: String,
    val title: String,
    val content: String,
    @SerialName("created_at")
    val createdAt: String? = null
)

@Serializable
data class HealthRecord(
    val id: Int? = null,
    @SerialName("user_id")
    val userId: String,
    val date: String,
    val symptom: String,
    val treatment: String,
    @SerialName("food_intake")
    val foodIntake: String,
    val weight: Double,
    @SerialName("created_at")
    val createdAt: String? = null
)

@Serializable
data class ImportantSchedule(
    val id: Int? = null,
    @SerialName("user_id")
    val userId: String,
    val date: String,
    val title: String,
    val location: String? = null,
    val notes: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null
)