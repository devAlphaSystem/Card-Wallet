@file:Suppress("DEPRECATION")

package com.midnightsonne.cardwallet

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MainActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var cardsRecyclerView: RecyclerView
    private lateinit var cardsAdapter: CardsAdapter
    private lateinit var cards: MutableList<Card>
    private lateinit var emptyCardsMessage: TextView

    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPreferences = getSharedPreferences(getString(R.string.shared_preferences_file_key), Context.MODE_PRIVATE)
        cards = loadCards()

        cardsRecyclerView = findViewById(R.id.cards_recycler_view)
        cardsAdapter = CardsAdapter(cards) { view, card ->
            showCardOptions(view, card)
        }
        cardsRecyclerView.layoutManager = LinearLayoutManager(this)
        cardsRecyclerView.adapter = cardsAdapter

        val addCardFab: FloatingActionButton = findViewById(R.id.add_card_fab)
        addCardFab.setOnClickListener {
            val intent = Intent(this, AddCardActivity::class.java)
            addCardLauncher.launch(intent)
        }

        emptyCardsMessage = findViewById(R.id.empty_cards_message)

        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout)
        swipeRefreshLayout.setOnRefreshListener {
            cards = loadCards()
            cardsAdapter = CardsAdapter(cards) { view, card ->
                showCardOptions(view, card)
            }
            cardsRecyclerView.adapter = cardsAdapter

            swipeRefreshLayout.isRefreshing = false
            Toast.makeText(this, "Cards refreshed", Toast.LENGTH_SHORT).show()
        }

        updateEmptyCardsMessageVisibility()
    }

    private fun showCardOptions(view: View, card: Card) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.inflate(R.menu.card_options_menu)
        popupMenu.setOnMenuItemClickListener { menuItem ->
            val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip: ClipData
            when (menuItem.itemId) {
                R.id.copy_card_number -> {
                    clip = ClipData.newPlainText("Card Number", card.cardNumber)
                }

                R.id.copy_card_holder -> {
                    clip = ClipData.newPlainText("Card User Name", card.cardHolderName)
                }

                R.id.copy_card_exp_date -> {
                    clip = ClipData.newPlainText("Card Exp Date", card.expirationDate)
                }

                R.id.copy_card_cvv -> {
                    clip = ClipData.newPlainText("Card CVV", card.cvv)
                }

                R.id.copy_card_password -> {
                    clip = ClipData.newPlainText("Card Password", card.password)
                }

                R.id.delete_card -> {
                    deleteCard(card)
                    return@setOnMenuItemClickListener true
                }

                else -> return@setOnMenuItemClickListener false
            }
            clipboardManager.setPrimaryClip(clip)
            Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show()
            true
        }
        popupMenu.show()
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
