# Compilers Project

### GROUP: 5E

- NAME1: Mariana Ramos, NR1: 201806869, GRADE1: 20, CONTRIBUTION1: 33.33%

- NAME2: Luanna Lopes, NR2: 201809282, GRADE2: 0, CONTRIBUTION2: 0%

- NAME3: Pedro Ferreira, NR3: 201806506, GRADE3: 20, CONTRIBUTION3: 33.33%

- NAME4: Pedro Ponte, NR4: 201809694, GRADE4: 20, CONTRIBUTION4: 33.33%

----

**GLOBAL Grade of the project:** 16


**SUMMARY:** (Describe what your tool does and its main features.)

The intention of this assignment was to develop a compiler which is able to translate Java-- programs. The compiler follows a well defined compilation flow, which includes: lexical analysis (with an LL(1) parser), syntactic analysis, semantic analysis and code generation. Among these stages, it includes:

- Syntactic Analysis with error treatment and recovery mechanisms;
- Generation of a Syntax Tree (Abstract Syntax Tree);
- Semantic Analysis;
- Generation of OLLIR;
- Generation of Jasmin.  


**DEALING WITH SYNTACTIC ERRORS:** (Describe how the syntactic error recovery of your tool works. Does it exit after the first error?)

We deal with syntactic errors in the *while* expression. In this case, when a syntactic error is found, the compiler recovers from this error and continues the parse of the program, presenting a list with the error reports when finishes the parse. The compiler could report the first 10 errors found before aborting the execution.

For the other error types, it returns immediatly and shows the error message to the user.

**SEMANTIC ANALYSIS:** 

The compiler implements the following semantic rules:

- **Variables**:
    - Checks if all the variables including arrays are previously declared.
    - Checks if a variable is not defined more than one time.
    - Checks if variables are assigned to other variables with compatible types.
    - Verifies if a variable associated with a function call is a class type variable.
    - Checks if a variable is valid within a given scope.

- **Methods**:
    - Checks if the function called is compatible with any function (that is, a function having the same signature - number of arguments, as well as the type of those arguments).
    - Checks if the return value of a function can be assigned to a variable.
    - Checks if the return value of a function is initialized.
    - Checks if the return value of a function can be used in an expression.
    - Checks if a function of type void does not return anything.
    - Verifies if the function parameters have all different names.
    - Verifies if the "target" of the method exists and if it contains the method (eg: a.foo, verifies if 'a' exists and if it contains 'foo' method).
    - Case of it's of the same type of the declared class (i.e., using *this*), checks if the method exists or if it has an *extend*.
    - In case the methos isn't part of the declared class (i.e., imported class), assume that the method exists and returns the expected type.

- **Arrays**:
    - Checks if array expression is an integer and if it has been initialized.
    - Checks if the array access is always made in a variable of an array type (int[]).
    - Checks if the property length is only used in arrays.

- **Block Statements (While and If..Else)**:
    - Checks if While and If have an expression that evaluates to a boolean.
    - Checks variable initialization inside if or else block.

- **Classes**:
    - Verifies the existance of a class when it is used.
    - When a class calls a function, verifies if the class variable is initialized and if it includes the function.
    - Verifies that a class is not instantiated inside an expression without any call to one of its functions. 

- **Operations**:
    - Verifies if conditional operations && (logical and) and ! (negation) are only used with boolean expressions.
    - Checks if conditional operator < (less than) is only used with arithmetic expressions or integers.
    - Verifies if an array is not directly used in an arithmetic or conditional operation (of type less than) (eg: array1 + array2 is invalid).
    - Checks if operands in the operations have he same type (eg: int + boolean is invalid).
    - Verifies if an array access is in fact done on an array(1[10] is invalid).
    - Verifies if the index of an array access has int type (a[true] is invalid).
    - Check if assignee type is equal to assigned type (a_int = b_boolean is invalid).
      
- **Extras**:
    - Verifies if variables are initialized before they are used in operations or method calls, etc...

 
**CODE GENERATION:** (describe how the code generation of your tool works and identify the possible problems your tool has regarding code generation.)

The parser first makes a syntactic analysis to the input program. Then, it fills in the symbol table, traversing the generated AST, in order to use that in the semantic analysis. After this step is done, it generates the OLLIR code using the AST. After having OLLIR code generated, the parser then moves forward to Jasmin stage, where it finally generates the Jasmin code.


**TASK DISTRIBUTION:** (Identify the set of tasks done by each member of the project. You can divide this by checkpoint it if helps)

