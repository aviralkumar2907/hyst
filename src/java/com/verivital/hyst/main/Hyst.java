package com.verivital.hyst.main;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.verivital.hyst.generators.IntegralChainGenerator;
import com.verivital.hyst.generators.ModelGenerator;
import com.verivital.hyst.generators.NavigationGenerator;
import com.verivital.hyst.grammar.formula.Expression;
import com.verivital.hyst.importer.ConfigurationMaker;
import com.verivital.hyst.importer.SpaceExImporter;
import com.verivital.hyst.importer.TemplateImporter;
import com.verivital.hyst.ir.AutomatonExportException;
import com.verivital.hyst.ir.Component;
import com.verivital.hyst.ir.Configuration;
import com.verivital.hyst.passes.TransformationPass;
import com.verivital.hyst.passes.basic.AddIdentityResetPass;
import com.verivital.hyst.passes.basic.RemoveSimpleUnsatInvariantsPass;
import com.verivital.hyst.passes.basic.ShortenModeNamesPass;
import com.verivital.hyst.passes.basic.SimplifyExpressionsPass;
import com.verivital.hyst.passes.basic.SplitDisjunctionGuardsPass;
import com.verivital.hyst.passes.basic.SubstituteConstantsPass;
import com.verivital.hyst.passes.basic.TimeScalePass;
import com.verivital.hyst.passes.complex.ContinuizationPass;
import com.verivital.hyst.passes.complex.ContinuizationPassTT;
import com.verivital.hyst.passes.complex.ConvertLutFlowsPass;
import com.verivital.hyst.passes.complex.FlattenAutomatonPass;
import com.verivital.hyst.passes.complex.OrderReductionPass;
import com.verivital.hyst.passes.complex.hybridize.HybridizeMixedTriggeredPass;
import com.verivital.hyst.passes.complex.pi.PseudoInvariantPass;
import com.verivital.hyst.passes.complex.pi.PseudoInvariantSimulatePass;
import com.verivital.hyst.printers.DReachPrinter;
import com.verivital.hyst.printers.FlowstarPrinter;
import com.verivital.hyst.printers.HyCompPrinter;
import com.verivital.hyst.printers.PySimPrinter;
import com.verivital.hyst.printers.PythonQBMCPrinter;
import com.verivital.hyst.printers.SimulinkStateflowPrinter;
import com.verivital.hyst.printers.SpaceExPrinter;
import com.verivital.hyst.printers.ToolPrinter;
import com.verivital.hyst.printers.hycreate2.HyCreate2Printer;
import com.verivital.hyst.python.PythonBridge;
import com.verivital.hyst.util.Preconditions.PreconditionsFailedException;
import com.verivital.hyst.util.StringOperations;

import de.uni_freiburg.informatik.swt.sxhybridautomaton.SpaceExDocument;

/**
 * Main start class for Hyst If run without args, a GUI will be used. If run
 * with args, the command-line version is assumed.
 */
public class Hyst
{
	public static String TOOL_NAME = "Hyst v1.2";
	public static String programArguments;

	private static ArrayList<String> xmlFilenames = new ArrayList<String>();
	private static String cfgFilename = null, outputFilename = null;
	private static int printerIndex = -1; // index into printers array
	public static boolean verboseMode = false; // flag used to toggle verbose
												// printing with Main.log()
	public static boolean debugMode = false; // flag used to toggle debug
												// printing with Main.logDebug()
	private static String toolParamsString = null; // tool parameter string set
													// using -toolparams or -tp
	private static int modelGenIndex = -1; // index into generators array
	private static String modelGenParam = null; // parameter for model generator

	public static boolean IS_UNIT_TEST = false; // should usage printing be
												// omitted (for unit testing)
	private static HystFrame guiFrame = null; // set if gui mode is being used

