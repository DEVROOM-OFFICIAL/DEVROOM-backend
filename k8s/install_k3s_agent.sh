#!/bin/bash

read -p "Enter the K3S server address: " myserver
read -p "Enter the K3S node token: " mynodetoken

curl -sfL https://get.k3s.io | K3S_URL=https://${myserver}:6443 K3S_TOKEN=${mynodetoken} sh -s - --disable=traefik --write-kubeconfig-mode=644 > k3s_install.log