package ar.net.fpetrola.humo;

public class TextViewHelper
{
    public static final String CURSOR_STYLE= "Cursor";
    public static final String PRODUCTION_FOUND_STYLE= "production-found";
    public static final String DEFAULT_STYLE= "default";
    public static final String FETCH_STYLE= "fetch";
    public static final String CURLY_STYLE= "curly";
    public static final String PRODUCTION_BEFORE_REPLACEMENT_STYLE= "production-before-replacement";

    public static HumoTextDocument createAndSetupDocument(StringBuilder sourceCode)
    {
	HumoTextDocument styleDocument= HumoTextDocumentFactory.createTextDocument();

	styleDocument.clear();
	styleDocument.insert(0, sourceCode.toString());
	for (int i= 0; i < sourceCode.length(); i++)
	{
	    if (sourceCode.charAt(i) == '{' || sourceCode.charAt(i) == '}')
		styleDocument.setSpan(CURLY_STYLE, i, i+1);
	    else
		styleDocument.setSpan(DEFAULT_STYLE, i, i+1);
	}

	return styleDocument;
    }
}
