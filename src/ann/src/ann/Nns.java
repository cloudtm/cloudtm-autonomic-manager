/*
 * CINI, Consorzio Interuniversitario Nazionale per l'Informatica
 * Copyright 2013 CINI and/or its affiliates and other
 * contributors as indicated by the @author tags. All rights reserved.
 * See the copyright.txt in the distribution for a full listing of
 * individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package ann;

import org.joone.engine.*;
import org.joone.engine.learning.*;
import org.joone.io.*;
import org.joone.net.NeuralNet;
import org.joone.net.NeuralNetLoader;

import java.io.*;
import java.util.*;

/*
* @Author Diego Rughetti
*/
public class Nns implements NeuralNetListener{
		
	private static String trainingData = "training.txt";
	private static String erroreTrainingFile = "erroreTraining.txt";
	private static String erroreValidationFile = "erroreValidation.txt";
	private static int ITERATION = 5000;
	private static int GRANULARITA = 5000;
	
	private static int HIDDEN = 8;
	
	private int samplesNumber;
	
	private double clientNormalizzation;
	private double serverNormalizzation;
	private double replicationNormalizzation;
	private double throughputNormalizzation;
	private double responseNormalizzation;
	
	private NeuralNet thNN;
	private NeuralNet rtNN;
	
	private MemoryInputSynapse thInputSynapse;
	private MemoryOutputSynapse thOutputSynapse;
	
	private MemoryInputSynapse rtInputSynapse;
	private MemoryOutputSynapse rtOutputSynapse;
	
	private Monitor thMonitor;
	private Monitor rtMonitor;
	
	
	public Nns(double a, double b, double c, double d, double e){
		this.clientNormalizzation = a;
		this.serverNormalizzation = b;
		this.replicationNormalizzation = c;
		this.throughputNormalizzation = d;
		this.responseNormalizzation = e;
	}
	
	public void trainNetworks(){
		this.Go();
	}
	
	public void Go(){
		trainNetwork("Throughput", 4);
		trainNetwork("Response", 5);
	}
	
