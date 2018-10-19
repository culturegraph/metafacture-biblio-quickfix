package org.metafacture.fix.biblio.marc21;

import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.metafacture.framework.StreamReceiver;
import org.metafacture.strings.StringConcatenator;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.inOrder;

public class FixedMarc21EncoderTest {

    private Marc21Decoder marc21Decoder;

    @Mock
    private StreamReceiver receiver;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        marc21Decoder = new Marc21Decoder();
        marc21Decoder.setReceiver(receiver);
    }

    @After
    public void cleanup() {
        marc21Decoder.closeStream();
    }

    @Test
    public void decoderEncoderIssueIsFixed() {
        Marc21Encoder marc21Encoder = new Marc21Encoder();
        marc21Encoder.setReceiver(marc21Decoder);
        marc21Encoder.startRecord("identifier");
        marc21Encoder.startEntity("leader");
        marc21Encoder.literal("characterCodingScheme", "a");
        marc21Encoder.endEntity();
        marc21Encoder.literal("001", "identifier");
        marc21Encoder.startEntity("021a ");
        marc21Encoder.literal("v", "Fritz");
        marc21Encoder.literal("n", "Bauer");
        marc21Encoder.endEntity();
        marc21Encoder.endRecord();

        final InOrder ordered = inOrder(receiver);
        ordered.verify(receiver).startRecord("identifier");
        ordered.verify(receiver).startEntity("leader");
        ordered.verify(receiver).literal("characterCodingScheme", "a");
        ordered.verify(receiver).endEntity();
        ordered.verify(receiver).startEntity("021a ");
        ordered.verify(receiver).literal("v", "Fritz");
        ordered.verify(receiver).literal("n", "Bauer");
        ordered.verify(receiver).endEntity();
        ordered.verify(receiver).endRecord();
    }

    @Test
    public void encodeDecode() {
        StringConcatenator buf = new StringConcatenator();
        Marc21Decoder decoder = new Marc21Decoder();
        Marc21Encoder encoder = new Marc21Encoder();

        decoder.setReceiver(encoder).setReceiver(buf);
        decoder.process(fakeMarc21("001"));

        assertEquals(buf.getString(), fakeMarc21("001"));
    }

    @Test
    public void buildFakeRecord() {
        fakeMarc21("001");
    }

    private String fakeMarc21(String ... fields) {
        StringConcatenator buf = new StringConcatenator();

        Marc21Encoder encoder = new Marc21Encoder();
        encoder.setReceiver(buf);
        encoder.startRecord("id");
        encoder.startEntity("leader");
        encoder.literal("status", "n");
        encoder.literal("type", "o");
        encoder.literal("bibliographicLevel", "a");
        encoder.literal("typeOfControl", " ");
        encoder.literal("characterCodingScheme", "a");
        encoder.endEntity();

        Arrays.stream(fields).forEach(field -> encoder.literal(field, field));

        encoder.endRecord();
        encoder.closeStream();

        return buf.getString();
    }
}
