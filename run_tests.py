import os
import sys
import subprocess

def main():
    show_all = True if len(sys.argv) > 1 and'-a' in sys.argv[1] else False

    test_dir = "tests"

    for root, subFolders, files in os.walk(test_dir):
        for folder in subFolders:
            print("Running {} Tests".format(folder))
            for root, subFolders, files in os.walk(test_dir+"/"+folder):
                for file in files:
                    file_path = "{}/{}/{}".format(test_dir, folder, file)
                    result = subprocess.run(['java', '-jar', 'build/jar/joosc.jar', file_path], stdout=subprocess.PIPE, stderr=subprocess.PIPE, encoding='utf-8')
                    retcode = result.returncode
                    if retcode:
                        print("\tError in {}: {}".format(file, result.stderr))
                    else:
                        if show_all:
                            print("\tPASSED: {}".format(file))



main()

