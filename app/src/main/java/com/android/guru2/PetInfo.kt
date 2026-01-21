package com.android.guru2

import kotlinx.serialization.Serializable

@Serializable
data class PetInfo(
    val id: String,        // auth.users.id에서 가져올 UUID
    val pet_name: String,
    val pet_age: Int,
    val is_male: Int,
    val pet_image: String
)