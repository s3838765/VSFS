#! /usr/bin/python3.8
from pathlib import Path
import os

fs = ''

# find and iterate through each directory in path of script
#for dir in [x for x in Path('.').iterdir() if x.is_dir()]:
for dir in [x for x in Path('.').glob('**/*') if x.is_dir()]:
    command = str(dir)
    stripped_command = command
    # find each file ending in .notes
    for file in [x for x in Path(dir).iterdir()]:
        if str(file).endswith('.notes'):
            fs = str(file)[0:-6]
            if '/' in command:
                stripped_command = command[:command.find('/')]

            if command == 'list':
                os.system(f'../VSFS list {fs}.notes 1> {fs}.out 2>&1')
            elif command == 'list/outputNotes':
                os.system(f'''
                cp {fs}.notes {fs}Temp.notes
                ../VSFS list {fs}Temp.notes > /dev/null
                cp {fs}Temp.notes {fs}.out
                rm {fs}Temp.notes
                ''')

            elif command == 'copyin':
                os.system(f'''
                cp {fs}.notes {fs}Temp.notes
                if [ -f {fs}.txt ]
                then
                    ../VSFS copyin {fs}Temp.notes {fs}.txt {fs}
                fi
                if [ -f {fs}.jar ]
                then
                    ../VSFS copyin {fs}Temp.notes {fs}.jar {fs}
                fi
                cp {fs}Temp.notes {fs}.out
                rm {fs}Temp.notes
                ''')
            elif command == 'copyin/outputErrors':
                os.system(f'''
                ../VSFS copyin {fs}.notes {fs}.txt {fs} 1> {fs}.out 2>&1
                ''')

            elif command == 'copyout':
                os.system(f'''
                ../VSFS copyout {fs}.notes {fs} {fs}.out
                ''')
            elif command == 'copyout/outputErrors':
                os.system(f'''
                ../VSFS copyout {fs}.notes {fs} {fs}.txt 1> {fs}.out 2>&1
                ''')


            elif command == 'mkdir':
                os.system(f'''
                cp {fs}.notes {fs}Temp.notes
                ../VSFS mkdir {fs}Temp.notes {fs}\/
                cp {fs}Temp.notes {fs}.out
                rm {fs}Temp.notes
                ''')
            elif command == 'mkdir/outputErrors':
                os.system(f'''
                ../VSFS mkdir {fs}.notes {fs}\/ 1> {fs}.out 2>&1
                ''')

            elif command == 'rm':
                os.system(f'''
                cp {fs}.notes {fs}Temp.notes
                ../VSFS rm {fs}Temp.notes {fs}
                cp {fs}Temp.notes {fs}.out
                rm {fs}Temp.notes
                ''')
            elif command == 'rm/outputErrors':
                os.system(f'''
                ../VSFS rm {fs}.notes {fs} 1> {fs}.out 2>&1
                ''')

            elif command == 'rmdir':
                os.system(f'''
                cp {fs}.notes {fs}Temp.notes
                ../VSFS rmdir {fs}Temp.notes {fs}
                cp {fs}Temp.notes {fs}.out
                rm {fs}Temp.notes
                ''')
            elif command == 'rmdir/outputErrors':
                os.system(f'''
                ../VSFS rmdir {fs}.notes {fs}\/ 1> {fs}.out 2>&1
                ''')

            elif command == 'defrag':
                os.system(f'''
                cp {fs}.notes {fs}Temp.notes
                ../VSFS defrag {fs}Temp.notes
                cp {fs}Temp.notes {fs}.out
                rm {fs}Temp.notes
                ''')

            elif command == 'index':
                os.system(f'''
                ../VSFS index {fs}.notes 1> {fs}.out 2>&1
                ''')

        elif str(file).endswith('.sh'):
            fs = str(file)[0:-3]
            os.system(f'''
                    cd manualTests/
                    ./{fs[11:]}.sh
            ''')

        if (str(file).endswith('.notes') and 'manual' not in command) or (str(file).endswith('.sh')):
            os.system(f'''
            if [ -f {fs}.exp ]
            then
            DIFF=$(diff {fs}.out {fs}.exp)
                if ["$DIFF" == ""]
                then
                    echo "🟢 Test on {fs}.notes passed!"
                else
                    echo "🔴 Test on {fs}.notes failed!"
                fi
            else
                echo "🟠 {fs}.exp does not exist."
            fi''')
            print('--------------------------------------------')
