package com.tencent.audiochanneldemo.player;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by zoroweili on 2019-2-22.
 */

public class WavFileWriter implements Closeable{
    private File file;
    private BufferedOutputStream outputStream;
    private static final int BYTE_BUFFER_SIZE = 20480;
    private int sampleRate;
    private int channels;
    private int sampleBits;
    private int bytesWritten;
    private boolean isNeedHeader;
    private AtomicBoolean isOpened = new AtomicBoolean(false);
    private AtomicBoolean isClosed = new AtomicBoolean(false);

    public WavFileWriter(File file, int sampleRate, int channels, int sampleBits) {
        this.file = file;
        this.sampleRate = sampleRate;
        this.channels = channels;
        this.sampleBits = sampleBits;
        this.bytesWritten = 0;
    }

    public void setSampleBits(int sampleBits) {
        this.sampleBits = sampleBits;
    }

    public boolean open(boolean needHeader) throws IOException {
        if(this.isOpened.getAndSet(true)) {
            return false;
        } else {
            this.isNeedHeader = needHeader;
            boolean flag = true;
            if(this.file.exists()) {
                flag = this.file.delete();
            }

            if(!flag) {
                return false;
            } else {
                IOUtils.mkdirs(this.file.getParentFile());
                if(this.file.createNewFile()) {
                    try {
                        if(this.isNeedHeader) {
                            IOUtils.fillBlankLength(this.file, 44);
                        }

                        this.outputStream = new BufferedOutputStream(new FileOutputStream(this.file), 20480);
                        return true;
                    } catch (Throwable var4) {
                        return false;
                    }
                } else {
                    return false;
                }
            }
        }
    }

    public void close() throws IOException {
        if(this.isOpened.get()) {
            if(!this.isClosed.getAndSet(true)) {
                IOUtils.close(this.outputStream);
                if(this.isNeedHeader) {
                    this.writeHeader();
                }

            }
        }
    }

    public void write(short[] src, int offset, int length) throws IOException {
        if(offset > length) {
            throw new IndexOutOfBoundsException(String.format("offset %d is greater than length %d", new Object[]{Integer.valueOf(offset), Integer.valueOf(length)}));
        } else {
            for(int i = offset; i < length; ++i) {
                IOUtils.writeUnsignedShortLE(this.outputStream, src[i]);
                this.bytesWritten += 2;
            }

        }
    }

    public void write(byte[] src, int offset, int length) throws IOException {
        if(offset > length) {
            throw new IndexOutOfBoundsException(String.format("offset %d is greater than length %d", new Object[]{Integer.valueOf(offset), Integer.valueOf(length)}));
        } else {
            this.outputStream.write(src, offset, length);
            this.bytesWritten += length;
        }
    }

    private void writeHeader() throws IOException {
        RandomAccessFile file = new RandomAccessFile(this.file, "rw");
        file.seek(0L);
        int bytesPerSec = (this.sampleBits + 7) / 8;
        file.writeBytes("RIFF");
        file.writeInt(Integer.reverseBytes(this.bytesWritten + 36));
        file.writeBytes("WAVE");
        file.writeBytes("fmt ");
        file.writeInt(Integer.reverseBytes(16));
        file.writeShort(Short.reverseBytes((byte)1));
        file.writeShort(Short.reverseBytes((short)this.channels));
        file.writeInt(Integer.reverseBytes(this.sampleRate));
        file.writeInt(Integer.reverseBytes(this.sampleRate * this.channels * bytesPerSec));
        file.writeShort(Short.reverseBytes((short)(this.channels * bytesPerSec)));
        file.writeShort(Short.reverseBytes((short)this.sampleBits));
        file.writeBytes("data");
        file.writeInt(Integer.reverseBytes(this.bytesWritten));
        file.close();
    }
}
