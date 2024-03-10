package data.dao

import data.dao.interfaces.FileReadStrategy
import java.io.File
import java.io.FileNotFoundException

class ReadOrCreateEmptyStrategy : FileReadStrategy {
    override fun read(filePath: String): String {
        val file = File(filePath)
        return try {
            file.readText()
        } catch (exception: FileNotFoundException) {
            file.createNewFile()
            ""
        }
    }
}