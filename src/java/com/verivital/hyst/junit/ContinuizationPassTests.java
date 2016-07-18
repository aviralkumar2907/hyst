package com.verivital.hyst.junit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.verivital.hyst.geometry.HyperPoint;
import com.verivital.hyst.geometry.Interval;
import com.verivital.hyst.geometry.SymbolicStatePoint;
import com.verivital.hyst.grammar.formula.Constant;
import com.verivital.hyst.grammar.formula.Expression;
import com.verivital.hyst.grammar.formula.FormulaParser;
import com.verivital.hyst.ir.Configuration;
import com.verivital.hyst.ir.base.AutomatonMode;
import com.verivital.hyst.ir.base.AutomatonTransition;
import com.verivital.hyst.ir.base.BaseComponent;
import com.verivital.hyst.ir.base.ExpressionInterval;
import com.verivital.hyst.passes.complex.ContinuizationPass;
import com.verivital.hyst.passes.complex.ContinuizationPassTT;
import com.verivital.hyst.python.PythonBridge;
import com.verivital.hyst.util.AutomatonUtil;

@RunWith(Parameterized.class)
public class ContinuizationPassTests
{
	@Before
	public void setUpClass()
	{
		Expression.expressionPrinter = null;
	}

	@Parameters
	public static Collection<Object[]> data()
	{
		return Arrays.asList(new Object[][] { { false }, { true } });
	}

	public ContinuizationPassTests(boolean block)
	{
		PythonBridge.setBlockPython(block);
	}

	/**
	 * Test for the python-base range detection
	 */
	@Test
	public void testPythonRangeTestSim()
	{
		if (!PythonBridge.hasPython())
			return;

		String[][] dynamics = { { "t", "1" }, { "y", "sin(t)" } };
		Configuration c = AutomatonUtil.makeDebugConfiguration(dynamics);

		ArrayList<Interval> timeIntervals = new ArrayList<Interval>();
		timeIntervals.add(new Interval(0, Math.PI / 4.0));
		timeIntervals.add(new Interval(0, 2 * Math.PI));

		SymbolicStatePoint start = new SymbolicStatePoint("on", new HyperPoint(0, 0));

		List<Interval> result = ContinuizationPass.pythonSimulateDerivativeRange(c, "y", start,
				timeIntervals);

		Assert.assertEquals(2, result.size());

		Interval i1 = result.get(0);
		Interval i2 = result.get(1);

		Interval.COMPARE_TOL = 1e-3;

		// integral of sin(t) is cos(t)
		// 0 to pi/4
		Assert.assertEquals(new Interval(0, Math.sqrt(2) / 2.0), i1);
		Assert.assertEquals(new Interval(-1, 1), i2);
	}

	@Test
	public void testContinuizationPassSineWave()
	{
		if (!PythonBridge.hasPython())
			return;

		String[][] dynamics = { { "y", "cos(t)" }, { "t", "1" } };
		Configuration c = AutomatonUtil.makeDebugConfiguration(dynamics);

		String continuizationParam = ContinuizationPass.makeParamString("y", null, 0.1, false,
				Arrays.asList(new Double[] { 1.57, 3.14 }),
				Arrays.asList(new Double[] { 0.2, 0.2 }));

		new ContinuizationPass().runTransformationPass(c, continuizationParam);
		BaseComponent ha = (BaseComponent) c.root;

		// we should have four error modes, and two normal modes
		AutomatonMode on1 = null, on2 = null;
		int numErrorModes = 0;

		for (AutomatonMode am : ha.modes.values())
		{
			if (am.name.equals("on"))
				on1 = am;
			else if (am.name.equals("on_2"))
				on2 = am;
			else if (am.name.contains("error"))
				++numErrorModes;
		}

		Assert.assertNotEquals("on found", null, on1);
		Assert.assertNotEquals("on_2 found", null, on2);
		Assert.assertEquals("four error modes", numErrorModes, 4);
	}

