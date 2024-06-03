#!/bin/bash

: '
스크립트 권한 설정
sudo chmod 755 install_k3s_server.sh
sudo chmod 755 install_k3s_agent.sh

git email 설정
git config --global user.email "hdyang0686@naver.com"
git config --global user.name "Yanghyeondong"
'

read -p "Enter the K3S server address: " myserver
read -p "Enter the K3S node token: " mynodetoken

curl -sfL https://get.k3s.io | K3S_URL=https://${myserver}:6443 K3S_TOKEN=${mynodetoken} sh -s - > k3s_install.log