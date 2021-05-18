import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NovelTherapeuticsPipeline {

	static String HOME_DIR = "/home/sai/challenges/CORD-19-research-challenge/";
	static String DATA_DIR= HOME_DIR+"PRIORIT_Pipeline/Pipeline_Output/";
	static String CORD19_ARTS= HOME_DIR+"cord19_ft/cord19_arts.tsv";
	static HashMap<String, HashSet<String>> FILTER_THERAPEUTICS = new HashMap<>();
	static final String FILTER_TH_TERM_FILE = "PostProcess-ID-Terms.txt";
	//Question 2
	static final String src_ent="C000657245";
	static final String src_ent_type="9";
	
	//Question 1
	static final String src_ent1="D019851";
	static final String src_ent_type1="28";

	static final String CLIN_STUDY_KEYWORDS_REGEX = "(treatment|([0-9]*\\s*(patients|participants))|study|studies)";
	static final Pattern CSP = Pattern.compile(CLIN_STUDY_KEYWORDS_REGEX, Pattern.CASE_INSENSITIVE);
	static HashSet<String> non_study_ids = new HashSet<>();
	
	static final String[] target_ent_types= new String[] {"7","8","27"};
	static final String LABELS_FILE=HOME_DIR+"Study_design_classification/data/cord19_study_design_v2.2k.csv";
	static final String CLINICAL_STUDY_ARTS_FILE = HOME_DIR+"Clinical_Study_classification/cord19_medline_clinical_study_v1.10k.csv";
	static HashSet<String> CLINICAL_STUDIES = new HashSet<>();
	static {
		try {
			loadFilteredTerms(FILTER_TH_TERM_FILE);
			loadClinicalStudies(CLINICAL_STUDY_ARTS_FILE);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	static int only_titles_cnt=0;
	static int non_clin_studies_cnt=0;
	static int filtered_th_art_cnt=0;
	static int abs_arts=0;
	static int ft_arts=0;
	
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		System.out.println("Filter Therapeutic terms "+FILTER_THERAPEUTICS.size());
		System.out.println("Clinical Studies  "+CLINICAL_STUDIES.size());
		
		NovelTherapeuticsPipeline ntp = new NovelTherapeuticsPipeline();
		
		HashSet<String> pc_pairs = ntp.getPCPairs(DATA_DIR+"pheno_assocs/pc_associations_master.txt", src_ent, src_ent_type, target_ent_types);
		System.out.println("Total Covid-19 PC Pairs with Drugs/Chemicals and Interventions "+pc_pairs.size());
		ntp.getPCArticles(src_ent, src_ent_type, pc_pairs, DATA_DIR+"inp/Tagger/output/annotations.inp", DATA_DIR+"novel_th_arts.tsv");
		System.out.println("Removed "+only_titles_cnt+" articles with only titles but no abstract or full-text ...");
		ntp.addTherapeuticsAndMetadata(DATA_DIR+"novel_th_arts.tsv", src_ent, src_ent_type, target_ent_types, DATA_DIR+"novel_th");
		System.out.println(" DONE ...");
	}

	private static void loadClinicalStudies(String clinicalStudyArtsFile) throws IOException {
		// TODO Auto-generated method stub
		FileReader fr = new FileReader(clinicalStudyArtsFile);
		BufferedReader br = new BufferedReader(fr);
		br.readLine(); //header
		String line = "";
		while( (line = br.readLine()) != null)
		{
			line = line.trim();
			String cols[] = line.split(",");
			int cs = new Integer(cols[1].trim());
			if(cs==1)
				CLINICAL_STUDIES.add(cols[0].trim()); //Add the TypeId of the term to be removed
		}
		
		br.close();
		fr.close();
	}

	private static void loadFilteredTerms(String filterThTermFile) throws IOException {
		// TODO Auto-generated method stub
		FileReader fr = new FileReader(filterThTermFile);
		BufferedReader br = new BufferedReader(fr);
		String line = "";
		while( (line = br.readLine()) != null)
		{
			String[] cols = line.trim().split("\t");
			String typeid = cols[0].trim();
			HashSet<String> terms = new HashSet<>();
			if(cols.length>1) {
				for(int i=1;i<cols.length;i++) {
					terms.add(cols[i].trim().toUpperCase());
				}
			}
			FILTER_THERAPEUTICS.put(typeid, terms); //Add the TypeId of the term to be removed
		}
		
		br.close();
		fr.close();
	}

	public HashSet<String> getPCPairs(String pc_pairfile, String src_ent, String src_ent_type, String[] target_ent_types) throws IOException {
		FileReader fr = new FileReader(pc_pairfile);
		BufferedReader br = new BufferedReader(fr);
		HashSet<String> tents = new HashSet<>(Arrays.asList(target_ent_types));
		String line = "";
		HashSet<String> assoc_pairs = new HashSet<>();
		while( (line = br.readLine()) != null)
		{
			String[] cols = line.split("\t");
			if( !(src_ent.equals(cols[1].trim()) && 
					src_ent_type.equals(cols[2].trim()))) continue;
			if(!tents.contains(cols[6].trim())) continue;
			assoc_pairs.add(cols[5].trim()+"::"+cols[6].trim());
		}
		
		br.close();
		fr.close();
		return assoc_pairs;
	}
	
	public void getPCArticles(String src_ent, String src_ent_type, HashSet<String> assoc_pairs, String annfile, String artfile) throws IOException {
		FileReader fr = new FileReader(annfile);
		BufferedReader br = new BufferedReader(fr);
		String line = "";
		String prev_id="";
		FileWriter fw = new FileWriter(artfile);
		Map<String, ArrayList<String>> art_span_map = new TreeMap<>();
		boolean art_flag=false;
		boolean assoc_flag=false;
		while( (line = br.readLine()) != null)
		{
			String[] cols = line.split("\t");
			if(!prev_id.equals(cols[0].trim())) {
				if(!art_span_map.isEmpty() && (art_flag && assoc_flag)) {
					processArticle(prev_id, art_span_map, src_ent, src_ent_type, fw);
				}
				art_span_map = new TreeMap<>();
				prev_id=cols[0].trim();
				art_flag=false;
				assoc_flag=false;
			}
			
			String pc_pair = cols[3].trim()+"::"+cols[4].trim();
			
			
			boolean add_row=false;
			if(cols[3].trim().equals(src_ent) && cols[4].trim().equals(src_ent_type)){
				art_flag=true;
				add_row=true;
			}
			if(assoc_pairs.contains(pc_pair)) {
				assoc_flag=true;
				add_row=true;
			}
			
			if(add_row) {
				String span = cols[8].trim();
				ArrayList<String> rows = art_span_map.get(span);
				if(rows==null) {
					rows = new ArrayList<>();
					art_span_map.put(span, rows);
				}
				rows.add(line);
			}
			
		}
		
		if(!art_span_map.isEmpty() && (art_flag && assoc_flag)) {
			processArticle(prev_id, art_span_map, src_ent, src_ent_type, fw);
		}
		
		br.close();
		fr.close();
		fw.close();
	}

	private void processArticle(String artid, Map<String, ArrayList<String>> art_span_map, String src_ent, String src_ent_type, FileWriter fw) throws IOException {
		
		if(Arrays.asList(new String[] {"T"}).containsAll(art_span_map.keySet())) {
			//System.out.println("### Found only Title Annotations, Ignoring the article .."+artid);
			only_titles_cnt++;
			return;
		}
		
		if(!CLINICAL_STUDIES.contains(artid)) {
			//System.out.println("### Found non-clinical study, Ignoring the article .."+artid);
			//non_clin_studies_cnt++;
			non_study_ids.add(artid);
			//return;
		}
		
		for(String art_span : art_span_map.keySet()) {
			ArrayList<String> span_rows = art_span_map.get(art_span);
			TreeMap<Integer, ArrayList<String>> sent_map = new TreeMap<>();
			HashSet<String> span_ents = new HashSet<>(); 
			for(String span_row : span_rows) {
				String[] cols = span_row.split("\t");
				String pc_pair = cols[3].trim()+"::"+cols[4].trim();
				span_ents.add(pc_pair);
				Integer sentid = new Integer(cols[10].trim());
				ArrayList<String> sent_rows = sent_map.get(sentid);
				if(sent_rows==null) {
					sent_rows = new ArrayList<>();
					sent_map.put(sentid, sent_rows);
				}
				sent_rows.add(span_row);
			}
			
			if(span_ents.size()<2) continue;
			
			String src_ent_tuple = src_ent+"::"+src_ent_type;
			if(span_ents.contains(src_ent_tuple)) {
				for(Integer sentid : sent_map.keySet()) {
					ArrayList<String> sent_rows = sent_map.get(sentid);
					for(String sent_row : sent_rows) {
						fw.write(sent_row+"\n");
					}
				}
			}
			
		}
	}
	
	private HashMap<String, String> getStudyDesignLabels(String labels_file) throws IOException {
		HashMap<String, String> art_labels = new HashMap<>();
		FileReader fr = new FileReader(labels_file);
		BufferedReader br = new BufferedReader(fr);
		br.readLine();
		String line="";
		while( (line = br.readLine()) != null)
		{
			String[] cols = line.trim().split(",");
			art_labels.put(cols[0].trim(), cols[1].trim());
		}
		fr.close();
		br.close();
		return art_labels;
	}

	public void addTherapeuticsAndMetadata(String infile, String src_ent, String src_ent_type, String[] target_ent_types, String outfile_prefix) throws IOException {
		System.out.println("Building sentences .... ");
		//Step 1. Build sentences from the resultset articles
		FileReader fr = new FileReader(infile);
		BufferedReader br = new BufferedReader(fr);
		String line = "";
		String prev_id="";
		String  ab_sents_file = outfile_prefix+"_ab_sents.tsv";
		String  ft_sents_file = outfile_prefix+"_ft_sents.tsv";
		FileWriter abs_fw = new FileWriter(ab_sents_file);
		FileWriter ft_fw = new FileWriter(ft_sents_file);
		HashSet<String> arts_added = new HashSet<>();
		int excl_art_cnt=0;
		Map<String, ArrayList<String>> art_span_map = new TreeMap<>();
		while( (line = br.readLine()) != null)
		{
			String[] cols = line.split("\t");
			if(!prev_id.equals(cols[0].trim())) {
				if(!art_span_map.isEmpty()) {
					boolean added = processSentence(art_span_map, src_ent, src_ent_type, abs_fw, ft_fw);
					if(added)
						arts_added.add(prev_id);
					else {
						excl_art_cnt++;
						System.out.println("XXXX "+prev_id);
					}
				}
				art_span_map = new TreeMap<>();
				prev_id=cols[0].trim();
			}
			
			String span = ((cols[8].trim().equals("A") || cols[8].trim().equals("T")) ? "AT" : cols[8].trim())+'-'+cols[10].trim();
			ArrayList<String> rows = art_span_map.get(span);
			if(rows==null) {
				rows = new ArrayList<>();
				art_span_map.put(span, rows);
			}
			rows.add(line);
		}
		
		if(!art_span_map.isEmpty()) {
			boolean added = processSentence(art_span_map, src_ent, src_ent_type, abs_fw, ft_fw);
			if(added)
				arts_added.add(prev_id);
			else {
				System.out.println("XXXX "+prev_id);
				excl_art_cnt++;
			}
		}
		
		br.close();
		fr.close();
		abs_fw.close();
		ft_fw.close();
		
		System.out.println(" Excluded non-span articles "+excl_art_cnt);
		
		//Step 2. Add article metadata to the output csv
		HashMap<String, DTBean> art_meta = getArticleMetadata(arts_added, LABELS_FILE);
		System.out.println("Fetched metadata for "+arts_added.size()+" articles ");
		
		System.out.println("Adding Therapeutics and General Outcomes ...");
		//Step 3. Write sentences to the output csv
		String ab_csv = outfile_prefix+"_ab.tsv";
		writeSentences(ab_sents_file, art_meta, target_ent_types, ab_csv);
		System.out.println("Removed "+non_clin_studies_cnt+" Non-clinical Study articles ...");
		System.out.println("Filtered "+filtered_th_art_cnt+" metadata  articles using excluded therapeutics ..");
		System.out.println(" Total Articles from Ab/Ti "+abs_arts);
		
		
		//System.out.println(" Total sentences from Full-text "+ft_sents);
	}

	private void writeSentences(String ab_sents_file, HashMap<String, DTBean> art_meta, String[] target_ent_types, String ab_csv) throws IOException {
		FileReader fr = new FileReader(ab_sents_file);
		BufferedReader br = new BufferedReader(fr);
		String line = "";
		String prev_id="";
		FileWriter fw = new FileWriter(ab_csv);
		fw.write("Date\tStudy\tStudy Link\tJournal\tTherapeutic method(s) utilized/assessed\tSample Size\tSeverity of Disease\tGeneral Outcome/Conclusion Excerpt\tPrimary Endpoint(s) of Study\tClinical Improvement (Y/N)\tStudy Type\tAdded On\tcord_uid\n");
		
		ArrayList<String> rows = new ArrayList<>();
		while( (line = br.readLine()) != null)
		{
			String[] cols = line.split("\t");
			if(!prev_id.equals(cols[0].trim())) {
				if(!rows.isEmpty()) {
					DTBean meta_b = art_meta.get(prev_id);
					if(meta_b==null) {
						System.out.println("Ignoring "+prev_id);
					}else {
						meta_b.cord_uid=prev_id;
						addAnnotations(prev_id, rows, meta_b, target_ent_types, fw);
					}
				}
				rows = new ArrayList<>();
				prev_id=cols[0].trim();
			}
			rows.add(line.trim());
		}
		
		if(!rows.isEmpty()) {
			DTBean meta_b = art_meta.get(prev_id);
			meta_b.cord_uid=prev_id;
			addAnnotations(prev_id, rows, meta_b, target_ent_types,  fw);
		}
		
		br.close();
		fr.close();
		fw.close();
	}

	private void addAnnotations(String cord_uid, ArrayList<String> rows, DTBean meta_b, String[] target_ent_types,  FileWriter fw) throws IOException {
		
		//Check if we are eliminating any clinical study document
		if(non_study_ids.contains(cord_uid) && 
				!checkStudyArticle(meta_b.study+" "+meta_b.abs)) {
			//System.out.println(" Ignoring non-clinical study article "+cord_uid);
			//System.out.println("\t"+cols[1].trim()+" "+cols[2].trim());
			non_clin_studies_cnt++;
			return;
		}
		
		boolean added=false;
		HashMap<String, String> ti_sent_map = getsentenceMap(meta_b.study); //Title
		HashMap<String, String> ab_sent_map = getsentenceMap(meta_b.abs); //abstract
		String prev_span_id="";
		HashMap<String, ArrayList<String>> ent_loc_map = new HashMap<>();
		for(String row : rows) {
			String[] cols = row.split("\t");
			String span_id=cols[8].trim()+"-"+cols[10].trim();
			
			if(!prev_span_id.equals(span_id)) {
				if(!ent_loc_map.isEmpty()) {
					String sent="";
					String[] locarr = prev_span_id.split("-");
					String span="";
					if(locarr[0].startsWith("T")) {
						sent = ti_sent_map.get(locarr[1]);
						span = meta_b.study;
					}	
					else if(locarr[0].startsWith("A")) {
						sent = ab_sent_map.get(locarr[1]);
						span =  meta_b.abs;
					}
					else {}
					boolean ad = getTherapeutics(ent_loc_map, target_ent_types, span, sent, meta_b, fw);
					if(ad)
						added=ad;
				}
				ent_loc_map = new HashMap<>();
				prev_span_id = span_id;
			}
			
			String ent_tup = cols[3].trim()+"::"+cols[4].trim(); //entity typeid and dictid
			ArrayList<String> locs = ent_loc_map.get(ent_tup);
			if(locs==null) {
				locs = new ArrayList<>();
				ent_loc_map.put(ent_tup, locs);
			}
			locs.add(cols[6].trim()+"-"+cols[7].trim()); //start and end location
			
		}
		
		//Remaining
		if(!ent_loc_map.isEmpty()) {
			String sent="";
			String[] locarr = prev_span_id.split("-");
			String span="";
			if(locarr[0].startsWith("T")) {
				sent = ti_sent_map.get(locarr[1]);
				span = meta_b.study;
			}	
			else if(locarr[0].startsWith("A")) {
				sent = ab_sent_map.get(locarr[1]);
				span =  meta_b.abs;
			}
			boolean ad = getTherapeutics(ent_loc_map, target_ent_types, span, sent, meta_b, fw);
			if(ad)
				added=ad;
		}
		
		if(added)
			abs_arts++;
		else {
			filtered_th_art_cnt++;
		}
	}

	private boolean getTherapeutics(HashMap<String, ArrayList<String>> ent_loc_map, String[] target_ent_types, String span, String sent,
			DTBean meta_b, FileWriter fw) throws IOException {
		// TODO Auto-generated method stub
		HashSet<String> tents = new HashSet<>(Arrays.asList(target_ent_types));
		HashMap<String, String> therapeutics = new HashMap<String,String>();
		//System.out.println(span);
		//System.out.println(sent);
		for(String ent : ent_loc_map.keySet()) {
			String[] ent_tup = ent.split("::");
			if(!tents.contains(ent_tup[1])) continue;
			ArrayList<String> locs = ent_loc_map.get(ent);
			
			//TODO : I can pick only one of these entity locations in the same sentence
			String larr[] = locs.get(0).split("-");
			String ent_id=ent_tup[0].trim();
			String th_term = span.substring(new Integer(larr[0]), new Integer(larr[1])+1);
			HashSet<String> filt_terms = FILTER_THERAPEUTICS.get(ent_id);
			if(!FILTER_THERAPEUTICS.containsKey(ent_id))
				therapeutics.put(ent_id, th_term);
			else {
				if(!filt_terms.isEmpty() && !filt_terms.contains(th_term.toUpperCase()))
					therapeutics.put(ent_id, th_term);
			}
		}
		
		if(!therapeutics.isEmpty()) {
			StringBuffer sb = new StringBuffer();
			for(String th : therapeutics.keySet()) {
				if(sb.length()>0)
					sb.append(",");
				sb.append(therapeutics.get(th) + " ["+th+"]");
			}
			meta_b.therapeutic=sb.toString();
			meta_b.outcomes=sent;

			writeDTBean(meta_b, fw);
			return true;
		}
		
		return false;
	}

	private void writeDTBean(DTBean meta_b, FileWriter fw) throws IOException {
		fw.write(meta_b.pdate+"\t"+meta_b.study+"\t"+meta_b.study_link+"\t"+meta_b.journal+"\t"+meta_b.therapeutic
				+"\t"+meta_b.sample+"\t"+meta_b.severity+"\t"+meta_b.outcomes+"\t"+meta_b.primary_endpoints+"\t"+meta_b.clinical_improv
				+"\t"+meta_b.study_design+"\t"+meta_b.added_on+"\t"+meta_b.cord_uid+"\n");
		
	}

	private HashMap<String, String> getsentenceMap(String s) {
		Text t = new Text();
		t.setText(s);
		HashMap<String, String> sent_map = new HashMap<>();
		ArrayList<Segment> sents = SentenceSplitter.splitIntoSentence(t);
		for(Segment seg : sents) {
			sent_map.put(seg.getIndex()+"",seg.getContent());
		}
		return sent_map;
	}
	
	private HashMap<String, DTBean> getArticleMetadata(HashSet<String> arts_added, String labels_file) throws IOException {
		HashMap<String,String> artid_labels = getStudyDesignLabels(labels_file);
		HashMap<String, DTBean> metadata_map = new HashMap<>();
		FileReader fr = new FileReader(CORD19_ARTS);
		BufferedReader br = new BufferedReader(fr);
		String line = "";
		while((line=br.readLine())!= null)
		{
			String[] cols = line.split("\t");
			String cord_uid = cols[0].trim();
			if(!arts_added.contains(cord_uid)) continue;
			String journal =  cols[6].trim(); 
			String added_on = "2020-27-05";
			String doi=cols[5].trim();
			String pdate=cols[4].trim();
			//System.out.println(line);
			
			DTBean m = new DTBean(pdate, cols[1].trim(), doi, journal, added_on); //TODO: added_on needs to be fixed 
			m.abs=cols[2].trim();
			
			//Add Study Design Labels
			String label = artid_labels.get(cord_uid);
			if(label!=null)
				m.study_design=label;
			
			metadata_map.put(cord_uid, m);
		}
		
		br.close();
		fr.close();
		return metadata_map;
	}
	
	private boolean checkStudyArticle(String text) {
		Matcher m = CSP.matcher(text);
		return m.find();
	}

	private boolean processSentence(Map<String, ArrayList<String>> art_span_map, String src_ent, String src_ent_type, FileWriter abs_fw, FileWriter ft_fw) throws IOException {
		boolean added=false;
		boolean src_art=false;
		for(String art_span : art_span_map.keySet()) {
			ArrayList<String> span_rows = art_span_map.get(art_span);
			boolean ab_ti = art_span.startsWith("AT-") ? true : false;
			boolean src_ent_flag =false;
			
			HashSet<String> span_ents = new HashSet<>();
			for(String span_row : span_rows) {
				String[] cols = span_row.split("\t");
				String pc_pair = cols[3].trim()+"::"+cols[4].trim();
				if(pc_pair.equals(src_ent+"::"+src_ent_type)) {
					src_ent_flag=true;
					src_art=true;
				}
				span_ents.add(pc_pair);
			}
			
			if(span_ents.size()<2) continue;
			
			if(ab_ti) {
				if(src_ent_flag) { //Source entity should be present in abstract or title
					added=true;
					for(String span_row : span_rows) {
						abs_fw.write(span_row+"\n");
					}
				}
			}else {
				if(src_art) {
					added=true;
					for(String span_row : span_rows) {
						ft_fw.write(span_row+"\n");
					}
				}
			}
		}
		
		return added;
	}
}
