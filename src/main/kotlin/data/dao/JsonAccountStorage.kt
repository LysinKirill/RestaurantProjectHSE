package data.dao

import data.dao.interfaces.AccountDao
import data.dao.interfaces.FileReadStrategy
import data.dao.interfaces.FileWriteStrategy
import data.entity.AccountEntity
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class JsonAccountStorage(
    private val jsonAccountStoragePath: String,
    private val readStrategy: FileReadStrategy = ReadOrCreateEmptyStrategy(),
    private val writeStrategy: FileWriteStrategy = WriteOrCreateEmptyStrategy()
) : AccountDao {
    private val json = Json { prettyPrint = true }

    override fun addAccount(account: AccountEntity) {
        val storageFileText = readStrategy.read(jsonAccountStoragePath)
        val storedAccounts: List<AccountEntity> =
            if (storageFileText.isBlank()) listOf()
            else json.decodeFromString(storageFileText)
        val updatedSessions = storedAccounts.toMutableList()

        updatedSessions.removeIf { oldAccount -> oldAccount.name == account.name }
        updatedSessions.add(account)
        val serializedUpdatedStorage = json.encodeToString(updatedSessions.toList())
        writeStrategy.write(jsonAccountStoragePath, serializedUpdatedStorage)
    }

    override fun getAccount(accountName: String): AccountEntity? {
        val storageFileText = readStrategy.read(jsonAccountStoragePath)
        val storedAccounts: List<AccountEntity> =
            if (storageFileText.isBlank()) listOf() else json.decodeFromString(storageFileText)

        return storedAccounts.find { account -> account.name == accountName }
    }

    override fun getAllAccounts(): List<AccountEntity> {
        val storageFileText = readStrategy.read(jsonAccountStoragePath)

        return if (storageFileText.isBlank()) listOf() else json.decodeFromString<List<AccountEntity>>(storageFileText)
    }
}