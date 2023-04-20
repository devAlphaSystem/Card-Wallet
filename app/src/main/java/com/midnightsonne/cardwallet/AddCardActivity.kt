package com.midnightsonne.cardwallet

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class AddCardActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_CARD = "com.midnightsonne.cardwallet.EXTRA_CARD"
    }

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_card)

        sharedPreferences = getSharedPreferences(getString(R.string.shared_preferences_file_key), Context.MODE_PRIVATE)

        // Initialize your input fields and button here
        val cardNameEditText: EditText = findViewById(R.id.card_name_edit_text)
        val cardNumberEditText: EditText = findViewById(R.id.card_number_edit_text)
        val expirationDateEditText: EditText = findViewById(R.id.expiration_date_edit_text)
        val cvvEditText: EditText = findViewById(R.id.cvv_edit_text)
        val cardHolderNameEditText: EditText = findViewById(R.id.card_holder_name_edit_text)
        val flagSpinner: Spinner = findViewById(R.id.flag_spinner)
        val cardColorSpinner: Spinner = findViewById(R.id.card_color_spinner)

        // Set up the flag and card color spinner with an ArrayAdapter
        val flagsAdapter = ArrayAdapter.createFromResource(
            this, R.array.flags, android.R.layout.simple_spinner_item
        )
        flagsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        flagSpinner.adapter = flagsAdapter

        val cardColorsAdapter = ArrayAdapter.createFromResource(
            this, R.array.card_colors, android.R.layout.simple_spinner_item
        )
        cardColorsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        cardColorSpinner.adapter = cardColorsAdapter

        cardNumberEditText.addTextChangedListener(object : TextWatcher {
            private var isUpdating = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            @SuppressLint("SetTextI18n")
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

            override fun afterTextChanged(s: Editable?) {}
        })

        // Set maxLength for cardNumberEditText to 19 (16 digits + 3 spaces)
        cardNumberEditText.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(19))

        // Add formatting for expiration date
        expirationDateEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            @SuppressLint("SetTextI18n")
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (before == 0 && count == 1 && start == 1) {
                    expirationDateEditText.setText(s.toString() + "/")
                    expirationDateEditText.setSelection(expirationDateEditText.text.length)
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Set maxLength for expiration date
        expirationDateEditText.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(5))

        // Set maxLength for CVV
        cvvEditText.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(3))

        val addButton: Button = findViewById(R.id.add_card_button)
        addButton.setOnClickListener {
            // Check if the input data is valid
            if (cardNameEditText.text.isNullOrEmpty() || cardNumberEditText.text.isNullOrEmpty() || expirationDateEditText.text.isNullOrEmpty() || cvvEditText.text.isNullOrEmpty() || cardHolderNameEditText.text.isNullOrEmpty()) {
                Toast.makeText(this, "Please fill in all the fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Check if the card number has 16 digits
            if (cardNumberEditText.text.toString().replace(" ", "").length != 16) {
                Toast.makeText(this, "Please enter a valid card number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Check if the expiration date has 2 parts with 2 digits each separated by a slash
            if (expirationDateEditText.text.toString().split("/").size != 2 || expirationDateEditText.text.toString().split("/")[0].length != 2 || expirationDateEditText.text.toString().split("/")[1].length != 2) {
                Toast.makeText(this, "Please enter a valid expiration date", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Check if the CVV has 3 digits
            if (cvvEditText.text.toString().length != 3) {
                Toast.makeText(this, "Please enter a valid CVV", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Get the input data and create a new Card instance
            val card = Card(
                cardNameEditText.text.toString(), cardNumberEditText.text.toString(), expirationDateEditText.text.toString(), cvvEditText.text.toString(), cardHolderNameEditText.text.toString(), flagSpinner.selectedItem.toString(), cardColorSpinner.selectedItem.toString()
            )        // Save the card locally, e.g., using SharedPreferences, Room, or another local storage solution
            saveCard(card)

            // Show a success message and return to the main activity
            Toast.makeText(this, "Card added successfully!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun saveCard(card: Card) {
        val gson = Gson()
        val json = sharedPreferences.getString(getString(R.string.cards_list_key), "")
        val type = object : TypeToken<MutableList<Card>>() {}.type
        val cards: MutableList<Card> = gson.fromJson(json, type) ?: mutableListOf()
        cards.add(card)
        val editor = sharedPreferences.edit()
        val updatedJson = gson.toJson(cards)
        editor.putString(getString(R.string.cards_list_key), updatedJson)
        editor.apply()
    }
}