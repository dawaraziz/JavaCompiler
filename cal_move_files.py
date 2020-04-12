import subprocess
import paramiko
import os
import sys

server = "linux.student.cs.uwaterloo.ca"
username = "ccvbruto"
password = ""
all = True
directory = ""
if (len(sys.argv) >= 2):
	all = False
	directory = sys.argv[1]

ssh = paramiko.SSHClient()
ssh.load_system_host_keys()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect(server, username=username, password=password)

def move_files(directory):
	mkdir_cmd = "mkdir /u1/"+username+"/cs644/" + directory
	ssh_stdin, ssh_stdout, ssh_stderr = ssh.exec_command(mkdir_cmd)

	assembly_files = os.listdir('./output/' + directory + "/")
	
	for file in assembly_files:
		if file[-2:] == ".s":
			localpath = './output/' + directory + "/" + file
			remotepath = "/u1/"+username+"/cs644/" + directory + "/" + file
			sftp = ssh.open_sftp()
			sftp.put(localpath, remotepath)
			sftp.close()
	

def main():
	directories = os.listdir('./output/')

	if all:
		for directory in directories:
		    if directory[-2:] != '.s':
			    move_files(directory)

	else:
		move_files('./'+sys.argv[1])


main()
