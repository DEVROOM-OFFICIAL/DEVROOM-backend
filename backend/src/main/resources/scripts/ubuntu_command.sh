echo "init student container"
cp -r /app/config /script
chmod +x /script/ubuntu_init.sh
/script/ubuntu_init.sh >> /script/log 2>&1