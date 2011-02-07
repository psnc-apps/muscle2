#!/bin/bash
#run merge.sh folder new_folder new_filename

if [ $# -ne 3 ]
then
  echo "Usage: merge.sh OLD_FOLDER/ NEW_FOLDER NEW_FILENAME"
  exit 
fi

PATTERN=$1'*.events'
OLD_FOLDER=$1
NEW_FOLDER=$2
FILENAME=$3

if [ ! -d "$NEW_FOLDER" ]; then
	echo "Creating "$NEW_FOLDER
	mkdir $NEW_FOLDER
fi

KERNEL=$(ls $OLD_FOLDER | head -n 1 | cut -d '.' -f 1)
for f in $(ls -l $PATTERN | tr -s ' ' '\t' | sort -n -r -k 5 | cut -f 8 | head -n  `find  ./$OLD_FOLDER/*.otf | cut -d '.' -f 2 | tr -d '/' | wc -l ` | cut -d '/' -f 2  )
do
#	echo $f
	POSTFIX=$(echo $f | cut -d '.' -f 2,3 )
#	echo $POSTFIX
	cp $OLD_FOLDER/$f $NEW_FOLDER/$FILENAME".$POSTFIX"
	echo $OLD_FOLDER/$f " -> " $NEW_FOLDER/$FILENAME".$POSTFIX"
done

cp $OLD_FOLDER/$KERNEL".1.events" $NEW_FOLDER/$FILENAME".1.events"
echo $OLD_FOLDER/$KERNEL".1.events" " -> " $NEW_FOLDER/$FILENAME".1.events"

cp $OLD_FOLDER/$KERNEL".0.def" $NEW_FOLDER/$FILENAME".0.def"
echo $OLD_FOLDER/$KERNEL".0.def" " -> " $NEW_FOLDER/$FILENAME".0.def"

cp $OLD_FOLDER/$KERNEL".1.def" $NEW_FOLDER/$FILENAME".1.def"
echo $OLD_FOLDER/$KERNEL".1.def" " -> " $NEW_FOLDER/$FILENAME".1.def"

cp $OLD_FOLDER/$KERNEL".otf" $NEW_FOLDER/$FILENAME".otf"
echo $OLD_FOLDER/$KERNEL".otf" " -> " $NEW_FOLDER/$FILENAME".otf"
