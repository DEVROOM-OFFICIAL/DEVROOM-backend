#!/bin/bash

student_id=${student}
class_id=${class}
username="$student_id"-"$class_id"

sudo apt update -qq
sudo apt install -qq -y build-essential curl software-properties-common openssh-client

sudo add-apt-repository ppa:deadsnakes/ppa
sudo apt update -qq
sudo apt install -qq -y python3.11 python3.11-venv python3.11-distutils

echo "Configuring Python 3.11 alternatives..."
sudo update-alternatives --install /usr/bin/python3 python3 /usr/bin/python3.11 1

# Check if Python 3.11 is available and set it as default
PYTHON_PATH=$(which python3.11)
if [ -n "$PYTHON_PATH" ]; then
    echo "Setting Python 3.11 as the default Python 3...=============================================="
    sudo update-alternatives --set python3 /usr/bin/python3.11
else
    echo "Python 3.11 not found in alternatives. Please check installation.==========================="
    exit 1
fi

echo "Exporting Python environment variable..."
echo 'export PYTHON=/usr/bin/python3.11' >> ~/.bashrc
source ~/.bashrc

# WeTTY 설치 및 실행
su - $username <<EOF
python3 --version

# 필수 패키지 및 WeTTY 설치
curl -sL https://raw.githubusercontent.com/nvm-sh/nvm/master/install.sh | bash && source ~/.profile
source ~/.nvm/nvm.sh
nvm install 20
node -v

# SSL 설정
#mkdir -p ~/.ssl
#openssl req -x509 -nodes -days 1095 -newkey ec:<(openssl ecparam -name secp384r1) -subj "/C=GB/ST=None/L=None/O=None/OU=None/CN=None" -out ~/.ssl/wetty.crt -keyout ~/.ssl/wetty.key
#chmod 700 ~/.ssl
#chmod 644 ~/.ssl/wetty.crt
#chmod 600 ~/.ssl/wetty.key

# SSH 키 설정
#mkdir -p ~/.ssh
#ssh-keygen -q -C "wetty-keyfile" -t ed25519 -N '' -f ~/.ssh/wetty 2>/dev/null <<< y >/dev/null
#cat ~/.ssh/wetty.pub >> ~/.ssh/authorized_keys
#chmod 700 ~/.ssh
#chmod 644 ~/.ssh/authorized_keys
#chmod 600 ~/.ssh/wetty

# WeTTY 설치 및 실행
mkdir -p ~/bin && source ~/.profile
npm -g i wetty@2.5.0

wetty -h

# 접속 URL 출력
echo https://$(curl -s4 icanhazip.com):3000

# WeTTY 시작
wetty --host 0.0.0.0 --port 3000 --title $username --base / --ssh-host localhost --ssh-user $username --ssh-port 22 --ssh-auth password

EOF