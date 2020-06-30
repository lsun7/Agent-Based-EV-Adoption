/**
 * 
 */
package jEvAdoption_v1;

import java.util.List;

import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.parameter.Parameters;
import repast.simphony.query.space.gis.GeographyWithin;
import repast.simphony.random.RandomHelper;
import repast.simphony.util.ContextUtils;
import repast.simphony.util.SimUtilities;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import repast.simphony.space.gis.Geography;

/**
 * @author Lisha
 * 
 */
public class EVMarket {

//	private String Node_ID;
// 4 levels for Range MSRP and kWh, 1: low, 2: medium, 3: high, 4: SUV 
	private int[] Range;   
	private int[][] MSRP;
	private int[] ICEMSRP;
	private double ChargerCost;
	private double GasCost;
	private double ElecCost;
	private int SimuYrs;
	private int StartYr;
	private int EndYr;
	private double discount;
	private int StationCount;
	private double TotCust;

	
	
	public EVMarket(double ChargerCost, int[] Range, int[][] MSRP, int[] ICEMSRP, double GasCost, double ElecCost, 
			int StartYr, int EndYr, double discount, int StationCount, double TotCust) {
		this.ChargerCost = ChargerCost;
		this.GasCost = GasCost; //$/gallon//1000
		this.ElecCost = ElecCost;  //cents/kwh
		this.Range = Range;
		this.MSRP = MSRP;
		this.ICEMSRP = ICEMSRP;
		this.StartYr = StartYr;
		this.EndYr = EndYr;
		this.discount = discount;
		this.StationCount = StationCount;
		this.TotCust = TotCust;

	}
	
//	Parameters params = RunEnvironment.getInstance().getParameters();
//	int SimuYrs = (Integer) params.getValue("SimulationHorizonYears");
//	double EVCostDecline = (double) params.getValue("EVCostDecline");
//	
	
	public int[] getRange() {return Range;}
	public void setRange(int[] Range) {this.Range = Range;}
	public void setStationCount (int StationCount) {this.StationCount = StationCount;}
	
	public double getChargerCost() {return ChargerCost;}
	public double getGasCost() {return GasCost;}
	public double getElecCost() {return ElecCost;}
	public double getDiscount() {return discount;}
	public double getTotCust() {return TotCust;}
	public int[][] getMSRP() {return MSRP;}
	public int[] getICEMSRP() {return ICEMSRP;}
	public int getStationCount() {return StationCount;}
	
	
	//Model for EV and ICE model price
	double[] ICEM0 = {107.546699, -186909.9769};
	double[] ICEM1 = {176.5404383, -315511.2279};
	
////	40% decline
//	double[][] EVM0 = {
//	{-1.043805868, 6398.584594, -13074574.62, 8905365856.00},
//	{-1.278489012, 7835.488199, -16007128.57, 10900340337.0},
//	{-1.076208693, 6605.522648, -13514375.24, 9216456192.0}};
//
//	double[][] EVM1 = {
//	{-1.477536279, 9065.026303, -18538688.3, 12637726826.0},
//	{-1.92740841, 11814.69054, -24140782.04, 16442260585.0},
//	{-1.60446271, 9851.91697, -20164613.3, 13757454173.0}};
	
