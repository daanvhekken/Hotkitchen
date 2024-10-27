package org.example.repositories

import kotlinx.coroutines.Dispatchers
import org.example.dtos.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

class MealRepositoryImpl(database: Database): MealRepository {
    object Meals : Table() {
        val mealId = long("mealId").autoIncrement()
        val title = varchar("title", length = 50)
        val price = float("price")
        val imageUrl = varchar("imageUrl", length = 255)

        override val primaryKey = PrimaryKey(mealId)
    }

    object MealCategoryIds : Table() {
        val mealId = long("mealId").references(Meals.mealId)
        val categoryId = long("categoryId")

        override val primaryKey = PrimaryKey(mealId, categoryId)
    }

    init {
        transaction(database) {
            SchemaUtils.create(Meals)
            SchemaUtils.create(MealCategoryIds)
        }
    }

    override suspend fun create(meal: CreateMealDTO): Long = dbQuery {
        val mealId = Meals
            .insert {
                it[title] = meal.title
                it[price] = meal.price
                it[imageUrl] = meal.imageUrl
            }.resultedValues!!.first()[Meals.mealId]

        meal.categoryIds.forEach { categoryId ->
            MealCategoryIds.insert {
                it[MealCategoryIds.mealId] = mealId
                it[MealCategoryIds.categoryId] = categoryId
            }
        }

        mealId
    }

    override suspend fun read(id: Long): MealDTO? = dbQuery {
        val categoryIds = MealCategoryIds.select { MealCategoryIds.mealId eq id }
            .map { it[MealCategoryIds.categoryId] }

        Meals
            .select { Meals.mealId eq id }
            .map { MealDTO(
                it[Meals.mealId],
                it[Meals.title],
                it[Meals.price],
                it[Meals.imageUrl],
                categoryIds
            ) }
            .singleOrNull()
    }

    override suspend fun readAll(): List<MealDTO> = dbQuery {
        Meals
            .selectAll()
            .map { MealDTO(
                it[Meals.mealId],
                it[Meals.title],
                it[Meals.price],
                it[Meals.imageUrl],
                MealCategoryIds.select { MealCategoryIds.mealId eq it[Meals.mealId] }
                    .map { it[MealCategoryIds.categoryId] }
            ) }
    }

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}