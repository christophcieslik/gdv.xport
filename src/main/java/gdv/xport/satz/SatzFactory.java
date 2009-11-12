/*
 * Copyright (c) 2009 by agentes
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
 * (c)reated 30.10.2009 by Oli B. (oliver.boehm@agentes.de)
 */

package gdv.xport.satz;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.*;

import org.apache.commons.logging.*;

/**
 * Diese Klasse dient dazu, um einen vorgegebene Satz, der z.B. aus einem
 * Import kommt, in den entsprechende Satz wandeln zu koennen.
 *
 * @author oliver (oliver.boehm@agentes.de)
 * @since 0.1.0 (30.10.2009)
 */
public class SatzFactory {

    private static final Log log = LogFactory.getLog(SatzFactory.class);
    private static Map<Integer, Class<? extends Satz>> registeredSatzClasses = new HashMap<Integer, Class<? extends Satz>>();
    private static Map<Integer, Class<? extends Datensatz>> registeredDatensatzClasses = new HashMap<Integer, Class<? extends Datensatz>>();
    
    static {
        registeredSatzClasses.put(1, Vorsatz.class);
        registeredSatzClasses.put(9999, Nachsatz.class);
    }
    
    /**
     * Mit dieser Methode koennen eigene Klassen fuer (z.B. noch nicht
     * unterstuetzte Datensaetze) registriert werden.
     * 
     * @param clazz
     * @param satzart
     * @since 0.2
     */
    public static void register(Class<? extends Satz> clazz, int satzart) {
        registeredSatzClasses.put(satzart, clazz);
    }
    
    /**
     * Mit dieser Methode koennen eigene Klassen fuer (z.B. noch nicht
     * unterstuetzte Datensaetze) registriert werden.
     * 
     * @param clazz
     * @param satzart
     * @param sparte
     * @since 0.2
     */
    public static void register(Class<? extends Datensatz> clazz, int satzart, int sparte) {
        assert (0 <= satzart) && (satzart <= 9999) : "Satzart muss zwischen 0 und 9999 liegen";
        assert (0 <= sparte) && (sparte <= 999)    : "Sparte muss zwischen 0 und 999 liegen";
        int key = getAsKey(satzart, sparte);
        registeredDatensatzClasses.put(key, clazz);
    }
    
    private static int getAsKey(int satzart, int sparte) {
        return satzart * 1000 + sparte;
    }
    
    /**
     * @param satzart
     * @return angeforderte Satz
     * @since 0.2
     */
    public static Satz getSatz(int satzart) {
        Class<? extends Satz> clazz = registeredSatzClasses.get(satzart);
        if (clazz == null) {
            throw new IllegalArgumentException("unregistered Satzart " + satzart);
        }
        try {
            return clazz.newInstance();
        } catch (Exception e) {
            try {
                log.info("default ctor does not work (" + e +"), trying another ctor...");
                Constructor<? extends Satz> ctor = clazz.getConstructor(int.class);
                return ctor.newInstance(satzart);
            } catch (Exception ce) {
                throw new RuntimeException("constructor problem with " + clazz, ce);
            }
        }
    }

    public static Satz getSatz(String content) {
        int satzart = Integer.parseInt(content.substring(0, 4));
        Satz satz;
        try {
            satz = getSatz(satzart);
        } catch (RuntimeException e) {
            log.debug("can't get Satz " + satzart + " (" + e + "), parsing Sparte...");
            int sparte = Integer.parseInt(content.substring(10, 13));
            satz = getDatensatz(satzart, sparte);
        }    
        try {
            satz.importFrom(content);
            return satz;
        } catch (IOException ioe) {
            throw new IllegalArgumentException("can't parse " + content, ioe);
        }
    }

    /**
     * @param satzart
     * @param content
     * @return
     */
    public static Datensatz getDatensatz(int satzart, int sparte) {
        switch (satzart) {
            case 100:
                return new Adressteil();
            case 200:
                return new AllgemeinerVertragsteil();
            case 210:
                return new VertragsspezifischerTeil(sparte);
            case 220:
                return new SpartenspezifischerTeil(sparte);
        }
        int key = getAsKey(satzart, sparte);
        Class<? extends Datensatz> clazz = registeredDatensatzClasses.get(key);
        if (clazz == null) {
            log.warn("reduced functionality for (unknown or unsupported) Satzart " + satzart);
            Datensatz satz = new Datensatz(satzart, sparte);
            satz.addFiller();
            return satz;
        }
        try {
            return clazz.newInstance();
        } catch (Exception e) {
            try {
                log.info("default ctor does not work (" + e + "), trying another ctor...");
                Constructor<? extends Datensatz> ctor = clazz.getConstructor(int.class, int.class);
                return ctor.newInstance(satzart, sparte);
            } catch (Exception exWithtwoParams) {
                try {
                    log.info("default ctor does not work (" + exWithtwoParams
                            + "), trying another ctor...");
                    Constructor<? extends Datensatz> ctor = clazz.getConstructor(int.class);
                    return ctor.newInstance(satzart);
                } catch (Exception exWithOneParam) {
                    throw new RuntimeException("constructor problem with " + clazz, exWithOneParam);
                }
            }
        }
    }

}

