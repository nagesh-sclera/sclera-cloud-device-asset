package io.sclera.utils;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;

/**
 * iText page event handler that draws a logo image and a centered page number on each page of a
 * generated PDF document.
 */
public class HeaderFooterPageEvent extends PdfPageEventHelper {

    private static final Logger log = LoggerFactory.getLogger(HeaderFooterPageEvent.class);

    /**
     * Invoked at the end of each PDF page to render the logo image and the page number.
     */
    public void onEndPage(PdfWriter writer, Document document) {
        String img = String.valueOf(getClass().getClassLoader().getResource("images/JLL_logo.png"));
        Image image;
        try {
            PdfContentByte cb = writer.getDirectContent();
            image = Image.getInstance(img);
            image.scaleToFit(50,50);
            log.debug("{}", "document : " + document.getPageSize().getWidth() + " " + document.getPageSize().getHeight());
            image.setAbsolutePosition(510, 810);
            cb.addImage(image);
        } catch (MalformedURLException e) {
            log.debug("{}", e);
        } catch (BadElementException e) {
            log.debug("{}", e);
        } catch (DocumentException e) {
            log.debug("{}", e);
        } catch (IOException e) {
            log.debug("{}", e);
        }
        ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER, new Phrase(document.getPageNumber()), 550, 30, 0);

    }
}