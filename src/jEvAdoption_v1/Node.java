/**
 * 
 */
package jEvAdoption_v1;

/**
 * @author Lisha
 * 
 */
public class Node {

	private String Node_ID;
	private long PhCount;
	
	
	public Node(String Node_ID, long PhCount){
		this.Node_ID = Node_ID;
		this.PhCount = PhCount;
	}
	
	public String getID() {
		return Node_ID;
	}
	
	public long getPhCount() {
		return PhCount;
	}

	public void setID(String ID) {
		this.Node_ID = Node_ID;
	}
	
}
