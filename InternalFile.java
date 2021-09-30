import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

public class InternalFile {
   public String name;
   public boolean isDir;
   public ArrayList<String> data;

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

   public InternalFile(String name, ArrayList<String> data) {
      this.name = name;
      this.isDir = false;
      this.data = data;
   }

   public InternalFile(String name) {
      this.name = name;
      this.isDir = true;
      this.data = null;
   }

   public void addToFileSystem() {
      // print initial prefix for file
      if (this.isDir) {
         FileSystem.writeToFile(Symbol.DIR);
      } else {
         FileSystem.writeToFile(Symbol.FILE);
      }

      FileSystem.writeLineToFile(this.name);
      for (String s : data) {
         FileSystem.writeLineToFile(Symbol.DATA + s);
      }
//      FileSystem.writeLineToFile(SbData);
   }

   public void removeFileFromSystem() {

   }
}
