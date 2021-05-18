import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;


public class BERTInputPreprocess {
   
	static String HOME_DIR = "/home/sai/challenges/CORD-19-research-challenge/";
	static String DATA_DIR= HOME_DIR+"PRIORIT_Pipeline/Pipeline_Output/";
	static String CORD19_ARTS= HOME_DIR+"cord19_ft/cord19_arts.tsv";
	
	
	private static String pmcdir = HOME_DIR+"cord19_ft/";
	private static String medlinedir = HOME_DIR+"cord19_medline/";
	private static String bertdir = DATA_DIR;
	
	static HashMap<String, String> ftartsmap = new HashMap<String, String>();
	
	static FileWriter  fw = null;
	static boolean ftime = true;
	
	static LinkedHashSet<String> qmap = new LinkedHashSet<String>();
	
	static
	{
		qmap.add("What drugs were used for treatment of covid-19");
		qmap.add("What was used for treatment of covid-19");
		qmap.add("How many patients were there");
		qmap.add("What were the primary endpoints");
		qmap.add("How were the patients");
		qmap.add("How were the symptoms");
		qmap.add("Was there any improvement in the treatment");
	}
	
	public static void main(String[] args) throws IOException
	{
		
		processArts("cord19_pmc_xml_arts.tsv");
		processArts("cord19_ft_arts.tsv");
		processArts("cord19_pmc_xml_title_arts.tsv");
		processArts("cord19_title_arts.tsv");
		processMed("cord19_medline_arts.inp");
		System.out.println(ftartsmap.size());
		preparejson("novel_th_ab.tsv");
			
	}
	
	private static void processMed(String string) throws IOException{
		FileReader covidarts = new FileReader(new File(medlinedir+string));
		BufferedReader br1 = new BufferedReader(covidarts);
		String line= "";
		while((line=br1.readLine())!=null){
			 if("".equals(line.trim())) continue;
			 	if(line.trim().startsWith("cord_uid"))continue;
				String cols[] = line.split("\t");
				String pmid = "PMID_"+cols[1].trim();
				
				String tit = cols[2].trim();
				if(tit==null || tit.equalsIgnoreCase("\\N")) tit="";
				if(!tit.endsWith(".") & tit.length()>0)tit=tit+".";
				tit=coverttouni(tit.replaceAll("\"", "\\\\\"").replaceAll("\\\\ ", " "));
				
				String abs = cols[3].trim();
				if(abs==null || abs.equalsIgnoreCase("\\N")) abs="";
				if(!abs.endsWith(".") & abs.length()>0)abs=abs+".";
				abs=coverttouni(abs.replaceAll("\"", "\\\\\"").replaceAll("\\\\ ", " "));
				
				if(ftartsmap.get(pmid)!=null){
					System.out.println("Duplicate ID "+pmid);
				}
				else{
					ftartsmap.put(pmid, tit+" "+abs);
				}
		}
		
	}

	private static void preparejson(String filetoprocess) throws IOException{
		
		fw=new FileWriter(bertdir+"novel_th_ab_bert.json");
		
		fw.write("{\n  \"version\": \""+filetoprocess+"\",\n");		
		fw.write("  \"data\": [\n    {\n");
		fw.write("      \"title\": \""+filetoprocess+"\",\n");		
		fw.write("      \"paragraphs\": [\n");
		
		FileReader covidarts = new FileReader(new File(bertdir+filetoprocess));
		BufferedReader br1 = new BufferedReader(covidarts);
		String line= "";
		String prev_id="";
		HashMap<String,String> th_map = new HashMap<>();
		while((line=br1.readLine())!=null){
			 if("".equals(line.trim())) continue;
			 if(line.trim().startsWith("Date")) continue;
				String cols[] = line.split("\t");
				String cordid = cols[12].trim();
				String drugsassessed = cols[4].trim();
				String[] drugs=drugsassessed.split(",");
				
				if(!prev_id.equals(cordid)) {
					if(!th_map.isEmpty()) {
						processArticle(th_map, prev_id);
					}
					th_map = new HashMap<>();
					prev_id = cordid;
				}
				
				for(String s : drugs){
					String sd = s.trim();
					String tid = "";
					if(s.trim().indexOf("[")>0) {
						sd = s.trim().substring(0, s.trim().indexOf("["));
						tid = s.trim().substring(s.trim().indexOf("[")+1, s.length()-1);
						th_map.put(tid, sd);
					}
				}
				
		}
		
		if(!th_map.isEmpty()) {
			processArticle(th_map, prev_id);
		}
		
		
		br1.close();
		fw.write("\n      ]\n    }\n  ]\n}");
		fw.close();		
	}

