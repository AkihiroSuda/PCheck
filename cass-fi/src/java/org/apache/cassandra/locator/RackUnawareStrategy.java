/*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements.  See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership.  The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License.  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.cassandra.locator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.dht.Token;
import java.net.InetAddress;

//JINSU
import java.util.*;
import org.apache.cassandra.Util;
import java.net.*;

/**
 * This class returns the nodes responsible for a given
 * key but does not respect rack awareness. Basically
 * returns the 3 nodes that lie right next to each other
 * on the ring.
 */
public class RackUnawareStrategy extends AbstractReplicationStrategy
{
    public RackUnawareStrategy(TokenMetadata tokenMetadata, IEndPointSnitch snitch)
    {
        super(tokenMetadata, snitch);
    }

    public ArrayList<InetAddress> getNaturalEndpoints(Token token, TokenMetadata metadata, String table)
    {
        int replicas = DatabaseDescriptor.getReplicationFactor(table);
        List<Token> tokens = metadata.sortedTokens();
        //jinsu
        System.out.println("token size : " + tokens.size() + ".... replicas = " + replicas);
        ArrayList<InetAddress> endpoints = new ArrayList<InetAddress>(replicas);

        if (tokens.isEmpty())
            return endpoints;
				
				
        // Add the token at the index by default
        Iterator<Token> iter = TokenMetadata.ringIterator(tokens, token);
        
        
        
        /*
        while (endpoints.size() < replicas && iter.hasNext())
        {
            endpoints.add(metadata.getEndPoint(iter.next()));
        }
        
        return endpoints;
        */
        
        //JINSU : HACK
        //Order the endpoints so that every insert will send data to the same set of nodes.
        TreeMap ipMap = new TreeMap(); 
        while (iter.hasNext()) {
        	InetAddress ip = metadata.getEndPoint(iter.next());
        	if(ip instanceof Inet4Address) {
        		//long ipLong = ((long) (0xff & ipByte[0]) << 24) + ((long) (0xff & ipByte[1]) << 16) + ((long) (0xff & ipByte[2]) << 8) + ((long) (0xff & ipByte[3]) << 0);
        		//Util.debug("ipLong = " + ipLong + "... ipHash = " + ip.hashCode());
        		ipMap.put(ip.hashCode(), ip);
        	} else {
        		Util.debug("Inet6Address not implemented yet.");
        		Util.debug("Need to be implemented");
        	}
        }
        assert tokens.size() == ipMap.size();
        //Util.debug("- In RUS.getNaturalEndpoints(), ipMap => " + ipMap);
        Iterator<InetAddress> ipIter = ipMap.values().iterator();
        while (endpoints.size() < replicas && ipIter.hasNext()) {
        	endpoints.add(ipIter.next());
        }
				        
        return endpoints;
        
    }
       
}
