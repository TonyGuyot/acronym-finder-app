package io.github.tonyguyot.acronym.operations;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import io.github.tonyguyot.acronym.data.Acronym;

/**
 * Parser for an XML response from the Silmaril acronym server.
 */
public class AcronymXmlParser {

    // constant definitions for the elements in the XML document
    private static final String ROOT_ELEMENT = "acronym";
    private static final String ITEM_ELEMENT = "acro";
    private static final String EXPANSION_ELEMENT = "expan";
    private static final String COMMENT_ELEMENT = "comment";
    private static final String NAME_ATTRIBUTE = "nym";
    private static final String DEWEY_ATTRIBUTE = "dewey";
    private static final String DATE_ATTRIBUTE = "added";

    // this is a static class
    private AcronymXmlParser() {
        throw new UnsupportedOperationException();
    }

    // the main parse method
    public static ArrayList<Acronym> parse(InputStream input)
        throws XmlPullParserException, IOException {

        // create and configure a new parser factory
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(false);

        // use the factory to create a parser
        XmlPullParser parser = factory.newPullParser();
        parser.setInput(new InputStreamReader(input));

        // define work variables used by the parsing procedure
        // variables are defined only once in order to minimize
        // garbage collector activity
        String tagName;
        String text = null;
        String currentName = null;
        String currentExpansion = null;
        String currentComment = null;
        String currentDewey = null;
        String currentDate = null;

        // parse the stream
        ArrayList<Acronym> acronyms = new ArrayList<>();
        int eventType = parser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {

            // a new start tag has been encountered
            if (eventType == XmlPullParser.START_TAG) {
                tagName = parser.getName();

                if (tagName.equals(ITEM_ELEMENT)) {
                    // start of acronym item => reset all variables
                    currentName = parser.getAttributeValue(null, NAME_ATTRIBUTE);
                    currentDewey = parser.getAttributeValue(null, DEWEY_ATTRIBUTE);
                    currentDate = parser.getAttributeValue(null, DATE_ATTRIBUTE);
                    currentExpansion = null;
                    currentComment = null;
                }
            }

            // a new text has been encountered
            if (eventType == XmlPullParser.TEXT) {
                text = parser.getText().trim();
            }

            // a new end tag has been encountered
            if (eventType == XmlPullParser.END_TAG) {
                tagName = parser.getName();

                if (tagName.equals(EXPANSION_ELEMENT)) {
                    currentExpansion = text;
                } else if (tagName.equals(COMMENT_ELEMENT)) {
                    currentComment = text;
                } else if (tagName.equals(ITEM_ELEMENT)) {
                    // end of acronym item => store the variables
                    if (currentName != null && currentExpansion != null) {
                        acronyms.add(
                                new Acronym.Builder(currentName, currentExpansion)
                                        .comment(currentComment)
                                        .dewey(currentDewey)
                                        .added(currentDate)
                                        .create());
                    }
                }

            }

            // read the next element in the stream
            eventType = parser.next();
        }

        return acronyms;
    }
}
