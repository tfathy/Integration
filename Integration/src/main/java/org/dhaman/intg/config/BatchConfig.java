package org.dhaman.intg.config;

import org.dhaman.intg.students.Student;
import org.dhaman.intg.students.StudentRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class BatchConfig {

	@Autowired
	private StudentRepository repository;

	@Autowired
	private JobRepository jobRepository;

	@Autowired
	private PlatformTransactionManager transactionManager;

	@Bean
	public FlatFileItemReader<Student> itemReader() {
		FlatFileItemReader<Student> itemReader = new FlatFileItemReader<>();
		itemReader.setResource(new FileSystemResource("src/main/resources/students.csv"));
		itemReader.setName("csvReader");
		itemReader.setLinesToSkip(1);// do not read from the header titles
		itemReader.setLineMapper(lineMapper());
		return itemReader;

	}

	private LineMapper<Student> lineMapper() {
		DefaultLineMapper<Student> lineMapper = new DefaultLineMapper<>();
		// here comes the challenge . the delimiter
		DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
		lineTokenizer.setDelimiter(",");
		lineTokenizer.setStrict(false);
		lineTokenizer.setNames("id", "firstName", "lastName", "age");
		// now transform each line you take from the line to a Student object
		BeanWrapperFieldSetMapper<Student> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
		fieldSetMapper.setTargetType(Student.class);

		lineMapper.setLineTokenizer(lineTokenizer);
		lineMapper.setFieldSetMapper(fieldSetMapper);

		return lineMapper;

	}

	@Bean
	public StudentProcessor processor() {
		return new StudentProcessor();
	}

	@Bean
	public RepositoryItemWriter<Student> writer() {
		RepositoryItemWriter<Student> writer = new RepositoryItemWriter<>();
		writer.setRepository(repository);
		writer.setMethodName("save");
		return writer;

	}

	@Bean
	public Step importStep() {
		return new StepBuilder("csvImport", jobRepository)
				.<Student, Student>chunk(100, transactionManager)
				.reader(itemReader())
				.processor(processor())
				.writer(writer())
				.taskExecutor(taskExecutor())
				.build();
	}
	
	@Bean
	public Job runJob() {
		return new JobBuilder("importStudents",jobRepository)
				.start(importStep())
				.build();		
	}
	
	// to enhance the performance
	@Bean
	public TaskExecutor taskExecutor() {
		SimpleAsyncTaskExecutor asyncTaskExecutor = new SimpleAsyncTaskExecutor();
		asyncTaskExecutor.setConcurrencyLimit(10);// how many threads you want to run. if you set it to -1 it means the program will run only on one thread. default is -1
		return asyncTaskExecutor;
	}
}
