package org.openpnp.machine.reference.driver.wizards;

import java.util.Locale;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerListModel;
import javax.swing.border.TitledBorder;

import org.openpnp.gui.components.ComponentDecorators;
import org.openpnp.gui.support.AbstractConfigurationWizard;
import org.openpnp.gui.support.DoubleConverter;
import org.openpnp.gui.support.IntegerConverter;
import org.openpnp.machine.reference.driver.TinygDriver;

import com.google.gson.JsonObject;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;

public class TinygDriverConfigurationWizard extends AbstractConfigurationWizard
{
	public class ConfigProxy
	{
		// [1ma] m1 map to axis 0 [0=X,1=Y,2=Z...]
		// [1sa] m1 step angle 1.800 deg
		// [1tr] m1 travel per revolution 1.250 mm
		// [1mi] m1 microsteps 8 [1,2,4,8]
		// [1po] m1 polarity 0 [0=normal,1=reverse]
		// [1pm] m1 power management 1 [0=off,1=on]
		// tinyg [mm] ok>

		// [xam] x axis mode 1 [standard]
		// [xvm] x velocity maximum 5000.000 mm/min
		// [xfr] x feedrate maximum 5000.000 mm/min
		// [xtm] x travel maximum 150.000 mm
		// [xjm] x jerk maximum 20000000 mm/min^3
		// [xjh] x jerk homing 20000000 mm/min^3
		// [xjd] x junction deviation 0.0500 mm (larger is faster)
		// [xsn] x switch min 1 [0=off,1=homing,2=limit,3=limit+homing]
		// [xsx] x switch max 0 [0=off,1=homing,2=limit,3=limit+homing]
		// [xsv] x search velocity 500.000 mm/min
		// [xlv] x latch velocity 100.000 mm/min
		// [xlb] x latch backoff 2.000 mm
		// [xzb] x zero backoff 1.000 mm
		// tinyg [mm] ok>

		private boolean getConfigBoolean(String name) throws Exception
		{
			return this.getConfigInt(name) == 1;
		}

		private double getConfigDouble(String name) throws Exception
		{
			JsonObject o = TinygDriverConfigurationWizard.this.driver.sendCommand(String.format(Locale.US, "{\"%s\":\"\"}", name));
			return o.get(name).getAsDouble();
		}

		// TODO: Check for response errors in these methods.
		private int getConfigInt(String name) throws Exception
		{
			JsonObject o = TinygDriverConfigurationWizard.this.driver.sendCommand(String.format(Locale.US, "{\"%s\":\"\"}", name));
			return o.get(name).getAsInt();
		}

		public int getFeedMaxX() throws Exception
		{
			return this.getConfigInt("xfr");
		}

		public int getFeedMaxY() throws Exception
		{
			return this.getConfigInt("yfr");
		}

		public int getJerkMaxX() throws Exception
		{
			return this.getConfigInt("xjm");
		}

		public int getJerkMaxY() throws Exception
		{
			return this.getConfigInt("yjm");
		}

		public boolean getPolarityReversedM1() throws Exception
		{
			return this.getConfigBoolean("1po");
		}

		public boolean getPolarityReversedM2() throws Exception
		{
			return this.getConfigBoolean("2po");
		}

		public boolean getPolarityReversedM3() throws Exception
		{
			return this.getConfigBoolean("3po");
		}

		public boolean getPolarityReversedM4() throws Exception
		{
			return this.getConfigBoolean("4po");
		}

		public boolean getPowerMgmtM1() throws Exception
		{
			return this.getConfigBoolean("1pm");
		}

		public boolean getPowerMgmtM2() throws Exception
		{
			return this.getConfigBoolean("2pm");
		}

		public boolean getPowerMgmtM3() throws Exception
		{
			return this.getConfigBoolean("3pm");
		}

		public boolean getPowerMgmtM4() throws Exception
		{
			return this.getConfigBoolean("4pm");
		}

		public double getStepAngleM1() throws Exception
		{
			return this.getConfigDouble("1sa");
		}

