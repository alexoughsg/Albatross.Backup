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

import java.util.List;

import com.cloud.hypervisor.vmware.util.VmwareContext;

import com.vmware.vim25.HostInternetScsiHbaStaticTarget;
import com.vmware.vim25.HostStorageDeviceInfo;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.mo.HostStorageSystem;

public class HostStorageSystemMO extends BaseMO {
    protected HostStorageSystem _hostStorageSystem;
    
	public HostStorageSystemMO(VmwareContext context, ManagedObjectReference morHostDatastore) {
		super(context, morHostDatastore);
		_hostStorageSystem = new HostStorageSystem(context.getServerConnection(), morHostDatastore);
	}

	public HostStorageSystemMO(VmwareContext context, String morType, String morValue) {
		super(context, morType, morValue);
		_hostStorageSystem = new HostStorageSystem(context.getServerConnection(), _mor);
	}
	
	public HostStorageDeviceInfo getStorageDeviceInfo() throws Exception {
		return (HostStorageDeviceInfo)_context.getVimClient().getDynamicProperty(_mor, "storageDeviceInfo");
	}
	
	public void addInternetScsiStaticTargets(String iScsiHbaDevice, List<HostInternetScsiHbaStaticTarget> lstTargets) throws Exception {
		_hostStorageSystem.addInternetScsiStaticTargets(iScsiHbaDevice, lstTargets.toArray(new HostInternetScsiHbaStaticTarget[0]));
	}
	
	public void removeInternetScsiStaticTargets(String iScsiHbaDevice, List<HostInternetScsiHbaStaticTarget> lstTargets) throws Exception {
		_hostStorageSystem.removeInternetScsiStaticTargets(iScsiHbaDevice, lstTargets.toArray(new HostInternetScsiHbaStaticTarget[0]));
	}
	
	public void rescanHba(String iScsiHbaDevice) throws Exception {
		_hostStorageSystem.rescanHba(iScsiHbaDevice);
	}

    public void rescanVmfs() throws Exception {
        _hostStorageSystem.rescanVmfs();
    }
}
