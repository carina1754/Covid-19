import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


public class ProcessCovidFtArtsTitle {
   
	private static String dir = "/home/sai/challenges/CORD-19-research-challenge/PRIORIT_Pipeline/Pipeline_Output/PMCDownloads/PMCArticlesprocessed/";
        private static String cord19_ft_dir = "/home/sai/challenges/CORD-19-research-challenge/cord19_ft/";
	static int paracount = 0;
	static int sectioncount = 0;
	static FileWriter  fw = null;
	static boolean lastisstart = true;
	static int prevseclabel = 0;
	static final String[] GreekLetterWords = { "alpha", "beta", // list of greek words
		"gamma", "delta", "epsilon", "zeta", "eta", "theta", "iota",
		"kappa", "lambda", "mu", "nu", "xi", "omicron", "pi", "rho",
		"sigma", "tau", "upsilon", "phi", "chi", "psi", "omega" };
	static HashMap<String,String> greekLetterMap = null;
	static final int upperGreekStart = 913; //0391
	static final int upperGreekEnd = 937; //03a9
	static final int lowerGreekStart = 945; //03b1
	static final int lowerGreekEnd = 969; //03c9
	
	static
	{
		
		int uppercnt = upperGreekStart;
		int lowercnt = lowerGreekStart;
		greekLetterMap = new HashMap<String, String>();
		for(String greekletter : GreekLetterWords)
		{
			if("sigma".equalsIgnoreCase(greekletter)){
				uppercnt++;
				greekLetterMap.put(lowercnt+"", greekletter); //Special - lower Case final
				lowercnt++;
			}
			greekLetterMap.put(uppercnt+"", greekletter); //Upper Case
			greekLetterMap.put(lowercnt+"", greekletter); //Lower case
			uppercnt++;
			lowercnt++;
		}
	}
	
	public static void main(String[] args) throws IOException
	{
		processtitle(cord19_ft_dir+"cord19_pmc_xml_title_arts.tsv","pmctit");
		processtitle(cord19_ft_dir+"cord19_title_arts.tsv","other");

	}
	
	private static void processtitle(String filetoprocess,String downloadtype) throws IOException{
		
		FileReader covidarts = new FileReader(new File(filetoprocess));
		
		BufferedReader br1 = new BufferedReader(covidarts);
		String line= "";
		while((line=br1.readLine())!=null){
			 if("".equals(line.trim())) continue;
			 	
				String cols[] = line.split("\t");
				if(cols.length<2) continue;

				String tit = cols[1].trim();
				String abs = "";
				if(cols.length<3) abs = "";
				else
				abs = cols[2].trim();
				//String ft = cols[4].trim();ft = ft.replaceAll("\"\"", "\"");
				
				String aid = cols[0].trim();
				//String refid = cols[4].trim();
				if(downloadtype.equalsIgnoreCase("pmctit"))
				fw = new FileWriter(dir+"cord19_xml_title_"+aid+".xml");
				else
				fw = new FileWriter(dir+"cord19_json_title_"+aid+".xml");
				sectioncount = 0;
				paracount = 0;
				if(aid!=null && aid.trim().length()>0){
					fw.write("<aid>\n"+aid+"\n</aid>\n");
				}
				if(tit!=null && tit.trim().length()>0){
					fw.write("<title loc=\"T\">\n"+tit+"\n</title>\n");
				}
				
				if(abs!=null && abs.trim().length()>0){
					fw.write("<abstract loc=\"A\">\n<section-title>\nAbstract\n</section-title>\n<section-content>\n"+abs+"\n</section-content>\n</abstract>\n");
				}
				
				fw.close();					
			
		}
		br1.close();

	}
	
	private static String cleanseContent(String rawcontent) throws UnsupportedEncodingException
	{
		StringBuffer sb = new StringBuffer();
		for(Character c : rawcontent.toCharArray())
		{
			int cval = ((int)c);
			if((cval >= upperGreekStart && cval<=upperGreekEnd) || (cval >= lowerGreekStart && cval<=lowerGreekEnd))
			{
				String conv = greekLetterMap.get(cval+"");
				if(conv!=null)
					sb.append(conv);
				
				else
					sb.append(c);
			}
			else
			{
				if(!isUnwantedLetter(cval))
					sb.append(c);
				else
					sb.append(' ');
			}
		}
		
		return sb.toString();
		
	}
	
	private static boolean isUnwantedLetter(int cval)
	{
		if((cval>0 && cval<32) || (cval>127))
			return true;
		return false;
	}
	
	
	private static void parseBody(Document doc) throws IOException, XPathExpressionException, Exception {
		//Elements : ce:section, ce:para
		String tagname = "";
		ArrayList<String> childtags = new ArrayList<String>();
		
		tagname ="body";
		NodeList nodes = doc.getElementsByTagName(tagname);
		
		if(nodes!=null && nodes.getLength()>0)
		{
			addCustomStartTag("body");
			for (int i = 0; i < nodes.getLength(); i++) 
		    {
				Node parnode = nodes.item(i);
				NodeList childnodes = parnode.getChildNodes();
				handleSections(childnodes);
		    }
			addCustomEndTag("body");
		}	
	}
	
