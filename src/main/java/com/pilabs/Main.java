package com.pilabs;

import org.apache.commons.net.ftp.FTPClient;

import java.io.*;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {

    private static List<String> fileList(String dir) {
        List<String> list = new ArrayList<>();
        File[] mDir = new File("./"+dir).listFiles();
        if (mDir != null) {
            for (File file : mDir) {
                list.add(file.getName());
            }
        }
        return list;
    }


    private static void moveFiles(FTPClient ftp, String dir) throws IOException {
        List<String> missing = new ArrayList<>();
        List<String> extra = new ArrayList<>();
        List<String> ignore = new ArrayList<>();
        String baseDir = FileSystems.getDefault().getPath(".").toAbsolutePath().getParent().toString();

        if (dir.equals("mods")) {
            try {

                File file = new File(baseDir+"/modIgnore.txt");

                if (file.createNewFile())
                    System.out.println("Ignore file doesn't exist. Creating file");
                LineNumberReader lineReader = new LineNumberReader(new FileReader(file));

                int i = 1;
                while (i<20) {
                    System.out.println(i);
                    i++;
                    String line = lineReader.readLine();
                    if (line == null)
                        break;
                    if (i == 1)
                        System.out.println("Found files to ignore:");
                    System.out.println("./mods/"+line);
                    ignore.add(line);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        List<String> sList = Arrays.asList(ftp.listNames(baseDir+"/pack/"+dir));
        List<String> cList = fileList(dir);

        for (String file : sList) {
            if (!sList.contains(file)) {
                missing.add(file);
            }
        }
        for (String file : cList) {
            if (!sList.contains(file)) {
                if (!dir.equals("mod"))
                    extra.add(file);
                else if (!ignore.contains(file))
                    extra.add(file);
            }
        }
        for (String file : extra) {
            File tFile = new File("./mods"+file);
            if (tFile.delete()) {
                System.out.println("Mod "+file+" was removed");
            }
            else
                System.out.println("Mod "+file+" could not be removed");
        }
        for (String file : missing) {
            System.out.println("Downloading "+file+"...");
            try (FileOutputStream fos = new FileOutputStream(baseDir+"/mods/"+file)) {
                if (ftp.retrieveFile("pack/mods/"+file, fos))
                    System.out.println("Downloaded "+file);
                else
                    System.out.println("Download failed");
            }
        }
    }


    public static void main(String[] args) {

        String host = null;
        String username = null;
        String password = null;

        FTPClient ftp = new FTPClient();
        try {
            ftp.connect(host);
            if (ftp.login(username,password)) {
                moveFiles(ftp, "mods");
                moveFiles(ftp, "scripts");
            }
            else
                System.out.println("Could not connect to server. Please contact an admin");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