	@Test
	public void testContinuizationPassDoubleIntegrator()
	{
		if (!PythonBridge.hasPython())
			return;

		String[][] dynamics = { { "x", "v", "0.05" }, { "v", "a", "0" },
				{ "a", "-10 * v - 3 * a", "9.5" } };
		Configuration c = AutomatonUtil.makeDebugConfiguration(dynamics);

		String continuizationParam = ContinuizationPass.makeParamString("a", "t", 0.005, false,
				Arrays.asList(new Double[] { 1.5, 5.0 }), Arrays.asList(new Double[] { 4.0, 4.0 }));

		// this relies on hypy and scipy
		new ContinuizationPass().runTransformationPass(c, continuizationParam);
		BaseComponent ha = (BaseComponent) c.root;

		// we should have four error modes, and two normal modes
		AutomatonMode running1 = null, running2 = null;
		int numErrorModes = 0;

		for (AutomatonMode am : ha.modes.values())
		{
			if (am.name.equals("on"))
				running1 = am;
			else if (am.name.equals("on_2"))
				running2 = am;
			else if (am.name.contains("error"))
				++numErrorModes;
		}

		Assert.assertNotEquals("on found", null, running1);
		Assert.assertNotEquals("on_2 found", null, running2);
		Assert.assertEquals("four error modes", numErrorModes, 4);

		// bloated range of a in times 0-1.5 is K=[-6.98, 13.5]
		// [-0.05, 0] * K = [-0.675, 0.349]
		Assert.assertEquals("mode1 v_der.max is 0.163", 0.163,
				running1.flowDynamics.get("v").getInterval().max, 1e-3);
		Assert.assertEquals("mode1 v_der.min is -0.046", -0.046,
				running1.flowDynamics.get("v").getInterval().min, 1e-3);

		Assert.assertEquals("mode2 a_der.max is 0.109", 0.109,
				running2.flowDynamics.get("a").getInterval().max, 1e-3);
		Assert.assertEquals("mode2 a_der.min is -0.075", -0.075,
				running2.flowDynamics.get("a").getInterval().min, 1e-3);
	}

	@Test
	public void testUrgentDoubleIntegrator()
	{
		if (!PythonBridge.hasPython())
			return;

		String[][] dynamics = { { "x", "v", "0" }, { "v", "a", "0" },
				{ "a", "-10 * v - 3 * a", "0" } };
		Configuration c = AutomatonUtil.makeDebugConfiguration(dynamics);

		BaseComponent ha = (BaseComponent) c.root;
		AutomatonMode on = ha.modes.get("on");

		AutomatonMode init = ha.createMode("init");
		init.flowDynamics = null;
		init.invariant = Constant.TRUE;
		init.urgent = true;

		AutomatonTransition at = ha.createTransition(init, on);
		at.guard = Constant.TRUE;
		at.reset.put("a", new ExpressionInterval("10 * (1 - x) + 3 * (-v)"));

		c.init.clear();
		c.init.put("init",
				FormulaParser.parseInitialForbidden("0 <= x <= 0.1 && v == 0 && a == 0"));

		c.validate();

		String continuizationParam = ContinuizationPass.makeParamString("a", "t", 0.005, false,
				Arrays.asList(new Double[] { 1.5, 5.0 }), Arrays.asList(new Double[] { 4.0, 4.0 }));

		// this relies on hypy and scipy
		new ContinuizationPass().runTransformationPass(c, continuizationParam);

		// we should have four error modes, and two normal modes
		AutomatonMode running1 = null, running2 = null;
		int numErrorModes = 0;

		for (AutomatonMode am : ha.modes.values())
		{
			if (am.name.equals("on"))
				running1 = am;
			else if (am.name.equals("on_2"))
				running2 = am;
			else if (am.name.contains("error"))
				++numErrorModes;
		}

		Assert.assertNotEquals("on found", null, running1);
		Assert.assertNotEquals("on_2 found", null, running2);
		Assert.assertEquals("four error modes", numErrorModes, 4);

		Assert.assertTrue("on invariant is correct",
				running1.invariant.toDefaultString().contains("t <= 1.5"));

		Assert.assertTrue("time-triggered invariant is correct",
				running1.invariant.toDefaultString().contains("t <= 1.505"));

		// bloated range of a in times 0-1.5 is K=[-6.98, 13.5]
		// [-0.05, 0] * K = [-0.675, 0.349]

		Assert.assertEquals("mode1 v_der.max is 0.163", 0.163,
				running1.flowDynamics.get("v").getInterval().max, 1e-3);
		Assert.assertEquals("mode1 v_der.min is -0.046", -0.046,
				running1.flowDynamics.get("v").getInterval().min, 1e-3);

		Assert.assertEquals("mode2 a_der.max is 0.109", 0.109,
				running2.flowDynamics.get("a").getInterval().max, 1e-3);
		Assert.assertEquals("mode2 a_der.min is -0.075", -0.075,
				running2.flowDynamics.get("a").getInterval().min, 1e-3);
	}

