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


// this context must be pure and have no dependence at all

public class Context {
  
  private String targetIO = "";
  
  private Object extraContext = "";
  
  private int port = 0;
  
  private final String TMPFI = "/tmp/fi/";
  private final String HADOOP_USERNAME = "hadoop-" + System.getenv("USER") ;
  private final String HADOOP_STORAGE_DIR = TMPFI + HADOOP_USERNAME + "/";
  
  //********************************************
  // rest
  //********************************************

  public Context() {
    
  }

  public Context(String s) { 
    targetIO = new String(s);
  }

  public Context(int port) {
    this.port = port;
  }
  
  public int getPort() {
    return port;
  }

  public String getTargetIO() {
    return targetIO;
  }
  
  public void setExtraContext(Object extra) {
    this.extraContext = extra;
  }
  
  public Object getExtraContext() {
    return extraContext;
  }
  
  public String toString() {
    String tmp = targetIO;
    if (targetIO.contains("/tmp/" + HADOOP_USERNAME))
      tmp = tmp.replaceFirst("/tmp/" + HADOOP_USERNAME, "/thh/");
    if (targetIO.contains(HADOOP_STORAGE_DIR))
      tmp = tmp.replaceFirst(HADOOP_STORAGE_DIR, "/rhh/");
    return tmp;
  }
  
  public void setTargetIO(String f) {
    targetIO = new String (f);
  }
  
}

