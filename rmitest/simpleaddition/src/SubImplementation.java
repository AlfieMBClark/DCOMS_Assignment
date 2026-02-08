
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class SubImplementation extends UnicastRemoteObject implements SubInterface {
    protected SubImplementation() throws RemoteException{
        super();
    }
    
    @Override
    public int sub(int a, int b) throws RemoteException{
        return a - b;
    }
}
