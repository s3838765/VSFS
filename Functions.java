import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.*;
import java.text.SimpleDateFormat;
import java.util.Scanner;

public class Functions {

   /**
    * Iteratively call the listFile function on every file and directory in the file system
    */
   public void list() {
      FileSystem.allFiles.forEach(internalFile -> {
         try {
            listFile(internalFile);
         } catch (IOException e) {
            e.printStackTrace();
            System.err.println(e);
         }
      });
   }

   /**
    * List the attributes of a single file within the internal file system
    *
    * @param intFile an internal file stored by the FileSystem class
    * @throws IOException
    */
   private void listFile(InternalFile intFile) throws IOException {
      System.out.printf("%s%s %d %s %s %d %s %s%n",
         intFile.isDir ? "d" : "-",
         PosixFilePermissions.toString(Files.getPosixFilePermissions(FileSystem.fs.toPath())),
         Files.getAttribute(FileSystem.fs.toPath(), "unix:nlink"),
         // Files.getAttribute(FSUtil.fs.toPath(), "unix:uid"),
         Files.getOwner(FileSystem.fs.toPath()),
         // TODO: Convert group id to group name
         Files.getAttribute(FileSystem.fs.toPath(), "unix:gid"),
         Files.getAttribute(FileSystem.fs.toPath(), "size"),
         new SimpleDateFormat("MMM dd HH:mm").format(Files.getLastModifiedTime(FileSystem.fs.toPath()).toMillis()),
         intFile.name);
   }

   /**
    *
    * @param extFileName
    * @param intFileName
    */
   public void copyIn(String extFileName, String intFileName) {
      try {
         // convert the external file into a File object and then create an InternalFile from it
         File extFile = new File(extFileName);
         InternalFile intFile = new InternalFile(extFile, intFileName);

         // if the file (name) does not exist, add it to the system
         // this allows you to copy an external file into the internal file system with an alternate name
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
