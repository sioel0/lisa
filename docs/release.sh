#!/bin/bash

read -p "Release number: " tag
read -p "Key changes: " changes

releases=releases/index.md
tempfile=tmp.txt
script=`basename "$0"`

cat > $tempfile <<- EOM

### $tag

Key changes: $changes. More information in the GitHub [release](https://github.com/UniVE-SSV/lisa/releases/tag/v$tag).

Javadoc: [sdk](https://www.javadoc.io/doc/com.github.unive-ssv/lisa-sdk/$tag/index.html) - [core](https://www.javadoc.io/doc/com.github.unive-ssv/lisa-core/$tag/index.html) - [imp](https://www.javadoc.io/doc/com.github.unive-ssv/lisa-imp/$tag/index.html)
EOM

sed -i "/## Beta releases/ r $tempfile" $releases

rm $tempfile

git fetch
git merge origin/master
git add $releases
git commit -m "$script: Adding release $tag"
git push