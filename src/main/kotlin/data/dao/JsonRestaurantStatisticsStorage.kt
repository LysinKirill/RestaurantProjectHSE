package data.dao

import data.dao.interfaces.FileReadStrategy
import data.dao.interfaces.FileWriteStrategy
import data.dao.interfaces.RestaurantStatisticsDao
import data.entity.RestaurantStatisticsEntity
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class JsonRestaurantStatisticsStorage(
    private val jsonRestaurantStatisticsPath: String,
    private val readStrategy: FileReadStrategy = ReadOrCreateEmptyStrategy(),
    private val writeStrategy: FileWriteStrategy = WriteOrCreateEmptyStrategy()
) : RestaurantStatisticsDao {
    private val json = Json { prettyPrint = true }
    override fun getStatistics() : RestaurantStatisticsEntity {
        val storageFileText = readStrategy.read(jsonRestaurantStatisticsPath)
        if(storageFileText.isBlank())
            return RestaurantStatisticsEntity(0.0)
        return json.decodeFromString<RestaurantStatisticsEntity>(storageFileText)
    }

    override fun saveStatistics(statistics: RestaurantStatisticsEntity) {
        val serializedStatistics = json.encodeToString(statistics)
        writeStrategy.write(jsonRestaurantStatisticsPath, serializedStatistics)
    }
}