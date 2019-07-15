package edu.fandm.research.vpnplus.VPNConfiguration.Protocol.IP;

import java.net.InetAddress;
import java.util.Arrays;

import edu.fandm.research.vpnplus.Utilities.ByteOperations;
import edu.fandm.research.vpnplus.VPNConfiguration.Protocol.AbsHeader;

public abstract class IPHeader extends AbsHeader {
    protected int headerLength, length;
    protected InetAddress srcAddress, dstAddress;
    protected byte protocol = 0;
    protected int srcIndex, dstIndex, addressSize;
    protected int lengthIndex;

    public static IPHeader create(byte[] data) {
        int version = (data[0] >> 4);
        if(version == 4) return new IPv4Header(data);
        else return new IPv6Header(data);
    }

    public int length() {
        return length;
    }

    public void setLength(int l) {
        length = l;
        data[lengthIndex] = (byte)(l >> 8);
        data[lengthIndex + 1] = (byte)(l % 256);
    }

    public byte protocol() {
        return protocol;
    }

    public InetAddress getSrcAddress() {
        return srcAddress;
    }
    public InetAddress getDstAddress() {
        return dstAddress;
    }

    public abstract byte[] getPseudoHeader(int dataLength);

    public byte[] getSrcAddressByteArray() {
        return Arrays.copyOfRange(data, srcIndex, srcIndex + addressSize);
    }

    public byte[] getDstAddressByteArray() {
        return Arrays.copyOfRange(data, dstIndex, dstIndex + addressSize);
    }

    @Override
    public IPHeader reverse() {
        byte[] reverseData = Arrays.copyOfRange(data, 0, data.length);
        ByteOperations.swap(reverseData, srcIndex, dstIndex, addressSize);
        return IPHeader.create(reverseData);
    }

    public void updateSrcAddress(InetAddress srcAddr) {
        this.srcAddress = srcAddr;
        System.arraycopy(srcAddr.getAddress(), 0, data, srcIndex, addressSize);
    }
}
