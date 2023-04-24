@file:Suppress("DEPRECATION")

package com.midnightsonne.cardwallet

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.app.AlertDialog
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.card.MaterialCardView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MainActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var cardsRecyclerView: RecyclerView
    private lateinit var cardsAdapter: CardsAdapter
    private lateinit var cards: MutableList<Card>
    private lateinit var emptyCardsMessage: MaterialCardView

    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    private lateinit var backgroundDimmer: View

    private val addCardLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            val card: Card? = result.data?.extras?.getParcelable(AddCardActivity.EXTRA_CARD)

            if (card != null) {
                cards.add(card)

                cardsAdapter.notifyItemInserted(cards.size - 1)

                updateEmptyCardsMessageVisibility()

                saveCards()
            }
        }
    }

    private val editCardLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            val editedCard: Card? = result.data?.extras?.getParcelable(AddCardActivity.EXTRA_CARD)

            val editedCardPosition = result.data?.extras?.getInt(AddCardActivity.EXTRA_CARD_POSITION)

            if (editedCard != null && editedCardPosition != null) {
                cards[editedCardPosition] = editedCard

                cardsAdapter.notifyItemChanged(editedCardPosition)

                saveCards()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_dimmer)

        sharedPreferences = getSharedPreferences(getString(R.string.shared_preferences_file_key), Context.MODE_PRIVATE)

        cards = loadCards()

        cardsRecyclerView = findViewById(R.id.cards_recycler_view)

        cardsAdapter = CardsAdapter(cards) { _, card, position -> showCardOptions(card, position) }

        cardsRecyclerView.layoutManager = LinearLayoutManager(this)

        cardsRecyclerView.adapter = cardsAdapter

        val addCardFab: ImageView = findViewById(R.id.add_card_fab)
        addCardFab.setOnClickListener {
            val intent = Intent(this, AddCardActivity::class.java)

            addCardLauncher.launch(intent)
        }

        emptyCardsMessage = findViewById(R.id.empty_cards_message)

        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout)
        swipeRefreshLayout.setOnRefreshListener {
            cards = loadCards()
            cardsAdapter = CardsAdapter(cards) { _, card, position -> showCardOptions(card, position) }
            cardsRecyclerView.adapter = cardsAdapter

            swipeRefreshLayout.isRefreshing = false
            Toast.makeText(this, "Cards refreshed", Toast.LENGTH_SHORT).show()
        }

        val walletInfoButton: ImageView = findViewById(R.id.wallet_info)
        walletInfoButton.setOnClickListener {
            showWalletInfoDialog()
        }

        backgroundDimmer = findViewById(R.id.background_dimmer)

        updateEmptyCardsMessageVisibility()
    }

    private fun showCardOptions(card: Card, position: Int) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.card_options_dialog)

        dialog.findViewById<TextView>(R.id.copy_card_number).setOnClickListener {
            val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Card Number", card.cardNumber)
            clipboardManager.setPrimaryClip(clip)
            Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.findViewById<TextView>(R.id.copy_card_holder).setOnClickListener {
            val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Card Holder Name", card.cardHolderName)
            clipboardManager.setPrimaryClip(clip)
            Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.findViewById<TextView>(R.id.copy_card_exp_date).setOnClickListener {
            val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Card Expiration Date", card.expirationDate)
            clipboardManager.setPrimaryClip(clip)
            Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.findViewById<TextView>(R.id.copy_card_cvv).setOnClickListener {
            val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Card CVV", card.cvv)
            clipboardManager.setPrimaryClip(clip)
            Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.findViewById<TextView>(R.id.copy_card_password).setOnClickListener {
            val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Card Password", card.password)
            clipboardManager.setPrimaryClip(clip)
            Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.findViewById<TextView>(R.id.edit_card).setOnClickListener {
            editCard(card, position)
            dialog.dismiss()
        }

        dialog.findViewById<TextView>(R.id.delete_card).setOnClickListener {
            deleteCard(card)
            dialog.dismiss()
        }

        val window = dialog.window
        val layoutParams = window?.attributes

        val width = (resources.displayMetrics.widthPixels * 0.90).toInt()
        dialog.window?.setLayout(width, RecyclerView.LayoutParams.WRAP_CONTENT)

        layoutParams?.gravity = Gravity.CENTER
        window?.attributes = layoutParams

        showDimmedBackground()
        dialog.setOnDismissListener {
            hideDimmedBackground()
        }

        dialog.show()
    }

    private fun showWalletInfoDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Security Information")

        val messageView = LayoutInflater.from(this).inflate(R.layout.dialog_security_info, null)
        builder.setView(messageView)

        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }

        val alertDialog = builder.create()
        alertDialog.show()
    }

    private fun showDimmedBackground() {
        val fadeInAnimator = ObjectAnimator.ofFloat(backgroundDimmer, View.ALPHA, 0f, 1f)
        fadeInAnimator.duration = 250
        fadeInAnimator.start()

        backgroundDimmer.visibility = View.VISIBLE
    }

    private fun hideDimmedBackground() {
        val fadeOutAnimator = ObjectAnimator.ofFloat(backgroundDimmer, View.ALPHA, 1f, 0f)
        fadeOutAnimator.duration = 250
        fadeOutAnimator.start()
        fadeOutAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                backgroundDimmer.visibility = View.INVISIBLE
            }
        })
    }

    private fun updateEmptyCardsMessageVisibility() {
        if (cards.isEmpty()) {
            emptyCardsMessage.visibility = View.VISIBLE
        } else {
            emptyCardsMessage.visibility = View.GONE
        }
    }

    private fun deleteCard(card: Card) {
        val cardPosition = cards.indexOf(card)

        if (cardPosition != -1) {
            cards.removeAt(cardPosition)

            cardsAdapter.notifyItemRemoved(cardPosition)

            saveCards()

            Toast.makeText(this, "Card deleted", Toast.LENGTH_SHORT).show()
        }

        updateEmptyCardsMessageVisibility()
    }

    private fun editCard(card: Card, position: Int) {
        val intent = Intent(this, AddCardActivity::class.java)
        intent.putExtra(AddCardActivity.EXTRA_CARD, card)
        intent.putExtra(AddCardActivity.EXTRA_CARD_POSITION, position)
        intent.putExtra(AddCardActivity.EXTRA_IS_EDIT, true)

        editCardLauncher.launch(intent)
    }

    private fun saveCards() {
        val editor = sharedPreferences.edit()

        val gson = Gson()

        val json = gson.toJson(cards)

        editor.putString(getString(R.string.cards_list_key), json)
        editor.apply()
    }

    private fun loadCards(): MutableList<Card> {
        val gson = Gson()

        val json = sharedPreferences.getString(getString(R.string.cards_list_key), "")

        val type = object : TypeToken<List<Card>>() {}.type

        return gson.fromJson(json, type) ?: mutableListOf()
    }
}
