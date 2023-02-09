//package bgu.spl.mics;
//
//import bgu.spl.mics.application.objects.*;
//import bgu.spl.mics.application.services.*;
//
//import java.io.IOException;
//import java.util.Vector;
//
///** This is the Main class of Compute Resources Management System application. You should parse the input file,
// * create the different instances of the objects, and run the system.
// * In the end, you should output a text file.
// */
////public class CRMSRunner {
//public class main {
//    public static void main(String[] args) throws InterruptedException {
//        Vector<Thread> programThreads = new Vector<>();
//        Input input = new Input();
//        try {
//            input = JasonInputReader.getInputFromJson(args[0]);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        for (int i = 0; i < input.getCPUS().length ; i++){
//            CPUService cpuService = new CPUService(new CPU(input.getCPUS()[i]));
//            Thread thread = new Thread(cpuService);
//            programThreads.add(thread);
//            thread.start();
//        }
//        for (int i = 0; i < input.getGPUS().length; i++) {
//            GPUService gpuService = new GPUService("gpu "+i, new GPU(input.getGPUS()[i]));
//            Thread thread = new Thread(gpuService);
//            programThreads.add(thread);
//            thread.start();
//        }
//        ConfrenceInformation [] conferencesForOutput = new ConfrenceInformation[input.getConferences().length];
//        for (int i = 0; i < input.getConferences().length; i++) {
//            conferencesForOutput[i] = new ConfrenceInformation(input.getConferences()[i].getName(),input.getConferences()[i].getDate());
//            ConferenceService conferenceService = new ConferenceService("conf "+i ,conferencesForOutput[i] );
//            Thread thread = new Thread(conferenceService);
//            programThreads.add(thread);
//            thread.start();
//        }
//        Student [] studentsForOutput = new Student[input.getStudents().length];
//        for (int i = 0; i < input.getStudents().length; i++) {
//            studentsForOutput[i] = new Student(input.getStudents()[i].getName()
//                    ,input.getStudents()[i].getDepartment(),input.getStudents()[i].getStatus()
//                    ,input.getStudents()[i].getMyModels());
//            for (Model m: studentsForOutput[i].getMyModels()) {
//                m.setStudent(studentsForOutput[i]);
//            }
//            StudentService studentService = new StudentService(studentsForOutput[i]);
//            Thread thread = new Thread(studentService);
//            programThreads.add(thread);
//            thread.start();
//        }
//        TimeService timeService = new TimeService(input.getTickTime(),input.getDuration());
//        Thread timeThread = new Thread(timeService);
//        programThreads.add(timeThread);
//        timeThread.start();
//
//        for (Thread t : programThreads) {
//            try {
//                t.join();
//            } catch (InterruptedException e) {}
//        }        //write output file
//    }
//}