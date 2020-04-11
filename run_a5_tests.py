import subprocess
import os
import sys

every = True
if len(sys.argv) >= 2:
    every = False
    directory = sys.argv[1]

def main():
    if every:
        directories = os.listdir('./')
        for directory in directories:
            compile_and_test(directory)
    else:
        compile_and_test(directory)



def compile_and_test(directory):

    assembly_files = os.listdir('./'  + directory)

    # Generate .o
    for f in assembly_files:
        if f[-2:] == ".s":
            result = subprocess.run(['/u/cs444/bin/nasm', '-O1', '-f','elf','-g', '-F', 'dwarf', './'+directory+'/'+f], stdout=subprocess.PIPE, stderr=subprocess.PIPE, encoding='utf-8')

    os.system("ld -melf_i386 -o ./" + directory + "/main ./" + directory + "/*.o")

    result = subprocess.run(['./' + directory + '/main'], stdout=subprocess.PIPE, stderr=subprocess.PIPE, encoding='utf-8')

    print(directory + ": " + str(result.returncode))

    sys.exit(result.returncode)



main()

