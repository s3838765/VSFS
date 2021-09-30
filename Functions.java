import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.*;
import java.text.SimpleDateFormat;
import java.util.Scanner;

public class Functions {

   public void list() {
      try {
         Scanner scanner = new Scanner(FileSystem.fs);
         String nextLine;

         // scan through every line of the file
         while (scanner.hasNextLine()) {
            nextLine = scanner.nextLine();
            // file
            if (nextLine.startsWith("@")) {
               listFile(false, nextLine.substring(1));
            // directory
            } else if (nextLine.startsWith("=")) {
               listFile(true, nextLine.substring(1));
            }
         }

      } catch (IOException e) {
         e.printStackTrace();
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   private void listFile(boolean isDir, String fileName) throws IOException {
      System.out.printf("%s%s %d %s %s %d %s %s%n",
         isDir ? "d" : "-",
         PosixFilePermissions.toString(Files.getPosixFilePermissions(FileSystem.fs.toPath())),
         Files.getAttribute(FileSystem.fs.toPath(), "unix:nlink"),
         // Files.getAttribute(FSUtil.fs.toPath(), "unix:uid"),
         Files.getOwner(FileSystem.fs.toPath()),
         // TODO: Convert group id to group name
         Files.getAttribute(FileSystem.fs.toPath(), "unix:gid"),
         Files.getAttribute(FileSystem.fs.toPath(), "size"),
         new SimpleDateFormat("MMM dd HH:mm").format(Files.getLastModifiedTime(FileSystem.fs.toPath()).toMillis()),
         fileName);
   }

   public void copyIn(String extFileName, String intFileName) {
      try {
         File extFile = new File(extFileName);
         InternalFile intFile = new InternalFile(extFile, intFileName);
         if (!FileSystem.fileExists(intFileName)) {
            intFile.addToFileSystem();
         } else {
            System.out.println("This file already exists within the file system.");
         }
      } catch (Exception e) {
         e.printStackTrace();
      }

   }

   public void rm(String fileName) {
   }

}