	private void trainNetwork(String nf, int column){
		NeuralNet nnet;
				
		LinearLayer input = new LinearLayer();
		SigmoidLayer hidden = new SigmoidLayer();
		SigmoidLayer output = new SigmoidLayer();
		    
		    
		input.setLayerName("input");
		hidden.setLayerName("hidden");
		output.setLayerName("output");
		    
		    
		input.setRows(3);
		hidden.setRows(HIDDEN);
		output.setRows(1);
		        
		    /*
		     * creo le connessioni tra i tre layer
		     */
		    
		FullSynapse synapse_IH = new FullSynapse(); /* input -> hidden conn. */
		FullSynapse synapse_HO = new FullSynapse(); /* hidden -> output conn. */
		        
		synapse_IH.setName("Input-Hidden");
		synapse_HO.setName("Hidden-Output");
		    
		    /*
		         * connetto input con hidden
		         */
		    
		input.addOutputSynapse(synapse_IH);
		hidden.addInputSynapse(synapse_IH);
		    
		    /*
		         * connetto l'hidden con l'output
		         */
		    
		hidden.addOutputSynapse(synapse_HO);
		output.addInputSynapse(synapse_HO);
		        
				// creo gli oggetti che gestiscono il file di input
				
		FileInputSynapse inputTraining = new FileInputSynapse();
		FileInputSynapse inputValidation = new FileInputSynapse();
		FileInputSynapse outputTraining = new FileInputSynapse();
		FileInputSynapse outputValidation = new FileInputSynapse();
		    
		   
		    
		inputTraining.setInputFile(new File(trainingData));
		inputTraining.setAdvancedColumnSelector("1-3");
		inputTraining.setFirstRow(1);
		inputTraining.setLastRow(samplesNumber/3);
		inputTraining.setBuffered(true);
		inputTraining.setName("inputTraining");
		    
		inputValidation.setInputFile(new File(trainingData));
		inputValidation.setAdvancedColumnSelector("1-3");
		inputValidation.setFirstRow((samplesNumber/3)+1);
		inputValidation.setLastRow(samplesNumber);
		inputValidation.setBuffered(true);
		inputValidation.setName("inputValidation");
		    
		outputTraining.setInputFile(new File(trainingData));
		outputTraining.setAdvancedColumnSelector(column + "-" + column);
		outputTraining.setFirstRow(1);
		outputTraining.setLastRow(samplesNumber/3);
		outputTraining.setBuffered(true);
		outputTraining.setName("outputTraining");
		    
		outputValidation.setInputFile(new File(trainingData));
		outputValidation.setAdvancedColumnSelector(column + "-" + column);
		outputValidation.setFirstRow((samplesNumber/3)+1);
		outputValidation.setLastRow(samplesNumber);
		outputValidation.setBuffered(true);
		outputValidation.setName("outputValidation");
		    
		    
		    // creo l'oggetto che gestisce lo switch tra il training e la validazione lato input
		    
		InputSwitchSynapse lswInput = new InputSwitchSynapse();
		    
		lswInput.addInputSynapse(inputTraining);
		lswInput.addInputSynapse(inputValidation);
		lswInput.setActiveInput("inputTraining");
				
			// creo l'oggetto che gestisce lo switch tra il training e la validazione lato output
				
		InputSwitchSynapse lswOutput = new InputSwitchSynapse();
		    
		lswOutput.addInputSynapse(outputTraining);
		lswOutput.addInputSynapse(outputValidation);
		lswOutput.setActiveInput("outputTraining");
		    
				
			// collego lo switch di input all'input
				
		input.addInputSynapse(lswInput);
		        
		    // creo l'oggetto che gestisce il training
				
		TeachingSynapse trainer = new TeachingSynapse();
		        
		    // collego lo switch di output all'oggetto che fa training
				
		trainer.setDesired(lswOutput);
		        
		        /* Creates the error output file */
    	FileOutputSynapse error = new FileOutputSynapse();
	   	error.setFileName(erroreTrainingFile);
		        //error.setBuffered(false);
	   	trainer.addResultSynapse(error);
		        
		        /* Connects the Teacher to the last layer of the net */
		output.addOutputSynapse(trainer);
		nnet = new NeuralNet();
		
		nnet.addLayer(input, NeuralNet.INPUT_LAYER);
		nnet.addLayer(hidden, NeuralNet.HIDDEN_LAYER);
		nnet.addLayer(output, NeuralNet.OUTPUT_LAYER);
		nnet.setTeacher(trainer);
		
		// Gets the Monitor object and set the learning parameters
		Monitor monitor = nnet.getMonitor();
	    monitor.setLearningRate(0.3);
	    monitor.setMomentum(0.1);
		        
		        /* The application registers itself as monitor's listener
		         * so it can receive the notifications of termination from
		         * the net.
		         */
			    
	    monitor.addNeuralNetListener(this);
	    monitor.setUseRMSE(true);    
	    monitor.setTrainingPatterns(samplesNumber/3); /* # of rows (patterns) contained in the input file */
	    monitor.setTotCicles(ITERATION); /* How many times the net must be trained on the input patterns */
	    monitor.setLearning(true); /* The net must be trained */
	    nnet.go(true); /* The net starts the training job */
			
			    // cambio gli input
			    
	    lswInput.setActiveInput("inputValidation");
	    lswOutput.setActiveInput("outputValidation");
		    
			    // cambio il file di output
			    
	    error.setFileName(erroreValidationFile);
			        //error.setBuffered(false);
			    
	    trainer.addResultSynapse(error);
			    
	    monitor.setLearning(false);
	    monitor.setValidation(true);
	    monitor.setTotCicles(1);
	    monitor.setValidationPatterns(samplesNumber/3);
	    nnet.go(true);
			    
		try{
			FileOutputStream stream = new FileOutputStream(nf + ".snet");
			ObjectOutputStream outputStream = new ObjectOutputStream(stream);
			outputStream.writeObject(nnet); 
			outputStream.close();
		}catch (Exception e){
			e.printStackTrace();
		}
	}	
	
	public void loadNetworks(){
		load("Throughput");
		load("Response");
	}
	
	private void load(String nnType){
		if(nnType.equals("Throughput")){

			NeuralNetLoader netLoader=new NeuralNetLoader(nnType + ".snet");
			thNN = netLoader.getNeuralNet();
			
			thNN.getInputLayer().removeAllInputs();
			thNN.getOutputLayer().removeAllOutputs();
			
			
			thInputSynapse=new MemoryInputSynapse();
			thInputSynapse.setFirstRow(1);
			thInputSynapse.setAdvancedColumnSelector("1,2,3");
			thOutputSynapse = new MemoryOutputSynapse();
			
			thNN.getInputLayer().addInputSynapse(thInputSynapse);
			thNN.getOutputLayer().addOutputSynapse(thOutputSynapse);
			
			thMonitor = thNN.getMonitor();
			thMonitor.addNeuralNetListener(this);
			thMonitor.setLearning(false);
			thMonitor.setTotCicles(1);
			thMonitor.setTrainingPatterns(1);
		
		}else if(nnType.equals("Response")){
			
			NeuralNetLoader netLoader=new NeuralNetLoader(nnType + ".snet");
			rtNN = netLoader.getNeuralNet();
			
			rtNN.getInputLayer().removeAllInputs();
			rtNN.getOutputLayer().removeAllOutputs();
			
			
			rtInputSynapse=new MemoryInputSynapse();
			rtInputSynapse.setFirstRow(1);
			rtInputSynapse.setAdvancedColumnSelector("1,2,3");
			rtOutputSynapse = new MemoryOutputSynapse();
			
			rtNN.getInputLayer().addInputSynapse(rtInputSynapse);
			rtNN.getOutputLayer().addOutputSynapse(rtOutputSynapse);
			
			rtMonitor = rtNN.getMonitor();
			rtMonitor.addNeuralNetListener(this);
			rtMonitor.setLearning(false);
			rtMonitor.setTotCicles(1);
			rtMonitor.setTrainingPatterns(1);
		}
	}
	
