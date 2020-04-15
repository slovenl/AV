package com.tencent.audiochanneldemo.player;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.Collection;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by zoroweili on 2019-2-22.
 */

public class IOUtils {
    public IOUtils() {
    }

    public static void close(OutputStream os) {
        try {
            os.flush();
        } catch (Throwable var3) {
            ;
        }

        try {
            os.close();
        } catch (Throwable var2) {
            ;
        }

    }

    public static void close(Closeable closeable) {
        try {
            closeable.close();
        } catch (Throwable var2) {
            ;
        }

    }

    public static void fillBlankLength(File file, int length) throws Throwable {
        RandomAccessFile rFile = new RandomAccessFile(file, "rw");
        rFile.setLength((long)length);
        rFile.close();
    }

    public static void writeUnsignedShortLE(OutputStream stream, short shortData) throws
        IOException {
        stream.write(shortData);
        stream.write(shortData >> 8);
    }

    public static boolean mkdirs(File file) {
        return file == null?false:(file.exists()?file.isDirectory():file.mkdirs());
    }

    public static boolean zip(Collection<File> srcFiles, File dest, byte[] buffer) {
        if(srcFiles != null && srcFiles.size() >= 1 && dest != null) {
            boolean resu = false;
            ZipOutputStream zos = null;

            try {
                zos = new ZipOutputStream(new FileOutputStream(dest, false));
                Iterator var5 = srcFiles.iterator();

                while(var5.hasNext()) {
                    File src = (File)var5.next();
                    doZip(zos, src, buffer);
                }

                zos.flush();
                zos.closeEntry();
                resu = true;
            } catch (IOException var10) {
                resu = false;
            } finally {
                close((OutputStream)zos);
            }

            return resu;
        } else {
            return false;
        }
    }

    private static void doZip(ZipOutputStream zos, File file, byte[] buffer) throws IOException {
        if(zos != null && file != null && file.length() > 0L) {
            if(!file.exists()) {
                throw new FileNotFoundException("Target File is missing");
            } else {
                InputStream inputStream = null;
                if(file.isFile()) {
                    try {
                        inputStream = new FileInputStream(file);
                        zos.putNextEntry(new ZipEntry(file.getName()));

                        int readLen;
                        while(-1 != (readLen = inputStream.read(buffer, 0, buffer.length))) {
                            zos.write(buffer, 0, readLen);
                        }

                        zos.closeEntry();
                    } catch (Throwable var12) {
                        throw var12;
                    } finally {
                        close((Closeable)inputStream);
                    }
                } else {
                    File[] subFiles = file.listFiles();
                    if(subFiles != null) {
                        File[] var6 = subFiles;
                        int var7 = subFiles.length;

                        for(int var8 = 0; var8 < var7; ++var8) {
                            File subFile = var6[var8];
                            doZip(zos, subFile, buffer);
                        }
                    }
                }

            }
        } else {
            throw new IOException("I/O Object got NullPointerException");
        }
    }
}
