package bgu.spl.mics.application;

import bgu.spl.mics.application.objects.JsonReader;
import bgu.spl.mics.application.objects.Student;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

/** This is the Main class of Compute Resources Management System application. You should parse the input file,
 * create the different instances of the objects, and run the system.
 * In the end, you should output a text file.
 */
public class CRMSRunner {
    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        JSONParser parser=new JSONParser();
        JsonReader jsonReader=new JsonReader();
        try {
            Object object = parser.parse(new FileReader(args[0]));
            JSONObject jsonObject=(JSONObject) object;

            JSONArray arrayStudents=(JSONArray) jsonObject.get("Students");
            Iterator<JSONObject> it=arrayStudents.iterator();

            while(it.hasNext()){

                JSONObject thisStudent=it.next();
                String name=(String) thisStudent.get("name");
                String status = (String) thisStudent.get("status");
                String department = (String) thisStudent.get("department");
                Student student = jsonReader.anotherStudent(department, status, name);
                JSONArray arrayModels = (JSONArray) thisStudent.get("models");

                if(arrayModels.size()==1){

                    Iterator<JSONObject> iterator = arrayModels.iterator();
                    if(iterator.hasNext()) {

                        JSONObject thisModel = iterator.next();
                        String modelName = (String) thisModel.get("name");
                        long size = (long) thisModel.get("size");
                        String type = (String) thisModel.get("type");
                        jsonReader.anotherModel(modelName,(int)size, type, student);
                    }
                }
                else{
                    Iterator<JSONObject> iterator = arrayModels.iterator();
                    while (iterator.hasNext()) {

                        JSONObject jsonModel = iterator.next();
                        String modelName = (String) jsonModel.get("name");
                        long size = (long) jsonModel.get("size");
                        String type = (String) jsonModel.get("type");
                        jsonReader.anotherModel(modelName,(int) size, type,  student);
                    }
                }
            }

            JSONArray arrayConfrence=(JSONArray) jsonObject.get("Conferences");
            Iterator<JSONObject> itConfrence=arrayConfrence.iterator();
            while (itConfrence.hasNext()) {

                JSONObject jsonConference=itConfrence.next();
                long date=(long) jsonConference.get("date");
                String nameConfrence=(String) jsonConference.get("name");
                jsonReader.addConference(nameConfrence,(int)date);
            }

            JSONArray arrayCPU=(JSONArray) jsonObject.get("CPUS");
            Iterator<Long> itCPU=arrayCPU.iterator();

            while (itCPU.hasNext()) {
                long cores=itCPU.next();
                jsonReader.anotherCPU((int) cores);
            }

            JSONArray arrayGPU=(JSONArray) jsonObject.get("GPUS");
            Iterator<String> itGPU=arrayGPU.iterator();

            while (itGPU.hasNext())
                jsonReader.anotherGPU(itGPU.next());


            long duration=(long) jsonObject.get("Duration");
            long tickTime=(long) jsonObject.get("TickTime");

            jsonReader.setTimeService((int)tickTime,(int)duration);
        }
        catch (FileNotFoundException e) { e.printStackTrace(); }
        catch (IOException e) { e.printStackTrace(); }
        catch (ParseException e) { e.printStackTrace(); }
        catch (Exception e) { e.printStackTrace(); }

        jsonReader.begin();
        jsonReader.outputCreate();

    }
}