		public double getStepAngleM2() throws Exception
		{
			return this.getConfigDouble("2sa");
		}

		public double getStepAngleM3() throws Exception
		{
			return this.getConfigDouble("3sa");
		}

		public double getStepAngleM4() throws Exception
		{
			return this.getConfigDouble("4sa");
		}

		public double getTravelPerRevM1() throws Exception
		{
			return this.getConfigDouble("1tr");
		}

		public double getTravelPerRevM2() throws Exception
		{
			return this.getConfigDouble("2tr");
		}

		public double getTravelPerRevM3() throws Exception
		{
			return this.getConfigDouble("3tr");
		}

		public double getTravelPerRevM4() throws Exception
		{
			return this.getConfigDouble("4tr");
		}

		public int getVelMaxX() throws Exception
		{
			return this.getConfigInt("xvm");
		}

		public int getVelMaxY() throws Exception
		{
			return this.getConfigInt("yvm");
		}

		private void setConfigBoolean(String name, boolean v) throws Exception
		{
			this.setConfigInt(name, v ? 1 : 0);
		}

		private void setConfigDouble(String name, double v) throws Exception
		{
			JsonObject o = TinygDriverConfigurationWizard.this.driver.sendCommand(String.format(Locale.US, "{\"%s\":%f}", name, v));
		}

		private void setConfigInt(String name, int v) throws Exception
		{
			JsonObject o = TinygDriverConfigurationWizard.this.driver.sendCommand(String.format(Locale.US, "{\"%s\":%d}", name, v));
		}

		public void setFeedMaxX(int v) throws Exception
		{
			this.setConfigInt("xfr", v);
		}

		public void setFeedMaxY(int v) throws Exception
		{
			this.setConfigInt("yfr", v);
		}

		public void setJerkMaxX(int v) throws Exception
		{
			this.setConfigInt("xjm", v);
		}

		public void setJerkMaxY(int v) throws Exception
		{
			this.setConfigInt("yjm", v);
		}

		public void setPolarityReversedM1(boolean v) throws Exception
		{
			this.setConfigBoolean("1po", v);
		}

		public void setPolarityReversedM2(boolean v) throws Exception
		{
			this.setConfigBoolean("2po", v);
		}

		public void setPolarityReversedM3(boolean v) throws Exception
		{
			this.setConfigBoolean("3po", v);
		}

		public void setPolarityReversedM4(boolean v) throws Exception
		{
			this.setConfigBoolean("4po", v);
		}

		public void setPowerMgmtM1(boolean v) throws Exception
		{
			this.setConfigBoolean("1pm", v);
		}

		public void setPowerMgmtM2(boolean v) throws Exception
		{
			this.setConfigBoolean("2pm", v);
		}

		public void setPowerMgmtM3(boolean v) throws Exception
		{
			this.setConfigBoolean("3pm", v);
		}

		public void setPowerMgmtM4(boolean v) throws Exception
		{
			this.setConfigBoolean("4pm", v);
		}

		public void setStepAngleM1(double v) throws Exception
		{
			this.setConfigDouble("1sa", v);
		}

		public void setStepAngleM2(double v) throws Exception
		{
			this.setConfigDouble("2sa", v);
		}

		public void setStepAngleM3(double v) throws Exception
		{
			this.setConfigDouble("3sa", v);
		}

		public void setStepAngleM4(double v) throws Exception
		{
			this.setConfigDouble("4sa", v);
		}

		public void setTravelPerRevM1(double v) throws Exception
		{
			this.setConfigDouble("1tr", v);
		}

		public void setTravelPerRevM2(double v) throws Exception
		{
			this.setConfigDouble("2tr", v);
		}

		public void setTravelPerRevM3(double v) throws Exception
		{
			this.setConfigDouble("3tr", v);
		}

		public void setTravelPerRevM4(double v) throws Exception
		{
			this.setConfigDouble("4tr", v);
		}

		public void setVelMaxX(int v) throws Exception
		{
			this.setConfigInt("xvm", v);
		}

