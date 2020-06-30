package jEvAdoption_v1;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.opengis.feature.simple.SimpleFeature;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.operation.distance.DistanceOp;
import com.vividsolutions.jts.geom.MultiPolygon;

import repast.simphony.context.Context;
import repast.simphony.context.DefaultContext;
import repast.simphony.context.space.gis.GeographyFactory;
import repast.simphony.context.space.gis.GeographyFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.gis.Geography;
import repast.simphony.space.gis.GeographyParameters;
import repast.simphony.util.ContextUtils;
import repast.simphony.gis.util.GeometryUtil;

public class JCustomerBuilder implements ContextBuilder<Object> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * repast.simphony.dataLoader.ContextBuilder#build(repast.simphony.context
	 * .Context)
	 */
	@Override
	public Context build(Context<Object> context) {
		context.setId("jEvAdoption_v1");
		
		GeographyParameters geoParams = new GeographyParameters();
		Geography geography = GeographyFactoryFinder.createGeographyFactory(null).createGeography("geography", context, geoParams);
		GeometryFactory fac = new GeometryFactory();
		
		//create Nodes agent from shape file
		String path = "Q:\\My Drive\\Research\\EV Study\\EV Repast Simphony\\JEvAdoption_Demo_v1\\data\\";
		
		
		List<SimpleFeature> features1 = new ArrayList<SimpleFeature>();
		features1 = loadFeaturesFromShapefile(path + "Nodes.shp");
		
		int VehperHouse = 2;
		int VehperApt = 2;
		
		for (SimpleFeature feature: features1) {
			Geometry geom = (Geometry)feature.getDefaultGeometry();
			Object agent = null;
			
			if (!geom.isValid()){
				System.out.println("Invalid geometry: " + feature.getID());
			}
			
			
			// For Points, create Node agents
			if (geom instanceof Point){
				geom = (Point)feature.getDefaultGeometry();				

				// Read the feature attributes and assign to the Node
				String NodeID = (String)feature.getAttribute("NodeID");
				long PhCount = (long)feature.getAttribute("PhCount");
				long LoadNode = (long)feature.getAttribute("LoadNode");
				long NodeType = (long)feature.getAttribute("NodeType");
				long NumCust = (long)feature.getAttribute("NumCust");
				
				Node node = new Node(NodeID, PhCount);
				context.add(node);
				geography.move(node, geom);
			}
			
		}
		

		//create house agent from shape file 
		
		List<SimpleFeature> features2 = new ArrayList<SimpleFeature>();
		features2 = loadFeaturesFromShapefile(path+"Parcels.shp");
		
		int ResCount = 0;
		int NonResCount = 0;
		int AptResCount = 0;
		
		int ResVCount = 0;
		int NonResVCount = 0;
		int AptResVCount = 0;
		

		int aptID = 0;

		
		////////////////////////////////////////////////////////////////////////////////////////////////////////
		//Read the txt file to create house agent created before
		Scanner x = null;
		try {x= new Scanner (new File (path+"0.txt"));} 
		catch (Exception e){System.out.println("could not find file");}
		while(x.hasNext()){
			double coordx=(double) x.nextDouble();
			double coordy=(double) x.nextDouble();
			long Income=(long) x.nextLong();
			int Apartment=(int) x.nextInt();  //0, not an apartment; 1 is an apartment
			String NodeID_temp=(String) x.next();
			double NBimpact=(double) x.nextDouble();
			int Family=(int) x.nextInt();
			int Age=(int) x.nextInt();
			int Educ=(int) x.nextInt();
			int Vage=(int) x.nextInt();
			int Vsize=(int) x.nextInt();
			int Vrange=(int) x.nextInt();
			int HouseID=(int) x.nextInt();
			
			if(Apartment == 1) {
				AptResCount = AptResCount + 1;
			}
			
			// add new resident
			int getICE=0;
			int turned = 0; 
			int Homecharger =0;
			Resident resident = new Resident(Income, Apartment, NodeID_temp, NBimpact, Family, Age, Educ, Vage, Vsize, Vrange, turned, getICE, Homecharger, HouseID); 
			context.add(resident);
			
			Geometry geom1 = null;
			GeometryFactory gm = new GeometryFactory();
			geom1 = gm.createPoint(new Coordinate(coordx, coordy));
			geography.move(resident, geom1);

		}
		x.close();
		/////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		
	
//		//////////////////////////////////////////////////////////{create resident (you only need to create once, then read the 0.txt file)
//		//row:  50k, $50k - 75k, $75k - 100k, $100k - 125k, 125k
//		//Column: (0-4) Less Than High School,  High School,  Associates,  Bachelor,  Graduate
//		int HouseID = 0; //need to comment out this
//		
//		int[] IncomeCat = {50000, 75000, 100000, 125000};
//		
//		int[][] EducDist = {{5, 27, 65, 86, 100},
//				{0, 6,36,71,100},
//				{0,3,27,59,100},
//				{0,4,15,49,100},
//				{0,1,8,35,100}};  
//
//		//Column:  20	20-30	30-40	40-50	50-60	60
//		int[][] AgeDist = {{0, 10, 21, 31, 48, 100},
//				{0,8,23,34,53,100},
//				{0,7,24,37,60,100},
//				{0,5,18,35,57,100},
//				{0,3,19,41,68,100}};  
//		int[] AgeCat={20, 30, 40, 50, 60, 90};
//		
//		//Column: 150, 150-200, 200-350, 400
//		int[] RangeDist = {95, 97, 99, 100};
//		
//		//Column: 1-2 yrs, 3-5 yrs, 6-10 yrs,  11 yrs
////		int[] VageDist = {12, 43, 72, 100}; // to connect vehicle age with income
//		int[][] VageDist = {{7, 27, 55, 100},
//				{11, 46, 75, 100},
//				{13, 55, 86, 100},
//				{17, 59, 89, 100},
//				{19, 64, 91, 100}};
//		int[] VageCat = {2, 5, 10, 20};
//		
//		// car, SUV, Both (0-2)
//		int[][] SizeDist = {{54,88,100},
//				{47,88,100},
//				{48,90,100},
//				{44,93,100},
//				{34,93,100},
//				{18,79,100},
//				{18,82,100}};
//		// Apartment renter
//		int[] AptIncomeDist = {40,70,90,100};
//		int[] AptIncomeCat = {30000, 50000, 100000, 125000};
//		int[] AptFamilyDist = {90, 100};
//		int[] AptFamilyCat = {1,2};
//		////////////////////////////////////////////////////////////////////////////////}
		
		for (SimpleFeature feature: features2) {
			Geometry geom = (Geometry)feature.getDefaultGeometry();
			Object agent = null;
			
			if (!geom.isValid()){
				System.out.println("Invalid geometry: " + feature.getID());
			}
			
			
			// For Polygons, create Zone agents
			if (geom instanceof MultiPolygon){
				MultiPolygon mp = (MultiPolygon)feature.getDefaultGeometry();
				geom = (Polygon)mp.getGeometryN(0);

				// Read the feature attributes and assign to the ZoneAgent
				String LandCode = (String)feature.getAttribute("LAND_CODE");
				long TotVal = (long)feature.getAttribute("TOTAL_VALU");
				long TotSale = (long)feature.getAttribute("TOTSALPRIC");
				long HeatArea = (long)feature.getAttribute("HEATEDAREA");
				long ObjectID = (long)feature.getAttribute("OBJECTID");
				double DeedAcres = (double)feature.getAttribute("DEED_ACRES");
				String TypeID = (String)feature.getAttribute("TYPE_AND_U");
				
//				long ObjectID = (long)feature.getAttribute("OBJECTID");
				
				Parcel parcel = new Parcel(LandCode, TotVal, TotSale, HeatArea, ObjectID, DeedAcres, TypeID);
				context.add(parcel);
				geography.move(parcel, geom);
				
				if (parcel.getLandCode().equals("R")) {
					
//					HouseID = HouseID + 1; //need to comment out this when run regular
					String NodeID_temp = "default";
					double distance = Double.POSITIVE_INFINITY;
					
					for (int vehcount = 0; vehcount< VehperHouse; vehcount++) {
						ResCount = ResCount + 1;
						
//						//////////////////////////////////////////////////////////////////////////////////////
//						//Randomly create geometry geom1 inside each residential house
//						long Income = Math.max (TotVal, TotSale)/5;   //Income relates to house value
//						List<Coordinate> XY = GeometryUtil.generateRandomPointsInPolygon(geom, 1);
//						Geometry geom1 = null;
//						GeometryFactory gm = new GeometryFactory();
//						geom1 = gm.createPoint(XY.get(0));
//						
//						//geom1 = gm.createPoint(new Coordinate(x, y));
//						if (vehcount == 0) {
////							double distance = Double.POSITIVE_INFINITY;
////							String NodeID_temp = "default";
//								//for each geom1 created, find the closest pole and assign the node ID
//							for (SimpleFeature feature_temp: features1) {
//								Geometry geom_temp = (Geometry)feature_temp.getDefaultGeometry();
//								if (!geom_temp.isValid()){
//									System.out.println("Invalid geometry: " + feature_temp.getID());
//								}
//								// For Points, check the distance to geom1
//								if (geom_temp instanceof Point){
//									if(String.valueOf(feature_temp.getAttribute("LoadNode")).equals("1")) {
//										geom_temp = (Point)feature_temp.getDefaultGeometry();
//										double distance_temp = DistanceOp.distance(geom_temp, geom1);
//										if (distance_temp < distance) {
//											distance = distance_temp;	
//											NodeID_temp = (String)feature_temp.getAttribute("NodeID");
//										}
//									}
//								}
//							}
//						}
//						// create the resident agent
//					
//						double NBimpact = 0;
//						int Family = (int) (Math.round (HeatArea/1000.00));
//						if (Family<=0) {Family=1;};
//						
//							//find income category
//						int IncomeNum, i;
//						for (i=0; i < IncomeCat.length; i++){if(Income<=IncomeCat[i]) {break;}};
//						IncomeNum = i;
//						
//							//initilize the parameters
//						int dis_temp, sizerow;
//						int Age, Educ, Vage, Vsize, Vrange;
//						
//						dis_temp = RandomHelper.nextIntFromTo(0, 99);
//						for (i=0; i < AgeDist[0].length; i++){if (dis_temp<=AgeDist[IncomeNum][i]) {break;}};
//						if(i==0) {Age=RandomHelper.nextIntFromTo(15, AgeCat[i]);
//						} else {Age=RandomHelper.nextIntFromTo(AgeCat[i-1]+1, AgeCat[i]);}
//						
//						dis_temp = RandomHelper.nextIntFromTo(0, 99);
//						for (i=0; i < VageDist[0].length; i++){if (dis_temp<=VageDist[IncomeNum][i]) {break;}};
//						if(i==0) {Vage=RandomHelper.nextIntFromTo(1, VageCat[i]);
//						} else {Vage=RandomHelper.nextIntFromTo(VageCat[i-1]+1, VageCat[i]);}	
//						
//						dis_temp = RandomHelper.nextIntFromTo(0, 99);
//						for (i=0; i < RangeDist.length; i++){if (dis_temp<=RangeDist[i]) {break;}};
//						Vrange = i;
//						
//						dis_temp = RandomHelper.nextIntFromTo(0, 99);
//						for (i=0; i < EducDist[0].length; i++){if (dis_temp<=EducDist[IncomeNum][i]) {break;}};
//						Educ = i;
//	
//						if(Family<SizeDist.length){sizerow=Family;}else{sizerow=SizeDist.length;};
//						dis_temp = RandomHelper.nextIntFromTo(0, 99);
//						for (i=0; i < SizeDist[0].length; i++){if (dis_temp<=SizeDist[sizerow-1][i]) {break;}};
//						Vsize = i;
//						
//						
//						//write to txt file. 
//						createxcel g = new createxcel();
//						g.openFile(0, path);
//						g.adddouble(XY.get(0).x);
//						g.adddouble(XY.get(0).y);
//						g.addlong(Income);
//						g.addint(0); //apartment indicator
//						g.addstring(NodeID_temp);
//						g.adddouble(NBimpact);
//						g.addint(Family);
//						g.addint(Age);
//						g.addint(Educ);
//						g.addint(Vage);
//						g.addint(Vsize);
//						g.addint(Vrange);
//						g.addintend(HouseID);
//						//XY.get(0), NodeID_temp, NBimpact, Family, Age, Educ, Vage, Vsize, Vrange
//						g.closeFile();
//						
//						// add new resident
//						int getICE=0;
//						int turned = 0; 
//						int Homecharger =0;
//						Resident resident = new Resident(Income, 0, NodeID_temp, NBimpact, Family, Age, Educ, Vage, Vsize, Vrange, turned, getICE, Homecharger, HouseID); // not live in an apartment
//						context.add(resident);
//						geography.move(resident, geom1);
//					//////////////////////////////////////////////////////////////////////////////////
					}
					
				}
				else if (parcel.getLandCode().equals("G")) {
					
					double sum_num_cust = 0;
//					////////////////////////////////////////////////////////////////////////////
//					for (SimpleFeature feature_temp: features1) {
//						Geometry geom_temp = (Geometry)feature_temp.getDefaultGeometry();
//						
//						if (!geom_temp.isValid()){
//							System.out.println("Invalid geometry: " + feature_temp.getID());
//						}
//						
//						// For Points, check whether it is within the polygon
//						if (geom_temp instanceof Point){
//							geom_temp = (Point)feature_temp.getDefaultGeometry();
//							if (geom.contains(geom_temp)) {
//								String NodeID_temp = (String)feature_temp.getAttribute("NodeID");
//								// Read the Number of customer and create apartment residents
//								long NumCust_temp = (long)feature_temp.getAttribute("NumCust");
//								sum_num_cust = sum_num_cust + NumCust_temp;
//								
//								double Num_Veh = 0;
//								if (NumCust_temp > 1) {
//									Num_Veh = NumCust_temp * VehperApt;
//								}
//								
//								else if (NumCust_temp ==1) {
//									Num_Veh = 30*VehperApt;
//								}
//								
//								AptResCount = AddApartment(Num_Veh, geom, path, NodeID_temp, HouseID, context, geography, AptResCount);
//
//							}
//						}
//					}
//					System.out.printf("num cust %f\n", sum_num_cust);
//				/////////////////////////////////////////////////////////////////////////////////////////
					
				}
				else
					NonResCount = NonResCount + 1;
			}
			
		}		
		
//		System.out.printf("The Count of Residential House is %d%n" , HouseID);
		System.out.printf("The Count of Residential Vehicle is %d%n" , ResCount);
		System.out.printf("The Count of Apartment Vehicle is %d%n" , AptResCount);
		System.out.printf("The Count of Total Vehicles is %d%n" , AptResCount + ResCount);
		System.out.printf("The Count of Non-Residential House is %d%n" , NonResCount);
		
//		Parameters params = RunEnvironment.getInstance().getParameters();
//		int evownerCount = (Integer) params.getValue("evowner_count");
//		for (int i = 0; i < evownerCount; i++) {
//			context.add(new EvOwner(space, grid));
//		}
		
		// get parameters from the GUI "Parameters"
		Parameters params = RunEnvironment.getInstance().getParameters();
//		int evownerCount = (Integer) params.getValue("evowner_count");
		int StartYr = (Integer) params.getValue("StartYr");
		int EndYr = (Integer) params.getValue("EndYr");
		double discount = (double) params.getValue("Discount");
		
		
		// Initialize the EV Market Environment
		int[] Range = {150, 200, 250}; //range level <= 
		int[][] MSRP = {{37655, 40712, 43920},{55850, 61017, 64585}}; //average MSRP
		int[] ICEMSRP = {30227, 40924};
		double ChargerCost= 1000;
		double GasCost = 2.9; //$/gallon//1000; //$ per year
		double ElecCost = 10.93; //cents/kwh //450; //$/year
		double TotCust = AptResCount + ResCount;
		

		EVMarket evmarket = new EVMarket(ChargerCost, Range, MSRP, ICEMSRP, GasCost, ElecCost, 
				StartYr, EndYr, discount, 0, TotCust);
		context.add(evmarket);
		
		//Old scripts
				
		//Set up run environment
		//get tick: RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
		
		
		if (RunEnvironment.getInstance().isBatch()) {
			RunEnvironment.getInstance().endAt(30);
		}

		return context;
	}
	
	private List<SimpleFeature> loadFeaturesFromShapefile(String filename){
	    URL url = null;
	    try {
	            url = new File(filename).toURL();
	    } catch (MalformedURLException e1) {
	            e1.printStackTrace();
	    }

	    List<SimpleFeature> features = new ArrayList<SimpleFeature>();

	    // Try to load the shapefile
	    SimpleFeatureIterator fiter = null;
	    ShapefileDataStore store = null;
	    store = new ShapefileDataStore(url);

	    try {
	            fiter = store.getFeatureSource().getFeatures().features();

	            while(fiter.hasNext()){
	                    features.add(fiter.next());
	            }
	    } catch (IOException e) {
	            e.printStackTrace();
	    }
	    finally{
	            fiter.close();
	            store.dispose();
	    }

	    return features;
	}
	
	public int AddApartment(double Num_Veh, Geometry geom, String path, String NodeID_temp, int HouseID, Context<Object> context, Geography geography, int AptResCount){
		// Apartment renter
		int[] AptIncomeDist = {40,70,90,100};
		int[] AptIncomeCat = {30000, 50000, 100000, 125000};
		int[] AptFamilyDist = {90, 100};
		int[] AptFamilyCat = {1,2};
		
		int[][] EducDist = {{5, 27, 65, 86, 100},
				{0, 6,36,71,100},
				{0,3,27,59,100},
				{0,4,15,49,100},
				{0,1,8,35,100}};  

		//Column:  20	20-30	30-40	40-50	50-60	60
		int[][] AgeDist = {{0, 10, 21, 31, 48, 100},
				{0,8,23,34,53,100},
				{0,7,24,37,60,100},
				{0,5,18,35,57,100},
				{0,3,19,41,68,100}};  
		int[] AgeCat={20, 30, 40, 50, 60, 90};
		
		//Column: 150, 150-200, 200-350, 400
		int[] RangeDist = {95, 97, 99, 100};
		
		//Column: 1-2 yrs, 3-5 yrs, 6-10 yrs,  11 yrs
//		int[] VageDist = {12, 43, 72, 100};
		int[][] VageDist = {{7, 27, 55, 100},
				{11, 46, 75, 100},
				{13, 55, 86, 100},
				{17, 59, 89, 100},
				{19, 64, 91, 100}};
		int[] VageCat = {2, 5, 10, 20};
		
		// car, SUV, Both (0-2)
		int[][] SizeDist = {{54,88,100},
				{47,88,100},
				{48,90,100},
				{44,93,100},
				{34,93,100},
				{18,79,100},
				{18,82,100}};
		
		
		for (int i = 0; i< Num_Veh; i++) {
			// create points inside polygon
			List<Coordinate> XY = GeometryUtil.generateRandomPointsInPolygon(geom, 1);
			
			int dis_temp, sizerow;
			int IncomeNum, j;
			long Income;
			int Family;
			
			dis_temp = RandomHelper.nextIntFromTo(0, 99);
			for (j=0; j < AptIncomeDist.length; j++){if (dis_temp<=AptIncomeDist[j]) {break;}};
			if(j==0) {Income=RandomHelper.nextIntFromTo(10000, AptIncomeCat[j]);
			} else {Income=RandomHelper.nextIntFromTo(AptIncomeCat[j-1]+1, AptIncomeCat[j]);}
			
			double NBimpact = 0;
			
			dis_temp = RandomHelper.nextIntFromTo(0, 99);
			for (j=0; j < AptFamilyDist.length; j++){if (dis_temp<=AptFamilyDist[j]) {break;}};
			if(j==0) {Family=RandomHelper.nextIntFromTo(1, AptFamilyCat[j]);
			} else {Family=RandomHelper.nextIntFromTo(AptFamilyCat[j-1]+1, AptFamilyCat[j]);}

			//find income category
			for (j=0; j < AptIncomeCat.length; j++){if(Income<=AptIncomeCat[j]) {break;}};
			IncomeNum = j;
			
				//initilize the parameters
			int Age, Educ, Vage, Vsize, Vrange;
			
			dis_temp = RandomHelper.nextIntFromTo(0, 99);
			for (j=0; j < AgeDist[0].length; j++){if (dis_temp<=AgeDist[IncomeNum][j]) {break;}};
			if(j==0) {Age=RandomHelper.nextIntFromTo(15, AgeCat[j]);
			} else {Age=RandomHelper.nextIntFromTo(AgeCat[j-1]+1, AgeCat[j]);}
			
			dis_temp = RandomHelper.nextIntFromTo(0, 99);
			for (j=0; j < VageDist[0].length; j++){if (dis_temp<=VageDist[IncomeNum][j]) {break;}};
			if(j==0) {Vage=RandomHelper.nextIntFromTo(1, VageCat[j]);
			} else {Vage=RandomHelper.nextIntFromTo(VageCat[j-1]+1, VageCat[j]);}	
			
			dis_temp = RandomHelper.nextIntFromTo(0, 99);
			for (j=0; j < RangeDist.length; j++){if (dis_temp<=RangeDist[j]) {break;}};
			Vrange = j;
			
			dis_temp = RandomHelper.nextIntFromTo(0, 99);
			for (j=0; j < EducDist[0].length; j++){if (dis_temp<=EducDist[IncomeNum][j]) {break;}};
			Educ = j;

			if(Family<SizeDist.length){sizerow=Family;}else{sizerow=SizeDist.length;};
			dis_temp = RandomHelper.nextIntFromTo(0, 99);
			for (j=0; j < SizeDist[0].length; j++){if (dis_temp<=SizeDist[sizerow-1][j]) {break;}};
			Vsize = j;
			
			
			//write to txt file. 
			createxcel g = new createxcel();
			g.openFile(0, path);
			g.adddouble(XY.get(0).x);
			g.adddouble(XY.get(0).y);
			g.addlong(Income);
			g.addint(1); //apartment indicator
			g.addstring(NodeID_temp);
			g.adddouble(NBimpact);
			g.addint(Family);
			g.addint(Age);
			g.addint(Educ);
			g.addint(Vage);
			g.addint(Vsize);
			g.addint(Vrange);
			g.addintend(HouseID);
			//XY.get(0), NodeID_temp, NBimpact, Family, Age, Educ, Vage, Vsize, Vrange
			g.closeFile();
			
			// add new resident
			int getICE = 0;
			int turned = 0;
			int Homecharger =0;
			int HouseIDapt = 0; //houseID =0 for apartment
			Resident resident = new Resident(Income, 1, NodeID_temp, NBimpact, Family, Age, Educ, Vage, Vsize, Vrange, turned, getICE, Homecharger, HouseIDapt); // live in an apartment
			context.add(resident);
			Geometry geom1 = null;
			GeometryFactory gm = new GeometryFactory();
			geom1 = gm.createPoint(XY.get(0));
			geography.move(resident, geom1);
			AptResCount = AptResCount + 1;
		}
		
		return AptResCount;
		
	}
	
}


