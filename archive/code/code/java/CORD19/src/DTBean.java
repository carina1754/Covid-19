
public class DTBean {

	String pdate;
	String study;
	String study_link;
	String journal;
	String therapeutic;
	String study_design="-";
	String sample="-";
	String severity="-";
	String outcomes;
	String primary_endpoints="-";
	String clinical_improv="-";
	String added_on;
	
	// Reference field
	String abs = new String();
	String cord_uid = new String();
	
	public DTBean() {}
	
	public DTBean(String pdate, String study, String study_link, String journal, String added_on) {
		super();
		this.pdate = pdate;
		this.study = study;
		this.study_link = study_link;
		this.journal = journal;
		this.added_on = added_on;
	}
	
	public String getTherapeutic() {
		return therapeutic;
	}

	public void setTherapeutic(String therapeutic) {
		this.therapeutic = therapeutic;
	}

	public String getOutcomes() {
		return outcomes;
	}

	public void setOutcomes(String outcomes) {
		this.outcomes = outcomes;
	}

	public String getStudy_design() {
		return study_design;
	}

	public void setStudy_design(String study_design) {
		this.study_design = study_design;
	}

	public String getSample() {
		return sample;
	}

	public void setSample(String sample) {
		this.sample = sample;
	}

	public String getSeverity() {
		return severity;
	}

	public void setSeverity(String severity) {
		this.severity = severity;
	}

	public String getPrimary_endpoints() {
		return primary_endpoints;
	}

	public void setPrimary_endpoints(String primary_endpoints) {
		this.primary_endpoints = primary_endpoints;
	}

	public String getClinical_improv() {
		return clinical_improv;
	}

	public void setClinical_improv(String clinical_improv) {
		this.clinical_improv = clinical_improv;
	}
	
}
