package com.midnightsonne.cardwallet

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

@SuppressLint("DiscouragedApi")
class CardsAdapter(private val cards: List<Card>, private val onCardClick: (View, Card, Int) -> Unit) : RecyclerView.Adapter<CardsAdapter.CardViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.card_item, parent, false)

        return CardViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val card = cards[position]
        holder.bind(card, position)

        if (position == cards.size - 1) {
            val layoutParams = holder.itemView.layoutParams as RecyclerView.LayoutParams
            layoutParams.bottomMargin = 72.dpToPx(holder.itemView.context)
            holder.itemView.layoutParams = layoutParams
        } else {
            val layoutParams = holder.itemView.layoutParams as RecyclerView.LayoutParams
            layoutParams.bottomMargin = 0
            holder.itemView.layoutParams = layoutParams
        }
    }

    override fun getItemCount() = cards.size

    inner class CardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val showHideInfoButton: ImageView = itemView.findViewById(R.id.show_hide_info_button)
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

            setCardFlagDrawableResource(card.flag)

            val colorName = card.cardColor
            val colorArray = itemView.context.resources.getStringArray(R.array.card_colors)
            val colorHexArray = itemView.context.resources.getStringArray(R.array.card_colors_hex)
            val colorIndex = colorArray.indexOf(colorName)
            val colorHex = if (colorIndex >= 0) colorHexArray[colorIndex] else ""

            cardView.setCardBackgroundColor(Color.parseColor(colorHex))

            itemView.setOnClickListener { onCardClick(it, card, position) }

            showHideInfoButton.setOnClickListener {
                showSensitiveInfo = !showSensitiveInfo

                showHideInfoButton.setImageResource(if (showSensitiveInfo) R.drawable.in_icon_visibility_off else R.drawable.in_icon_visibility)

                bind(card, position)
            }
        }

        private fun setCardFlagDrawableResource(flag: String) {
            val formattedFlag = flag.lowercase().replace(" ", "_")

            val flagResourceId = itemView.context.resources.getIdentifier(
                "ic_${formattedFlag}_logo", "drawable", itemView.context.packageName
            )

            if (flagResourceId != 0) {
                itemView.findViewById<ImageView>(R.id.card_flag_image_view).setImageResource(flagResourceId)
            }
        }
    }

    private fun Int.dpToPx(context: Context): Int {
        val density = context.resources.displayMetrics.density

        return (this * density).toInt()
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
