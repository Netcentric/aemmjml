/*
 * (C) Copyright 2019 Netcentric, a Cognizant Digital Business.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package biz.netcentric.cq.tools.aemmjml.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.junit.jupiter.api.Test;

public class PatternDetectingReaderTest {

    @Test
    public void testNotFoundSingleBytes() throws IOException {
        Reader data = new StringReader("aabbcc");
        PatternDetectingReader in = new PatternDetectingReader(data, "abc");
        assertNotEquals(-1, in.read());
        assertNotEquals(-1, in.read());
        assertNotEquals(-1, in.read());
        assertNotEquals(-1, in.read());
        assertNotEquals(-1, in.read());
        assertNotEquals(-1, in.read());
        assertEquals(-1, in.read());
        assertThrows(IOException.class, in::close);
    }

    @Test
    public void testFoundSingleBytes() throws IOException {
        Reader data = new StringReader("aabbcc");
        PatternDetectingReader in = new PatternDetectingReader(data, "abbc");
        assertNotEquals(-1, in.read());
        assertNotEquals(-1, in.read());
        assertNotEquals(-1, in.read());
        assertNotEquals(-1, in.read());
        assertNotEquals(-1, in.read()); // closed after first c
        assertEquals(-1, in.read());
        assertEquals(-1, in.read());

        in.close(); // should not throw
    }

    @Test
    public void testNotFoundMultiBytes() throws IOException {
        char[] buffer = new char[5];
        Reader data = new StringReader("aabbccdd");
        PatternDetectingReader in = new PatternDetectingReader(data, "abc");
        assertEquals(3, in.read(buffer, 1, 3));
        assertEquals(3, in.read(buffer, 1, 3));
        assertEquals(2, in.read(buffer, 1, 3));
        assertEquals(-1, in.read(buffer, 1, 3));
        assertThrows(IOException.class, in::close);
    }

    @Test
    public void testFoundMultiBytes() throws IOException {
        char[] buffer = new char[5];
        Reader data = new StringReader("aabbccdd");
        PatternDetectingReader in = new PatternDetectingReader(data, "bbccd");
        assertEquals(3, in.read(buffer, 1, 3));
        assertEquals(3, in.read(buffer, 1, 3));
        assertEquals(1, in.read(buffer, 1, 3)); // close after first d
        assertEquals(-1, in.read(buffer, 1, 3));

        in.close(); // should not throw
    }
}
