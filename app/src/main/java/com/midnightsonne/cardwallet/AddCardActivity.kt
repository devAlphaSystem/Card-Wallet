@file:Suppress("DEPRECATION")

package com.midnightsonne.cardwallet

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

@SuppressLint("SetTextI18n")
class AddCardActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_CARD = "com.midnightsonne.cardwallet.EXTRA_CARD"
        const val EXTRA_CARD_POSITION = "com.midnightsonne.cardwallet.EXTRA_CARD_POSITION"
        const val EXTRA_IS_EDIT = "com.midnightsonne.cardwallet.EXTRA_IS_EDIT"
    }

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_card)

        sharedPreferences = getSharedPreferences(getString(R.string.shared_preferences_file_key), Context.MODE_PRIVATE)

        val cardNameEditText: EditText = findViewById(R.id.card_name_edit_text)
        val cardNumberEditText: EditText = findViewById(R.id.card_number_edit_text)
        val expirationDateEditText: EditText = findViewById(R.id.expiration_date_edit_text)
        val cvvEditText: EditText = findViewById(R.id.cvv_edit_text)
        val cardHolderNameEditText: EditText = findViewById(R.id.card_holder_name_edit_text)
        val passwordEditText: EditText = findViewById(R.id.password_edit_text)

        val isEdit = intent.getBooleanExtra(EXTRA_IS_EDIT, false)

        var cardFlag = "Unknown"

        val cardColorRecyclerView = findViewById<RecyclerView>(R.id.card_color_recycler_view)
        val cardColors = listOf(
            R.drawable.card_black_20, R.drawable.card_silver_20, R.drawable.card_purple_20, R.drawable.card_red_20, R.drawable.card_blue_20, R.drawable.card_orange_20, R.drawable.card_green_20, R.drawable.card_yellow_20
        )
        var selectedCardColor: Int? = null
        val cardColorAdapter = CardColorAdapter(cardColors) { cardColor ->
            selectedCardColor = cardColor
        }

        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        cardColorRecyclerView.layoutManager = layoutManager

        cardColorRecyclerView.adapter = cardColorAdapter

        cardNumberEditText.addTextChangedListener(object : TextWatcher {
            private var isUpdating = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (isUpdating) {
                    isUpdating = false

                    return
                }

                if (before == 0 && count == 1 && (start + 1) % 5 == 0) {
                    isUpdating = true

                    cardNumberEditText.setText(s.toString().substring(0, start) + " " + s.toString().substring(start, start + count))
                    cardNumberEditText.setSelection(cardNumberEditText.text.length)
                }
            }

            override fun afterTextChanged(s: Editable?) {
                cardFlag = if (s != null && s.length == 19) {
                    getCardFlag(s.toString().replace(" ", ""))
                } else {
                    "Unknown"
                }
            }
        })

        cardNumberEditText.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(19))

        expirationDateEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (before == 0 && count == 1 && start == 1) {
                    expirationDateEditText.setText(s.toString() + "/")
                    expirationDateEditText.setSelection(expirationDateEditText.text.length)
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        expirationDateEditText.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(5))

        cvvEditText.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(3))

        passwordEditText.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(4))

        val addButton: Button = findViewById(R.id.add_card_button)

        val addCardText: TextView = findViewById(R.id.add_card_text_view)

        if (isEdit) {
            val cardToEdit: Card? = intent.getParcelableExtra(EXTRA_CARD)

            if (cardToEdit != null) {
                cardNameEditText.setText(cardToEdit.name)
                cardNumberEditText.setText(cardToEdit.cardNumber)
                expirationDateEditText.setText(cardToEdit.expirationDate)
                cvvEditText.setText(cardToEdit.cvv)
                cardHolderNameEditText.setText(cardToEdit.cardHolderName)
                passwordEditText.setText(cardToEdit.password)

                selectedCardColor = cardToEdit.cardColor
                selectedCardColor?.let { cardColorAdapter.updateSelectedCardColor(it) }
            }

            addButton.text = "Update Card"

            addCardText.text = "Update Card"
        }

        addButton.setOnClickListener {
            if (cardNameEditText.text.isNullOrEmpty() || cardNumberEditText.text.isNullOrEmpty() || expirationDateEditText.text.isNullOrEmpty() || cvvEditText.text.isNullOrEmpty() || cardHolderNameEditText.text.isNullOrEmpty()) {
                Toast.makeText(this, "Please fill in all the fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (cardNumberEditText.text.toString().replace(" ", "").length != 16) {
                Toast.makeText(this, "Please enter a valid card number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (expirationDateEditText.text.toString().split("/").size != 2 || expirationDateEditText.text.toString().split("/")[0].length != 2 || expirationDateEditText.text.toString().split("/")[1].length != 2) {
                Toast.makeText(this, "Please enter a valid expiration date", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (cvvEditText.text.toString().length != 3) {
                Toast.makeText(this, "Please enter a valid CVV", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val card = Card(
                cardNameEditText.text.toString(), cardNumberEditText.text.toString(), expirationDateEditText.text.toString(), cvvEditText.text.toString(), cardHolderNameEditText.text.toString(), cardFlag, (selectedCardColor ?: cardColors.first()), passwordEditText.text.toString()
            )

            val resultIntent = Intent()

            resultIntent.putExtra(EXTRA_CARD, card)

            if (isEdit) {
                val cardPosition: Int = intent.getIntExtra(EXTRA_CARD_POSITION, -1)

                if (cardPosition != -1) {
                    resultIntent.putExtra(EXTRA_CARD_POSITION, cardPosition)
                }
            }

            setResult(RESULT_OK, resultIntent)

            val actionMessage = if (isEdit) "Card updated successfully!" else "Card added successfully!"

            Toast.makeText(this, actionMessage, Toast.LENGTH_SHORT).show()

            finish()
        }
    }

    private fun getCardFlag(cardNumber: String): String {
        return when {
            cardNumber.startsWith("4") -> "Visa"
            cardNumber.matches(Regex("^5[1-5].*")) -> "MasterCard"
            cardNumber.matches(Regex("^3[47].*")) -> "American Express"
            cardNumber.matches(Regex("^3(?:0[0-5]|[68].).*")) -> "Diners Club"
            cardNumber.matches(Regex("^6(?:011|5..).*")) -> "Discover"
            cardNumber.matches(Regex("^35(?:2[89]|[3-8].).*")) -> "JCB"
            cardNumber.matches(Regex("^((?:506699|5067|509\\d|65003|65004|65040|65043|65048|6505|6506|6507|6509|6516|6550)\\d{10,17})")) -> "ELO"
            cardNumber.matches(Regex("^((606282|637095|637568)\\d{10})")) -> "Hipercard"
            else -> "Unknown"
        }
    }
}