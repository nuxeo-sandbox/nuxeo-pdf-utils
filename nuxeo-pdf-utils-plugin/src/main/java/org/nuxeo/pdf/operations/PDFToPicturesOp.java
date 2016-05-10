package org.nuxeo.pdf.operations;

import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.util.BlobList;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.pdf.PDFPageExtractor;

/**
 *
 */
@Operation(id = PDFToPicturesOp.ID, category = Constants.CAT_CONVERSION, label = "PDF: Convert to Pictures", description = "Convert each page of a PDF into a picture. Returns Blob list of pictures.")
public class PDFToPicturesOp {

    public static final String ID = "PDF.PDFToPictures";

    @Context
    protected CoreSession session;

    @Param(name = "fileName", required = false)
    protected String fileName = "";

    @Param(name = "xpath", required = false, values = {"file:content"})
    protected String xpath = "";

    @Param(name = "password", required = false)
    protected String password = null;

    @OperationMethod
    public BlobList run(DocumentModel inDoc) {

        PDFPageExtractor pe = new PDFPageExtractor(inDoc, xpath);
        pe.setPassword(password);

        BlobList result = pe.getPagesAsImages( fileName );

        return result;
    }
}
