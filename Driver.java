public class Driver {
   public static void main(String[] args) {

      Functions func = new Functions();
      if (args.length >= 2 && args.length <= 4) {
         FileSystem.initialiseFS(args[1]);
         if (FileSystem.fs == null) {
            Util.exitProgram("The file system could not be loaded.");
         }
      }

      if (args.length == 2) {
         switch (args[0].toLowerCase()) {
            case "list":
               func.list();
               break;
            case "defrag":
               func.defrag();
               break;
            case "index":
               Util.exitProgram("No implementation required.");
               break;
            default:
               Util.exitProgram("The command you entered was not valid.");
         }
      } else if (args.length == 3) {
         switch (args[0].toLowerCase()) {
            case "rm":
               String fileName = args[2];
               if (fileName.endsWith("/")) {
                  Util.exitProgram("A directory name was provided. Please use the rmdir command to remove directories.");
               } else {
                  func.rm(fileName);
               }
               break;
            case "rmdir":
               String dirName = args[2].endsWith("/") ? args[2] : args[2] + "/";
               func.rm(dirName);
               break;
            case "mkdir":
               func.mkDir(args[2]);
               break;
            default:
               Util.exitProgram("The command you entered was not valid.");
         }
      } else if (args.length == 4) {
         switch (args[0].toLowerCase()) {
            case "copyin":
               func.copyIn(args[2], args[3]);
               break;
            case "copyout":
               func.copyOut(args[2], args[3]);
               break;
            default:
               Util.exitProgram("The command you entered was not valid.");
         }
      } else {
         Util.exitProgram("An incorrect number of arguments was provided.");
      }
      FileSystem.closeFS();
   }

}
