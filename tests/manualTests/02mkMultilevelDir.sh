#! /usr/bin/sh
cp 02mkMultilevelDir.notes 02mkMultilevelDirTemp.notes
../../VSFS mkdir 02mkMultilevelDirTemp.notes dir1/dir2/dir3/dir4/
cp 02mkMultilevelDirTemp.notes 02mkMultiLevelDir.out
rm 02mkMultilevelDirTemp.notes
