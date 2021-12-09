import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.*;

public class project2main {
    public static void main(String[] args) throws FileNotFoundException {
    	Locale.setDefault(new Locale("en", "US"));

        Scanner in = new Scanner(new File(args[0]));
        PrintStream out = new PrintStream(new File(args[1]));

        int numPlayers = in.nextInt();
        Player players[] = new Player[numPlayers];
        for(int i=0; i<numPlayers; i++){
            int id = in.nextInt();
            int skillLevel = in.nextInt();
            players[id] = new Player(id, skillLevel);
        }
                
        PriorityQueue<Event> events = new PriorityQueue<Event>();

        PriorityQueue<Staff> availableCoaches = new PriorityQueue<Staff>();
        PriorityQueue<Staff> availablePhysiotherapists = new PriorityQueue<Staff>();
        PriorityQueue<Staff> availableMasseurs = new PriorityQueue<Staff>();
        
        Hashtable<Integer, Staff> unavailableCoaches = new Hashtable<Integer, Staff>();
        Hashtable<Integer, Staff> unavailablePhysiotherapists = new Hashtable<Integer, Staff>();
        Hashtable<Integer, Staff> unavailableMasseurs = new Hashtable<Integer, Staff>();
        
        PriorityQueue<Player> trainingQueue = new PriorityQueue<Player>();
        PriorityQueue<Player> physiotherapyQueue = new PriorityQueue<Player>();
        PriorityQueue<Player> massageQueue = new PriorityQueue<Player>();

        double totalSeconds = 0;
        int invalidAttempts = 0;
        int canceledAttempts = 0;
        int maxLenTQ = 0;
        int maxLenPQ = 0;
        int maxLenMQ = 0;

        int numArrivals = in.nextInt();
        for(int j=0; j<numArrivals; j++){
            String indicator = in.next();
            int playerID = in.nextInt();
            double arrivalTime = Double.parseDouble(in.next());
            double duration = Double.parseDouble(in.next());

            if(indicator.equals("t")){
                events.add(new Event(arrivalTime, "arrivalForTraining", players[playerID], duration));
            }else if(indicator.equals("m")){
                events.add(new Event(arrivalTime, "arrivalForMassage", players[playerID], duration));
            }
        }

        int numPhysiotherapists = in.nextInt();
        for(int physioID=0; physioID<numPhysiotherapists; physioID++){
            double serviceTime = Double.parseDouble(in.next());
            availablePhysiotherapists.add(new Staff(physioID, serviceTime));
        }

        int numTrainingCoaches = in.nextInt();
        for(int coachID=0; coachID<numTrainingCoaches; coachID++){
            availableCoaches.add(new Staff(coachID));
        }

        int numMasseurs = in.nextInt();
        for(int masseurID=0; masseurID<numMasseurs; masseurID++){
            availableMasseurs.add(new Staff(masseurID));
        }

        while(!events.isEmpty()){
            Event curr = events.poll();
            String eventType = curr.type;
            Player currPlayer = curr.player;
            totalSeconds = curr.time;

            if(eventType.equals("arrivalForTraining")){
            	
            	//if the player is already in the system it is considered as a canceled attempt
                if(currPlayer.isInSystem){
                    canceledAttempts++;
                }else{
                	currPlayer.trainingTime = curr.duration;
                	currPlayer.isInSystem = true;
                    if(trainingQueue.isEmpty() && !availableCoaches.isEmpty()){
                        Staff coach = availableCoaches.poll();
                        events.add(new Event(curr.time, "startTraining", currPlayer, curr.duration, coach));
                    }else{
                        events.add(new Event(curr.time, "enterTrainingQueue", currPlayer));
                    }
                }

            }else if(eventType.equals("enterTrainingQueue")){
                trainingQueue.add(curr.player);
                if(trainingQueue.size() > maxLenTQ)
                    maxLenTQ = trainingQueue.size();

            }else if(eventType.equals("startTraining")){

                unavailableCoaches.put(curr.staff.id, curr.staff);
                events.add(new Event(curr.time+curr.duration, "endTraining", curr.player, curr.staff));

            }else if(eventType.equals("endTraining")){

                unavailableCoaches.remove(curr.staff.id);
                availableCoaches.add(curr.staff);

                events.add(new Event(curr.time, "physioAttempt", curr.player));
                
                //if there is a possible match between players and coaches, they start training
                if(!trainingQueue.isEmpty() && !availableCoaches.isEmpty()){
                    Player player = trainingQueue.poll();
                    Staff coach = availableCoaches.poll();
                    events.add(new Event(curr.time, "startTraining", player, player.trainingTime, coach));
                    Player.sumWaitingTraining += (curr.time - player.enteredQueue);
                }

            }else if(eventType.equals("physioAttempt")){

                if(physiotherapyQueue.isEmpty() && !availablePhysiotherapists.isEmpty()){
                    Staff physiotherapist = availablePhysiotherapists.poll();
                    events.add(new Event(curr.time, "startPhysiotherapy", currPlayer, physiotherapist.serviceTime, physiotherapist));
                }else{
                    events.add(new Event(curr.time, "enterPhysiotherapyQueue", currPlayer));
                }
                
            }else if(eventType.equals("enterPhysiotherapyQueue")){
            	
                physiotherapyQueue.add(currPlayer);
                if(physiotherapyQueue.size() > maxLenPQ)
                    maxLenPQ = physiotherapyQueue.size();

            }else if(eventType.equals("startPhysiotherapy")){
            	
                unavailablePhysiotherapists.put(curr.staff.id, curr.staff);
                events.add(new Event(curr.time+curr.duration, "endPhysiotherapy", currPlayer, curr.staff));

            }else if(eventType.equals("endPhysiotherapy")){

                unavailablePhysiotherapists.remove(curr.staff.id);
                availablePhysiotherapists.add(curr.staff);
                currPlayer.isInSystem = false;

                //if there is a possible match between waiting players and available physiotherapists, service starts
                if(!physiotherapyQueue.isEmpty() && !availablePhysiotherapists.isEmpty()){
                    Player player = physiotherapyQueue.poll();
                    Staff physiotherapist = availablePhysiotherapists.poll();
                    events.add(new Event(curr.time, "startPhysiotherapy", player, physiotherapist.serviceTime, physiotherapist));

                    Player.sumWaitingPhysio += (curr.time - player.enteredQueue);
                    player.waitingTimeInPQ += (curr.time - player.enteredQueue);
                    player.updateMaxWaitingInPQ();

                }

            }else if(eventType.equals("arrivalForMassage")){
            	
                if(currPlayer.isInvalid())
                    invalidAttempts++;
                else if(currPlayer.isInSystem)
                    canceledAttempts++;
                else{
                    currPlayer.isInSystem = true;
                	currPlayer.massageAttempts++;
                	currPlayer.updateMinWaitingInMQ();

                    currPlayer.massageTime = curr.duration;
                    if(massageQueue.isEmpty() && !availableMasseurs.isEmpty()){
                        Staff masseur = availableMasseurs.poll();
                        events.add(new Event(curr.time, "startMassage", curr.player, curr.duration, masseur));
                    }else{
                        events.add(new Event(curr.time, "enterMassageQueue", curr.player));
                    }
                }

            }else if(eventType.equals("enterMassageQueue")){
                massageQueue.add(currPlayer);

                if(massageQueue.size() > maxLenMQ)
                    maxLenMQ = massageQueue.size();

            }else if(eventType.equals("startMassage")){

                unavailableMasseurs.put(curr.staff.id, curr.staff);
                events.add(new Event(curr.time+curr.duration, "endMassage", currPlayer, curr.staff));
                
            }else if(eventType.equals("endMassage")){

                unavailableMasseurs.remove(curr.staff.id);
                availableMasseurs.add(curr.staff);
                currPlayer.isInSystem = false;
                
                //checks for a possible masseur-player match, if there is massage service starts
                if(!massageQueue.isEmpty() && !availableMasseurs.isEmpty()){
                    Player player = massageQueue.poll();
                    Staff masseur = availableMasseurs.poll();
                    events.add(new Event(curr.time, "startMassage", player, player.massageTime, masseur));

                    Player.sumWaitingMassage += (curr.time - player.enteredQueue);
                    player.waitingTimeInMQ += (curr.time - player.enteredQueue);
                }
            }
        }
        out.println(maxLenTQ);
        out.println(maxLenPQ);
        out.println(maxLenMQ);
        out.printf("%.3f\n", Event.averageTrainingWaiting());
        out.printf("%.3f\n", Event.averagePhysioWaiting());
        out.printf("%.3f\n", Event.averageMassageWaiting());
        out.printf("%.3f\n", Event.averageTrainingTime());
        out.printf("%.3f\n", Event.averagePhysioTime());
        out.printf("%.3f\n", Event.averageMassageTime());
        out.printf("%.3f\n", Event.averageTurnaroundTime());

        if(Player.leastWaitedInMQ == Integer.MAX_VALUE) {
            Player.playerIDWaitedLeast = -1;
            Player.leastWaitedInMQ = -1;
        }
        out.print(Player.playerIDWaitedMost + " "); out.printf("%.3f\n", Player.mostWaitedInPQ);
        out.print(Player.playerIDWaitedLeast + " "); out.printf("%.3f\n", Player.leastWaitedInMQ);
        out.println(invalidAttempts);
        out.println(canceledAttempts);
        out.printf("%.3f\n", totalSeconds);
        
        }
}
