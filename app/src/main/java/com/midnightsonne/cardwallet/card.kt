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
    val flag: String,
    val cardColor: String,
    val password: String
) : Parcelable
