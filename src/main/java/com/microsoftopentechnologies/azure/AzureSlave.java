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
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import jenkins.model.Jenkins;

import org.kohsuke.stapler.DataBoundConstructor;

import com.microsoftopentechnologies.azure.CleanupAction;
import com.microsoftopentechnologies.azure.util.Constants;
import com.microsoftopentechnologies.azure.util.FailureStage;
import com.microsoftopentechnologies.azure.remote.AzureSSHLauncher;

import hudson.Extension;
import hudson.model.TaskListener;
import hudson.model.Descriptor.FormException;
import hudson.slaves.AbstractCloudComputer;
import hudson.slaves.AbstractCloudSlave;
import hudson.slaves.JNLPLauncher;
import hudson.slaves.NodeProperty;
import hudson.slaves.ComputerLauncher;
import hudson.slaves.OfflineCause;
import hudson.slaves.RetentionStrategy;

public class AzureSlave extends AbstractCloudSlave  {
	private static final long serialVersionUID = 1L;
	private  String cloudName;
	private  String adminUserName;
	private  String sshPrivateKey;
	private  String sshPassPhrase;
	private  String adminPassword;
	private  String jvmOptions;
	private  boolean shutdownOnIdle;
	private  String cloudServiceName;
	private  int retentionTimeInMin;
	private String slaveLaunchMethod;
	private  String initScript;
	private  String deploymentName;
	private  String osType;
	// set during post create step
	private String publicDNSName;
	private int sshPort;
	private  Mode mode;
	private  String subscriptionID;
	private  String managementCert;
	private  String passPhrase;
	private  String managementURL;
	private String templateName;
    private CleanupAction cleanupAction;
	private static final Logger LOGGER = Logger.getLogger(AzureSlave.class.getName());

	@DataBoundConstructor
	public AzureSlave(String name, String templateName, String nodeDescription, String osType, String remoteFS, int numExecutors, Mode mode, String labelString,
			ComputerLauncher launcher, RetentionStrategy<AzureComputer> retentionStrategy, List<? extends NodeProperty<?>> nodeProperties, 
			String cloudName, String adminUserName, String sshPrivateKey, String sshPassPhrase, String adminPassword, String jvmOptions, 
			boolean shutdownOnIdle, String cloudServiceName, String deploymentName, int retentionTimeInMin, String initScript, 
			String subscriptionID, String managementCert, String passPhrase, String managementURL, String slaveLaunchMethod, CleanupAction cleanupAction) throws FormException, IOException {
		super(name, nodeDescription, remoteFS, numExecutors, mode, labelString, launcher, retentionStrategy, nodeProperties);
		this.cloudName = cloudName;
		this.templateName = templateName;
		this.adminUserName = adminUserName;
		this.sshPrivateKey = sshPrivateKey;
		this.sshPassPhrase = sshPassPhrase;
		this.adminPassword = adminPassword;
		this.jvmOptions = jvmOptions;
		this.shutdownOnIdle = shutdownOnIdle;
		this.cloudServiceName = cloudServiceName;
		this.deploymentName = deploymentName;
		this.retentionTimeInMin = retentionTimeInMin;
		this.initScript = initScript;
		this.osType = osType;
		this.mode = mode;
		this.subscriptionID = subscriptionID;
		this.managementCert = managementCert;
		this.passPhrase = passPhrase;
		this.managementURL = managementURL;
		this.slaveLaunchMethod = slaveLaunchMethod;
		this.cleanupAction = cleanupAction;
	}
	
