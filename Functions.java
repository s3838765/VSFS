import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Class used to handle file system functions
 */
public class Functions {

   /**
    * Iteratively call the listFile function on every file and directory in the file system
    */
   public void list() {
      // sort all files into tree structure
      Util.treeSort();
      // find size of all files and store in array - default directories to 512
      Integer[] fileSizes = new Integer[FileSystem.allFiles.size()];
      Integer[] hardLinks = new Integer[FileSystem.allFiles.size()];
      for (int i = 0; i < fileSizes.length; i++) {
         int fileSize = 0;
         if (!FileSystem.allFiles.get(i).isDir) {
            for (String s : FileSystem.allFiles.get(i).data) {
               fileSize += s.length();
            }
         } else {
            fileSize = 512;
         }
         fileSizes[i] = fileSize;

         // calculate number of hard links and store in array
         hardLinks[i] = 0;
         for (InternalFile f : FileSystem.allFiles) {
            if (f.isDir
                 && f.name.startsWith(FileSystem.allFiles.get(i).name)
                 && !f.name.equals(FileSystem.allFiles.get(i).name)
                 && f.name.substring(FileSystem.allFiles.get(i).name.length()).chars().filter(num -> num == '/').count() == 1) {
               hardLinks[i] += 1;
            }
         }
         // no hard links were found - default to 1
         if (hardLinks[i] == 0) {
            hardLinks[i] = 1;
         }
      }

      // list each file
      FileSystem.allFiles.forEach(file -> {
         // calculate max file size to adjust width accordingly
         int maxFileSize = Collections.max(Arrays.asList(fileSizes));

         // format string with variable width for size
         String format = "%s%s %s %s %s %" + String.valueOf(maxFileSize).length() + "s %s %s%n";
         try {
            System.out.printf(format,
                    file.isDir ? "d" : "-",
                    PosixFilePermissions.toString(Files.getPosixFilePermissions(FileSystem.fs.toPath())),
                    hardLinks[FileSystem.allFiles.indexOf(file)],
                    Files.readAttributes(Paths.get(FileSystem.fs.toURI()), PosixFileAttributes.class).owner().getName(),
                    Files.readAttributes(Paths.get(FileSystem.fs.toURI()), PosixFileAttributes.class).group().getName(),
                    fileSizes[FileSystem.allFiles.indexOf(file)],
                    new SimpleDateFormat("MMM dd HH:mm").format(Files.getLastModifiedTime(FileSystem.fs.toPath()).toMillis()),
                    file.name
            );
         } catch (IOException e) {
            e.printStackTrace();
         }
      });
   }

   /**
    * Copy a file from external file system into internal file system
    * @param extFileName name of file on your external (actual) system
    * @param intFileName name of file to name internally (in .notes file)
    */
   public void copyIn(String extFileName, String intFileName) {
      try {
         // if the provided name was invalid (contains symbols) - terminate program
         if (!intFileName.matches(Symbol.FILENAME_REGEX)) {
            Util.exitProgram("The filename you provided was invalid.");
         }

         // if the external file does not exist or is a directory - terminate program
         File extFile = new File(extFileName);
         if (!extFile.exists()) {
            Util.exitProgram("The provided external file does not exist.");
         } else if (extFile.isDirectory()) {
            Util.exitProgram("Copying directories is not supported.");
         }

         // remove file if it exists within the file system - to be overwritten
         if (Util.fileExists(intFileName)) {
            rm(intFileName);
         }

         // add new file to file system
         InternalFile intFile = new InternalFile(extFile, intFileName);
         intFile.addToFileSystem();

      } catch (Exception e) {
         e.printStackTrace();
      }

   }

