package edu.fandm.research.vpnplus.VPNConfiguration.Forwarder;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import edu.fandm.research.vpnplus.Helpers.Logger;
import edu.fandm.research.vpnplus.VPNConfiguration.Protocol.IP.IPDatagram;
import edu.fandm.research.vpnplus.VPNConfiguration.Protocol.IP.IPHeader;
import edu.fandm.research.vpnplus.VPNConfiguration.Protocol.UDP.UDPDatagram;
import edu.fandm.research.vpnplus.VPNConfiguration.Protocol.UDP.UDPHeader;

public class UDPForwarderWorker extends Thread {
    private static final String TAG = UDPForwarderWorker.class.getSimpleName();
    private static final boolean DEBUG = false;
    private final int LIMIT = 32767;
    private UDPForwarder forwarder;
    private IPHeader newIPHeader;
    private UDPHeader newUDPHeader;
    private int srcPort;
    private DatagramSocket socket;

    // ipDatagram is a request UDP packet that is going to be put into socket by forwarder
    // this thread will wait for responses to this request (or later request UDP packets sent from same source port)
    public UDPForwarderWorker(IPDatagram ipDatagram, DatagramSocket socket, UDPForwarder forwarder) {
        this.socket = socket;
        this.forwarder = forwarder;
        this.newIPHeader = ipDatagram.header().reverse();
        UDPDatagram udpDatagram = (UDPDatagram)ipDatagram.payLoad();
        this.newUDPHeader = (UDPHeader)udpDatagram.header().reverse();
        // will have to update address/port of sender of response before we can create the UDP response
    }

    @Override
    public void interrupt(){
        super.interrupt();
        // closing the socket will interrupt the receive() operation
        if (socket != null) socket.close();
    }

    public void run() {
        ByteBuffer packet = ByteBuffer.allocate(LIMIT);
        DatagramPacket datagram = new DatagramPacket(packet.array(), LIMIT);

        try {
            while (!isInterrupted()) {
                packet.clear();
                //socket.setSoTimeout(60000);
                socket.receive(datagram);

                byte[] received = Arrays.copyOfRange(datagram.getData(), 0, datagram.getLength());
                if (received != null) {
                    newIPHeader.updateSrcAddress(datagram.getAddress());
                    newUDPHeader.updateSrcPort(datagram.getPort());
                    UDPDatagram response = new UDPDatagram(newUDPHeader, received);
                    //response.debugInfo(dstAddress);
                    if (DEBUG) Logger.d(TAG, "response is " + response.debugString());
                    forwarder.forwardResponse(newIPHeader, response);
                }
            }
        } catch (SocketException e) {
            // receive() got interrupted
            return;
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }
}
