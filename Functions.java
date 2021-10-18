import java.io.*;
import java.nio.file.Files;
import java.nio.file.attribute.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

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
      System.out.printf("%s%s %s %s %s %s %s %s%n",
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
            Driver.exitProgram("This file already exists within the file system.");
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
               Driver.exitProgram("Could not create directory " + intFileName + ".");
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
      // add "/" to end of directory name if it does not contain it
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
         Driver.exitProgram(dirName + " already exists within the file system.");
      }
   }

   /**
    * Remove file/directory from the system by adding an ignore symbol in front of the
    * respective lines within the file system
    * @param fileName name of the internal file/directory to remove
    */
   public void rm(String fileName) {
      System.out.println("REMOVING FILE " + fileName);
      InternalFile toDelete = FileSystem.getFile(fileName);
      if (toDelete == null) {
         Driver.exitProgram("The provided file was not found.");
      }
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
               // TODO: files that start with this name
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
//                  String subDir = currLine.substring(currLine.indexOf("/") + 1);
//                  if (subDir.length() > 0) {
//                     System.out.println("RECURSIVELY CALLING RM ON " + subDir);
//                     rm(subDir);
//                  }
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
      } catch (IOException e) {
         System.err.println("There was a problem with opening the file.");
         e.printStackTrace();
      }
   }

   public void treeSort() {
      // sort all internal files in reverse alphabetical order
      FileSystem.allFiles.sort(Comparator.comparing(internalFile -> internalFile.name.toLowerCase()));

      // split all internal files into directories and files (each will end up sorted)
      ArrayList<InternalFile> allDirs = new ArrayList<>();
      ArrayList<InternalFile> allFiles = new ArrayList<>();
      for (InternalFile internalFile : FileSystem.allFiles) {
         if (internalFile.isDir) {
            allDirs.add(internalFile);
         } else {
            allFiles.add(internalFile);
         }
      }

      // new order of internal files
      ArrayList<InternalFile> newFileStructure = new ArrayList<>();

      // recursively sort each directory
      for (InternalFile dir : allDirs) {
         recursiveTreeSort(dir, allDirs, allFiles, newFileStructure);
      }

      // add any remaining root files (that have not yet been added)
      for (InternalFile remainingFile : allFiles) {
         if (!newFileStructure.contains(remainingFile)) {
            newFileStructure.add(remainingFile);
         }
      }

      for (InternalFile intFile : newFileStructure) {
         System.out.println(intFile.name);
      }

   }

   public void recursiveTreeSort(InternalFile currFile, ArrayList<InternalFile> allDirs,
                                 ArrayList<InternalFile> allFiles, ArrayList<InternalFile> newFileStructure) {
      // add current file to new structure
      if (!newFileStructure.contains(currFile)) {
         newFileStructure.add(currFile);
      }

      // find any subdirectories of current file (directory)
      for (InternalFile subDir : allDirs) {
         // subdirectory found
         if (subDir.name.startsWith(currFile.name) && !newFileStructure.contains(subDir)) {
            newFileStructure.add(subDir);
            recursiveTreeSort(subDir, allDirs, allFiles, newFileStructure);
         }
      }
      // find any files in current directory
      for (InternalFile file : allFiles) {
         // file belongs in current file (current subdirectory)
         if (file.name.startsWith(currFile.name) && !file.name.substring(currFile.name.length()).contains("/") && !newFileStructure.contains(file)) {
            newFileStructure.add(file);
         }
      }
   }

   /**
    * Iterate through the lines in the file system and remove any lines beginning with the
    * ignore symbol (#)
    */
   public void defrag() {
      treeSort();
      PrintWriter extWriter = null;
      File tempFile = new File(Symbol.TEMP_FILE_NAME);

      try {
         // prepare temporary file for writing
         extWriter = new PrintWriter(new BufferedWriter(new FileWriter(tempFile, true)));
         extWriter.println(Symbol.HEADER_TAG);

         for (InternalFile file : FileSystem.allFiles) {
            // print initial prefix for file ("=" for directory, "@" for file)
            if (file.isDir) {
               extWriter.print(Symbol.DIR);
            } else {
               extWriter.print(Symbol.FILE);
            }

            // print the name of the file
            extWriter.println(file.name);

            // print the data of the file
            if (file.data != null) {
               for (String s : file.data) {
                  extWriter.println(Symbol.DATA + s);
               }
            }
         }

         extWriter.flush();
         extWriter.close();
         // delete current file system and replace with the temporary file
         FileSystem.fs.delete();
         tempFile.renameTo(FileSystem.fs);
      } catch (IOException e) {
         System.err.println("There was a problem with opening the file.");
         e.printStackTrace();
      }
   }

   public void index() {
      Driver.exitProgram("No implementation required.");
   }
}