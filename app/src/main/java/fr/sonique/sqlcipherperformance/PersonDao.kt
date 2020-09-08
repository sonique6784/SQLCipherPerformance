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

import androidx.room.*

@Dao
interface PersonDao {
    @Query("SELECT * FROM person")
    fun getAllPersons(): List<Person>

    @Query("SELECT * FROM person WHERE id = :id")
    fun getById(id: Long): Person

    @Query("SELECT * FROM person WHERE first_name LIKE :find")
    fun findByFirstName(find: String): List<Person>

    @Query("SELECT * FROM person WHERE cv_info LIKE :find")
    fun findByCV(find: String): List<Person>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPersons(vararg persons: Person)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPersonList(persons: List<Person>)

    @Update
    fun updatePersons(vararg persons: Person)

    @Delete
    fun deletePersons(vararg persons: Person)

    @Query("DELETE FROM person")
    fun deleteAll()
}