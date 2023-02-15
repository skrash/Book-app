package com.skrash.book.FormatBook

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import java.io.File

class PDF(private val file: File) {

    private var pdfRenderer: PdfRenderer? = null

    init {
        val fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        pdfRenderer = PdfRenderer(fileDescriptor)
    }

    fun getCover(width: Int, height: Int): Bitmap{
        val page = pdfRenderer?.openPage(0)
        val bitmap = Bitmap.createBitmap(
            width, height,
            Bitmap.Config.ARGB_4444
        )
        if (page != null){
            page!!.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        }
        return bitmap
    }

    fun getPageCount(): Int{
        return if (pdfRenderer == null){
            return 0
        } else
        {
            pdfRenderer!!.pageCount
        }
    }

    fun openPage(pageNum: Int, width: Int, height: Int): Bitmap{
        val bitmap = Bitmap.createBitmap(
            width, height,
            Bitmap.Config.ARGB_4444
        )
        val page = pdfRenderer!!.openPage(pageNum)
        page!!.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        page.close()
        return bitmap
    }
}