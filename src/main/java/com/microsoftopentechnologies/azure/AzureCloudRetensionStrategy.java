/*
 Copyright 2014 Microsoft Open Technologies, Inc.
 
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
 http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
package com.microsoftopentechnologies.azure;

import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import org.kohsuke.stapler.DataBoundConstructor;

import com.microsoftopentechnologies.azure.exceptions.AzureCloudException;
import com.microsoftopentechnologies.azure.retry.LinearRetryForAllExceptions;
import com.microsoftopentechnologies.azure.util.Constants;
import com.microsoftopentechnologies.azure.util.ExecutionEngine;

import hudson.model.Descriptor;
import hudson.slaves.RetentionStrategy;
import hudson.util.TimeUnit2;

public class AzureCloudRetensionStrategy extends RetentionStrategy<AzureComputer>  {
	public final long idleTerminationMillis;
    private transient ReentrantLock slaveNodeLock;

	private static final Logger LOGGER = Logger.getLogger(AzureManagementServiceDelegate.class.getName());

	@DataBoundConstructor
	public AzureCloudRetensionStrategy(int idleTerminationMinutes) {
		readResolve();
		this.idleTerminationMillis = TimeUnit2.MINUTES.toMillis(idleTerminationMinutes);
	}
	
	public long check(final AzureComputer slaveNode) {
        if (! slaveNodeLock.tryLock()) {
            return 1;
        } else {
            try {
                return _check(slaveNode);
            } finally {
            	slaveNodeLock.unlock();
            }
        }
    }

	private long _check(final AzureComputer slaveNode) {
        // if idleTerminationMinutes is zero then it means that never terminate the slave instance 
        // an active node or one that is not yet up and running are ignored as well
        if (idleTerminationMillis > 0 && slaveNode.isIdle() && slaveNode.isOnline() //slaveNode.isProvisioned()
                && idleTerminationMillis < (System.currentTimeMillis() - slaveNode.getIdleStartMilliseconds())) {
            // block node for further tasks
            slaveNode.setAcceptingTasks(false);
            LOGGER.info("AzureCloudRetensionStrategy: check: Idle timeout reached for slave: "+slaveNode.getName());

            try {
            	slaveNode.getNode().idleTimeout();
            } catch (AzureCloudException ae) {
                LOGGER.info("AzureCloudRetensionStrategy: check: could not terminate or shutdown "+slaveNode.getName()+ " Error code "+ae.getMessage());
            } catch (Exception e) {
                LOGGER.info("AzureCloudRetensionStrategy: check: could not terminate or shutdown "+slaveNode.getName()+ "Error code "+e.getMessage());
                
            }
            // close channel? Need to see if below code solved any issues.
            try {
                slaveNode.setProvisioned(false);
                if (slaveNode.getChannel() != null) {
                    slaveNode.getChannel().close();
                }
            } catch (Exception e) {
                e.printStackTrace();
                LOGGER.info("AzureCloudRetensionStrategy: check: exception occured while closing channel for: " + slaveNode.getName());
            }
        }
        return 1;
	}

	public void start(AzureComputer azureComputer) {
		//TODO: check when this method is getting called and add code accordingly
		LOGGER.info("AzureCloudRetensionStrategy: start: azureComputer name "+azureComputer.getDisplayName());
		azureComputer.connect(false);
	}
	
	protected Object readResolve() {
		slaveNodeLock = new ReentrantLock(false);
		return this;
	}

	public static class DescriptorImpl extends Descriptor<RetentionStrategy<?>> {
		public String getDisplayName() {
			return Constants.AZURE_CLOUD_DISPLAY_NAME;
		}
	}

}