The tasks were well distributed between all the peers in this work. 

- Pedro Ponte: Syntactic Analysis (parser construction and AST generation), Symbol Table generation, Semantic Analysis and Ollir code generation;
- Mariana Ramos: Syntactic Analysis (parser construction), Ollir code generation, generate new tests;
- Pedro Ferreira: Syntactic Analysis (Handling errors), Jasmin code generation, generate new tests;


**PROS:**
 
All the suggested stages for the compiler were followed and accomplished, resulting on a successfully implemented Java-- compiler. 
Some extras were implemented, like verifying variables initialization before they are used.
This project gave us a better insight vision of how a compiller works and processes the information. 
It should also be taken in account the amount of new information learnt over the course of the semester to build this compiler.

**CONS:** (Identify the most negative aspects of your tool)

During the initial stages of the project, we did not realize how much code the compiler would need, not being very carefull about code organization in the beggining. This required a lot of refactoring in the middle of the implementation of the project.
Method Overloading: When we start the Semantic Analysis, we did not realize that this is possible in Java--, only when we finished the implementation of this analysis we had known this possibility. To adapt our parser to that feature, it would require to much work, so our parser doesn't work with classes that have method overloading.


## Project setup

For this project, you need to [install Gradle](https://gradle.org/install/)

Copy your ``.jjt`` file to the ``javacc`` folder. If you change any of the classes generated by ``jjtree`` or ``javacc``, you also need to copy them to the ``javacc`` folder.

Copy your source files to the ``src`` folder, and your JUnit test files to the ``test`` folder.

## Compile

To compile the program, run ``gradle build``. This will compile your classes to ``classes/main/java`` and copy the JAR file to the root directory. The JAR file will have the same name as the repository folder.

### Run

To run you have two options: Run the ``.class`` files or run the JAR.

### Run ``.class``

To run the ``.class`` files, do the following:

```cmd
java -cp "./build/classes/java/main/" <class_name> <arguments>
```

Where ``<class_name>`` is the name of the class you want to run and ``<arguments>`` are the arguments to be passed to ``main()``.

### Run ``.jar``

To run the JAR, do the following command:

```cmd
java -jar <jar filename> <arguments>
```

Where ``<jar filename>`` is the name of the JAR file that has been copied to the root folder, and ``<arguments>`` are the arguments to be passed to ``main()``.

## Test

To test the program, run ``gradle test``. This will execute the build, and run the JUnit tests in the ``test`` folder. If you want to see output printed during the tests, use the flag ``-i`` (i.e., ``gradle test -i``).
You can also see a test report by opening ``build/reports/tests/test/index.html``.

## Checkpoint 1
For the first checkpoint the following is required:

1. Convert the provided e-BNF grammar into JavaCC grammar format in a .jj file
2. Resolve grammar conflicts (projects with global LOOKAHEAD > 1 will have a penalty)
3. Proceed with error treatment and recovery mechanisms for the while expression
4. Convert the .jj file into a .jjt file
5. Include missing information in nodes (i.e. tree annotation). E.g. include class name in the class Node.
6. Generate a JSON from the AST

### JavaCC to JSON
To help converting the JavaCC nodes into a JSON format, we included in this project the JmmNode interface, which can be seen in ``src-lib/pt/up/fe/comp/jmm/JmmNode.java``. The idea is for you to use this interface along with your SimpleNode class. Then, one can easily convert the JmmNode into a JSON string by invoking the method JmmNode.toJson().

Please check the SimpleNode included in this repository to see an example of how the interface can be implemented, which implements all methods except for the ones related to node attributes. How you should store the attributes in the node is left as an exercise.

### Reports
We also included in this project the class ``src-lib/pt/up/fe/comp/jmm/report/Report.java``. This class is used to generate important reports, including error and warning messages, but also can be used to include debugging and logging information. E.g. When you want to generate an error, create a new Report with the ``Error`` type and provide the stage in which the error occurred.


### Parser Interface

We have included the interface ``src-lib/pt/up/fe/comp/jmm/JmmParser.java``, which you should implement in a class that has a constructor with no parameters (please check ``src/Main.java`` for an example). This class will be used to test your parser. The interface has a single method, ``parse``, which receives a String with the code to parse, and returns a JmmParserResult instance. This instance contains the root node of your AST, as well as a List of Report instances that you collected during parsing.

To configure the name of the class that implements the JmmParser interface, use the file ``parser.properties``.
