/*
 * Copyright (c) 2016 Markus Uhlin <markus@dataswamp.org>
 *
 * Permission to use, copy, modify, and distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

import java.net.*;

public class Utils {
    private DatagramPacket pkt;
    private DatagramSocket udpSock;
    private Socket sock;
    private SocketAddress sockAddr;
    private boolean isPortOpenState = false;
    private final byte[] ar = {'X'};
    private final int timeoutMilliSec = 50;

    public boolean checkForOpenTcpPort(String host, int port) {
	try {
	    sockAddr = new InetSocketAddress(host, port);
	    sock = new Socket();
	    sock.connect(sockAddr, timeoutMilliSec);
	    isPortOpenState = true;
	} catch (Exception e) {
	    isPortOpenState = false;
	}

	try { sock.close(); } catch (Exception e) {}
	return (isPortOpenState);
    }

    public boolean checkForOpenUdpPort(String host, int port) {
	try {
	    udpSock  = new DatagramSocket();
	    sockAddr = new InetSocketAddress(host, port);
	    pkt	     = new DatagramPacket(ar, 1, sockAddr);

	    udpSock.setSoTimeout(timeoutMilliSec);
	    udpSock.connect(sockAddr);

	    /* Do a few writes to see if the UDP port is there */
	    for (int i = 0; i <= 3; i++)
		udpSock.send(pkt);

	    isPortOpenState = true;
	} catch (Exception e) {
	    isPortOpenState = false;
	}

	try { udpSock.close(); } catch (Exception e) {}
	return (isPortOpenState);
    }

    public final String strPort(int port) {
	switch (port) {
	case 20:
	    return "ftp-data";
	case 21:
	    return "ftp";
	case 22:
	    return "ssh";
	case 23:
	    return "telnet";
	case 25:
	    return "smtp";
	case 69:
	    return "tftp";
	case 79:
	    return "finger";
	case 80:
	    return "http";
	case 107:
	    return "rtelnet";
	case 109:
	    return "pop2";
	case 110:
	    return "pop3";
	case 111:
	    return "sunrpc";
	case 115:
	    return "sftp";
	case 123:
	    return "ntp";
	case 137:
	    return "netbios-ns";
	case 138:
	    return "netbios-dgm";
	case 139:
	    return "netbios-ssn";
	case 143:
	    return "imap";
	case 179:
	    return "bgp";
	case 443:
	    return "https";
	case 604:
	    return "tunnel";
	case 631:
	    return "ipp";
	case 873:
	    return "rsync";
	case 992:
	    return "telnets";
	case 993:
	    return "imaps";
	case 995:
	    return "pop3s";
	}

	return "";
    }
}
