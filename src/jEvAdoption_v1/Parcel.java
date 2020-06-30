/**
 * 
 */
package jEvAdoption_v1;

import java.util.ArrayList;
import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Envelope;

import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.gis.util.GeometryUtil;
import repast.simphony.parameter.Parameters;
import repast.simphony.query.space.gis.GeographyWithin;
import repast.simphony.query.space.grid.GridCell;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.SpatialMath;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.gis.Geography;
import repast.simphony.space.gis.GeographyParameters;
import repast.simphony.space.graph.Network;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;
import repast.simphony.util.SimUtilities;

/**
 * @author Lisha
 * 
 */
public class Parcel {

	private String LandCode;
	private long TotVal;
	private long TotSale;
	private long HeatArea;
	private long ObjectID;
	private double DeedAcres;
	private String TypeID;
	
	public Parcel(String LandCode, long TotVal, long TotSale, long HeatArea, long ObjectID, double DeedAcres, String TypeID){
		this.LandCode = LandCode;
		this.TotVal = TotVal;
		this.TotSale = TotSale;
		this.HeatArea = HeatArea;
		this.ObjectID = ObjectID;
		this.DeedAcres = DeedAcres;
		this.TypeID = TypeID;
	}
	
	public String getLandCode() {
		return LandCode;
	}

	public void setLandCode(String ID) {
		this.LandCode = LandCode;
	}
	
	public String getTypeID() {
		return TypeID;
	}
	
	public double getDeedAcres() {
		return DeedAcres;
	}
	
	
	@ScheduledMethod(start = 1, interval = 1, priority = 4.0)
	public void run2() {
		// check every non-resident Parcel to decide whether an EV charging station will be installed
//		System.out.printf("Parcel updated%n");
		
		int tick = (int) RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
		
		int score = 0;
		int StationCount = 0;
		if (LandCode.equals("R")) //Residential. Apartment will be considered as well 
			score = 0;
		else {
			// get the geom of this parcel
			Context context = ContextUtils.getContext(this);
			Geography geography = (Geography)context.getProjection("geography");
			Geometry geom = geography.getGeometry(this);
			
			//Randomly create geometry geom1 inside each parcel
			List<Coordinate> XY = GeometryUtil.generateRandomPointsInPolygon(geom, 1);
			Geometry geom1 = null;
			GeometryFactory gm = new GeometryFactory();
			geom1 = gm.createPoint(XY.get(0));
			
			double CountStation = 0;
			double CountEV = 0;
			double CountNEV = 0;
			
			
			// policy testing add charging station
			
//			LandCode is C
//			ObjectID is 342132
//			ObjectID is 26841084
//			
//			LandCode is C
//			ObjectID is 50344
//			ObjectID is 2006439
			Parameters params = RunEnvironment.getInstance().getParameters();
			int test = (Integer) params.getValue("PublicCharging");
//			int test = 1;
			if (test==1) {
//				int tick = (int) RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
				int StartYr = (int) RunEnvironment.getInstance().getParameters().getValue("StartYr");
				if(tick == 1) {System.out.printf("Policy testing on Public Charging\n");}
				int Add_Station_yr = 2019;
				String Add_Node_ID = "26841084";
				if (StartYr+tick-1 == Add_Station_yr) {
					if (ObjectID==342132){
						for (Object o : context.getObjects(EVMarket.class)) {
					        EVMarket obj = (EVMarket) o;
					        StationCount = obj.getStationCount();
					        StationCount = StationCount + 1;
					        obj.setStationCount(StationCount);
					        }
						StationInfect(geom1, LandCode, Add_Node_ID, StationCount);
					}
				}	
			}
			
			// returns specified geography that are within the specified distance in meters from the centroid of the source object
			double EVscoreT = 45;
			double EVscoreScale = 10;
			double PhaseScoreT = 15;
			double LandScoreT = 40;
			
			
			
			// try to use getObjectsWithin to accelerate the process
			Coordinate coordinates = geom1.getCoordinates()[0];
			//create envelope around Agent, 111km is 1 degree of latitude or longitude
			Envelope envelope = new Envelope(coordinates.x + 0.0145, coordinates.x - 0.0145, coordinates.y + 0.0145, coordinates.y - 0.0145);
			//for all the Things in the envelope, add them to the list
			for(Object obj: geography.getObjectsWithin(envelope)) {
				if (obj instanceof ResidentEV){
					CountEV = CountEV +1;
				}
				if (obj instanceof Resident){
					CountNEV = CountNEV +1;
				}
			}
			
			Envelope envelope4 = new Envelope(coordinates.x + 0.0145, coordinates.x - 0.0145, coordinates.y + 0.0145, coordinates.y - 0.0145);
			//for all the Things in the envelope, add them to the list
			for(Object obj: geography.getObjectsWithin(envelope4)) {
				if (obj instanceof ParcelStation){
					CountStation = CountStation +1;
				}
			}
			
			double TotCust = 0;
			for (Object o : context.getObjects(EVMarket.class)) {
		        EVMarket obj = (EVMarket) o;
		        TotCust = obj.getTotCust();
			}
			
			
			if (CountStation == 0) {
				
				//score number of EVs nearby
				int score_evcount = 0;
				score_evcount = (int) (EVscoreT*CountEV*EVscoreScale/TotCust);
				score_evcount = (int) ((score_evcount>EVscoreT)?EVscoreT:score_evcount);
				
				//check the land attribute
				int score_land = 0;
				if (TypeID.equals("34")||TypeID.equals("36")||TypeID.equals("46")||TypeID.equals("48")||TypeID.equals("77"))
					score_land = (int) LandScoreT;
//				else if (LandCode.equals("E"))
				else if (TypeID.equals("7")||TypeID.equals("9")||TypeID.equals("17")||TypeID.equals("65")||TypeID.equals("66")||TypeID.equals("74"))
					score_land = (int) (LandScoreT*2.0/5.0);
				else if (LandCode.equals("E")&& (DeedAcres >=3) )
					score_land = (int) (LandScoreT/10.0);
				else if (LandCode.equals("V")||LandCode.equals("E"))
					score_land = (int) (LandScoreT/20.0);
				
				//check 3 phases nodes
				int score_phase = 0;
				String Node_ID = "Default";
				
				// try to use getObjectsWithin to accelerate the process
				Envelope envelope1 = new Envelope(coordinates.x + 0.00145, coordinates.x - 0.00145, coordinates.y + 0.00145, coordinates.y - 0.00145);
				for(Object obj: geography.getObjectsWithin(envelope1, Node.class)) {
//					Node_ID = ((Node) obj).getID();
					int phcount_temp = (int) ((Node) obj).getPhCount();
					if (phcount_temp == 3) {
						score_phase = (int) PhaseScoreT;
						Node_ID = ((Node) obj).getID();
					}
				}
				if (score_phase == 0) {
					Envelope envelope2 = new Envelope(coordinates.x + 0.007, coordinates.x - 0.007, coordinates.y + 0.007, coordinates.y - 0.007);
					for(Object obj: geography.getObjectsWithin(envelope2, Node.class)){
//						Node_ID = ((Node) obj).getID();
						int phcount_temp = (int) ((Node) obj).getPhCount();
						if (phcount_temp == 3) {
							score_phase = (int) (PhaseScoreT*10.0/15.0);
							Node_ID = ((Node) obj).getID();
						}
					}
				}
				if (score_phase == 0) {
					Envelope envelope3 = new Envelope(coordinates.x + 0.0145, coordinates.x - 0.0145, coordinates.y + 0.0145, coordinates.y - 0.0145);
					for(Object obj: geography.getObjectsWithin(envelope3, Node.class)){
//						Node_ID = ((Node) obj).getID();
						int phcount_temp = (int) ((Node) obj).getPhCount();
						if (phcount_temp == 3) {
							score_phase = (int) (PhaseScoreT/3.0);
							Node_ID = ((Node) obj).getID();
						}
					}
				}
				
				score = score_evcount + score_land + score_phase;
				
				
				if (score > 60) {
//				if (score > 100000) {
					for (Object o : context.getObjects(EVMarket.class)) {
				        EVMarket obj = (EVMarket) o;
				        StationCount = obj.getStationCount();
				        StationCount = StationCount + 1;
				        obj.setStationCount(StationCount);
				        }
					StationInfect(geom1, LandCode, Node_ID, StationCount);
					System.out.printf("Year %d, LandCode is %s%n", tick, LandCode);
					System.out.printf("Year %d, LandTypeID is %s%n", tick, TypeID);
//					System.out.printf("ObjectID is %d%n", ObjectID);
//					System.out.printf("ObjectID is %s%n", Node_ID);
				}

			}
		}
		
	}
	
