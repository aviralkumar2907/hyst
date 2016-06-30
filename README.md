# HyST: A Source Transformation and Translation Tool for Hybrid Automaton Models: http://www.verivital.com/hyst/
## HyST Source Code: https://github.com/verivital/hyst
## HyST Benchmarks: https://github.com/verivital/hyst-benchmark

***********************************
### Contributors
***********************************

* Christian Schilling (2014-present), http://swt.informatik.uni-freiburg.de/staff/christian_schilling
* Luan Viet Nguyen (2014-present)
* Stanley Bak (2014-present), http://stanleybak.com
* Sergiy Bogomolov (2013-present), http://swt.informatik.uni-freiburg.de/staff/bogom
* Taylor T. Johnson (2014-present), http://www.taylortjohnson.com/
* Christopher Dillo (2013-2014)

HyST started during a 2014 Visiting Faculty Research Program visit by Taylor to AFRL, and is based on an initial project that provided a SpaceEx parser by Christopher Dillo and Sergiy Bogomolov.

THIS SOFTWARE WAS DEVELOPED WITH FUNDING BY AIR FORCE RESEARCH LABORATORY (AFRL) AND IS APPROVED FOR PUBLIC RELEASE. DISTRIBUTION A. Approved for public release; Distribution unlimited. (Approval AFRL PA case number 88ABW-2016-1014, 08 MAR 2016.)

************************************
### HYST USAGE
************************************

Hyst has been tested on Windows 7/8/10 and Linux (Ubuntu) using Java 1.7 and Java 1.8.

#### GUI USAGE:

Hyst can be run through a GUI or using the command line. To use the GUI, after building Hyst.jar simply run it as an executable .jar file with no arguments:

$ java -jar Hyst.jar

#### COMMAND-LINE USAGE: 

After building Hyst.jar, you can run it as an executable .jar file with the -help flag to see usage:

```
$ java -jar Hyst.jar -help
Hyst v1.17
Usage:
hyst [OutputType] (args) XMLFilename(s) (CFGFilename)

OutputType:
	-flowstar Flow* format
	-dreach dReach format
        -hycomp HyComp/HyDI format
	-hycreate HyCreate2 format
	-hycreate_sim HyCreate2 format
	-qbmc Python QBMC (Testing) format
	-spaceex SpaceEx format
	-z SMT-LIB printer format
Optional Model Transformation Passes:
	-pass_pi Pseudo-Invariant at Point Pass [(modename|)pt1;inv_dir1|pt2;inv_dir2|...] (point/invariant direction is a comma-separated list of reals)
	-pass_pi_sim Pseudo-Invariant Simulation Pass [time1;time2;...]
	-pass_scale_time Scale Time Pass [multiplier;ignorevar]
	-pass_sub_constants Substitute Named Constants for Values Pass [no param]
	-pass_simplify Simplify Expressions Pass [no param]
	-pass_split_disjunctions Split Guards with Disjunctions [no param]
	-pass_remove_unsat Remove Unsatisfiable Modes Pass [no param]
	-shorten Shorten Mode Names Pass [no param]
	-regularize Regularization (eliminate zeno behaviors) Pass [<num jumps>;<delta>;<epsilon>]

-help show this command-line help text
-v Enable verbose printing
-debug Enable debug printing (even more verbose)
-novalidate skip internal model validation (may result in Exceptions being thrown)
-o [filename] output to the given filename
XMLFilename: The SpaceEx XML automaton to be processed (*.xml)
CFGFilename: The automaton's config file. Will be derived from the XML filename if not explicitly stated (*.cfg)
```

#### CONVERTING AN EXAMPLE: 

To convert from a SpaceEx model, you run Hyst, provide the proper flag for the format you want to output, and the path to the SpaceEx .xml and, if named differently the .cfg file. You can also provide an output filename with the -o flag (stdout will be used otherwise, which may be incompatible with model formats that require multiple files).

From the default directory of hyst/src (where Hyst.jar is compiled), execute:

```
$ java -jar Hyst.jar -flowstar ../examples/toy/toy.xml
```

