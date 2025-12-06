package com.example.acronodroid.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.acronodroid.R
import com.example.acronodroid.models.Acronym

class AcronymAdapter(
    private var list: List<Acronym>,
    private val onClick: (Acronym) -> Unit
) : RecyclerView.Adapter<AcronymAdapter.VH>() {

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val shortTv: TextView = itemView.findViewById(R.id.item_short)
        val fullTv: TextView = itemView.findViewById(R.id.item_full)
        val catTv: TextView = itemView.findViewById(R.id.item_category)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_acronym, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = list[position]
        holder.shortTv.text = item.short
        holder.fullTv.text = item.full
        holder.catTv.text = item.category
        holder.itemView.setOnClickListener { onClick(item) }
    }

    override fun getItemCount(): Int = list.size

    fun update(newList: List<Acronym>) {
        list = newList
        notifyDataSetChanged()
    }
}
