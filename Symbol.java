public class Symbol {
   public final static String FILE = "@";
   public final static String DIR = "=";
   public final static String DATA = " ";
   public final static String IGNORE = "#";
   public final static String HEADER_TAG = "NOTES V1.0";
   public final static String TEMP_FILE_NAME = "temp";
   public final static int MAX_CHARS = 255;
   // regex expression to check if characters match ascii - matches true if it is an ascii expression
   public final static String ASCII_CHECK_REGEX = "\\A\\p{ASCII}*\\z";
   public final static String ENCODED_SHEBANG = " !!b64-encoded";
}