   /**
    * Copy a file from internal file system into external file system
    * @param extFileName name of file on your external (actual) system
    * @param intFileName name of file to name internally (in .notes file)
    */
   public void copyOut(String intFileName, String extFileName) {
      try {
         InternalFile intFile = Util.getFile(intFileName);
         if (intFile.isDir) {
            Util.exitProgram("Copying directories is not supported.");
         }
         File extFile = new File(extFileName);


         // file is encoded - decode and write to file
         if (intFile.isEncoded) {
            FileOutputStream fos = new FileOutputStream(extFile);
            StringBuilder fileData = new StringBuilder();
            // iterate each line in data excluding first line (shebang line)
            for (int i = 1; i < intFile.data.size(); i++) {
               fileData.append(intFile.data.get(i));
            }
            // decode text and write to file
            String sbStr = fileData.toString();
            fos.write(Base64.getDecoder().decode(sbStr));
         // file is regular file - simply write to file
         } else {
            // initialise writer for external file
            PrintWriter extWriter = new PrintWriter(new BufferedWriter(new FileWriter(extFileName, false)));
            for (String s : intFile.data) {
               extWriter.println(s);
            }
            extWriter.flush();
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
      // add "/" to end of directory name if it does not contain it
      if (!dirName.endsWith("/")) {
         dirName += "/";
      }
      if (!Util.fileExists(dirName)) {
         // make full directory path and any subdirectories that do not already exist
         Util.recursiveCheckDirs(dirName, 0);
      } else {
         Util.exitProgram(dirName + " already exists within the file system.");
      }
   }

   /**
    * Remove file/directory from the system by adding an ignore symbol in front of the
    * respective lines within the file system
    * @param fileName name of the internal file/directory to remove
    */
   public void rm(String fileName) {
      InternalFile toDelete = Util.getFile(fileName);
      PrintWriter extWriter = null;
      File tempFile = new File(Symbol.TEMP_FILE_NAME);

      try {
         // prepare temporary file for writing
         extWriter = new PrintWriter(new BufferedWriter(new FileWriter(tempFile, true)));
         // prepare scanner on file system
         PeekableScanner sc = new PeekableScanner(FileSystem.fs);

         // iterate through each line in the file system
         String currLine;
         while (sc.hasNextLine()) {
            currLine = sc.nextLine();
            // delete a file
            if (!toDelete.isDir) {
               // check if the currently scanned item should be deleted
               if (currLine.substring(1).equals(toDelete.name)) {
                  // rewrite the line to include an ignore symbol in front
                  extWriter.println(Symbol.IGNORE + toDelete.name);
                  // iterate each of the lines of data and include an ignore symbol
                  for (String line : toDelete.data) {
                     extWriter.println(Symbol.IGNORE + line);
                     sc.nextLine();
                  }
               // the currently scanned line is not to be deleted - print it as it currently is
               } else {
                  extWriter.println(currLine);
               }
            // delete a directory (recursively)
            } else {
               // if the current line begins with the same path
               if (currLine.substring(1).startsWith(toDelete.name)) {
                  // check for a file within the directory
                  if (currLine.startsWith(Symbol.FILE)) {
                     // iterate through its data and add the ignore symbol
                     while (sc.hasNextLine() && sc.peek().startsWith(Symbol.DATA)) {
                        extWriter.println(Symbol.IGNORE + currLine.substring(1));
                        currLine = sc.nextLine();
                     }
                  }
                  // rewrite the line to include an ignore symbol in front
                  extWriter.println(Symbol.IGNORE + currLine.substring(1));
               // the currently scanned line is not to be deleted - print it as it currently is
               } else {
                  extWriter.println(currLine);
               }
            }
         }
         sc.close();
         extWriter.flush();
         extWriter.close();
         // delete current file system and replace with the temporary file
         FileSystem.fs.delete();
         tempFile.renameTo(FileSystem.fs);
         FileSystem.out = new PrintWriter(new BufferedWriter(new FileWriter(FileSystem.fs.getPath(), true)));
      } catch (IOException e) {
         System.err.println("There was a problem with opening the file.");
         e.printStackTrace();
      }
   }

   /**
    * Iterate through the lines in the file system and remove any lines beginning with the
    * ignore symbol (#)
    */
   public void defrag() {
      Util.treeSort();
      Util.rewriteNotesFile();
   }

}