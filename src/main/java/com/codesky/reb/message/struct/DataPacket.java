package com.codesky.reb.message.struct;

import com.codesky.reb.message.struct.DataStructOuterClass.DataStruct;

public class DataPacket {

	public final static int HEADER_SIZE = Integer.SIZE + Long.BYTES * 3;
	
	private int length;
	private long cmd;
	private long flags;
	private long sign;
	private byte[] data;
	
	public DataPacket(DataStruct ds) {
		this.length = ds.getLength();
		this.cmd = ds.getCmd();
		this.flags = ds.getFlags();
		this.sign = ds.getSign();
		this.data = ds.getData().toByteArray();
	}
	
	public DataPacket(long cmd, long flags, long sign, byte[] data) {
		this.length = data.length + HEADER_SIZE;
		this.cmd = cmd;
		this.flags = flags;
		this.sign = sign;
		this.data = data;
	}
	
	public int getLength() {
		return length;
	}
	public void setLength(int length) {
		this.length = length;
	}
	public long getCmd() {
		return cmd;
	}
	public void setCmd(long cmd) {
		this.cmd = cmd;
	}
	public long getFlags() {
		return flags;
	}
	public void setFlags(long flags) {
		this.flags = flags;
	}
	public long getSign() {
		return sign;
	}
	public void setSign(long sign) {
		this.sign = sign;
	}
	public byte[] getData() {
		return data;
	}
	public void setData(byte[] data) {
		this.data = data;
	}
	
}
