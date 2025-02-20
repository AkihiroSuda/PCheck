/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.cassandra.net;

import java.io.IOException;
import java.net.InetAddress;

import org.apache.log4j.Logger;

import org.apache.cassandra.concurrent.StageManager;

//jinsu
import org.apache.cassandra.Util;

class OutboundTcpConnectionPool
{
    private InetAddress remoteEp_;
    private OutboundTcpConnection cmdCon;
    private OutboundTcpConnection ackCon;

    OutboundTcpConnectionPool(InetAddress remoteEp)
    {
        remoteEp_ = remoteEp;
    }

    /**
     * returns the appropriate connection based on message type.
     * returns null if a connection could not be established.
     */
    synchronized OutboundTcpConnection getConnection(Message msg)
    {
        if (StageManager.RESPONSE_STAGE.equals(msg.getMessageType())
            || StageManager.GOSSIP_STAGE.equals(msg.getMessageType()))
        {
            if (ackCon == null)
            {
                ackCon = new OutboundTcpConnection(this, remoteEp_);
                ackCon.start();
            }
            return ackCon;
        }
        else
        {
            if (cmdCon == null)
            {
                cmdCon = new OutboundTcpConnection(this, remoteEp_);
                cmdCon.start();
            }
            return cmdCon;
        }
    }

    synchronized void reset()
    {
    		
        for (OutboundTcpConnection con : new OutboundTcpConnection[] { cmdCon, ackCon })
            if (con != null)
                con.closeSocket();
                
        Util.debug("    - OTCP.reset() executed");
    }
}
