#!/bin/bash

log=true;

if [ -z $TENAYA_HOME ]; then
	export TENAYA_HOME=$HOME/.tenaya;
fi

# tmp=$TENAYA_HOME/tmp;
cache=$TENAYA_HOME/cache;
logs=$TENAYA_HOME/logs;

if [ -z $1 ]; then
	output=$(realpath .)/fasta_data;
else
	output=$1;
fi

# mkdir -p $tmp;
mkdir -p $cache;
mkdir -p $logs;
# rm -rf $tmp/*;


certpath=$TENAYA_HOME/cert.openssh;

cat > $certpath <<- AsperaCertificate
-----BEGIN DSA PRIVATE KEY-----
MIIBuwIBAAKBgQDkKQHD6m4yIxgjsey6Pny46acZXERsJHy54p/BqXIyYkVOAkEp
KgvT3qTTNmykWWw4ovOP1+Di1c/2FpYcllcTphkWcS8lA7j012mUEecXavXjPPG0
i3t5vtB8xLy33kQ3e9v9/Lwh0xcRfua0d5UfFwopBIAXvJAr3B6raps8+QIVALws
yeqsx3EolCaCVXJf+61ceJppAoGAPoPtEP4yzHG2XtcxCfXab4u9zE6wPz4ePJt0
UTn3fUvnQmJT7i0KVCRr3g2H2OZMWF12y0jUq8QBuZ2so3CHee7W1VmAdbN7Fxc+
cyV9nE6zURqAaPyt2bE+rgM1pP6LQUYxgD3xKdv1ZG+kDIDEf6U3onjcKbmA6ckx
T6GavoACgYEAobapDv5p2foH+cG5K07sIFD9r0RD7uKJnlqjYAXzFc8U76wXKgu6
WXup2ac0Co+RnZp7Hsa9G+E+iJ6poI9pOR08XTdPly4yDULNST4PwlfrbSFT9FVh
zkWfpOvAUc8fkQAhZqv/PE6VhFQ8w03Z8GpqXx7b3NvBR+EfIx368KoCFEyfl0vH
Ta7g6mGwIMXrdTQQ8fZs
-----END DSA PRIVATE KEY-----
AsperaCertificate

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
		ascp -i $certpath -k1 -Tr -l200m anonftp@ftp.ncbi.nlm.nih.gov:/sra/sra-instant/reads/ByRun/sra/${run:0:3}/${run:0:6}/${run}/${run}.sra $runpath | grep --line-buffered -P ".*" &> "$logs/$run.log" &
	else
		if [ $log = true ]; then echo "Found cached version at $runpath"; fi
	fi
done

while true; do
  running=0;
	i=0;
	for run in "${runs[@]}"; do
		line=$(cat "$logs/$run.log");
		if [ "$(echo $line | grep Completed)" ]; then
			if [ $log = true ]; then echo -ne "$run: Done"; fi
		elif [ "$(echo "$line" | grep skipped)" ]; then
			if [ $log = true ]; then echo -ne "$run: Skipped"; fi
		elif [ "$(echo $line | grep Error)" ]; then
			if [ $log = true ]; then echo -ne "$run: Error"; fi
			echo $line > $cache/$run.sra.aspx;
		else
			running=$((running+1));
			echo -ne "$line";
		fi
		i=$((i+1));
		echo -e "\033[0K\r";
	done
	if [ $log = true ]; then echo -e "Run Count: $running\033[0K\r"; fi
	if [ $running -eq 0 ]; then
		break;
	fi
	if [ $log = true ]; then echo -ne "\e["$((i+1))"A"; fi
	sleep 1;
done

if [ $log = true ]; then echo "All downloads complete"; fi

mkdir -p $output;

for run in "${runs[@]}"; do
	runpath=$cache/$run.sra;
	if [ -f "$runpath.aspx" ]; then
		if [ $log = true ]; then echo "$run did not correctly download"; fi
	else
	  echo "$run" >> $cache/downloads.txt;
		if [ $log = true ]; then echo "Generating fasta dump for $runpath"; fi
		fastq-dump --fasta --stdout $runpath > $output/$run.fasta &
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
# rm -rf $tmp/*;
