package com.aezin.workshop.activiti.asyncsendreceive;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.runtime.Execution;

public class RequesterService implements JavaDelegate {
	
	public void execute(DelegateExecution execution) throws Exception {
		switch (execution.getCurrentActivityId()) {
			case "servicetask_req_send_request":
				String responserProcessId = (String) execution.getVariable("responser_process");
				
				System.out.println("Process '" + execution.getProcessInstanceId() + "', "
						+ "ServiceTask [" + execution.getCurrentActivityName() + "] "
						+ "--> Sending request to process '" + responserProcessId + "'");
				
				Execution executionResponser = execution.getEngineServices()
						.getRuntimeService()
						.createExecutionQuery()
						.processInstanceId(responserProcessId)
						.activityId("receivetask_res_receive_request")
						.singleResult();

				if (executionResponser != null) {
					Map<String, Object> variableMap = new HashMap<String, Object>();
					variableMap.put("requester_process", execution.getProcessInstanceId());
					execution.getEngineServices()
							.getRuntimeService()
							.signal(executionResponser.getId(), variableMap);
					
					System.out.println("Process '" + execution.getProcessInstanceId() + "', "
							+ "ServiceTask [" + execution.getCurrentActivityName() + "] "
							+ "--> Sent request!!");
				} else {
					System.out.println("Process '" + execution.getProcessInstanceId() + "', "
							+ "ServiceTask [" + execution.getCurrentActivityName() + "] "
							+ "--> Unsent request. No process listening events.");
				}
				break;
				
			case "servicetask_req_manage_response":
				String response = (String) execution.getVariable("response");
				System.out.println("Process '" + execution.getProcessInstanceId() + "', "
						+ "ServiceTask [" + execution.getCurrentActivityName() + "] "
						+ "--> Receive response '" + response + "'");
				break;
		}
	}

}
