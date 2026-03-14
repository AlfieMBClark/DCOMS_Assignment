
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

public class AddServer {
    public static void main(String[] args) {
        try {
            // Start RMI registry on port 1099
            LocateRegistry.createRegistry(1099);

            // Create remote object
            AddInterface obj = new AddImplementation();
            // Bind the remote object to the registry
            Naming.rebind("rmi://localhost/AddService", obj);
            
            SubInterface obj2 = new SubImplementation();
            Naming.rebind("rmi://localhost/SubService", obj2);

            System.out.println("RMI Server is running...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