	public final static String FLAG_HELP = "-help";
	public final static String FLAG_GUI = "-gui";
	public final static String FLAG_TOOLPARAMS = "-toolparams";
	public final static String FLAG_TOOLPARAMS_SHORT = "-tp";
	public final static String FLAG_HELP_SHORT = "-h";
	public final static String FLAG_VERBOSE = "-verbose";
	public final static String FLAG_VERBOSE_SHORT = "-v";
	public final static String FLAG_DEBUG = "-debug";
	public final static String FLAG_DEBUG_SHORT = "-d";
	public final static String FLAG_NOVALIDATE = "-novalidate";
	public final static String FLAG_OUTPUT = "-o";
	public final static String FLAG_TESTPYTHON = "-testpython";
	public final static String FLAG_GENERATE = "-generate";
	public final static String FLAG_GENERATE_SHORT = "-gen";

	// add new tool support here
	private static final ToolPrinter[] printers = { new FlowstarPrinter(), new DReachPrinter(),
			new HyCreate2Printer(), new HyCompPrinter(), new PythonQBMCPrinter(),
			new SpaceExPrinter(), new SimulinkStateflowPrinter(), new PySimPrinter(), };

	// passes that are run only if the user selects them
	private static final TransformationPass[] availablePasses = { new AddIdentityResetPass(),
			new PseudoInvariantPass(), new PseudoInvariantSimulatePass(), new TimeScalePass(),
			new SubstituteConstantsPass(), new SimplifyExpressionsPass(),
			new SplitDisjunctionGuardsPass(), new RemoveSimpleUnsatInvariantsPass(),
			new ShortenModeNamesPass(), new ContinuizationPass(), new ContinuizationPassTT(),
			new HybridizeMixedTriggeredPass(), new FlattenAutomatonPass(), new OrderReductionPass(),
			new ConvertLutFlowsPass(), };

	private static final ModelGenerator[] generators = { new IntegralChainGenerator(),
			new NavigationGenerator(), };

	// passes that the user has selected
	private static ArrayList<RequestedTransformationPass> requestedPasses = new ArrayList<RequestedTransformationPass>();

	public enum ExitCode
	{
		SUCCESS, // 0
		EXPORT_EXCEPTION, // 1
		PRECONDITIONS_EXCEPTION, // 2
		ARG_PARSE_ERROR, INTERNAL_ERROR, GUI_QUIT, EXPORT_AUTOMATON_EXCEPTION, NOPYTHON // exit
																						// if
																						// -checkpython
																						// fails
	};

	public static void main(String[] args)
	{
		if (!checkPrintersPasses())
			System.exit(ExitCode.INTERNAL_ERROR.ordinal());

		if (args.length > 0 && !args[0].equals(FLAG_GUI))
		{
			int code = convert(args);

			System.exit(code);
		}
		else
		{
			final String loadFilename = (args.length >= 2) ? args[1] : null;

			// use gui
			System.out.println(
					"Started in GUI mode. For command-line help use the " + FLAG_HELP + " flag.");

			fixLookAndFeel();

			SwingUtilities.invokeLater(new Runnable()
			{

				@Override
				public void run()
				{
					guiFrame = new HystFrame(printers, availablePasses);

					if (loadFilename != null)
						guiFrame.guiLoad(loadFilename);

					guiFrame.setVisible(true);
				}
			});
		}
	}

