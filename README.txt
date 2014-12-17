PCheck 8a38c7459427486472ca59816534e755ac49b1f2 (Nov 14 2012)
https://github.com/pallavij/PCheck

possible research area:
 * eliminate app-specific rules
 * explore parameter-specific states [No one has tackled with this kind of problem]
 * improve the implementation:
  * no recompilation (use byteman instead of AspectJ, or introduce new JVM)
  * integrate into earthquake


Environment: Ubuntu 14.10 (amd64) with JDK6
===
# set JDK version to 6; aspectj requires this version
$ export LANG=C LC_ALL=C #for awk
$ for f in `ls  -l /etc/alternatives/ | grep java | grep -v '\.1\.gz' | awk -e '{ print $9 }'`;do sudo update-alternatives --config $f; done
# preferred envs
$ export ANT_OPTS="-Xms1024m -Xmx1024m -Dhttp.proxyHost=YOUR_PROXY_HOST -Dhttp.proxyPort=YOUR_PROXY_PORT" 
$ export JAVA_HOME=/usr/lib/jvm/java-6-openjdk-amd64/jre


Terms (just a speculation)
===
* DOS: DataOutputStream
* FAC: FMAllContext
* FailType: enum defined in FMServer: {CRASH, BADDISK(persistent), EXCEPTION(transient), RETFALSE, CORRUPTION, NETFAILURE, NONE}
* FMAdmin, FMProtocol: something already abandonned (empty classes)
* FMLogic: auto fault injection logic (c.f.ManuaFI). Note that actual injector is implemented in FMClient
* Frog: something already abandonned (fi/OLD-unused/FrogServer.java)
* FSN: injected failure number
* JP: join point
* JROV: join point returned object value
* ManualFI: manual fault injector (can be used as an alternative to FMLogic. see also FMServer)
* warningHooks.aj: an aspect that seems to be deprecated
* ZxID: ZooKeeper Transaction ID

Usage
===
ZooKeeper
---
$ git grep /home/suda/WORK/PCheck 
$ vi grepped-files
$ cd zook-fi
$ ant firt && ant && antfi
$ cd ../zook-wd
$ make kill #mandatory
$ JAVA_TOOL_OPTIONS='-DenableFailure=true -DenableOptimizer=true -DZK_NODES=3 -DMAX_FSN=100 -DBREAK_EXP_NUMBER=10' ant


Call graphs (too adhoc format, fixme)
===
ZooKeeper message dependency
---
..
* FMServer.start()
 # NOTE: According to the comments, FMServer uses "Frog".
 #       However, "Frog" is actually not used anymore.
 #       (Frog => PCheck/cass-fi/OLD/fi/OLD-ununsed/FrogServer.java)
* Reorder.start(), Reorder.run() 
 # Reorder extends java.lang.Thread
 # so run() is called by java.lang.Thread.start().
* Search.wakeUpBreadthFirst()
* Search.getEvtsToReorder()
* Search.areDep() #indeed this is a private method
* dep.py
 # this accesses /tmp/fi/depComp/{e1, e2, qstate, state, res}.
 # these files are made by Search.areDep().
 # state = AState.getStateOfLeaders; qstate = AState.getStateOfQueues()
 # NOTE: Dep{1..7}.java are not used. dep.py is used instead.
* P1.py
* parse.py
 # this parses /tmp/fi/coverageComplete/h%d.txt.
 #$ cat /tmp/fi/coverageComplete/h352332824.txt                                                                                                           
 #TYPE: POLL
 #LOC: org/apache/zookeeper/server/quorum/FastLeaderElection.java(224)
 #QUEUE: nodeId = Node-3
 #srcLn = org/apache/zookeeper/server/quorum/QuorumCnxManager.java(129)
 #STACK:     [00] java.lang.Thread.getStackTrace(Thread.java:1516) 
 #    [01] org.fi.Util.getStackTrace(Util.java:960) 
 #    [02] org.fi.FMClient.getStack(FMClient.java:921) 
 #    [03] org.fi.FMClient.fiHookPoll(FMClient.java:269) 
 #    [04] org.apache.zookeeper.server.quorum.FastLeaderElection$Messenger$WorkerReceiver.poll_aroundBody1$advice(FastLeaderElection.java:271) 
 #    [05] org.apache.zookeeper.server.quorum.FastLeaderElection$Messenger$WorkerReceiver.run(FastLeaderElection.java:224) 
 #    [06] java.lang.Thread.run(Thread.java:701) 
 #


ZooKeeper fault injection (excepts NETFAILURE)
---
* aspect zkHooks{}
* FMClient.fiHookIox(FMJoinPoint), fiHookFnfx(), fiHookNox()
 # Iox: throws IOException / Fnfx: throws FileNotFoundException / Nox: throws no exception
* FMClient.tryFiHook(FMJoinPoint)
* FMClient.syncedTryHook(FMJoinPoint)
* FMClient.doFiHook(FMJoinPoint)
 # this calls sendContextOptimizer(), printFailType(), processFailure()
	* FMClient.sendContextOptimizer(FMAllContext)
	* FMClient.sendContextViaXmlRpc(FMAllContext)
	 # this calls FMServer.sendContext() by org.apache.client.XmlRpcClient
	* FMServer.sendContext(int randId)
	* FMServer.syncedSendContext(int randId)
	* FMServer.syncedSendContext(FMAllContext)
	 # this calls printFailure() if debug
	* FMServer.doFail(FMAllContext)
	 # here you can call manualFI instead of FMLogic.run(), if you want
	* FMLogic.run(FMAllContext)
	* FMLogic.tryTheseFailures(FMAllContext, FailType[])
	* FMLogic.tryThisFailure(FMAllContext, FailType)
	 # this checks existence of /tmp/fi/enableFailureFlag by calling isEnableFailureFlagExist() 
	* FMLogic.runFailLogic()
	 # ATTENTION: this just returns enum FailType; will NOT invoke any injection.
* FMClient.processFailure(FMJoinPoint, FMStackTrace, FMContext, FailType)
* FMClient.processCrash(), processCorruption(), processReturnFalse(), processException(), processBadDisk()
 # these are practical injectors.
 # NOTE: NETFAILURE is not handled here


ZooKeeper fault injection (NETFAILURE)
---
* aspect zkHooks{}
* FMClient.fiHookNetRead(FMJoinPoint)
* FMClient.waitUntilMyTurnNetRd(FMJoinPoint, VoteContext, String loc, String srcloc)
 #FMJoinPoint holds a referrence to VoteContext, which is ZooKeeper-specific class.
 #zkHooks.aj instantiates VoteContext by parsing ByteBuffer of org.apache.zookeeper.server.quorum.QuorumCnxManager.Message  
..
