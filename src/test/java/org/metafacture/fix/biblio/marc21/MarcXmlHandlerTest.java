package org.metafacture.fix.biblio.marc21;

import org.junit.Before;
import org.junit.Test;
import org.metafacture.strings.StringConcatenator;
import org.metafacture.xml.XmlDecoder;

import java.io.FileReader;
import java.io.Reader;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

public class MarcXmlHandlerTest {

    private MarcXmlHandler handler;
    private StringConcatenator buffer;

    @Before
    public void setUp() throws Exception {
        handler = new MarcXmlHandler();
        buffer = new StringConcatenator();
    }

    @Test
    public void shouldEncodeMARCXMLRecordToMarc21() throws Exception {
        Reader record = new FileReader("src/test/resources/record.xml");
        XmlDecoder decoder = new XmlDecoder();
        decoder.setReceiver(handler)
                .setReceiver(new Marc21Encoder())
                .setReceiver(buffer);
        decoder.process(record);
        decoder.closeStream();

        int recordCount = countRecords(buffer.getString());
        assertThat(recordCount, equalTo(1));
    }

    @Test
    public void shouldEncodeMARCXMLCollectionOfSizeTwoToMarc21() throws Exception {
        Reader record = new FileReader("src/test/resources/collection.xml");
        XmlDecoder decoder = new XmlDecoder();
        decoder.setReceiver(handler)
                .setReceiver(new Marc21Encoder())
                .setReceiver(buffer);
        decoder.process(record);
        decoder.closeStream();

        int recordCount = countRecords(buffer.getString());
        assertThat(recordCount, equalTo(2));
    }

    private static int countRecords(String str){
        String[] lines = str.split("\u001D");
        return  lines.length;
    }

}