#! /usr/bin/sh
cp 06mkdirNoSlash.notes 06mkdirNoSlashTemp.notes
../../VSFS mkdir 06mkdirNoSlashTemp.notes dirname
cp 06mkdirNoSlashTemp.notes 06mkdirNoSlash.out
rm 06mkdirNoSlashTemp.notes
