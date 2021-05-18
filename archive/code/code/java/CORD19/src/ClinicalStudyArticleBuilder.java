

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClinicalStudyArticleBuilder {
	
	//Fields - PMID, TI, AB, PT
	static final String[] fld_names = new String[]{"PMID","TI","AB"};
	static final Pattern sp = Pattern.compile("^[\\s|\\W]+");
			
	public static void main(String[] args) throws IOException {
		File raw_dir = new File(args[0]);
		int art_cnt_limit = args.length==3 ? new Integer(args[2].trim()) : -1;
		FileWriter fw = new FileWriter(args[1]+(art_cnt_limit>0 ? "_"+art_cnt_limit : "")+".tsv");
		fw.write("artid\ttitle\tabstract\tlabel\n");
		
		for(File file : raw_dir.listFiles()) {
			String fname = file.getName();
			if(file.getName().endsWith(".txt")) {
				System.out.println(" Processing file "+file);
				String label = fname.substring(fname.indexOf("-")+1, fname.indexOf(".txt"));
				processFile(file, label.toUpperCase(), art_cnt_limit, fw);
			}
		}
		fw.close();
	}
	
	public static void processFile(File infile , String label, int art_cnt, FileWriter fw) throws IOException {
		
		FileReader fr = new FileReader(infile);
		BufferedReader br = new BufferedReader(fr);
		
		String line = "";
		HashMap<String, String> fields = new HashMap<String,String>();
		int cnt=0;
		StringBuffer fval = new StringBuffer();
		boolean add =false;
		String prev_fsymb = "";
		while((line = br.readLine())!= null){
			if(cnt==art_cnt) break;
			Matcher m = sp.matcher(line);
			if(m.find()) {
				if(add) {
					fval.append(" "+line.trim());
				}
				continue;
			}
			
			line=line.trim();
			
			//Add the article
			if(line.length()==0) {
				if(!fields.isEmpty()) {
					StringBuffer s = new StringBuffer();
					for(String fld_name : fld_names) {
						String fld_val = fields.get(fld_name);
						if(s.length()>0)
							s.append("\t");
						s.append(fld_val==null ? "-" : fld_val);
					}
					fw.write(s.toString()+"\t"+label+"\n");
				}
				fields = new HashMap<>();
				cnt++;
				continue;
			}
			
			int fdelim = line.indexOf('-');
			if(fdelim==-1) continue;
			String fsymb =line.substring(0, fdelim).trim();
			String val = line.substring(fdelim+1, line.length()).trim();
			
			switch(fsymb) {
				case "PMID" :
					fields.put(fsymb, val);
					break;
				case "TI" :
					fval.append(val);
					add=true;
					prev_fsymb=fsymb;
					break;
				case "AB" : 
					fval.append(val);
					add=true;
					prev_fsymb=fsymb;
					break;
				/*case "PT" : 
					if(fields.containsKey("PT"))
						fields.put("PT", fields.get("PT")+","+val);
					else
						fields.put("PT", val);
					break;*/
				default:
					if(add && !prev_fsymb.equals(fsymb)) {
						fields.put(prev_fsymb, fval.toString());
						add=false;
						fval = new StringBuffer();
					}
					break;
			}
		}
		br.close();
		fr.close();
	}
}
