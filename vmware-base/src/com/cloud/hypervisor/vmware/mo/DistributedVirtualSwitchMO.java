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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.cloud.hypervisor.vmware.util.VmwareContext;

import com.vmware.vim25.DVPortgroupConfigSpec;
import com.vmware.vim25.DVSConfigInfo;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.TaskInfo;
import com.vmware.vim25.VMwareDVSConfigInfo;
import com.vmware.vim25.VMwareDVSConfigSpec;
import com.vmware.vim25.VMwareDVSPvlanMapEntry;
import com.vmware.vim25.mo.DistributedVirtualPortgroup;
import com.vmware.vim25.mo.DistributedVirtualSwitch;
import com.vmware.vim25.mo.Task;

public class DistributedVirtualSwitchMO extends BaseMO {
    private static final Logger s_logger = Logger.getLogger(DistributedVirtualSwitchMO.class);

    protected DistributedVirtualSwitch _distributedVirtualSwitch;
    
    public DistributedVirtualSwitchMO(VmwareContext context, ManagedObjectReference morDvs) {
        super(context, morDvs);
        _distributedVirtualSwitch = (DistributedVirtualSwitch) this.getManagedEntity();
    }

    public DistributedVirtualSwitchMO(VmwareContext context, String morType, String morValue) {
        super(context, morType, morValue);
        _distributedVirtualSwitch = (DistributedVirtualSwitch) this.getManagedEntity();
    }

    public void createDVPortGroup(DVPortgroupConfigSpec dvPortGroupSpec) throws Exception {
        List<DVPortgroupConfigSpec> dvPortGroupSpecArray = new ArrayList<DVPortgroupConfigSpec>();
        dvPortGroupSpecArray.add(dvPortGroupSpec);
        _distributedVirtualSwitch.addDVPortgroup_Task(dvPortGroupSpecArray.toArray(new DVPortgroupConfigSpec[0]));
    }

    public void updateDvPortGroup(ManagedObjectReference dvPortGroupMor, DVPortgroupConfigSpec dvPortGroupSpec) throws Exception {
        // TODO(sateesh): Update numPorts
        DistributedVirtualPortgroup dvPortGroup = new DistributedVirtualPortgroup(_context.getServerConnection(), dvPortGroupMor);
        dvPortGroup.reconfigureDVPortgroup_Task(dvPortGroupSpec);
    }

    public void updateVMWareDVSwitch(ManagedObjectReference dvSwitchMor, VMwareDVSConfigSpec dvsSpec) throws Exception {
        // FIXME Why pass in a dvSwitchMor here?
        DistributedVirtualSwitch dvSwitch = new DistributedVirtualSwitch(_context.getServerConnection(), dvSwitchMor);
        dvSwitch.reconfigureDvs_Task(dvsSpec);
    }

    public TaskInfo updateVMWareDVSwitchGetTask(ManagedObjectReference dvSwitchMor, VMwareDVSConfigSpec dvsSpec) throws Exception {
        // FIXME Why pass in a dvSwitchMor here?
        DistributedVirtualSwitch dvSwitch = new DistributedVirtualSwitch(_context.getServerConnection(), dvSwitchMor);
        Task task = dvSwitch.reconfigureDvs_Task(dvsSpec);
        TaskInfo info = (TaskInfo) (_context.getVimClient().getDynamicProperty(task.getMOR(), "info"));
        _context.getVimClient().waitForTask(task.getMOR());
        return info;
    }

    public String getDVSConfigVersion(ManagedObjectReference dvSwitchMor) throws Exception {
        assert (dvSwitchMor != null);
        DVSConfigInfo dvsConfigInfo = (DVSConfigInfo)_context.getVimClient().getDynamicProperty(dvSwitchMor, "config");
        return dvsConfigInfo.getConfigVersion();
    }

    public Map<Integer, HypervisorHostHelper.PvlanType> retrieveVlanPvlan(int vlanid, int secondaryvlanid, ManagedObjectReference dvSwitchMor) throws Exception {
        assert (dvSwitchMor != null);

        Map<Integer, HypervisorHostHelper.PvlanType> result = new HashMap<Integer, HypervisorHostHelper.PvlanType>();

        VMwareDVSConfigInfo configinfo = (VMwareDVSConfigInfo)_context.getVimClient().getDynamicProperty(dvSwitchMor, "config");
        VMwareDVSPvlanMapEntry[] pvlanconfig = configinfo.getPvlanConfig();

        if (null == pvlanconfig || 0 == pvlanconfig.length) {
            return result;
        }
        // Iterate through the pvlanMapList and check if the specified vlan id and pvlan id exist. If they do, set the fields in result accordingly.

        for (VMwareDVSPvlanMapEntry mapEntry : pvlanconfig) {
            int entryVlanid = mapEntry.getPrimaryVlanId();
            int entryPvlanid = mapEntry.getSecondaryVlanId();
            if (entryVlanid == entryPvlanid) {
                // promiscuous
                if (vlanid == entryVlanid) {
                    // pvlan type will always be promiscuous in this case.
                    result.put(vlanid, HypervisorHostHelper.PvlanType.valueOf(mapEntry.getPvlanType()));
                } else if ((vlanid != secondaryvlanid) && secondaryvlanid == entryVlanid) {
                    result.put(secondaryvlanid, HypervisorHostHelper.PvlanType.valueOf(mapEntry.getPvlanType()));
                }
            } else {
                if (vlanid == entryVlanid) {
                    // vlan id in entry is promiscuous
                    result.put(vlanid, HypervisorHostHelper.PvlanType.promiscuous);
                } else if (vlanid == entryPvlanid) {
                    result.put(vlanid, HypervisorHostHelper.PvlanType.valueOf(mapEntry.getPvlanType()));
                }
                if ((vlanid != secondaryvlanid) && secondaryvlanid == entryVlanid) {
                    //promiscuous
                    result.put(secondaryvlanid, HypervisorHostHelper.PvlanType.promiscuous);
                } else if (secondaryvlanid == entryPvlanid) {
                    result.put(secondaryvlanid, HypervisorHostHelper.PvlanType.valueOf(mapEntry.getPvlanType()));
                }

            }
            // If we already know that the vlanid is being used as a non primary vlan, it's futile to
            // go over the entire list. Return.
            if (result.containsKey(vlanid) && result.get(vlanid) != HypervisorHostHelper.PvlanType.promiscuous)
                return result;

            // If we've already found both vlanid and pvlanid, we have enough info to make a decision. Return.
            if (result.containsKey(vlanid) && result.containsKey(secondaryvlanid))
                return result;
        }
        return result;
    }

}
