package com.udl.tfg.sposapp.utils;

import com.udl.tfg.sposapp.models.VirtualMachine;
import org.opennebula.client.Client;
import org.opennebula.client.ClientConfigurationException;
import org.opennebula.client.OneResponse;
import org.opennebula.client.vm.VirtualMachinePool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class OCAManager {

    @Value("${openNebulaUser}") private String user;
    @Value("${openNebulaPass}") private String pass;
    @Value("${openNebulaEntryPoint}") private String entryPoint;

    private Client client;
    private VirtualMachine vm;

    public void Initialize(){
        try {
            System.out.println("OCA Manager created");
            client = new Client(user + ":" + pass, entryPoint);
        } catch (ClientConfigurationException e) {
            e.printStackTrace();
        }
    }

    public List<String> GetAllVmIds(){
        if (client != null){
            List<String> ids = new ArrayList<>();
            OneResponse response = VirtualMachinePool.infoAll(client);
            if ( response.isError() ){
                System.out.printf("GET ALL VM ERROR: " + response.getErrorMessage());
            } else {
                String responseMsg = response.getMessage();
                ids = findAllOccurrences("<ID>", "</ID", responseMsg);
            }
            return ids;
        } else {
            return null;
        }
    }

    private List<String> findAllOccurrences(String prefix, String sufix, String str) {
        List<String> occurrences = new ArrayList<>();
        int index = 0;
        while (index >= 0){
            int prefixPos = str.indexOf(prefix, index);

            if (prefixPos == -1)
                break;

            int start = prefixPos + prefix.length();
            int end = str.indexOf(sufix, start);
            if (start >= 0 && end >= 0){
                String content = str.substring(start, end);
                if (!content.contains("<"))
                    occurrences.add(content);
            }
            index = end + sufix.length();
        }
        return occurrences;
    }
}