	//20% decline
	double[][] EVM0 = {
			{-0.705025078801147,	4320.94034483510,	-8827304.47987308,	6011130366.74143},
			{-0.810641811108453,	4965.50684133234,	-10138562.96530870,	6900322389.48248},
			{-1.15094565674158,	7043.22997956593,	-14367061.35061880,	9768873913.04726}};
	double[][] EVM1 = {
			{-1.64985840219428,	10102.80074913740,	-20621248.1402078,	14030311957.1322},
			{-1.36027854860805,	8339.03903074872,	-17040552.6772809,	11607329883.3397},
			{-1.83246866766012,	11223.45583535180,	-22913727.3668379,	15593555537.2849}};
	
	
	double[] ElecM = {0.000286666666674766,	-1.75610000004953,3585.83633343429,-2440611.2900686};
	double[] GasM = {-0.00095, 3.8925, -3983.515};
	
	
	@ScheduledMethod(start = 1, interval = 1, priority = 5.0)
	public void run0() {
		double tick = RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
		double simu_yr = tick + StartYr -1;
		
		int size = Range.length;
		for (int i = 0; i<size; i++) {
			MSRP[0][i]=(int)(EVM0[i][0]*Math.pow(simu_yr, 3)+EVM0[i][1]*Math.pow(simu_yr, 2)+EVM0[i][2]*simu_yr+EVM0[i][3]);
			MSRP[1][i]=(int)(EVM1[i][0]*Math.pow(simu_yr, 3)+EVM1[i][1]*Math.pow(simu_yr, 2)+EVM1[i][2]*simu_yr+EVM1[i][3]);
		}
		
//		//no decline
//		for (int i = 0; i<size; i++) {
//			MSRP[0][i]=(int)(EVM0[i][0]*Math.pow(2019, 3)+EVM0[i][1]*Math.pow(2019, 2)+EVM0[i][2]*2019+EVM0[i][3]);
//			MSRP[1][i]=(int)(EVM1[i][0]*Math.pow(2019, 3)+EVM1[i][1]*Math.pow(2019, 2)+EVM1[i][2]*2019+EVM1[i][3]);
//		}
		
		//scenarios testing////////////////////////////////////////////////////////////////////////////////////////////
		int[] policy= {0,0,0}; //{Vehicle price rebate, home charger rebate, public charging station}
		
		//get policy testing indicator factor from the GUI
		Parameters params = RunEnvironment.getInstance().getParameters();
		int vehicle_rebate = (Integer) params.getValue("CarPrice");
		int HC_rebate = (Integer) params.getValue("HC_rebate");
		int Fuel_rebate = (Integer) params.getValue("Fuel_rebate");
		
		policy[0]=vehicle_rebate;
		policy[1]=HC_rebate;
		policy[2]=Fuel_rebate;
		
		//rebate on electric vehicle cost
		
		if (policy[0]==1) {
			if(simu_yr == 2019) {System.out.printf("Policy testing on EV Price Rebate\n");}
			double P_rebate_start = 2021;
			double Program_yr = 5;
			double P_rebate = 3000 + 7500; //$$ one time rebate
			
			if (simu_yr >= P_rebate_start && simu_yr < (P_rebate_start + Program_yr)) {
				for (int i = 0; i<size; i++) {
					MSRP[0][i]=(int)(MSRP[0][i]-P_rebate);
					MSRP[1][i]=(int)(MSRP[1][i]-P_rebate);
				}
			}
		}	
		
		//rebate on home charger cost
		if (policy[1]==1) {
			if(simu_yr == 2019) {System.out.printf("Policy testing on Home Charger Rebate\n");}
			double HC_rebate_start = 2021;
			double HC_Program_yr = 5;
			double HC_rebate_dollar = 1000; //$$ one time rebate
			if (simu_yr >= HC_rebate_start && simu_yr < (HC_rebate_start + HC_Program_yr)) {
				ChargerCost = ChargerCost - HC_rebate_dollar;
				if (ChargerCost <0) {ChargerCost = 0;}
			}
		}
		
		//manage electricity price
		if (policy[2]==1) {
			if(simu_yr == 2019) {System.out.printf("Policy testing on Electricity Price Control\n");}
			double HC_rebate_start = 2021;
			double HC_Program_yr = 5;
			double Elec_price_new = 0; // //cents/kwh
			if (simu_yr >= HC_rebate_start && simu_yr < (HC_rebate_start + HC_Program_yr)) {
				ElecCost = Elec_price_new;
			}
		}
		
		
		
		
		ICEMSRP[0] = (int)(simu_yr*ICEM0[0]+ICEM0[1]);
		ICEMSRP[1] = (int)(simu_yr*ICEM1[0]+ICEM1[1]);
//		System.out.printf("EV prices %d %d %n" , MSRP[0][0], MSRP[1][0]);
		
		ElecCost = 0;
		for (int i=0; i<ElecM.length; i++) {ElecCost = ElecCost + ElecM[i]*Math.pow(simu_yr, ElecM.length-i-1);};
		GasCost = 0;
		for (int i=0; i<GasM.length; i++) {GasCost = GasCost + GasM[i]*Math.pow(simu_yr, GasM.length-i-1);};
		
//		System.out.printf("Market is updated for simulation year %.1f%n" , simu_yr);
	}
	
}