	/**
	 * Main conversion thread
	 * 
	 * @param args
	 * @return the exit code
	 */
	public static int convert(String[] args)
	{
		resetVars();

		if (!parseArgs(args))
			return ExitCode.ARG_PARSE_ERROR.ordinal();

		if (debugMode)
			log("Debug mode (even more verbose) printing enabled.\n");
		else if (verboseMode)
			log("Verbose mode printing enabled.\n");

		programArguments = makeSingleArgument(args);
		Expression.expressionPrinter = null; // this should be assigned by the
												// pass / printer as needed

		long startMs = System.currentTimeMillis();
		ToolPrinter printer = newToolPrinterInstance(printers[printerIndex]);

		try
		{
			Configuration config = null;
			if (modelGenIndex == -1)
			{
				// 1. import the SpaceExDocument
				SpaceExDocument spaceExDoc = SpaceExImporter.importModels(cfgFilename,
						xmlFilenames.toArray(new String[xmlFilenames.size()]));

				// 2. convert the SpaceEx data structures to template automata
				Map<String, Component> componentTemplates = TemplateImporter
						.createComponentTemplates(spaceExDoc);

				// 3. run any component template passes here (future)

				// 4. instantiate the component templates into a networked
				// configuration
				config = ConfigurationMaker.fromSpaceEx(spaceExDoc, componentTemplates);
			}
			else
			{
				ModelGenerator gen = generators[modelGenIndex];

				config = gen.generate(modelGenParam);
			}

			// 5. run passes
			runPasses(config);

			// 6. run printer
			runPrinter(printer, config);
		}
		catch (AutomatonExportException aee)
		{
			logError("Automaton Export Exception while exporting: " + aee.getLocalizedMessage());

			if (verboseMode)
			{
				log("Stack trace from exception:");

				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				aee.printStackTrace(pw);
				log(sw.toString());
			}
			else
				logError("For more information about the error, use the -verbose or -debug flag.");

			return ExitCode.EXPORT_AUTOMATON_EXCEPTION.ordinal();
		}
		catch (PreconditionsFailedException ex)
		{
			logError("Preconditions not met for exporting: " + ex.getLocalizedMessage());

			if (verboseMode)
			{
				log("Stack trace from exception:");

				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				ex.printStackTrace(pw);
				log(sw.toString());
			}

			return ExitCode.PRECONDITIONS_EXCEPTION.ordinal();
		}
		catch (Exception ex)
		{
			String message = ex.getLocalizedMessage() != null ? ex.getLocalizedMessage()
					: ex.toString();
			logError("Exception in Hyst while exporting: " + message);

			if (verboseMode)
			{
				log("Stack trace from exception:");

				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				ex.printStackTrace(pw);
				log(sw.toString());
			}
			else
				logError("For more information about the error, use the -verbose or -debug flag.");

			return ExitCode.EXPORT_EXCEPTION.ordinal();
		}

		long difMs = System.currentTimeMillis() - startMs;

		printer.flush();
		Hyst.logInfo("\nFinished converting in " + difMs + " ms");

		return ExitCode.SUCCESS.ordinal();
	}

	private static void runPrinter(ToolPrinter printer, Configuration config)
	{
		String originalFilename = StringOperations.join(" ", xmlFilenames.toArray(new String[] {}));

		if (outputFilename != null)
			printer.setOutputFile(outputFilename);
		else if (guiFrame != null)
			printer.setOutputGui(guiFrame);

		printer.print(config, toolParamsString, originalFilename);
	}

	private static void runPasses(Configuration config)
	{
		for (RequestedTransformationPass rp : requestedPasses)
		{
			Hyst.log("Running pass " + rp.tp.getName() + " with params " + rp.params);

			rp.tp.runTransformationPass(config, rp.params);

			Hyst.logDebug("\n----------After running pass " + rp.tp.getName()
					+ ", configuration is:\n" + config);
		}
	}

	private static void resetVars()
	{
		xmlFilenames = new ArrayList<String>();
		cfgFilename = null;
		outputFilename = null;
		printerIndex = -1;
		verboseMode = false;
		debugMode = false;
		toolParamsString = "";
		requestedPasses.clear();
	}

	private static void fixLookAndFeel()
	{
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (Exception e)
		{
		}
	}

	public static String makeSingleArgument(String[] ar)
	{
		String rv = "";

		for (String s : ar)
		{
			if (rv.length() != 0)
				rv += " ";

			// escape spaces and special handling of empty string
			if (s.length() == 0)
				rv += "\"\"";
			else if (s.contains(" "))
				rv += "\"" + s + "\"";
			else
				rv += s;
		}

		return rv;
	}

