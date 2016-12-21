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

import java.io.IOException;
import java.util.logging.Logger;

import com.microsoftopentechnologies.azure.exceptions.AzureCloudException;

import jenkins.model.Jenkins;
import hudson.Extension;
import hudson.model.AsyncPeriodicWork;
import hudson.model.TaskListener;
import hudson.model.Computer;

@Extension
public final class AzureSlaveCleanUpTask extends AsyncPeriodicWork {
	private static final Logger LOGGER = Logger.getLogger(AzureSlaveCleanUpTask.class.getName());

	public AzureSlaveCleanUpTask() {
		super("Azure slave clean task");
	}

	public void execute(TaskListener arg0) throws IOException, InterruptedException {
		for (Computer computer : Jenkins.getInstance().getComputers()) {
			if (computer instanceof AzureComputer) {
				AzureComputer azureComputer = (AzureComputer)computer;
				final AzureSlave slaveNode = azureComputer.getNode();
				
                // Only clean up offline nodes.
				if (azureComputer.isOffline()) {
					if(AzureManagementServiceDelegate.isVirtualMachineExists(slaveNode)) {
                        // Retry 10 times, since there may be exclusive locks and cleaning up
                        // is very valuable.
                        int tries = 0;
                        int maxTries = 10;
                        do {
                            try {
                                slaveNode.cleanup();
                                break;
                            } catch (AzureCloudException exception) {
                                // No need to throw exception back, just log and move on. 
                                 LOGGER.info("AzureSlaveCleanUpTask: execute: failed to remove node (" + (maxTries-tries-1) + " tries remaining) " +exception);
                            } catch (Exception e) {
                                // TODO Auto-generated catch block
                                LOGGER.info("AzureSlaveCleanUpTask: execute: failed to remove node (" + (maxTries-tries-1) + " tries remaining) " +e);
                            }
                            Thread.sleep(18 * 1000);
                        }
                        while (++tries < maxTries);
					} else {
                        LOGGER.info("AzureSlaveCleanUpTask: execute: removing node " + slaveNode.getNodeName());
						Jenkins.getInstance().removeNode(slaveNode);
					}
				}
			}
		}
	}

	public long getRecurrencePeriod() {
		// Every 30 minutes
		return 30 * 60 * 1000;
	}
}