	public void StationInfect(Geometry geom, String LandCode, String Node_ID, int station_count) {
		// only move if we are not already in this grid location
		// get the geom of this resident
		Context context = ContextUtils.getContext(this);
		Geography geography = (Geography)context.getProjection("geography");
		
		ParcelStation parcelstation = new ParcelStation(LandCode, Node_ID, 0, 0); // not live in an apartment
		context.add(parcelstation);
		geography.move(parcelstation, geom);
		
		double tick = RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
		
		createxcel g = new createxcel();
		String path = "Q:\\My Drive\\Research\\EV Study\\EV OI v1\\Repast\\year\\station";
		g.openFile((int)tick, path);
		g.addStation(Node_ID, station_count);
		g.closeFile();
	
	}
	
}



//// use GeographyWithin
//GeographyWithin within1 = new GeographyWithin(geography, 161, geom1);  //8047 meters neighbour, 0.1 miles
//for (Object obj : within1.query()) {
//	if (obj instanceof Node){
//		Node_ID = ((Node) obj).getID();
//		int phcount_temp = (int) ((Node) obj).getPhCount();
//		if (phcount_temp == 3) {
//			score_phase = 15;
//			Node_ID = ((Node) obj).getID();
//		}
//	}
//}
//if (score_phase == 0) {
//	GeographyWithin within2 = new GeographyWithin(geography, 805, geom1);  //805 meters neighbour, 0.5 miles
//	for (Object obj : within2.query()) {
//		if (obj instanceof Node){
//			Node_ID = ((Node) obj).getID();
//			int phcount_temp = (int) ((Node) obj).getPhCount();
//			if (phcount_temp == 3) {
//				score_phase = 10;
//				Node_ID = ((Node) obj).getID();
//			}
//		}
//	}
//}
//if (score_phase == 0) {
//	GeographyWithin within3 = new GeographyWithin(geography, 1609, geom1);  //1609 meters neighbour, 1 miles
//	for (Object obj : within3.query()) {
//		if (obj instanceof Node){
//			Node_ID = ((Node) obj).getID();
//			int phcount_temp = (int) ((Node) obj).getPhCount();
//			if (phcount_temp == 3) {
//				score_phase = 5;
//				Node_ID = ((Node) obj).getID();
//			}
//		}
//	}
//}


//// use GerographyWithin
//GeographyWithin within = new GeographyWithin(geography, 1000, geom1);  //8047 meters neighbour, 5 miles
//for (Object obj : within.query()) {
//	if (obj instanceof ParcelStation){
//		CountStation = CountStation +1;
//	}
//	if (obj instanceof ResidentEV){
//		CountEV = CountEV +1;
//	}
//}