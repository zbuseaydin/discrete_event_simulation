public class Player implements Comparable<Player> {

    protected int id;
    private int skillLevel;
    protected int massageAttempts = 0;
    private String eventType;
    protected double trainingTime;
    protected double massageTime;
    protected double enteredQueue;
    protected double waitingTimeInMQ = 0;
    protected double waitingTimeInPQ = 0;
    protected boolean isInSystem = false;

    public static double sumWaitingTraining = 0;
    public static double sumWaitingPhysio = 0;
    public static double sumWaitingMassage = 0;
    
    //the id of the player that waited most in the physiotherapy queue
    public static int playerIDWaitedMost;
    public static double mostWaitedInPQ = 0;

    //the id of the player that waited least in the massage queue
    public static int playerIDWaitedLeast = -1;
    public static double leastWaitedInMQ = Integer.MAX_VALUE;

    public Player(int id, int skillLevel){
        this.id = id;
        this.skillLevel = skillLevel;
    }
    
    //checks if the message attempt is invalid or not
    public boolean isInvalid(){
        if(this.massageAttempts==3)
            return true;
        return false;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }
    
    //if the player has 3 massage attempts and player's total waiting time in massage queue is smaller than the minimum
    //waiting time in this queue, updates the minimum waiting time among the players
    public void updateMinWaitingInMQ() {
    	
    	if(this.massageAttempts == 3) {
    		
    		if(this.waitingTimeInMQ < leastWaitedInMQ) {
        		leastWaitedInMQ = this.waitingTimeInMQ;
        		playerIDWaitedLeast = this.id;
        		
        	}else if(Math.abs(this.waitingTimeInMQ - leastWaitedInMQ) < 0.0000000000001 && this.id < playerIDWaitedLeast) {
        		leastWaitedInMQ = this.waitingTimeInMQ;
        		playerIDWaitedLeast = this.id;
        	}
    	}
    }

    //if the player's total waiting time in physiotherapy queue is bigger than the minimum
    //waiting time in that queue, updates the maximum waiting time among the players
    public void updateMaxWaitingInPQ() {
    	
    	if(this.waitingTimeInPQ > mostWaitedInPQ){
            playerIDWaitedMost = this.id;
            mostWaitedInPQ = this.waitingTimeInPQ;
            
        }else if(Math.abs(this.waitingTimeInPQ - mostWaitedInPQ) < 0.000000001 && this.id < playerIDWaitedMost){
            playerIDWaitedMost = this.id;
            mostWaitedInPQ = this.waitingTimeInPQ;
        }
    }
    
    
    @Override
    public int compareTo(Player o) {

        if(this.eventType.equals("physiotherapy") && !(Math.abs(this.trainingTime-o.trainingTime)<0.00000000001)){

            if(this.trainingTime > o.trainingTime)
                return -1;
            else if(this.trainingTime < o.trainingTime)
                return 1;

        }else if(this.eventType.equals("massage") && !(Math.abs(this.skillLevel-o.skillLevel)<0.0000000001))
            return o.skillLevel - this.skillLevel;
        
        if(this.enteredQueue > o.enteredQueue)
            return 1;
        else if(this.enteredQueue < o.enteredQueue)
            return -1;

        return this.id - o.id;
    }
}
