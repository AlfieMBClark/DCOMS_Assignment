package server;

import common.interfaces.PRSService;
import common.models.PayrollRecord;
import common.models.UserAccount;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

/**
 * Implementation of the Payroll System (PRS) stub.
 * Demonstrates secure RMI communication between HRM and PRS.
 * This is exported with SSL-secured RMI to satisfy Task 4 (secure PRS communication).
 */
public class PRSServiceImpl extends UnicastRemoteObject implements PRSService {

    private final CSVDataStore dataStore;
    private final AuthServiceImpl authService;
    private final AuditLogger auditLogger;

    public PRSServiceImpl(CSVDataStore dataStore, AuthServiceImpl authService,
                          AuditLogger auditLogger) throws RemoteException {
        super();
        this.dataStore = dataStore;
        this.authService = authService;
        this.auditLogger = auditLogger;
    }

    @Override
    public PayrollRecord getPayrollRecord(int employeeId, int month, int year,
                                           String sessionToken) throws RemoteException {
        UserAccount user = authService.validateSession(sessionToken);
        if (user == null) throw new RemoteException("Unauthorized");

        // Employees can only view their own payroll
        if (user.getRole().equals("EMPLOYEE") && user.getEmployeeId() != employeeId) {
            throw new RemoteException("Forbidden: Cannot view another employee's payroll");
        }

        PayrollRecord record = dataStore.getPayrollRecord(employeeId, month, year);
        if (record == null) {
            throw new RemoteException("No payroll record found for employee " + employeeId
                + " in " + month + "/" + year);
        }

        System.out.println("[PRS] Payroll retrieved for employee #" + employeeId + " (" + month + "/" + year + ")");
        return record;
    }

    @Override
    public List<PayrollRecord> getYearlyPayroll(int employeeId, int year,
                                                 String sessionToken) throws RemoteException {
        UserAccount user = authService.validateSession(sessionToken);
        if (user == null) throw new RemoteException("Unauthorized");

        if (user.getRole().equals("EMPLOYEE") && user.getEmployeeId() != employeeId) {
            throw new RemoteException("Forbidden");
        }

        return dataStore.getYearlyPayroll(employeeId, year);
    }

    @Override
    public synchronized boolean generatePayroll(int employeeId, int month, int year,
                                                 double basicSalary, String sessionToken) throws RemoteException {
        UserAccount user = authService.validateSession(sessionToken);
        if (user == null) throw new RemoteException("Unauthorized");
        if (!user.getRole().equals("HR") && !user.getRole().equals("ADMIN")) {
            throw new RemoteException("Forbidden: Only HR/Admin can generate payroll");
        }

        // Check if payroll already exists
        if (dataStore.getPayrollRecord(employeeId, month, year) != null) {
            throw new RemoteException("Payroll already generated for " + month + "/" + year);
        }

        // Simple payroll calculation (stub logic)
        double deductions = basicSalary * 0.11;  // EPF 11%
        double netSalary = basicSalary - deductions;

        PayrollRecord record = new PayrollRecord(0, employeeId, month, year,
            basicSalary, deductions, netSalary, null);
        int id = dataStore.addPayrollRecord(record);

        auditLogger.log(user, "GENERATE_PAYROLL", "payroll_records", id,
            "Generated payroll for employee #" + employeeId + " (" + month + "/" + year
            + "): Basic=" + basicSalary + ", Net=" + netSalary);

        System.out.println("[PRS] Payroll generated for employee #" + employeeId
            + " (" + month + "/" + year + "): RM" + netSalary);
        return true;
    }
}