	@Test
	public void testUrgentDoubleIntegratorSingleDomain()
	{
		if (!PythonBridge.hasPython())
			return;

		String[][] dynamics = { { "x", "v", "0" }, { "v", "a", "0" },
				{ "a", "-10 * v - 3 * a", "0" }, { "ader", "-10*a + 30 * v + 9 * a", "-9.5" } };
		Configuration c = AutomatonUtil.makeDebugConfiguration(dynamics);

		BaseComponent ha = (BaseComponent) c.root;
		AutomatonMode on = ha.modes.get("on");

		AutomatonMode init = ha.createMode("init");
		init.flowDynamics = null;
		init.invariant = Constant.TRUE;
		init.urgent = true;

		AutomatonTransition at = ha.createTransition(init, on);
		at.guard = Constant.TRUE;
		at.reset.put("a", new ExpressionInterval("10 * (1 - x) + 3 * (-v)"));

		c.init.clear();
		c.init.put("init", FormulaParser
				.parseInitialForbidden("0 <= x <= 0.1 && v == 0 && a == 0 && ader == 0"));

		c.validate();

		String continuizationParam = ContinuizationPass.makeParamString("a", null, 0.005, false,
				Arrays.asList(new Double[] { 5.0 }), Arrays.asList(new Double[] { 4.0 }));

		// this relies on hypy and scipy
		new ContinuizationPass().runTransformationPass(c, continuizationParam);
	}

	@Test
	public void testContinuizationPassTimeTriggered()
	{
		if (!PythonBridge.hasPython())
			return;

		String[][] dynamics = { { "x", "v", "0.05" }, { "v", "a", "0" }, { "a", "0", "9.5" },
				{ "t", "1", "0" }, { "clock", "1", "0" } };
		Configuration c = AutomatonUtil.makeDebugConfiguration(dynamics);
		BaseComponent ha = (BaseComponent) c.root;
		AutomatonMode am = ha.modes.values().iterator().next();

		double PERIOD = 0.005;
		c.settings.spaceExConfig.timeHorizon = 5;
		am.invariant = FormulaParser.parseInvariant("t <= 5 && clock <= " + PERIOD);

		AutomatonTransition at = ha.createTransition(am, am);
		at.guard = FormulaParser.parseGuard("clock >= " + PERIOD);
		at.reset.put("clock", new ExpressionInterval(0));
		at.reset.put("a", new ExpressionInterval("10 - 10*x - 3*v"));

		double TIME_STEP = 5.0;
		double BLOAT = 4;
		String params = ContinuizationPassTT.makeParamString(false, TIME_STEP, BLOAT);
		// this relies on hypy and scipy

		new ContinuizationPassTT().runTransformationPass(c, params);

		// we should have three error modes, and one normal mode
		int numErrorModes = 0;

		for (AutomatonMode m : ha.modes.values())
		{
			if (m.name.contains("error"))
				++numErrorModes;
		}

		Assert.assertEquals("three modes", 3, ha.modes.size());
		Assert.assertEquals("two error modes", 2, numErrorModes);

		// v' = 10 - 10*x - 3*v + [-36, 8] * [-0.05, 0]
		ExpressionInterval vDer = am.flowDynamics.get("v");
		Assert.assertEquals("v der expression part is correct", "10 - 10 * x - 3 * v",
				vDer.getExpression().toDefaultString());

		Interval i = vDer.getInterval();
		Assert.assertNotNull("v-der does not have interval", i);

		Interval aRange = new Interval(-2.989, 9.472); // from matlab simulation
		aRange.min -= BLOAT;
		aRange.max += BLOAT;
		Interval expectedI = Interval.mult(aRange, new Interval(-PERIOD, 0));

		Assert.assertEquals("v_der-interval.max was wrong", expectedI.max, i.max, 1e-3);
		Assert.assertEquals("v_der-interval.min was wrong", expectedI.min, i.min, 1e-3);
	}

