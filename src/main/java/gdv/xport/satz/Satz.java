/**
 *
 */
package gdv.xport.satz;

import static gdv.xport.feld.Bezeichner.SATZART;
import static patterntesting.runtime.NullConstants.NULL_STRING;
import gdv.xport.config.Config;
import gdv.xport.feld.*;

import java.io.*;
import java.util.*;

import net.sf.oval.*;
import net.sf.oval.constraint.AssertCheck;
import net.sf.oval.context.ClassContext;

import org.apache.commons.logging.*;

/**
 * Die Satz-Klasse ist die oberste Klasse, von der alle weiteren Saetze
 * abgeleitet sind.
 *
 * @author oliver
 */
public class Satz {

    private static final Log log = LogFactory.getLog(Satz.class);
    /** 4 Zeichen */
    protected final NumFeld satzart = new NumFeld(SATZART, 4, 1);
    /** Teildatensaetze */
    protected Teildatensatz[] teildatensatz;

    protected Satz(final int art) {
        this(art, 1);
    }

    protected Satz(final String art) {
        this(art, (art.length() + 255) / 256);
    }

    protected Satz(final NumFeld art) {
        this(art.getInhalt());
    }

    /**
     * Instantiates a new satz.
     *
     * @param art the art
     * @param n the n
     */
    public Satz(final NumFeld art, final int n) {
        this.satzart.setInhalt(art.getInhalt());
        this.createTeildatensaetze(n);
    }

    /**
     * Instantiates a new satz.
     *
     * @param content the content
     * @param n the n
     */
    public Satz(final String content, final int n) {
        this.satzart.setInhalt(content.substring(0, 4));
        this.createTeildatensaetze(n);
        if (content.length() > 4) {
            try {
                this.importFrom(content);
            } catch (IOException ioe) {
                throw new IllegalArgumentException("1st argument too short", ioe);
            }
        }
    }

    /**
     * The Constructor.
     *
     * @param art z.B. 100 (f. Adressteil)
     * @param n Anzahl der Teildatensaetze
     */
    public Satz(final int art, final int n) {
        this.satzart.setInhalt(art);
        this.createTeildatensaetze(n);
    }

    protected void createTeildatensaetze(final int n) {
        teildatensatz = new Teildatensatz[n];
        for (int i = 0; i < n; i++) {
            teildatensatz[i] = new Teildatensatz(satzart, i+1);
        }
    }

    /**
     * Liefert alle Teildatensaetze zurueck.
     *
     * @return Teildatensaetze
     *
     * @since 0.2
     */
    public Collection<Teildatensatz> getTeildatensaetze() {
        return Arrays.asList(this.teildatensatz);
    }

    /**
     * Liefert den n-ten Teildatensatz zurueck.
     *
     * @param n Nummer des Teildatensatzes (beginnend mit 1)
     *
     * @return the teildatensatz
     *
     * @since 0.2
     */
    public Teildatensatz getTeildatensatz(final int n) {
        return this.teildatensatz[n-1];
    }

    /**
     * Fuegt das uebergebene Feld zur Liste der Datenfelder hinzu.
     *
     * @param feld the feld
     */
    public void add(final Feld feld) {
        this.add(feld, 1);
    }

    /**
     * Adds the.
     *
     * @param feld the feld
     * @param teildatensatzNr the teildatensatz nr
     */
    public void add(final Feld feld, final int teildatensatzNr) {
        if (feld.getByteAdresse() > 256) {
            throw new IllegalArgumentException(feld + " ueberschreitet Teildatensatz-Grenze");
        }
        this.teildatensatz[teildatensatzNr-1].add(feld);
    }

    /**
     * Fuellt fuer alle leeren Stellen ein entsprechendes Fuellfeld auf.
     */
    public void addFiller() {
        throw new UnsupportedOperationException("not yet implemented");
    }

