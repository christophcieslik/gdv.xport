/*
 * Copyright (c) 2010 - 2014 by Oli B.
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
 * (c)reated 23.11.2010 by Oli B. (ob@aosd.de)
 */

package gdv.xport.util;

import gdv.xport.Datenpaket;
import gdv.xport.config.Config;
import gdv.xport.feld.Feld;
import gdv.xport.feld.Undefiniert;
import gdv.xport.satz.Datensatz;
import gdv.xport.satz.Nachsatz;
import gdv.xport.satz.Satz;
import gdv.xport.satz.Teildatensatz;
import gdv.xport.satz.Vorsatz;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Iterator;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.io.IOUtils;

/**
 * Diese Klasse gibt die verschiedenen Saetze und Felder als HTML aus.
 *
 * @author oliver (ob@aosd.de)
 * @since 0.5.0 (23.11.2010)
 */
public final class HtmlFormatter extends AbstractFormatter {

    private static final XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();
    private static final String TEMPLATE;
    private static final String HEAD;
    private static final String TAIL;

    private final XMLStreamWriter xmlWriter;
    private final StringWriter detailsWriter = new StringWriter();

    private String title = "GDV-Datei";
    private int zeile = 1;

    static {
        try {
            TEMPLATE = readTemplate("template.html");
            HEAD = readTemplate("head.html");
            TAIL = readTemplate("tail.html");
        } catch (IOException ioe) {
            throw new ExceptionInInitializerError(ioe);
        }
    }

    private static String readTemplate(final String name) throws IOException {
        InputStream istream = HtmlFormatter.class.getResourceAsStream(name);
        try {
            return IOUtils.toString(istream);
        } finally {
            istream.close();
        }
    }

    /**
     * Default-Konstruktor.
     */
    public HtmlFormatter() {
        this(System.out);
    }

    /**
     * Instantiiert einen neuen HtmlFormatter.
     *
     * @param file the file
     * @throws IOException Signals that an I/O exception has occurred.
     * @deprecated bitte {@link #HtmlFormatter(Writer)} verwenden, da sonst
     *             niemand da ist, der den internen Writer schliesst.
     */
    @Deprecated
    public HtmlFormatter(final File file) throws IOException {
        this(new FileWriter(file));
        this.title = "GDV-Datei " + file;
    }

    /**
     * Instantiiert einen neuen HtmlFormatter.
     *
     * @param ostream
     *            the ostream
     */
    public HtmlFormatter(final OutputStream ostream) {
        this(new OutputStreamWriter(ostream));
    }

    /**
     * Instantiiert einen neuen HtmlFormatter.
     *
     * @param writer
     *            the writer
     */
    public HtmlFormatter(final Writer writer) {
        super(writer);
        try {
            xmlWriter = xmlOutputFactory.createXMLStreamWriter(writer);
        } catch (XMLStreamException ex) {
            throw new IllegalArgumentException("cannot create XMLStreamWriter with " + writer, ex);
        }
    }

    /**
     * Hiermit kann der Titel (Ueberschrift) gesetzt werden.
     *
     * @param title
     *            Titel bzw. Ueberschrift
     */
    public void setTitle(final String title) {
        this.title = title;
    }

