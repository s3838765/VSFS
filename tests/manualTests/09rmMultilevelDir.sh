#! /usr/bin/sh
cp 09rmMultilevelDir.notes 09rmMultilevelDirTemp.notes
../../VSFS rmdir 09rmMultilevelDirTemp.notes dir1/dir2/
cp 09rmMultilevelDirTemp.notes 09rmMultilevelDir.out
rm 09rmMultilevelDirTemp.notes
