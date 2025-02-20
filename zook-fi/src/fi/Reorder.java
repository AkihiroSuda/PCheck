/**
 * Copyright (c) 2012,
 * Pallavi Joshi  <pallavi@cs.berkeley.edu>
 * All rights reserved.
 * <p/>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * <p/>
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * <p/>
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * <p/>
 * 3. The names of the contributors may not be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 * <p/>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */


package org.fi;

import java.util.*;
import java.io.*;

public class Reorder extends Thread{
	public static LinkedList<Pair<Event, Object>> events;
	public static LinkedList<Pair<Event, Object>> readEvts;
	public static int nMsgsRecd;
	
	public static List<Integer> evtOrder;
	public static List<Event.EType> eTypeOrder;
	public static List<String> eNodeOrder;
	public static Map<String, List<Integer>> nodeToEvtOrder;

	public static List<String> evtsWoken;
	public static List<String> evtsWokenWONode;
	public static List<String> absStates;
	public static List<String> absQStates;
	public static int nEvtBeingWokenUp;

	public static long seed = 23418;
	public static Random randomGen = new Random(seed);

	public synchronized static void initBeforeEachExp(){
		Util.MESSAGE("Initialized before an experiment started");

		FMServer.removeWaitStates();
		FMServer.clearQueueToNumElems();
		//AState.clearState();

		resetNormalExec();

		events = new LinkedList<Pair<Event, Object>>();
		readEvts = new LinkedList<Pair<Event, Object>>();
		resetNMsgsRecd();

		evtOrder = new LinkedList<Integer>();
		eTypeOrder = new LinkedList<Event.EType>();
		eNodeOrder = new LinkedList<String>();

		nodeToEvtOrder = new HashMap<String, List<Integer>>();
		evtsWoken = new LinkedList<String>();
		evtsWokenWONode = new LinkedList<String>();
		absStates = new LinkedList<String>();
		absQStates = new LinkedList<String>();
		nEvtBeingWokenUp = 0;

		History.readHistory();
		History.readPfx();
		Search.initBeforeEachExp();
	}


	public static void addEvent(Event e, Object waitObj){
		int sz = events.size();
		int idx = sz;

		Pair ep = new Pair(e, waitObj);

		events.add(idx, ep);
		
		if(e.isReadEvent()){
			nMsgsRecd++;
			readEvts.add(ep);
		}
	}


	public static void addAndWait(Event e, Object waitObj){
		synchronized(Reorder.class){
			if(isExpStart()){
				initBeforeEachExp();
				resetExpStart();
			}
		
			if(isNormalExec()){
				return;
			}

			addEvent(e, waitObj);
		}
		
		synchronized(waitObj){
			try{
				Util.MESSAGE("Making a thread wait here");
				waitObj.wait();
			}
			catch(Exception exp){
				Util.EXCEPTION("Reorder.addAndWait", exp);
			}
		}
	}

	public void run(){
		while(true){
		    try{
			Util.MESSAGE("In Reorder thread");
		
			if(isElectionOver()){
				wakeUpAll();
				continue;
			}

			if(!FMServer.allWaiting()) {Util.sleep(200); continue;}
			

			List<Pair<Event, Object>> evtsOld;
			synchronized(Reorder.class){
				evtsOld = new LinkedList<Pair<Event, Object>>(events);
			}
			
			Util.sleep(200);

			boolean noNewEvt;
			boolean readMsgRecd;
			boolean noReadEvts;
			int nEvts = 0;
			synchronized(Reorder.class){
				noNewEvt = events.equals(evtsOld);
				readMsgRecd = (nMsgsRecd > 0);
				noReadEvts = ((readEvts == null) || (readEvts.size() == 0));
				if(events != null) nEvts = events.size();
			}


			if(!noNewEvt || (nEvts == 0)) {
				continue;
			}


			Util.MESSAGE("All servers waiting. Going to see if I can wake a thread up. Size of events is " 
					+ nEvts);

			if(readMsgRecd && noReadEvts && FMServer.areQueuesEmpty()){
				Util.MESSAGE("Going to wake up all servers");
				FMServer.removeWaitStates();
				resetNMsgsRecd();
				AState.clearState();
				setNormalExec();
				finishElection();
				wakeUpAll();
				continue;
			}
				

			Pair<Event, String> eWSt = Search.wakeUpBreadthFirst();
			//Pair<Event, String> eWSt = Search.wakeUpDet();
			//Pair<Event, String> eWSt = Search.wakeUpRandom();
			//Pair<Event, String> eWSt = Search.wakeUpPrioritizedNode();
			
			if(eWSt != null){
				Event e = eWSt.fst();
				String state = eWSt.snd();

				evtOrder.add(e.hashCode());
				//Util.MESSAGE("Adding to evtOrder: " + e.hashCode());
				eTypeOrder.add(e.t);
				eNodeOrder.add(e.getNodeId());
				
				String nodeId = e.getNodeId();
				List<Integer> evts = nodeToEvtOrder.get(nodeId);
				if(evts == null){
					evts = new LinkedList<Integer>();
					nodeToEvtOrder.put(nodeId, evts);	
				}
				evts.add(e.hashCodeWONodeStack());

				Search.setLastNode(nodeId);
			}
			else{
				Util.MESSAGE("Event was null");
				evtOrder.add(0);
			}
		   }
	           catch(Exception e){
			Util.EXCEPTION("Reorder.run", e);
		   }	   
		}
	}

        
	public static void letEvtGo(Pair<Event, Object> eWObj, boolean record){
		if(eWObj == null) return;

		Event e = eWObj.fst();
		Object objToNotf = eWObj.snd();
		
		if((e != null) && (objToNotf != null)){
			synchronized(Reorder.class){
				events.remove(eWObj);
				if(e.isReadEvent()){
					readEvts.remove(eWObj);
				}

				if(record){
					//if(e.isPollEvent()){
					//	boolean b = FMServer.areQueuesEmpty();
					//	Util.MESSAGE("Going to record the event " + e);
					//}
					History.recordEvtWoken(events, e, objToNotf, evtsWoken, evtsWokenWONode, absStates, absQStates);	
					//History.recordCoverage(e);
				}
			}
			
			synchronized(objToNotf){objToNotf.notify();}		
			
			String nodeId = e.getNodeId();
			int sid = Util.getIdValFromNodeId(nodeId);
			FMServer.serverNoLongerWaiting(sid);
		}
		else{
			Util.ERROR("The object on which the thread is waiting to block an event is null");
		}

	}


