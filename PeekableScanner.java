import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * A scanner wrapper with functionality to peek at the next item
 *
 * @implNote derived from https://stackoverflow.com/a/4288861
 */
public class PeekableScanner {
   private Scanner scan;
   private String nextLine;

   public PeekableScanner(File source) throws FileNotFoundException {
      scan = new Scanner(source);
      nextLine = scan.hasNextLine() ? scan.nextLine() : null;
   }

   public boolean hasNextLine()
   {
      return (nextLine != null);
   }

   public String nextLine()
   {
      String current = nextLine;
      nextLine = (scan.hasNextLine() ? scan.nextLine() : null);
      return current;
   }

   public String peek()
   {
      return nextLine;
   }
}