	public OptimalPrevision getOptimalPrevision(int minRepl, int maxServer, int numeroClient){
		
		OptimalPrevision op = new OptimalPrevision();
		
		double thMax = -1;
		double thTemp = -1;
		double rsMin = 0;
		double rsTemp = 0;
		for(int server=minRepl;server<(maxServer+1);server++){
			for(int replicazione=minRepl; replicazione<(server+1); replicazione++){
				System.out.println("Server = " + server);
				System.out.println("Replicazione = " + replicazione);
				
				thTemp = getPrevisionThroughput(numeroClient, server, replicazione);
				if(thTemp > thMax){
					op.setReplicationThroughput(replicazione);
					op.setServerThroughput(server);
					thMax = thTemp;
				}
				
				rsTemp = getPrevisionResponseTime(numeroClient, server, replicazione);
				if(rsMin == 0 || rsTemp < rsMin){
					op.setReplicationResponseTime(replicazione);
					op.setServerResponseTime(server);
					rsMin = rsTemp;
				}
			}
		}
		System.out.println("End getOptimalPrevision"+op.getServerThroughput());
		return op;
		
	}
	
	private double getPrevisionThroughput(int client, int server, int replicazione){
		
		double cli = (((double) client)/clientNormalizzation);
		double ser = (((double) server)/serverNormalizzation);
		double rep = (((double) replicazione)/replicationNormalizzation);
		double[][] inputArray = {{cli, ser, rep}};
		
		thInputSynapse.setInputArray(inputArray);
		thNN.go();
		double[] pattern = thOutputSynapse.getNextPattern();
		
		return pattern[0];					
	}

	private double getPrevisionResponseTime(int client, int server, int replicazione){
		
		double cli = (((double) client)/clientNormalizzation);
		double ser = (((double) server)/serverNormalizzation);
		double rep = (((double) replicazione)/replicationNormalizzation);
		double[][] inputArray = {{cli, ser, rep}};
		rtInputSynapse.setInputArray(inputArray);
		rtNN.go();
		double[] pattern = rtOutputSynapse.getNextPattern();
		return pattern[0];					
	}

