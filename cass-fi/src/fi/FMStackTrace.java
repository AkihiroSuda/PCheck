/**
 * Copyright (c) 2012,
 * Thanh Do  <thanhdo@cs.wisc.edu>
 * Haryadi S. Gunawi  <haryadi@cs.uchicago.edu>
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

import java.io.*;
import java.lang.*;




// public class FMStackTrace implements Writable {
public class FMStackTrace {

  private int totalHashCode = 0;  
  private FMStackTraceElement [] s = null;


  //********************************************
  // writable interface
  // this is serialization (see DatanodeID.java for example)
  // need to decide how to serialize the RPC
  //********************************************
  public void write(DataOutput out) throws IOException {

    out.writeInt(totalHashCode);
    out.writeInt(s.length);
    for (int i = 0; i < s.length; i++) 
      s[i].write(out);
    
  }


  public void readFields(DataInput in) throws IOException {
    
    totalHashCode = in.readInt();
    int sLength = in.readInt();
    s = new FMStackTraceElement[sLength];
    for (int i = 0; i < s.length; i++) {
      s[i] = new FMStackTraceElement();
      s[i].readFields(in);
    }
  }
  

  //********************************************
  // rest
  //********************************************

  public FMStackTrace() {
    
  }
  
  public FMStackTrace(StackTraceElement[] ste) { 
    
    // int hc;
    // int total = 0;
    int validCount = 0;

    // for calculating hash code
    String tmpHash = "";

    // need two passes
    for (int i = 0; i < ste.length ; i++) {
      if (isValid(ste[i]))
	validCount++;
    }
    
    if (validCount == 0) {
      return;
    }
    
    // create
    s = new FMStackTraceElement[validCount];
    int j = 0;
    
    // 2nd pass
    for (int i = 0; i < ste.length ; i++) {    
      
      
      if (!isValid(ste[i])) 
	continue;
      
      s[j] = new FMStackTraceElement(ste[i]);
      
      tmpHash = tmpHash + ste[i].toString();

      // compute the hash code
      // hc = Math.abs(ste[i].hashCode()) & 1000000;
      // total += hc;
      
      j++;
    }
    
    totalHashCode = tmpHash.hashCode();
    // totalHashCode = total;
  }
  
  
  // *******************************
  // the filtering is done here
  public boolean isValid(StackTraceElement e) {

    
    if (e.getFileName() != null)
      if (e.getFileName().contains(".aj")) return false;
    
    if (e.getClassName() != null) {
      if (e.getClassName().indexOf("org.apache.tools") == 0) return false;
      if (e.getClassName().indexOf("sun.") == 0) return false;
      if (e.getClassName().indexOf("java.") == 0) return false;
      if (e.getClassName().indexOf("javax.") == 0) return false;
      if (e.getClassName().indexOf("org.fi") == 0) return false;
      // if (e.getClassName().contains("$")) return false;
    }

    if (e.getMethodName() != null) {
      if (e.getMethodName().contains("aroundBody")) return false;
    }

    return true;
  }

  // ******************************************
  public boolean contains(String className, String funcName) {

    for (int i = 0; i < s.length; i++) {
      if (s[i].getClassName().contains(className) &&
	  s[i].getMethodName().equals(funcName)) {
	return true;
      }
    }
    return false;

  }
    


  
  public FMStackTraceElement [] getStackTraces() {
    return s;
  }

  public int getTotalHashCode() {
    return totalHashCode;
  }

  public String toString() {

    String tmp = "";
    

    if (s == null)
      return "\n";
    
    for (int i = 0; i < s.length; i++) {

      String tmp1 = s[i].getClassName();

      // trimming is done here ..
      tmp1 = tmp1.replaceFirst("org.apache.hadoop.", "");
      tmp1 = tmp1.replaceFirst("hdfs.server.", "");
      
      
      tmp += ("      [" + i + "] ");
      // tmp += (s[i].getFileName() + " / ");
      // tmp += (s[i].getClassName() + " (");
      tmp += (tmp1 + " (");
      tmp += (s[i].getMethodName() + ":");
      tmp += (s[i].getLineNumber() + ")");
      // tmp += "[" + totalHashCode + "]";
      tmp += "\n";
    }
    
    // tmp += ("      [T] TOTAL HASH CODE: [" + totalHashCode + "] \n");
    return tmp;
  }



  
}