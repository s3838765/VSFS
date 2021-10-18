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
         out = new PrintWriter(new BufferedWriter(new FileWriter(fileSystem.getPath(), true)));
         fs = fileSystem;




         // set up scanner for given file system
         sc = new PeekableScanner(fileSystem);
         // ensure first line is the correct format (otherwise terminate)
         if (!sc.nextLine().equals(Symbol.HEADER_TAG)) {
            Driver.exitProgram("File system format is incorrect. It should begin with \"NOTES V1.0\".");
         }

         // prepare file system for being written/appended to
         initialiseInternalFiles();


//         for (InternalFile file : allFiles) {
         int numFiles = allFiles.size();
         for (int i = 0; i < numFiles; i++) {
            if (allFiles.get(i).isDir) {
               recursiveCheckDirs(allFiles.get(i).name, 0);
            }
         }


         //         System.out.println("OUTSIDE WHILE LOOP");
//         allFiles.forEach((file) -> {
//            System.out.println("PRINTING NEW FILE ---------------");
//            System.out.println(file.name);
//            System.out.println(file.isDir);
//            System.out.println(file.data);
//            System.out.println("---------------------------------");
//         });

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
      String nextLine;
      while (sc.hasNextLine()) {
         nextLine = sc.nextLine();
         // truncate line if it exceeds 254 characters (not 255 to allow for \n)
         if (nextLine.length() >= Symbol.MAX_CHARS) {
            nextLine = nextLine.substring(0, Symbol.MAX_CHARS);
         }
         // if file does not already exist in the internal file system
         if (!fileExists(nextLine.substring(1))) {
            // read a single file
            if (nextLine.startsWith(Symbol.FILE)) {
               // title of the file
               String currFileName = nextLine.substring(1);
               ArrayList<String> currFileData = new ArrayList<>();

               // iterate through data of current file
               while (sc.hasNextLine() && sc.peek().startsWith(Symbol.DATA)) {
                  nextLine = sc.nextLine();
                  // truncate line if it exceeds 254 characters (not 255 to allow for \n)
                  if (nextLine.length() >= Symbol.MAX_CHARS) {
                     nextLine = nextLine.substring(0, Symbol.MAX_CHARS);
                  }
                  currFileData.add(nextLine.substring(1));
               }
               // add the file to allFiles to keep track of it
               allFiles.add(new InternalFile(currFileName, currFileData));
//               System.out.println("Adding file " + currFileName);
            // read a directory
            } else if (nextLine.startsWith(Symbol.DIR)) {
               // ensure the directory ends with "/"
               if (nextLine.endsWith("/")) {
                  String currDirName = nextLine.substring(1);
                  // truncate line if it exceeds 254 characters (not 255 to allow for \n)
                  allFiles.add(new InternalFile(currDirName));
//                  System.out.println("Adding dir " + currDirName);
               // if the directory does not end with a "/", consider it incorrectly formatted
               } else {
                  Driver.exitProgram("The directory " + nextLine.substring(1) + " is not correctly formatted (must end with a \"/\").");
               }
            // handle comments/ignored lines (beginning with "#")
            } else if (nextLine.startsWith(Symbol.IGNORE)) {
//               System.out.println("A line was ignored by the compiler");
            // handle extraneous values
            } else {
               Driver.exitProgram("An unknown file type was found by the compiler (" + nextLine.charAt(0) + ").");
            }
         // file already exists within the internal file system
         } else {
            Driver.exitProgram("A duplicate file (" + nextLine.substring(1) + ") was found whilst parsing the file system.");
         }
      }
   }

   private static void recursiveCheckDirs(String fullPath, int prevIndex) {
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
      // TODO: files with same name as directory?
      for (InternalFile file : allFiles) {
         if (file.name.equals(fileName)) {
            return file;
         }
      }
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
      out.print(text);
   }

   /**
    * Append to the file system notes file with a newline
    * @param text text to be appended to the file system
    */
   public static void writeLineToFile(String text) {
      System.out.println("WRITING LINE TO FILE!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
      System.out.println(out);
      out.println(text);
   }

   public static void delFromFile() {

   }

}
