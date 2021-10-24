#! /usr/bin/sh
cp 13gzipFile.notes.gz 13gzipFileTemp.notes.gz
../../VSFS list 13gzipFileTemp.notes.gz 1> /dev/null 2>&1
cp 13gzipFileTemp.notes.gz 13gzipFile.out
rm 13gzipFileTemp.notes.gz
