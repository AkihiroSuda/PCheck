
package org.apache.cassandra;


import java.lang.Thread;
import java.io.*;
import java.lang.*;
import java.lang.management.ManagementFactory;
import java.net.*;


public final class Util {


  private static PrintStream pps;
  private static MyLong theLastTime = new MyLong(0);
  private static String lastWarning = "";

  // **********************************
  public static class MyLong {
    long l;
    public MyLong() { l = 0; }
    public MyLong(long x) { l = x; }
    public long longValue() { return l;    }
    public void setValue(long x) { l = x; }
  }

  // **********************************
  public static long now() {
    return System.currentTimeMillis();
  }

  // **********************************
  public static String diff() {
    return diff(theLastTime);
  }

  // **********************************
  public static String diff(MyLong lastTime) {
    long currentTime = System.currentTimeMillis();
    double diff = 0.0;
    if (lastTime.longValue() == 0) {
      lastTime.setValue(currentTime);
    }
    else {
      diff = (double)(currentTime - lastTime.longValue()) / 1000;
      lastTime.setValue(currentTime);
    }
    String warning = "";

    if (diff > 1) {
      warning = "*LONG!!*";
    }

    if (diff > 5) {
      warning = "*LONG LONG!!*";

      warning += ("\n" + getStackTrace());
    }

    if (diff > 30) {
      warning = "*LONG LONG LONG!!*";

      warning += ("\n" + getStackTrace());

    }

    lastWarning = warning;

    long ct = (currentTime / 1000) % 1000;

    long tid = (Thread.currentThread().getId()) % 100;

    return String.format("[hd-diff][%5.3f secs] [%d] (t-%d) %s",
                         diff, (int)ct, (int)tid, warning);

  }


  // **************************************
  public static void debug(String msg) {

    // only print if experiment is running
    File f = new File("/tmp/fi/experimentRunning");
    if (f.exists()) {
      msg = msg + " [part of workload]";
    }

    debug(pps, msg);
  }

  // **************************************
  public static void debug(PrintStream ps, String msg) {
    boolean debug = true;
    //boolean debug = false;
    if (debug) {
      println(ps, msg);
    }
  }


  // **************************************
  public static void printStackTrace(Exception e) {
    if (e == null)
      print(getStackTrace());
    else
      print(stackTraceToString(e.getStackTrace()));
  }

  // **************************************
  public static void printStackTrace() {
    printStackTrace(null);
  }

  // **************************************
  public static String getStackTrace() {
    Thread t = Thread.currentThread();
    StackTraceElement[] ste = t.getStackTrace();
    return stackTraceToString(ste);
  }

  // **************************************
  public static String stackTraceToString(StackTraceElement[] ste) {
    String str = "";
    for (int i = 0; i < ste.length ; i++) {
      str += String.format("    [%02d] %s \n", i, ste[i].toString());
    }
    return str;
  }


  // **************************************
  public static void setPrintStream(PrintStream ppsArg) {
    pps = ppsArg;
  }


  // **************************************
  public static void print(String msg) {
    print(pps, msg);
  }

  // **************************************
  public static void print(PrintStream ps, String msg) {
    System.out.print(msg);
    System.out.flush(); // ??
    if (ps != null) {
      ps.print(msg);
    }

  }



  // **************************************
  public static void println(PrintStream ps, String msg) {
    print(ps, msg + "\n");
  }

  // *******************************************
  public static int getIntPid() {
    String pidStr = getPid();
    return (new Integer(pidStr)).intValue();
  }

  // *******************************************
  public static String getPid() {
    String fullPid = ManagementFactory.getRuntimeMXBean().getName();
    String [] split = fullPid.split("@", 2);
    String pid = split[0];
    return pid;
  }


  // *****************************************
  // a tool to run the command
  public static String runCommand(String cmd) {

    String msg = "";
    try {
      
      String line;
      
      Process p = Runtime.getRuntime().exec(cmd);
      BufferedReader input =new BufferedReader
        (new InputStreamReader(p.getInputStream()));      
      
      while ((line = input.readLine()) != null) {
        msg = msg + line + "\n";
      }
      
      input.close();
      
      //JINSU HACK
      //JINSU: I was getting TOO MANY FILE OPENED...
      p.getErrorStream().close();
      p.getOutputStream().close();
    } catch (Exception e) {
      System.out.println("runcmd!!! " + e); 
      //System.err.println("runcmd!!!");
    }

    return msg;
  }


  // **************************************
  public static boolean stringToFileContent(String buf, String path) {
    return stringToFileContent(buf, new File(path), false);
  }


  // **************************************
  public static boolean stringToFileContent(String buf, File f) {
    return stringToFileContent(buf, f, false);
  }


  // **************************************
  public static boolean stringToFileContent(String buf, File f, boolean flush) {

    assertSafeFile(f);

    try {
      FileWriter fw = new FileWriter(f);
      fw.write(buf);
      if (flush)
        fw.flush();
      fw.close();
    } catch (IOException e) {

      return false;
    }
    return true;
  }

  // **************************************
  public static void assertSafeFile(File f) {
    assertSafeFile(f.getAbsolutePath());
  }

