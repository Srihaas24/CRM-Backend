package com.crm.BackendApp.conroller;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.crm.BackendApp.dto.request.AddTaskRequest;
import com.crm.BackendApp.dto.request.UpdateTaskRequest;
import com.crm.BackendApp.dto.response.AddTaskResponse;
import com.crm.BackendApp.dto.response.GetTaskDetails;
import com.crm.BackendApp.enums.TaskStatus;
import com.crm.BackendApp.security.CustomUserDetails;
import com.crm.BackendApp.service.TaskService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
public class TaskController 
{
	private final TaskService taskService;
	
	@PostMapping
	@PreAuthorize("hasAnyRole('ADMIN','PROJECT_MANAGER')")
	public ResponseEntity<AddTaskResponse> addTasks(
			@Valid @RequestBody AddTaskRequest addTaskRequest, 
			@AuthenticationPrincipal CustomUserDetails loggedInUser)
	{
		
		AddTaskResponse addTaskResponse = taskService.addTask(addTaskRequest, loggedInUser);
		return ResponseEntity.status(HttpStatus.CREATED).body(addTaskResponse);
	}
	
	@GetMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'DEVELOPER')")
	public ResponseEntity<GetTaskDetails> getTask(
			@PathVariable("id") Long taskId,
			@AuthenticationPrincipal CustomUserDetails loggedInUser)
	{
		GetTaskDetails getTaskDetails = taskService.getTaskById(taskId, loggedInUser);
		
		return ResponseEntity.ok(getTaskDetails);
	}
	
	@GetMapping
	@PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'DEVELOPER')")
	public ResponseEntity<Page<GetTaskDetails>> getTasks(
			@AuthenticationPrincipal CustomUserDetails loggedInUser,
			@RequestParam(required= false) Long projectId,
			@RequestParam(required = false) Long assignedUserId,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(defaultValue="0") int page,
            @RequestParam(defaultValue="10") int size,
            @RequestParam(defaultValue="id") String sortBy,
            @RequestParam(defaultValue="desc") String sortDir)
	{
		Page<GetTaskDetails> tasks = taskService.getTasks(projectId, assignedUserId, status, page, size, sortBy, sortDir, loggedInUser);
		
		return ResponseEntity.ok(tasks);
	}
	
	@PutMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'DEVELOPER')")
	public ResponseEntity<GetTaskDetails> updateTasks(
			@AuthenticationPrincipal CustomUserDetails loggedInUser,
			@PathVariable("id") Long id,
			@RequestBody UpdateTaskRequest updateTaskRequest)
	{
		GetTaskDetails updatedTask = taskService.updateTask(loggedInUser, id, updateTaskRequest);
		
		return ResponseEntity.ok(updatedTask);
	}
}
