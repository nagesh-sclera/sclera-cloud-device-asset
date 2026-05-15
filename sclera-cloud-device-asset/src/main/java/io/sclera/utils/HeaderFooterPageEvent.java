package io.sclera.utils;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.IOException;
import java.net.MalformedURLException;

public class HeaderFooterPageEvent extends PdfPageEventHelper {

    public void onEndPage(PdfWriter writer, Document document) {
        String img = String.valueOf(getClass().getClassLoader().getResource("images/JLL_logo.png"));
        Image image;
        try {
            PdfContentByte cb = writer.getDirectContent();
            image = Image.getInstance(img);
            image.scaleToFit(50,50);
            System.out.println("document : " + document.getPageSize().getWidth() + " " + document.getPageSize().getHeight());
            image.setAbsolutePosition(510, 810);
            cb.addImage(image);
        } catch (MalformedURLException e) {
            System.out.println(e);
        } catch (BadElementException e) {
            System.out.println(e);
        } catch (DocumentException e) {
            System.out.println(e);
        } catch (IOException e) {
            System.out.println(e);
        }
        ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER, new Phrase(document.getPageNumber()), 550, 30, 0);

    }
}