	/**
	 * Parse arguments, return TRUE if they're alright, FALSE if not
	 * 
	 * @param args
	 * @return
	 */
	private static boolean parseArgs(String[] args)
	{
		boolean testPython = false;
		boolean rv = true;
		boolean quitAfterUsage = false;

		for (int i = 0; i < args.length; i++)
		{
			String arg = args[i];

			boolean processedArg = false;

			for (int pi = 0; pi < printers.length; ++pi)
			{
				String flag = printers[pi].getCommandLineFlag();

				if (flag.equals(arg))
				{
					if (printerIndex != -1)
					{
						String other = printers[printerIndex].getCommandLineFlag();

						logError("Error: multiple printers selected " + arg + " and " + other);
						rv = false;
					}
					else
					{
						printerIndex = pi;
						processedArg = true;
					}
				}
			}

			if (processedArg)
				continue;

			for (TransformationPass tp : availablePasses)
			{
				if (tp.getCommandLineFlag().equals(arg))
				{
					if (i + 1 >= args.length)
					{
						logError("Error: Custom pass flags always needs a subsequent argument: "
								+ arg);
						rv = false;
					}
					else
					{
						String passParam = args[++i];

						TransformationPass instance = newTransformationPassInstance(tp);
						requestedPasses.add(new RequestedTransformationPass(instance, passParam));
					}

					processedArg = true;
				}
			}

			if (processedArg)
				continue;

			if (arg.equals(FLAG_HELP) || arg.equals(FLAG_HELP_SHORT) || arg.equals(FLAG_GUI))
				quitAfterUsage = true; // ignore
			else if (arg.equals(FLAG_VERBOSE) || arg.equals(FLAG_VERBOSE_SHORT))
				verboseMode = true;
			else if (arg.equals(FLAG_TESTPYTHON))
				testPython = true;
			else if (arg.equals(FLAG_DEBUG) || arg.equals(FLAG_DEBUG_SHORT))
			{
				verboseMode = true;
				debugMode = true;
			}
			else if (arg.equals(FLAG_TOOLPARAMS) || arg.equals(FLAG_TOOLPARAMS_SHORT))
			{
				if (toolParamsString.length() > 0)
				{
					logError("Error: " + FLAG_TOOLPARAMS + " argument used twice.");
					rv = false;
				}
				else if (i + 1 < args.length)
				{
					toolParamsString = args[++i];
				}
				else
				{
					logError("Error: " + FLAG_TOOLPARAMS + " argument expects parameter after");
					rv = false;
				}
			}
			else if (arg.equals(FLAG_NOVALIDATE))
			{
				Hyst.setModeNoValidate();
			}
			else if (arg.equals(FLAG_OUTPUT))
			{
				if (i + 1 < args.length)
				{
					outputFilename = args[++i];
				}
				else
				{
					logError("Error: " + FLAG_OUTPUT + " argument expects filename after");
					rv = false;
				}

			}
			else if (arg.equals(FLAG_GENERATE) || arg.equals(FLAG_GENERATE_SHORT))
			{
				if (i + 2 < args.length)
				{
					String genName = args[++i];

					for (int index = 0; index < generators.length; ++index)
					{
						ModelGenerator g = generators[index];

						if (g.getCommandLineFlag().equals(genName))
						{
							modelGenIndex = index;
							break;
						}
					}

					if (modelGenIndex == -1)
					{
						logError("Error: Model Generator with argument '" + genName
								+ "' was not found.");
						rv = false;
					}

					modelGenParam = args[++i];
				}
				else
				{
					logError("Error: " + FLAG_GENERATE
							+ " argument expects <name> and <param> after.");
					rv = false;
				}
			}
			else if (arg.endsWith(".xml"))
			{
				xmlFilenames.add(arg);

				if (cfgFilename == null)
				{
					String base = arg.substring(0, arg.length() - 4);
					cfgFilename = base + ".cfg";
				}
			}
			else if (arg.endsWith(".cfg"))
				cfgFilename = arg;
			else
			{
				logError("Error: Unknown argument: " + arg);
				rv = false;
			}
		}

		if (testPython)
		{
			if (PythonBridge.hasPython())
			{
				System.out.println("Python and required packages successfully detected.");
				System.exit(ExitCode.SUCCESS.ordinal());
			}
			else
			{
				System.out.println("Python and all required packages NOT detected.");
				System.out.println(PythonBridge.getInstanceErrorString);
				System.exit(ExitCode.NOPYTHON.ordinal());
			}
		}

		if (!rv || ((xmlFilenames.size() == 0 || cfgFilename == null) && modelGenIndex == -1)
				|| printerIndex < 0 || printerIndex >= printers.length)
		{
			if (IS_UNIT_TEST)
				return false;

			// show usage

			System.out.println(TOOL_NAME);
			System.out.println("Usage:");
			System.out.println("hyst [OutputType] (args) XMLFilename(s) " + "(CFGFilename)");
			System.out.println();
			System.out.println("OutputType:");

			for (ToolPrinter tp : printers)
			{
				String arg = tp.getCommandLineFlag();

				String experimental = tp.isInRelease() ? "" : "(Experimental)";
				System.out.println(
						"\t" + arg + " " + tp.getToolName() + " " + experimental + " format");

				// also print the default params
				Map<String, String> params = tp.getDefaultParams();

				if (params != null)
				{
					System.out.print("\t\t" + FLAG_TOOLPARAMS + " ");
					boolean first = true;

					for (Entry<String, String> e : params.entrySet())
					{
						if (first)
							first = false;
						else
							System.out.print(":");

						System.out.print(e.getKey() + "=" + e.getValue());
					}
					System.out.println();
				}
			}

			System.out.println("\nAvailable Model Transformation Passes:");

			for (TransformationPass tp : availablePasses)
			{
				String p = tp.getParamHelp();

				if (p == null)
					p = "[no param]";

				System.out.println("\t" + tp.getCommandLineFlag() + " " + tp.getName() + " " + p);
			}

			System.out.println();
			System.out.println(FLAG_TOOLPARAMS
					+ " name1=val1:name2=val2:... Specify printer-specific parameters");

			System.out.println(FLAG_GENERATE
					+ " [type] [generate_params] Generate a model instead of loading from a file");
			System.out.println("\nAvailable Models:");

			for (ModelGenerator mg : generators)
			{
				String p = mg.getParamHelp();

				if (p == null)
					p = "[no param]";

				System.out.println("\t" + mg.getCommandLineFlag() + " " + mg.getName() + " " + p);
			}

			System.out.println();
			System.out.println(FLAG_HELP + " show this command-line help text");
			System.out.println(FLAG_GUI + " [filename] force gui mode with the given input model");
			System.out.println(FLAG_VERBOSE + " Enable verbose printing");
			System.out.println(FLAG_DEBUG + " Enable debug printing (even more verbose)");
			System.out.println(FLAG_NOVALIDATE
					+ " skip internal model validation (may result in Exceptions being thrown)");
			System.out.println(FLAG_OUTPUT + " [filename] output to the given filename");
			System.out
					.println("XMLFilename: The SpaceEx XML automaton to be " + "processed (*.xml)");
			System.out.println("CFGFilename: The automaton's config file. Will "
					+ "be derived from the XML filename if not explicitly stated (*.cfg)");
			rv = false;
		}

		if (quitAfterUsage) // if -help was used
			System.exit(ExitCode.SUCCESS.ordinal());

		return rv;
	}

