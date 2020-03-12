'''
Written to test the compiler on legal and illegal Joos 1W files

Use the first positional argument to decide which assignment tests to run
Use the -s argument to show all of the passed tests
Use the -h for Dawar Hierarchy stuff
Use the -o flag to show the standard output of each test ran
Use the -p <PREFIX> argument to only run tests of file with that prefix

For example to run the Je_6_Assignable_* tests you can simple run:
    run_tests.py a3 -p Je_6_A
add the -s option to see the ones that pass like so:
    run_tests.py a3 -p Je_6_A -s


'''


import os
import sys
import subprocess

def main():
    # run_tests.py a1
    folder_prefix = sys.argv[1] if len(sys.argv) > 1 else exit('give assignment as first argument ex. run_tests.py a1')
    show_all = True if len(sys.argv) > 1 and'-s' in sys.argv else False
    hierarchy = True if len(sys.argv) > 1 and'-h' in sys.argv else False
    show_stdout = True if len(sys.argv) > 1 and'-o' in sys.argv else False
    use_prefix = True if len(sys.argv) > 1 and'-p' in sys.argv else False
    if use_prefix:
        ind = sys.argv.index('-p')
        file_prefix = sys.argv[ind+1]

    test_dir = "tests"

    stdLib = getStandardLibraryPaths()

    Failed = 0
    Passed = 0

    for root, subFolders, files in os.walk(test_dir):
        for folder in subFolders:
            if folder.startswith(folder_prefix):
                print("\033[0mRunning {} Tests".format(folder))
                illegalTest = True if "illegalJoos" in folder else False
                path = os.path.join(test_dir, folder)

                for test in listdir_nohidden(path):
                    runTest = True
                    # If using a file name prefix argument only run files with the prefix
                    if use_prefix:
                        if not test.startswith(file_prefix):
                            runTest = False

                    if runTest:
                        # For every test file in the directory that isn't a sub directory just compile the file
                        if os.path.isfile(os.path.join(path, test)):
                            file_path = "{}/{}/{}".format(test_dir, folder, test)
                            if (hierarchy):
                                with open(file_path, "r") as file:
                                    data = file.readlines()
                                    for line in data:
                                        if 'Hierarchy check' in line or 'HIERARCHY' in line:
                                            print('*****Hierarchy FILE: ' + file_path)
                            result = subprocess.run(['java', '-jar', 'build/jar/joosc.jar', file_path] + stdLib, stdout=subprocess.PIPE, stderr=subprocess.PIPE, encoding='utf-8')
                            Passed, Failed = passOrFail(test, result, illegalTest, show_all, show_stdout, Passed, Failed)

                        # If directory compile all files within it
                        else:
                            file_paths = allFilesInDirAndSubdir(os.path.join(path, test))
                            if (hierarchy):
                                for file_path in file_paths:
                                    with open(file_path, "r") as file:
                                        data = file.readlines()
                                        for line in data:
                                            if 'Hierarchy check' in line or 'HIERARCHY' in line:
                                                print('*****Hierarchy FILE: ' + file_path)
                            compilation_unit = ['java', '-jar', 'build/jar/joosc.jar'] + file_paths + stdLib
                            result = subprocess.run(compilation_unit, stdout=subprocess.PIPE, stderr=subprocess.PIPE, encoding='utf-8')
                            Passed, Failed = passOrFail(test, result, illegalTest, show_all, show_stdout, Passed, Failed)


    print("\033[0mTOTAL PASSED TESTS: {}".format(Passed))
    print("\033[0mTOTAL FAILED TESTS: {}".format(Failed))


def listdir_nohidden(path):
    for f in os.listdir(path):
        if not f.startswith('.'):
            yield f


def getStandardLibraryPaths():
    pwd = os.getcwd()
    io = [os.path.join(pwd, c) for c in allFilesInDirAndSubdir("JavaStdLib/2.0/java/io")]
    lang = [os.path.join(pwd, c) for c in allFilesInDirAndSubdir("JavaStdLib/2.0/java/lang")]
    util = [os.path.join(pwd, c) for c in allFilesInDirAndSubdir("JavaStdLib/2.0/java/util")]
    stdLib = io + lang + util
    return stdLib

def allFilesInDirAndSubdir(path):
    files = []
    # r=root, d=directories, f = files
    for r, d, f in os.walk(path):
        for file in f:
            if '.java' in file:
                files.append(os.path.join(r, file))

    return files


def passOrFail(test, result, illegalTest, show_all, show_stdout, Passed, Failed):
    retcode = result.returncode
    stdOut = result.stdout if show_stdout else ""
    if not illegalTest:
        # We expect no failures for the legal tests
        if retcode:
            print("\t{2}FAILED - {0}: {1}{2}{3}".format(test, result.stderr.strip(), '\033[91m', stdOut))
            Failed += 1
        else:
            if show_all:
                print("\t{2}PASSED - {0} {1}{2}{3}".format(test, result.stderr.strip(), '\033[92m', stdOut))
            Passed += 1

    else:
        # Failing is a passed test for the illegal Joos 1W tests
        if retcode:
            if show_all:
                print("\t{2}PASSED - {0}: {1}{2}{3}".format(test, result.stderr.strip(), '\033[92m', stdOut))
            Passed += 1
        else:
            print("\t{2}ILLEGAL FAILED - {0}: {1}{2}{3}".format(test, "Should have failed!", '\033[91m', stdOut))
            Failed += 1
    return Passed, Failed



main()