	public static void wakeUpAll(){
		List<Pair<Event, Object>> evts = null;
		synchronized(Reorder.class){
			if(events.isEmpty())
				return;
			evts = new LinkedList<Pair<Event, Object>>(events);
		}
		for(Pair<Event, Object> e : evts){
			letEvtGo(e, false);
		}
	}


	public static void endElection(){
		File f = new File(FMLogic.EXP_RUN_FLAG);
		if(f.exists()){
			f.delete();
			Util.MESSAGE("Going to write to history");
			History.writeToHistory(evtOrder);
			History.writePfx();
			Search.recordPfxDepInfo();
		}
	}


	public static void wipeOut(){
		File f = new File(FMLogic.EXP_RUN_FLAG);
		if(f.exists()){
			Util.MESSAGE("Wiping out");
			setNormalExec();
			createWipeOutFlag();
			FMServer.removeWaitStates();
			resetNMsgsRecd();
			AState.clearState();
			wakeUpAll();
			f.delete();
		}
	}
	
	public static void createWipeOutFlag(){
		File f = new File(FMLogic.WIPE_OUT_FLAG);
		if(!f.exists()){
			try{
				f.createNewFile();
			}
			catch(Exception e){
				Util.ERROR("Exception thrown while creating the wipe out flag file");
			}
		}
	}

	public static synchronized boolean isNormalExec(){
		return FMLogic.NORMAL_EXEC;
	}

	public static synchronized void setNormalExec(){
		FMLogic.NORMAL_EXEC = true;
	}
	
	public static synchronized void resetNormalExec(){
		FMLogic.NORMAL_EXEC = false;
	}

	public static synchronized void resetNMsgsRecd(){
		nMsgsRecd = 0;
	}

	public static void finishElection(){
		Util.createNewFile(FMLogic.ELECTION_OVER);
	}

	public static boolean isElectionOver(){
		return (new File(FMLogic.ELECTION_OVER)).exists();
	}

	public static synchronized int incEvtBeingWokenUp(){
		nEvtBeingWokenUp++;
		return nEvtBeingWokenUp;
	}

	public static synchronized List<Pair<Event, Object>> getEvents(){
		return events;
	}
	
	public static synchronized Map<String, List<Integer>> getNodeToEvtOrder(){
		return nodeToEvtOrder;
	}
	
	public static synchronized List<Integer> getEvtOrder(){
		return evtOrder;
	}
	
	public static synchronized List<Event.EType> getETypeOrder(){
		return eTypeOrder;
	}
	
	public static synchronized List<String> getENodeOrder(){
		return eNodeOrder;
	}

	public static synchronized List<String> getAbsStates(){
		return absStates;
	}
	
	public static synchronized List<String> getAbsQStates(){
		return absQStates;
	}

	public static boolean isExpStart(){
		File f = new File(FMLogic.EXP_START_FLAG);
		return (f.exists());
	}

	public static void resetExpStart(){
		File f = new File(FMLogic.EXP_START_FLAG);
		f.delete();
	}

}

