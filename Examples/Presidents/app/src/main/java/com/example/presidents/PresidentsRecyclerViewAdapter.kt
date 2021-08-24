package com.example.presidents

import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView

import com.example.presidents.objects.PresidentViewModel.President
import com.example.presidents.databinding.ListItemBinding

/**
 * [RecyclerView.Adapter] that can display a [President].
 * TODO: Replace the implementation with code for your data type.
 */
class PresidentsRecyclerViewAdapter(
    private val values: List<President>,
    val onClick: (President) -> Unit
) : RecyclerView.Adapter<PresidentsRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            ListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        holder.idView.text = item.name
        holder.itemView.setOnClickListener { onClick(item) }
    }


    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: ListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val idView: TextView = binding.presidentName

        override fun toString(): String {
            return super.toString() + " '" + idView + "'"
        }
    }


}

