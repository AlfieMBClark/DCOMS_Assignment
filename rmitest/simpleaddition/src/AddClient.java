
import java.rmi.Naming;
import java.util.Scanner;

public class AddClient {
    public static void main(String[] args) {
        try {
            Scanner scanner = new Scanner(System.in);

            // Basic input for two integers
            System.out.print("Enter first number: ");
            int a = scanner.nextInt();

            System.out.print("Enter second number: ");
            int b = scanner.nextInt();

            // Lookup remote object
            AddInterface obj = (AddInterface) Naming.lookup("rmi://localhost/AddService");

            // Call remote method
                       int result = obj.add(a, b);
            System.out.println("Result of addition: " + result);
            
            SubInterface obj2 = (SubInterface) Naming.lookup("rmi://localhost/SubService");
            int subresult = obj2.sub(a, b);
            System.out.println("Result of Subtraction: "+ subresult);

            scanner.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
