package org.dhaman.intg.config;

import org.dhaman.intg.students.Student;
import org.springframework.batch.item.ItemProcessor;

public class StudentProcessor implements ItemProcessor<Student,Student>{

	@Override
	public Student process(Student student) throws Exception {
		// all the buiness here. to tranform data
		return student;
	}
	
	

}
