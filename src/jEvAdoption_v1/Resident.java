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
public class Resident {

//	private String Node_ID;
	private long Income;
	private int Apartment;
	private String Node_ID;
	private double NBimpact;
	private int Family;
	private int Age;
	private int Educ;
	private int Vage;
	private int Vsize;
	private int Vrange;
	private int getICE;
	private int turned; //turned = 1, do not consider in the run
	private int Homecharger;
	private int HouseID;
	
	public Resident(long Income, int Apartment, String Node_ID, double NBimpact, int Family, int Age, int Educ, int Vage, int Vsize, int Vrange, int turned, int getICE, int Homecharger, int HouseID){
		this.Node_ID = Node_ID;
		this.Income = Income;
		this.Apartment = Apartment;   // 0 or 1. 1 means resident live in an apartment
		this.NBimpact = NBimpact;
		this.Family = Family;
		this.Age = Age;
		this.Educ = Educ;
		this.Vage = Vage;
		this.Vsize = Vsize;
		this.Vrange = Vrange;
		this.getICE = getICE;
		this.turned = turned;
		this.Homecharger = Homecharger;
		this.HouseID = HouseID;
	}
	
	public String getID() {
		return Node_ID;
	}

	public void setID(String ID) {
		this.Node_ID = Node_ID;
	}
	
	public int getHouseID() {
		return HouseID;
	}
	
	public int getICE() {
		return getICE;
	}
	
	public int gethomecharger() {
		return Homecharger;
	}
	
	public void sethomecharger(int Homecharger) {
		this.Homecharger = Homecharger;
	}
	
	public int getVage() {
		return Vage;
	}
	
	public long getIncome() {
		return Income;
	}
	
	long startTime = System.nanoTime();
	
