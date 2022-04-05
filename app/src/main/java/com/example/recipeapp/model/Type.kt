package com.example.recipeapp.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "type_table")
data class Type(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val name: String,
    val image: String
): Parcelable