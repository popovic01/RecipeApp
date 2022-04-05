package com.example.recipeapp.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Nutrient(
    val amount: Double?,
    val name: String?,
    val unit: String?
) : Parcelable