In this case -flowstar indicates we want a model in the Flow* format (see the usage above). The .cfg file will be assumed to be ../examples/toy/toy.cfg since it is not explicitly specified. Since no filename is given using the -o flag, the output will be printed to stdout.

************************
### SPECIFIC EXAMPLES FOR SUPPORTED OUTPUT FORMATS
************************

#### HYCREATE2: http://stanleybak.com/projects/hycreate/hycreate.html

```
java -jar Hyst.jar ../examples/heaterLygeros/heaterLygeros.xml -hycreate -o heaterLygeros.hycreate
```

This will convert the heater/thermostat example described in the paper to the HyCreate2 format, and write the result to the file heaterLygeros.hycreate.

#### FLOW*: http://systems.cs.colorado.edu/research/cyberphysical/taylormodels/

```
java -jar Hyst.jar ../examples/heaterLygeros/heaterLygeros.xml -flowstar -o heaterLygeros.flow
```

#### DREACH: http://dreal.github.io/dReach/

NOTE: dReach (as of this writing) requires files to have the extension .drh to execute.

```
java -jar Hyst.jar ../examples/heaterLygeros/heaterLygeros.xml -dreach -o heaterLygeros.drh
```

#### SPACEEX: http://spaceex.imag.fr/

You may want to convert from a SpaceEx model back to SpaceEx to run some transformation passes or just to do network flattening.

```
java -jar Hyst.jar examples/heaterLygeros/heaterLygeros.xml -spaceex -o heaterLygeros.xml
```

#### HYCOMP / HYDI / NUXMV: https://es-static.fbk.eu/tools/hycomp/

```
java -jar Hyst.jar examples/heaterLygeros/heaterLygeros.xml -hycomp -o heaterLygeros.hydi
```

#### EXAMPLES AND RESULTS DIRECTORY:

Several examples have been included which can be converted in the examples directory. The result shows the result of converting the models and running them with the various tools using the default settings (not all tools complete on all models).

Some examples are not yet complete, as we are currently working to convert all the ARCH 2014/2015 workshop (http://cps-vo.org/group/ARCH) benchmarks to SpaceEx format (where possible).

#### ADVANCED USAGE:

To generate the output plots provided across all the examples, we have included Python scripts to call HYST against all examples in all formats.  See them in:

```
src\hybridpy\hybrid_tool.py
```

Additionally, the scripts provided the ability to call the tools themselves and generate plots.  If you are interested to do this, please contact us and we will share our repository to help get you set up.

To run Hyst on all the models, as well as the associated tools in a batch run, simply do:

```
python run_examples.py
```

In run_examples.py there are a bunch of global variables you can set for different parameters (such as different timeouts, the paths to the tool binaries to run). The tool binaries are NOT included in this package (they each have their own licenses you should read before using). The result is placed in the result folder. 

A copy of the expected model files and plot files produced is in the expected_results directory. Also included is stdout.txt which includes stdout when running the script.

*******************************
### DEVELOPER USAGE
*******************************

#### HYST Version Control:

We are using a forking repository workflow, more details are here: https://www.atlassian.com/git/tutorials/comparing-workflows/forking-workflow

The basic process is:

1) fork https://github.com/verivital/hyst
2) commit changes to your fork (if you already made changes to the main repository line, just copy/paste the source files in the new fork and commit them)
3) push changes to your fork
4) issue pull request, we will review it and then approve if it looks good

