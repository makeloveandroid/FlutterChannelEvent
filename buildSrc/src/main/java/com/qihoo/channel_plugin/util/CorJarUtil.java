package com.qihoo.channel_plugin.util;

import com.qihoo.channel_plugin.RegisterTransform;
import com.qihoo.channel_plugin.RegisterTransformKt;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public final class CorJarUtil {
    public static void insertInitCodeIntoJarFile(List<String> channelRootClass, File jarIn) throws IOException {
        File jarOut = new File(jarIn.getParentFile(), "_channel_tmp.jar");
        System.out.println("临时使用路径:" + jarOut.getAbsolutePath()+"   原来路径:"+jarIn.getAbsolutePath());
        try {
            processJar(channelRootClass, jarIn, jarOut, Charset.forName("UTF-8"), Charset.forName("UTF-8"));
        } catch (IllegalArgumentException e) {
            if ("MALFORMED".equals(e.getMessage())) {
                processJar(channelRootClass, jarIn, jarOut, Charset.forName("GBK"), Charset.forName("UTF-8"));
            } else {
                throw e;
            }
        }
        // 擦屁股
        jarIn.delete();

        jarOut.renameTo(jarIn);


    }

    @SuppressWarnings("NewApi")
    private static void processJar(List<String> channelRootClass, File jarIn, File jarOut, Charset charsetIn, Charset charsetOut) throws IOException {
        ZipInputStream zis = null;
        ZipOutputStream zos = null;
        try {
            zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(jarIn)), charsetIn);
            zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(jarOut)), charsetOut);
            ZipEntry entryIn;
            Map<String, Integer> processedEntryNamesMap = new HashMap<>();
            while ((entryIn = zis.getNextEntry()) != null) {
                final String entryName = entryIn.getName();
                if (!processedEntryNamesMap.containsKey(entryName)) {
                    ZipEntry entryOut = new ZipEntry(entryIn);
                    // Set compress method to default, fixed #12
                    if (entryOut.getMethod() != ZipEntry.DEFLATED) {
                        entryOut.setMethod(ZipEntry.DEFLATED);
                    }
                    entryOut.setCompressedSize(-1);
                    zos.putNextEntry(entryOut);
                    if (!entryIn.isDirectory()) {
                        if (entryName.equals(RegisterTransformKt.CORE_CHANNEL_CLASS_NAME)) {
                            System.out.println("是否是要改的类:" + entryName);
                            processClass(channelRootClass, zis, zos);
                        } else {
                            copy(zis, zos);
                        }
                    }
                    zos.closeEntry();
                    processedEntryNamesMap.put(entryName, 1);
                }
            }
        } finally {
            closeQuietly(zos);
            closeQuietly(zis);
        }
    }

    private static void processClass(List<String> channelRootClass, InputStream classIn, OutputStream classOut) throws IOException {
        ClassReader cr = new ClassReader(classIn);
        ClassWriter cw = new ClassWriter(0);
        ClassVisitor cv = new CoreChannelVisitor(channelRootClass, cw);
        cr.accept(cv, 0);
        classOut.write(cw.toByteArray());
        classOut.flush();
    }


    private static void closeQuietly(Closeable target) {
        if (target != null) {
            try {
                target.close();
            } catch (Exception e) {
                // Ignored.
            }
        }
    }

    private static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[8192];
        int c;
        while ((c = in.read(buffer)) != -1) {
            out.write(buffer, 0, c);
        }
    }


}