	public AzureSlave(String name, String templateName, String nodeDescription, String osType, String remoteFS, int numExecutors, Mode mode, String labelString,
			String cloudName, String adminUserName, String sshPrivateKey, String sshPassPhrase, String adminPassword, String jvmOptions, 
			boolean shutdownOnIdle, String cloudServiceName, String deploymentName, int retentionTimeInMin, String initScript, 
			String subscriptionID, String managementCert, String passPhrase, String managementURL, String slaveLaunchMethod, CleanupAction cleanupAction) throws FormException, IOException {
		this(name, templateName, nodeDescription, osType, remoteFS, numExecutors, mode, labelString, 
				slaveLaunchMethod.equalsIgnoreCase("SSH")? osType.equalsIgnoreCase("Windows")? new AzureSSHLauncher():new AzureSSHLauncher() : new JNLPLauncher(),
				new AzureCloudRetensionStrategy(retentionTimeInMin), Collections.<NodeProperty<?>> emptyList(), cloudName, adminUserName,
				sshPrivateKey, sshPassPhrase, adminPassword, jvmOptions, shutdownOnIdle, cloudServiceName, deploymentName, retentionTimeInMin, initScript,
				subscriptionID, managementCert, passPhrase, managementURL, slaveLaunchMethod, cleanupAction);
		this.cloudName = cloudName;
		this.templateName = templateName;
		this.adminUserName = adminUserName;
		this.sshPrivateKey = sshPrivateKey;
		this.sshPassPhrase = sshPassPhrase;
		this.adminPassword = adminPassword;
		this.jvmOptions = jvmOptions;
		this.shutdownOnIdle = shutdownOnIdle;
		this.cloudServiceName = cloudServiceName;
		this.deploymentName = deploymentName;
		this.retentionTimeInMin = retentionTimeInMin;
		this.initScript = initScript;
		this.osType = osType;
		this.mode = mode;
		this.subscriptionID = subscriptionID;
		this.managementCert = managementCert;
		this.passPhrase = passPhrase;
		this.managementURL = managementURL;
		this.cleanupAction = cleanupAction;
	}

	public String getCloudName() {
		return cloudName;
	}
	
	public Mode getMode() {
		return mode;
	}

	public String getAdminUserName() {
		return adminUserName;
	}

	public String getSubscriptionID() {
		return subscriptionID;
	}

	public String getManagementCert() {
		return managementCert;
	}

	public String getPassPhrase() {
		return passPhrase;
	}

	public String getManagementURL() {
		return managementURL;
	}

	public String getSshPrivateKey() {
		return sshPrivateKey;
	}
	
	public String getOsType() {
		return osType;
	}

	public String getSshPassPhrase() {
		return sshPassPhrase;
	}
	
	public String getCloudServiceName() {
		return cloudServiceName;
	}
	
	public String getDeploymentName() {
		return deploymentName;
	}

	public String getAdminPassword() {
		return adminPassword;
	}

	public CleanupAction getCleanupAction() {
		return cleanupAction;
	}

	public void setCleanupAction(CleanupAction cleanupAction) {
		this.cleanupAction = cleanupAction;
	}

	public String getJvmOptions() {
		return jvmOptions;
	}

	public boolean isShutdownOnIdle() {
		return shutdownOnIdle;
	}

	public void setShutdownOnIdle(boolean shutdownOnIdle) {
		this.shutdownOnIdle = shutdownOnIdle;
	}
	public String getPublicDNSName() {
		return publicDNSName;
	}
	
	public void setPublicDNSName(String publicDNSName) {
		this.publicDNSName = publicDNSName;
	}

	public int getSshPort() {
		return sshPort;
	}
	
	public void setSshPort(int sshPort) {
		this.sshPort = sshPort;
	}

	public int getRetentionTimeInMin() {
		return retentionTimeInMin;
	}

	public String getInitScript() {
		return initScript;
	}
	
	public String getSlaveLaunchMethod() {
		return slaveLaunchMethod;
	}
	
	public String getTemplateName() {
		return templateName;
	}

	public void setTemplateName(String templateName) {
		this.templateName = templateName;
	}
	
	protected void _terminate(TaskListener arg0) throws IOException, InterruptedException {
		//TODO: Check when this method is getting called and code accordingly
		LOGGER.info("AzureSlave: _terminate: called for slave "+getNodeName());
	}

