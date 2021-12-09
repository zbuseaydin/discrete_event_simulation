public class Staff implements Comparable<Staff>{

    protected double serviceTime;
    protected int id;
    
    public Staff(int id){
        this.id = id;
    }

    public Staff(int id, double serviceTime){
        this.id = id;
        this.serviceTime = serviceTime;
    }

    @Override
    public int compareTo(Staff o) {
        return this.id - o.id;
    }

}
