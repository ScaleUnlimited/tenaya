package com.scaleunlimited.tenaya.nio;

import com.scaleunlimited.tenaya.data.Kmer;

public class NIOEncoder {
	
	private NIOSample sample;
	private int ksize;
	
	private boolean sawNull;
	private boolean consumed;
	private long f, r;
	private long shiftMask;
	
	public NIOEncoder(int ksize, NIOSample sample) {
		this.ksize = ksize;
		this.sample = sample;
		
		sawNull = false;
		consumed = true;
		
		shiftMask = ~(0x0ffffffffffffffffL << (ksize * 2));
		
		setSample(sample);
	}
	
	public void setSample(NIOSample sample) {
		this.sample = sample;
		loadRegisters();
	}
	
	private long kmerRepr(byte b) {
		if (b == 65 || b == 97) {
			return 0L;
		} else if (b == 84 || b == 116) {
			return 1L;
		} else if (b == 67 || b == 99) {
			return 2L;
		} else {
			return 3L;
		}
	}
	
	private long kmerComp(byte b) {
		if (b == 65 || b == 97) {
			return 1L;
		} else if (b == 84 || b == 116) {
			return 0L;
		} else if (b == 67 || b == 99) {
			return 3L;
		} else {
			return 2L;
		}
	}
	
	private void loadRegisters() {
		f = 0;
		r = 0;
		for (int i = 0; i < (ksize - 1); i++) {
			byte currentByte = sample.readByte();
			shift();
			updateByte(currentByte);
		}
	}
	
	private void updateByte(byte currentByte) {
		f |= kmerRepr(currentByte);
		r |= (kmerComp(currentByte) << (ksize * 2 - 2));
	}
	
	private void shift() {
		f <<= 2;
		f &= shiftMask;
		r >>>= 2;
	}

	public boolean hasNext() {
		if (sawNull) return false;
		if (!consumed) return true;
		byte currentByte = sample.readByte();
		if (currentByte == 0) {
			sawNull = true;
			return false;
		// byte 10 is newline
		} else if (currentByte == 10) {
			loadRegisters();
			return hasNext();
		}
		shift();
		updateByte(currentByte);
		consumed = false;
		return true;
	}
	
	public long next() {
		if (!hasNext()) return 0L;
		consumed = true;
		return Kmer.unify(f,  r);
	}

}
