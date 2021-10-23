import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;

/**
 * Class containing utility methods for .notes file systems
 */
public class Util {
   /**
    * Prints a message to std error and terminate with exit code 1
    * @param message Error message to print to std error
    */
   public static void exitProgram(String message) {
      System.err.println(message + " Terminating program.");
      System.exit(1);
   }

   /**
    * Creates sub-directories (if they do not exist) for a given file or directory
    * @param fullPath the path to create sub-directories for
    * @param prevIndex the previous index of the slash - default 0, used for recursive calculation
    */
   public static void recursiveCheckDirs(String fullPath, int prevIndex) {
      // find index of the next sub-directory
      int newIndex = fullPath.indexOf("/", prevIndex+1)+1;
      // if there is a next sub-directory (we have not yet reached the end)
      if (newIndex != 0) {
         // string of currently scanned sub-directory
         String subDir = fullPath.substring(0, newIndex);
         // if the currently scanned sub-directory does not exist
         if (!fileExists(subDir)) {
            // add sub-directory to file system
            FileSystem.allFiles.add(new InternalFile(subDir));
            writeLineToFile(Symbol.DIR + subDir);
         }
         // check the next sub-directory in the full path
         recursiveCheckDirs(fullPath, newIndex);
      }
   }

   /**
    * Check the existence of a given internal file based on the name of it
    * @param fileName name of the file to check
    * @return true if the file exists within the internal file system, false otherwise
    */
   public static boolean fileExists(String fileName) {
      for (InternalFile file : FileSystem.allFiles) {
         if (file.name.equals(fileName)) {
            return true;
         }
      }
      return false;
   }

   /**
    * Get the internal file from a given name
    * @param fileName name of the file to be retrieved
    * @return the internal file associated with the provided file name if it exists, null if it does not exist
    */
   public static InternalFile getFile(String fileName) {
      for (InternalFile file : FileSystem.allFiles) {
         if (file.name.equals(fileName)) {
            return file;
         }
      }
      // customise error message if file is not found
      if (fileName.endsWith("/")) {
         exitProgram("The provided directory was not found.");
      } else {
         exitProgram("The provided file was not found.");
      }
      // dummy return - program will not reach here
      return null;
   }

   /**
    * Append to the file system notes file with no newline
    * @param text text to be appended to the file system
    */
   public static void writeToFile(String text) {
      if (text.length() > Symbol.MAX_CHARS) {
         text = text.substring(0, Symbol.MAX_CHARS) + "\n";
      }
      FileSystem.out.print(text);
   }

   /**
    * Append to the file system notes file with a newline
    * @param text text to be appended to the file system
    */
   public static void writeLineToFile(String text) {
      if (text.length() > Symbol.MAX_CHARS) {
         text = text.substring(0, Symbol.MAX_CHARS) + "\n";
      }
      FileSystem.out.println(text);
   }

   /**
    * Sort all files into tree-like structure
    */
   public static void treeSort() {
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

      FileSystem.allFiles = newFileStructure;
   }

   /**
    * Recursive helper method to sort files into tree structure
    * @param currFile file to find parent directories for
    * @param allDirs array list of all directories
    * @param allFiles array list of all files
    * @param newFileStructure array list to store organised structure
    */
   private static void recursiveTreeSort(InternalFile currFile, ArrayList<InternalFile> allDirs,
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
    * Re-write notes file according to internal files in FileSystem.allFiles array
    */
   public static void rewriteNotesFile() {
      try {
         // prepare temporary file for writing
         PrintWriter extWriter = null;
         File tempFile = new File(Symbol.TEMP_FILE_NAME);
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


}
