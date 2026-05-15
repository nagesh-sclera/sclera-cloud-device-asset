package io.sclera.utils;

import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPCellEvent;
import com.itextpdf.text.pdf.PdfPTable;

public class RoundedCell implements PdfPCellEvent {
    public void cellLayout(PdfPCell cell, Rectangle position,
                           PdfContentByte[] canvases) {
        PdfContentByte canvas = canvases[PdfPTable.LINECANVAS];
        canvas.roundRectangle(position.getLeft() + 1.5f , position.getBottom() + 1.5f, position.getWidth() - 3,position.getHeight() - 3,4);
        canvas.stroke();

    }

}