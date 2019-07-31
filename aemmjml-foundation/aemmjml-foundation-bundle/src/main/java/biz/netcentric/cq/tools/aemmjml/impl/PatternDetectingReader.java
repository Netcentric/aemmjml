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

import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;

/**
 * This implementation of {@link FilterReader} will throw an exception when it gets closed before the given pattern was seen.
 */
final class PatternDetectingReader extends FilterReader {

    private final char[] pattern;
    private boolean patternMatched = false;
    private int state; // pointer in pattern[] for what we currently looking for

    PatternDetectingReader(Reader delegate, String pattern) {
        super(delegate);
        this.pattern = pattern.toCharArray();
    }

    @Override
    public void close() throws IOException {
        super.close();

        if (!patternMatched) {
            throw new IOException("Didn't see pattern: " + Arrays.toString(pattern));
        }
    }

    @Override
    public int read() throws IOException {
        char[] buffer = new char[1];
        int read = read(buffer);

        if (read < 0) {
            return read;
        } else {
            return buffer[0];
        }
    }

    @Override
    public int read(char[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public int read(char[] b, int off, int len) throws IOException {
        if (patternMatched) {
            return -1;
        }

        int read = super.read(b, off, len);
        int i;

        for (i = 0; i < read && !patternMatched; i++) {
            char in = b[i + off];
            if (pattern[state] != in) {
                // reset state
                state = 0;
            }
            if (pattern[state] == in) {
                // advance state
                state++;
            }
            patternMatched = state >= pattern.length;
        }

        // only return until the last byte of the pattern
        return patternMatched ? i : read;
    }
}
