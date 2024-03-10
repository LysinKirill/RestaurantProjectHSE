package data.dao

import data.dao.interfaces.FileReadStrategy
import data.dao.interfaces.FileWriteStrategy
import data.dao.interfaces.MenuDao
import data.entity.MenuEntryEntity
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class JsonMenuStorage(
    private val jsonMenuStoragePath: String,
    private val readStrategy: FileReadStrategy = ReadOrCreateEmptyStrategy(),
    private val writeStrategy: FileWriteStrategy = WriteOrCreateEmptyStrategy()
) : MenuDao {

    private val json = Json { prettyPrint = true }
    override fun getEntryByDishName(name: String): MenuEntryEntity? {
        val storedEntries: List<MenuEntryEntity> = getAllEntries()
        return storedEntries.find { entry -> entry.dish.name == name }
    }

    override fun addEntry(menuEntry: MenuEntryEntity) {
        val storedEntries: List<MenuEntryEntity> = getAllEntries()
        if(getEntryByDishName(menuEntry.dish.name) != null)
            return
        
        val updatedEntries = storedEntries.toMutableList()
        updatedEntries.add(menuEntry)
        val serializedUpdatedStorage = json.encodeToString(updatedEntries.toList())
        writeStrategy.write(jsonMenuStoragePath, serializedUpdatedStorage)
    }

    override fun getAllEntries(): List<MenuEntryEntity> {
        val storageFileText = readStrategy.read(jsonMenuStoragePath)

        return if (storageFileText.isBlank())
            listOf() else json.decodeFromString<List<MenuEntryEntity>>(storageFileText)
    }

    override fun removeEntry(dishName: String) {
        val storedEntities: List<MenuEntryEntity> = getAllEntries()
        val updatedEntries = storedEntities.toMutableList()

        updatedEntries.removeIf { oldEntry -> oldEntry.dish.name == dishName }
        val serializedUpdatedStorage = json.encodeToString(updatedEntries.toList())
        writeStrategy.write(jsonMenuStoragePath, serializedUpdatedStorage)
    }

    override fun updateEntry(updatedEntry: MenuEntryEntity) {
        val storedEntities: List<MenuEntryEntity> = getAllEntries()
        if(storedEntities.find { entry -> entry.dish.name == updatedEntry.dish.name } == null)
            return

        val updatedEntries = storedEntities.toMutableList()
        updatedEntries.removeIf { oldEntry -> oldEntry.dish.name == updatedEntry.dish.name }
        updatedEntries.add(updatedEntry)

        val serializedUpdatedStorage = json.encodeToString(updatedEntries.toList())
        writeStrategy.write(jsonMenuStoragePath, serializedUpdatedStorage)
    }
}