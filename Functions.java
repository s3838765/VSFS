import java.io.*;
import java.nio.file.Files;
import java.nio.file.attribute.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

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

   public void copyOut(String intFileName, String extFileName) {
      try {
         System.out.println("Copying file " + intFileName + " to " + extFileName);

         InternalFile intFile = FileSystem.getFile(intFileName);
         File extFile = new File(extFileName);
         if (intFile.isDir) {
            // TODO: recursively copy directories
            if (extFile.mkdir()) {
               System.out.println("Successfully created directory " + intFileName);
            } else {
               System.out.println("Could not create directory " + intFileName);
            }
         } else {
            // create file on external system using given file name
            // initialise writer for external file
            PrintWriter extWriter = new PrintWriter(new BufferedWriter(new FileWriter(extFileName, true)));
            // iterate each line of data from the file and print it to the external file
            FileSystem.getFile(intFileName).data.forEach(dataLine -> {
               extWriter.println(dataLine);
            });
            // close the writer
            extWriter.close();
         }


      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   /**
    * Creates an empty internal directory in the internal file system
    *
    * @param dirName name of the directory you would like to create
    */
   public void mkDir(String dirName) {
      // add "/" to directory if it does not contain it
      if (!dirName.endsWith("/")) {
         dirName += "/";
      }
      System.out.println("dirName: " + dirName);
      System.out.println("exists: " + FileSystem.fileExists(dirName));
      if (!FileSystem.fileExists(dirName)) {
         FileSystem.writeLineToFile(Symbol.DIR + dirName);
         FileSystem.allFiles.add(new InternalFile(dirName));
         System.out.println("Adding " + dirName + " to internal file system.");
      } else {
         System.out.println("Cannot add " + dirName + " to file system. (already exists)");
      }
   }

   public void rm(String fileName) {
      // call remove from internal file
      FileSystem.getFile(fileName).removeFileFromSystem();
   }

   public void defrag() {
      PeekableScanner sc = null;
      try {
         sc = new PeekableScanner(FileSystem.fs);
      } catch (FileNotFoundException e) {
         e.printStackTrace();
      }
      // iterate the file system and convert all data to java objects for easy access
      String nextLine;
      while (sc.hasNextLine()) {
         nextLine = sc.nextLine();
         // if file does not already exist in the internal file system
         // read a single file
         if (nextLine.startsWith(Symbol.IGNORE)) {
            System.out.println("A line was ignored by the compiler");
            // handle extraneous values
         } else {
            System.out.println("An unknown file type was found by the compiler. (" + nextLine.charAt(0) + ")");
         }
         // file already exists within the internal file system
      }
   }

   public void index() {
      try {
         PrintWriter pw = new PrintWriter(FileSystem.fs);
         FileSystem.allFiles.forEach(file -> {
            if (file.isDir) {
               pw.println(Symbol.DIR + file.name);
            } else {
               pw.println(Symbol.FILE + file.name);
               file.data.forEach(line -> {
                  pw.println(Symbol.DATA + line);
               });
            }
         });
         pw.close();
      } catch (FileNotFoundException e) {
         System.out.println("File system was not found.");
         System.err.println(e);
      }
   }
}