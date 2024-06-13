ssh -o ConnectTimeout=2 -o KeepAlive=no -i ~/.ssh/vds_rsa aleksei@quoridor.ru "echo \$SSH_CLIENT | awk '{ print \$1 }' > home_ip"
