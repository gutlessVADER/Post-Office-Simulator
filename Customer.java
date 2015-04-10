import java.util.concurrent.Semaphore;
import java.util.Random;
import java.util.ArrayList;

public class Customer implements Runnable
{
	private int 					number;			//	customer number that will be printed in the console for identification
	private int 					task;			//	buying stamps, mailing a letter, or mailing a package
	private ArrayList<Semaphore> 	done;
	private Semaphore 				capacity;
	private Semaphore 				servicing;
	private Semaphore 				ready;
	private Semaphore				working;
	private Semaphore 				FirstMutex;
	private Semaphore 				ThirdMutex;
	private Semaphore 				request;
	private String					taskString;
	
	Customer(int number,							//	default constructor, requires many variables to be passed
			 ArrayList<Semaphore> 	done,
			 Semaphore 				capacity,
			 Semaphore 				servicing,
			 Semaphore 				ready,
			 Semaphore 				working,
			 Semaphore 				FirstMutex,
			 Semaphore 				ThirdMutex,
			 Semaphore 				request)
	{
		this.done 					= done;
		this.number 				= number;
		this.capacity 				= capacity;
		this.servicing 				= servicing;
		this.ready 					= ready;
		this.working 				= working;
		this.FirstMutex 			= FirstMutex;
		this.ThirdMutex 			= ThirdMutex;
		this.request 				= request;
		
		Random randomGenerator = new Random();
	    this.task = randomGenerator.nextInt(3);
		
	    //	assigns string describing task based on random integer
		if(this.task == 0)
			this.taskString	= "buy stamps";
		else if(this.task == 1)
			this.taskString	= "mail a letter";
		else	
			this.taskString	= "mail a package";
		
		System.out.println("Customer " + number + " created");
	}
	
	public void run()
	{
		try
		{
			capacity.acquire();	//	get inside the store
		} catch(InterruptedException e){}
		
		try
		{
			servicing.acquire(); 	// 	become one of three customers being served
		} catch(InterruptedException e){}
		
		try
		{
			FirstMutex.acquire();	//	customer and postal worker tied to each other, only this thread can write to global variables
		} catch(InterruptedException e){}
		
		PostOfficeSimulation.customerNumber = this.number;
		PostOfficeSimulation.task = this.task;

		ready.release();	//	allows worker to begin reading data
	
		try
		{
			ThirdMutex.acquire();			//	waits for worker to acquire data
		} catch(InterruptedException e){}
		
		
		System.out.println("Customer " + number + " asks postal worker " + PostOfficeSimulation.assignedWorker[number] + " to " + this.taskString);
		
		request.release();	//	allows worker to begin working on task
		
		FirstMutex.release();		//	other threads can now write to global variables
		
		try
		{
			done.get(this.number).acquire();	//	pauses until done being served
		} catch(InterruptedException e){}

		working.release();		//	customer is satisfied and is leaving
		
		System.out.println( "Customer " + number + " leaves post office" );
		
		capacity.release();		//	allows another customer to enter post office
	}
}