		public void setVelMaxY(int v) throws Exception
		{
			this.setConfigInt("yvm", v);
		}
	}

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;
	private final TinygDriver	driver;
	private JTextField			m1StepAngle;
	private JTextField			m2StepAngle;
	private JTextField			m3StepAngle;
	private JTextField			m4StepAngle;
	private JTextField			m1TravelPerRev;
	private JTextField			m2TravelPerRev;
	private JTextField			m3TravelPerRev;
	private JTextField			m4TravelPerRev;
	private JTextField			xVelMax;
	private JTextField			yVelMax;
	private JTextField			textField_10;
	private JTextField			textField_11;
	private JTextField			textField_12;
	private JTextField			textField_13;
	private JTextField			xFeedMax;
	private JTextField			yFeedMax;
	private JTextField			textField_16;
	private JTextField			textField_17;
	private JTextField			textField_18;
	private JTextField			textField_19;
	private JTextField			xJerkMax;
	private JTextField			yJerkMax;
	private JTextField			textField_22;
	private JTextField			textField_23;
	private JTextField			textField_24;
	private JTextField			textField_25;
	private JCheckBox			m1PowerMgmt;
	private JCheckBox			m2PowerMgmt;
	private JCheckBox			m3PowerMgmt;
	private JCheckBox			m4PowerMgmt;
	private JCheckBox			m1RevPol;
	private JCheckBox			m2RevPol;
	private JCheckBox			m3RevPol;

	private JCheckBox m4RevPol;

