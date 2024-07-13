#!/bin/bash

: '
스크립트 권한 설정
sudo chmod 755 install_k3s_server.sh
sudo chmod 755 install_k3s_agent.sh
'

LINE="===================="

# install nfs
echo "${LINE} Install nfs... ${LINE}"
read -p "Enter the nfs address: " mynfs
sudo apt install -y nfs-common >> install.log 2>&1
sudo mkdir -p /nfs_devroom >> install.log
sudo mount ${mynfs} /nfs_devroom >> install.log
sudo chmod -R 777 /nfs_devroom
ls -al /nfs_devroom

echo "${LINE} Install k3s... ${LINE}"
read -p "Enter the K3S server address: " myserver
read -p "Enter the K3S node token: " mynodetoken
curl -sfL https://get.k3s.io | K3S_URL=https://${myserver}:6443 K3S_TOKEN=${mynodetoken} sh -s - > k3s_install.log