/*
 Copyright 2016 Microsoft Open Technologies, Inc.
 
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
package com.microsoftopentechnologies.azure.util;

import com.microsoftopentechnologies.azure.AzureCloud;
import hudson.Extension;
import hudson.model.AdministrativeMonitor;
import hudson.model.Hudson;

@Extension
public class UpgradeNotifier extends AdministrativeMonitor{

    private enum MessageType {
        NONE, UPGRADE, MIGRATE
    }

    private MessageType showMsgType = MessageType.NONE;

    public boolean isActivated() {
        showMsgType = MessageType.NONE;
        // we should show the upgrade message only if the Azure VM Agents plugin is not already installed
        if(Hudson.getInstance().pluginManager.getPlugin(Constants.NEW_PLUGIN_NAME) == null) {
            showMsgType = MessageType.UPGRADE;
        }
        else {
            //if the user has Azure clouds defined we should show a migration message. Otherwise we shouldn't print anything
            if(!Hudson.getInstance().clouds.getAll(AzureCloud.class).isEmpty()) {
                showMsgType = MessageType.MIGRATE;
            }
        }
        return showMsgType != MessageType.NONE;
    }

    public boolean getShouldShowUpgradeMsg() {
        return showMsgType == MessageType.UPGRADE;
    }

    public boolean getShouldShowMigrateMsg() {
        return showMsgType == MessageType.MIGRATE;
    }
}