	private static TransformationPass newTransformationPassInstance(TransformationPass tp)
	{
		// create a new instance of the transformation pass to give it fresh
		// state
		Class<? extends TransformationPass> cl = tp.getClass();
		Constructor<? extends TransformationPass> ctor;
		TransformationPass instance = null;

		try
		{
			ctor = cl.getConstructor();
			instance = ctor.newInstance();
		}
		catch (NoSuchMethodException e)
		{
			throw new AutomatonExportException("Error instantiating TransformationPass", e);
		}
		catch (InstantiationException e2)
		{
			throw new AutomatonExportException("Error instantiating TransformationPass", e2);
		}
		catch (IllegalArgumentException e3)
		{
			throw new AutomatonExportException("Error instantiating TransformationPass", e3);
		}
		catch (IllegalAccessException e4)
		{
			throw new AutomatonExportException("Error instantiating TransformationPass", e4);
		}
		catch (InvocationTargetException e5)
		{
			throw new AutomatonExportException("Error instantiating TransformationPass", e5);
		}

		return instance;
	}

	private static ToolPrinter newToolPrinterInstance(ToolPrinter tp)
	{
		// create a new instance of the transformation pass to give it fresh
		// state
		Class<? extends ToolPrinter> cl = tp.getClass();
		Constructor<? extends ToolPrinter> ctor;
		ToolPrinter instance = null;

		try
		{
			ctor = cl.getConstructor();
			instance = ctor.newInstance();
		}
		catch (NoSuchMethodException e)
		{
			throw new AutomatonExportException("Error instantiating TransformationPass", e);
		}
		catch (InstantiationException e2)
		{
			throw new AutomatonExportException("Error instantiating TransformationPass", e2);
		}
		catch (IllegalArgumentException e3)
		{
			throw new AutomatonExportException("Error instantiating TransformationPass", e3);
		}
		catch (IllegalAccessException e4)
		{
			throw new AutomatonExportException("Error instantiating TransformationPass", e4);
		}
		catch (InvocationTargetException e5)
		{
			throw new AutomatonExportException("Error instantiating TransformationPass", e5);
		}

		return instance;
	}