	public void parseInputFile(String path){
		samplesNumber = 0;
		BufferedReader br;
		FileInputStream fis;
		InputStreamReader isr;
		FileOutputStream fos;
		PrintStream ps;
		StringTokenizer st, stInternal;
		String stringa;
		String [] linea = new String[5]; 
		File directory = new File(path);
		if(directory.exists() && directory.isDirectory()){
			try{
				File output = new File(trainingData);
				fos = new FileOutputStream(output);
				ps = new PrintStream(fos);
				String lista[] = directory.list();
				Vector <String> samples = new Vector<String>();
				for (String fileName : lista){
					if(fileName.contains(".log")){
						fileName = path + "/"+ fileName;
						System.out.println("Processing: " + fileName);
						File f = new File(fileName);
						fis = new FileInputStream(f);
						isr = new InputStreamReader(fis);
						br = new BufferedReader(isr);
						String line = br.readLine();
						while(line!=null){
							if (line.contains("JMX[")){
								st = new StringTokenizer(line, ",");
								stringa = st.nextToken();
								while(stringa != null){
									if(stringa.contains("LocalActiveTransaction")){
										System.out.println(stringa);
										stInternal = new StringTokenizer(stringa, " ");
										stInternal.nextToken();
										stInternal.nextToken();
										stInternal.nextToken();
										linea[0] =  stInternal.nextToken();
									}
									if(stringa.contains("NumNodes")){
										//System.out.println(stringa);
										stInternal = new StringTokenizer(stringa, " ");
										stInternal.nextToken();
										if(stInternal.nextToken().equals("NumNodes:")){
											stInternal.nextToken();
											linea[1] = stInternal.nextToken();
											System.out.println(stringa);
										}
									}
									if(stringa.contains("ReplicationDegree")){
										System.out.println(stringa);
										stInternal = new StringTokenizer(stringa, " ");
										stInternal.nextToken();
										stInternal.nextToken();
										stInternal.nextToken();
										linea[2] = stInternal.nextToken();
									}
									if(stringa.contains("Throughput")){
										System.out.println(stringa);
										stInternal = new StringTokenizer(stringa, " ");
										stInternal.nextToken();
										stInternal.nextToken();
										stInternal.nextToken();
										linea[3] = stInternal.nextToken();
									}
						
									if(stringa.contains("AvgResponseTime")){
										System.out.println(stringa);
										stInternal = new StringTokenizer(stringa, " ");
										stInternal.nextToken();
										stInternal.nextToken();
										stInternal.nextToken();
										linea[4] = stInternal.nextToken();
									}
									if(st.hasMoreTokens()){
										stringa=st.nextToken();
									}else{
										stringa=null;
									}
								}
								if(Double.valueOf(linea[0]) != 0){
									double client = Double.valueOf(linea[0]).doubleValue();
									double server = Double.valueOf(linea[1]).doubleValue();
									client = client*server;
									linea[0] =  String.valueOf(client/clientNormalizzation);
									linea[1] = String.valueOf(server/serverNormalizzation);
									linea[2] = String.valueOf(Double.valueOf(linea[2]).doubleValue()/replicationNormalizzation);;
									linea[3] = String.valueOf(Double.valueOf(linea[3]).doubleValue()/throughputNormalizzation);;
									linea[4] = String.valueOf(Double.valueOf(linea[4]).doubleValue()/responseNormalizzation);;
									samples.add(linea[0] + " " + linea[1] + " " + linea[2] + " " + linea[3] + " " + linea[4]);
									samplesNumber++;
									
								}
							}
							line = br.readLine();
						}
						br.close();
						isr.close();
						fis.close();
					}
				}
				int elem;
				while(samples.size()!=0){
					elem = (int) (Math.random()*samples.size());
					ps.println(samples.elementAt(elem));
					samples.removeElementAt(elem);
				}
				ps.close();
				fos.close();
			}catch(Exception e){
						e.printStackTrace();
			}
		}
		
	}
	
	public int getSampleFromInputFile(String fileName){
		BufferedReader br;
		FileInputStream fis;
		InputStreamReader isr;
		StringTokenizer st, stInternal;
		String stringa;
		String activeThread =""; 
		String numNodes = "";
		
		System.out.println("Processing: " + fileName);
		File f = new File(fileName);
		try{
			fis = new FileInputStream(f);
			isr = new InputStreamReader(fis);
			br = new BufferedReader(isr);
			String line = br.readLine();
			while(line!=null){
				if (line.contains("JMX[")){
					st = new StringTokenizer(line, ",");
					stringa = st.nextToken();
					while(stringa != null){
						if(stringa.contains("LocalActiveTransaction")){
							System.out.println(stringa);
							stInternal = new StringTokenizer(stringa, " ");
							stInternal.nextToken();
							stInternal.nextToken();
							stInternal.nextToken();
							activeThread =  stInternal.nextToken();
						}
						if(stringa.contains("NumNodes")){
							stInternal = new StringTokenizer(stringa, " ");
							stInternal.nextToken();
							if(stInternal.nextToken().equals("NumNodes:")){
								stInternal.nextToken();
								numNodes = stInternal.nextToken();
								System.out.println(stringa);
							}
						}
						if(st.hasMoreTokens()){
							stringa=st.nextToken();
						}else{
							stringa=null;
						}
					}
				}
				line = br.readLine();
			}
			br.close();
			isr.close();
			fis.close();
		}catch (Exception e){
			e.printStackTrace();
		}
		
		return (int) (Double.valueOf(activeThread).doubleValue()*Double.valueOf(numNodes).doubleValue());

	}
	
	public static void main (String [] arg){
		Nns nn = new Nns(33,5,5,2000,17000);
    	nn.parseInputFile("/Users/twistbuster/Desktop/testCTM/all");
	}
	
	public void netStopped(NeuralNetEvent e) {
        System.out.println("Training finished");
    }
    
	public void cicleTerminated(NeuralNetEvent e) {
    }
    
    public void netStarted(NeuralNetEvent e) {
        System.out.println("Training...");
    }
    
    public void errorChanged(NeuralNetEvent e) {
        Monitor mon = (Monitor)e.getSource();
        /* We want print the results every 200 cycles */
        if (mon.getCurrentCicle() % 200 == 0)
            System.out.println(mon.getCurrentCicle() + " epochs remaining - RMSE = " + mon.getGlobalError());
    }
    
    public void netStoppedError(NeuralNetEvent e,String error) {
    
    }
}
