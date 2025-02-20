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
import java.nio.channels.*;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;



public class LeaderElectionWorkload implements Workload {
  
  private Driver     driver;
  private Utility    u;
  private Experiment exp;


  // *******************************************
  public LeaderElectionWorkload(Driver driver, Experiment exp) {
  
    this.driver = driver;
    this.u = driver.getUtility();
    this.exp = exp;
  
  }
  

  // *******************************************
  // the algorithm
  // *******************************************
  public void run() {

    // 1. setup for this specific workload
    preSetup();

    // 2. the exact workload where we want to run with failure
    runWithFailure();
    
    // 3. run post setup 
    postSetup();
  
  }
  
  
  // *******************************************
  public void runWithFailure() {

    u.createNewFile(Driver.EXPERIMENT_RUN_FLAG);

    u.runCommand("./bin/run-test.sh");

    u.deleteFile(Driver.EXPERIMENT_RUN_FLAG);    
    
  }


  // *******************************************
  public void preSetup() {
    
    Driver.enableFailureManager();
    Driver.enableClientOptimizer();
    Driver.enableFrog();
    Driver.enableCoverage();
    
  }

  // *******************************************
  public void postSetup() {    

    // 4. stop the failure
    Driver.disableCoverage();
    Driver.disableFrog();
    Driver.disableFailureManager();
    Driver.disableClientOptimizer();
    
  }

  
}
