#!/bin/bash

echo "Installing SRA Toolkit";
sudo apt-get install sra-toolkit -y --force-yes;

echo "Install ascp (Aspera executable)";
wget http://download.asperasoft.com/download/sw/ascp-client/3.5.4/ascp-install-3.5.4.102989-linux-64.sh -O /tmp/ascp-install.sh;
sudo chmod 755 /tmp/ascp-install.sh;
sudo /tmp/ascp-install.sh;
rm /tmp/ascp-install.sh;