  // **************************************
  public static void assertSafeFile(String fullPath) {
    boolean safe = false;

    // we only allow removal of anything that is in /tmp/*  !!
    if (fullPath.indexOf("/tmp") == 0) {
      safe = true;
    }
    else  {
      safe = false;
    }

    // if not safe, don't proceed
    if (safe == false) {
      System.out.println("Dangerous!!!");
      System.err.println("Dangerous!!!");
      System.out.flush();
      System.exit(-1);
    }
  }


  // **************************************
  public static String getThreadName() {
    Thread t = Thread.currentThread();
    String name = t.getName();
    return name;
  }


  // *****************************************
  public static void sleep(int ms) {
    try {
      Thread.sleep(ms);
    } catch (Exception e) {}

  }

	//EXTRA UTILITY FOR HACK PURPOSE
	// *******************************************
  public static String getNodeId() {
    String pid = getPid();
    String nodeId = getNodeIdFromPid(pid);
    return nodeId;
  }
  // ***************************************************
  public static String getNodeIdFromPid(String pid) {

    String nodeId = "UnknownNodeId";
    String fname;

    // check the cache key first
    // key = String pid, value = nodeId
    /*
    if (pidCacheMap.containsKey(pid)) {
      nodeId = (String) pidCacheMap.get(pid);
      return nodeId;
    }
    */

		//jinsu making changes because we just store pids inside /tmp/fi/pids
		// check if it is a node
		int cnode = getCnodeIdFromPid(pid);
		if(cnode >= 0) {
			nodeId = String.format("Node%d", cnode);
			//pidCacheMap.put(pid, nodeId);
			return nodeId;
		}
		
		//jinsu it prints too many lines...
		if(false) {
		/*
    // check if datanode
    int nid = getDnIdFromPid(pid);
    if (nid != 0) {
      nodeId = String.format("DataNode-%d", nid);
      pidCacheMap.put(pid, nodeId);
      return nodeId;
    }

    // check if namenode
    fname = "/tmp/" + FMLogic.CASS_USERNAME+ "-namenode.pid";
    if (isPidMatch(fname, pid)) {
      nodeId = "NameNode";
      pidCacheMap.put(pid, nodeId);
      return nodeId;
    }

    // check if secondary namenode
    fname = "/tmp/" + FMLogic.CASS_USERNAME + "-namenode.pid";
    if (isPidMatch(fname, pid)) {
      nodeId = "SecondaryNameNode";
      pidCacheMap.put(pid, nodeId);
      return nodeId;
    }

    // check if client
    fname = "/tmp/" + FMLogic.CASS_USERNAME + "-client.pid";
    if (isPidMatch(fname, pid)) {
      nodeId = "Client";
      pidCacheMap.put(pid, nodeId);
      return nodeId;
    }
    */		 
		}
    return nodeId;

  }

	//jinsu making a function to check all node#.pid files.
  private static int getCnodeIdFromPid(String pid) {
  	//start from 0!!
  	int max_nodes = 5;
  	if(false) {
  		System.out.println(max_nodes + " Cass Nodes running...");
  	}
  	//supporting up to 10 nodes for now...
  	for (int i = 0; i < max_nodes; i++) {
  		String fname = String.format("/tmp/fi/" + "pids/" + "node%d.pid", i);
  		if (isPidMatch(fname, pid))
  			return i;	
  	}
  	return -1;
  }
  
  // ***************************************************
  private static boolean isPidMatch(String fname, String pid) {
    String tmp = getPidFromTmpPidFile(fname);
    if (tmp.equals(pid)) {
      return true; // match!
    }
    return false;
  }

  // ***************************************************
  private static String getPidFromTmpPidFile(String fname) {

    File f = new File(fname);
    if (!f.exists()) {
      //Util.WARNING("not exist: " + fname);
      return "0";
    }

    // else read the file and return the pid
    try {
      BufferedReader in = new BufferedReader(new FileReader(fname));
      String str = in.readLine();
      if (str == null) {
        //Util.WARNING("str is null");
        return "0";
      }
      in.close();
      return str; // successful one
    } catch (IOException e) {
      //Util.EXCEPTION("getdatanodefrompid", e);
      return "0";
    }
  }
  
  //jinsu get nodeId using ip address
  //***************************************************
  public static String getNodeIdFromINetAddr(InetAddress iaddr) {
		//look in IP_HISTORY_DIR
		String cnode = getCnodeIdFromIp(iaddr);
		if(cnode != null) {
			return cnode;
		}
		return String.format("Unknown-Ip");
		}

  //jinsu
  //consults ip history dir and returns "Node0, Node1... or null if it cant find the file."
  private static String getCnodeIdFromIp(InetAddress iaddr) {
  	String nodeId = null;
  	int addr_hash = iaddr.hashCode();
  	nodeId = getNodeIDFromTmpIpFile("/tmp/fi/ipHistory/" + addr_hash);
  	if(nodeId.equals("0")) {
  		nodeId = null;
  	}
  	return nodeId;
  }
	// ***************************************************
  private static String getNodeIDFromTmpIpFile(String fname) {

    File f = new File(fname);
    if (!f.exists()) {
      //Util.WARNING("not exist: " + fname);
      return "0";
    }

    // else read the file and return the pid
    try {
      BufferedReader in = new BufferedReader(new FileReader(fname));
      String str = in.readLine();
      if (str == null) {
        //Util.WARNING("str is null");
        return "0";
      }
      in.close();
      return str; // successful one
    } catch (IOException e) {
      //Util.EXCEPTION("getnodeidfromipfile", e);
      return "0";
    }
  }
  
}





