package it.studio44z.zing.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodLotDao {
    @Query("SELECT * FROM food_lots ORDER BY CASE WHEN expiryDateMillis IS NULL THEN 1 ELSE 0 END, expiryDateMillis ASC, name COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<FoodLotEntity>>

    @Query("SELECT * FROM food_lots ORDER BY CASE WHEN expiryDateMillis IS NULL THEN 1 ELSE 0 END, expiryDateMillis ASC, name COLLATE NOCASE ASC")
    suspend fun getAll(): List<FoodLotEntity>

    @Query("SELECT * FROM food_lots WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): FoodLotEntity?

    @Query("SELECT * FROM food_lots WHERE barcode = :barcode ORDER BY createdAtMillis DESC LIMIT 1")
    suspend fun findByBarcode(barcode: String): FoodLotEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: FoodLotEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<FoodLotEntity>)

    @Update
    suspend fun update(item: FoodLotEntity)

    @Delete
    suspend fun delete(item: FoodLotEntity)

    @Query("DELETE FROM food_lots")
    suspend fun clear()
}

@Dao
interface RecipeDao {
    @Query("SELECT * FROM recipes ORDER BY favorite DESC, name COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<RecipeEntity>>

    @Query("SELECT * FROM recipes ORDER BY favorite DESC, name COLLATE NOCASE ASC")
    suspend fun getAll(): List<RecipeEntity>

    @Query("SELECT COUNT(*) FROM recipes WHERE isSeed = 1")
    suspend fun countSeed(): Int

    @Query("SELECT * FROM recipes WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): RecipeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: RecipeEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSeedIfAbsent(item: RecipeEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<RecipeEntity>)

    @Update
    suspend fun update(item: RecipeEntity)

    @Delete
    suspend fun delete(item: RecipeEntity)

    @Query("DELETE FROM recipes")
    suspend fun clear()

    @Query("DELETE FROM recipes WHERE isSeed = 0")
    suspend fun clearPersonal()

    @Query("UPDATE recipes SET favorite = 0, rating = 0, notes = '', imageUri = NULL, updatedAtMillis = :updatedAt WHERE isSeed = 1")
    suspend fun resetSeedPreferences(updatedAt: Long)
}

@Dao
interface ShoppingDao {
    @Query("SELECT * FROM shopping_items WHERE ignored = 0 ORDER BY isChecked ASC, category COLLATE NOCASE ASC, name COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<ShoppingItemEntity>>

    @Query("SELECT * FROM shopping_items WHERE ignored = 0 ORDER BY isChecked ASC, category COLLATE NOCASE ASC, name COLLATE NOCASE ASC")
    suspend fun getAll(): List<ShoppingItemEntity>

    @Query("SELECT * FROM shopping_items WHERE ignored = 0 AND isChecked = 0")
    suspend fun getOpen(): List<ShoppingItemEntity>

    @Query("SELECT * FROM shopping_items WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): ShoppingItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ShoppingItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ShoppingItemEntity>)

    @Update
    suspend fun update(item: ShoppingItemEntity)

    @Delete
    suspend fun delete(item: ShoppingItemEntity)

    @Query("DELETE FROM shopping_items")
    suspend fun clear()
}

@Dao
interface FoodEventDao {
    @Query("SELECT * FROM food_events ORDER BY occurredAtMillis DESC")
    fun observeAll(): Flow<List<FoodEventEntity>>

    @Query("SELECT * FROM food_events WHERE occurredAtMillis >= :since ORDER BY occurredAtMillis DESC")
    suspend fun since(since: Long): List<FoodEventEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: FoodEventEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<FoodEventEntity>)

    @Query("DELETE FROM food_events")
    suspend fun clear()
}

@Dao
interface BarcodeCacheDao {
    @Query("SELECT * FROM barcode_cache WHERE barcode = :barcode LIMIT 1")
    suspend fun findByBarcode(barcode: String): BarcodeCacheEntity?

    @Query("SELECT * FROM barcode_cache ORDER BY fetchedAtMillis DESC")
    suspend fun getAll(): List<BarcodeCacheEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: BarcodeCacheEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<BarcodeCacheEntity>)

    @Query("DELETE FROM barcode_cache")
    suspend fun clear()
}
