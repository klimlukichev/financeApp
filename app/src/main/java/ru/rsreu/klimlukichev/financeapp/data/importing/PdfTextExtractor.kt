package ru.rsreu.klimlukichev.financeapp.data.importing

import android.content.Context
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import java.io.InputStream

class PdfTextExtractor(
    context: Context,
) {

    private val appContext = context.applicationContext

    fun extract(inputStream: InputStream): String {
        PDFBoxResourceLoader.init(appContext)
        return PDDocument.load(inputStream).use { document ->
            PDFTextStripper().getText(document)
        }
    }
}