	/**
	 * Check for internal consistency of the defined Printers and
	 * TransformationPasses
	 * 
	 * @return true if they are internally consistent
	 */
	private static boolean checkPrintersPasses()
	{
		boolean rv = true;

		// make sure command line flags do not collide
		TreeMap<String, String> flags = new TreeMap<String, String>();

		flags.put(FLAG_GUI, "force gui flag");
		flags.put(FLAG_HELP, "help flag");
		flags.put(FLAG_HELP_SHORT, "help flag (short version)");
		flags.put(FLAG_VERBOSE, "verbose printing mode flag");
		flags.put(FLAG_VERBOSE_SHORT, "verbose printing mode flag (short version)");
		flags.put(FLAG_DEBUG, "debug printing mode flag");
		flags.put(FLAG_DEBUG_SHORT, "debug printing mode flag (short version)");
		flags.put(FLAG_NOVALIDATE, "no validation flag");
		flags.put(FLAG_OUTPUT, "output to filename flag");
		flags.put(FLAG_TOOLPARAMS, "tool params flag");
		flags.put(FLAG_TOOLPARAMS_SHORT, "tool params flag (short version)");

		for (ToolPrinter tp : printers)
		{
			String name = "tool argument for " + tp.getToolName();
			String arg = tp.getCommandLineFlag();

			if (flags.get(arg) != null)
			{
				logError("Error: Command-line argument " + arg + " is defined both as " + name
						+ " as well as " + flags.get(arg));
				rv = false;
			}
			else
				flags.put(arg, name);
		}

		if (rv)
		{
			for (TransformationPass p : availablePasses)
			{
				String name = "transformation pass argument for " + p.getClass().getName();
				String arg = p.getCommandLineFlag();

				// command line argument must be defined for optionalPasses
				if (arg == null)
				{
					rv = false;
					logError(
							"Command-line flag for " + p.getClass().getName() + " is not defined.");
				}
				else
				{
					if (flags.get(arg) != null)
					{
						logError("Error: Command-line argument " + arg + " is defined both as "
								+ name + " as well as " + flags.get(arg));
						rv = false;
					}
					else
						flags.put(arg, name);
				}

				if (p.getName() == null)
				{
					rv = false;
					logError("getName() for pass " + p.getClass().getName() + " is not defined.");
				}
			}
		}

		return rv;
	}

	/**
	 * Print an info message to stderr, if the -v flag has been set (verbose
	 * mode is enabled)
	 * 
	 * @param message
	 *            the message to print
	 */
	public static void log(String message)
	{
		if (verboseMode || debugMode)
		{
			if (guiFrame != null)
				guiFrame.addOutput(message);

			System.err.println(message);
		}
	}

	/**
	 * Print an info message to stderr, regardless of verbose / debug flags
	 * 
	 * @param message
	 *            the message to print
	 */
	public static void logInfo(String message)
	{
		if (guiFrame != null)
			guiFrame.addOutput(message);
		else
			System.err.println(message);
	}

	/**
	 * Print an info message to stderr, if the -d flag has been set (debug mode
	 * is enabled). This is even more verbose
	 * 
	 * @param message
	 *            the message to print
	 */
	public static void logDebug(String message)
	{
		if (debugMode)
		{
			if (guiFrame != null)
				guiFrame.addOutput(message);

			System.err.println(message);
		}
	}

	/**
	 * Print an error message to stderr
	 * 
	 * @param message
	 *            the message to print
	 */
	public static void logError(String message)
	{
		if (guiFrame != null)
			guiFrame.addOutput(message);

		System.err.println(message);
	}

	/**
	 * Disable internal model validation (and removes some error checking)
	 */
	public static void setModeNoValidate()
	{
		Configuration.DO_VALIDATION = false;
		System.err.println("Internal model validatation disabled.");
	}
}
