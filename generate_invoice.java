package application;

import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.FileWriter;
import java.io.IOException;

public class generate_invoice {

    public void printInvoice(String invoiceContent) {
        // Create a PrinterJob
        PrinterJob job = PrinterJob.getPrinterJob();

        // Set the printable object
        job.setPrintable((graphics, pageFormat, pageIndex) -> {
            if (pageIndex > 0) {
                return Printable.NO_SUCH_PAGE;
            }

            // Draw the content of the invoice
            graphics.drawString(invoiceContent, 100, 100);

            return Printable.PAGE_EXISTS;
        });

        // Show print dialog to the user
        boolean doPrint = job.printDialog();

        if (doPrint) {
            try {
                // Perform the print operation
                job.print();
            } catch (PrinterException e) {
                e.printStackTrace();
            }
        }
    }

    public void saveInvoiceAsText(String invoiceContent, String fileName) {
        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write(invoiceContent);
            System.out.println("Invoice saved as: " + fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
