/*
 * Copyright (c) 2009 - 2012 by Oli B.
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
 * (c)reated 12.10.2009 by Oli B. (ob@aosd.de)
 */

package gdv.xport.satz;

import static gdv.xport.feld.Bezeichner.BUENDELUNGSKENNZEICHEN;
import static gdv.xport.feld.Bezeichner.FOLGENUMMER;
import static gdv.xport.feld.Bezeichner.LEERSTELLEN;
import static gdv.xport.feld.Bezeichner.SPARTE;
import static gdv.xport.feld.Bezeichner.VERMITTLER;
import static gdv.xport.feld.Bezeichner.VERSICHERUNGSSCHEINNUMMER;
import gdv.xport.config.Config;
import gdv.xport.feld.AlphaNumFeld;
import gdv.xport.feld.Bezeichner;
import gdv.xport.feld.NumFeld;
import gdv.xport.feld.VUNummer;

import java.io.IOException;
import java.io.PushbackReader;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The Class Datensatz.
 *
 * @author oliver
 * @since 12.10.2009
 */
public class Datensatz extends Satz {

    private static Log log = LogFactory.getLog(Datensatz.class);
    /** 5 Zeichen, Byte 5 - 9. */
    private final VUNummer vuNummer = new VUNummer(Config.getVUNummer(), 5);
    /** 1 Zeichen, Byte 10. */
    private final AlphaNumFeld buendelungsKennzeichen = new AlphaNumFeld(BUENDELUNGSKENNZEICHEN, 1, 10);
    /** 3 Zeichen, Byte 11 - 13. */
    private final NumFeld sparte = new NumFeld(SPARTE, 3, 11);
    /** 17 Zeichen, Byte 14 - 30. */
    private final AlphaNumFeld versicherungsscheinNr = new AlphaNumFeld(VERSICHERUNGSSCHEINNUMMER, 17, 14);
    /** 2 Zeichen, Byte 31 + 32. */
    private final NumFeld folgeNr = new NumFeld(FOLGENUMMER, 2, 31);
    /** 10 Zeichen, Byte 33 - 42. */
    private final AlphaNumFeld vermittler = new AlphaNumFeld(VERMITTLER, 10, 33);

    /**
     * Default-Konstruktor (wird zur Registrierung bei der {@link gdv.xport.util.SatzFactory} benoetigt).
     *
     * @since 0.6
     */
    public Datensatz() {
        super(0);
    }

    /**
     * Instantiiert einen neuen Datensatz.
     *
     * @param satzart
     *            z.B. "0100"
     */
    public Datensatz(final String satzart) {
        super(satzart);
        this.setUpTeildatensaetze();
    }

    /**
     * Instantiiert einen neuen Datensatz.
     *
     * @param satzart
     *            z.B. 100
     */
    public Datensatz(final int satzart) {
        super(satzart, 1);
        this.setUpTeildatensaetze();
    }

    /**
     * Instantiiert einen neuen Datensatz.
     *
     * @param satzart
     *            z.B. 100
     * @param n
     *            Anzahl der Teildatensaetze
     */
    public Datensatz(final String satzart, final int n) {
        super(satzart, n);
        this.setUpTeildatensaetze();
    }

    /**
     * Instantiiert einen neuen Datensatz.
     *
     * @param satzart
     *            z.B. 100
     * @param tdsList
     *            Liste mit den Teildatensaetzen
     */
    public Datensatz(final int satzart, final List<Teildatensatz> tdsList) {
        super(satzart, tdsList);
        this.completeTeildatensaetze();
    }

    /**
     * Instantiiert einen neuen Datensatz.
     *
     * @param satzart
     *            z.B. 100
     * @param sparte
     *            z.B. 70 (Rechtsschutz)
     */
    public Datensatz(final int satzart, final int sparte) {
        this(satzart, sparte, 1);
    }

