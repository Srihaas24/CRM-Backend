package com.crm.BackendApp.service;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.crm.BackendApp.dto.request.AddTaskRequest;
import com.crm.BackendApp.dto.request.UpdateTaskRequest;
import com.crm.BackendApp.dto.response.AddTaskResponse;
import com.crm.BackendApp.dto.response.GetTaskDetails;
import com.crm.BackendApp.entity.Project;
import com.crm.BackendApp.entity.Task;
import com.crm.BackendApp.entity.User;
import com.crm.BackendApp.enums.TaskStatus;
import com.crm.BackendApp.exception.ResourceNotFoundException;
import com.crm.BackendApp.exception.BadRequestException;
import com.crm.BackendApp.enums.UserStatus;
import com.crm.BackendApp.repo.ProjectRepo;
import com.crm.BackendApp.repo.TaskRepo;
import com.crm.BackendApp.repo.UserRepo;
import com.crm.BackendApp.security.CustomUserDetails;
import com.crm.BackendApp.specification.TaskSpecification;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TaskService 
{
	private final TaskRepo taskRepo;
	private final ProjectRepo projectRepo; 
	private final UserRepo userRepo;
	
	@Transactional
	public AddTaskResponse addTask(@Valid AddTaskRequest addTaskRequest, CustomUserDetails loggedInUser)
	{
		Long orgId = loggedInUser.getUser().getOrganization().getId();
		
		Project project = projectRepo.findByOrganizationIdAndId(orgId, addTaskRequest.getProjectId())
				.orElseThrow(() -> new ResourceNotFoundException("Project not found"));
		
		User user = userRepo.findByIdAndOrganizationId(addTaskRequest.getAssignedToUserId(), orgId)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));
		
		if (user.getUserStatus() == UserStatus.DEACTIVATED || user.getUserStatus() == UserStatus.INACTIVE) {
			throw new BadRequestException("Cannot assign task to a deactivated user");
		}
		
		Task task = Task.builder()
				.title(addTaskRequest.getTitle())
				.description(addTaskRequest.getDescription())
				.taskStatus(TaskStatus.TODO)
				.taskPriority(addTaskRequest.getTaskPriority())
				.dueDate(addTaskRequest.getDueDate())
				.project(project)
				.assignedTo(user)
				.assignedBy(loggedInUser.getUser())
				.build();
		
		Task saved = taskRepo.save(task);
		
		return AddTaskResponse.builder()
				.id(saved.getId())
				.title(saved.getTitle())
				.description(saved.getDescription())
				.taskPriority(saved.getTaskPriority())
				.taskStatus(saved.getTaskStatus())
				.dueDate(saved.getDueDate())
				.projectId(saved.getProject().getId())
				.projectName(saved.getProject().getProjectName())
				.assignedToUserId(saved.getAssignedTo().getId())
				.assignedToUserName(saved.getAssignedTo().getName())
				.build();
	}
	
	@Cacheable(value = "tasks", key = "#loggedInUser.user.organization.id + ':' + #taskId")
	public GetTaskDetails getTaskById(Long taskId, CustomUserDetails loggedInUser)
	{
		Long orgId = loggedInUser.getUser().getOrganization().getId();
		
		Task task = taskRepo.findByIdAndProjectOrganizationId(taskId, orgId)
				.orElseThrow(() -> new ResourceNotFoundException("Task not found"));
		
		return this.mapToGetTaskDetails(task);
	}
	
	public Page<GetTaskDetails> getTasks(Long projectId, Long assignedUserId, TaskStatus status, int page, int size, String sortBy, String sortDir, CustomUserDetails loggedInUser)
	{	
		User user = loggedInUser.getUser();
		
		Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
		
		Pageable pageable = PageRequest.of(page, size, sort);
		
		Specification<Task> specification = TaskSpecification.taskFilter(user,projectId, assignedUserId, status);
		
		Page<Task> taskPage = taskRepo.findAll(specification, pageable);
		
		return taskPage.map(task -> mapToGetTaskDetails(task));
	}
	
	private GetTaskDetails mapToGetTaskDetails(Task task)
	{
		return GetTaskDetails.builder()
				.id(task.getId())
				.title(task.getTitle())
				.description(task.getDescription())
				.taskStatus(task.getTaskStatus())
				.taskPriority(task.getTaskPriority())
				.dueDate(task.getDueDate())
				.projectId(task.getProject().getId())
				.projectName(task.getProject().getProjectName())
				.assignedToUserId(task.getAssignedTo().getId())
				.assignedToUserName(task.getAssignedTo().getName())
				.assignedToUserEmail(task.getAssignedTo().getEmail())
				.build();		
	}

	@Transactional
	@CacheEvict(value = "tasks", key = "#loggedInUser.user.organization.id + ':' + #taskId")
	public GetTaskDetails updateTask(CustomUserDetails loggedInUser, Long taskId, UpdateTaskRequest updateTaskRequest) 
	{
		User currentUser = loggedInUser.getUser();
		
		Task task = taskRepo.findByIdAndProjectOrganizationId(taskId, currentUser.getOrganization().getId())
				.orElseThrow(() -> new ResourceNotFoundException("Task not found"));
		
		if(updateTaskRequest.getAssignedToUserId() != null)
		{
			User user = userRepo.findByIdAndOrganizationId(updateTaskRequest.getAssignedToUserId(), currentUser.getOrganization().getId())
					.orElseThrow(() -> new ResourceNotFoundException("User not found"));
			if (user.getUserStatus() == UserStatus.DEACTIVATED || user.getUserStatus() == UserStatus.INACTIVE) {
				throw new BadRequestException("Cannot assign task to a deactivated user");
			}
			task.setAssignedTo(user);
		}
		
		if(updateTaskRequest.getTitle()!=null)
		{
			task.setTitle(updateTaskRequest.getTitle());
		}
		
		if(updateTaskRequest.getDescription()!=null)
		{
			task.setDescription(updateTaskRequest.getDescription());
		}
		if(updateTaskRequest.getDueDate()!=null)
		{
			task.setDueDate(updateTaskRequest.getDueDate());
		}
		if(updateTaskRequest.getTaskStatus()!=null)
		{
			task.setTaskStatus(updateTaskRequest.getTaskStatus());
		}
		if(updateTaskRequest.getTaskPriority()!=null)
		{
			task.setTaskPriority(updateTaskRequest.getTaskPriority());
		}
		
		Task updatedTask = taskRepo.save(task);
		 return this.mapToGetTaskDetails(updatedTask);
	}
}
