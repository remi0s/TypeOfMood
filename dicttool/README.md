1)Find an language dump file from https://archive.org/ or http://opus.nlpl.eu/OpenSubtitles2018.php

2)Edit sort.sh to match the file downloaded. For tar.gz files use "zcat", for bz2 use "bzcat" else try "cat". And run it with ./sort.sh

3)After the wordlist is created use the parser.pl with command perl parser.pl filename

4)Add header information in the new wordlist file

5)Edit run.sh file to fit the filenames and then use it with ./run.sh command.


Have Fun!
