/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package net.firejack.platform.provider;

import java.io.IOException;
import java.io.InputStream;

public class MultipartInputStream extends InputStream {
    public static final int ITERATE = 10;
    private ProgressListener listener;
    private InputStream stream;

    private long percent;
    private long current;
    private long part;

    /**
     * @param stream
     * @param listener
     * @param length
     */
    public MultipartInputStream(InputStream stream, ProgressListener listener, Long length) {
        this.stream = stream;
        this.listener = listener;
        this.part = length / ITERATE;
    }

    @Override
    public int read() throws IOException {
        return stream.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        int read = stream.read(b);
        progress(read);
        return read;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int read = stream.read(b, off, len);
        progress(read);
        return read;
    }

    @Override
    public long skip(long n) throws IOException {
        return stream.skip(n);
    }

    @Override
    public int available() throws IOException {
        return stream.available();
    }

    @Override
    public void close() throws IOException {
        stream.close();
    }

    @Override
    public void mark(int readlimit) {
        stream.mark(readlimit);
    }

    @Override
    public void reset() throws IOException {
        stream.reset();
    }

    @Override
    public boolean markSupported() {
        return stream.markSupported();
    }

    private void progress(int length) {
        if (length == -1) {
            return;
        }
        current += length;
        long count = current / part;
        current = current % part;
        percent += (count * 100) / ITERATE;

        if (count != 0) {
            notify((int) percent);
        }
    }

    private void notify(int percent) {
        if (listener != null) {
            listener.progress(percent);
        }
    }
}
