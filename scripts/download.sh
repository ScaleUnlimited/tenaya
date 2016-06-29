#!/bin/bash

log=true;

if [ -z $TENAYA_HOME ]; then
	export TENAYA_HOME=$HOME/.tenaya;
fi

tmp=$TENAYA_HOME/tmp;
cache=$TENAYA_HOME/cache;
statuses=$TENAYA_HOME/statuses;

if [ -z $1 ]; then
	output=$(realpath .)/fasta;
else
	output=$1;
fi

mkdir -p $tmp;
mkdir -p $cache;
mkdir -p $statuses;
mkdir -p $output;
rm -rf $tmp/*;

i=0;
while read run; do
  i=$((i+1));
	if [ -z $run ]; then
		break;
	fi
	# Remove surrounding whitespace
	run="$(echo -e "${run}" | tr -d '[[:space:]]')";
	runs[i]=$run;
done

if [ ! -f $cache/downloads.txt ]; then
	touch $cache/downloads.txt;
fi

list=$(cat $cache/downloads.txt);
for run in "${runs[@]}"; do
	runpath=$cache/$run.sra;
	if [ -z "$(echo "$list" | grep $run)" ]; then
		if [ $log = true ]; then echo "Starting download into $runpath"; fi
		certpath=$(realpath ./asperaweb_id_dsa.openssh);
		if [ ! -f $certpath ]; then
			echo "Tool failed: aspera certificate not found";
			exit 1;
		fi
		ascp -i $certpath -k2 -QTr -l200m anonftp@ftp.ncbi.nlm.nih.gov:/sra/sra-instant/reads/ByRun/sra/${run:0:3}/${run:0:6}/${run}/${run}.sra $runpath > "$statuses/$run.log" &
	else
		if [ $log = true ]; then echo "Found cached version at $runpath"; fi
	fi
done

while true; do
  running=0;
	for run in "${runs[@]}"; do
		line=$(cat "$statuses/$run.log");
		if [ "$(echo $line | grep Completed)" ]; then
			if [ $log = true ]; then echo -ne "$run: Done"; fi
			echo "$run" >> $cache/downloads.txt;
		elif [ "$(echo "$line" | grep skipped)" ]; then
			if [ $log = true ]; then echo -ne "$run: Skipped"; fi
		elif [ "$(echo $line | grep Error)" ]; then
			if [ $log = true ]; then echo -ne "$run: Error"; fi
			echo $line > $cache/$run.sra.aspx;
		else
			running=$((running+1));
			echo -ne "$line";
		fi
		echo -e "\033[0K\r";
	done
	if [ $log = true ]; then echo -e "Run Count: $running\033[0K\r"; fi
	if [ $log = true ]; then echo -ne "\e["$((i+1))"A"; fi
	if [ $running -eq 0 ]; then
		break;
	fi
	sleep 1;
done

if [ $log = true ]; then echo "All downloads complete"; fi

for run in "${runs[@]}"; do
	runpath=$cache/$run.sra;
	if [ -f "$runpath.aspx" ]; then
		if [ $log = true ]; then echo "$run did not correctly download"; fi
	else
		if [ $log = true ]; then echo "Generating fasta dump for $runpath"; fi
		fastq-dump --fasta --stdout $runpath | gzip --stdout > $output/$run.fasta.gz &
	fi
done

wait
# if [ $log = true ]; then echo "All dumping complete"; fi
#
# if [ $log = true ]; then echo "Concatenating fasta files and writing to $output"; fi
# if [ $output == --stdout ]; then
# 	cat $tmp/*.fasta;
# else
# 	cat $tmp/*.fasta > $output;
# fi
rm -rf $tmp/*;
