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
         } else if (fileName.endsWith(".notes.gz")) {
            fileSystem = new File(fileName);
            Util.decrompressFile(fileSystem);
         } else {
            fileSystem = new File(fileName + ".notes");
         }

         // terminate program if the file is not found
         if (!fileSystem.exists()) {
            Util.exitProgram("The specified file system does not exist.");
         }

         out = new PrintWriter(new BufferedWriter(new FileWriter(fileSystem.getPath(), true)));
         fs = fileSystem;

         // set up scanner for given file system
         sc = new PeekableScanner(fileSystem);

         // ensure first line is the correct format (otherwise terminate)
         if (!sc.hasNextLine()) {
            Util.exitProgram("The specified file system is empty.");
         } else if (!sc.nextLine().equals(Symbol.HEADER_TAG)) {
            Util.exitProgram("File system format is incorrect. It should begin with \"NOTES V1.0\".");
         }

         // prepare file system for being written/appended to
         initialiseInternalFiles();

         // create any directories that do not exist
         for (int i = 0; i < allFiles.size(); i++) {
            Util.recursiveCheckDirs(allFiles.get(i).name, 0);
         }

      } catch (Exception e) {
         e.printStackTrace();
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

         // if current line has data - skips over blank lines
         if (currLine.trim().length() > 0) {
            // file already exists within the internal file system
            if (Util.fileExists(currLine.substring(1))) {
               Util.exitProgram("A duplicate file (" + currLine.substring(1) + ") was found whilst parsing the file system.");
            }
            // getting here means file does not already exist in the internal file system

            // truncate line if it exceeds 255 characters - used for file names and directories
            if (currLine.length() >= Symbol.MAX_CHARS) {
               // mark the file system for rewriting - the notes file will truncate the data
               reWrite = true;
               // preserve the "/" or ".xxx" extension at the end of a file after truncating
               int lastDotIndex = currLine.lastIndexOf(".");
               if (currLine.startsWith(Symbol.DIR)) {
                  currLine = currLine.substring(0, Symbol.MAX_CHARS - 1) + "/";
               } else {
                  String extension = currLine.substring(lastDotIndex);
                  currLine = currLine.substring(0, Symbol.MAX_CHARS - extension.length()) + extension;
               }
            }

            // handling of invalid names
            // multiple dots in a row in file
            if (currLine.contains("..")) {
               Util.exitProgram("The file " + currLine + " is not correctly formatted (mustn't be named as \"..\").");
            // files named "."
            } else if (currLine.contains("/.") || currLine.contains("/./") || currLine.endsWith(".")) {
               Util.exitProgram("The file " + currLine + " is not correctly formatted (mustn't be named as \".\").");
            // empty directory names
            } else if (currLine.contains("//")) {
               Util.exitProgram("A directory with no name was detected.");
            // non-valid characters
            } else if (!currLine.startsWith(Symbol.IGNORE) && !(currLine.substring(1)).matches(Symbol.FILENAME_REGEX)) {
               Util.exitProgram("An invalid filename was detected.");
            }

            // read a single file
            if (currLine.startsWith(Symbol.FILE)) {
               // title of the file
               String currFileName = currLine.substring(1);
               // terminate if file name ends with "/"
               if (currFileName.endsWith("/")) {
                  Util.exitProgram("The file " + currFileName + " is not correctly formatted (mustn't end with a \"/\").");
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
                  Util.exitProgram("The directory " + currLine.substring(1) + " is not correctly formatted (must end with a \"/\").");
               }
               // getting here indicates the directory is formatted correctly
               String currDirName = currLine.substring(1);
               allFiles.add(new InternalFile(currDirName));
               // handle extraneous values
            } else if (!currLine.startsWith(Symbol.IGNORE)) {
               Util.exitProgram("An unknown file type was found by the compiler (" + currLine.charAt(0) + ").");
            }
         }
      }
      // rewrite notes file - a required change was detected
      if (reWrite) {
         Util.rewriteNotesFile();
      }
   }

   /**
    * Clean up the file system variables by closing the PrintWriter and Scanner
    */
   public static void closeFS() {
      try {
         sc.close();
         out.close();
         // compress file if it is required
         if (fs.getName().endsWith(".gz")) {
            Util.compressFile(fs);
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

}
