package com.udl.tfg.sposapp.utils;

import com.udl.tfg.sposapp.models.VirtualMachine;
import com.udl.tfg.sposapp.repositories.VirtualMachineRepository;
import org.opennebula.client.Client;
import org.opennebula.client.ClientConfigurationException;
import org.opennebula.client.OneResponse;
import org.opennebula.client.template.Template;
import org.opennebula.client.vm.VirtualMachinePool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

@Service
public class OCAManager {

    public enum PredefinedVM
    {
        LOW,
        MEDIUM,
        HIGH
    }

    private final String LOW_VM_IP = "192.168.101.69";
    private final String MEDIUM_VM_IP = "192.168.101.69";
    private final String HIGH_VM_IP = "192.168.101.69";

    @Value("${openNebulaUser}") private String user;
    @Value("${openNebulaPass}") private String pass;
    @Value("${openNebulaEntryPoint}") private String entryPoint;

    @Autowired
    private SSHManager sshManager;

    @Autowired
    private VirtualMachineRepository virtualMachineRepository;

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

    public String GetIP(org.opennebula.client.vm.VirtualMachine vm) {
        if (vm != null){
            OneResponse response = vm.info();
            if (response.isError())
                System.out.println("GET IP ERROR: " + response.getErrorMessage());
            else
                return findOneOccurrence("<ETH0_IP><![CDATA[", "]]></ETH0_IP>", response.getMessage(), 0);
        }
        return "";
    }

    public String GetPredefinedVMIP(PredefinedVM type) {
        switch (type) {
            case LOW:
                return LOW_VM_IP;
            case MEDIUM:
                return  MEDIUM_VM_IP;
            case HIGH:
                return HIGH_VM_IP;
            default:
                return "";
        }
    }

    public String createNewVM(VirtualMachine vmInfo) throws Exception {

        OneResponse newRc = Template.update(client, 45, "CPU = " + vmInfo.getrealCPUs() + " VCPU = " + vmInfo.getVirtualCPUs() + " MEMORY = " + vmInfo.getRam() + "\n",true);
        int newId = Integer.parseInt(newRc.getMessage());
        Template t = new Template(newId, client);

        OneResponse rc = t.instantiate();

        if( rc.isError() )
        {
            System.out.println( "failed!");
            System.out.println(rc.getMessage());
            throw new Exception( rc.getErrorMessage() );
        }

        int newVMId = Integer.parseInt(rc.getMessage());
        org.opennebula.client.vm.VirtualMachine vm = new org.opennebula.client.vm.VirtualMachine(newVMId, client);

        if(rc.isError())
        {
            System.out.println("failed!");
            throw new Exception( rc.getErrorMessage() );
        }

        String ip = GetIP(vm);

        WaitUntilCreated(ip);

        vmInfo.setApiID(newVMId);
        virtualMachineRepository.save(vmInfo);

        return ip;
    }

    public void deleteVM(int vmID) throws Exception {

        OneResponse rc = vmPool.info();
        if(rc.isError())
            throw new Exception( rc.getErrorMessage() );


        org.opennebula.client.vm.VirtualMachine vm = vmPool.getById(vmID);

        if (vm != null) {
            rc = vm.delete();
        }

        if (rc.isError())
            throw new Exception( rc.getErrorMessage() );
    }

    private void WaitUntilCreated(String ip) {
        InetAddress ping;
        int attempts = 0;
        while (true) {
            try {
                ping = InetAddress.getByName(ip);
                if(ping.isReachable(5000)){
                    break;
                }else {
                    Thread.sleep(5000);
                    attempts++;
                    if (attempts >= 120){
                        break;
                    }
                }
            } catch (Exception ex) {
                break;
            }
        }
    }
}
