import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

public class InternalFile {
   public String name;
   public boolean isDir;
   public ArrayList<String> data;

   /**
    * Add an internal file to the system using an external file; used when copying from external files
    * @param extFile a File object of the external file
    * @param intFileName the name you would like to associate with the internal file name
    */
   public InternalFile(File extFile, String intFileName) {
      try {
         Scanner sc = new Scanner(extFile);
         data = new ArrayList<>();
         while (sc.hasNextLine()) {
            data.add(sc.nextLine());
         }
         this.name = intFileName;
         this.isDir = extFile.isDirectory();
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   /**
    * Add an internal file to the system using the name and associated data; used for adding files
    * @param name
    * @param data
    */
   public InternalFile(String name, ArrayList<String> data) {
      this.name = name;
      this.isDir = false;
      this.data = data;
   }

   /**
    * Add an internal file to the system using only a name; used for adding directories
    * @param dirName the name of the directory to add to the internal file system
    */
   public InternalFile(String dirName) {
      this.name = dirName;
      this.isDir = true;
      this.data = null;
   }

   /**
    * Add the data of a given internal file to the file system notes file
    */
   public void addToFileSystem() {
      // print initial prefix for file ("=" for directory, "@" for file)
      if (this.isDir) {
         FileSystem.writeToFile(Symbol.DIR);
      } else {
         FileSystem.writeToFile(Symbol.FILE);
      }

      // print the name of the file
      FileSystem.writeLineToFile(this.name);

      // print the data of the file (if applicable: a directory will not contain any data)
      for (String s : data) {
         FileSystem.writeLineToFile(Symbol.DATA + s);
      }
   }

   public void removeFileFromSystem() {
   }

   public void printFile() {
      System.out.println("--------------------------");
      if (this.isDir) {
         System.out.println("Dirname: " + this.name);
      } else {
         System.out.println("Filename: " + this.name);
         for (String s : this.data) {
            System.out.println(s);
         }
      }
      System.out.println("--------------------------");
   }
}