	@Test
	public void testContinuizationPassTimeTriggeredMultiInterval()
	{
		if (!PythonBridge.hasPython())
			return;

		String[][] dynamics = { { "x", "v", "0.05" }, { "v", "a", "0" }, { "a", "0", "9.5" },
				{ "t", "1", "0" }, { "clock", "1", "0" } };
		Configuration c = AutomatonUtil.makeDebugConfiguration(dynamics);
		c.settings.spaceExConfig.timeHorizon = 5;
		BaseComponent ha = (BaseComponent) c.root;
		AutomatonMode am = ha.modes.values().iterator().next();

		double PERIOD = 0.005;
		am.invariant = FormulaParser.parseInvariant("t <= 5 && clock <= " + PERIOD);

		AutomatonTransition trans = ha.createTransition(am, am);
		trans.guard = FormulaParser.parseGuard("clock >= " + PERIOD);
		trans.reset.put("clock", new ExpressionInterval(0));
		trans.reset.put("a", new ExpressionInterval("10 - 10*x - 3*v"));

		double TIME_STEP = 2.5;
		double BLOAT = 4;
		String params = ContinuizationPassTT.makeParamString(false, TIME_STEP, BLOAT);
		// this relies on hypy and scipy

		new ContinuizationPassTT().runTransformationPass(c, params);

		// we should have three error modes, and one normal mode
		int numErrorModes = 0;
		AutomatonMode am2 = null;

		for (AutomatonMode m : ha.modes.values())
		{
			if (m.name.contains("error"))
				++numErrorModes;

			if (m.name.equals("on"))
				am = m;

			if (m.name.equals("on_2"))
				am2 = m;
		}

		Assert.assertNotNull("mode 'on' not found", am);
		Assert.assertNotNull("mode 'on2' not found", am2);

		Assert.assertEquals("size modes", 6, ha.modes.size());
		Assert.assertEquals("four error modes", 4, numErrorModes);

		// from matlab simulation:
		// first part a_range = [-2.989, 9.472]
		// second part a_range = [-0.102, 0.099]
		Interval[] aRanges = { new Interval(-2.989, 9.472), new Interval(-0.102, 0.099) };

		AutomatonMode[] modes = { am, am2 };

		for (int i = 0; i < 2; ++i)
		{
			AutomatonMode m = modes[i];
			// v' = 10 - 10*x - 3*v + [-36, 8] * [-0.05, 0]
			ExpressionInterval vDer = m.flowDynamics.get("v");
			Assert.assertEquals("v der expression part is correct in " + m.name,
					"10 - 10 * x - 3 * v", vDer.getExpression().toDefaultString());

			Interval in = vDer.getInterval();
			Assert.assertNotNull("v-der in " + m.name + " does not have interval", in);

			Interval aRange = aRanges[i];
			aRange.min -= BLOAT;
			aRange.max += BLOAT;
			Interval expectedI = Interval.mult(aRange, new Interval(-PERIOD, 0));

			Assert.assertEquals("v_der-interval.max was wrong in mode " + m.name, expectedI.max,
					in.max, 1e-3);
			Assert.assertEquals("v_der-interval.min was wrong in mode " + m.name, expectedI.min,
					in.min, 1e-3);
		}

		AutomatonTransition at = null;

		for (AutomatonTransition t : ha.transitions)
		{
			if (t.from == am && t.to == am2)
			{
				at = t;
				break;
			}
		}

		Assert.assertNotNull("transition 'on' -> 'on_2' not found", at);

		Assert.assertFalse("Clock should not be a variable", ha.variables.contains("clock"));

		Assert.assertNull("There shouldn't be any reset to t on transition", at.reset.get("t"));

		Assert.assertTrue("Transition time should be 2.5",
				at.guard.toDefaultString().contains("t >= 2.5"));

		Assert.assertTrue("invariant should have t <= 2.5",
				am.invariant.toDefaultString().contains("t <= 2.5"));
	}
}