	// the greater priority number, the higher priority
	@ScheduledMethod(start = 1, interval = 1, priority = 3.0)
	public void run1() {
		// get the geom of this resident
		
		if (turned==1) {turned=0;} else {
		getICE = 0;
		
		//check if change customer, only age change, other assume the new customer will have similar characteristic
		int agetemp= getRandIntBetween(0, 99);
		if (agetemp<(((Age-40)<0)?1: Age-40) || Age > 100){
			Age = newresident(Income);
		}
		
		
		Context context = ContextUtils.getContext(this);
		Geography geography = (Geography)context.getProjection("geography");
		Geometry geom = geography.getGeometry(this);
		
		///////////////////////////////////////////////////////////////////////////////////////////////////////////
		//calculate the utility of purchasing EV, ICE and do nothing
		//Utility is based on CANE model: car age, attractiveness, neighborhood, economics 
		int action[][]= {{1, 1}, {0, 1}, {0, 0}}; //1 means do, 0 means not do; {whether buy EV, whether buy car}
		double[] score= {100, 100, 100, 100}; //total score for C A N E each category
		
		//get tune factor from the GUI default to be
		Parameters params = RunEnvironment.getInstance().getParameters();
		double tune = (double) params.getValue("TuneFactor");
		
//		double tune=0.5;
		
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		//calculate car age utility
		double [] UAge= new double[3];
		double a1=0.4, a2=0.8;
		double AgeSellCat[] = {2, 5, 10, 100};
		double AgeSellDist[] = {4, 30, 53, 12};
		double Mot_Coe[]= {0.00096, -0.00574, 0.01864}; //scarppage rate ax^2+bx+c, {a, b,c}
		double PMot = (Vage*Vage*Mot_Coe[0] + Vage*Mot_Coe[1] + Mot_Coe[2])*100; //probability that vehicle scarppaged
		
		int i, like2sell=0, need2sell=0;
		
		for (i=0; i < AgeSellCat.length; i++) {
			if (Vage<AgeSellCat[i]) {break;}
		}
		int dis_temp = RandomHelper.nextIntFromTo(0, 99);
		if(dis_temp<AgeSellDist[i]) {like2sell=1;}
		int dis_temp0 = RandomHelper.nextIntFromTo(0, 99);
		if(dis_temp0<PMot) {need2sell=1;}
		
		
		//form the score A matrix for car age
		double [][] Aage=new double[3][3];
		double [] AageScore = new double[3];
		
		double [][] A1age=new double[2][2];
		double [] A1ageScore = new double[2];
		double [][] A2age=new double[2][2];
		double [] A2ageScore = new double[2];
		
		//100 points for whether to sell
		if (like2sell==0 && need2sell ==0) {
			UAge[0]= -score[0];
			UAge[1]=UAge[0];
			UAge[2]=score[0];
		}else if (like2sell==1 && need2sell ==0) {
			UAge[0]= score[0]*a1;
			UAge[1]=UAge[0];
			UAge[2]=0;
		}else if (like2sell==0 && need2sell ==1) {
			UAge[0]= score[0]*a2;
			UAge[1]=UAge[0];
			UAge[2]= -score[0]*a2;			
		}else if (like2sell==1 && need2sell ==1) {
			UAge[0]= score[0];
			UAge[1]=UAge[0];
			UAge[2]= -score[0];		
		}
			
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// Calculate the Attractiveness and logit score based on backgrounds info
		double Attractiveness, logit_score;
		double coe_age=0.010287784;
		double coe_HHSize = -0.011117679;
		double coe_State_NC = 0.114963827; 
		double beta_0=-1.61300198;
		double[] coe_educ={-0.98290851,	-0.898194856,	-0.473089845,	0.154680832,	0.586510399};
		double[] coe_income= {-1.26530592,	-0.683699885,	-0.453432904,	0.162534999,	0.62690173};
		double[] coe_urban= {-0.544178221,	-1.06882376};
		
		int[] IncomeCat = {50000, 75000, 100000, 125000};
		int Income_idx = findIndex(IncomeCat, (int)Income);
		
		Attractiveness = Math.exp(beta_0 + coe_age*Age + coe_HHSize*Family + coe_educ[Educ] + coe_income[Income_idx] + coe_urban[0] + coe_State_NC);
		logit_score = Attractiveness/(1+Attractiveness);
		
		//100 points for attraction
		double[] UAtt = {logit_score * score[1], score[1]-logit_score * score[1], score[1]-logit_score * score[1]}; 
		
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// Get the count of EV and Charging Station in defined neighborhood, Calculate Utility for neighbor
		double CEVCount = 0;
		int StationCount = 0;
		double NonEVCount = 0;
		double NBEVPene = 0;
		// try to use getObjectsWithin to accelerate the process
		Coordinate coordinates = geom.getCoordinates()[0];
		// create envelope around Agent, 111km is 1 degree of latitude or longitude
		// number of km per degree = ~111km (111.32 in google maps, but range varies between 110.567km at the equator and 111.699km at the poles)
		// 1km in degree = 1 / 111.32km = 0.0089  // 1m in degree = 0.0089 / 1000 = 0.0000089
		double DistMeter = 100.0;
		double tf = 0.0089/1000.0;
		double Dist = DistMeter*tf;
		Envelope envelope = new Envelope(coordinates.x + Dist, coordinates.x - Dist, coordinates.y + Dist, coordinates.y - Dist);

		for(Object obj: geography.getObjectsWithin(envelope)) {
			if (obj instanceof Resident){
				NonEVCount = NonEVCount +1;
			}
			if (obj instanceof ResidentEV){
				CEVCount = CEVCount +1;
			}
		}
		NBEVPene = CEVCount/(CEVCount+NonEVCount);
		
		// 5000 meter about 10 minutes drive, 3 miles
		double DistMeter1 = 1609.344; // 1 mile
		double Dist1 = DistMeter1*tf;
		Envelope envelope1 = new Envelope(coordinates.x + Dist1, coordinates.x - Dist1, coordinates.y + Dist1, coordinates.y - Dist1);
		for(Object obj: geography.getObjectsWithin(envelope1, ParcelStation.class)) {
			StationCount = StationCount + 1;
		}		
		
		// Utility for neighbor: from station and from penetration of EV in the neighborhood
		double station_coe = 0.1;// 0.02 is good
		double Pene_coe = 1 - station_coe;
		double pene_part = NBEVPene * Pene_coe * score[2];
		double Neighbor = station_coe * score[2]/5.0 * ((StationCount>0)?StationCount:0) + ((pene_part>Pene_coe*score[2])?Pene_coe*score[2]: pene_part);
		
		NBimpact = NBimpact + Neighbor;
		double UNBimpact;
		UNBimpact = (NBimpact>score[2])?score[2]:NBimpact;
		NBimpact=UNBimpact;
		double [] UNeighbour = {UNBimpact, score[2]-UNBimpact, score[2]-UNBimpact};
		
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		//Economics - Cost benefit analysis
		int[] Range_Run;  
		int[][] MSRP_Run = null;
		int[] ICEMSRP_Run = null;
		double ChargerCost_Run = 0;
		double GasCost_Run = 0;
		double ElecCost_Run = 0;
		double Discount = 0;
		
		for (Object o : context.getObjects(EVMarket.class)) {
	        EVMarket obj = (EVMarket) o;
	        Range_Run = obj.getRange();
	        MSRP_Run = obj.getMSRP();
	        ICEMSRP_Run = obj.getICEMSRP();
	        ChargerCost_Run = obj.getChargerCost();
	        GasCost_Run = obj.getGasCost();
	        ElecCost_Run = obj.getElecCost();
	        Discount = obj.getDiscount();
	        }
		
		//Calculate the minimum cost to own a vehicle
		int minsize;
		if(Vsize==2) {minsize=0;} else {minsize=Vsize;};
		int loan_yr = 5;
		double CRF = Discount*Math.pow(1+Discount,loan_yr)/(Math.pow(1+Discount,loan_yr)-1);
		double annual_mile = 15000;
		
		//Calculate the minimum cost to own an electric vehicle
		double min_EV_cost, Annual_EV_Cost, maintenance_EV, fuel_EV;
		int charger_index;
		if (StationCount ==0) {charger_index = 1;} else {charger_index = 0;}
		min_EV_cost = MSRP_Run[minsize][Vrange] + ChargerCost_Run * charger_index* (1-Homecharger);   //One time cost: vehicle cost + charger cost
		maintenance_EV = 0.026*annual_mile;
		fuel_EV = ElecCost_Run*annual_mile/3.00/100.00;
		Annual_EV_Cost = min_EV_cost*CRF + maintenance_EV + fuel_EV; //Annual part + fuel cost and maintenance cost
		
		//Calculate the minimum cost to own an ICE vehicle
		double ICE_cost, Annual_ICE_Cost, maintenance, fuel;
		ICE_cost = ICEMSRP_Run[minsize]-10000;
		maintenance = 0.061 * annual_mile;
		fuel = GasCost_Run * annual_mile/21.00; //21 MPG
		Annual_ICE_Cost = ICE_cost*CRF + maintenance + fuel;
		
		//100 points for economic aspect
		double [] UEcon= new double[3];
		double PercentWTP = 0.35; // willing to pay on car total/annual Income (this is the high end)
		double AnnualWTP=PercentWTP*Income*CRF;
		UEcon[0]=((AnnualWTP<Annual_EV_Cost)?-Annual_EV_Cost/AnnualWTP*score[3]:((1.0-(Annual_EV_Cost/AnnualWTP))*score[3]));
		UEcon[1]=((AnnualWTP<Annual_ICE_Cost)?-Annual_ICE_Cost/AnnualWTP*score[3]:((1.0-(Annual_ICE_Cost/AnnualWTP))*score[3]));
		UEcon[2]=score[3];
//		UEcon[2]=((AnnualWTP<Math.min(Annual_EV_Cost, Annual_ICE_Cost))?score[3]:Math.min(Annual_EV_Cost, Annual_ICE_Cost)/AnnualWTP*score[3]);
		
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		//calculate the weight
		double [][] Aweight=new double[4][4];
		double [] AHPweight = new double[4];
		
//		if (need2sell==1) {
//			Aweight[0][1]=5;
//			Aweight[0][2]=2;
//			Aweight[0][3]=1.0/6.0;
//		}else {
//			Aweight[0][1]=1.0/2.0;
//			Aweight[0][2]=1.0/5.0;
//			Aweight[0][3]=1.0/9.0;
//		}
		
		Aweight[0][1]=6;
		Aweight[0][2]=3;
		Aweight[0][3]=1.0/2.0;
		
		Aweight[1][2]=1.0/3.0;
		Aweight[1][3]=1.0/9.0;		
		Aweight[2][3]=1.0/4.0;
		
		Aweight=fillmatrix(Aweight);
		AHPweight=MatrixScore(Aweight);
		
		
		double [][] Aweight2=new double[3][3];
		double [] AHPweight2 = new double[3];
		
		Aweight2[0][1]=1.0/6;
		Aweight2[0][2]=1.0/2;
		Aweight2[1][2]=6;
		
		Aweight2=fillmatrix(Aweight2);
		AHPweight2=MatrixScore(Aweight2);
		
//		AHPweight[0]=0.3;
//		AHPweight[1]=0.05;
//		AHPweight[2]=0.15;
//		AHPweight[3]=0.5;
		
		//entropy weight
		
//		AHPweight=EntropyWeight(AageScore, AattScore, AneiScore, AeconScore);
		
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		//take infect action based on utility fraction
		double [] Utility =  new double[3];
		double totUtility = 0.00;
		
		int Apartment_Not_index = 0;
		if (Apartment==1 && charger_index ==1) {
			Apartment_Not_index = 1;
		}
		
		int dis_temp1 = getRandIntBetween(0, 99);
//		System.out.printf("dis_temp1 is %d%n", dis_temp1);
		
		
		//////////////////////////////////////////////////////////////////////////////AHP combines Method2
		double [] AHPscore1 = new double[3];
		double [] AHPscore2 = new double[3];
		double AHPtot1=0.0, AHPtot2=0.0;
		for (i=0;i<3;i++) {
			AHPscore1[i]=AHPweight[0]*UAge[i]+AHPweight[1]*UAtt[i]+AHPweight[2]*UNeighbour[i]+AHPweight[3]*UEcon[i];
			AHPscore2[i]=AHPweight2[0]*UAtt[i]+AHPweight2[1]*UNeighbour[i]+AHPweight2[2]*UEcon[i];
//			AHPscore2[i]=AHPweight2[0]*UAge[i]+AHPweight2[1]*UAtt[i]+AHPweight2[2]*UNeighbour[i]+AHPweight2[3]*UEcon[i];
		}
		
//		System.out.printf("next customer\n");
//		for (double f:UAge) System.out.printf("%f ", f);
//		System.out.println();
//		for (double f:UAtt) System.out.printf("%f ", f);
//		System.out.println();
//		for (double f:UNeighbour) System.out.printf("%f ", f);
//		System.out.println();
//		for (double f:UEcon) System.out.printf("%f ", f);
//		System.out.println();
		
		AHPtot1 = ((Math.max(AHPscore1[0], AHPscore1[1])>0)?Math.max(AHPscore1[0], AHPscore1[1]):0) + ((AHPscore1[2]>0)?AHPscore1[2]:0);
		AHPtot2 = ((AHPscore2[0]>0)?AHPscore2[0]:0) + ((AHPscore2[1]>0)?AHPscore2[1]:0);
		if (AHPtot1==0) {AHPtot1=1;}
		if (AHPtot2==0) {AHPtot2=1;}
		
		double p1=0.0, p2=0.0;
		
		p1=Math.max(AHPscore1[0], AHPscore1[1])/AHPtot1; //probability to purchase vehicle
		p2=AHPscore2[0]/AHPtot2; //probability to purchase EV
		
		if (dis_temp1<p1*p2*100.0*tune && Apartment_Not_index ==0 && p1>=0 && p2>=0){	
			Homecharger = ((charger_index==1)?1: Homecharger);
			if (charger_index ==0) { //no need to install homecharger
				int dis_temp2 = RandomHelper.nextIntFromTo(0, 99);
				if (dis_temp2 < UEcon[0]) {
					Homecharger = 1;
				} 
			} //if homecharger ==0, means there are charging station in the neighbourhood. Customer will choose whether to install 
			//a homecharger based on UEconomics
			infect(Income, Apartment, Node_ID, Homecharger, NBimpact, Family, Age, Educ, Vsize, Vrange, logit_score, HouseID);
		}else if (dis_temp1<p1*100*tune){
			Vage = 0;
			getICE = 1;
		}	
		
		/////////////////////////////////////////////////////////////////////////////////////////////////////////	
		//Update vehicle age and income
		Age = Age +1;
		Vage = Vage + 1;
		int dis_temp3 = RandomHelper.nextIntFromTo(0, 99);
		Income = (long) (Income+(1.00+0.04*dis_temp3/100.00));
		
//		long endTime   = System.nanoTime();
//		long totalTime = (endTime - startTime)/1000000000;
//		System.out.println(totalTime);
		}
	}
	
