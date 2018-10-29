/*
 * Copyright 2013, 2014 Deutsche Nationalbibliothek
 *
 * Licensed under the Apache License, Version 2.0 the "License";
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.metafacture.fix.biblio.marc21;

import org.metafacture.framework.FluxCommand;
import org.metafacture.framework.StreamReceiver;
import org.metafacture.framework.XmlReceiver;
import org.metafacture.framework.annotations.Description;
import org.metafacture.framework.annotations.In;
import org.metafacture.framework.annotations.Out;
import org.metafacture.framework.helpers.DefaultXmlPipe;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.util.Optional;


/**
 * A marc xml reader.
 * @author Markus Michael Geipel
 *
 */
@Description("A marc xml reader")
@In(XmlReceiver.class)
@Out(StreamReceiver.class)
@FluxCommand("handle-marcxml")
public final class MarcXmlHandler extends DefaultXmlPipe<StreamReceiver> {

	private static final String SUBFIELD = "subfield";
	private static final String DATAFIELD = "datafield";
	private static final String CONTROLFIELD = "controlfield";
	private static final String RECORD = "record";
	private static final String NAMESPACE = "http://www.loc.gov/MARC21/slim";
	private static final String LEADER = "leader";
	private static final String TYPE = "type";
	private String currentTag = "";
	private StringBuilder builder = new StringBuilder();

	@Override
	public void startElement(final String uri, final String localName, final String qName, final Attributes attributes)
			throws SAXException {
		if(SUBFIELD.equals(localName)){
			builder = new StringBuilder();
			currentTag = attributes.getValue("code");
		}else if(DATAFIELD.equals(localName)){
			getReceiver().startEntity(attributes.getValue("tag") + attributes.getValue("ind1") + attributes.getValue("ind2"));
		}else if(CONTROLFIELD.equals(localName)){
			builder = new StringBuilder();
			currentTag = attributes.getValue("tag");
		}else if(RECORD.equals(localName) && NAMESPACE.equals(uri)){
			getReceiver().startRecord("");
			Optional<String> recordType = Optional.ofNullable(attributes.getValue(TYPE));
			recordType.ifPresent(type -> getReceiver().literal(TYPE, type));
		}else if(LEADER.equals(localName)){
			builder = new StringBuilder();
			currentTag = LEADER;
		}
	}

	@Override
	public void endElement(final String uri, final String localName, final String qName) throws SAXException {
		if(SUBFIELD.equals(localName)){
			getReceiver().literal(currentTag, builder.toString().trim());

		}else if(DATAFIELD.equals(localName)){
			getReceiver().endEntity();
		}else if(CONTROLFIELD.equals(localName)){
			getReceiver().literal(currentTag, builder.toString().trim());

		}else if(RECORD.equals(localName)  && NAMESPACE.equals(uri)){
			getReceiver().endRecord();

		}else if(LEADER.equals(localName)){
			String leader = builder.toString();

			// Copied from Iso2709Constants due to package private
			final int RECORD_STATUS_POS = 5;
			final int IMPL_CODES_START = 6;
			final int SYSTEM_CHARS_START = 17;

			// Decompose leader for marc21 encoder
			getReceiver().startEntity(LEADER);

			String recordStatus = String.valueOf(leader.charAt(RECORD_STATUS_POS));
			getReceiver().literal(Marc21EventNames.RECORD_STATUS_LITERAL, recordStatus);

			String recordType = String.valueOf(leader.charAt(IMPL_CODES_START + Marc21Constants.RECORD_TYPE_INDEX));
			getReceiver().literal(Marc21EventNames.RECORD_TYPE_LITERAL, recordType);

			String bibliographicLevel = String.valueOf(leader.charAt(IMPL_CODES_START + Marc21Constants.BIBLIOGRAPHIC_LEVEL_INDEX));
			getReceiver().literal(Marc21EventNames.BIBLIOGRAPHIC_LEVEL_LITERAL, bibliographicLevel);

			String typeOfControl = String.valueOf(leader.charAt(IMPL_CODES_START + Marc21Constants.TYPE_OF_CONTROL_INDEX));
			getReceiver().literal(Marc21EventNames.TYPE_OF_CONTROL_LITERAL, typeOfControl);

			String characterCoding = String.valueOf(leader.charAt(IMPL_CODES_START + Marc21Constants.CHARACTER_CODING_INDEX));
			getReceiver().literal(Marc21EventNames.CHARACTER_CODING_LITERAL, characterCoding);

			String encodingLevel = String.valueOf(leader.charAt(SYSTEM_CHARS_START + Marc21Constants.ENCODING_LEVEL_INDEX));
			getReceiver().literal(Marc21EventNames.ENCODING_LEVEL_LITERAL, encodingLevel);

			String catalogingForm = String.valueOf(leader.charAt(SYSTEM_CHARS_START + Marc21Constants.CATALOGING_FORM_INDEX));
			getReceiver().literal(Marc21EventNames.CATALOGING_FORM_LITERAL, catalogingForm);

			String multipartLevel = String.valueOf(leader.charAt(SYSTEM_CHARS_START + Marc21Constants.MULTIPART_LEVEL_INDEX));
			getReceiver().literal(Marc21EventNames.MULTIPART_LEVEL_LITERAL, multipartLevel);

			getReceiver().endEntity();

		}
	}

	@Override
	public void characters(final char[] chars, final int start, final int length) throws SAXException {
		builder.append(chars, start, length);
	}

}
