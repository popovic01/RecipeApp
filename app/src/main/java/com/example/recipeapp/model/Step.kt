package com.example.recipeapp.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Step(
    val number: Int?,
    val step: String?
) : Parcelable