    /**
     * Ausgabe eines kompletten Datenpakets als XML.
     *
     * @param datenpaket
     *            Datenpaket, das als XML ausgegeben werden soll
     * @throws IOException
     *             bei Problemen mit der HTML-Generierung
     * @see AbstractFormatter#write(gdv.xport.Datenpaket)
     */
    @Override
    public void write(final Datenpaket datenpaket) throws IOException {
        long t0 = System.currentTimeMillis();
        StringWriter buffer = new StringWriter();
        try {
            XMLStreamWriter xmlStreamWriter = xmlOutputFactory.createXMLStreamWriter(buffer);
            //int zeile = 1;
            writeTo(xmlStreamWriter, datenpaket.getVorsatz(), zeile);
            zeile += datenpaket.getVorsatz().getTeildatensaetze().size();
            for (Iterator<Datensatz> iterator = datenpaket.getDatensaetze().iterator(); iterator.hasNext();) {
                Satz datensatz = iterator.next();
                writeTo(xmlStreamWriter, datensatz, zeile);
                zeile += datensatz.getTeildatensaetze().size();
            }
            writeTo(xmlStreamWriter, datenpaket.getNachsatz(), zeile);
            xmlStreamWriter.close();
            buffer.close();
            String content = MessageFormat.format(TEMPLATE, Config.DEFAULT_ENCODING.name(), title, buffer.toString(),
                    getDetails(datenpaket));
            super.write(content);
            super.write("<!-- (c)reated by gdv-xport in " + (System.currentTimeMillis() - t0) + " ms -->\n");
            super.getWriter().flush();
        } catch (XMLStreamException e) {
            throw new IOException("XML-Fehler", e);
        }
    }

    /**
     * HTML-Ausgabe eines einzelnen Satzes.
     *
     * @param satz der Satz, der als HTML ausgegeben werden soll
     * @see gdv.xport.util.AbstractFormatter#write(gdv.xport.satz.Satz)
     */
    @Override
    public void write(final Satz satz) {
        try {
            if (satz instanceof Vorsatz) {
                this.write((Vorsatz) satz);
            } else if (satz instanceof Nachsatz) {
                this.write((Nachsatz) satz);
            } else {
                this.writeSatz(satz);
            }
        } catch (XMLStreamException ex) {
            throw new FormatterException("cannot format " + satz, ex);
        } catch (IOException ioe) {
            throw new FormatterException("cannot write " + satz, ioe);
        }
    }

    private void write(final Vorsatz vorsatz) throws XMLStreamException, IOException {
        String head = MessageFormat.format(HEAD, Config.DEFAULT_ENCODING.name(), title);
        this.write(head);
        this.writeSatz(vorsatz);
    }

    private void write(final Nachsatz nachsatz) throws XMLStreamException, IOException {
        this.writeSatz(nachsatz);
        this.detailsWriter.close();
        String tail = MessageFormat.format(TAIL, this.detailsWriter.toString());
        this.write(tail);
        super.write("<!-- (c)reated by gdv-xport at " + new Date() + " -->\n");
    }

    private void writeSatz(final Satz satz) throws XMLStreamException {
        writeTo(this.xmlWriter, satz, zeile);
        writeDetailsTo(this.detailsWriter, satz, zeile);
        zeile += satz.getTeildatensaetze().size();
    }

    private static String getDetails(final Datenpaket datenpaket) throws XMLStreamException, IOException {
        StringWriter buffer = new StringWriter();
        XMLStreamWriter xmlStreamWriter = xmlOutputFactory.createXMLStreamWriter(buffer);
        int zeile = 1;
        writeDetailsTo(xmlStreamWriter, datenpaket.getVorsatz(), zeile);
        zeile += datenpaket.getVorsatz().getTeildatensaetze().size();
        for (Iterator<Datensatz> iterator = datenpaket.getDatensaetze().iterator(); iterator.hasNext();) {
            Satz datensatz = iterator.next();
            writeDetailsTo(xmlStreamWriter, datensatz, zeile);
            zeile += datensatz.getTeildatensaetze().size();
        }
        writeDetailsTo(xmlStreamWriter, datenpaket.getNachsatz(), zeile);
        xmlStreamWriter.close();
        buffer.close();
        return buffer.toString();
    }

