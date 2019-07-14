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

import com.github.kokorin.jaffree.util.HttpServer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.nio.channels.SeekableByteChannel;

public class HttpInput extends BaseInput<HttpInput> implements Input {
    private final String fileName;
    private final SeekableByteChannel channel;
    private final ServerSocket serverSocket;
    //private final HttpServer httpServer;

    public HttpInput(String fileName, SeekableByteChannel channel) {
        this.fileName = fileName;
        this.channel = channel;
        this.serverSocket = allocateSocket();
        //this.serverSocket = allocateServer();
        setInput("http://127.0.0.1:" + serverSocket.getLocalPort()+ "/" + fileName);
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
        //return new HttpServer2(fileName, serverSocket, channel);
        return new HttpServer(channel, serverSocket);
        //final Negotiator negotiator = negotiator();

        /*return new Runnable() {
            @Override
            public void run() {
                try {
                    negotiator.negotiateAndClose(serverSocket);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to read from socket " + serverSocket, e);
                }
            }
        };*/
    }

    protected Negotiator negotiator() {
        return new HttpNegotiator(channel);
    }

    protected interface Negotiator {
        void negotiateAndClose(ServerSocket serverSocket);
    }

    private static class HttpNegotiator implements Negotiator {
        private final SeekableByteChannel channel;

        public HttpNegotiator(SeekableByteChannel channel) {
            this.channel = channel;
        }

        @Override
        public void negotiateAndClose(ServerSocket serverSocket) {
        }
    }
}
