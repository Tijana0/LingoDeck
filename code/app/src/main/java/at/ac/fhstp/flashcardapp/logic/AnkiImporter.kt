package at.ac.fhstp.flashcardapp.logic

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import at.ac.fhstp.flashcardapp.data.FlashcardRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class AnkiImporter(
    private val context: Context,
    private val repository: FlashcardRepository
) {

    suspend fun importApkg(inputStream: InputStream, deckName: String) {
        withContext(Dispatchers.IO) {
            val tempFile = File(context.cacheDir, "collection.anki2")
            var foundDb = false

            ZipInputStream(inputStream).use { zip ->
                var entry: ZipEntry? = zip.nextEntry
                while (entry != null) {
                    if (entry.name == "collection.anki2") {
                        FileOutputStream(tempFile).use { fileOut ->
                            zip.copyTo(fileOut)
                        }
                        foundDb = true
                        break
                    }
                    entry = zip.nextEntry
                }
            }

            if (!foundDb) {
                throw Exception("Invalid .apkg file: collection.anki2 not found")
            }

            val newDeckId = repository.addDeck(deckName, "en", "en").toInt()

            val ankiDb = SQLiteDatabase.openDatabase(tempFile.path, null, SQLiteDatabase.OPEN_READONLY)
            try {
                val cursor = ankiDb.rawQuery("SELECT flds FROM notes", null)
                val fldsIndex = cursor.getColumnIndex("flds")

                if (fldsIndex != -1) {
                    while (cursor.moveToNext()) {
                        val flds = cursor.getString(fldsIndex)
                        val parts = flds.split("\u001f")
                        if (parts.size >= 2) {
                            val front = parts[0]
                            val back = parts[1]
                            repository.addFlashcard(newDeckId, front, back)
                        }
                    }
                }
                cursor.close()
            } catch (e: Exception) {
                throw e
            } finally {
                ankiDb.close()
                tempFile.delete()
            }
        }
    }
}