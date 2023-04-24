package com.midnightsonne.cardwallet

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Card(
    val name: String,
    val cardNumber: String,
    val expirationDate: String,
    val cvv: String,
    val cardHolderName: String,
    val flag: Int,
    val cardColor: Int,
    val password: String
) : Parcelable