    /**
     * Instantiiert einen neuen Datensatz.
     *
     * @param satzart
     *            z.B. 100
     * @param sparte
     *            z.B. 70 (Rechtsschutz)
     * @param n
     *            Anzahl der Teildatensaetze
     */
    public Datensatz(final int satzart, final int sparte, final int n) {
        super(satzart, n);
        this.setSparte(sparte);
        this.setUpTeildatensaetze();
    }

    /**
     * Instantiiert einen neuen Datensatz.
     *
     * @param satzart
     *            z.B. 100
     * @param sparte
     *            z.B. 70 (Rechtsschutz)
     * @param tdsList
     *            Liste mit den Teildatensaetzen
     */
    public Datensatz(final int satzart, final int sparte, final List<Teildatensatz> tdsList) {
        this(satzart, tdsList);
        this.setSparte(sparte);
        this.completeTeildatensaetze();
    }

    /**
     * Kann von Unterklassen verwendet werden, um die Teildatensaetze aufzusetzen.
     */
    protected void setUpTeildatensaetze() {
        for (Teildatensatz tds : this.getTeildatensaetze()) {
            setUpTeildatensatz(tds);
        }
    }

    /**
     * Hiermit kann ein einzelner Teildatensatz aufgesetzt werden.
     * <p>
     * Wenn alle Datensaetze nur noch ueber Enums (Soplets) initialisiert
     * werden, duerfte die Inialisierung hier hinfaellig sein.
     * </p>
     *
     * @param tds
     *            der (leere) Teildatensatz
     * @since 0.4
     */
    protected void setUpTeildatensatz(final Teildatensatz tds) {
        if (!tds.hasFeld(Bezeichner.VU_NUMMER)) {
            log.debug("initializing " + tds + "...");
            tds.add(this.vuNummer);
            tds.add(this.buendelungsKennzeichen);
            tds.add(this.sparte);
            tds.add(this.versicherungsscheinNr);
            tds.add(this.folgeNr);
            tds.add(this.vermittler);
        }
    }

    /**
     * Kann von Unterklassen verwendet werden, um fehlende Felder in den Teildatensaetze zu vervollstaendigen.
     *
     * @since 0.6
     */
    protected void completeTeildatensaetze() {
        for (Teildatensatz tds : this.getTeildatensaetze()) {
            setUpTeildatensatz(tds);
        }
    }

    /**
     * Hiermit kann ein einzelner Teildatensatz aufgesetzt werden.
     *
     * @param n
     *            Nummer des Teildatensatzes (beginnend bei 1)
     * @since 0.5
     */
    protected void setUpTeildatensatz(final int n) {
        this.setUpTeildatensatz(this.getTeildatensatz(n));
    }

    /*
     * (non-Javadoc)
     *
     * @see gdv.xport.satz.Satz#addFiller()
     */
    @Override
    public void addFiller() {
        for (Teildatensatz tds : this.getTeildatensaetze()) {
            tds.add(new AlphaNumFeld(LEERSTELLEN, 213, 43));
        }
    }

    /**
     * Sets the sparte.
     *
     * @param x
     *            z.B. 70 (Rechtsschutz)
     */
    public void setSparte(final int x) {
        this.sparte.setInhalt(x);
    }

    /**
     * Gets the sparte.
     *
     * @return die Sparte als int
     */
    public int getSparte() {
        return this.sparte.toInt();
    }

    /**
     * Ueberprueft, ob der Datensatz ueberhaupt eine Sparte gesetzt hat.
     *
     * @return true, if successful
     * @since 0.6
     */
    public boolean hasSparte() {
        if (this.sparte.isEmpty()) {
            return false;
        }
        return this.getSparte() > 0;
    }

    /**
     * Gets the sparte feld.
     *
     * @return die Sparte als Feld
     */
    public NumFeld getSparteFeld() {
        return this.sparte;
    }