	public void infect(long Income, int Apartment, String Node_ID, int Homecharger, double NBimpact, int Family, int Age, int Educ, int Vsize, int Vrange, double logit, int HouseID) {
			// only move if we are not already in this grid location
		
		
			Context context = ContextUtils.getContext(this);
			
			//Update the homecharger info based on houseID
			if (HouseID>0) {
				for (Object o : context.getObjects(Resident.class)) {
					Resident obj = (Resident) o;
					int HouseID_loop = obj.getHouseID();
					if (HouseID_loop == HouseID) {
						obj.sethomecharger(1);
						break;
					}
				}
			}
		
			// get the geom of this resident
			Geography geography = (Geography)context.getProjection("geography");
			Geometry geom = geography.getGeometry(this);
			
			context.remove(this);
			
			int getEV = 1;
			int VEVage = 1;
			ResidentEV residentEV = new ResidentEV(Income, Apartment, Node_ID, Homecharger, VEVage, NBimpact, Family, Age, Educ, Vsize, Vrange, getEV, logit, HouseID); // not live in an apartment
			context.add(residentEV);
			geography.move(residentEV, geom);
			
			double tick = RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
			
			createxcel g = new createxcel();
			String path = "Q:\\My Drive\\Research\\EV Study\\EV OI v1\\Repast\\year\\year";
			g.openFile((int)tick, path);
			g.addCustomer(Node_ID, Homecharger);
			g.closeFile();
		
	}
	
