# CCInvec
Incremental Code Clone detector using Vectorization.

# Requirement
 - Windows
 - Java 1.8
 
# Setup
## Step 1
The project uses Maven with Maven Assembly Plugin.
The following command builds a runnable jar `ccinvec.jar`:

         mvn package

The project  includes Eclipse project file and `pom.xml` for dependencies. 

## Step 2
Place `ccinvec.jar`, `FALCONN` and `DIFF` directory in the same directory.

# Usage
```
java -jar ccinvec.jar [configfilename]
```

# Config File Format
You can make a config file by reffering to [configsample(git)](https://github.com/hirotaka0616/CCInvec/blob/master/configsample(git)) and [configsample(local)](https://github.com/hirotaka0616/CCInvec/blob/master/configsample(local)).
The details of the setting are shown below.
```
 LANGUAGE              select language from following ( default: java )
                           * java   [Java]
                           * c      [C]
                           * cpp    [C++]
                           * csharp [C#]
 VEC_METHOD            select vectorization method from following ( default: BoW )
                           * BoW
                           * TF-IDF
 SIZE                  set threshold of size for method ( 0<=size ) ( dafault: 50 )
 BLOCK_SIZE            set threshold of size for block ( 0<=size ) ( dafault: same as size for method) 
 SIM                   set set threshold of similarity for clone detection ( 0.0<=sim<=1.0 ) ( default: 0.9 )
 OUTPUT_FORMAT         select output format from following
                           * csv  [*.csv]
                           * text [*.txt]
 OUTPUT_DIR            set output destination folder
 TARGET                select analysis target form following 
	                   * local (Target your local project)
                           * git   (Target git project)
```

If you set "TARGET:git", you should prepare two folders containing the same git project for using GNU diff and set following additional options.
```
COMMIT_ID      set the commit ID in the order from the oldest date.
NEW_DIR        set the folder path of git project (different path from OLD_DIR) 
OLD_DIR        set the folder path of git project (different path from NEW_DIR)
```

If you set "TARGET:local", you should set following additional option.
```
INPUT_DIR      set target path of folder in the order from the oldest date.
```

# Licence
This software is released under the MIT License, see LICENSE.

This project uses the following components.

 - ANTLR4 (http://www.antlr.org/license.html): BSD License
 - Apache (http://www.apache.org/licenses/LICENSE-2.0):  Apache License, Version 2.0
 - FALCONN (https://falconn-lib.org/): MIT License
 - Cygwin (https://cygwin.com/licensing.html): LGPL License
 - JGraphT (http://jgrapht.org/): EPL License
 - GNU diff (http://www.gnu.org/software/diffutils/): GPL License
