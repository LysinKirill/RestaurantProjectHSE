package data.dao.interfaces

interface FileReadStrategy {
    fun read(filePath: String) : String
}