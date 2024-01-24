package org.dhaman.intg.controller;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

// in real project , we use scdualed task , not controller
@RestController
@RequestMapping("/students")
@RequiredArgsConstructor
public class StudentController {

	@Autowired
	private JobLauncher jobLuncher;

	@Autowired
	private Job job;

	@PostMapping
	public void importCsvToDbJob() {
		JobParameters jobParameters = new JobParametersBuilder().addLong("startAt", System.currentTimeMillis())
				.toJobParameters();
		try {
			jobLuncher.run(job, jobParameters);
		} catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException
				| JobParametersInvalidException e) {			
			e.printStackTrace();
		}
	}

}
