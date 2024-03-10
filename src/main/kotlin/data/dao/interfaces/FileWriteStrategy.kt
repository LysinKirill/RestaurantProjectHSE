package data.dao.interfaces

interface FileWriteStrategy {
    fun write(filePath: String, text: String)
}