Please create unit tests and possibly regression tests for any changes submitted (e.g., see unit tests here: https://github.com/verivital/hyst/tree/master/src/java/com/verivital/hyst/junit and see regression tests here: https://github.com/verivital/hyst/tree/master/src/tests/regression ), this way we will know if we accidentally break any changes you've committed through other changes in Hyst.

#### Code Quality:

If you plan to do Hyst development that will work its way back into the main branch, please make an effort to produce high quality code. In addition to general practices of organizing your code flow in a logical manner, breaking up code into classes and methods which make sense for whatever logic being implemented, please pay attention to the following guidelines:

* Add tests. You should at least have one or two tests showing that your printer or transformation pass works as expected. Add individual tests for complicated subcomponents or key methods.
* Avoid duplicate code. If the same functionailty is implemented twice, generalize it and make a method called in both instances.
* Avoid catching or throwing general exceptions. For errors in the printer use `AutomatonExportException` rather than `RuntimeException`.
* When catching and rethrowing `Exception` objects, pass the old `Exception` into the constructor of the new one, so the trace is maintained. For example, if `e` is a caught `Exception`, you might do `throw new AutomatonExportExcpetion("error message", e);`
* Instead of printing status messages to stdout, use `Hyst.log` or `Hyst.logDebug`
* Delete commented-out garbage code.
* Use meaningful variable and method names.
* Use Java naming conventions for class and method names. Class names should be CapitalizedCamelCase, and methods should be lowercaseFirstCamelCase.
* If you copy a printer as a template, for example the Flow* printer, your final printer shouldn't make references to Flow* or its parameters. Read through your code before submitting.
* Please follow the Hyst Code Format

#### Code Format:

Hyst has a standard code format. When you run ant, if the eclipse executable is on your path, it will automatically try to reformat every java file. You will see a message: "Eclipse is present. Performing code reformatting." Otherwise, if it cannot find eclipse, you'll see this message "Eclipse NOT found on PATH. Your code will not be auto-reformatted." If the reformat process fails, it might be because the format file is open within eclipse. Try closing eclipse and then running ant again.

You can save significant coding time by setting up auto formatting every time you save your code. The Eclipse file describing the Hyst code format is in hyst/HystCodeFormat.xml. To import this file into Eclipse, go to Project Properties -> Java Code Style -> Formatter -> Enable Project Specific Settings -> Import, and then select HystCodeFormat.xml. To reformat every time you save (recommended), go to Project Properties, choose Java -> Editor -> Save Actions -> Enable Project Specific Properties. Next, enable the "Perform the selected actions on save", and check the "Format source code" box. You make also want to select "Organize Imports" as a save action.

#### BUILDING HYST: 

To build Hyst, proceed to the hyst/src/ directory and run "ant". This will create the Hyst.jar file.


#### TESTS:

There are included unit tests as well as regression tests. You can run these using "ant test". The regression tests may require tool step in the file src/tests/regression/run_tests.py

*******************************
#### ADDING A NEW PRINTER:
*******************************

It is relatively easy to add a new printer. The typical process we follow is to copy an existing printer that extends com.verivital.hyst.printers.ToolPrinter ( https://github.com/verivital/hyst/blob/master/src/java/com/verivital/hyst/printers/ToolPrinter.java ), then start converting syntactic elements to match the input format of the other tool (HyST's output). For examples of implemented printers, see: https://github.com/verivital/hyst/tree/master/src/java/com/verivital/hyst/printers

For tools that support hybrid automata or networks of hybrid automata, this is relatively straightforward, and there are typically no major semantics differences. The internal representation is in essence a network of hybrid automata, where each hybrid automaton is a tuple consisting of the standard sets (a set of variables, a set of modes/locations, a set of transitions between modes, etc.). So, a printer typically just walks this data structure printing the appropriate components in the syntax of the output format.

For example, see: https://github.com/verivital/hyst/blob/master/src/java/com/verivital/hyst/printers/DReachPrinter.java#L92 which consists of:

```
	private void printProcedure() 
	{
		printVars();
		printConstants();
		printModes();
		printInitialStates();
		printGoalStates();
	}
```

For this, printVars declares the continuous variables, printConstants sets up some constant values, printModes prints the modes/locations of the automaton and the transitions between them (for this format), and the initial states and bad (goal) states are printed last.

For the translation of expressions (as appearing in guards, resets, invariants, flows/differential equations, etc.), the printer extends a DefaultExpressionPrinter class ( https://github.com/verivital/hyst/blob/master/src/java/com/verivital/hyst/grammar/formula/DefaultExpressionPrinter.java ) and can override easily operand types, convert between prefix/infix expressions if necessary, etc. For the dReach example, this is done at https://github.com/verivital/hyst/blob/master/src/java/com/verivital/hyst/printers/DReachPrinter.java#L325 :

```
public static class DReachExpressionPrinter extends DefaultExpressionPrinter
	{
		public DReachExpressionPrinter()
		{
			super();

			opNames.put(Operator.AND, "and");
			opNames.put(Operator.OR, "or");
			opNames.put(Operator.POW, "^");
			...
		}
```

Which overrides the default conjunction (AND) operator to be `and` instead of `&`, and similarly for OR and other operators. The dReach printer also does some conversion to prefix form (dReach's syntax is an unusual mixture of infix and prefix format).

There may be subtle semantics differences as well as syntactic incompatibilities. To handle these issues, adding a new printer may require creating some syntax and/or semantics transformation passes, which is a class that extends com.verivital.hyst.passes.TransformationPass ( https://github.com/verivital/hyst/blob/master/src/java/com/verivital/hyst/passes/TransformationPass.java ). Several examples are included at: https://github.com/verivital/hyst/tree/master/src/java/com/verivital/hyst/passes

A simple example is the AddIdentityResetsPass.java ( https://github.com/verivital/hyst/blob/master/src/java/com/verivital/hyst/passes/basic/AddIdentityResetPass.java ) pass that adds identity resets on all transitions, since some tools' input format requires this explicitly, while some other tools will automatically add such identity resets on transitions. For example, suppose a hybrid automaton has two variables x and y, and has a transition from some mode a to some mode b, with a reset that x' := 0, but does not mention y. Under some assumptions (e.g., controlled and not havoc variables), the typical semantics interpretation of this (and is what SpaceEx's input format does) is that the reset is: x' := 0 /\ y' := y, so that the value of y in the post-state remains unchanged.

Another basic example is the ShortenModeNamesPass.java ( https://github.com/verivital/hyst/blob/master/src/java/com/verivital/hyst/passes/basic/ShortenModeNamesPass.java ) pass, which will decrease the length of the descriptive mode names in a hybrid automaton. This is required for some tools that have string length limitations on mode names (such as 255 characters). Related passes would strip reserved keywords from variable/mode/etc. names, and so on.

#### ECLIPSE SETUP:

You need to compile with antlt-4.4-runtime.jar and several others jars (in /lib) on your classpath. In Eclipse, you do this by going to: 

Project -> Properties -> Java Build Path -> Libraries -> Add External Jar -> select the antlt-4.4-complete.jar file (should be in the project directory) -> Ok

#### RUNNING DIRECTLY FROM .CLASS FILES:

To run the .class files directly, rather than from the .jar, you also need this jar on your classpath (option -cp to java).

*******************************************************************************
Python Interface (by Stanley Bak)
*******************************************************************************

Hyst has an optional interface with python (pythonbridge), which may be required for some transformation passes. To test if python and the required packages are detected correctly, use the -testpython flag from the command line. If python is not setup correctly, you can add the -debug flag to get terminal output to get more insight into the problem.

Currently, Python 2.7 needs to be installed, as well as the following packages:

* sympy: https://github.com/sympy/sympy/releases

* scipy: http://www.scipy.org/install.html

The python executable will be looked for on the paths given in the environment variable HYST_PYTHON_PATH, as well as PATH. It will look for binaries named python2.7 and python.


*******************************************************************************
Kodiak for Optimization (by Stanley Bak)
*******************************************************************************

For validated optimization tasks in Hyst, kodiak is an option. Kodiak is a NASA tool which uses interval branch and bound and Bernstein expansions to come up with upper and lower bounds on a nonlinear function, given interval bounds. To use this, the kodiak executable must be on your PATH or KODIAK_PATH environment variable.

*******************************************************************************
Running the Regression Tests and hypy (by Stanley Bak)
*******************************************************************************

The regression tests make use of hypy, which is a python library for running reachability tools and producing plots. 

#### Description

Hypy can be used to run Hyst (including its transformation passes), as well as various reachability and simulation tools, and then interpret their output. This can then be looped in a python script, enabling high-level hybrid systems analysis which involves running tools multiple times.

#### Setup

The tools may need various packaged to produce plots. You may need to run:
sudo apt-get install gimp plotutils gnuplot

For easy usage, point your PYTHONPATH environment variable to this directory. To setup hypy to run the tools, you'll need to define environment variables to the appropriate binaries. At a minimum (for conversion), you must set HYST_BIN to point to the Hyst jar file. On Ubuntu, in your ~/.profile file you can do something like:

# tool environment variables
TOOL_DIR="/home/stan/tools"
HYST_DIR="/home/stan/repositories/hyst"

export FLOWSTAR_BIN="$TOOL_DIR/flowstar-1.2.3/flowstar"
export SPACEEX_BIN="$TOOL_DIR/spaceex/spaceex"
export DREACH_BIN="$TOOL_DIR/dreach/dReal-2.15.01-linux/bin/dReach"
export HYCREATE_BIN="$TOOL_DIR/tools/HyCreate2.8/HyCreate2.8.jar"

export HYST_BIN="$HYST_DIR/src/Hyst.jar"
export PYTHONPATH="${PYTHONPATH}:$HYST_DIR/src/hybridpy"


#### Pysim

Pysim is a simple simulation library for a hybrid automata. It is written in python, and can be run using hypy.

pysim dependencies:

* scipy: http://www.scipy.org/install.html

* matplotlib:  http://matplotlib.org/users/installing.html

#### Example

A simple hypy script to test if it's working (you may need to adjust your path to the model file) is:


'''
Test hypy on the toy model (path may need to be adjusted)
'''

# assumes hybridpy is on your PYTHONPATH
import hybridpy.hypy as hypy

def main():
    '''run the toy model and produce a plot'''
    
    model = "/home/stan/repositories/hyst/examples/toy/toy.xml"
    out_image = "toy_output.png"
    tool = "pysim" # pysim is the built-in simulator; try flowstar or spaceex

    e = hypy.Engine()
    e.set_model(model) # sets input model path
    e.set_tool(tool) # sets tool name to use
    #e.set_print_terminal_output(True) # print output to terminal? 
    #e.set_save_model_path(converted_model_path) # save converted model?
    e.set_output_image(out_image) # sets output image path
    #e.set_tool_params(["-tp", "jumps=2"]) # sets parameters for hyst conversion

    code = e.run()

    if code != hypy.RUN_CODES.SUCCESS:
        print "engine.run() returned error: " + str(code)
        exit(1)
        
    print "Completed successfully"

if __name__ == "__main__":
    main()


*******************************************************************************
Adding Tools to Hypy:
*******************************************************************************

When you run hypy, it will (1) convert using Hyst and (2) run the desired tool. If that fails, you are better off doing these two steps independently, first just running hyst and producing a model file, then take the model file and running it directly in the tool (one of these two will fail and you can investigate further).

The implementation of hypy is in hyst/src/hybridpy. Each supported tool in hypy has a tool-specific script which provides a common interface to each tool. This tool-specific script is implemented in the tool_*.py file, and it inherits from the HybridTool object (defined in hybrid_tool.ph). You need to write custom functions (override the abstract ones in HybridTool) that (1) run the tool, (2) produce an image at the desired path, and (3) read the output into a python object (optional). Such files already exists for flow*, spaceex, hycreate, and dreach (but that one doesn't produce an image since dreach makes that difficult). For example, you can find the tool-specific file for flowstar in hyst/src/hybridpy/hybridpy/tool_flowstar.py . After you write the tool-specific file, you need to modify hypy.py to add the appropriate import statement for your tool-specific script and add the tool object to the list of known tools near the top of the fily:


# tools for which models can be generated
TOOLS = {'flowstar':FlowstarTool(), 'hycreate':HyCreateTool(), \
         'spaceex':SpaceExTool(), 'dreach':DReachTool()}


Each tool-specific file should be directly runnable from the command line rather than through hypy. If run directly, no hyst conversion is done. This is enabled by adding the following at the bottom of a tool-specific script (for Flow*, for example):

if __name__ == "__main__":
    tool_main(FlowstarTool())

the tool_main is a generic method (defined in hybrid_tool.py), which will call the appropriate methods in the passing-in HybridTool object. If you need extra parameters from the command line, see how it is done in tool_spaceex.py.

Once the tool-specific script is written and you added your tool to hypy.py, you should test by running hypy from a terminal. You should try:

(1) the direct run approach: python /path/to/tool_<toolname>.py <input model> <(optional) output image png path>

(2) the conversion and run approach: python /path/to/hybridpy.py <tool name> <input .xml model> <output image png path>

*******************************************************************************
Model Transformation Passes:
*******************************************************************************

Demonstrations for certain, more complicated, model transformation passes, as well as hypy scripts to recreate results, are provided in the doc/transformation_passes directory. There are READMEs inside each sub-directory which provide additional information about the specific pass being demonstrated.

*****************************************
ADDITIONAL PRINTER DOCUMENTATION
*****************************************
Continuous-Time Stateflow Charts in MathWorks' Simulink/Stateflow
Modes of operation:
* Semantics preserving
* Non-semantics preserving (but preserves semantics if the automaton is deterministic)

Title: Embedding Hybrid Automata into Simulation-Equivalent Simulink/Stateflow Models
Authors: Stanley Bak, Sergiy Bogomolov, Taylor T. Johnson, Luan Viet Nguyen, and Christian Schilling


To run the translator, do the following:

1) Open Matlab
2) Move to the folder where you extracted the files
3) Run the following command (where you enter a meaningful name for MYMODEL and MYCONFIG):
  SpaceExToStateflow('MYMODEL.xml', 'MYCONFIG.cfg', '--folder', 'buckboost', '-s')

This produces a Stateflow model (might take some seconds, especially when the
 Simulink libraries have not been loaded yet).

To run the simulation loop, run the following (parametrized) command:
  simulationLoop('MYMODEL', NUM_SIMULATION, MAX_TIME, NUM_BACKTRACK)

Here the parameters denote the following:
1) the model name (automatically taken from the *.xml file),
2) the number of simulations
3) the maximum simulation time
4) the number of backtrackings


In the following, we list the commands for the main models used in the evaluation:

---

1) Yaw damper model:

Create model:
SpaceExToStateflow('periodic.xml', 'periodic_init_region.cfg', '--folder', 'yaw_damper', '-s');

Run simulations and plot results:
simulationLoop('periodic', 50, 40, 3, 0.00001, {'x4'}, -1, 0);

---

2) Glycemic control model:

Create model:
SpaceExToStateflow('glycemic_control_poly1.xml', 'glycemic_control_poly1.cfg', '--folder', 'glycemic_control_polynomial','-s');

Run simulations and plot results:
simulationLoop('glycemic_control_poly1', 100, 360, 10, 0.001, {'G'}, 1, 1);

---

3) Buck converter model:

Create model:
SpaceExToStateflow('buck_hysteresis_nodcm.xml', 'buck_hysteresis_nodcm.cfg', '--folder', 'buckboost', '-s');

Run simulations and plot results:
simulationLoop('buck_hysteresis_nodcm', 10, 0.5, 3, 0.0001, {'vc'});

---

4) Fischer mutual exclusion model:

UNSAFE:

Create model:
ha = SpaceExToStateflow('fischer_N2_flat_unsafe.xml', 'fischer_N2_flat_unsafe.cfg', '--folder', 'fischer', '-s');

Run simulations:
[time, valuesALL, labels] = simulationLoop('fischer_N2_flat_unsafe', 1000, 1000, 3);

