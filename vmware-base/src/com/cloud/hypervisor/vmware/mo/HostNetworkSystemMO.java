// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
package com.cloud.hypervisor.vmware.mo;

import org.apache.log4j.Logger;

import com.cloud.hypervisor.vmware.util.VmwareContext;

import com.vmware.vim25.HostPortGroupSpec;
import com.vmware.vim25.HostVirtualSwitchSpec;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.mo.HostNetworkSystem;

public class HostNetworkSystemMO extends BaseMO {
    private static final Logger s_logger = Logger.getLogger(HostNetworkSystemMO.class);
    protected HostNetworkSystem _hostNetworkSystem;
    
	public HostNetworkSystemMO(VmwareContext context, ManagedObjectReference morNetworkSystem) {
		super(context, morNetworkSystem);
		_hostNetworkSystem = new HostNetworkSystem(context.getServerConnection(), morNetworkSystem);
	}
	
	public HostNetworkSystemMO(VmwareContext context, String morType, String morValue) {
		super(context, morType, morValue);
		_hostNetworkSystem = new HostNetworkSystem(context.getServerConnection(), _mor);
	}
	
	public void addPortGroup(HostPortGroupSpec spec) throws Exception {
		_hostNetworkSystem.addPortGroup(spec);
	}
	
	public void updatePortGroup(String portGroupName, HostPortGroupSpec spec) throws Exception {
		_hostNetworkSystem.updatePortGroup(portGroupName, spec);
	}
	
	public void removePortGroup(String portGroupName) throws Exception {
		_hostNetworkSystem.removePortGroup(portGroupName);
	}
	
	public void addVirtualSwitch(String vSwitchName, HostVirtualSwitchSpec spec) throws Exception {
		_hostNetworkSystem.addVirtualSwitch(vSwitchName, spec);
	}
	
	public void updateVirtualSwitch(String vSwitchName, HostVirtualSwitchSpec spec) throws Exception {
		_hostNetworkSystem.updateVirtualSwitch(vSwitchName, spec);
	}
	
	public void removeVirtualSwitch(String vSwitchName) throws Exception {
		_hostNetworkSystem.removeVirtualSwitch(vSwitchName);
	}
	
	public void refresh() throws Exception {
		_hostNetworkSystem.refreshNetworkSystem();
	}
}

