#! /usr/bin/python3.8
from pathlib import Path
import os

RED = '\033[0;31m'
GREEN = '\033[0;32'
NC = '\033[0m'

# find and iterate through each directory in path of script
#for dir in [x for x in Path('.').iterdir() if x.is_dir()]:
for dir in [x for x in Path('.').glob('**/*') if x.is_dir()]:
    # find each file ending in .notes
    for file in [x for x in Path(dir).iterdir()]:
        if str(file).endswith('.notes'):
            command = str(dir)
            file_name = str(file)[0:-6]
            if '/' in command:
                print(f'Testing {command[:command.find("/")]} on {file_name}.notes')
            else:
                print(f'Testing {command} on {file_name}.notes')
            if command == 'list':
                os.system(f'../VSFS list {file_name}.notes 1> {file_name}.out 2>&1')
            elif command == 'list/outputNotes':
                os.system(f'''
                cp {file_name}.notes {file_name}Temp.notes
                ../VSFS list {file_name}Temp.notes > /dev/null
                cp {file_name}Temp.notes {file_name}.out
                rm {file_name}Temp.notes
                ''')
            elif command == 'copyin':
                os.system(f'''
                cp {file_name}.notes {file_name}Temp.notes
                if [ -f {file_name}.txt ]
                then
                    ../VSFS copyin {file_name}Temp.notes {file_name}.txt {file_name}
                fi
                if [ -f {file_name}.jar ]
                then
                    ../VSFS copyin {file_name}Temp.notes {file_name}.jar {file_name}
                fi
                cp {file_name}Temp.notes {file_name}.out
                rm {file_name}Temp.notes
                ''')
            elif command == 'copyout':
                os.system(f'../VSFS copyout {FS}')
            elif command == 'mkdir':
                os.system(f'../VSFS mkdir {FS}')
            elif command == 'rm':
                os.system(f'../VSFS rm {FS}')
            elif command == 'rmdir':
                os.system(f'../VSFS rmdir {FS}')
            elif command == 'defrag':
                os.system(f'../VSFS defrag {FS}')
            elif command == 'index':
                os.system(f'../VSFS index {FS}')
            os.system(f'''
            if [ -f {file_name}.exp ]
            then
            DIFF=$(diff {file_name}.out {file_name}.exp)
                if ["$DIFF" == ""]
                then
                    echo "🟢 Test on {file_name}.notes passed!"
                else
                    echo "🔴 Test on {file_name}.notes failed!"
                fi
            else
                echo "🟠 {file_name}.exp does not exist."
            fi''')
            print('--------------------------------------------')