Plot results:
plotExecution;


SAFE:

Create model:
ha = SpaceExToStateflow('fischer_N2_flat_safe.xml', 'fischer_N2_flat_safe.cfg', '--folder', 'fischer', '-s');

Run simulations:
[time, valuesALL, labels] = simulationLoop('fischer_N2_flat_safe', 1000, 1000, 3);

Plot results:
plotExecution;

HYRG README (TO REMOVE):

HyRG: A Random Generation Tool for Affine Hybrid Automata
Luan Viet Nguyen, Christian Schilling, Sergiy Bogomolov, Taylor T. Johnson 
verivital.uta.edu/hyrg/

HyRG has been tested in Matlab 2013b, 2014a, and 2014b.  You call it from a Matlab command prompt as follows.

Function call:
randgen_hybridautomaton(m, n, options)

Inputs:
m	: number of locations
n	: number of variables
options	: string list of options, specified below

Output:
Output SpaceEx model files are generated in ..\examples\rangen

Example calls:

1) The following generates an automaton with 3 state variables and 3 locations, where each mode has unstable dynamic(every mode's A matrix has all eigenvalues are positive real numbers). A generated automaton is state-depedent switching system, and it has no self-transition.

randgen_hybridautomaton(3,3)

2) The following generates an automaton with 4 state variables and 6 locations, where each mode has unstable dynamic(every mode's A matrix has all eigenvalues are positive real numbers). It also generates a SpaceEx model included of this automaton and another time dependent switching system(functions as a global time clock) for sanity checking.  

randgen_hybridautomaton(6,4,'-s')

2) The following generates an automaton with 4 state variables and 20 locations, where each mode has stable dynamic (e.g., every mode's A matrix is Hurwitz). A generated automaton is state-depedent switching sytem, and it has no self-transition.

randgen_hybridautomaton(20,4,'-nr')

3) The following generates an automaton with 3 state variables and 5 locations, where each mode may have different classes of dynamic. A generated automaton is state-depedent switching sytem, and it has no self-transition.

