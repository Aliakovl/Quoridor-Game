import re
import subprocess


def main():
    ipv4_pattern = r'^(\d{1,3}\.){3}\d{1,3}$'
    with open('/home/aleksei/home_ip', 'r') as file:
        str_ip = file.readlines()[0].strip()
        if re.match(ipv4_pattern, str_ip):
            update_zone(str_ip)
            subprocess.run(["systemctl", "restart", "bind9"], capture_output=True)


def update_zone(ip):
    text = f"""; BIND reverse data file for empty int.quoridor.ru zone
$TTL	60
@	IN	SOA	ns1.quoridor.ru. master.quoridor.ru. (
			      1		; Serial
			 604800		; Refresh
			  86400		; Retry
			2419200		; Expire
			  86400 )	; Negative Cache TTL
;
ns1     IN      A       194.58.117.11
ns2     IN      A       176.99.13.12
@	IN	NS	ns1.quoridor.ru.
@	IN	NS	ns2.quoridor.ru.
int.quoridor.ru. IN	A	{ip}
"""
    with open('/etc/bind/int.quoridor.ru.zone', 'w') as file:
        file.write(text)


if __name__ == '__main__':
    main()
