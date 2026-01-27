package com.android.guru2

import kotlinx.serialization.Serializable

@Serializable
data class PetInfo(
    val id: String,
    val pet_name: String,
    val pet_age: Int,
    val is_male: Int,
    val pet_image: String
)