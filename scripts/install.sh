#!/bin/bash

echo "Installing SRA Toolkit";
wget http://ftp-trace.ncbi.nlm.nih.gov/sra/sdk/2.6.3/sratoolkit.2.6.3-ubuntu64.tar.gz -o sratoolkit.tar.gz;
tar -xvzf sratoolkit.tar.gz;
PATH=$PATH:$(pwd)/sratoolkit/bin;

echo "Install ascp (Aspera executable)";
wget http://download.asperasoft.com/download/sw/ascp-client/3.5.4/ascp-install-3.5.4.102989-linux-64.sh -O /tmp/ascp-install.sh;
sudo chmod 755 /tmp/ascp-install.sh;
sudo /tmp/ascp-install.sh;
rm /tmp/ascp-install.sh;