randgen_hybridautomaton(5,3,'-rd')

4) The following generates an automaton with 3 variables and 4 locations. This automaton is time-dependent swithcing('-t'). Each mode may have different classes of dynamic('-rd'). A generated automaton may have self-loop transitions('-g'). Invariants and guard conditions are randomly generated as inequalities of state variables and constant numbers('-ci'). State variables are updated to their random symbolic expression algebra('-sr'). And a generated automaton is also translated to Simulink/Stateflow(SLSF) model('-ss').

Output SLSF files (.mdl) are generated in .\output_SLSF
An SLSF files are automatically hooked up to scopes and can executable without anyadditional manual setting.

randgen_hybridautomaton(4,3,'-rd','-t','-g','-ss','-ci','-sr')


The following options are:
-t 	: randomly generate time-dependent switching system.
-g 	: randomly generate model included self-loop transitions.
-s 	: randomly generate model for sanity checking.
-ss 	: enable translation from SpaceEx XML model to SLSF model.
-ci 	: randomly generate constant invariants, constant guard conditions.
-si 	: randomly generate invariants and guard conditions as symbolic expression algebra of state variables.
-cr 	: update state variables to random constants.
-sr 	: update state variables to their random symbolic expression algebra.


Options for randomly generating different flow dynamics:  

