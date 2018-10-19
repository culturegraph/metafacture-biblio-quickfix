package org.metafacture.fix.biblio.marc21;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.metafacture.biblio.marc21.Marc21Decoder;
import org.metafacture.biblio.marc21.Marc21Encoder;
import org.metafacture.framework.StreamReceiver;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.inOrder;

public class MetafactureMarc21EncoderTest {

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

    /**
     * This test is green if issue <a href="https://github.com/metafacture/metafacture-core/issues/278">#278</a> and <a href="https://github.com/metafacture/metafacture-core/issues/283">#283</a> are unresolved.
     */
    @Test(expected = IllegalStateException.class)
    public void decoderEncoderIssueIsNotFixed() {
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
}
