package com.midnightsonne.cardwallet

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class CardsAdapter(private val cards: List<Card>, private val onCardClick: (View, Card) -> Unit) : RecyclerView.Adapter<CardsAdapter.CardViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.card_item, parent, false)
        return CardViewHolder(itemView, onCardClick)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val currentItem = cards[position]
        holder.bind(currentItem)
    }

    override fun getItemCount() = cards.size

    inner class CardViewHolder(itemView: View, private val onCardClick: (View, Card) -> Unit) : RecyclerView.ViewHolder(itemView) {
        private val cardNicknameTextView: TextView = itemView.findViewById(R.id.card_nickname_text_view)
        private val cardNumberTextView: TextView = itemView.findViewById(R.id.card_number_text_view)
        private val cardNameTextView: TextView = itemView.findViewById(R.id.card_name_text_view)
        private val cardValidTextView: TextView = itemView.findViewById(R.id.card_valid_text_view)
        private val cardCvvTextView: TextView = itemView.findViewById(R.id.card_cvv_text_view)
        private val cardView: CardView = itemView.findViewById(R.id.card_view)

        fun bind(card: Card) {
            cardNicknameTextView.text = card.name
            cardNumberTextView.text = formatCardNumber(card.cardNumber)
            cardNameTextView.text = card.cardHolderName
            cardValidTextView.text = card.expirationDate
            cardCvvTextView.text = card.cvv
            setCardFlagDrawableResource(card.flag)

            // Get the HEX code for the card's color name
            val colorName = card.cardColor
            val colorArray = itemView.context.resources.getStringArray(R.array.card_colors)
            val colorHexArray = itemView.context.resources.getStringArray(R.array.card_colors_hex)
            val colorIndex = colorArray.indexOf(colorName)
            val colorHex = if (colorIndex >= 0) colorHexArray[colorIndex] else ""

            // Set the background color of the CardView to the HEX code value
            cardView.setCardBackgroundColor(Color.parseColor(colorHex))

            itemView.setOnClickListener { onCardClick(it, card) }
        }

        private fun formatCardNumber(cardNumber: String): String {
            return if (cardNumber.length >= 4) {
                "**** **** **** " + cardNumber.substring(cardNumber.length - 4)
            } else {
                cardNumber
            }
        }

        @SuppressLint("DiscouragedApi")
        private fun setCardFlagDrawableResource(flag: String) {
            val formattedFlag = flag.lowercase().replace(" ", "_")
            val flagResourceId = itemView.context.resources.getIdentifier(
                "ic_${formattedFlag}_logo",
                "drawable",
                itemView.context.packageName
            )
            if (flagResourceId != 0) {
                itemView.findViewById<ImageView>(R.id.card_flag_image_view).setImageResource(flagResourceId)
            }
        }
    }
}
