package com.android.guru2.data

import kotlinx.serialization.Serializable

@Serializable
data class CharacterDbModel(
    val id: String,
    val character_url: String?
)