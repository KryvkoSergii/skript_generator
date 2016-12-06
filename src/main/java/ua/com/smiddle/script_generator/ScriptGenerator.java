package ua.com.smiddle.script_generator;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * @author ksa on 06.12.16.
 * @project skript_generator
 */
public class ScriptGenerator {
    private static final String sourceScriptPath = "/home/ksa/Clients/sbrf-emul/Emulator/SiebelEmulator/users/sagir_2.txt";
    private static final String targetScriptPath = "/home/ksa/mount-point/users/";
    private static final String targetUserListFilePath = "/home/ksa/mount-point/users/";
    //    private static final String targetScriptPath = "/home/ksa/Clients/sbrf-emul/Emulator/SiebelEmulator/users/";
//    private static final String targetUserListFilePath = targetScriptPath;
    private List<String[]> model = new ArrayList<>();
    private List<String> loadedScript = new ArrayList<>();
    private List<String> users = new ArrayList<>();
    private String host = "172.22.254.121";

    public static void main(String[] args) {
        ScriptGenerator sg = new ScriptGenerator();
        sg.proccess(sourceScriptPath, 60);
    }

    public void proccess(String path, int count) {
        System.out.println("Starting....");
        try {
            loadScript(path);
            String fileName;
            for (int i = 0; i < count; i++) {
                fillModel(String.format("%07d", 8000000 + i), String.format("%05d", 90000 + i));
                fileName = "user_" + String.format("%05d", 90000 + i);
                writeToFile(fileName + ".txt");
                users.add(fileName + ".txt");
            }
            writeUserListToFile(users, "users.txt");
            System.out.println("Done");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private void writeUserListToFile(List<String> users, String path) throws FileNotFoundException, UnsupportedEncodingException {
        File f = new File(targetUserListFilePath + path);
        if (f.exists()) f.delete();
        PrintWriter pw = new PrintWriter(f, "Cp1251");
        String string;
        for (int i = 0; i < users.size(); i++) {
            string = i != users.size() - 1 ? users.get(i) + '\r' + '\n' : users.get(i);
            pw.print(string);
        }
        pw.flush();
        pw.close();
        System.out.println("Wrote to " + f.getAbsolutePath());
    }

    private void loadScript(String path) throws FileNotFoundException {
        File file = new File(path);
        if (!file.exists()) throw new FileNotFoundException("File " + path + " not found");
        loadedScript.clear();
        Scanner sc = new Scanner(new FileReader(file));
        while (sc.hasNextLine())
            loadedScript.add(sc.nextLine());
        System.out.println("Reading script from path=" + path + " completed. Loaded rows numb=" + loadedScript.size());
    }

    private void writeToFile(String fileName) throws FileNotFoundException, UnsupportedEncodingException {
        File f = new File(targetScriptPath.concat(fileName));
        PrintWriter pw = new PrintWriter(f, "Cp1251");
        String string;
        for (int i = 0; i < loadedScript.size(); i++) {
            string = i != loadedScript.size() - 1 ? loadedScript.get(i) + '\r' + '\n' : loadedScript.get(i);
            pw.print(string);
        }
        pw.flush();
        pw.close();
        System.out.println("Wrote to " + f.getAbsolutePath());
    }

    private void fillModel(String agentInstrument, String agentId) {
        model.add(new String[]{"Param:SideAHost", host});
        model.add(new String[]{"Param:SideBHost", host});
        model.add(new String[]{"Param:DNList", agentInstrument});
        model.add(new String[]{"Param:SelectDN", agentInstrument});
        model.add(new String[]{"Param:AgentId", agentId});
        Optional<String[]> r;
        String scriptRow;
        for (int i = 0; i < loadedScript.size(); i++) {
            scriptRow = loadedScript.get(i);
            final String val = (scriptRow.indexOf("=") != -1) ? scriptRow.substring(0, scriptRow.indexOf("=")) : scriptRow;
            r = model.stream().filter(array -> val.contains(array[0])).findFirst();
            if (r.isPresent())
                loadedScript.set(i, r.get()[0].concat("=").concat(r.get()[1]));
        }
        model.clear();
    }


}
