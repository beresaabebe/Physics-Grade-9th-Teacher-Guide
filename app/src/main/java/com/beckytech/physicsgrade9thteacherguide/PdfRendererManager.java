package com.beckytech.physicsgrade9thteacherguide;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.ParcelFileDescriptor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class PdfRendererManager {
    private PdfRenderer pdfRenderer;
    private ParcelFileDescriptor parcelFileDescriptor;

    public PdfRendererManager(Context context, String assetName) throws IOException {
        File file = new File(context.getCacheDir(), assetName);
        if (!file.exists()) {
            try (InputStream is = context.getAssets().open(assetName);
                 FileOutputStream os = new FileOutputStream(file)) {
                byte[] buffer = new byte[1024];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
            }
        }
        parcelFileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
        pdfRenderer = new PdfRenderer(parcelFileDescriptor);
    }

    public int getPageCount() {
        return pdfRenderer != null ? pdfRenderer.getPageCount() : 0;
    }

    public Bitmap renderPage(int pageIndex) {
        if (pdfRenderer == null || pageIndex < 0 || pageIndex >= pdfRenderer.getPageCount()) {
            return null;
        }
        PdfRenderer.Page page = pdfRenderer.openPage(pageIndex);
        // Increase scale for better quality
        int width = (int) (page.getWidth() * 1.5);
        int height = (int) (page.getHeight() * 1.5);
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
        page.close();
        return bitmap;
    }

    public void close() {
        try {
            if (pdfRenderer != null) pdfRenderer.close();
            if (parcelFileDescriptor != null) parcelFileDescriptor.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
