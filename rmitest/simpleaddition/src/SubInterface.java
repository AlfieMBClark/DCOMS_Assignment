
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface SubInterface extends Remote {
    int sub(int a, int b) throws RemoteException;
}
