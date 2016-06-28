#!/bin/bash
tenaya=$HOME/.tenaya;
tmp=$tenaya/tmp;
cache=$tenaya/cache;
if [ -z $1 ]; then
	output=$(realpath .)/bundled.fastq.gz;
else
	output=$1;
fi
mkdir -p $tenaya;
mkdir -p $tmp;
mkdir -p $cache;
rm -rf $tmp/*;
i=0;
while read run; do
  i=$((i+1));
	if [ -z $run ]; then
		break;
	fi
	# Remove surrounding whitespace
	run="$(echo -e "${run}" | tr -d '[[:space:]]')";
	echo "Queueing $run";
	runs[i]=$run;
done
for run in "${runs[@]}"; do
	runpath=$cache/$run.sra;
	if [ ! -f $runpath ]; then
		echo "Starting download into $runpath";
		certpath=$(realpath ./asperaweb_id_dsa.openssh);
		if [ ! -f $certpath ]; then
			echo "Tool failed: aspera certificate not found";
			exit 1;
		fi
		ascp -i $certpath -k1 -QTr -l200m anonftp@ftp.ncbi.nlm.nih.gov:/sra/sra-instant/reads/ByRun/sra/${run:0:3}/${run:0:6}/${run}/${run}.sra $runpath &
	else
		echo "Found cached version at $runpath";
	fi
done
wait
for run in "${runs[@]}"; do
	runpath=$cache/$run.sra;
	echo "Generating FASTQ dump for $runpath";
	fastq-dump $runpath -O $tmp;
done
echo "Concatenating FASTQ files and writing to $output";
if [ $output == --stdout ]; then
	cat $tmp/*.fastq;
else
	cat $tmp/*.fastq > $output;
fi
rm -rf $tmp/*;
