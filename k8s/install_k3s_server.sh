#!/bin/bash

: '
스크립트 권한 설정
sudo chmod 755 install_k3s_server.sh
sudo chmod 755 install_k3s_agent.sh

git email 설정
git config --global user.email "hdyang0686@naver.com"
git config --global user.name "Yanghyeondong"
'

# install k8s
echo "Install k3s..."
curl -sfL https://get.k3s.io | sh -s - --disable=traefik --write-kubeconfig-mode=644 > k3s_install.log

echo "Make k3s Token..."
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
echo $TOKEN > token_file

# 헬름을 위해 k3s 설정 파일 추출하기
kubectl config view --raw > ~/.kube/config

echo "Waiting for .kube directory to be created..."
while [ ! -d ~/.kube ]; do
    sleep 5
done

chmod 600 ~/.kube/config

# 클러스터의 특정 노드에 레이블 부여
kubectl label node $(kubectl get nodes -o jsonpath='{.items[0].metadata.name}') storage=dev-room-pv
# 레이블 셀렉터로 노드의 존재 확인
kubectl get nodes -l storage=dev-room-pv

# 스크립트를 사용한 헬름 설치
echo "Install Helm..."
curl https://raw.githubusercontent.com/helm/helm/master/scripts/get-helm-3 | bash  > Helm_install.log
# 헬름 버전 확인
helm version

# 차트에 들어갈 파일의 유효성을 검증
helm lint dev-room-k8s
# 차트 디렉터리에서 릴리스를 설치
helm install dev-room-k8s dev-room-k8s/
# 설치된 릴리스의 상태를 확인
helm ls