package it.studio44z.zing.data

import androidx.room.Entity
import androidx.room.ColumnInfo
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "food_lots", indices = [Index("name"), Index("barcode"), Index("expiryDateMillis")])
data class FoodLotEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val brand: String = "",
    val barcode: String = "",
    val category: String = "Altro",
    val quantity: Double,
    val unit: String = "pz",
    val location: String = "Dispensa",
    val purchaseDateMillis: Long? = null,
    val expiryDateMillis: Long? = null,
    val openedDateMillis: Long? = null,
    @ColumnInfo(defaultValue = "0.0") val minQuantity: Double = 0.0,
    val notes: String = "",
    val createdAtMillis: Long = System.currentTimeMillis(),
)

@Entity(tableName = "recipes", indices = [Index("name"), Index("isSeed"), Index("favorite")])
data class RecipeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String = "",
    val category: String = "Altro",
    val prepMinutes: Int = 10,
    val cookMinutes: Int = 0,
    val servings: Int = 2,
    val ingredientsJson: String,
    val instructions: String,
    @ColumnInfo(defaultValue = "''") val notes: String = "",
    @ColumnInfo(defaultValue = "0") val rating: Int = 0,
    @ColumnInfo(defaultValue = "0") val favorite: Boolean = false,
    val imageUri: String? = null,
    @ColumnInfo(defaultValue = "0") val isSeed: Boolean = false,
    @ColumnInfo(defaultValue = "0") val createdAtMillis: Long = System.currentTimeMillis(),
    @ColumnInfo(defaultValue = "0") val updatedAtMillis: Long = System.currentTimeMillis(),
)

@Entity(tableName = "shopping_items", indices = [Index("name"), Index("isChecked")])
data class ShoppingItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val quantity: Double = 1.0,
    val unit: String = "pz",
    val category: String = "Altro",
    val notes: String = "",
    val isChecked: Boolean = false,
    val isManual: Boolean = true,
    val sourceRecipeId: Long? = null,
    @ColumnInfo(defaultValue = "0") val ignored: Boolean = false,
    @ColumnInfo(defaultValue = "0") val createdAtMillis: Long = System.currentTimeMillis(),
)

@Entity(tableName = "food_events", indices = [Index("foodName"), Index("type"), Index("occurredAtMillis")])
data class FoodEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val foodName: String,
    val type: String,
    val quantity: Double = 0.0,
    val unit: String = "pz",
    val occurredAtMillis: Long = System.currentTimeMillis(),
    val recipeId: Long? = null,
    val details: String = "",
)

@Entity(tableName = "barcode_cache")
data class BarcodeCacheEntity(
    @PrimaryKey val barcode: String,
    val name: String,
    val brand: String = "",
    val category: String = "Altro",
    val quantityLabel: String = "",
    val imageUrl: String? = null,
    val fetchedAtMillis: Long = System.currentTimeMillis(),
    val nutritionGrade: String? = null,
    val novaGroup: Int? = null,
    val energyKcal100g: Double? = null,
    val sugarsG100g: Double? = null,
    val saturatedFatG100g: Double? = null,
    val saltG100g: Double? = null,
    val fiberG100g: Double? = null,
    val proteinG100g: Double? = null,
    val ingredientsText: String? = null,
)
