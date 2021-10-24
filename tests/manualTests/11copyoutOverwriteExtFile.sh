#! /usr/bin/sh
cp 11copyoutOverwriteExtFile.txt 11copyoutOverwriteExtFileTemp.txt
../../VSFS copyout 11copyoutOverwriteExtFile.notes overwriteFile 11copyoutOverwriteExtFileTemp.txt
cp 11copyoutOverwriteExtFileTemp.txt 11copyoutOverwriteExtFile.out
rm 11copyoutOverwriteExtFileTemp.txt
