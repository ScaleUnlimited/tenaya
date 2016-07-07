#!/bin/bash
filelist=$1;
IFS=',' read -r -a files <<< "$filelist"
numfiles=${#files[@]};

groups=$2;
threads=$3;

threadgroup=$((threads/groups));
filegroup=$((numfiles/groups));

j=0;
for i in `seq 1 $groups`; do
  inputfile=${files[j]};
  j=$((j+1));
  for k in `seq 1 $((filegroup-1))`; do
    inputfile="$inputfile,${files[j]}";
    j=$((j+1));
  done
  java -Xmx8g -jar tenaya.jar generate --pid -i $inputfile -o $TENAYA_HOME/sigs/#id.sig -M 5000000000 -k 20 -c 1 -m partition -b 1048576 -q 10000 -t $threadgroup &
done

wait
