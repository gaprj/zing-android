package it.studio44z.zing.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [FoodLotEntity::class, RecipeEntity::class, ShoppingItemEntity::class, FoodEventEntity::class, BarcodeCacheEntity::class],
    version = 4,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun foodLotDao(): FoodLotDao
    abstract fun recipeDao(): RecipeDao
    abstract fun shoppingDao(): ShoppingDao
    abstract fun foodEventDao(): FoodEventDao
    abstract fun barcodeCacheDao(): BarcodeCacheDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE food_lots ADD COLUMN openedDateMillis INTEGER")
                db.execSQL("ALTER TABLE food_lots ADD COLUMN minQuantity REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE recipes ADD COLUMN notes TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE recipes ADD COLUMN rating INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE recipes ADD COLUMN favorite INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE recipes ADD COLUMN imageUri TEXT")
                db.execSQL("ALTER TABLE recipes ADD COLUMN isSeed INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE recipes ADD COLUMN createdAtMillis INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE recipes ADD COLUMN updatedAtMillis INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE shopping_items ADD COLUMN ignored INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE shopping_items ADD COLUMN createdAtMillis INTEGER NOT NULL DEFAULT 0")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_food_lots_barcode ON food_lots(barcode)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_food_lots_expiryDateMillis ON food_lots(expiryDateMillis)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_recipes_isSeed ON recipes(isSeed)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_recipes_favorite ON recipes(favorite)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_shopping_items_isChecked ON shopping_items(isChecked)")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS barcode_cache (barcode TEXT NOT NULL, name TEXT NOT NULL, brand TEXT NOT NULL, category TEXT NOT NULL, quantityLabel TEXT NOT NULL, imageUrl TEXT, fetchedAtMillis INTEGER NOT NULL, PRIMARY KEY(barcode))")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE barcode_cache ADD COLUMN nutritionGrade TEXT")
                db.execSQL("ALTER TABLE barcode_cache ADD COLUMN novaGroup INTEGER")
                db.execSQL("ALTER TABLE barcode_cache ADD COLUMN energyKcal100g REAL")
                db.execSQL("ALTER TABLE barcode_cache ADD COLUMN sugarsG100g REAL")
                db.execSQL("ALTER TABLE barcode_cache ADD COLUMN saturatedFatG100g REAL")
                db.execSQL("ALTER TABLE barcode_cache ADD COLUMN saltG100g REAL")
                db.execSQL("ALTER TABLE barcode_cache ADD COLUMN fiberG100g REAL")
                db.execSQL("ALTER TABLE barcode_cache ADD COLUMN proteinG100g REAL")
                db.execSQL("ALTER TABLE barcode_cache ADD COLUMN ingredientsText TEXT")
            }
        }

        fun get(context: Context): AppDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "dispensa-smart.db")
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                .build()
                .also { INSTANCE = it }
        }
    }
}