    private static void writeTo(final XMLStreamWriter xmlStreamWriter, final Satz satz, final int zeile)
            throws XMLStreamException {
        xmlStreamWriter.writeStartElement("div");
        xmlStreamWriter.writeAttribute("class", "Satz");
        if (satz instanceof Datensatz) {
            Datensatz datensatz = (Datensatz) satz;
            xmlStreamWriter.writeAttribute("title", "Satzart " + datensatz.getSatzartFeld().getInhalt() + "."
                    + datensatz.getSparteFeld().getInhalt());
        } else {
            xmlStreamWriter.writeAttribute("title", "Satzart " + satz.getSatzartFeld().getInhalt());
        }
        int n = zeile;
        for (Iterator<Teildatensatz> iterator = satz.getTeildatensaetze().iterator(); iterator.hasNext();) {
            Teildatensatz teildatensatz = iterator.next();
            writeTo(xmlStreamWriter, teildatensatz, n);
            if (iterator.hasNext()) {
                xmlStreamWriter.writeCharacters("\n");
            }
            n++;
        }
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.flush();
    }

    private static void writeDetailsTo(final Writer writer, final Satz satz, final int zeile) throws XMLStreamException {
        XMLStreamWriter xmlStreamWriter = xmlOutputFactory.createXMLStreamWriter(writer);
        writeDetailsTo(xmlStreamWriter, satz, zeile);
    }

    private static void writeDetailsTo(final XMLStreamWriter xmlStreamWriter, final Satz satz, final int zeile)
            throws XMLStreamException {
        xmlStreamWriter.writeStartElement("h3");
        xmlStreamWriter.writeCharacters("Satzart " + satz.getSatzart() + " (" + satz.getClass().getSimpleName() + ")");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        int n = zeile;
        for (Iterator<Teildatensatz> iterator = satz.getTeildatensaetze().iterator(); iterator.hasNext();) {
            Teildatensatz teildatensatz = iterator.next();
            writeDetailsTo(xmlStreamWriter, teildatensatz, n);
            n++;
        }
        xmlStreamWriter.flush();
    }

    private static void writeTo(final XMLStreamWriter xmlStreamWriter, final Teildatensatz teildatensatz,
            final int zeile) throws XMLStreamException {
        xmlStreamWriter.writeStartElement("span");
        xmlStreamWriter.writeAttribute("class", "Teildatensatz");
        xmlStreamWriter.writeAttribute("title", "Nr. " + teildatensatz.getNummer().getInhalt());
        int endAdresse = 1;
        for (Iterator<Feld> iterator = teildatensatz.getFelder().iterator(); iterator.hasNext();) {
            Feld feld = iterator.next();
            int gap = feld.getByteAdresse() - endAdresse;
            if (gap > 1) {
                Feld undefiniert = new Undefiniert(gap - 1, endAdresse + 1);
                writeTo(xmlStreamWriter, undefiniert, zeile);
            }
            writeTo(xmlStreamWriter, feld, zeile);
            endAdresse = feld.getEndAdresse();
        }
        if (endAdresse < 256) {
            Feld undefiniert = new Undefiniert(256 - endAdresse, endAdresse + 1);
            writeTo(xmlStreamWriter, undefiniert, zeile);
        }
        xmlStreamWriter.writeEndElement();
    }

    private static void writeDetailsTo(final XMLStreamWriter xmlStreamWriter, final Teildatensatz teildatensatz,
            final int zeile) throws XMLStreamException {
        xmlStreamWriter.writeStartElement("h4");
        xmlStreamWriter.writeCharacters("Zeile " + zeile + ": Teildatensatz " + teildatensatz.getNummer().getInhalt());
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeStartElement("table");
        xmlStreamWriter.writeStartElement("thead");
        xmlStreamWriter.writeStartElement("tr");
        xmlStreamWriter.writeStartElement("th");
        xmlStreamWriter.writeCharacters("Nr");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeStartElement("th");
        xmlStreamWriter.writeCharacters("Byte");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeStartElement("th");
        xmlStreamWriter.writeCharacters("Bezeichner");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeStartElement("th");
        xmlStreamWriter.writeCharacters("Datentyp");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeStartElement("th");
        xmlStreamWriter.writeCharacters("Inhalt");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeStartElement("tbody");
        int nr = 1;
        for (Iterator<Feld> iterator = teildatensatz.getFelder().iterator(); iterator.hasNext();) {
            Feld feld = iterator.next();
            writeDetailsTo(xmlStreamWriter, feld, zeile, nr);
            nr++;
        }
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.flush();
    }

