/*
 * Copyright 2002-2022 CODESKY.COM Team Group.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * Github:
 *	https://github.com/codesky-com/reb.git
 */

package com.codesky.reb.message.struct;

import com.codesky.reb.message.struct.DataStructOuterClass.DataStruct;
import com.google.protobuf.ByteString;

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

	public byte[] toDataStructByteArray() {
		DataStruct ds = DataStruct.newBuilder()
				.setLength(length)
				.setCmd(cmd)
				.setFlags(flags)
				.setSign(sign)
				.setData(ByteString.copyFrom(data))
				.build();
		return ds.toByteArray();
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
