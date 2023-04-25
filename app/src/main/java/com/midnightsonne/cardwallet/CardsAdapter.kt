package com.midnightsonne.cardwallet

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

@SuppressLint("DiscouragedApi")
class CardsAdapter(
    private val cards: List<Card>, private val onCardOptionsClick: (Card, Int) -> Unit
) : RecyclerView.Adapter<CardsAdapter.CardViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.card_item, parent, false)

        return CardViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val card = cards[position]
        holder.bind(card, position)

        val layoutParams = holder.itemView.layoutParams as RecyclerView.LayoutParams
        layoutParams.bottomMargin = 0
        holder.itemView.layoutParams = layoutParams
    }

    override fun getItemCount() = cards.size

    inner class CardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardFlagImageView: ImageView = itemView.findViewById(R.id.card_flag_image_view)
        private val cardNicknameTextView: TextView = itemView.findViewById(R.id.card_nickname_text_view)
        private val cardNumberTextView: TextView = itemView.findViewById(R.id.card_number_text_view)
        private val cardNameTextView: TextView = itemView.findViewById(R.id.card_name_text_view)
        private val cardValidTextView: TextView = itemView.findViewById(R.id.card_valid_text_view)
        private val cardCvvTextView: TextView = itemView.findViewById(R.id.card_cvv_text_view)
        private val cardView: CardView = itemView.findViewById(R.id.card_view)

        private var showSensitiveInfo: Boolean = false

        fun bind(card: Card, position: Int) {
            cardNicknameTextView.text = card.name
            cardNumberTextView.text = if (showSensitiveInfo) card.cardNumber else maskCardNumber(card.cardNumber)
            cardNameTextView.text = card.cardHolderName
            cardValidTextView.text = if (showSensitiveInfo) card.expirationDate else maskExpirationDate(card.expirationDate)
            cardCvvTextView.text = if (showSensitiveInfo) card.cvv else maskCvv(card.cvv)

            setCardFlagDrawableResource(getCardFlagDrawableResource(card.flag))

            cardView.setBackgroundResource(card.cardColor)

            cardView.setOnClickListener {
                showSensitiveInfo = !showSensitiveInfo
                cardNumberTextView.text = if (showSensitiveInfo) card.cardNumber else maskCardNumber(card.cardNumber)
                cardValidTextView.text = if (showSensitiveInfo) card.expirationDate else maskExpirationDate(card.expirationDate)
                cardCvvTextView.text = if (showSensitiveInfo) card.cvv else maskCvv(card.cvv)
            }

            val showHideOptionsButton: ImageView = itemView.findViewById(R.id.show_hide_options_button)

            showHideOptionsButton.setOnClickListener {
                onCardOptionsClick(card, position)
            }
        }

        private fun setCardFlagDrawableResource(flagResourceId: Int) {
            if (flagResourceId != 0) {
                cardFlagImageView.setImageResource(flagResourceId)
            }
        }
    }

    private fun getCardFlagDrawableResource(cardFlag: String): Int {
        return when (cardFlag) {
            "Visa" -> R.drawable.flag_visa
            "MasterCard" -> R.drawable.flag_mastercard
            "American Express" -> R.drawable.flag_american_express
            "Diners Club" -> R.drawable.flag_diners
            "Discover" -> R.drawable.flag_discover
            "JCB" -> R.drawable.flag_jcb
            "ELO" -> R.drawable.flag_elo
            "Hipercard" -> R.drawable.flag_hipercard
            else -> 0
        }
    }

    private fun maskCardNumber(cardNumber: String): String {
        return if (cardNumber.length >= 4) {
            "**** **** **** " + cardNumber.substring(cardNumber.length - 4)
        } else {
            "**** **** **** ****"
        }
    }

    private fun maskExpirationDate(expirationDate: String): String {
        val parts = expirationDate.split("/")

        return if (parts.size == 2) {
            "**/${parts[1]}"
        } else {
            "**/**"
        }
    }

    private fun maskCvv(cvv: String): String {
        return cvv.replace(Regex("[0-9]"), "*")
    }
}
