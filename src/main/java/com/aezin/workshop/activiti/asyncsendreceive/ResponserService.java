package com.aezin.workshop.activiti.asyncsendreceive;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.runtime.Execution;

public class ResponserService implements JavaDelegate {
	
	public void execute(DelegateExecution execution) throws Exception {
		switch (execution.getCurrentActivityId()) {
			case "servicetask_res_evaluate_request":
				String evalRequesterProcess = (String) execution.getVariable("requester_process");
				System.out.println("Process '" + execution.getProcessInstanceId() + "', "
						+ "ServiceTask [" + execution.getCurrentActivityName() + "] "
						+ "--> Evaluating request from process '" + evalRequesterProcess + "'");
				break;
				
			case "servicetask_res_send_response":
				String requesterProcess = (String) execution.getVariable("requester_process");
				
				System.out.println("Process '" + execution.getProcessInstanceId() + "', "
						+ "ServiceTask [" + execution.getCurrentActivityName() + "] "
						+ "--> Sending response to process '" + requesterProcess + "'");
				
				Execution executionRequester = execution.getEngineServices()
						.getRuntimeService()
						.createExecutionQuery()
						.processInstanceId(requesterProcess)
						.activityId("receivetask_req_receive_response")
						.singleResult();

				if (executionRequester != null) {
					Map<String, Object> variableMap = new HashMap<String, Object>();
					variableMap.put("response", "ACK");
					execution.getEngineServices()
							.getRuntimeService()
							.signal(executionRequester.getId(), variableMap);
					
					System.out.println("Process '" + execution.getProcessInstanceId() + "', "
							+ "ServiceTask [" + execution.getCurrentActivityName() + "] "
							+ "--> Sent response!!");
				} else {
					System.out.println("Process '" + execution.getProcessInstanceId() + "', "
							+ "ServiceTask [" + execution.getCurrentActivityName() + "] "
							+ "--> Unsent response. No process listening events.");
				}
				break;
		}
	}

}
