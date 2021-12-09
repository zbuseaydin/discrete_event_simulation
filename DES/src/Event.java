public class Event implements Comparable<Event>{

    public double time;
    public double duration=0;
    public Player player;

    // type can only be one of the following:
    // arrivalForTraining, enterTrainingQueue, startTraining, endTraining,
    // physioAttempt, enterPhysiotherapyQueue, startPhysiotherapy, endPhysiotherapy,
    // arrivalForMassage, enterMassageQueue, startMassage, endMassage
    public String type;
    public Staff staff;
    private static int numOfTraining = 0;
    private static int numOfPhysiotherapy = 0;
    private static int numOfMassage = 0;
    private static double totalTrainingTime = 0;
    private static double totalPhysioTime = 0;
    private static double totalMassageTime = 0;

    public Event(double time, String type, Player player, double duration){
        this.time = time;
        this.type = type;
        this.player = player;
        this.duration = duration;
    }
    public Event(double time, String type, Player player, double duration, Staff staff){
        this.time = time;
        this.type = type;
        this.duration = duration;
        this.staff = staff;
        this.player = player;

        if(this.type.equals("startTraining")){
            totalTrainingTime += this.duration;
            numOfTraining++;
        }
        else if(this.type.equals("startPhysiotherapy")){
            totalPhysioTime += this.duration;
            numOfPhysiotherapy++;
        }
        else if(this.type.equals("startMassage")){
            totalMassageTime += this.duration;
            numOfMassage++;
        }
    }

    public Event(double time, String type, Player player, Staff staff){
        this.time = time;
        this.type = type;
        this.staff = staff;
        this.player = player;
    }

    public Event(double time, String type, Player player){
        this.time = time;
        this.type = type;
        this.player = player;

        if(this.type.equals("enterTrainingQueue")){
            this.player.setEventType("training");
            this.player.enteredQueue = this.time;

        }else if(this.type.equals("enterPhysiotherapyQueue")){
            this.player.setEventType("physiotherapy");
            this.player.enteredQueue = this.time;

        }else if(this.type.equals("enterMassageQueue")){
            this.player.setEventType("massage");
            this.player.enteredQueue = this.time;
        }
    }

    public static double averageTrainingTime(){
    	if(numOfTraining == 0)
    		return 0;
        return totalTrainingTime / (double) numOfTraining;
    }

    //calculates and returns the average physiotherapy time
    public static double averagePhysioTime(){
    	if(numOfPhysiotherapy == 0)
    		return 0;
        return totalPhysioTime / (double) numOfPhysiotherapy;
    }

    public static double averageMassageTime(){
    	if(numOfMassage == 0)
    		return 0;
        return totalMassageTime / (double) numOfMassage;
    }

    //calculates and returns the average turnaround time (which is from the training queue entrance to the end of physiotherapy)
    public static double averageTurnaroundTime(){
    	if(numOfTraining == 0)
    		return 0;
        return (Player.sumWaitingTraining + totalTrainingTime + Player.sumWaitingPhysio + totalPhysioTime) / (double) numOfTraining;
    }

    public static double averageTrainingWaiting(){
    	if(numOfTraining == 0)
    		return 0;
        return Player.sumWaitingTraining / (double) numOfTraining;
    }

    //calculates and returns the average waiting time in physiotherapy queue
    public static double averagePhysioWaiting(){
    	if(numOfPhysiotherapy == 0)
    		return 0;
        return Player.sumWaitingPhysio / (double) numOfPhysiotherapy;

    }

    public static double averageMassageWaiting(){
    	if(numOfMassage == 0)
    		return 0;
        return Player.sumWaitingMassage / (double) numOfMassage;
    }

    @Override
    public int compareTo(Event o) {
    	if(!(Math.abs(this.time-o.time)<0.0000000001)) {
    		if(this.time < o.time)
                return -1;
            return 1;
    	}
        return this.player.id-o.player.id;
    }
}
