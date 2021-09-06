package com.example.database.ui.main

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.database.Instructions
import com.example.database.R
import com.example.database.RecipeAdapter
import com.example.database.RecipeDB
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
    }

    lateinit var recyclerView: RecyclerView
    lateinit var adapter: RecipeAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.main_fragment, container, false)

        recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        adapter = RecipeAdapter()
        adapter.onClick = { showDetail(it) }

        recyclerView.adapter = adapter
        refreshAsync()

        // Add new empty recipe
        view.findViewById<FloatingActionButton>(R.id.floatingActionButton).setOnClickListener {
            add()
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView.layoutManager = LinearLayoutManager(requireContext().applicationContext)

    }

    private fun showDetail(recipe : String)
    {
        Log.e("DBG","Showing ${recipe}")
        GlobalScope.launch {
            val db = RecipeDB.get(requireContext().applicationContext)
            val recipe = db.RecipeDao().getRecipe(recipe)


            withContext(Main) {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.container, DetailFragment.newInstance(recipe = recipe))
                    .addToBackStack("my_fragment")
                    .commit();
            }
        }
    }

    private fun refreshAsync(instructions: List<Instructions>? = null)
    {
        if(instructions == null) {
            GlobalScope.launch {
                val instructions = RecipeDB.get(requireContext().applicationContext).instructionsDao().getAll()

                withContext(Main) {
                    adapter.items = instructions.toMutableList()
                    adapter.notifyDataSetChanged()
                }
            }
        }
        else
        {
            adapter.items = instructions.toMutableList()
            adapter.notifyDataSetChanged()
        }
    }

    private fun add() {
        GlobalScope.launch {
            val db = RecipeDB.get(requireContext().applicationContext)
            if(db.instructionsDao().getRecipe("new recipe") == null) {
                db.instructionsDao().insert(
                    Instructions(
                        recipe_name = "new recipe",
                        instructions = ""
                    )
                )


            refreshAsync()
            }
        }
    }
}