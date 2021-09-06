package com.example.database

import android.content.Context
import androidx.room.*
import androidx.room.Dao




// TABLES

// Has a One to Many relation with Manifest

@Entity
data class Instructions(
    @PrimaryKey(autoGenerate = false)
    val iid : Long? = null,
    val recipe_name: String,
    val instructions: String,
) {
    override fun toString() = "($recipe_name) $instructions"
}

// Has a Many to One relation with Recipe
@Entity
data class Manifest(
    @PrimaryKey(autoGenerate = true)
    val mid : Int? = null,
    val recipe_name: String,
    val ingredient: String, // Ingredient name & quantity (1tbsp/1l/5g)
    val amount : String
)


data class Recipe(
    @Embedded
    val instructions: Instructions,

    @Relation(parentColumn = "recipe_name", entityColumn = "recipe_name")
    val ingredients: List<Manifest>
)

// ACCESS INTERFACES

@Dao
interface InstructionsDao {
    @Query("SELECT * FROM Instructions")
    fun getAll(): List<Instructions>

    @Query("SELECT * FROM Instructions WHERE Instructions.recipe_name = :recipe_name")
    fun getRecipe(recipe_name: String): Instructions

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(recipe: Instructions) : Long

    @Update
    fun update(recipe: Instructions)

    @Delete
    fun delete(recipe: Instructions)

    @Query("DELETE FROM Instructions WHERE Instructions.recipe_name = :recipe_name")
    fun deleteInstruction(recipe_name: String)
}

@Dao
interface ManifestDao {
    @Query("SELECT * FROM Manifest")
    fun getAll(): List<Manifest>

    @Query("SELECT * FROM Manifest WHERE Manifest.recipe_name = :recipe_name")
    fun getManifest(recipe_name: String): List<Manifest>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(manifest: Manifest)

    @Update
    fun update(manifest: Manifest)

    @Delete
    fun delete(manifest: Manifest)

    @Query("DELETE FROM Manifest WHERE Manifest.recipe_name = :recipe_name")
    fun deleteManifest(recipe_name: String)
}

@Dao
interface RecipeDao {
    @Query("SELECT * FROM Instructions")
    fun getAll(): List<Recipe>

    @Query("SELECT * FROM Instructions WHERE Instructions.recipe_name = :recipe_name")
    fun getRecipe(recipe_name: String): Recipe
}

// DATABASES

@Database(
    entities = [(Instructions::class), (Manifest::class)],
    version = 1
)
abstract class RecipeDB : RoomDatabase() {
    abstract fun instructionsDao(): InstructionsDao
    abstract fun manifestDao(): ManifestDao
    abstract fun RecipeDao(): RecipeDao

    companion object {
        private var sInstance: RecipeDB? = null

        @Synchronized
        fun get(context: Context): RecipeDB {
            if (sInstance == null) {
                sInstance =
                    Room.databaseBuilder(
                        context.applicationContext,
                        RecipeDB::class.java, "recipes.db"
                    ).build()
            }
            return sInstance!!
        }
    }
}
