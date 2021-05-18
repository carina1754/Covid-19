import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class CORD19_Medline_Merger {
	
	static String HOME_DIR = "/home/sai/challenges/CORD-19-research-challenge/";
	static String DATA_DIR= HOME_DIR+"PRIORIT_Pipeline/Pipeline_Output/";
	static String CORD19_FILT_MEDLINE_ARTS= HOME_DIR+"cord19_medline/cord19_medline_arts.inp";
	static String CORD19_ARTS_DIR= HOME_DIR+"cord19_ft/";
	static String CORD19_ARTS= CORD19_ARTS_DIR+"cord19_arts.tsv";
	
	
	public static void main(String[] args) throws IOException {
		
		System.out.println(" Performing sanity checks on the files ..");
		
		//Step 1. Check CORD19 FT and medline articles 
		//new CORD19_Medline_Merger().checkMedlineArts(new File(CORD19_FILT_MEDLINE_ARTS));
		File cord19_ft_dir = new File(CORD19_ARTS_DIR);
		/*for(File f : cord19_ft_dir.listFiles()) {
			new CORD19_Medline_Merger().checkColumns(f);
		}*/
		
		new File(CORD19_ARTS).delete();
		
		System.out.println(" Merging metadata files ..");
		//Step 2. Merge the articles and check for duplicates
		boolean includeHeader=true;
		for(File f : cord19_ft_dir.listFiles()) {
			System.out.println(" Merging metadata file .."+f.getName());
			new CORD19_Medline_Merger().mergeFiles(f, CORD19_ARTS, includeHeader);
			includeHeader=false;
		}
		
		System.out.println(" Adding medline metadata files ..");
		//Step 3. Merge the medline articles
		new CORD19_Medline_Merger().mergeWMedlineFile(new File(CORD19_FILT_MEDLINE_ARTS), CORD19_ARTS);
		
		System.out.println(CORD19_ARTS+" metadata file created ..");
	}
	
	private void mergeFiles(File infile, String outfile, boolean includeHeader) throws IOException {
		FileReader fr = new FileReader(infile);
		BufferedReader br = new BufferedReader(fr);
		String line = "";
		FileWriter fw = new FileWriter(outfile, true);
		String header = br.readLine().trim();
		if(includeHeader)
			fw.write(header+"\n");
		while( (line = br.readLine()) != null)
		{
			fw.write(line+"\n");
		}
		
		fr.close();
		br.close();
		fw.close();
		
	}
	
	private void mergeWMedlineFile(File filt_medline_file, String outfile) throws IOException {
		//PMID_"$2"\t"$3"\t"$4"\t-\t"$6"\t"$8"\t-\t"$1
		FileReader fr = new FileReader(filt_medline_file);
		BufferedReader br = new BufferedReader(fr);
		String line = "";
		FileWriter fw = new FileWriter(outfile, true);
		br.readLine().trim();
		while( (line = br.readLine()) != null)
		{
			String[] cols = line.trim().split("\t");
			String row = "PMID_"+cols[1].trim()+"\t"+cols[2].trim()+"\t"+cols[3].trim()+"\t-\t"+cols[5].trim()+"\t"+cols[7].trim()+"\t-\t"+cols[0].trim();
			fw.write(row+"\n");
		}
		
		fr.close();
		br.close();
		fw.close();
		
	}
	
	private void checkColumns(File infile) throws IOException {
		FileReader fr = new FileReader(infile);
		BufferedReader br = new BufferedReader(fr);
		String line = "";
		FileWriter fw = new FileWriter("temp.tsv");
		String header = br.readLine().trim();
		
		int hcols = header.split("\t").length;
		int cnt=0;
		fw.write(header+"\n");
		while( (line = br.readLine()) != null)
		{
			line = line.trim().replaceAll("\\t*$", "");
			String[] cols = line.split("\t");
			StringBuffer sb = new StringBuffer();
			for(int i=0;i<hcols;i++) {
				if(sb.length()>0)
					sb.append("\t");
				if(i>=cols.length) {
					sb.append("-");
					continue;
				}
				String col = cols[i].trim();
				if(col.length()>0) {
					col = col.replace("\\N","-");
					sb.append(col);
				}else
					sb.append("-");
			}
			fw.write(sb.toString()+"\n");
			cnt++;
		}
		
		fw.close();
		fr.close();
		br.close();
		
		new File("temp.tsv").renameTo(infile);
		System.out.println(" Checked "+cnt+" records ..");
	}
}


