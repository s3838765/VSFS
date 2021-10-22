import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;

public class FileSystem {
   public static File fs;
   public static PrintWriter out = null;
   private static PeekableScanner sc = null;
   public static ArrayList<InternalFile> allFiles;

   /**
    * Initialises file system by loading given .notes file from file name
    * @param fileName name of file in which to load the file system from
    */
   public static void initialiseFS(String fileName) {
      allFiles = new ArrayList<>();

      try {
         // creates file for filesystem whether you type the extension or not
         File fileSystem = null;
         if (fileName.endsWith(".notes")) {
            fileSystem = new File(fileName);
         } else {
            fileSystem = new File(fileName + ".notes");
         }

         if (!fileSystem.exists()) {
            Driver.exitProgram("The specified file system does not exist.");
         }

         out = new PrintWriter(new BufferedWriter(new FileWriter(fileSystem.getPath(), true)));
         fs = fileSystem;

         // set up scanner for given file system
         sc = new PeekableScanner(fileSystem);
         // ensure first line is the correct format (otherwise terminate)
         if (!sc.hasNextLine()) {
            Driver.exitProgram("The specified file system is empty.");
         } else if (!sc.nextLine().equals(Symbol.HEADER_TAG)) {
            Driver.exitProgram("File system format is incorrect. It should begin with \"NOTES V1.0\".");
         }

         // prepare file system for being written/appended to
         initialiseInternalFiles();

         // create any directories that do not exist
         for (int i = 0; i < allFiles.size(); i++) {
            recursiveCheckDirs(allFiles.get(i).name, 0);
         }

         // rewrite file with any changes

      } catch (Exception e) {
         e.printStackTrace();
         System.err.println(e.getMessage());
      }
   }

   /**
    * Iterate through the file system notes file and initialise all files and directories
    */
   private static void initialiseInternalFiles() {
      // iterate the file system and convert all data to java objects for easy access
      boolean reWrite = false;
      String currLine;
      while (sc.hasNextLine()) {
         currLine = sc.nextLine();

         if (currLine.contains("//")) {
            Driver.exitProgram("A directory with no name was detected.");
         }

         if (currLine.length() > 0) {
            // file already exists within the internal file system
            if (fileExists(currLine.substring(1))) {
               Driver.exitProgram("A duplicate file (" + currLine.substring(1) + ") was found whilst parsing the file system.");
            }
            // getting here means file does not already exist in the internal file system

            // truncate line if it exceeds 255 characters - used for file names and directories
            if (currLine.length() >= Symbol.MAX_CHARS) {
               int lastDotIndex = currLine.lastIndexOf(".");
               if (currLine.startsWith(Symbol.DIR)) {
                  currLine = currLine.substring(0, Symbol.MAX_CHARS - 1) + "/";
               } else {
                  String extension = currLine.substring(lastDotIndex);
                  currLine = currLine.substring(0, Symbol.MAX_CHARS - extension.length()) + extension;
               }
            }

            if (currLine.contains("..")) {
               Driver.exitProgram("The file " + currLine + " is not correctly formatted (mustn't be named as \"..\").");
            } else if (currLine.contains("/.") || currLine.contains("/./")) {
               Driver.exitProgram("The file " + currLine + " is not correctly formatted (mustn't be named as \".\").");
            }

            if (!currLine.startsWith(Symbol.IGNORE) && !(currLine.substring(1)).matches(Symbol.FILENAME_REGEX)) {
               Driver.exitProgram("An invalid filename was detected.");
            }

            // read a single file
            if (currLine.startsWith(Symbol.FILE)) {
               // title of the file
               String currFileName = currLine.substring(1);
               if (currFileName.endsWith("/")) {
                  Driver.exitProgram("The file " + currFileName + " is not correctly formatted (mustn't end with a \"/\").");
               }


               ArrayList<String> currFileData = new ArrayList<>();
               boolean isEncoded = sc.hasNextLine() && sc.peek().equals(Symbol.ENCODED_SHEBANG);

               // iterate through data of current file
               while (sc.hasNextLine() && sc.peek().startsWith(Symbol.DATA)) {
                  currLine = sc.nextLine();
                  // truncate line if it exceeds 255 characters
                  if (currLine.length() > Symbol.MAX_CHARS) {
                     currLine = currLine.substring(0, Symbol.MAX_CHARS);
                     reWrite = true;
                  }
                  currFileData.add(currLine.substring(1));
               }
               // add the file to allFiles to keep track of it
               allFiles.add(new InternalFile(currFileName, currFileData, isEncoded));
               // read a directory
            } else if (currLine.startsWith(Symbol.DIR)) {
               // terminate program if the directory is not formatted correctly
               if (!currLine.endsWith("/")) {
                  Driver.exitProgram("The directory " + currLine.substring(1) + " is not correctly formatted (must end with a \"/\").");
               }
               // getting here indicates the directory is formatted correctly
               String currDirName = currLine.substring(1);
               allFiles.add(new InternalFile(currDirName));
               // handle extraneous values
            } else if (!currLine.startsWith(Symbol.IGNORE)) {
               Driver.exitProgram("An unknown file type was found by the compiler (" + currLine.charAt(0) + ").");
            }
         }
      }
      if (reWrite) {
         Functions.rewriteNotesFile();
      }
   }

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
            allFiles.add(new InternalFile(subDir));
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
      for (InternalFile file : allFiles) {
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
      for (InternalFile file : allFiles) {
         if (file.name.equals(fileName)) {
            return file;
         }
      }
      Driver.exitProgram("The provided file was not found.");
      return null;
   }

   public static void removeFile(String fileName) {
      allFiles.removeIf(file -> file.name.equals(fileName));
   }

   /**
    * Clean up the file system variables by closing the PrintWriter and Scanner
    */
   public static void closeFS() {
      try {
         sc.close();
         out.flush();
         out.close();
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   /**
    * Append to the file system notes file with no newline
    * @param text text to be appended to the file system
    */
   public static void writeToFile(String text) {
      if (text.length() > Symbol.MAX_CHARS) {
         text = text.substring(0, Symbol.MAX_CHARS) + "\n";
      }
      out.print(text);
   }

   /**
    * Append to the file system notes file with a newline
    * @param text text to be appended to the file system
    */
   public static void writeLineToFile(String text) {
      out.println(text);
   }

   public static void delFromFile() {

   }

}
