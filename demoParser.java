package stanfordParser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.Reader;
import java.io.StringReader;


import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;

import edu.stanford.nlp.trees.*;

import edu.stanford.nlp.parser.lexparser.LexicalizedParser;

public class demoParser {

	/**
	 * The main method demonstrates the easiest way to load a parser.
	 * Simply call loadModel and specify the path, which can either be a
	 * file or any resource in the classpath.  For example, this
	 * demonstrates loading from the models jar file, which you need to
	 * include in the classpath for ParserDemo to work.
	 */
	private static String[] connect = {"send", "share", "text", "give", "provide", "tell", "get"};
	private static String[] thingsToBeSent = {"contact","details","number","no","mobile","email","emailid","address"};
	private Pattern pattern;
	private Matcher matcher;
	private ArrayList<String> intentData;
	private String fullName;
	public static void main(String[] args) {
		LexicalizedParser lp = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
		String input = "Can you give the number and details of jesse pinkman";
		demoParser dp = new demoParser();
		dp.intentData = new ArrayList<String>();
		dp.demoAPI(lp, input);
	}

	/**
	 * demoAPI demonstrates other ways of calling the parser with
	 * already tokenized text, or in some cases, raw text that needs to
	 * be tokenized as a single sentence.  Output is handled with a
	 * TreePrint object.  Note that the options used when creating the
	 * TreePrint can determine what results to print out.  Once again,
	 * one can capture the output by passing a PrintWriter to
	 * TreePrint.printTree.
	 */
	public String getName(){
		return fullName;
	}
	
