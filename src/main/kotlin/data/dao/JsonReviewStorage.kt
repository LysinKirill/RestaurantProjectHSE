package data.dao

import data.dao.interfaces.FileReadStrategy
import data.dao.interfaces.FileWriteStrategy
import data.dao.interfaces.ReviewDao
import data.entity.ReviewEntity
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDateTime

class JsonReviewStorage(
    private val jsonReviewStoragePath: String,
    private val readStrategy: FileReadStrategy = ReadOrCreateEmptyStrategy(),
    private val writeStrategy: FileWriteStrategy = WriteOrCreateEmptyStrategy()
) : ReviewDao {
    private val json = Json { prettyPrint = true }
    override fun addReview(
        accountName: String,
        dishName: String,
        text: String,
        rating: Byte,
        timeStamp: LocalDateTime,
    ) {
        val storedReviews = getAllReviews().toMutableList()
        val newId = if (storedReviews.isEmpty()) 1 else storedReviews.maxOf { order -> order.id } + 1

        val newReview = ReviewEntity(
            id = newId,
            accountName = accountName,
            dishName = dishName,
            text = text,
            rating = rating,
            timeStamp = timeStamp,
        )
        storedReviews.add(newReview)
        writeStrategy.write(jsonReviewStoragePath, json.encodeToString(storedReviews.toList()))
    }

    override fun getReview(reviewId: Long) = getAllReviews().find { it.id == reviewId }

    override fun removeReview(reviewId: Long) = writeStrategy.write(
        filePath = jsonReviewStoragePath,
        text = json.encodeToString(getAllReviews().filterNot { it.id == reviewId })
    )

    override fun getAllReviews(): List<ReviewEntity> {
        val storageFileText = readStrategy.read(jsonReviewStoragePath)
        return if (storageFileText.isBlank())
            listOf() else json.decodeFromString<List<ReviewEntity>>(storageFileText)
    }

    override fun updateReview(updatedReview: ReviewEntity) {
        val storedReviews = getAllReviews().toMutableList()
        if (!storedReviews.removeIf { it.id == updatedReview.id }) {
            // Nothing to update. Order with this id not found
            return
        }
        storedReviews.add(updatedReview)
        writeStrategy.write(jsonReviewStoragePath, json.encodeToString(storedReviews.toList()))
    }
}