    /**
     * Setzt das angegebene Feld in allen Teildatensaetzen, in denen es
     * gefunden wird. Normalerweise braeuchten wir eigentlich nur die erste
     * Fundstelle setzen, da die anderen Teildatensaetze (hoffentlich) auf
     * die gleiche Referenz verweisen - aber sicher ist sicher.
     * Falls das Feld nicht gefunden wird, wird eine IllegalArgumentException
     * geworfen.
     *
     * @param name Name des Felds (Bezeichnung)
     * @param value the value
     */
    public void set(final String name, final String value) {
        boolean found = false;
        for (int i = 0; i < teildatensatz.length; i++) {
            Feld x = teildatensatz[i].getFeld(name);
            if (x != Feld.NULL_FELD) {
                x.setInhalt(value);
                found = true;
            }
        }
        if (!found) {
            throw new IllegalArgumentException("Feld \"" + name +"\" not found");
        }
    }

    /**
     * Liefert den Inhalt des gewuenschten Feldes.
     *
     * @param name gesuchtes Feld
     *
     * @return Inhalt des gefundenden Felds
     * (NULL_STRING, falls 'name' nicht gefunden wurde)
     */
    public String get(final String name) {
        Feld f = getFeld(name);
        if (f == Feld.NULL_FELD) {
            return NULL_STRING;
        } else {
            return f.getInhalt();
        }
    }

    /**
     * Liefert das gewuenschte Feld.
     *
     * @param name gewuenschter Bezeichner des Feldes
     *
     * @return NULL_FELD, falls das angegebene Feld nicht gefunden wird
     */
    public Feld getFeld(final String name) {
        for (int i = 0; i < teildatensatz.length; i++) {
            Feld x = teildatensatz[i].getFeld(name);
            if (x != Feld.NULL_FELD) {
                return x;
            }
        }
        return Feld.NULL_FELD;
    }

    /**
     * Liefert das gewuenschte Feld.
     *
     * @param name gewuenschter Bezeichner des Feldes
     * @param nr Nummer des Teildatensatzes (1, 2, ...)
     *
     * @return NULL_FELD, falls das angegebene Feld nicht gefunden wird
     *
     * @since 0.2
     */
    public Feld getFeld(final String name, int nr) {
        assert (0 < nr) &&  (nr <= teildatensatz.length) : nr + " liegt ausserhalb des Bereichs";
        return teildatensatz[nr-1].getFeld(name);
    }

    /**
     * Gets the satzart.
     *
     * @return the satzart
     */
    public NumFeld getSatzart() {
        return this.satzart;
    }