	public ArrayList<String> getData(){
		return intentData;
	}
	
	
	public void demoAPI(LexicalizedParser lp, String input) {
		System.out.println(java.lang.Runtime.getRuntime().maxMemory()); 
		Tree parse;
		
		String paragraph = input;
		Reader reader = new StringReader(paragraph);
		DocumentPreprocessor dp = new DocumentPreprocessor(reader);

		List<String> sentenceList = new LinkedList<String>();
		Iterator<List<HasWord>> it = dp.iterator();
		while (it.hasNext()) {
			StringBuilder sentenceSb = new StringBuilder();
			List<HasWord> sentence = it.next();
			sentenceSb.append(" ");
			for (HasWord token : sentence) {
				sentenceSb.append(token);
				if(sentenceSb.length()>1) {
					sentenceSb.append(" ");
				}

			}
			sentenceList.add(sentenceSb.toString());
		}

		for(String sentence:sentenceList) {
			TokenizerFactory<CoreLabel> tokenizerFactory = PTBTokenizer
					.factory(new CoreLabelTokenFactory(), "");
			List<CoreLabel> rawWords = tokenizerFactory.getTokenizer(
					new StringReader(sentence)).tokenize();
			//1018
			//			for (CoreLabel token: rawWords) {
			//				// this is the text of the token
			//				String word = token.get(TextAnnotation.class);
			//				// this is the POS tag of the token
			//				String pos = token.get(PartOfSpeechAnnotation.class);
			//				// this is the NER label of the token
			//				String ne = token.get(NamedEntityTagAnnotation.class);  
			//				//System.out.println(word + " " + pos + " " + token.ner());
			//			}

			parse = lp.apply(rawWords);
			TreebankLanguagePack tlp = new PennTreebankLanguagePack();
			GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
			GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
			//List<TypedDependency> tdl = gs.typedDependenciesCCprocessed();
			//System.out.println(tdl);
			//System.out.println();
			TreePrint tp = new TreePrint("typedDependenciesCollapsed");
			//tp.printTree(parse);
			Collection<TypedDependency> tdl = gs.typedDependenciesCCprocessed();
			int size = tdl.size();
			String[] relations = new String[size];
			String[] startWords = new String[size];
			String[] endWords = new String[size];

			String[] words = new String[rawWords.size()];
			String[] tags = new String[rawWords.size()];
			int index = 0;

			ArrayList<TaggedWord> ar=parse.taggedYield();
			for(TaggedWord tw: ar){
				words[index] = getWord(tw.toString());
				tags[index] = getTag(tw.toString());
				//System.out.print(getTag(tags[index]));
				index++;
			}
			//System.out.println();
			index = 0;
			//1018
			for (Iterator<TypedDependency> iter = tdl.iterator(); iter
					.hasNext();) {
				TypedDependency var = iter.next();

				TreeGraphNode dep = var.dep();
				TreeGraphNode gov = var.gov();

				String reln = var.reln().getShortName();
				relations[index] = reln;
				startWords[index] = dep.toString();
				endWords[index] = gov.toString();
				System.out.println(reln + ":" + dep.toString() + ","
						+ gov.toString());
				index++;
			}
			String trigger = "";
			ArrayList<String> information = new ArrayList<String>();
			ArrayList<String> tempInformation = new ArrayList<String>();
			ArrayList<String> completeData = new ArrayList<String>();
			String lastname = "";
			String name = "";
			String recipientLastname = "";
			String recipient = "";
			// find trigger words
			//System.out.println();
			for (index = 0; index < size; index++) {
				if (isTriggerRelation(relations[index])) {
					String data = "";
					if (givePermission(startWords[index])) {
						trigger = startWords[index];
						data = endWords[index];
						//System.out.println(endWords[index]);
					}
					if (givePermission(endWords[index])) {
						trigger = endWords[index];
						data = startWords[index];
						//System.out.println(startWords[index]);
					}
					pattern = Pattern
							.compile("^me.*", Pattern.CASE_INSENSITIVE);
					matcher = pattern.matcher(data);
					if (!matcher.find()) {
						if(isIntendedData(getRelevant(data))) information.add(data);
					}
				}
			}//1018
			ArrayList<String> triggerWordsDup = new ArrayList<String>();
			ArrayList<String> triggerNumbers = new ArrayList<String>();
			//find trigger - generic method
			for (index = 0; index < size; index++) {
				if (givePermission(startWords[index])) {
					triggerWordsDup.add(startWords[index]);
					//triggerNumbers.add(getIndex(startWords[index]));
				}
				if (givePermission(endWords[index])) {
					triggerWordsDup.add(endWords[index]);
					//triggerNumbers.add(getIndex(endWords[index]));
				}
			}


			ArrayList<String> triggerWords = new ArrayList<String>();
			for(String tr : triggerWordsDup){
				triggerWords.add(tr);
				for(index = 0; index < size; index++){
					if(isNeg(relations[index])){
						if (tr.equalsIgnoreCase(startWords[index])) {
							triggerWords.remove(tr);
						}
						else if (tr.equalsIgnoreCase(endWords[index])) {
							triggerWords.remove(tr);
						}

					}
				}
			}

			//find relations which have details data and name
			if(triggerWords.size() > 0){
				for (index = 0; index < size; index++) {
					if(isIntendedData(startWords[index])){
						if(isNNJJdup(tags[-1 + Integer.parseInt(getIndex(endWords[index]))])){
							if(!isIntendedData(endWords[index])){
								lastname = endWords[index];
							}
						}
					}
					if(isIntendedData(endWords[index])){
						if(isNNJJdup(tags[-1 + Integer.parseInt(getIndex(startWords[index]))])){
							if(!isIntendedData(startWords[index])){
								lastname = startWords[index];
							}
						}
					}
				}
				trigger="k";
			}
			else trigger="";
			//System.out.println(lastname);


			//System.out.println(trigger + ";");
			if (trigger != "") {
				//System.out.println("IF");
				//find other data user needed
				for (index = 0; index < size; index++) {
					if (canFindOtherData(relations[index])) {
						for (String data : information) {
							if (startWords[index].equalsIgnoreCase(data)) {
								if(isIntendedData(getRelevant(endWords[index]))) tempInformation.add(endWords[index]);
							}
							if (endWords[index].equalsIgnoreCase(data)) {
								if(isIntendedData(getRelevant(startWords[index]))) tempInformation.add(startWords[index]);
							}
						}
					}
				}

				for (String data : tempInformation) {
					information.add(data);
				}

				if (information.isEmpty()) {
					System.out.println("Empty");
					for (index = 0; index < size; index++) {
						if (isNameFinder(relations[index])) {
							if(isIntendedData(getRelevant(endWords[index]))) {
								information.add(endWords[index]);
								lastname = startWords[index];
							}	
						}
					}

					for (index = 0; index < size; index++) {
						if (canFindOtherData(relations[index])) {
							for (String data : information) {
								if (startWords[index].equalsIgnoreCase(data)) {
									if(isIntendedData(getRelevant(endWords[index]))) {
										tempInformation.add(endWords[index]);
									}
								}
								if (endWords[index].equalsIgnoreCase(data)) {
									if(isIntendedData(getRelevant(startWords[index]))) {
										tempInformation.add(startWords[index]);
									}
								}
							}
						}
					}

					for (String data : tempInformation) {
						information.add(data);
					}

				} else {
					for (index = 0; index < size; index++) {

						if (isNameFinder(relations[index])) {
							if (isInformationRelation(information,
									startWords[index])) {
								lastname = endWords[index];
							}
							if (isInformationRelation(information,
									endWords[index])) {
								lastname = startWords[index];
							}
						}
					}

				}

				// find full name
				for (index = 0; index < size; index++) {
					if (isNNJJ(relations[index])) {
						if (startWords[index].equalsIgnoreCase(lastname)) {
							name = getRelevant(endWords[index]) + "#" + name;
						}
						if (endWords[index].equalsIgnoreCase(lastname)) {
							name = getRelevant(startWords[index]) + "#" + name;
						}
					}

				}
				lastname = getRelevant(lastname);

				//			//find other data user needed
				//			for(index = 0; index < size; index++){
				//				if(canFindOtherData(relations[index])){
				//					for(String data: information){
				//						if(startWords[index].equalsIgnoreCase(data)){
				//							tempInformation.add(endWords[index]); 
				//						}
				//						if(endWords[index].equalsIgnoreCase(data)){
				//							tempInformation.add(startWords[index]);
				//						}
				//					}
				//				}
				//			}
				//
				//			for(String data : tempInformation){
				//				information.add(data);
				//			}
				//find complete info. about the information
				for (index = 0; index < size; index++) {
					if (canAugmentData(relations[index])) {
						for (String data : information) {
							if (startWords[index].equalsIgnoreCase(data)) {
								completeData.add(getRelevant(endWords[index])
										+ " " + getRelevant(data));
							} else if (endWords[index].equalsIgnoreCase(data)) {
								completeData.add(getRelevant(startWords[index])
										+ " " + getRelevant(data));
							} else {
								completeData.add(getRelevant(data));
							}
						}
					}
				}

				// Receipt of data finder
				for (index = 0; index < size; index++) {
					if (isPrep(relations[index])) {
						if (startWords[index].equalsIgnoreCase(trigger)) {
							recipientLastname = endWords[index];
						}
						if (endWords[index].equalsIgnoreCase(trigger)) {
							recipientLastname = startWords[index];
						}
					}
				}

				if (!recipientLastname.equals("")) {
					//System.out.println("recipient");
					name = "";
					lastname = "";
				} else {
					for (index = 0; index < size; index++) {
						if (isInfMod(relations[index])) {
							for (String data : information) {
								if (startWords[index].equalsIgnoreCase(data)) {
									recipientLastname = endWords[index];
								}
								if (endWords[index].equalsIgnoreCase(data)) {
									recipientLastname = startWords[index];
								}
							}
						}
					}
					for (index = 0; index < size; index++) {
						if (isAux(relations[index])) {
							if (startWords[index]
									.equalsIgnoreCase(recipientLastname)) {
								recipientLastname = endWords[index];
								name = "";
								lastname = "";
							}
							if (endWords[index]
									.equalsIgnoreCase(recipientLastname)) {
								recipientLastname = startWords[index];
								name = "";
								lastname = "";
							}
						}
					}
				}

			} else {
				//System.out.println("ELSE");

				boolean what = false;
				for (index = 0; index < size; index++) {
					if (isWhatRelation(relations[index])) {
						pattern = Pattern.compile("^what.*",
								Pattern.CASE_INSENSITIVE);
						matcher = pattern.matcher(startWords[index]);
						if (matcher.find()) {
							what = true;

						}
						matcher = pattern.matcher(endWords[index]);
						if (matcher.find()) {
							what = true;
						}
					}
				}
				//				triggerWords = new ArrayList<String>();
				//				triggerNumbers = new ArrayList<String>();
				//				//find trigger - generic method
				//				for (index = 0; index < size; index++) {
				//					if (givePermission(startWords[index])) {
				//						triggerWords.add(startWords[index]);
				//						//triggerNumbers.add(getIndex(startWords[index]));
				//					}
				//					if (givePermission(endWords[index])) {
				//						triggerWords.add(endWords[index]);
				//						//triggerNumbers.add(getIndex(endWords[index]));
				//					}
				//				}
				//				
				//				
				//				
				//				for(String tr : triggerWords){
				//					for(index = 0; index < size; index++){
				//						if(isNeg(relations[index])){
				//							if (tr.equalsIgnoreCase(startWords[index])) {
				//								triggerWords.remove(startWords[index]);
				//							}
				//							else if (tr.equalsIgnoreCase(endWords[index])) {
				//								triggerWords.remove(endWords[index]);
				//							}
				//						}
				//					}
				//				}
				//				
				//				//find relations which have details data and name
				//				if(triggerWords.size() > 0){
				//					for (index = 0; index < size; index++) {
				//						if(isIntendedData(startWords[index])){
				//							if(isNNJJdup(tags[-1 + Integer.parseInt(getIndex(endWords[index]))])){
				//								if(!isIntendedData(endWords[index])){
				//									lastname = endWords[index];
				//								}
				//							}
				//						}
				//						if(isIntendedData(endWords[index])){
				//							if(isNNJJdup(tags[-1 + Integer.parseInt(getIndex(startWords[index]))])){
				//								if(!isIntendedData(startWords[index])){
				//									lastname = startWords[index];
				//								}
				//							}
				//						}
				//					}
				//				}
				//				System.out.println(lastname);
				if (what) {
					//System.out.println("arrived");
					for (index = 0; index < size; index++) {
						if (isNameFinder(relations[index])) {
							information.add(endWords[index]);
							lastname = startWords[index];
						}
					}

					// find full name
					for (index = 0; index < size; index++) {
						if (isNNJJ(relations[index])) {
							if (startWords[index].equalsIgnoreCase(lastname)) {
								name = getRelevant(endWords[index]) + " "
										+ name;
							}
							if (endWords[index].equalsIgnoreCase(lastname)) {
								name = getRelevant(startWords[index]) + " "
										+ name;
							}
						}

					}
					lastname = getRelevant(lastname);
					//find other data user needed
					for (index = 0; index < size; index++) {
						if (canFindOtherData(relations[index])) {
							for (String data : information) {
								if (startWords[index].equalsIgnoreCase(data)) {
									tempInformation.add(endWords[index]);
								}
								if (endWords[index].equalsIgnoreCase(data)) {
									tempInformation.add(startWords[index]);
								}
							}
						}
					}

					for (String data : tempInformation) {
						information.add(data);
					}
					//find complete info. about the information
					for (index = 0; index < size; index++) {
						if (canAugmentData(relations[index])) {
							for (String data : information) {
								if (startWords[index].equalsIgnoreCase(data)) {
									completeData
									.add(getRelevant(endWords[index])
											+ " " + getRelevant(data));
								} else if (endWords[index].equalsIgnoreCase(data)) {
									completeData.add(getRelevant(startWords[index])
											+ " " + getRelevant(data));
								} else {
									completeData.add(getRelevant(data));
								}
							}
						}
					}

				}
			}
			Set<String> finalData = new TreeSet<String>();
			//System.out.println();
			name = name + "#" + lastname;
			//System.out.println("Name: " + name);
			//System.out.print("Information needed: ");
			for (String data : completeData) {
				finalData.add(data);
				//System.out.print(data + " ");
			}
			System.out.println();
			for (String data : information) {
				finalData.add(getRelevant(data));
				//System.out.print(data + " ");
			}
			finalData.remove("me");
			for (String data : finalData) {
				//System.out.print(data + " ");
			}
			//System.out.println();
			fullName = name;
			for (String data : finalData) {
				intentData.add(data);
			}
		}
	}

