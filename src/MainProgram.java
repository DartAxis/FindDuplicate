import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class MainProgram {
    final static int BUFF = 16384;

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, InterruptedException {
        Map<String, List<String>> fileDuplicate = new HashMap<>();
        fileDuplicate = getListFiles("c:\\dir\\", fileDuplicate);

        fileDuplicate.forEach((a, b) -> {
            if (b.size() > 1) {
                System.out.println("Файлы дубликаты:");
                b.forEach(x -> {
                    System.out.println(x);
                });
            }
        });
    }

    private static Map<String, List<String>> getListFiles(String directory, final Map<String, List<String>> fileDuplicate) throws IOException, NoSuchAlgorithmException {
        final MessageDigest md = MessageDigest.getInstance("SHA-256");//SHA, MD2, MD5, SHA-256, SHA-384
        Files.walk(Paths.get(directory))
                .filter(Files::isRegularFile)
                .peek(x -> {
                    try {
                        String hash = checksum(x.toString(), md);
                        if (fileDuplicate.containsKey(hash)) {
                            fileDuplicate.get(hash).add(x.toString());
                        } else {
                            List<String> filelist = new ArrayList<>();
                            filelist.add(x.toString());
                            fileDuplicate.put(hash, filelist);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).count();
        return fileDuplicate;
    }

    public static String checksum(String filepath, MessageDigest md) throws IOException {
        long timeStart = System.currentTimeMillis();
        long fileSize = Files.size(Paths.get(filepath));
        int buff = BUFF;
        RandomAccessFile file = new RandomAccessFile(filepath, "r");
        byte[] buffer = new byte[buff];
        long read = 0;
        long offset = file.length();
        System.out.print("Processing file " + filepath);
        int unitsize;
        while (read < offset) {
            unitsize = (int) (((offset - read) >= buff) ? buff : (offset - read));
            file.read(buffer, 0, unitsize);
            md.update(buffer, 0, unitsize);
            read += unitsize;
        }
        file.close();
        StringBuilder result = new StringBuilder();
        for (byte b : md.digest()) {
            result.append(String.format("%02x", b));
        }
        long timeStop = System.currentTimeMillis();
        System.out.println(" . Complete in " + (timeStop - timeStart) / 1000 + " sec. With filesize is - " + (fileSize) + " bytes. Hash: " + result.toString());
        return result.toString();

    }
}
