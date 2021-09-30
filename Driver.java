public class Driver {
   public static void main(String[] args) {
      /**
       * DIBFS list    FS
       * DIBFS defrag  FS
       * DIBFS index   FS
       * DIBFS mkdir   FS ID
       * DIBFS rm      FS IF
       * DIBFS rmdir   FS ID
       * DIBFS copyin  FS EF IF
       * DIBFS copyout FS IF EF
       */
      Functions func = new Functions();
      if (args.length >= 2 && args.length <= 4) {
         FileSystem.initialiseFS(args[1]);
         if (FileSystem.fs == null) {
            System.exit(1);
         }
      }
//      FileSystem.allFiles.forEach(f -> {
//         f.printFile();
//      });

      if (args.length == 2) {
         switch (args[0].toLowerCase()) {
            case "list":
               System.out.println("List command selected");
               func.list();
               break;
            case "defrag":
               System.out.println("defrag command selected");
               break;
            case "index":
               System.out.println("index command selected");
               func.index();
               break;
            default:
               System.out.println("The command you entered was not valid");
         }
      } else if (args.length == 3) {
         switch (args[0].toLowerCase()) {
            case "rm":
               System.out.println("rm command selected");
               break;
            case "rmdir":
               System.out.println("rmdir command selected");
               break;
            case "mkdir":
               System.out.println("mkdir command selected");
               func.mkDir(args[2]);
               break;
            default:
               System.out.println("The command you entered was not valid");
         }
      } else if (args.length == 4) {
         switch (args[0].toLowerCase()) {
            case "copyin":
               System.out.println("copyin command selected");
               func.copyIn(args[2], args[3]);
               break;
            case "copyout":
               System.out.println("copyout command selected");
               func.copyOut(args[2], args[3]);
               break;
            default:
               System.out.println("The command you entered was not valid");
         }
      } else {
         System.out.println("An incorrect number of arguments was provided.");
      }
      FileSystem.closeFS();
   }

}
