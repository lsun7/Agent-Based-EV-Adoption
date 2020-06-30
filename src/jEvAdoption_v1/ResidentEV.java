/**
 * 
 */
package jEvAdoption_v1;

import com.vividsolutions.jts.geom.Geometry;

import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.gis.Geography;
import repast.simphony.util.ContextUtils;

/**
 * @author Lisha
 * 
 */
public class ResidentEV {

//	private String Node_ID;
	private long Income;
	private int Apartment;
	private String Node_ID;
	private int Homecharger;
	private int getEV;
	private int VEVage;
	
	private double NBimpact;
	private int Family;
	private int Age;
	private int Educ;
	private int Vsize;
	private int Vrange;
	private double logit;
	private int HouseID;
	
	
	public ResidentEV(long Income, int Apartment, String Node_ID, int Homecharger, int VEVage, double NBimpact, int Family, int Age, int Educ, int Vsize, int Vrange, int getEV, double logit, int HouseID){
		this.Node_ID = Node_ID;
		this.Income = Income;
		this.Apartment = Apartment;   // 0 or 1. 1 means resident live in an apartment
		this.Homecharger = Homecharger; //0 means no home charger, 1 means has
		this.getEV = getEV;
		this.VEVage = VEVage;
		
		this.NBimpact = NBimpact;
		this.Family = Family;
		this.Age = Age;
		this.Educ = Educ;
		this.Vsize = Vsize;
		this.Vrange = Vrange;
		this.logit = logit;
		this.HouseID = HouseID;

	}
	
	public String getID() {
		return Node_ID;
	}

	public void setID(String ID) {
		this.Node_ID = Node_ID;
	}
	
	public int getHomecharger() {
		return Homecharger;
	}
	
	public long getIncome() {
		return Income;
	}
	
	public double getNBimpact() {
		return NBimpact;
	}
	
	public double getlogit() {
		return logit;
	}
	
	public int getEV() {
		return getEV;
	}
	
	@ScheduledMethod(start = 1, interval = 1, priority = 6.0)
	public void runEV() {
		getEV = 0;
		int changed=0;
		
		//check if change customer, only age change, other assume the new customer will have similar characteristic
		int agetemp= RandomHelper.nextIntFromTo(0, 99);
		if (agetemp<(((Age-40)<0)?1: Age-40) || Age > 100){
			Age = newresident(Income);
			changed =1;
		}
		//the changed customer maybe an ICE holder if homecharger ==0
		int ICEtemp=RandomHelper.nextIntFromTo(0, 99);
		if (Homecharger==0 && ICEtemp<50 && changed ==1) {
			infectback(Income, Apartment, Node_ID, NBimpact, Family, Age, Educ, Vsize, Vrange, 0, 0, VEVage, Homecharger, HouseID);
		}else {
		
			double AgeSellCat[] = {2, 5, 10, 100};
			double AgeSellDist[] = {4, 30, 53, 12};
			double Mot_Coe[]= {-0.026823529, 0.964}; //probability to pass the test ax+b, {a, b}
			double PMot = 100-(VEVage*Mot_Coe[0] + Mot_Coe[1])*100; //probability that veh did not pass Mot test
			double EV2ICE = 7; //7% will go back to ICE from EV
			
			int i, like2sell=0, need2sell=0;
			
			for (i=0; i < AgeSellCat.length; i++) {
				if (VEVage<AgeSellCat[i]) {break;}
			}
			int dis_temp = RandomHelper.nextIntFromTo(0, 99);
			if(dis_temp<AgeSellDist[i]) {like2sell=1;}
			int dis_temp0 = RandomHelper.nextIntFromTo(0, 99);
			if(dis_temp0<PMot) {need2sell=1;}
			
			if (like2sell>=1 || need2sell>=1) {
				int dis_temp1 = RandomHelper.nextIntFromTo(0, 99);
				if (dis_temp1<=EV2ICE) {
					// get the geom of this EV resident
					Age = Age +1;
					int dis_temp2 = RandomHelper.nextIntFromTo(0, 99);
					Income = (long) (Income+(1.00+0.04*dis_temp2/100.00));
					
					infectback(Income, Apartment, Node_ID, NBimpact, Family, Age, Educ, Vsize, Vrange, 1, 1, 1, Homecharger, HouseID);
					
				}else {
					getEV = 1;
					VEVage = 1;
					Age = Age +1;
				}
			}
			int dis_temp3 = RandomHelper.nextIntFromTo(0, 99);
			Income = (long) (Income+(1.00+0.04*dis_temp3/100.00));
		}

	}
	
	public void infectback(long Income, int Apartment, String Node_ID, double NBimpact, int Family, int Age, int Educ, int Vsize, int Vrange, int turned, int getICE, int Vage, int Homecharger, int HouseID) {
//		int turned=1;
//		int getICE=1;
//		int Vage = 1;
		// only move if we are not already in this grid location
		// get the geom of this resident
		Context context = ContextUtils.getContext(this);
		Geography geography = (Geography)context.getProjection("geography");
		Geometry geom = geography.getGeometry(this);
		
		context.remove(this);

		Resident resident = new Resident(Income, Apartment, Node_ID, NBimpact, Family, Age, Educ, Vage, Vsize, Vrange, turned, getICE, Homecharger, HouseID); 
		context.add(resident);
		geography.move(resident, geom);
	}
	
	public static int newresident(double Income) {
		int[] IncomeCat = {50000, 75000, 100000, 125000};
		
		//Column:  20	20-30	30-40	40-50	50-60	60
		int[][] AgeDist = {{0, 10, 21, 31, 48, 100},
				{0,8,23,34,53,100},
				{0,7,24,37,60,100},
				{0,5,18,35,57,100},
				{0,3,19,41,68,100}};  
		int[] AgeCat={20, 30, 40, 50, 60, 90};
		
		//find income category
		int IncomeNum, i, Age;
		for (i=0; i < IncomeCat.length; i++){if(Income<=IncomeCat[i]) {break;}};
		IncomeNum = i;
		
		int dis_temp = RandomHelper.nextIntFromTo(0, 99);
		for (i=0; i < AgeDist[0].length; i++){if (dis_temp<=AgeDist[IncomeNum][i]) {break;}};
		if(i==0) {Age=RandomHelper.nextIntFromTo(15, AgeCat[i]);
		} else {Age=RandomHelper.nextIntFromTo(AgeCat[i-1]+1, AgeCat[i]);}
		
		return Age;
	}
	
}
