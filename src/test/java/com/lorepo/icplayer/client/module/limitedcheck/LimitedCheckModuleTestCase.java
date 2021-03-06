package com.lorepo.icplayer.client.module.limitedcheck;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.xml.sax.SAXException;

import com.google.gwt.xml.client.Element;
import com.lorepo.icf.utils.i18n.DictionaryWrapper;
import com.lorepo.icplayer.client.mockup.xml.XMLParserMockup;

@RunWith(PowerMockRunner.class)
@PrepareForTest(DictionaryWrapper.class)
public class LimitedCheckModuleTestCase {

	@Test
	public void moduleTypeName() {
		PowerMockito.spy(DictionaryWrapper.class);
		when(DictionaryWrapper.get("Limited_Check_name")).thenReturn("Limited Check");

		LimitedCheckModule module = new LimitedCheckModule();

		assertEquals("Limited Check", module.getModuleTypeName());
	}
	
	@Test
	public void load() throws SAXException, IOException {
		InputStream inputStream = getClass().getResourceAsStream("testdata/limitedCheckBasicModel.xml");
		XMLParserMockup xmlParser = new XMLParserMockup();
		Element element = xmlParser.parser(inputStream);
		
		LimitedCheckModule module = new LimitedCheckModule();
		module.load(element, "");
		
		assertEquals("check", module.getCheckText());
		assertEquals("unCheck", module.getUnCheckText());
		assertEquals("Text1", module.getRawWorksWith());
	}
	
	@Test
	public void modelToXML() {
		LimitedCheckModule module = new LimitedCheckModule();
		module.setCheckText("Some text");
		module.setUnCheckText("Some uncheck text");
		module.setRawWorksWith("Table1;Table1");
		
		String xml = module.modelToXML();
		
		assertEquals("<limitedCheck checkText='Some text' unCheckText='Some uncheck text'><![CDATA[Table1;Table1]]></limitedCheck>", xml);
	}
}
