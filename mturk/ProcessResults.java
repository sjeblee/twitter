/** ProcessResults - TWITTER VERSION
* Processes the results from the Categorize Arabic HIT on MTurk
* @author Serena Jeblee (sjeblee1@jhu.edu)
* Last modified on 27 July 2012
*
* Specify the number of sentences per HIT on line 25
* - Outputs the assignment ids to approve
*/

import java.io.*;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.StringTokenizer;
import com.csvreader.CsvReader;

public class ProcessResults{

public static void main(String[] args){

	if(args.length < 3){
		System.out.println("Usage: java ProcessResults [inputfile] [resultsfile] [outputfilename]");
		System.exit(1);
	}

	final int numsent = 12;	//number of sentences in each HIT

	//get filenames
	String filename = args[1];
	String inputname = args[0];
	String outname = args[2];
	String controlname = "arabic-twitter.controlmap";

	try{
		//setup
		CsvReader infile = new CsvReader(filename, '\t');
		FileWriter outfile = new FileWriter(outname);
		FileWriter rejectfile = new FileWriter("reject.txt");
		FileWriter approvefile = new FileWriter("approve.txt");
		FileWriter scratch = new FileWriter("scratch.txt");
		int count = 0;
		int tokcount = 0;
		int approvecount = 0;
		int rejectcount = 0;
		int numalreadyreviewed = 0;
		ArrayList<String> index = new ArrayList<String>();
		approvefile.write("assignmentIdToApprove\tassignmentIdToApproveComment");
		rejectfile.write("assignmentIdToReject\tassignmentIdToRejectComment");

		//get headers
		infile.readHeaders();

		//Read results from file
		while (infile.readRecord()){

		//TEST
		//infile.readRecord();

			//0. Field setup
			tokcount = 0;
			int num = 0; 		//the id number of the sentence group
			boolean complete = false; //has the HIT been worked on yet?
			boolean approve = false; //are we going to approve the results?
			String assignid = "";	//assignment id if it has been done
			String turkid = ""; 	//Turker id
			String city = "NA";
			String country = "NA";
			String years = "NA";
			boolean nativespeak = false;
			String[] levels = new String[numsent+1];	//level of dialect
			String[] classes = new String[numsent+1];	//type of dialect
			String[] langs = new String[numsent+1];
			for(int i=0; i<levels.length; i++){
				levels[i] = "N/A";
				classes[i] = "N/A";
				langs[i] = "Arabic";
			}

			//1. Pull fields 

			//group number
			num = Integer.parseInt(infile.get("annotation"));
			System.out.println("num: " + num);
	
			//assignment id
			assignid = infile.get("assignmentid");
			System.out.println("assignid: " + assignid);
			if(assignid.length()<3)
				complete = false;
			else
				complete = true;

			//review status
			String reviewstatus = infile.get("reviewstatus");
			System.out.println("reviewstatus: " + reviewstatus);
			if(!reviewstatus.contains("NotReviewed")){
				complete = false;
				numalreadyreviewed++;
			}
			
			//worker id
			turkid = infile.get("workerid");
			System.out.println("workerid: " + turkid);

			//worker city
			city = infile.get("Answer.WorkerCity");
			if(city.length() < 1)
				city = "NA";
			System.out.println("city: " + city);
							
			//worker country
			country = infile.get("Answer.WorkerCountry");
			System.out.println("country: " + country);

			//years speaking
			years = infile.get("Answer.years_speaking");
			if(years.length()<1)
				years = "NA";
			System.out.println("years: " + years);

			//native speaker?
			String nat = infile.get("Answer.native_arabic");
			System.out.println("native: " + nat);
			if(nat.equals("Yes"))
				nativespeak = true;

			//levels
			for(int l=1; l<=12; l++){
				levels[l] = infile.get("Answer.DLevel" + l);	
				System.out.println("levels[" + l + "]: " + levels[l]);
				if(levels[l].equals("junk"))
					langs[l] = "?";
			}

			//classes
			for(int c=1; c<=12; c++){
				classes[c] = infile.get("Answer.DClass" + c);
				System.out.println("classes[" + c + "]: " + classes[c]);
			}

			//contains other languages?
			String others = infile.get("Answer.otherlang");
			String [] otherlangs;
			if(others.contains("|"))
				otherlangs = others.split("|");
			else{
				otherlangs = new String[1];
				otherlangs[0] = others;
			}
			for(int z=0; z<otherlangs.length; z++)
				System.out.println("otherlangs[" + z + "] = " + otherlangs[z]);
			for(String snum : otherlangs){
				if((snum.length()>0) && (!snum.equals("|"))){
					int n = Integer.parseInt(snum);
					String lang = infile.get("Answer.otherlangid" + n);
					if(lang.length()<1)
						lang = "?";
					langs[n] += "," + lang;
					System.out.println("- Added lang: " + lang);
				}
			}

				
			//2. Get original input line
			if(complete){ 
				Scanner scaninput = new Scanner(new FileReader(inputname));
				//skip to desired input line
				for(int i=0; i<num; i++){
					if(scaninput.hasNextLine())
						scaninput.nextLine();
				}
				//get the line
				String inputline = scaninput.nextLine();
				scaninput.close();
				StringTokenizer tokin = new StringTokenizer(inputline, "\t");

					//make sure it's the right line
					int numtokin = tokin.countTokens();
					if(numtokin==0){
						System.out.println("Error: input line " + num + " empty.");
						System.exit(1);
					}
					String idstring = tokin.nextToken();
					System.out.println("num = " + num + " ; id = " + idstring);
					int id = Integer.parseInt(idstring);
					if(id != num){
						System.out.println("Error: line mismatch: results: " + num + ", input: " + id);
						System.exit(1);
					}

					//3. Check Turker info - see if turker id recorded already, check accuracy rate
					String rejectMessage = "";
					String approveMessage = "";
					Scanner turkerfile = null;
					FileWriter tempfile;
					boolean newturkfile = false;	//true if we need to create turkers.txt
					File f = new File("turkers.txt");
					if(f.isFile()) { //turkers.txt exists
						turkerfile = new Scanner(new FileReader("turkers.txt"));
						tempfile = new FileWriter("temp.txt");
					}
					else{	//it doesn't, create it
						System.out.println("NOTE: turkers.txt does not exist, creating it");
						tempfile = new FileWriter("turkers.txt");
						tempfile.write("TurkerID\tYearsSpeaking\tCity\tCountry\tNumHITs" 
								+ "\tNumApproved\tnumControls\tjustMSA\t" +
								"num_MSA_vs_dia_right\tnum_dia_controls\tnum_dia_right\n");
						newturkfile = true;
					}

					//search for the turker id
					boolean stop = false;
					boolean analyze = true;
					StringTokenizer turktok = null;
					String turkline;
					String turkerstats = turkid + "\t";
					int numhits = 0;
					int numappr = 0;
					int numcontrols = 0;
					int numjustmsa = 0;
					int nummsaright = 0;
					int numdia = 0;
					int numdiaright = 0;
					if((!newturkfile)&&(turkerfile != null)){
						while(turkerfile.hasNextLine() && !stop){
							turkline = turkerfile.nextLine();
							turktok = new StringTokenizer(turkline, "\t");
							if(turktok.hasMoreTokens()){
								String fileid = turktok.nextToken();
								if(fileid.equals(turkid))
									stop = true;
							}
							if(!stop)
								tempfile.write(turkline + "\n");	
						}//end while turkerfile

						if(stop){ //found turker, process stats
							String y = turktok.nextToken();
							if(years.equals("NA"))
								years = y;
							city = turktok.nextToken(); 
							country = turktok.nextToken(); 
							numhits = Integer.parseInt(turktok.nextToken());
							numappr = Integer.parseInt(turktok.nextToken());
							numcontrols = Integer.parseInt(turktok.nextToken());
							numjustmsa = Integer.parseInt(turktok.nextToken());
							nummsaright = Integer.parseInt(turktok.nextToken());
							numdia = Integer.parseInt(turktok.nextToken());
							numdiaright = Integer.parseInt(turktok.nextToken());
							double percent = ((double) (nummsaright + numdiaright))/((double) (numcontrols + numdia));
							if(numhits > 10){
								//analyze = false;
								if(percent < 50.0){
									approve = false;
									rejectMessage = "We're sorry, but your approval " + 											"rating for this task has fallen below 50%, therefore"
										+ " we cannot accept your work.";
								}
								else if(percent > 80.0){
									approve = true;
									approveMessage = "Thank you for doing this task! Your " +
										"work has been good consistently, and we encourage" +
										"you to do more HITs in this group. Thanks!";
								}
								else{
									analyze = true;
								}
							}//end if numhits>10
						}
					}//end if !newturkfile
				
					numhits++;

					if(analyze){ //4. Check control sentences for approval
						Scanner scancontrol = new Scanner(new FileReader(controlname));

						//Scanner scancontrol = new Scanner(new FileReader("controls-msa.txt")); //TEMP

						for(int i=0; i<num-1; i++){
							if(scancontrol.hasNextLine())
								scancontrol.nextLine();
								scancontrol.nextLine();
						}
						//get MSA control sentence
						String controlline = scancontrol.nextLine();
						System.out.println("controlline: " + controlline);
						StringTokenizer controltok = new StringTokenizer(controlline, "\t");
						int lineid = Integer.parseInt(controltok.nextToken());
						if(lineid != num){
							System.out.println("Error: line mismatch: results: " + num + ", controlmap: " + lineid);	
							System.exit(1);
						}
						int itemnum = Integer.parseInt(controltok.nextToken());
					
						String ann = controltok.nextToken();
						numcontrols++;

						//TEMP: Search for sentence
					/*	int itemnum = -1;
						String msatext = controltok.nextToken();
						msatext = msatext.replaceAll(" ","");
				
						int x = 1;
						while(tokin.hasMoreTokens()){
							String inputtext = tokin.nextToken();
							if(inputtext.equals("•")){
								x--;
								System.out.println("--- skipping bullet point");
							}
							if(msatext.equals(inputtext)){
								itemnum = x;
								System.out.println("- control msa matched to item " + itemnum);	
							}
						//	if(x==12)
						//		System.out.println("comparing: " + inputtext);
							x++;
						}
						if(itemnum==-1){
							System.out.println("-- ERROR: control msa not found!");
						}
						//end TEMP */

						if(levels[itemnum].contains("msa")){
							System.out.println("-- msa correctly identified");
							approve = true;
							nummsaright++;
							numjustmsa++;
						}
						else{
							approve = false;
							System.out.println("-- msa NOT correctly identified");
						}

						//get dialect control sentence

						//TEMP
					/*	scancontrol.close();
						scancontrol = new Scanner(new FileReader("controls-dia.txt"));
						for(int i=0; i<num-1; i++){
							if(scancontrol.hasNextLine())
								scancontrol.nextLine();
								//scancontrol.nextLine();
						}
						//end TEMP */

						controlline = scancontrol.nextLine();
						controltok = new StringTokenizer(controlline, "\t");
						lineid = Integer.parseInt(controltok.nextToken());
						if(lineid != num){
							System.out.println("Error: line mismatch: results: " + num + ", controlmap: " + lineid);	
							System.exit(1);
						}
						itemnum = Integer.parseInt(controltok.nextToken());
						
						ann = controltok.nextToken();
						numdia++;
						numcontrols++;

						//TEMP: Search for sentence
					/*	itemnum = -1;
						String diatext = controltok.nextToken();
						diatext = diatext.replaceAll(" ","");
		
						x = 1;
						tokin = new StringTokenizer(inputline, "\t");
						tokin.nextToken();	//throw away group num
						while(tokin.hasMoreTokens()){
							String inputtext = tokin.nextToken();
							if(inputtext.equals("•"))
								x--;
							if(diatext.equals(inputtext)){
								itemnum = x;
								System.out.println("- control dia matched to item " + itemnum);	
							}
							x++;
						}
						if(itemnum==-1){
							System.out.println("-- ERROR: control dia not found!");
						}
						//end TEMP */

						scancontrol.close();

						//did they say it was a dialect?
						String lev = levels[itemnum];
						if(lev.contains("little") || lev.contains("mixed") || lev.contains("most")){
							approve = true;
							nummsaright++;
							System.out.println("-- dia correctly identified");
						}
						else{
							System.out.println("-- dia NOT correctly identified");
							double permsaright = (double) nummsaright / (double) numcontrols;
							if((permsaright < .5) && !approve)	//if they normally get these right, we'll let it go
								approve = false;
						}

						//did they get the right dialect?
						String dialect = "msa";
						if(ann.contains("levantine-levantine") || ann.contains("3D__levantine"))
							dialect = "levantine";
						if(ann.contains("egyptian-egyptian") || ann.contains("3D__egyptian"))
							dialect = "egyptian";
						if(ann.contains("gulf-gulf") || ann.contains("3D__gulf"))
							dialect = "gulf";
						if(ann.contains("maghrebi-maghrebi") || ann.contains("3D__maghrebi"))
							dialect = "maghrebi";
						if(classes[12].contains(dialect)){
							numdiaright++;
							approve = true;
							System.out.println("-- dia correctly labeled");
						}
						//format:  lineid msanum ann / lineid dianum ann
	
					}//end if analyze	
	
					//5. Write results
					if(approve){
						//update turker stats
						//"TurkerID\tNumHITs\tNumApproved\tnumControls\t" +
								//"num_MSA_vs_dia_right\tnum_dia_controls\tnum_dia_right\n");
						numappr++;
						turkerstats += years + "\t" + city + "\t" + country + "\t" + numhits + "\t" + numappr + "\t" + numcontrols + "\t"
								+ numjustmsa + "\t" + nummsaright + "\t" + numdia + "\t" + numdiaright;
						tempfile.write(turkerstats);
		
						//note that we're going to approve these results
						approvefile.write("\n" + assignid + "\t" + approveMessage);
						approvecount++;
						tokcount = 1;
	
						//output annotated sentences
						while(tokin.hasMoreTokens() && (tokcount <= numsent-2)){
							String tweetid = tokin.nextToken();
							String sent = tokin.nextToken();
							outfile.write(levels[tokcount] + "-" + classes[tokcount] + "\t[" + langs[tokcount] + "]\t" 
								+ tweetid + "\t" + sent + "\n");
							tokcount++;
						}
					}
					else{	//note that we're going to reject these results
						rejectfile.write("\n" + assignid + "\t" + rejectMessage);
						rejectcount++;
						turkerstats += years + "\t" + city + "\t" + country + "\t" + numhits + "\t" + numappr + "\t" + numcontrols + "\t"
								+ numjustmsa + "\t" + nummsaright + "\t" + numdia + "\t" + numdiaright;
						tempfile.write(turkerstats);
					}

					if((!newturkfile)&&(turkerfile != null)){ //copy the rest of the turker file
						while(turkerfile.hasNextLine()){
							tempfile.write("\n" + turkerfile.nextLine());
						}
					}

					//write turkfile
					if(newturkfile)
						tempfile.close();
					else{
						if(turkerfile != null)
							turkerfile.close();
						tempfile.close();
						boolean deleted = f.delete(); //delete turkers.txt
						//swap turkers.txt and temp.txt
						File file = new File("temp.txt");
						File file2 = new File("turkers.txt");
						boolean success = file.renameTo(file2);
						if((!success)||(!deleted))
							System.out.println("ERROR: turkers and temp were NOT successfully swapped.");
					}

				}//end if complete

			//}//end else
			count++;

		}//end while infile.readRecord()

		//cleanup
		infile.close();
		outfile.close();
		approvefile.close();
		rejectfile.close();
		scratch.close();
		
		System.out.println("\t" + (count-1) + " results processed");
		System.out.println("\t" + numalreadyreviewed + " results previously reviewed");
		System.out.println("\t" + approvecount + " assignments to approve");
		System.out.println("\t" + rejectcount + " assignments to reject");
			
	}
	catch(IOException e){
		System.out.println("IOException: " + e.getMessage());
	}

}//end main

}