	public static int findIndex(int arr[], int t) 
    { 
        if (arr == null) { return -1; } 
        int len = arr.length; 
        int i = 0; 
        // traverse in the array 
        while (i < len) { 
            if (arr[i] >= t) { return i; } 
            else { i = i + 1; } 
        } 
        return i; 
    } 
	
	public static int getRandIntBetween(int min, int max){
	    int x =  (int)(Math.random()*((max-min)+1))+min;
	    return x;
	}
	
	
	public static double[][] fillmatrix(double A[][]){
	    int row = A.length;
	    int col = A[0].length;
		
	    for (int i=0; i<row; i++) {
	    	for (int j=0; j<col; j++) {
	    		if(i==j) {
	    			A[i][j]=1;
	    		}else if(i>j) {
	    			A[i][j]=1/A[j][i];
	    		}
	    	}
	    }
	    
	    return A;
	}
	
	public static double[] MatrixScore(double A[][]){
	    int row = A.length;
	    int col = A[0].length;
		double[] sumcol=new double[col];
		double[] sumrow=new double[row];
		double[] score=new double[row];
	   
		//get the sum of the column
    	for (int j=0; j<col; j++) {
    		for (int i=0; i<row; i++) {
    		sumcol[j]=sumcol[j]+A[i][j];
    		}
    	}
    	
    	//get the nornalized matrix
	    for (int i=0; i<row; i++) {
	    	for (int j=0; j<col; j++) {
	    	A[i][j]=A[i][j]/sumcol[j];	
	    	}
	    }
    	
	    //get the score
	    for (int i=0; i<row; i++) {
	    	for (int j=0; j<col; j++) {
	    		sumrow[i]=sumrow[i]+A[i][j];	
	    	}
	    	score[i]=sumrow[i]/col;
	    }
	    
	    return score;
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
	
	public static double[] EntropyWeight(double C[], double A[], double N[], double E[]) {
		int row = C.length;

		double[] Aweight=new double[4];
		
		double[][] AA=new double[row][4];
		double[] colsum=new double[4];
		double[] colsum1=new double[4];
		int i, j;
		
		for (i=0; i<row; i++) {
			AA[i][0]=C[i];
			AA[i][1]=A[i];
			AA[i][2]=N[i];
			AA[i][3]=E[i];
			colsum[0]=colsum[0]+C[i];
			colsum[1]=colsum[1]+A[i];
			colsum[2]=colsum[2]+N[i];
			colsum[3]=colsum[3]+E[i];
		}
		
		//normalize
		for (i=0; i<row; i++) {
			for (j=0; j<4; j++) {
				AA[i][j]=AA[i][j]/colsum[i];
				AA[i][j]=AA[i][j]*Math.log(AA[i][j]);
				colsum1[j]=colsum1[j]+AA[i][j];
			}
		}
		
		double h=-1.0/Math.log(row);
		double[] ej=new double[4];
		double sumej;
		
		sumej=0;
		
		for (i=0;i<4;i++) {
			ej[i]=colsum1[i]*h;
			ej[i]=1-ej[i];
			sumej=sumej+ej[i];
		}
		
		for (i=0;i<4;i++) {
			Aweight[i]=ej[i]/sumej;
		}
		
		return Aweight;
	}
	
}



//// returns specified geography that are within the specified distance in meters from the centroid of the source object
//GeographyWithin within = new GeographyWithin(geography, 100, this);  //100 meters neighbour
//for (Object obj : within.query()) {
//	if (obj instanceof ResidentEV){
//		CEVCount = CEVCount +1;
//	}
//}
