package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.migration.Migration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [GarmentItem::class, HistoryLog::class], version = 5, exportSchema = false)
@TypeConverters(Converters::class)
abstract class GarmentDatabase : RoomDatabase() {

    abstract fun garmentDao(): GarmentDao
    abstract fun historyDao(): HistoryDao

    companion object {
        @Volatile
        private var INSTANCE: GarmentDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `history_logs` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `timestamp` INTEGER NOT NULL, `description` TEXT NOT NULL)"
                )
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE history_logs ADD COLUMN garmentId INTEGER DEFAULT NULL")
                db.execSQL("ALTER TABLE history_logs ADD COLUMN size TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE history_logs ADD COLUMN stockValue INTEGER DEFAULT NULL")
                db.execSQL("ALTER TABLE history_logs ADD COLUMN totalStockValue INTEGER DEFAULT NULL")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE history_logs ADD COLUMN actionType TEXT NOT NULL DEFAULT 'UPDATE'")
            }
        }

        // Feature 1 & 2: Non-destructive migration to support soft delete, undo persistence, and low-stock thresholds
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE garment_items ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE garment_items ADD COLUMN deletedAt INTEGER DEFAULT NULL")
                db.execSQL("ALTER TABLE garment_items ADD COLUMN lowStockThreshold INTEGER DEFAULT NULL")
            }
        }

        fun getDatabase(context: Context, scope: CoroutineScope): GarmentDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GarmentDatabase::class.java,
                    "garment_inventory_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .addCallback(GarmentDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class GarmentDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateDatabase(database.garmentDao())
                    }
                }
            }

            suspend fun populateDatabase(dao: GarmentDao) {
                if (dao.getItemCount() == 0) {
                    dao.insertAll(InitialGarmentData.getInitialGarments())
                }
            }
        }
    }
}
