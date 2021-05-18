/**
 *  First code review comments given by Naveen
 *  Verified by Naveen
 */

import java.io.IOException;
import java.text.BreakIterator;
import java.util.ArrayList;

/**
 * @author preethi
 * @description Splits the sentence. This class cannot be instantiated 
 * 
 */
public class SentenceSplitter {
	
	public SentenceSplitter(){
		
	}
	
	/**
	 * @param  a of class Text. 
	 * @return ArrayList of Sentences.
	 */
	public static ArrayList<Segment>  splitIntoSentence( Text a )
	{
		ArrayList <Segment>Sentences = new ArrayList<Segment>();
		BreakIterator boundary = BreakIterator.getSentenceInstance();
		boundary.setText(a.getText());
		int start = boundary.first();
		int index =1;
		for (int end = boundary.next();end != BreakIterator.DONE;start = end, end = boundary.next(),index++) 
		{
			//System.out.println("start="+start+" end="+end+" content="+a.getText().substring(start,end));
			Segment t = new Segment(start,end,a.getText().substring(start,end),index);
			Sentences.add(t);
		}
		return Sentences;
	}
	
	public static void main(String args[])
	{
		Text t = new Text();
		//t.setText("This is the first sentence which has words, letters, alphabets etc., and it ends here. Second sentence starts here.");
		//t.setText("1.1.7. In standard and in modified cationic environments, nerve functions vary in the ease with which they manifest changes characteristic of high [K(+)] or of high [Ca(++)]. 8. The after-potential functions are less completely controlled by the cationic environment than are the other functions investigated.");
		//t.setText("Composition of buffalo milk fat.");
		//t.setText("   Question/Comment:   i just started actos... and i also have seizures.... I noticed as soon as I started taking actos I got muscle cramps in my legs...could this be from an interaction?   49 year old                                                Female                                                    –                            Source: iGuard United States         Posted: 2010-07-05 01:21:53                                                                     iGuard                        Answer/Reply:   Actos can cause muscle pain.  Less than 1% of iGuard patients report muscle cramps with Actos.  In clinical trials, 5% of patients report muscle pain when taking Actos.  It is possible that the symptoms will decrease as you keep taking the medicine, but you should report your symptoms to your doctor to make sure it is not a more significant reaction.         Posted: 2010-07-12 15:36:57                                                                     Question/Comment:   I have been on Actos for several years now and I too have anemia and been on Procrit shots and taking Iron pills.  I would like to know as well if these systems are caused by taking Actos?44 year old Female- California   44 year old                                                Female                                                    –                            Source: iGuard United States         Posted: 2010-04-13 14:23:07                                                                     iGuard                        Answer/Reply:   Actos has been shown to cause anemia in about 1% of patients.  Talk with your doctor about the possible causes of your anemia and if the Actos could be contributing to the severity of your condition.  If your doctor thinks the Actos is causing it to be worse, discuss possible alternatives to the Actos for treating your diabetes.         Posted: 2010-05-01 12:12:42                                                                     Question/Comment:   I have been on Actos for several years. Gained 25 pounds. I became very anemic around the same time. Am having to take monthly Procrit shots and occasional iron IV&amp;#39;s. Now the doctor took me off of Actos because we have seen where one of the possible side affects can be anemia. I must wait three months and then have blood work done to see if this has made a difference in the anemia. Lost 26 pounds right away. Also take a diuretic, but have been on that for years.   76 year old                                                Female                                                    –                            Source: iGuard United States         Posted: 2010-03-23 16:02:08                                                                     Question/Comment:   My husband was recently diagnosed with Diabetes Type 2 and put on Metformin which debillitated him mentally and physically, he then was put on Actos 15mg and his readings went up, then put on 30mg and they went higher yet.  Now they want to try insulan?  He&amp;#39;s not overweight, he hasn&amp;#39;t excercised now because too tired from the Actos which is now causing mental difficulties...Help!   64 year old                                                Male                                                    –                            Source: iGuard United States         Posted: 2009-12-27 16:57:05                                                                     Question/Comment:   I have been taking actos for over a year. (45 mg) I can not tell if it is helping my blood sugars at all but it helps me with my colestoral level. Unlike others have mentioned I see no side effects of weight gain or increased appitite. i tried it with Junuvia for awhile but it didnot do anything.   61 year old                                                Male                                                    –                            Source: iGuard United States         Posted: 2009-12-07 16:20:07");
		//t.setText("Nitric oxide (NO) is essential to many physiological functions and operates in several signaling pathways. It is not understood how and when the different isoforms of nitric oxide synthase (NOS) the enzyme responsible for NO production evolved in metazoans.");
		t.setText("Neuronal expression of ATP-binding cassette, sub-family B (MDR/TAP), member 1 (ABCB1) has been demonstrated after brain ischemia. To investigate whether ABCB1 polymorphisms are associated with the development, risk factors (hypertension, dyslipidemia, and diabetes mellitus), severity (National Institutes of Health Stroke Scale, NIHSS), and sequelae (Modified Barthel Index, MBI) of ischemic stroke (IS), four single nucleotide polymorphisms (SNPs) of the ABCB1 gene [rs4148727, promoter, -154T>C; rs3213619, 5'-untranslation region (5'UTR), -129T>C); rs1128503, synonymous, Gly412 (C>T); rs3842, 3'UTR, A>G] were analyzed in 121 IS, p53 patients and 291 control subjects. SNPStats and SPSS 18.0 were used to obtain odds ratios (OR), 95% confidence intervals (CI), and p values. Multiple logistic regression models (codominant1, codominant2, dominant, recessive, and log-additive models) were applied to analyze the genetic data. The rs3842 SNP was weakly associated with the development of IS (p=0.020 in codominant1 model and p=0.028 in dominant model). In the analysis of clinical phenotypes, ABCB1 polymorphisms were nominally associated with hypertension (rs3213619 and rs3842, p<0.05), dyslipidemia (rs1128503, p<0.05), diabetes (rs3842, p<0.05), and NIHSS (rs4148727, p<0.05). Interestingly, rs3842 showed statistically strong association between IS with hypertension and IS without hypertension (Fisher's exact p=0.003, OR=0.11, 95% CI=0.03-0.51 in recessive model). These results suggest that the ABCB1 gene may be associated with the development and clinical phenotypes of IS in Korean population.");
		
		
		ArrayList<Segment> sentences = SentenceSplitter.splitIntoSentence(t);
		for(Segment sentence : sentences)
		{
			System.out.println("Start ="+sentence.getStartingPos());
			System.out.println("End ="+sentence.getEndingPos());
			System.out.println("Sentence Number ="+sentence.getIndex());
			System.out.println(sentence.getContent());
			System.out.println("---------------------------");
		}
	}
	
}