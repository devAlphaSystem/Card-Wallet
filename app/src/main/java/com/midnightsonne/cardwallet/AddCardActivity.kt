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
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

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
        val flagSpinner: Spinner = findViewById(R.id.flag_spinner)
        val cardColorSpinner: Spinner = findViewById(R.id.card_color_spinner)

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

        val isEdit = intent.getBooleanExtra(EXTRA_IS_EDIT, false)

        if (isEdit) {
            val cardToEdit: Card? = intent.getParcelableExtra(EXTRA_CARD)

            if (cardToEdit != null) {
                cardNameEditText.setText(cardToEdit.name)
                cardNumberEditText.setText(cardToEdit.cardNumber)
                expirationDateEditText.setText(cardToEdit.expirationDate)
                cvvEditText.setText(cardToEdit.cvv)
                cardHolderNameEditText.setText(cardToEdit.cardHolderName)
                passwordEditText.setText(cardToEdit.password)

                flagSpinner.setSelection(flagsAdapter.getPosition(cardToEdit.flag))
                cardColorSpinner.setSelection(cardColorsAdapter.getPosition(cardToEdit.cardColor))
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

            if (passwordEditText.text.toString().length != 4) {
                Toast.makeText(this, "Please enter a valid password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val card = Card(
                cardNameEditText.text.toString(), cardNumberEditText.text.toString(), expirationDateEditText.text.toString(), cvvEditText.text.toString(), cardHolderNameEditText.text.toString(), flagSpinner.selectedItem.toString(), cardColorSpinner.selectedItem.toString(), passwordEditText.text.toString()
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
}
