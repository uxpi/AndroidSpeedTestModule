package com.android.luas.networkspeedtest;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import javax.net.SocketFactory;


public class CustomSocketFactory extends SocketFactory {

    public Socket createSocket() throws IOException {
        return customSocket(SocketFactory.getDefault().createSocket());
    }

    public static SocketFactory getDefault() {
        return new CustomSocketFactory();
    }

    public Socket createSocket(String host, int port) throws IOException {
        return customSocket(new Socket(host, port));
    }

    public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException {
        return customSocket(new Socket(host, port, localHost, localPort));
    }

    public Socket createSocket(InetAddress host, int port) throws IOException {
        return customSocket(new Socket(host, port));
    }

    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
        return customSocket(new Socket(address, port, localAddress, localPort));
    }

    public Socket customSocket(Socket s) throws SocketException {
        s.setReceiveBufferSize(1024*1024*1024);
        s.setTcpNoDelay(false);
        s.setKeepAlive(true);
        s.setPerformancePreferences(0,1,2);
        return s;
    }
}
