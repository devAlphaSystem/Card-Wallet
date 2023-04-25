package com.midnightsonne.cardwallet

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

class CardColorAdapter(
    private val cardColors: List<Int>, private val onCardColorClick: (Int) -> Unit
) : RecyclerView.Adapter<CardColorAdapter.CardColorViewHolder>() {

    private var selectedCardColor: Int? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardColorViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.card_color, parent, false)
        return CardColorViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CardColorViewHolder, position: Int) {
        val cardColor = cardColors[position]
        holder.bind(cardColor)
    }

    override fun getItemCount() = cardColors.size

    inner class CardColorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardColorImageView: ImageView = itemView.findViewById(R.id.card_color_image_view)

        @SuppressLint("NotifyDataSetChanged")
        fun bind(cardColor: Int) {
            cardColorImageView.setImageResource(cardColor)

            if (cardColor == selectedCardColor) {
                cardColorImageView.alpha = 1f
            } else {
                cardColorImageView.alpha = 0.5f
            }

            itemView.setOnClickListener {
                selectedCardColor = cardColor
                notifyDataSetChanged()
                onCardColorClick(cardColor)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateSelectedCardColor(cardColor: Int) {
        val index = cardColors.indexOf(cardColor)
        if (index != -1) {
            selectedCardColor = cardColor
            notifyDataSetChanged()
        }
    }
}
