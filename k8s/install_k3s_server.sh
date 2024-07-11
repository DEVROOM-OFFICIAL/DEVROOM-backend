#!/bin/bash

: '
git config --global user.email "hdyang0686@naver.com"
git config --global user.name "Yanghyeondong"
git clone https://github.com/Yanghyeondong/DEV-ROOM
cd DEV-ROOM
git checkout -b develop remotes/origin/develop
git branch -a

cd k8s/
sudo chmod 755 install_k3s_server.sh
sudo chmod 755 install_k3s_agent.sh
./install_k3s_server.sh

========================================================

Gcp 인스턴스 설정

FileStore : devroom-k3s-nfs
-> 활성화, 인스턴스만들기
-> asia-northeast3
-> VPC default
-> 파일공유 이름 devroom_nfs
-> nfs 마운트 지점 확인하기 ex.) 10.180.81.34:/devroom_nfs

VM 인스턴스 : devroom-k3s-01
-> 네크워크 세부정보 보기
-> IP 주소 (외부고정IP 할당) : devroom-k3s-api
-> 기존 IP의 경우, 생성할때나 세부정보에서 네트워크 탭 확장
-> 방화벽 상단에 방화벽 규칙 만들기 0.0.0.0/0 tcp:3000, 6443, 37001-37500
-> 디스크만 교체도 가능
-> 기존 외부고정IP 활용시, 인스턴스 세부설정 
-> 네트워크 폴드 열어서 수정
-> clean 이미지 저장해놓기
'

LINE="===================="


# install nfs
echo "${LINE} Install nfs... ${LINE}"
read -p "Enter the nfs address: " mynfs
sudo apt install -y nfs-common >> install.log 2>&1
sudo apt install -y acl >> install.log 2>&1
sudo mkdir -p /dev-room >> install.log
sudo mount ${mynfs} /dev-room >> install.log
current_user=$(whoami)
sudo setfacl -R -d -m u:$current_user:rwx /dev-room
ls -al /dev-room

# install k8s
echo "${LINE} Install k3s... ${LINE}"
curl -sfL https://get.k3s.io | sh -s - --disable=traefik --write-kubeconfig-mode=644 >> install.log

echo "wait for 30s..."
sleep 30

echo "${LINE} Make k3s Token... ${LINE}"
# 기본 서비스 어카운트용 토큰을 보관할 시크릿을 생성한다.
kubectl apply -f - <<EOF
apiVersion: v1
kind: Secret
metadata:
  name: default-token
  annotations:
    kubernetes.io/service-account.name: default
type: kubernetes.io/service-account-token
EOF

# 토큰 컨트롤러가 해당 시크릿에 토큰을 기록할 때까지 기다린다.
while ! kubectl describe secret default-token | grep -E '^token' >/dev/null; do
  echo "waiting for token..." >&2
  sleep 1
done

# default 계정 권한 설정
kubectl create clusterrolebinding default-cluster-admin --clusterrole cluster-admin --serviceaccount default:default

# 토큰 값을 얻는다
TOKEN=$(kubectl get secret default-token -o jsonpath='{.data.token}' | base64 --decode)
echo $TOKEN > api-token_file

# 헬름을 위해 k3s 설정 파일 추출하기
kubectl config view --raw > ~/.kube/config

echo "Waiting for .kube directory to be created..."
while [ ! -d ~/.kube ]; do
    sleep 5
done

chmod 600 ~/.kube/config

# # 클러스터의 특정 노드에 레이블 부여
# kubectl label node $(kubectl get nodes -o jsonpath='{.items[0].metadata.name}') storage=dev-room-pv
# # 레이블 셀렉터로 노드의 존재 확인
# kubectl get nodes -l storage=dev-room-pv

# 스크립트를 사용한 헬름 설치
echo "${LINE} Install Helm... ${LINE}"
curl https://raw.githubusercontent.com/helm/helm/master/scripts/get-helm-3 | bash >> install.log

# 차트에 들어갈 파일의 유효성을 검증
helm lint dev-room-k8s >> install.log
# 차트 디렉터리에서 릴리스를 설치
helm install dev-room-k8s dev-room-k8s/ >> install.log
# 설치된 릴리스의 상태를 확인
helm ls

sudo cat /var/lib/rancher/k3s/server/node-token > node-token_file
