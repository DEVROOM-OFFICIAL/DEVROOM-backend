#!/bin/bash

# 패키지 업데이트 및 필요 패키지 설치
apt update -qq
apt install -qq -y sudo
apt install -qq -y curl

# 새로운 사용자 생성
student_id=${student}
class_id=${class}
username="$student_id"-"$class_id"

useradd -m "$username"
echo "$username:test" | chpasswd
usermod -aG sudo "$username"

# 볼륨 권한 설정
student_folder_path="/home/$username"
chmod -R 777 "$student_folder_path"

# user bash용 기본 설정
chsh -s /bin/bash $username
cp /etc/skel/.bashrc $student_folder_path/.bashrc
cp /etc/profile $student_folder_path/.profile

# .bash_profile 파일 생성
cat > $student_folder_path/.bash_profile <<EOF
if [ -f ~/.bashrc ]; then
    . ~/.bashrc
fi
if [ -f ~/.profile ]; then
    . ~/.profile
fi
EOF

script_dir=$(dirname "$0")
echo "스크립트 폴더: $script_dir"

for script_file in "$script_dir"/*.sh; do
    # 현재 실행 중인 스크립트와 같은 파일이 아닌 경우에만 실행
    if [ "$script_file" != "$0" ]; then
        echo "실행 스크립트 파일: $script_file"
        bash "$script_file" >> /script/log
        echo "---------------------------------------"
    fi
done

trap "echo 'SIGTERM received'; exit 0" SIGTERM
sleep infinity