#! /usr/bin/sh
cp 13gzipFile.notes.gz 13gzipFileTemp.notes.gz
../../VSFS list 13gzipFileTemp.notes.gz 1> 13gzipFile.out 2>&1
rm 13gzipFileTemp.notes.gz
