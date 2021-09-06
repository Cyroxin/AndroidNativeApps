package com.example.database.ui.main

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.children
import com.example.database.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DetailFragment(private val recipe: Recipe) : Fragment() {

    companion object {
        fun newInstance(recipe: Recipe) = DetailFragment(recipe)
    }

    lateinit var layout: ConstraintLayout
    lateinit var ingredientslist: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        showBackButton()
        layout = inflater.inflate(R.layout.main_detail_fragment, container, false) as ConstraintLayout
        ingredientslist = layout.findViewById<LinearLayout>(R.id.linearLayoutIngredientsList)!!

        refreshManifest()

        // Title
        layout.findViewById<EditText>(R.id.editTextRecipe).setText(recipe.instructions.recipe_name)

        // Description
        layout.findViewById<EditText>(R.id.editTextMultilineInstructions)
            .setText(recipe.instructions.instructions)

        // FAB
        layout.findViewById<FloatingActionButton>(R.id.fabAddIngredient).setOnClickListener {
            addIngredient()
        }

        // Delete
        layout.findViewById<ImageView>(R.id.btnDelete).setOnClickListener {
            remove()
        }

        return layout
    }

    private fun showBackButton(show: Boolean = true) {
        if (activity is AppCompatActivity) {
            setHasOptionsMenu(show)
            (activity as AppCompatActivity?)?.supportActionBar?.setDisplayHomeAsUpEnabled(show)
        }
    }

    private fun refreshManifest() {

        recipe.ingredients.forEach {
            var view = layoutInflater.inflate(R.layout.ingredient_view, null)
            val ingredient = view.findViewById<EditText>(R.id.editTextIngredient)
            ingredient.setText(it.ingredient)
            val quantity = view.findViewById<TextView>(R.id.editTextQuantity)
            quantity.setText(it.amount)
            val remove = view.findViewById<ImageView>(R.id.btnRemove)
            remove.setOnClickListener {
                ingredientslist.removeView(view)
            }
            ingredientslist.addView(view)
        }
    }

    private fun remove()
    {
        GlobalScope.launch {
            val db = RecipeDB.get(requireContext().applicationContext)
            db.manifestDao().deleteManifest(recipe.instructions.recipe_name)
            db.instructionsDao().deleteInstruction(recipe_name = recipe.instructions.recipe_name)

            withContext(Dispatchers.Main) {
                showBackButton(false)
                requireActivity().onBackPressed()
            }
        }
    }

    private fun addIngredient() {
        var view = layoutInflater.inflate(R.layout.ingredient_view, null)
        val remove = view.findViewById<ImageView>(R.id.btnRemove)
        remove.setOnClickListener {
            ingredientslist.removeView(view)
        }
        ingredientslist.addView(view)
    }

    private fun commitChangesAndPop() {
        GlobalScope.launch {
            val db = RecipeDB.get(requireContext().applicationContext)
            db.manifestDao().deleteManifest(recipe.instructions.recipe_name)

            // Update instructions
            db.instructionsDao().update(
                Instructions(
                    recipe.instructions.iid,
                    layout.findViewById<EditText>(R.id.editTextRecipe).text.toString(),
                    layout.findViewById<EditText>(R.id.editTextMultilineInstructions).text.toString()
                )
            )

            // Replace manifest

            ingredientslist.children.forEach {
                db.manifestDao().insert(
                    Manifest(
                        recipe_name = layout.findViewById<EditText>(R.id.editTextRecipe).text.toString(),
                        ingredient = it.findViewById<EditText>(R.id.editTextIngredient).text.toString(),
                        amount = it.findViewById<TextView>(R.id.editTextQuantity).text.toString()
                    )
                )
            }

            withContext(Dispatchers.Main) {
                showBackButton(false)
                requireActivity().onBackPressed()
            }
        }
    }

    // Actionbar back press
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        commitChangesAndPop()
        return true
    }

    // Device back press
    override fun onResume() {
        super.onResume()
        requireView().isFocusableInTouchMode = true
        requireView().requestFocus()
        requireView().setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                commitChangesAndPop()
                true
            } else false
        }
    }

}