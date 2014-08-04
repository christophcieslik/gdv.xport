/*
 * Copyright (c) 2014 by Oli B.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express orimplied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * (c)reated 31-Jul-2014 by Oli B. (ob@aosd.de)
 */

package gdv.xport.satz.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import gdv.xport.feld.NumFeld;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.stream.*;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import patterntesting.runtime.annotation.Broken;
import patterntesting.runtime.junit.SmokeRunner;

/**
 * Unit tests for {@link SatzXml} class.
 *
 * @author oliver (oliver.boehm@gmail.com)
 * @since 1.0 (31.07.2014)
 */
@RunWith(SmokeRunner.class)
public class SatzXmlTest {

    private static final XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
    private static SatzXml satz100;

    /**
     * Setzt ein SatzXml-Objekt fuer den Satz 100 auf.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws XMLStreamException the XML stream exception
     */
    @BeforeClass
    public static void setUpSatz100() throws IOException, XMLStreamException {
        InputStream istream = SatzXmlTest.class.getResourceAsStream("Satz100.xml");
        assertNotNull("resource 'Satz100.xml' not found", istream);
        XMLEventReader parser = xmlInputFactory.createXMLEventReader(istream);
        try {
            satz100 = new SatzXml(parser);
        } finally {
            istream.close();
        }
    }

    /**
     * Test method for {@link SatzXml#getSatzart()}.
     */
    @Test
    public void testGetSatzart() {
        assertEquals(100, satz100.getSatzart());
    }

    /**
     * Auch der Satz 100 kann eine Sparte beinhalten.
     */
    @Test
    @Broken(till = "05-Aug-2014", why="not yet implemented")
    public void testGetSparte() {
        NumFeld sparte = satz100.getSatzartFeld();
        assertEquals(11, sparte.getByteAdresse());
        assertEquals(3, sparte.getAnzahlBytes());
        assertEquals("Sparte", sparte.getBezeichnung());
    }

}
