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

import java.util.Calendar;
import java.util.List;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import com.cloud.hypervisor.vmware.util.VmwareContext;

import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.PerfCompositeMetric;
import com.vmware.vim25.PerfCounterInfo;
import com.vmware.vim25.PerfEntityMetricBase;
import com.vmware.vim25.PerfInterval;
import com.vmware.vim25.PerfMetricId;
import com.vmware.vim25.PerfProviderSummary;
import com.vmware.vim25.PerfQuerySpec;
import com.vmware.vim25.mo.ManagedEntity;

public class PerfManagerMO extends BaseMO {
    
    public PerfManagerMO(VmwareContext context, ManagedObjectReference mor) {
        super(context, mor);
    }

    public PerfManagerMO(VmwareContext context, String morType, String morValue) {
        super(context, morType, morValue);
    }

    public void createPerfInterval(PerfInterval interval) throws Exception {
        _context.getPerformanceManager().createPerfInterval(interval);
    }

    public PerfMetricId[] queryAvailablePerfMetric(ManagedObjectReference morEntity, Calendar beginTime,
        Calendar endTime, Integer intervalId) throws Exception {

        ManagedEntity me = new ManagedEntity(_context.getServerConnection(), morEntity);
        return _context.getPerformanceManager().queryAvailablePerfMetric(me, beginTime,
                endTime, intervalId);
    }

    public PerfCompositeMetric queryPerfComposite(PerfQuerySpec spec) throws Exception {
        return _context.getPerformanceManager().queryPerfComposite(spec);
    }

    public PerfCounterInfo[] queryPerfCounter(int[] counterId) throws Exception {
        return _context.getPerformanceManager().queryPerfCounter(counterId);
    }

    public PerfCounterInfo[] queryPerfCounterByLevel(int level) throws Exception {
        return _context.getPerformanceManager().queryPerfCounterByLevel(level);
    }

    public PerfProviderSummary queryPerfProviderSummary(ManagedObjectReference morEntity) throws Exception {
        ManagedEntity me = new ManagedEntity(_context.getServerConnection(), morEntity);
        return _context.getPerformanceManager().queryPerfProviderSummary(me);
    }

    public PerfEntityMetricBase[] queryPerf(PerfQuerySpec[] specs) throws Exception {
        return _context.getPerformanceManager().queryPerf(specs);
    }

    public void removePerfInterval(int samplePeriod) throws Exception {
        _context.getPerformanceManager().removePerfInterval(samplePeriod);
    }

    public void updatePerfInterval(PerfInterval interval) throws Exception {
        _context.getPerformanceManager().updatePerfInterval(interval);
    }

    public List<PerfCounterInfo> getCounterInfo() throws Exception {
        return (List<PerfCounterInfo>)_context.getVimClient().getDynamicProperty(_mor, "perfCounter");
    }

    public List<PerfInterval> getIntervalInfo() throws Exception {
        return (List<PerfInterval>)_context.getVimClient().getDynamicProperty(_mor, "historicalInterval");
    }
}
