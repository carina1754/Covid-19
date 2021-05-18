/**
 *  First code review comments given by Naveen
 *  Added comments by preethi :18.01.2008
 */

public class Text {

	String Text; //Input text
	String AnnotatedXMLText;//annoted text obtained  from nerdict is stored here
	String section;
	
	public String getText() {
		return Text;
	}
	public void setText(String abstract1) {
		Text = abstract1;
	}
	public String getAnnotatedXMLText() {
		return AnnotatedXMLText;
	}
	public void setAnnotatedXMLText(String annotatedXMLText) {
		AnnotatedXMLText = annotatedXMLText;
	}
	
	/**
	 * @return Returns the section.
	 */
	public String getSection() {
		return section;
	}
	/**
	 * @param section The section to set.
	 */
	public void setSection(String section) {
		this.section = section;
	} 
}
