public class Curte {
    private int portiiMancare, nrIngrijitori, finishedIngrijitori;

    public Curte(int portiiMancareInitial, int nrIngrijitori) {
        this.portiiMancare = portiiMancareInitial;
        this.nrIngrijitori = nrIngrijitori;
        this.finishedIngrijitori = 0;
    }

    public synchronized void ofera(int nrPortii) throws InterruptedException {
        while (portiiMancare > 0) {
            wait();
        }
        portiiMancare += nrPortii;
        notifyAll();
    }

    public synchronized int consuma() throws InterruptedException {
        while(portiiMancare == 0) {
            if(finishedIngrijitori == nrIngrijitori) {
                return 0;
            }
            wait();
        }
        portiiMancare -= 1;
        if(portiiMancare == 0) {
            notifyAll();
        }
        return 1;
    }

    public synchronized void setFinishedIngrijitori() {
        this.finishedIngrijitori++;
        notifyAll();
    }
}