	public TinygDriverConfigurationWizard(TinygDriver driver)
	{
		this.driver = driver;

		this.contentPanel.setLayout(new BoxLayout(this.contentPanel, BoxLayout.Y_AXIS));

		JPanel panelGeneral = new JPanel();
		panelGeneral.setBorder(new TitledBorder(null, "General Settings", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		this.contentPanel.add(panelGeneral);
		panelGeneral.setLayout(new FormLayout(new ColumnSpec[]
		{}, new RowSpec[]
		{}));

		JPanel panelMotors = new JPanel();
		panelMotors.setBorder(new TitledBorder(null, "Motors", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		this.contentPanel.add(panelMotors);
		panelMotors.setLayout(new FormLayout(new ColumnSpec[]
		{ FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"),
				FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), }, new RowSpec[]
		{ FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, }));

		JLabel label = new JLabel("1");
		panelMotors.add(label, "4, 2");

		JLabel label_1 = new JLabel("2");
		panelMotors.add(label_1, "6, 2");

		JLabel label_2 = new JLabel("3");
		panelMotors.add(label_2, "8, 2");

		JLabel label_3 = new JLabel("4");
		panelMotors.add(label_3, "10, 2");

		JLabel lblAxis = new JLabel("Axis");
		panelMotors.add(lblAxis, "2, 4, right, default");

		JSpinner m1Axis = new JSpinner();
		panelMotors.add(m1Axis, "4, 4");

		JSpinner m2Axis = new JSpinner();
		panelMotors.add(m2Axis, "6, 4");

		JSpinner m3Axis = new JSpinner();
		panelMotors.add(m3Axis, "8, 4");

		JSpinner m4Axis = new JSpinner();
		panelMotors.add(m4Axis, "10, 4");

		JLabel lblStepAngle = new JLabel("Step Angle");
		panelMotors.add(lblStepAngle, "2, 6, right, default");

		this.m1StepAngle = new JTextField();
		this.m1StepAngle.setText("1.8");
		panelMotors.add(this.m1StepAngle, "4, 6");
		this.m1StepAngle.setColumns(10);

		this.m2StepAngle = new JTextField();
		this.m2StepAngle.setText("1.8");
		panelMotors.add(this.m2StepAngle, "6, 6");
		this.m2StepAngle.setColumns(10);

		this.m3StepAngle = new JTextField();
		this.m3StepAngle.setText("1.8");
		panelMotors.add(this.m3StepAngle, "8, 6");
		this.m3StepAngle.setColumns(10);

		this.m4StepAngle = new JTextField();
		this.m4StepAngle.setText("1.8");
		panelMotors.add(this.m4StepAngle, "10, 6");
		this.m4StepAngle.setColumns(10);

		JLabel lblTravelPerRev = new JLabel("Travel Per Rev.");
		panelMotors.add(lblTravelPerRev, "2, 8, right, default");

		this.m1TravelPerRev = new JTextField();
		panelMotors.add(this.m1TravelPerRev, "4, 8, fill, default");
		this.m1TravelPerRev.setColumns(10);

		this.m2TravelPerRev = new JTextField();
		panelMotors.add(this.m2TravelPerRev, "6, 8, fill, default");
		this.m2TravelPerRev.setColumns(10);

		this.m3TravelPerRev = new JTextField();
		panelMotors.add(this.m3TravelPerRev, "8, 8, fill, default");
		this.m3TravelPerRev.setColumns(10);

		this.m4TravelPerRev = new JTextField();
		panelMotors.add(this.m4TravelPerRev, "10, 8, fill, default");
		this.m4TravelPerRev.setColumns(10);

		JLabel lblMicrosteps = new JLabel("Microsteps");
		panelMotors.add(lblMicrosteps, "2, 10, right, default");

		JSpinner m1Microsteps = new JSpinner(new SpinnerListModel(new Object[]
		{ 1, 2, 4, 8 }));
		panelMotors.add(m1Microsteps, "4, 10");

		JSpinner m2Microsteps = new JSpinner(new SpinnerListModel(new Object[]
		{ 1, 2, 4, 8 }));
		panelMotors.add(m2Microsteps, "6, 10");

		JSpinner m3Microsteps = new JSpinner(new SpinnerListModel(new Object[]
		{ 1, 2, 4, 8 }));
		panelMotors.add(m3Microsteps, "8, 10");

		JSpinner m4Microsteps = new JSpinner(new SpinnerListModel(new Object[]
		{ 1, 2, 4, 8 }));
		panelMotors.add(m4Microsteps, "10, 10");

		JLabel lblPolarity = new JLabel("Reverse Polarity");
		panelMotors.add(lblPolarity, "2, 12, right, default");

		this.m1RevPol = new JCheckBox("");
		panelMotors.add(this.m1RevPol, "4, 12");

		this.m2RevPol = new JCheckBox("");
		panelMotors.add(this.m2RevPol, "6, 12");

		this.m3RevPol = new JCheckBox("");
		panelMotors.add(this.m3RevPol, "8, 12");

		this.m4RevPol = new JCheckBox("");
		panelMotors.add(this.m4RevPol, "10, 12");

		JLabel lblPowerManagement = new JLabel("Power Management");
		panelMotors.add(lblPowerManagement, "2, 14");

		this.m1PowerMgmt = new JCheckBox("");
		panelMotors.add(this.m1PowerMgmt, "4, 14");

		this.m2PowerMgmt = new JCheckBox("");
		panelMotors.add(this.m2PowerMgmt, "6, 14");

		this.m3PowerMgmt = new JCheckBox("");
		panelMotors.add(this.m3PowerMgmt, "8, 14");

		this.m4PowerMgmt = new JCheckBox("");
		panelMotors.add(this.m4PowerMgmt, "10, 14");

		JPanel panelAxes = new JPanel();
		panelAxes.setBorder(new TitledBorder(null, "Axes", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		this.contentPanel.add(panelAxes);
		panelAxes.setLayout(new FormLayout(new ColumnSpec[]
		{ FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"),
				FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormSpecs.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"), FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), }, new RowSpec[]
		{ FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, }));

		JLabel lblX = new JLabel("X");
		panelAxes.add(lblX, "4, 2");

		JLabel lblY = new JLabel("Y");
		panelAxes.add(lblY, "6, 2");

		JLabel lblZ = new JLabel("Z");
		panelAxes.add(lblZ, "8, 2");

		JLabel lblA = new JLabel("A");
		panelAxes.add(lblA, "10, 2");

		JLabel lblB = new JLabel("B");
		panelAxes.add(lblB, "12, 2");

		JLabel lblC = new JLabel("C");
		panelAxes.add(lblC, "14, 2");

		JLabel lblAxisMode = new JLabel("Axis Mode");
		panelAxes.add(lblAxisMode, "2, 4, right, default");

		JComboBox xAxisMode = new JComboBox();
		panelAxes.add(xAxisMode, "4, 4, fill, default");

		JComboBox yAxisMode = new JComboBox();
		panelAxes.add(yAxisMode, "6, 4, fill, default");

		JComboBox comboBox_10 = new JComboBox();
		panelAxes.add(comboBox_10, "8, 4, fill, default");

		JComboBox comboBox_11 = new JComboBox();
		panelAxes.add(comboBox_11, "10, 4, fill, default");

		JComboBox comboBox_12 = new JComboBox();
		panelAxes.add(comboBox_12, "12, 4, fill, default");

		JComboBox comboBox_13 = new JComboBox();
		panelAxes.add(comboBox_13, "14, 4, fill, default");

		JLabel lblVelocityMax = new JLabel("Velocity Max.");
		panelAxes.add(lblVelocityMax, "2, 6, right, default");

		this.xVelMax = new JTextField();
		panelAxes.add(this.xVelMax, "4, 6, fill, default");
		this.xVelMax.setColumns(10);

		this.yVelMax = new JTextField();
		panelAxes.add(this.yVelMax, "6, 6, fill, default");
		this.yVelMax.setColumns(10);

		this.textField_10 = new JTextField();
		panelAxes.add(this.textField_10, "8, 6, fill, default");
		this.textField_10.setColumns(10);

		this.textField_11 = new JTextField();
		panelAxes.add(this.textField_11, "10, 6, fill, default");
		this.textField_11.setColumns(10);

		this.textField_12 = new JTextField();
		panelAxes.add(this.textField_12, "12, 6, fill, default");
		this.textField_12.setColumns(10);

		this.textField_13 = new JTextField();
		panelAxes.add(this.textField_13, "14, 6, fill, default");
		this.textField_13.setColumns(10);

		JLabel lblFeedrateMax = new JLabel("Feedrate Max.");
		panelAxes.add(lblFeedrateMax, "2, 8, right, default");

		this.xFeedMax = new JTextField();
		panelAxes.add(this.xFeedMax, "4, 8, fill, default");
		this.xFeedMax.setColumns(10);

		this.yFeedMax = new JTextField();
		panelAxes.add(this.yFeedMax, "6, 8, fill, default");
		this.yFeedMax.setColumns(10);

		this.textField_16 = new JTextField();
		panelAxes.add(this.textField_16, "8, 8, fill, default");
		this.textField_16.setColumns(10);

		this.textField_17 = new JTextField();
		panelAxes.add(this.textField_17, "10, 8, fill, default");
		this.textField_17.setColumns(10);

		this.textField_18 = new JTextField();
		panelAxes.add(this.textField_18, "12, 8, fill, default");
		this.textField_18.setColumns(10);

		this.textField_19 = new JTextField();
		panelAxes.add(this.textField_19, "14, 8, fill, default");
		this.textField_19.setColumns(10);

		JLabel lblJerkMax = new JLabel("Jerk Max.");
		panelAxes.add(lblJerkMax, "2, 10, right, default");

		this.xJerkMax = new JTextField();
		panelAxes.add(this.xJerkMax, "4, 10, fill, default");
		this.xJerkMax.setColumns(10);

		this.yJerkMax = new JTextField();
		panelAxes.add(this.yJerkMax, "6, 10, fill, default");
		this.yJerkMax.setColumns(10);

		this.textField_22 = new JTextField();
		panelAxes.add(this.textField_22, "8, 10, fill, default");
		this.textField_22.setColumns(10);

		this.textField_23 = new JTextField();
		panelAxes.add(this.textField_23, "10, 10, fill, default");
		this.textField_23.setColumns(10);

		this.textField_24 = new JTextField();
		panelAxes.add(this.textField_24, "12, 10, fill, default");
		this.textField_24.setColumns(10);

		this.textField_25 = new JTextField();
		panelAxes.add(this.textField_25, "14, 10, fill, default");
		this.textField_25.setColumns(10);
	}

	@Override
	public void createBindings()
	{
		IntegerConverter integerConverter = new IntegerConverter();
		DoubleConverter doubleConverter = new DoubleConverter("%f");

		ConfigProxy configProxy = new ConfigProxy();

		this.addWrappedBinding(configProxy, "stepAngleM1", this.m1StepAngle, "text", doubleConverter);
		this.addWrappedBinding(configProxy, "stepAngleM2", this.m2StepAngle, "text", doubleConverter);
		this.addWrappedBinding(configProxy, "stepAngleM3", this.m3StepAngle, "text", doubleConverter);
		this.addWrappedBinding(configProxy, "stepAngleM4", this.m4StepAngle, "text", doubleConverter);

		this.addWrappedBinding(configProxy, "travelPerRevM1", this.m1TravelPerRev, "text", doubleConverter);
		this.addWrappedBinding(configProxy, "travelPerRevM2", this.m2TravelPerRev, "text", doubleConverter);
		this.addWrappedBinding(configProxy, "travelPerRevM3", this.m3TravelPerRev, "text", doubleConverter);
		this.addWrappedBinding(configProxy, "travelPerRevM4", this.m4TravelPerRev, "text", doubleConverter);

		this.addWrappedBinding(configProxy, "polarityReversedM1", this.m1RevPol, "selected");
		this.addWrappedBinding(configProxy, "polarityReversedM2", this.m2RevPol, "selected");
		this.addWrappedBinding(configProxy, "polarityReversedM3", this.m3RevPol, "selected");
		this.addWrappedBinding(configProxy, "polarityReversedM4", this.m4RevPol, "selected");

		this.addWrappedBinding(configProxy, "powerMgmtM1", this.m1PowerMgmt, "selected");
		this.addWrappedBinding(configProxy, "powerMgmtM2", this.m2PowerMgmt, "selected");
		this.addWrappedBinding(configProxy, "powerMgmtM3", this.m3PowerMgmt, "selected");
		this.addWrappedBinding(configProxy, "powerMgmtM4", this.m4PowerMgmt, "selected");

		this.addWrappedBinding(configProxy, "velMaxX", this.xVelMax, "text", integerConverter);
		this.addWrappedBinding(configProxy, "velMaxY", this.yVelMax, "text", integerConverter);

		this.addWrappedBinding(configProxy, "feedMaxX", this.xFeedMax, "text", integerConverter);
		this.addWrappedBinding(configProxy, "feedMaxY", this.yFeedMax, "text", integerConverter);

		this.addWrappedBinding(configProxy, "jerkMaxX", this.xJerkMax, "text", integerConverter);
		this.addWrappedBinding(configProxy, "jerkMaxY", this.yJerkMax, "text", integerConverter);

		ComponentDecorators.decorateWithAutoSelect(this.m1StepAngle);
		ComponentDecorators.decorateWithAutoSelect(this.m2StepAngle);
		ComponentDecorators.decorateWithAutoSelect(this.m3StepAngle);
		ComponentDecorators.decorateWithAutoSelect(this.m4StepAngle);

		ComponentDecorators.decorateWithAutoSelect(this.m1TravelPerRev);
		ComponentDecorators.decorateWithAutoSelect(this.m2TravelPerRev);
		ComponentDecorators.decorateWithAutoSelect(this.m3TravelPerRev);
		ComponentDecorators.decorateWithAutoSelect(this.m4TravelPerRev);

		ComponentDecorators.decorateWithAutoSelect(this.xVelMax);
		ComponentDecorators.decorateWithAutoSelect(this.yVelMax);

		ComponentDecorators.decorateWithAutoSelect(this.xFeedMax);
		ComponentDecorators.decorateWithAutoSelect(this.yFeedMax);

		ComponentDecorators.decorateWithAutoSelect(this.xJerkMax);
		ComponentDecorators.decorateWithAutoSelect(this.yJerkMax);
	}
}
