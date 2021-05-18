/**
 *  First code review comments given by Naveen
 *  Added comments by preethi :18.01.2008
 */

public class Segment {
	int startingPos;//Position coordinates relative to the text given
	int endingPos;
	int index;
	String content = new String();//Segment itself.(eg.sentence in an abstract)
	public Segment(int st,int en,String n,int index)
	{
		setStartingPos(st);
		setEndingPos(en);
		setContent(n);
		setIndex(index);
	}
	public Segment()
	{
		
	}
	
	public int getEndingPos() {
		return endingPos;
	}
	public void setEndingPos(int endingPos) {
		this.endingPos = endingPos;
	}
	public int getStartingPos() {
		return startingPos;
	}
	public void setStartingPos(int startingPos) {
		this.startingPos = startingPos;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String Content) {
		//Make the first letter small case.. NO
		content = Content;
		//content = content.substring(0,1).toLowerCase() + content.substring(1);
	}
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}

	
}