    private static void writeTo(final XMLStreamWriter xmlStreamWriter, final Feld feld, final int zeile)
            throws XMLStreamException {
        String feldType = feld.getClass().getSimpleName();
        xmlStreamWriter.writeStartElement("a");
        xmlStreamWriter.writeAttribute("class", feldType);
        xmlStreamWriter.writeAttribute("title", "Byte " + feld.getByteAdresse() + "-" + feld.getEndAdresse() + ": "
                + feld.getBezeichnung());
        xmlStreamWriter.writeAttribute("href", "#" + getAnchorFor(zeile, feld));
        writeInhaltTo(xmlStreamWriter, feld);
        xmlStreamWriter.writeEndElement();
    }

    private static void writeDetailsTo(final XMLStreamWriter xmlStreamWriter, final Feld feld, final int zeile,
            final int nr) throws XMLStreamException {
        String typ = feld.getClass().getSimpleName();
        xmlStreamWriter.writeStartElement("tr");
        xmlStreamWriter.writeAttribute("class", typ);
        xmlStreamWriter.writeStartElement("td");
        xmlStreamWriter.writeCharacters(Integer.toString(nr));
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeStartElement("td");
        xmlStreamWriter.writeStartElement("a");
        xmlStreamWriter.writeAttribute("name", getAnchorFor(zeile, feld));
        xmlStreamWriter.writeEndElement();
        if (feld.getAnzahlBytes() == 1) {
            xmlStreamWriter.writeCharacters(Integer.toString(feld.getByteAdresse()));
        } else {
            xmlStreamWriter.writeCharacters(feld.getByteAdresse() + " - " + feld.getEndAdresse());
        }
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeStartElement("td");
        xmlStreamWriter.writeCharacters(feld.getBezeichnung());
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeStartElement("td");
        xmlStreamWriter.writeCharacters(typ);
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeStartElement("td");
        writeInhaltTo(xmlStreamWriter, feld);
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
    }

    /**
     * Urspruenglich war diese Methode dazu gedacht, um Umlaute zu ersetzen. Das "Escaping" wird aber bereits vom
     * XMLStreamWriter uebernommen, der aber leider die Umlaute nicht ersetzt. Der Versuch, die Umlaute zu ersetzen,
     * endete leider mit "...&amp;Uuml;..." im erzeugten HTML.
     *
     * @param xmlStreamWriter
     *            the xml stream writer
     * @param feld
     *            the feld
     * @throws XMLStreamException
     *             the xML stream exception
     */
    private static void writeInhaltTo(final XMLStreamWriter xmlStreamWriter, final Feld feld)
            throws XMLStreamException {
        String inhalt = feld.getInhalt();
        xmlStreamWriter.writeCharacters(inhalt);
    }

    private static String getAnchorFor(final int zeile, final Feld feld) {
        return "z" + zeile + "b" + feld.getByteAdresse();
    }

    /**
     * Wandelt das uebergebene Datenpaket in einen HTML-String um.
     *
     * @param datenpaket
     *            das Datenpaket
     * @return Datenpaket als XML-String
     */
    public static String toString(final Datenpaket datenpaket) {
        StringWriter swriter = new StringWriter();
        HtmlFormatter formatter = new HtmlFormatter(swriter);
        try {
            formatter.write(datenpaket);
        } catch (IOException shouldnothappen) {
            throw new RuntimeException("can't convert " + datenpaket + " to String", shouldnothappen);
        }
        IOUtils.closeQuietly(swriter);
        return swriter.toString();
    }

}