	private static void processArticle(HashMap<String,String> th_map, String cordid) throws IOException {
		
		/*for(String s : drugs){
			String sd = s.trim();
			String tid = "";
			if(s.trim().indexOf("[")>0) {
				sd = s.trim().substring(0, s.trim().indexOf("["));
				tid = s.trim().substring(s.trim().indexOf("[")+1, s.length()-1);
				if(!th_art.contains(tid)) {
					qmap.add("How effective was "+sd);
					th_art.add(tid);
				}
			}
		}*/
		
		for(String tid : th_map.keySet()) {
			qmap.add("How effective was "+th_map.get(tid));
		}
		
		String ta = ftartsmap.get(cordid);
		addContext(ta,cordid);
		
		for(String tid : th_map.keySet()) {
			qmap.remove("How effective was "+th_map.get(tid));
		}
		
		/*for(String s : drugs){
			String sd = s.trim();
			String tid = "";
			if(s.trim().indexOf("[")>0) {
				sd = s.trim().substring(0, s.trim().indexOf("["));
				tid = s.trim().substring(s.trim().indexOf("[")+1, s.length()-1);
				if(th_art.contains(tid)) {
					qmap.remove("How effective was "+sd);
				}
			}
		}*/
		
	}
	
	private static void addContext(String ta,String corduid) throws IOException{
		int i = 0;
		for(String s : qmap){
			i++;
			if(ftime)fw.write("        {\n");
			else fw.write(",\n        {\n");
			ftime=false;
			fw.write("          \"context\": \""+ta+"\",\n");
			fw.write("          \"qas\": [\n");
			fw.write("            {\n");
			fw.write("              \"question\": \""+s.trim()+"?\",\n");
			fw.write("              \"id\": \""+corduid+"_q"+i+"\"\n");
			fw.write("            }\n          ]\n        }");
		}
	}

	private static void processArts(String filetoprocess) throws IOException{
		
		FileReader covidarts = new FileReader(new File(pmcdir+filetoprocess));
		BufferedReader br1 = new BufferedReader(covidarts);
		String line= "";
		while((line=br1.readLine())!=null){
			 if("".equals(line.trim())) continue;
			 	if(line.trim().startsWith("cord_uid"))continue;
				String cols[] = line.split("\t");
				String aid = cols[0].trim();
				String tit = cols[1].trim();
				if(tit==null || tit.equalsIgnoreCase("\\N")) tit="";
				if(!tit.endsWith(".") & tit.length()>0)tit=tit+".";
				tit=coverttouni(tit.replaceAll("\"", "\\\\\"").replaceAll("\\\\ ", " "));
				
				String abs = cols[2].trim();
				if(abs==null || abs.equalsIgnoreCase("\\N")) abs="";
				if(!abs.endsWith(".") & abs.length()>0)abs=abs+".";
				abs=coverttouni(abs.replaceAll("\"", "\\\\\"").replaceAll("\\\\ ", " "));
				
				if(ftartsmap.get(aid)!=null){
					//System.out.println("Duplicate ID "+aid);
				}
				else{
					ftartsmap.put(aid, tit+" "+abs);
				}
				}
			
		br1.close();
	
	}
	
	private static String coverttouni(String arttab){
		StringBuilder b = new StringBuilder();

	    for (char c : arttab.toCharArray()) {
	        if (c >= 128)
	            b.append("\\u").append(String.format("%04X", (int) c));
	        else
	            b.append(c);
	    }
	    return b.toString();
	}
	
}
