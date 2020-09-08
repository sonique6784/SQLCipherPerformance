/*
 * Copyright (C) 2020 Sonique Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.sonique.sqlcipherperformance

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SQLiteDatabaseHook
import net.sqlcipher.database.SupportFactory


@Database(entities = [Person::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun personDao(): PersonDao

    companion object {
        @Volatile
        private var personDB: AppDatabase? = null
        @Volatile
        private var personDBSecure: AppDatabase? = null
        @Volatile
        private var personDBSecureWithMemorySecurity: AppDatabase? = null

        fun getInstance(
            context: Context,
            secure: Boolean = false,
            memorySecure: Boolean = false
        ): AppDatabase {
            return if (secure) {
                if(memorySecure) {
                    personDBSecure ?: synchronized(this) {
                        personDBSecure ?: buildDatabase(context, secure, memorySecure).also { personDBSecure = it }
                    }
                } else {
                    personDBSecureWithMemorySecurity ?: synchronized(this) {
                        personDBSecureWithMemorySecurity ?: buildDatabase(context, secure, memorySecure).also { personDBSecureWithMemorySecurity = it }
                    }
                }
            } else {
                personDB ?: synchronized(this) {
                    personDB ?: buildDatabase(context, secure).also { personDB = it }
                }
            }
        }

        private fun buildDatabase(
            context: Context,
            secure: Boolean,
            memorySecure: Boolean = false
        ): AppDatabase {
            val dbname = if(secure && memorySecure) {
                "encrypted-with-mem"
            } else if(secure && !memorySecure) {
                "encrypted"
            } else {
                "not-encrypted"
            }
            val builder = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java, "${dbname}.db"
            )
            if (secure) {
                val passphrase: ByteArray =
                    SQLiteDatabase.getBytes("P@s5P4ras3VeryL0n9".toCharArray())
                val factory = SupportFactory(passphrase, object : SQLiteDatabaseHook {
                    override fun preKey(database: SQLiteDatabase?) = Unit

                    override fun postKey(database: SQLiteDatabase?) {
                        if (memorySecure) {
                            database?.rawExecSQL(
                                "PRAGMA cipher_memory_security = ON"
                            )
                        } else {
                            database?.rawExecSQL("PRAGMA cipher_memory_security = OFF")
                        }
                    }
                })
                builder.openHelperFactory(factory)
            }

            return builder.build()
        }
    }
}
