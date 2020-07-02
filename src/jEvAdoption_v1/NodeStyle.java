package jEvAdoption_v1;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.render.BasicWWTexture;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Offset;
import gov.nasa.worldwind.render.PatternFactory;
import gov.nasa.worldwind.render.WWTexture;
import repast.simphony.visualization.gis3D.PlaceMark;
import repast.simphony.visualization.gis3D.style.MarkStyle;

/**
 * Style for Nodes.  
 *
 */
public class NodeStyle implements MarkStyle<Node>{
	
	private Offset labelOffset;
	
	private Map<String, WWTexture> textureMap;
	
	public NodeStyle(){
		
		/**
		 * The gov.nasa.worldwind.render.Offset is used to position the label from 
		 *   the mark point location.  The first two arguments in the Offset 
		 *   constructor are the x and y offset values.  The third and fourth 
		 *   arguments are the x and y units for the offset. AVKey.FRACTION 
		 *   represents units of the image texture size, with 1.0 being one image 
		 *   width/height.  AVKey.PIXELS can be used to specify the offset in pixels. 
		 */
		labelOffset = new Offset(1.2d, 0.6d, AVKey.FRACTION, AVKey.FRACTION);
		
		/**
		 * Use of a map to store textures significantly reduces CPU and memory use
		 * since the same texture can be reused.  Textures can be created for different
		 * agent states and re-used when needed.
		 */
		textureMap = new HashMap<String, WWTexture>();
		
		//PATTERNS INCLUD PATTERN_CIRCLE, PATTERN_TRIANGLE_UP, PATTERN_DIAGONAL_UP, PATTERN_SQUARE 
		
		BufferedImage image = PatternFactory.createPattern(PatternFactory.PATTERN_TRIANGLE_UP, 
				new Dimension(50, 50), 0.7f,  Color.BLUE);
		
		textureMap.put("BLUE circle", new BasicWWTexture(image));
		
		image = PatternFactory.createPattern(PatternFactory.PATTERN_TRIANGLE_UP, 
				new Dimension(50, 50), 0.7f,  Color.darkGray);
		
		textureMap.put("GREEN circle", new BasicWWTexture(image));
	}
	
	/**
	 * The PlaceMark is a WWJ PointPlacemark implementation with a different 
	 *   texture handling mechanism.  All other standard WWJ PointPlacemark 
	 *   attributes can be changed here.  PointPlacemark label attributes could be
	 *   set here, but are also available through the MarkStyle interface.
	 *   
	 *   @see gov.nasa.worldwind.render.PointPlacemark for more info.
	 */
	@Override
	public PlaceMark getPlaceMark(Node agent, PlaceMark mark) {
		
		// PlaceMark is null on first call.
		if (mark == null)
			mark = new PlaceMark();
		
		mark.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
		mark.setLineEnabled(false);
		
		return mark;
	}
	

	@Override
	public double getElevation(Node agent) {
			return 0;
	}
	
	/**
	 * Here we set the appearance of the Node.
	 */
	@Override
	public WWTexture getTexture(Node agent, WWTexture texture) {
	
		// WWTexture is null on first call.
	
		if (agent.getPhCount() == 3){
			return textureMap.get("BLUE circle");
		}
		else{
			return textureMap.get("GREEN circle");
		}
	}
	
	/**
	 * Scale factor for the mark size.
	 */
	@Override
	public double getScale(Node agent) {
		if (agent.getPhCount() == 3){
			return 0.5; //0.2
		}
		else{
			return 0.5;
		}
	}

	@Override
	public double getHeading(Node agent) {
		return 0;
	}
	
	/**
	 * The agent on-screen label.  Return null instead of empty string "" for better
	 *   performance.
	 */
	@Override
	public String getLabel(Node agent) {
//		return "" + agent.getWaterRate();
		return null;
	}

	@Override
	public Color getLabelColor(Node agent) {
		if (agent.getPhCount() == 3){
			return Color.blue;
		}
		else{
			return Color.green;
		}
	}
	
	/**
	 * Return an Offset that determines the label position relative to the mark 
	 * position.  @see gov.nasa.worldwind.render.Offset
	 * 
	 */
	@Override
	public Offset getLabelOffset(Node agent) {
		return labelOffset;
	}

	@Override
	public Font getLabelFont(Node obj) {
		return null;
	}

	/** Width of the line that connects an elevated mark with the surface.  Use
	 *    a value of 0 to disable line drawing.
	 *   
	 */
	@Override
	public double getLineWidth(Node agent) {
		if (agent.getPhCount() == 3){
			return 2;
		}
		else{
			return 0;
		}
	}

	@Override
	public Material getLineMaterial(Node obj, Material lineMaterial) {
		if (lineMaterial == null){
			lineMaterial = new Material(Color.RED);
		}
		
		return lineMaterial;
	}

	@Override
	public Offset getIconOffset(Node obj) {
		return Offset.CENTER;
	}
}
