'''
Written to test the compiler on legal and illegal Joos 1W files
Use the -a argument to show all of the passed tests
'''


import os
import sys
import subprocess

def main():
    show_all = True if len(sys.argv) > 1 and'-a' in sys.argv[1] else False

    test_dir = "tests"

    Failed = 0
    Passed = 0

    for root, subFolders, files in os.walk(test_dir):
        for folder in subFolders:
            print("\033[0mRunning {} Tests".format(folder))
            illegalTest = True if folder == ("illegalJoos") else False
            for root, subFolders, files in os.walk(test_dir+"/"+folder):
                for file in files:
                    file_path = "{}/{}/{}".format(test_dir, folder, file)
                    result = subprocess.run(['java', '-jar', 'build/jar/joosc.jar', file_path], stdout=subprocess.PIPE, stderr=subprocess.PIPE, encoding='utf-8')
                    retcode = result.returncode
                    if not illegalTest:
                        # We expect no failures for the legal tests
                        if retcode:
                            print("\t{2}FAILED - {0}: {1}{2}".format(file, result.stderr, '\033[91m'))
                            Failed += 1
                        else:
                            if show_all:
                                print("\t{2}PASSED - {0} {1}{2}".format(file, result.stderr, '\033[92m'))
                            Passed += 1

                    else:
                        # Failing is a passed test for the illegal Joos 1W files
                        if retcode:
                            if show_all:
                                print("\t{2}PASSED - {0}: {1}{2}".format(file, result.stderr, '\033[92m'))
                            Passed += 1
                        else:
                            print("\t{2}ILLEGAL FAILED - {0}: {1}{2}".format(file, "Should have failed!", '\033[91m'))
                            Failed += 1

    print("\033[0mTOTAL PASSED TESTS: {}".format(Passed))
    print("\033[0mTOTAL FAILED TESTS: {}".format(Failed))


main()

