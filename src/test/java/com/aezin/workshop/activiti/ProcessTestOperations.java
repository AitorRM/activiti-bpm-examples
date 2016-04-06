package com.aezin.workshop.activiti;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.ActivitiRule;

/**
 * Clase que engloba las operaciones más comunes usadas en los test.
 * 
 * @author Aitor Rodríguez
 *
 */
public class ProcessTestOperations {

	private ActivitiRule activitiRule;
	
	public ProcessTestOperations(ActivitiRule activitiRule) {
		this.activitiRule = activitiRule;
	}
	
	/**
	 * Inicia un proceso.
	 * @param processKey Key del proceso a iniciar
	 * @param variables mapa de variables
	 * @return processInstanceId del proceso iniciado
	 */
	public String startProcessInstance(String processKey) {
		return startProcessInstance(processKey, null);
	}
	
	/**
	 * Inicia un proceso con un map de variables.
	 * @param processKey Key del proceso a iniciar
	 * @param variables mapa de variables
	 * @return processInstanceId del proceso iniciado
	 */
	public String startProcessInstance(String processKey, Map<String, Object> variables) {
		ProcessInstance processInstance = activitiRule.getRuntimeService()
				.startProcessInstanceByKey(processKey, variables);
		assertNotNull(processInstance.getId());
		System.out.println("Process '" + processInstance.getId() + "' --> Start");
		return processInstance.getId();
	}
	
	/**
	 * Obtiene la tarea de usuario actual. Supone que solo hay una activa (no hay paralelismo)
	 * @param expected Key que define la tarea esperada
	 * @param processInstanceId Identificador del proceso
	 * @return Tarea encontrada
	 */
	public Task getCurrentTask(String expected, String processInstanceId) {
		List<Task> currentTaskList = activitiRule.getTaskService()
				.createTaskQuery()
				.processInstanceId(processInstanceId)
				.list();
		assertNotNull(currentTaskList);
		assertEquals(1, currentTaskList.size());
		assertEquals(expected, currentTaskList.get(0).getTaskDefinitionKey());
		System.out.println("Process '" + processInstanceId + "' --> UserTask: " + currentTaskList.get(0).getTaskDefinitionKey());
		return currentTaskList.get(0);
	}
	
	/**
	 * Comprueba si el proceso esta finalizado y si el(los) evento(s) de
	 * finalización son los esperados.
	 * @param expectedEndEvent Evento(s) de finalización esperados. Si son varios deben estar separados por un pipe "|".
	 * @param processInstanceId Identificador del proceso a evaluar
	 */
	public void checkEndProcess(String expectedEndEvent, String processInstanceId) {
		// Comprobar que el proceso está finalizado
		HistoricProcessInstance historicProcessInstance = activitiRule.getHistoryService()
			.createHistoricProcessInstanceQuery()
			.processInstanceId(processInstanceId)
			.singleResult();
		assertNotNull(historicProcessInstance);
		assertNotNull(historicProcessInstance.getEndTime());
		
		// Obtener eventos de finalización 
		List<HistoricActivityInstance> listEndEvents = activitiRule.getHistoryService()
			.createHistoricActivityInstanceQuery()
			.processInstanceId(processInstanceId)
			.activityType("endEvent").list();
		assertNotNull(listEndEvents);
		
		List<String> endEventNameList = new ArrayList<String>();
		for (HistoricActivityInstance hcoActInst : listEndEvents) {
			endEventNameList.add(hcoActInst.getActivityId());
		}
		
		// Obtener eventos de finalización esperados
		if (expectedEndEvent != null) {
			List<String> expectedEndEventList = Arrays.asList(expectedEndEvent.split("\\|"));
			assertEquals(listEndEvents.size(), expectedEndEventList.size());
			
			// Comparar todos los eventos esperados y obtenidos
			boolean allExpectedEndEventExist = true;
			for (String eventName : endEventNameList) {
				if (!expectedEndEventList.contains(eventName)) {
					allExpectedEndEventExist = false;
					break;
				}
			}
			assertTrue(allExpectedEndEventExist);
		}
		
		System.out.println("Process '" + processInstanceId + "' --> Finish: " + endEventNameList);
	}
}
