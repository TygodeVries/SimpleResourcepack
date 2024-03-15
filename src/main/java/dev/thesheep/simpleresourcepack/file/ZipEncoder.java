package dev.thesheep.simpleresourcepack.file;

import org.bukkit.Bukkit;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipEncoder {
    /**
     * Generate a zipfile
     * @param sourceDirPath The folder to compress
     * @param zipFilePath The location of the output file
     */
    public static void createZipFile(String sourceDirPath, String zipFilePath) {
        FileOutputStream fileStream;
        try {
            fileStream = new FileOutputStream(zipFilePath);
            ZipOutputStream zipFile = new ZipOutputStream(fileStream);

            File zippingFile = new File(sourceDirPath);
            zipFile(zippingFile, zippingFile.getName(), zipFile);

            zipFile.close();
            fileStream.close();
        }
        catch (Exception e) {
            Bukkit.getLogger().info("Unable to zip the file: " + e + " \n " + e.getMessage());
        }
    }

    private static void zipFile(File fileToZip, String fileName, ZipOutputStream zipFileOutputStream) throws Exception
    {
        byte[] bytes = new byte[1024];

        if (fileToZip.isDirectory()) {
            File[] children = fileToZip.listFiles();
            assert children != null;
            for (File childFile : children) {
                zipSubFile(childFile, childFile.getName(), zipFileOutputStream);
            }
            return;
        }

        FileInputStream fis = new FileInputStream(fileToZip);

        ZipEntry zipEntry = new ZipEntry(fileName);

        zipFileOutputStream.putNextEntry(zipEntry);

        int length;
        while ((length = fis.read(bytes)) > 0) {
            zipFileOutputStream.write(bytes, 0, length);
        }
        fis.close();
    }

    private static void zipDirectory(File fileToZip, String fileName, ZipOutputStream zipFileOutputStream) throws Exception
    {
        if (fileToZip.isDirectory()) {
            File[] children = fileToZip.listFiles();
            if(children != null) {
                for (File childFile : children) {
                    zipSubFile(childFile, fileName + "/" + childFile.getName(), zipFileOutputStream);
                }
            }
        }
    }

    private static void zipSubFile(File fileToZip, String fileName, ZipOutputStream zipFileOutputStream) throws Exception {
        if (fileToZip.isDirectory()) {
            zipDirectory(fileToZip, fileName, zipFileOutputStream);
            return;
        }

        byte[] bytes = new byte[1024];

        FileInputStream fis = new FileInputStream(fileToZip);

        ZipEntry zipEntry = new ZipEntry(fileName);

        zipFileOutputStream.putNextEntry(zipEntry);

        int length;
        while ((length = fis.read(bytes)) > 0) {
            zipFileOutputStream.write(bytes, 0, length);
        }

        zipFileOutputStream.flush();

        fis.close();
    }
}
