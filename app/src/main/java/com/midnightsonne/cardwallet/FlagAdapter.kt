package com.midnightsonne.cardwallet

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

class FlagAdapter(
    private val flags: List<Int>,
    private val onFlagClick: (Int) -> Unit
) : RecyclerView.Adapter<FlagAdapter.FlagViewHolder>() {

    private var selectedFlag: Int? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FlagViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.flag_item, parent, false)
        return FlagViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: FlagViewHolder, position: Int) {
        val flag = flags[position]
        holder.bind(flag)

        holder.itemView.isSelected = flag == selectedFlag
    }

    override fun getItemCount() = flags.size

    inner class FlagViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val flagImageView: ImageView = itemView.findViewById(R.id.flag_image_view)

        @SuppressLint("NotifyDataSetChanged")
        fun bind(flag: Int) {
            flagImageView.setImageResource(flag)

            if (flag == selectedFlag) {
                flagImageView.alpha = 1f
            } else {
                flagImageView.alpha = 0.5f
            }

            itemView.setOnClickListener {
                selectedFlag = flag
                notifyDataSetChanged()
                onFlagClick(flag)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setSelectedFlag(flag: Int) {
        val index = flags.indexOf(flag)
        if (index != -1) {
            selectedFlag = flag
            notifyDataSetChanged()
        }
    }
}
