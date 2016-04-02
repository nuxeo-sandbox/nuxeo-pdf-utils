/**
 *
 */

package org.nuxeo.pdf.operations;

import java.io.IOException;

import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.collectors.DocumentModelCollector;
import org.nuxeo.ecm.automation.core.util.DocumentHelper;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.pdf.PDFTextExtractor;

/**
 * <p>
 * If the pdf is encrypted, a password is required.
 * 
 * @author fvadon
 */
@Operation(id = ExtractTextFromPDFOp.ID, category = Constants.CAT_DOCUMENT, label = "ExtractTextFromPDF", description = "")
public class ExtractTextFromPDFOp {

    public static final String ID = "ExtractTextFromPDF";

    @Context
    protected CoreSession session;

    @Param(name = "pdfxpath", required = false)
    protected String pdfxpath = "file:content";

    @Param(name = "save", required = false)
    protected boolean save = false;

    @Param(name = "targetxpath", required = false)
    protected String targetxpath;

    @Param(name = "patterntofind", required = false)
    protected String patterntofind;

    @Param(name = "removepatternfromresult", required = false)
    protected boolean removepatternfromresult = false;

    @Param(name = "password", required = false)
    protected String password = null;

    @OperationMethod(collector = DocumentModelCollector.class)
    public DocumentModel run(DocumentModel input) throws IOException {
        PDFTextExtractor textExtractor = new PDFTextExtractor(input, pdfxpath);
        textExtractor.setPassword(password);
        
        String extractedText = null;
        if (removepatternfromresult) {
            extractedText = textExtractor.extractLastPartOfLine(patterntofind);
        } else {
            extractedText = textExtractor.extractLineOf(patterntofind);
        }

        if (extractedText != null) {
            input.setPropertyValue(targetxpath, extractedText);
        } else {
            DocumentHelper.removeProperty(input, targetxpath);
        }
        if (save) {
            input = session.saveDocument(input);
        }

        return input;
    }

}
