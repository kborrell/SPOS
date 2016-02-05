package com.udl.tfg.sposapp.utils;

import org.opennebula.client.Client;
import org.opennebula.client.ClientConfigurationException;
import org.opennebula.client.OneResponse;
import org.opennebula.client.vm.VirtualMachine;
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
    VirtualMachinePool vmPool;

    public void Initialize(){
        try {
            System.out.println("OCA Manager created");
            client = new Client(user + ":" + pass, entryPoint);
            vmPool = new VirtualMachinePool(client);
        } catch (ClientConfigurationException e) {
            e.printStackTrace();
        }
    }

    public List<Integer> GetAllVmIds(){
        if (vmPool != null){
            List<Integer> ids = new ArrayList<>();
            OneResponse response = vmPool.infoAll();
            if ( response.isError() ){
                System.out.printf("GET ALL VM ERROR: " + response.getErrorMessage());
            } else {
                String responseMsg = response.getMessage();
                for (String s : findAllOccurrences("<ID>", "</ID", responseMsg)){
                    ids.add(Integer.parseInt(s));
                }
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

    private String findOneOccurrence(String prefix, String sufix, String str, int initialPos){
        int start = str.indexOf(prefix, initialPos) + prefix.length();
        int end = str.indexOf(sufix, start);

        if (start >= 0 && end >= 0){
            String content = str.substring(start, end);
            if (!content.contains("<"))
                return content;
        }

        return "";
    }

    public String GetIP(int vmID) {
        if (vmPool != null){
            VirtualMachine vm = vmPool.getById(vmID);
            OneResponse response = vm.info();

            if (response.isError())
                System.out.println("GET IP ERROR - ID: " + vmID + " Message: " + response.getErrorMessage());
            else
                return findOneOccurrence("<ETH0_IP><![CDATA[", "]]></ETH0_IP>", response.getMessage(), 0);
        }
        return "";
    }
}
