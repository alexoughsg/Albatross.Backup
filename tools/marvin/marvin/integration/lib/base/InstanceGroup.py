# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
from marvin.integration.lib.base import CloudStackEntity
from marvin.cloudstackAPI import createInstanceGroup
from marvin.cloudstackAPI import listInstanceGroups
from marvin.cloudstackAPI import updateInstanceGroup
from marvin.cloudstackAPI import deleteInstanceGroup

class InstanceGroup(CloudStackEntity.CloudStackEntity):


    def __init__(self, **kwargs):
        self.__dict__.update(**kwargs)


    @classmethod
    def create(cls, apiclient, InstanceGroupFactory, **kwargs):
        cmd = createInstanceGroup.createInstanceGroupCmd()
        [setattr(cmd, factoryKey, factoryValue) for factoryKey, factoryValue in InstanceGroupFactory.__dict__.iteritems()]
        [setattr(cmd, key, value) for key,value in kwargs.iteritems()]
        instancegroup = apiclient.createInstanceGroup(cmd)
        return InstanceGroup(instancegroup.__dict__)


    @classmethod
    def list(self, apiclient, **kwargs):
        cmd = listInstanceGroups.listInstanceGroupsCmd()
        [setattr(cmd, key, value) for key,value in kwargs.items]
        instancegroup = apiclient.listInstanceGroups(cmd)
        return map(lambda e: InstanceGroup(e.__dict__), instancegroup)


    def update(self, apiclient, id, **kwargs):
        cmd = updateInstanceGroup.updateInstanceGroupCmd()
        cmd.id = id
        [setattr(cmd, key, value) for key,value in kwargs.items]
        instancegroup = apiclient.updateInstanceGroup(cmd)


    def delete(self, apiclient, id, **kwargs):
        cmd = deleteInstanceGroup.deleteInstanceGroupCmd()
        cmd.id = id
        [setattr(cmd, key, value) for key,value in kwargs.items]
        instancegroup = apiclient.deleteInstanceGroup(cmd)