    /**
     * Sets the vu nummer.
     *
     * @param s
     *            VU-Nummer (max. 5 Stellen)
     */
    public void setVuNummer(final String s) {
        this.vuNummer.setInhalt(s);
    }

    /**
     * Gets the vu nummer.
     *
     * @return die VU-Nummer
     */
    public String getVuNummer() {
        return this.vuNummer.getInhalt().trim();
    }

    /**
     * Sets the versicherungsschein nummer.
     *
     * @param nr
     *            die Versicherungsschein-Nummer
     * @since 0.3
     */
    public void setVersicherungsscheinNummer(final String nr) {
        this.versicherungsscheinNr.setInhalt(nr);
    }

    /**
     * Gets the versicherungsschein nummer.
     *
     * @return die Versicherungsschein-Nummer
     * @since 0.3
     */
    public String getVersicherungsscheinNummer() {
        return this.versicherungsscheinNr.getInhalt().trim();
    }

    /**
     * Hiermit kann die Folgenummer gesetzt werden.
     *
     * @param nr
     *            man sollte hier bei 1 anfangen mit zaehlen
     * @since 0.3
     */
    public void setFolgenummer(final int nr) {
        this.folgeNr.setInhalt(nr);
    }

    /**
     * Gets the folgenummer.
     *
     * @return die Folgenummer
     * @since 0.3
     */
    public int getFolgenummer() {
        return this.folgeNr.toInt();
    }

    /**
     * Gets the vermittler.
     *
     * @return the vermittler
     * @since 0.6
     */
    public String getVermittler() {
        return this.getFeldInhalt(VERMITTLER);
    }

    /**
     * Liest 14 Bytes, um die Satzart zu bestimmen und stellt die Bytes anschliessend wieder zurueck in den Reader.
     *
     * @param reader
     *            muss mind. einen Pushback-Puffer von 14 Zeichen bereitstellen
     * @return Satzart
     * @throws IOException
     *             falls was schief gegangen ist
     */
    public static int readSparte(final PushbackReader reader) throws IOException {
        char[] cbuf = new char[14];
        if (reader.read(cbuf) == -1) {
            throw new IOException("can't read 14 bytes (" + new String(cbuf) + ") from " + reader);
        }
        reader.unread(cbuf);
        return Integer.parseInt(new String(cbuf).substring(10, 13));
    }

    /**
     * Liest 1 Byte, um die Wagnisart zu bestimmen und stellt das Byte anschliessend wieder zurueck in den Reader.
     *
     * @param reader
     *            muss mind. einen Pushback-Puffer von 1 Zeichen bereitstellen
     * @return Wagnisart
     * @throws IOException
     *             falls was schief gegangen ist
     */
    public static int readWagnisart(final PushbackReader reader) throws IOException {
        char[] cbuf = new char[60];
        if (reader.read(cbuf) == -1) {
            throw new IOException("can't read 1 bytes (" + new String(cbuf) + ") from " + reader);
        }
        reader.unread(cbuf);
        return Integer.parseInt(new String(cbuf).substring(59, 60));
    }

    /**
     * Unterklassen (wie Datensatz) sind dafuer verantwortlich, dass auch noch die Sparte ueberprueft wird, ob sie noch
     * richtig ist oder ob da schon der naechste Satz beginnt.
     *
     * @param reader
     *            the reader
     * @return true (Default-Implementierung)
     * @throws IOException
     *             bei I/O-Fehlern
     * @since 0.5.1
     * @see Satz#matchesNextTeildatensatz(PushbackReader)
     */
    @Override
    protected boolean matchesNextTeildatensatz(final PushbackReader reader) throws IOException {
        if (super.matchesNextTeildatensatz(reader)) {
            int sparteRead = readSparte(reader);
            return sparteRead == this.getSparte();
        }
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see gdv.xport.satz.Satz#toShortString()
     */
    @Override
    public String toShortString() {
        return super.toShortString() + "." + this.sparte.getInhalt();
    }

}