	public AbstractCloudComputer<AzureSlave> createComputer() {
		LOGGER.info("AzureSlave: createComputer: start for slave "+this.getDisplayName());
		return new AzureComputer(this);
	}
	
	public void idleTimeout() throws Exception {
		if (shutdownOnIdle) {
            LOGGER.info("AzureSlave: idleTimeout: shutdownOnIdle is true, marking slave for shutdown: " + this.getDisplayName());
            setCleanupAction(CleanupAction.SHUTDOWN);
            this.getComputer().disconnect(OfflineCause.create(Messages._IDLE_TIMEOUT_SHUTDOWN()));
		} else {
			LOGGER.info("AzureSlave: idleTimeout: shutdownOnIdle is false, marking slave for deletion: " + this.getDisplayName());
			setCleanupAction(CleanupAction.TERMINATE);
            this.getComputer().setTemporarilyOffline(true, OfflineCause.create(Messages._Delete_Slave()));
		}
    }
    
    public void cleanup() throws Exception {
        // Clean up the node.  If we are unsuccesful, then we simple skip and continue
        if (getCleanupAction() == CleanupAction.TERMINATE) { 
            LOGGER.info("AzureSlave: cleanup: Terminate called for slave " + this.getDisplayName());
            AzureManagementServiceDelegate.terminateVirtualMachine(this, true);
            LOGGER.info("AzureSlave: cleanup: Slave " + this.getDisplayName() + " succesfully deleted.");
            Jenkins.getInstance().removeNode(this);
        } else if (getCleanupAction() == CleanupAction.SHUTDOWN) {
            // If we aren't to delete it, then we should just shut it down.
            LOGGER.info("AzureSlave: cleanup: Shutdown called for slave " + this.getDisplayName());
            AzureManagementServiceDelegate.shutdownVirtualMachine(this);
            LOGGER.info("AzureSlave: cleanup: Slave " + this.getDisplayName() + " succesfully shutdown.");
        }
        else {
            LOGGER.info("AzureSlave: cleanup: Slave " + this.getDisplayName() + " is offline but not marked for shutdown/deletion.");
        }
    }
	
	public AzureCloud getCloud() {
    	return (AzureCloud) Jenkins.getInstance().getCloud(cloudName);
    }
	
	public boolean isVMAliveOrHealthy() throws Exception {		
		return AzureManagementServiceDelegate.isVMAliveOrHealthy(this);
	}
	
	public boolean isVirtualMachineExists() throws Exception {	
		return AzureManagementServiceDelegate.isVirtualMachineExists(this);
	}
	
	public void setTemplateStatus(String templateStatus, String templateStatusDetails) {
		AzureCloud azureCloud = getCloud();
		AzureSlaveTemplate slaveTemplate = azureCloud.getAzureSlaveTemplate(templateName);
		
		slaveTemplate.handleTemplateStatus(templateStatusDetails, FailureStage.POSTPROVISIONING, this);
	}
	
	public String toString() {
		return "AzureSlave [cloudName=" + cloudName + ", adminUserName="
				+ adminUserName + ", jvmOptions=" + jvmOptions
				+ ", shutdownOnIdle=" + shutdownOnIdle + ", cloudServiceName="
				+ cloudServiceName + ", retentionTimeInMin="
				+ retentionTimeInMin + ", deploymentName=" + deploymentName
				+ ", osType=" + osType + ", publicDNSName=" + publicDNSName
				+ ", sshPort=" + sshPort + ", mode=" + mode
				+ ", subscriptionID=" + subscriptionID + ", passPhrase=" + passPhrase
				+ ", managementURL=" + managementURL + ", cleanupAction="
				+ cleanupAction + "]";
	}

	@Extension
	public static final class AzureSlaveDescriptor extends SlaveDescriptor {

		public String getDisplayName() {
			return Constants.AZURE_SLAVE_DISPLAY_NAME;
		}

		public boolean isInstantiable() {
			return false;
		}
	}
}