Default	: randomly generate a flow dynamic based on a matrix whose all eigenvalues are positive real numbers. 

-z 	: randomly generate a flow dynamic based on a matrix whose all eigenvalues are zero. 
-n 	: randomly generate a flow dynamic based on a matrix whose all eigenvalues are negative real numbers
-pn 	: randomly generate a flow dynamic based on a matrix whose eigenvalues are either positive or negative real numbers.
-nz	: randomly generate a flow dynamic based on a matrix whose eigenvalues are either zero or negative real numbers.
-pnz 	: randomly generate a flow dynamic based on a matrix whose eigenvalues are positive, zero or negative real numbers.
-pr 	: randomly generate a flow dynamic based on a matrix whose eigenvalues are complex numbers with positive real parts.
-nr 	: generate a flow dynamic based on a matrix whose eigenvalues are complex numbers with negative real parts.
-i 	: generate a flow dynamic based on a matrix whose all eigenvalues are are complex numbers with purely imaginary.
-rd 	: generate a flow dynamic by randomly selecting one of all previous options.

Generating k examples with qualitatively similar behavior to known hybrid systems examples.
Inputs:
* k	: number of trials
* m	: number of locations
* n	: number of variables

Generate bouncing ball examples:

Bouncing ball system has one location and two variables:
ball_production(k,1,2)
E.g if we want to generate 100 examples of bouncing ball system:
ball_production(100,1,2)	


Generate thermostat(heater) system examples:
Themostat system has two locations and one variable
heater_production(k,2,1)
E.g if we want to generate 100 examples of thermostat system:
heater_production(100,2,1)
