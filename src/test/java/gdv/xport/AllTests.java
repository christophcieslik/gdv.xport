/*
 * Copyright (c) 2012 by Oli B.
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
 * (c)reated 11.01.2012 by Oli B. (boehm@javatux.de)
 */

package gdv.xport;

import gdv.xport.config.ConfigTest;
import gdv.xport.demo.MyUnfallDatensatzTest;
import gdv.xport.feld.FeldTests;
import gdv.xport.io.ImportExceptionTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

import patterntesting.concurrent.junit.ParallelSuite;

/**
 * Die Klasse AllTests ist eine Test-Suite fuer JUnit 4, mit der alle
 * JUnit-Tests parallel getestet werden.
 * 
 * @author oliver (boehm@javatux.de)
 * @since 0.7 (11.01.2012)
 */
@RunWith(ParallelSuite.class)
@SuiteClasses({ ConfigTest.class, MyUnfallDatensatzTest.class, FeldTests.class, ImportExceptionTest.class,
        BasisTest.class, DatenpaketTest.class, MainTest.class })
public class AllTests {

}
