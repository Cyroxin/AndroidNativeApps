package com.example.database

import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RecipeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

class RecipeAdapter(var onClick: (String) -> (Unit) = { }, var items : MutableList<Instructions> = mutableListOf()) : RecyclerView.Adapter<RecipeViewHolder>()
{

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecipeViewHolder = RecipeViewHolder(TextView(parent.context))

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(
        holder: RecipeViewHolder,
        position: Int
    ) {
        Log.e("DBG","Called onBindViewHolder")
        (holder.itemView as TextView).text = items[position].recipe_name
        holder.itemView.setOnClickListener {
            onClick(items[position].recipe_name)
        }
    }
}