	private static void addCustomEndTag(String tagname) throws Exception
	{
		if(tagname.indexOf(":")!=-1){
			tagname = tagname.substring(tagname.indexOf(":")+1, tagname.length());
		}
		if(lastisstart){
			lastisstart = false;
			fw.write("\n");
		}
		fw.write("</"+tagname+">\n");
	}
	
	private static void addCustomStartTag(String tagname) throws Exception
	{
		if(tagname.indexOf(":")!=-1){
			tagname = tagname.substring(tagname.indexOf(":")+1, tagname.length());
		}
		fw.write("<"+tagname+">\n");
		lastisstart = true;
	}
	
	private static  void addCustomStartTag(String tagname, String attrib) throws Exception
	{
		if(tagname.indexOf(":")!=-1){
			tagname = tagname.substring(tagname.indexOf(":")+1, tagname.length());
		}
		fw.write("<"+tagname+" "+attrib+">\n");
		lastisstart = true;
	}
	
	private static  void handleSections(NodeList nodes) throws Exception
	{
		boolean isSectionContent = false;
		String secattribs = "";
		String sectiontype = "";
		for (int i = 0; i < nodes.getLength(); i++) 
	    {
			Node node = nodes.item(i);
			String nodename = node.getNodeName();
			if(node.getNodeType()==1 && "p".equals(nodename))
			{
				if(!isSectionContent) {addCustomStartTag("section-content");isSectionContent=true;}
				handlePara(node, sectiontype);
			}
			else if(node.getNodeType()==1 && ("section".equals(nodename)))
			{
				
				String secid = null;
				if(node.hasAttributes() && 
						node.getAttributes().getNamedItem("title")!=null){
					secid = node.getAttributes().getNamedItem("title").getNodeValue();
				}
				
				if(isSectionContent){addCustomEndTag("section-content"); isSectionContent=false;}
				secattribs="loc=\"S"+sectioncount+"\"";
					sectiontype="";
				
				addCustomStartTag("section", secattribs);
				handleSection(node, secid);
				addCustomEndTag("section");
			}
	    }
		
	}
	
	
	private static  void handleSection(Node node, String section) throws Exception 
	{
		NodeList childnodes = node.getChildNodes();
		boolean isSectionContent = false;
		String secattribs = "";
		String seclocattrib="loc=\"";
		if(!"".equals(section))
			seclocattrib="loc=\""+section+"-";
		handleSectiontitle(section);
		for (int i = 0; i < childnodes.getLength(); i++) 
	    {
			Node childnode = childnodes.item(i);
			String nodename = childnode.getNodeName();
			if(childnode.getNodeType()==1 && "p".equals(nodename))
			{
				if(!isSectionContent) {addCustomStartTag("section-content");isSectionContent=true;}
				handlePara(childnode, seclocattrib);
			}
	    }
		
		if(isSectionContent) {addCustomEndTag("section-content"); isSectionContent=false;}
		
		return;
	}

	private static  void handleSectiontitle(String section) throws Exception 
	{
		addCustomStartTag("section-title");
		sectioncount++;
		if(section==null) section="";
		fw.write((section));
		addCustomEndTag("section-title");
	}

	private static  void handlePara(Node node, String seclocattrib) throws Exception 
	{
		if("".equals(seclocattrib))
			seclocattrib="loc=\"";
		addCustomStartTag("para","loc=\"P"+paracount+"\"");
		paracount++;
		fw.write((node.getTextContent()));
		addCustomEndTag("para");
	}

	
	private static Document convertStringToXMLDocument(String xmlString) throws ParserConfigurationException, SAXException, IOException   
    {
        //Parser that produces DOM object trees from XML content
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        factory.setNamespaceAware(false);
        //API to obtain DOM Document instance
        DocumentBuilder builder = null  ;
        //xmlString = StringEscapeUtils.escapeXml(xmlString);
        xmlString = xmlString.replaceAll(" \""," &quot;").replaceAll("'","&#039;").replaceAll(" & ", " &amp; ").replaceAll(" < ", " &lt; ").replaceAll(" > ", " &gt; ");
            //Create DocumentBuilder with default configuration
            builder = factory.newDocumentBuilder();
            
           // InputSource is = new InputSource();
            //is.setEncoding("UTF-8");
            //is.setCharacterStream(new StringReader(xmlString));
            //Parse the content to Document object
            Document doc = builder.parse(new InputSource(new StringReader(cleanseContent(xmlString))));
    		doc.normalizeDocument();
            return doc;
    }



}

