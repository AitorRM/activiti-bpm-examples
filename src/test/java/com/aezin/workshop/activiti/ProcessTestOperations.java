package com.aezin.workshop.activiti;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.ProcessEngineImpl;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.ActivitiRule;

/**
 * Clase que engloba las operaciones m�s comunes usadas en los test.
 * 
 * @author Aitor Rodr�guez
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
	 * finalizaci�n son los esperados.
	 * @param expectedEndEvent Evento(s) de finalizaci�n esperados. Si son varios deben estar separados por un pipe "|".
	 * @param processInstanceId Identificador del proceso a evaluar
	 */
	public void checkEndProcess(String expectedEndEvent, String processInstanceId) {
		// Comprobar que el proceso est� finalizado
		HistoricProcessInstance historicProcessInstance = activitiRule.getHistoryService()
			.createHistoricProcessInstanceQuery()
			.processInstanceId(processInstanceId)
			.singleResult();
		assertNotNull(historicProcessInstance);
		assertNotNull(historicProcessInstance.getEndTime());
		
		// Obtener eventos de finalizaci�n 
		List<HistoricActivityInstance> listEndEvents = activitiRule.getHistoryService()
			.createHistoricActivityInstanceQuery()
			.processInstanceId(processInstanceId)
			.activityType("endEvent").list();
		assertNotNull(listEndEvents);
		
		List<String> endEventNameList = new ArrayList<String>();
		for (HistoricActivityInstance hcoActInst : listEndEvents) {
			endEventNameList.add(hcoActInst.getActivityId());
		}
		
		// Obtener eventos de finalizaci�n esperados
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
	
	public void startJobExecutor() {
		((ProcessEngineImpl) activitiRule.getProcessEngine())
				.getProcessEngineConfiguration()
				.getJobExecutor()
				.start();
	}
	
	public void shutdownJobExecutor() {
		((ProcessEngineImpl) activitiRule.getProcessEngine())
				.getProcessEngineConfiguration()
				.getJobExecutor()
				.shutdown();
	}
	
	/**
	 * Comprueba si hay trabajos en paralelo pendientes de ejecutar.
	 * @return true si hay trabajos en paralelo pendientes de ejecutar
	 */
	public boolean areJobsAvailable() {
		return !activitiRule.getProcessEngine()
				.getManagementService()
				.createJobQuery()
				.executable()
				.list().isEmpty();
	}
	
	/**
	 * Espera hasta que se finalicen los trabajos en paralelo durante un tiempo máximo (maxMillisToWait) y 
	 * realizando las consultas cada cierto intervalo de tiempo (intervalMillis)
	 * @param maxMillisToWait tiempo máximo de espera
	 * @param intervalMillis intervalo de tiempo en el que ejecutar la consulta de trabajos pendientes
	 * @return
	 */
	public boolean waitForJobExecutorToProcessAllJobs(long maxMillisToWait, long intervalMillis) {
		Timer timer = new Timer();
		InteruptTask task = new InteruptTask(Thread.currentThread());
		timer.schedule(task, maxMillisToWait);
		boolean areJobsAvailable = true;
		try {
			while (areJobsAvailable && !task.isTimeLimitExceeded()) {
				Thread.sleep(intervalMillis);
				areJobsAvailable = areJobsAvailable();
			}
		} catch (InterruptedException e) {
		} finally {
			timer.cancel();
		}
		return areJobsAvailable;
	}
	
	/**
	 * Clase TimerTask para ser usada en un Timer que permite controlar la ejecución de un Thread
	 * @author Aitor Rodríguez
	 */
	private static class InteruptTask extends TimerTask {
		protected boolean timeLimitExceeded = false;
		protected Thread thread;

		public InteruptTask(Thread thread) {
			this.thread = thread;
		}

		public boolean isTimeLimitExceeded() {
			return timeLimitExceeded;
		}

		public void run() {
			timeLimitExceeded = true;
			thread.interrupt();
		}
	}
}
