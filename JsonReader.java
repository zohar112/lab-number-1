package bgu.spl.mics.application.objects;

import bgu.spl.mics.application.services.TimeService;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;

public class JsonReader {
    LinkedList<Student> Students;
    Cluster cluster;
    LinkedList<GPU> GPUS;
    LinkedList<CPU> CPUS;
    LinkedList<ConfrenceInformation> Conferences;
    TimeService timeService;
    LinkedList<Thread> threads;

    public JsonReader() {
        cluster = Cluster.getInstance();
        Students = new LinkedList<>();
        GPUS = new LinkedList<>();
        CPUS = new LinkedList<>();
        Conferences = new LinkedList<>();
        threads = new LinkedList<>();
    }

    public void outputCreate() {
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException ignored) {
            }
        }

        File thisFile = new File("/home/spl211/IdeaProjects/ass2-20211219T140524Z-001/ass2/src");
        try {
            FileWriter writer = new FileWriter("output.txt");
            writer.write(studentsInfo());
            writer.write(conferenceInfo());
            writer.write(statistics());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String conferenceInfo() {
        String ans = "Conferences:\n";
        String st = "    ";
        for (ConfrenceInformation confrence : Conferences) {
            ans += "{\n";
            ans = ans + st + "name: " + confrence.getName() + "\n";
            ans = ans + st + "date: " + confrence.getDate() + "\n";
            ans = ans + st + "publications:\n";
            LinkedList<Model> models = confrence.getMyModels();
            for (Model model : models) {
                ans = ans + modelString(model);
            }
            ans += "}\n";
        }
        return ans;
    }

    private String studentsInfo() {
        String ans = "Students:\n";
        String st = "    ";
        for (Student student : Students) {
            ans = ans + "{\n";
            ans = ans + st + "name: " + student.getName() + "\n";
            ans = ans + st + "department: " + student.getDepartment() + "\n";
            ans = ans + st + "status: " + student.getStatus().toString() + "\n";
            ans = ans + st + "publications: " + student.getPublications() + "\n";
            ans = ans + st + "papersRead: " + student.getPapersRead() + "\n";
            ans = ans + st + "trainedModels:\n";
            ConcurrentLinkedQueue<Model> models = student.getMyModels();
            for (Model model : models) {
                if (model.getStatus().equals("Trained") || model.getStatus().equals("Tested")) {
                    ans += modelString(model);
                }
            }
            ans += "}\n";
        }
        return ans;
    }

    private String modelString(Model model) {
        String ans = "";
        String st = "    ";
        ans = ans + st + "{\n";
        ans = ans + st + st + "name: " + model.getName() + "\n";
        ans = ans + st + st + "data:\n";
        ans = ans + st + st + "type: " + model.getData().getType().toString() + "\n";
        ans = ans + st + st + "size: " + model.getData().getSize() + "\n";
        ans = ans + st + st + "status: " + model.getStatus().toString() + "\n";
        ans = ans + st + st + "results: " + model.getResults().toString() + "\n";
        ans = ans + st + "}\n";
        return ans;
    }

    private String statistics() {
        String ans = "cpuTimeUsed: " + timeCPU() + "\n";
        ans += "gpuTimeUsed: " + timeGPU() + "\n";
        ans += "batchesProcessed: " + cluster.getSumProcessedData() + "\n";
        return ans;
    }

    private int timeCPU() {
        return cluster.getNumCPUtime();
    }

    private int timeGPU() {
        return cluster.getNumGPUtime();
    }


    public void begin() {
        for (CPU cpu : CPUS)
            threads.add(cpu.start());
        for (GPU gpu : GPUS)
            threads.add(gpu.start());
        for (Student student : Students)
            threads.add(student.start());
        for (ConfrenceInformation con : Conferences)
            threads.add(con.start());
        Thread t = new Thread(timeService);
        t.start();
        threads.add(t);
    }

    public Student anotherStudent(String department, String status, String name) {
        Student.Degree deg = Student.Degree.PhD;
        if (status.equals("MSc"))
            deg = Student.Degree.PhD;
        Student st = new Student(name, department, deg);
        Students.add(st);
        return st;
    }

    public void anotherGPU(String st) {
        GPU.Type type = GPU.Type.RTX3090;
        if (st.equals("GTX1080"))
            type = GPU.Type.GTX1080;
        if (st.equals("RTX2080"))
            type = GPU.Type.RTX2080;
        GPU gpu = new GPU(type);
        GPUS.add(gpu);
        cluster.addGPU(gpu);
    }

    public void anotherCPU(int cores) {
        CPU cpu = new CPU(cores);
        CPUS.add(cpu);
        cluster.addCPU(cpu);
    }
    public void setTimeService(int tickTime, int duration) {
        this.timeService = new TimeService(tickTime, duration);
    }

    public void anotherModel(String name, int size, String stringType, Student student) {
        Data.Type type = Data.Type.Tabular;
        if (stringType.equals("Images"))
            type = Data.Type.Tabular;
        if (stringType.equals("Text"))
            type = Data.Type.Text;

        Data data = new Data(type, size);
        Model model = new Model(name, data, student);
        student.addModel(model);
    }


    public void addConference(String name, int date) {
        Conferences.add(new ConfrenceInformation(name, date));
    }



}