	private String getWord(String string){
		String []parts = string.split("/");
		return parts[0];
	}

	private String getTag(String string){
		String []parts = string.split("/");
		return parts[1];
	}

	private String getRelevant(String string){
		String []parts = string.split("-");
		return parts[0];
	}

	private String getIndex(String string){
		String []parts = string.split("-");
		return parts[1];
	}

	private boolean isNeg(String relation){
		if(relation.equalsIgnoreCase("neg")) return true;
		return false;
	}

	private boolean isAux(String relation){
		if(relation.equalsIgnoreCase("aux")) return true;
		return false;
	}

	private boolean isPrep(String relation){
		if(relation.equalsIgnoreCase("prep")) return true;
		return false;
	}

	private boolean isInfMod(String relation){
		if(relation.equalsIgnoreCase("infmod")) return true;
		return false;
	}

	private boolean isWhatRelation(String relation){
		if(relation.equalsIgnoreCase("attr")) return true;
		return false;
	}

	private boolean canFindOtherData(String relation){
		if(relation.equalsIgnoreCase("conj")) return true;
		if(relation.equalsIgnoreCase("conj_and")) return true;
		if(relation.equalsIgnoreCase("appos")) return true;
		if(relation.equalsIgnoreCase("partmod")) return true;
		return false;
	}

