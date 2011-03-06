/*
 * Copyright (c) 2010 by agentes
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
 * (c)reated 26.01.2011 by Oli B. (oliver.boehm@agentes.de)
 */

package gdv.xport;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import org.junit.Test;
import org.junit.runner.RunWith;

import patterntesting.runtime.annotation.IntegrationTest;
import patterntesting.runtime.junit.SmokeRunner;

/**
 * JUnit-Test fuer die Main-Klasse.
 * 
 * @author oliver (oliver.boehm@agentes.de)
 * @since 0.5.1 (26.01.2011)
 */
@RunWith(SmokeRunner.class)
@IntegrationTest
public final class MainTest {

    /**
     * Test method for {@link gdv.xport.Main#main(java.lang.String[])}.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws XMLStreamException the xML stream exception
     */
    @Test
    public void testMain() throws IOException, XMLStreamException {
        String[] args = { "-import", "src/test/resources/musterdatei_041222.txt", "-validate" };
        Main.main(args);
    }

}
