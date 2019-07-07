/*
 *    Copyright  2019 Denis Kokorin
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package com.github.kokorin.jaffree.ffmpeg;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.SeekableByteChannel;

public class HttpInput<T extends HttpInput<T>> extends BaseInput<T> implements Input {
    private final SeekableByteChannel channel;
    private final ServerSocket serverSocket;

    public HttpInput(SeekableByteChannel channel) {
        this.channel = channel;
        this.serverSocket = allocateSocket();
        setInput("http://127.0.0.1:" + serverSocket.getLocalPort());
    }

    protected ServerSocket allocateSocket() {
        try {
            return new ServerSocket(0, 1, InetAddress.getLoopbackAddress());
        } catch (IOException e) {
            throw new RuntimeException("Failed to allocate socket", e);
        }
    }

    @Override
    public final Runnable helperThread() {
        final Negotiator negotiator = negotiator();

        return new Runnable() {
            @Override
            public void run() {
                try {
                    negotiator.negotiateAndClose(serverSocket);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to read from socket " + serverSocket, e);
                }
            }
        };
    }

    public Negotiator negotiator() {
        return new HttpNegotiator(channel);
    }

    public interface Negotiator {
        void negotiateAndClose(ServerSocket serverSocket);
    }

    private static class HttpNegotiator implements Negotiator {
        private final SeekableByteChannel channel;

        public HttpNegotiator(SeekableByteChannel channel) {
            this.channel = channel;
        }

        @Override
        public void negotiateAndClose(ServerSocket serverSocket) {
            boolean complete = false;
            try (AutoCloseable toClose = serverSocket) {
                while (!complete) {
                    try (
                            Socket socket = serverSocket.accept();
                            InputStream input = socket.getInputStream();
                            OutputStream output = socket.getOutputStream()
                    ) {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(input));

                        String verbAndVersion = reader.readLine();
                        String range = null;
                        while (true) {
                            String line = reader.readLine();
                            System.out.println(">>> " + line);
                            if (line == null || line.isEmpty()) {
                                break;
                            }

                            String[] headerAndValue = line.split(": ");
                            String header = headerAndValue[0];
                            String value = headerAndValue[1];
                            if (header.equalsIgnoreCase("Range")) {
                                range = value;
                            }
                        }

                        if (range != null && !range.startsWith("bytes=")) {
                            throw new RuntimeException("Unknown Range unit: " + range);
                        }

                        String[] startAndEnd = range.substring(6).split("-");
                        String start = startAndEnd[0];
                        String end = startAndEnd[1];

                        long from = 0;
                    }
                    complete = true;
                }
            } catch (Exception e) {

            }
        }
    }
}