	private boolean canAugmentData(String relation){
		if(relation.equalsIgnoreCase("nn")) return true;
		if(relation.equalsIgnoreCase("amod")) return true;
		return false;
	}

	private boolean isInformationRelation(ArrayList<String> info, String checkWord){
		for(String data : info){
			if(data.equalsIgnoreCase(checkWord)) return true;
		}
		return false;
	}

	private boolean isNNJJdup(String relation){
		pattern = Pattern.compile("^(NN).*",Pattern.CASE_INSENSITIVE);
		matcher = pattern.matcher(relation);
		if(matcher.find()) return true;
		//		if(relation.equalsIgnoreCase("nn")) return true;
		//		if(relation.equalsIgnoreCase("jj")) return true;
		//		if(relation.equalsIgnoreCase("amod")) return true;
		return false;
	}

	private boolean isNNJJ(String relation){
		pattern = Pattern.compile("^(NN|JJ|amod).*",Pattern.CASE_INSENSITIVE);
		matcher = pattern.matcher(relation);
		if(matcher.find()) return true;
		//		if(relation.equalsIgnoreCase("nn")) return true;
		//		if(relation.equalsIgnoreCase("jj")) return true;
		//		if(relation.equalsIgnoreCase("amod")) return true;
		return false;
	}

	private boolean isNameFinder(String relation){
		if(relation.equalsIgnoreCase("poss")) return true;
		if(relation.equalsIgnoreCase("prep")) return true;
		return false;
	}

	private boolean isTriggerRelation(String relation){
		if(relation.equalsIgnoreCase("dobj")) return true;
		if(relation.equalsIgnoreCase("iobj")) return true;
		return false;
	}

	private boolean givePermission(String verb){
		for(int i = 0; i < connect.length; i++){
			pattern = Pattern.compile("^"+connect[i]+".*",Pattern.CASE_INSENSITIVE);
			matcher = pattern.matcher(verb);
			//System.out.println(verb + " " + connect[i]);
			if(matcher.find()) return true;
			//if(verb.equalsIgnoreCase(connect[i])) return true;
		}
		return false;
	}

	private boolean isIntendedData(String string){
		for(int i = 0; i < thingsToBeSent.length; i++){
			pattern = Pattern.compile("^"+thingsToBeSent[i]+".*",Pattern.CASE_INSENSITIVE);
			matcher = pattern.matcher(string);
			if(matcher.find()) return true;
		}
		return false;
	}
}
