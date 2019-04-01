// ============================================================================
//
// Talend Community Edition
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
//
// ============================================================================
package routines.system;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.List;
import java.util.Map;

public class JobStructureCatcherUtils {

	public class JobStructureCatcherMessage {
		
		private String component_id;
		
		private String component_name;

		private Map<String, String> component_parameters;

		private List<Map<String, String>> component_schema;

		private String input_connectors;

		private String output_connectors;

		private Map<String, String> connector_name_2_connector_schema;

		private String jobName;
		
		private String jobId;
		
		private String jobVersion;

		private Long systemPid;

		public JobStructureCatcherMessage(String component_id, String component_name, Map<String, String> component_parameters, List<Map<String, String>> component_schema,
				String input_connectors, String output_connectors,
				Map<String, String> connector_name_2_connector_schema, String jobName, String jobId, String jobVersion) {
			this.component_id = component_id;
			this.component_name = component_name;
			this.component_parameters = component_parameters;
			this.component_schema = component_schema;
			this.input_connectors = input_connectors;
			this.output_connectors = output_connectors;
			this.connector_name_2_connector_schema = connector_name_2_connector_schema;

			this.jobName = jobName;
			this.jobVersion = jobVersion;
			this.jobId = jobId;
			this.systemPid = JobStructureCatcherUtils.getPid();
		}
		
		public String getComponent_id() {
			return component_id;
		}

		public void setComponent_id(String component_id) {
			this.component_id = component_id;
		}

		public String getComponent_name() {
			return component_name;
		}

		public void setComponent_name(String component_name) {
			this.component_name = component_name;
		}


		public Map<String, String> getComponent_parameters() {
			return component_parameters;
		}

		public void setComponent_parameters(Map<String, String> component_parameters) {
			this.component_parameters = component_parameters;
		}

		public List<Map<String, String>> getComponent_schema() {
			return component_schema;
		}

		public void setComponent_schema(List<Map<String, String>> component_schema) {
			this.component_schema = component_schema;
		}

		public String getInput_connectors() {
			return input_connectors;
		}

		public void setInput_connectors(String input_connectors) {
			this.input_connectors = input_connectors;
		}

		public String getOutput_connectors() {
			return output_connectors;
		}

		public void setOutput_connectors(String output_connectors) {
			this.output_connectors = output_connectors;
		}

		public Map<String, String> getConnector_name_2_connector_schema() {
			return connector_name_2_connector_schema;
		}

		public void setConnector_name_2_connector_schema(Map<String, String> connector_name_2_connector_schema) {
			this.connector_name_2_connector_schema = connector_name_2_connector_schema;
		}

		public String getJobVersion() {
			return jobVersion;
		}

		public void setJobVersion(String jobVersion) {
			this.jobVersion = jobVersion;
		}

		public String getJobId() {
			return jobId;
		}

		public void setJobId(String jobId) {
			this.jobId = jobId;
		}

		public Long getSystemPid() {
			return systemPid;
		}

		public void setSystemPid(Long systemPid) {
			this.systemPid = systemPid;
		}
		
		public String getJobName() {
			return jobName;
		}

		public void setJobName(String jobName) {
			this.jobName = jobName;
		}

	}

	java.util.List<JobStructureCatcherMessage> messages = java.util.Collections
			.synchronizedList(new java.util.ArrayList<JobStructureCatcherMessage>());
	
	String jobName = "";

	String jobId = ""; 

	String jobVersion = ""; 

	public JobStructureCatcherUtils(String jobName, String jobId, String jobVersion) {
		this.jobName = jobName;
		this.jobId = jobId;
		this.jobVersion = jobVersion;
	}

	public void addMessage(String component_id, String component_name, Map<String, String> component_parameters, List<Map<String, String>> component_schema, String input_connectors,
			String output_connectors, Map<String, String> connector_name_2_connector_schema) {
		JobStructureCatcherMessage scm = new JobStructureCatcherMessage(component_id, component_name, component_parameters, component_schema,
				input_connectors, output_connectors, connector_name_2_connector_schema, this.jobName, this.jobId, this.jobVersion);
		messages.add(scm);
	}

	public java.util.List<JobStructureCatcherMessage> getMessages() {
		java.util.List<JobStructureCatcherMessage> messagesToSend = new java.util.ArrayList<JobStructureCatcherMessage>();
		synchronized (messages) {
			for (JobStructureCatcherMessage scm : messages) {
				messagesToSend.add(scm);
			}
			messages.clear();
		}
		return messagesToSend;
	}

	public static long getPid() {
		RuntimeMXBean mx = ManagementFactory.getRuntimeMXBean();
		String[] mxNameTable = mx.getName().split("@");
		if (mxNameTable.length == 2) {
			return Long.parseLong(mxNameTable[0]);
		} else {
			return Thread.currentThread().getId();
		}
	}
}
