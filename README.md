# Java Bytecode Reducer
[![Build Status](https://travis-ci.org/jku-ssw/java-bytecode-reducer.svg?branch=master)](https://travis-ci.org/jku-ssw/java-bytecode-reducer)

This project contains a a test case reducer for Java bytecode in the spirit of [C-Reduce](https://github.com/csmith-project/creduce).
It provides multiple modules that apply different reduction techniques to selected bytecodes
in order to remove complex instruction sequences and unused members.

## Usage
To initiate a reduction sequence, the following command line options are supported:

`jreduce [-d <arg>] [-help] [-i <arg>] [-k] [-out <arg>] [-q | -v] [-t <arg>] [-tmp <arg>] [-version]`

| Argument                | Description                                                                                      |
|-------------------------|--------------------------------------------------------------------------------------------------|
| -d,--working-dir <arg>  | The working directory in which the task is run (if omitted, the current directory is assumed)    |
| -help,--help            | Display information about application usage                                                      |
| -i,--i-tests <arg>      | The interestingness test file (test.{sh,bat} is assumed if no argument is supplied)              |
| -k,--keep               | Keep temporary test directories and files                                                        |
| -out,--out-dir <arg>    | The directory where results will be placed                                                       |
| -q,--quiet              | Suppress log messages                                                                            |
| -t,--timeout <arg>      | The timeout in seconds until runs of the test files are interrupted (to prevent infinite loops)  |
| -tmp,--temp-dir <arg>   | The temporary directory where the intermediate test results will be placed                       |
| -v,--verbose            | Verbose logging                                                                                  |
| -version,--version      | Print program version                                                                            |

## Supported modules

* **Fields**

  * *Remove all field attributes*
      
    Attempts to strip fields of all their attributes (effectively making them package-protected instance variables)
  
  * *Remove random field attributes*
  
    Attempts to randomly remove field attributes (e.g. `STATIC`, `FINAL`, `PRIVATE`)
  
  * *Remove read-only fields*
    
    Removes fields that are only read and never updated (except initial assignment in constructor / initializer)
  
  * *Remove static field attributes*
  
    Attempts to remove static attributes of fields (making them instance variables)
  
  * *Remove unused fields*
  
    Searches for fields that are never used (except in initial assignment) and removes them
  
  * *Remove write-only fields*
  
    Searches for fields that are only updated but never queried and remove them

* **Methods**
  
  * *Remove all method attributes*
  
    Strips all attributes of a method (effectively making them package-protected instance methods)
  
  * *Remove empty methods*
  
    Removes (void) methods that only consist of `return` instructions
  
  * *Remove initializers*
  
    Removes constructors and static initializers (skipping the implicitly created default constructor)
  
  * *Remove random method attributes*
  
    Attempts to randomly remove a method attribute (e.g. `STATIC`, `FINAL`, `SYNCHRONIZED`)
  
  * *Remove unused methods*
    
    Searches for methods that are never called and removes them - this includes self-recursions i.e. 
    ```
    public void aMethod() {
      aMethod();
    }
    ```
    
  * *Remove void method calls*
  
    Randomly removes call sites of `void` methods
  
  * *Replace method calls*
  
    Replaces non-`void` method call sites with assignments to default values - i.e.
    ```
    int i = getInt();
    // becomes
    int i = 0;
    ```

* **Control flow**
  
  * *Remove constant assignments*
  
    This low-level module removes consecutive constant assignments - i.e.
    ```
    iconst_0
    istore_0
    ```
  
  * *Remove instruction sequences*
  
    This low-level module keeps track of the stack size and removes instruction sequences that start at and then again lead to an empty stack
  
  * *Remove stack-neutral instructions*

    Low-level module that removes (replaced with `nop`) single instructions that do not change the stack level (even if they push and pop elements)

* **Miscellaneous**
  
  * *Shrink constant pool*

    Removes elements from the constant pool that are no longer referenced

