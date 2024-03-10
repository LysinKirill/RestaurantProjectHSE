package data.dao

import data.dao.interfaces.FileWriteStrategy
import java.io.File

class WriteOrCreateEmptyStrategy : FileWriteStrategy {
    override fun write(filePath: String, text: String) {
        val file = File(filePath)
        file.writeText(text)
    }
}