/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://izpack.codehaus.org/
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
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

package com.izforge.izpack;

import junit.framework.TestCase;

public class LocaleDatabaseTest extends TestCase
{

    LocaleDatabase _db = null;

    @Override
    public void setUp() throws Exception
    {
        _db = new LocaleDatabase(LocaleDatabaseTest.class
                .getResourceAsStream("testing-langpack.xml"));

    }

    public void testGetString() throws Exception
    {

        TestCase.assertEquals("String Text", _db.getString("string"));
        TestCase.assertEquals("none", _db.getString("none"));
    }

    public void testNpeHandling()
    {
        TestCase.assertEquals("Argument1: one, Argument2: N/A", _db.getString(
                "string.with.arguments", new String[]{"one", null}));
    }

    public void testQuotedPlaceholder()
    {
        TestCase.assertEquals("Argument1: 'one', Argument2: 'N/A'", _db.getString(
                "string.with.quoted.arguments", new String[]{"one", null}));
    }

}
