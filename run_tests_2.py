'''
Written to test the compiler on legal and illegal Joos 1W files

Use the first positional argument to decide which assignment tests to run
Use the -s argument to show all of the passed tests

ex. run_tests.py a1 -s
'''


import os
import sys
import subprocess

def main():
    # run_tests.py a1
    prefix = sys.argv[1] if len(sys.argv) > 1 else exit('give assignment as first argument ex. run_tests.py a1')
    show_all = True if len(sys.argv) > 1 and'-s' in sys.argv else False

    test_dir = "tests"

    Failed = 0
    Passed = 0

    for root, subFolders, files in os.walk(test_dir):
        for folder in subFolders:
            if folder.startswith(prefix):
                print("\033[0mRunning {} Tests".format(folder))
                illegalTest = True if "illegalJoos" in folder else False
                path = os.path.join(test_dir, folder)

                for test in os.listdir(path):
                    print("{1}{0}{1}".format(test, "\033[39m"))

                    # For every test file in the directory that isn't a sub directory just compile the file
                    if os.path.isfile(os.path.join(path, test)):
                        file_path = "{}/{}/{}".format(test_dir, folder, test)
                        with open(file_path, "r") as file:
                            data = file.readlines()
                            for line in data:
                                if 'Hierarchy check' in line or 'HIERARCHY' in line:
                                    print('*****Hierarchy FILE: ' + file_path)

                        result = subprocess.run(['java', '-jar', 'out/artifacts/cs444_w20_group33_jar/cs444-w20-group33.jar', file_path], stdout=subprocess.PIPE, stderr=subprocess.PIPE, encoding='utf-8')
                        Passed, Failed = passOrFail(test, result, illegalTest, show_all, Passed, Failed)

                    # If directory compile all files within it
                    else:
                        file_paths = allFilesInDirAndSubdir(os.path.join(path, test))
                        for file_path in file_paths:
                            with open(file_path, "r") as file:
                                data = file.readlines()
                                for line in data:
                                    if 'Hierarchy check' in line or 'HIERARCHY' in line:
                                        print('*****Hierarchy FILE: ' + file_path)
                        compilation_unit = ['java', '-jar', 'out/artifacts/cs444_w20_group33_jar/cs444-w20-group33.jar'] + file_paths
                        result = subprocess.run(compilation_unit, stdout=subprocess.PIPE, stderr=subprocess.PIPE, encoding='utf-8')
                        Passed, Failed = passOrFail(test, result, illegalTest, show_all, Passed, Failed)


    print("\033[0mTOTAL PASSED TESTS: {}".format(Passed))
    print("\033[0mTOTAL FAILED TESTS: {}".format(Failed))


def allFilesInDirAndSubdir(path):
    files = []
    # r=root, d=directories, f = files
    for r, d, f in os.walk(path):
        for file in f:
            if '.java' in file:
                files.append(os.path.join(r, file))

    return files


def passOrFail(test, result, illegalTest, show_all, Passed, Failed):
    retcode = result.returncode
    if not illegalTest:
        # We expect no failures for the legal tests
        if retcode:
            print("\t{2}FAILED - {0}: {1}{2}".format(test, result.stderr.strip(), '\033[91m'))
            Failed += 1
        else:
            if show_all:
                print("\t{2}PASSED - {0} {1}{2}".format(test, result.stderr.strip(), '\033[92m'))
            Passed += 1

    else:
        # Failing is a passed test for the illegal Joos 1W tests
        if retcode:
            if show_all:
                print("\t{2}PASSED - {0}: {1}{2}".format(test, result.stderr.strip(), '\033[92m'))
            Passed += 1
        else:
            print("\t{2}ILLEGAL FAILED - {0}: {1}{2}".format(test, "Should have failed!", '\033[91m'))
            Failed += 1
    return Passed, Failed



main()