    /**
     * Export.
     *
     * @param writer the writer
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void export(final Writer writer) throws IOException {
        for (int i = 0; i < teildatensatz.length; i++) {
            teildatensatz[i].export(writer);
        }
    }

    /**
     * Eigentlich wollte ich ja diese Methode "import" nennen, aber das
     * kollidiert leider mit dem Schluesselwort "import" in Java.
     *
     * @param s the s
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void importFrom(final String s) throws IOException {
        for (int i = 0; i < teildatensatz.length; i++) {
            teildatensatz[i].importFrom(s.substring(i * 256));
        }
    }

    /**
     * Import from.
     *
     * @param istream the istream
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public final void importFrom(final InputStream istream) throws IOException {
        importFrom(new InputStreamReader(istream, Config.DEFAULT_ENCODING));
    }

    /**
     * Import from.
     *
     * @param reader the reader
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public final void importFrom(final Reader reader) throws IOException {
        importFrom(new PushbackReader(reader, 4));
    }

    /**
     * Import from.
     *
     * @param reader the reader
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public final void importFrom(final PushbackReader reader) throws IOException {
        char[] cbuf = new char[256];
        for (int i = 0; i < teildatensatz.length; i++) {
            int art = readSatzart(reader);
            if (art != this.getSatzart().toInt()) {
                log.info((teildatensatz.length - i) + " more Teildatensaetze expected for " + this
                        + ", but got data for Satzart " + art);
                break;
            }
            importFrom(reader, cbuf);
            teildatensatz[i].importFrom(new String(cbuf));
            skipNewline(reader);
        }
    }

    private static void importFrom(final Reader reader, final char[]cbuf) throws IOException {
        if (reader.read(cbuf) == -1) {
            String s = new String(cbuf);
            throw new IOException("can't read " + cbuf.length + " bytes from " + reader + ", only "
                    + s.length() + " bytes: " + s);
        }
    }

    /**
     * Liest 4 Bytes, um die Satzart zu bestimmen und stellt die Bytes
     * anschliessend wieder zurueck in den Reader.
     *
     * @param reader the reader
     *
     * @return Satzart
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static int readSatzart(final PushbackReader reader) throws IOException {
        char[] cbuf = new char[4];
        importFrom(reader, cbuf);
        reader.unread(cbuf);
        return Integer.parseInt(new String(cbuf));
    }

    private static void skipNewline(final PushbackReader reader) throws IOException {
        char[] cbuf = new char[1];
        do {
            if (reader.read(cbuf) == -1) {
                log.info("end of file detected");
                return;
            }
        } while ((cbuf[0] == '\n') || (cbuf[0] == '\r'));
        reader.unread(cbuf);
    }

    /**
     * Aus Performance-Gruenden stuetzt sich diese Methode nicht auf die
     * validate()-Methode ab.
     *
     * @return true/false
     */
    public boolean isValid() {
        if (!this.satzart.isValid()) {
            return false;
        }
        if (this.teildatensatz != null) {
            for (int i = 0; i < teildatensatz.length; i++) {
                if (!teildatensatz[i].isValid()) {
                    log.info("Teildatensatz " + (i+1) + " is invalid");
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Validiert die einzelnen Teildatensaetze.
     *
     * @return the list< constraint violation>
     */
    public List<ConstraintViolation> validate() {
        Validator validator = new Validator();
        List<ConstraintViolation> violations = validator.validate(this);
        if (!this.satzart.isValid()) {
            ConstraintViolation cv = new ConstraintViolation(new AssertCheck(), "invalid Satzart "
                    + this.satzart.getInhalt(), this, this.satzart, new ClassContext(this
                    .getClass()));
            violations.add(cv);
        }
        if (this.teildatensatz != null) {
            for (int i = 0; i < teildatensatz.length; i++) {
                violations.addAll(teildatensatz[i].validate());
            }
        }
        return violations;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public final String toString() {
        try {
            return this.toShortString();
        } catch (RuntimeException shouldNeverHappen) {
            log.error("shit happens in toString()", shouldNeverHappen);
            return super.toString();
        }
    }

    /**
     * To short string.
     *
     * @return the string
     */
    public String toShortString() {
        return "Satzart " + this.satzart.getInhalt() + " ("
                + this.toLongString().substring(0, 60) + "...)";
    }

    /**
     * To long string.
     *
     * @return the string
     */
    public String toLongString() {
        StringWriter swriter = new StringWriter();
        try {
            this.export(swriter);
        } catch (IOException canthappen) {
            log.warn(canthappen + " ignored", canthappen);
            swriter.write(canthappen.getLocalizedMessage());
        }
        return swriter.toString();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object other) {
        try {
            return this.equals((Satz) other);
        } catch (ClassCastException cce) {
            return false;
        }
    }

    /**
     * Equals.
     *
     * @param other the other
     *
     * @return true, if successful
     */
    public final boolean equals(final Satz other) {
        if (this.getSatzart().toInt() != other.getSatzart().toInt()) {
            return false;
        }
        for (int i = 0; i < teildatensatz.length; i++) {
            if (!this.teildatensatz[i].equals(other.teildatensatz[i])) {
                return false;
            }
        }
        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return this.toLongString().hashCode();
    }

}
