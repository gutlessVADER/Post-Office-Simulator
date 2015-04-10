import java.util.concurrent.Semaphore;
import java.util.ArrayList;

public class PostOfficeSimulation
{
	public static int 	customerNumber;
	public static int 	task;
	public static int[] assignedWorker = new int[50];
	
	public static void main(String args[])
	{
		int customers	= 50;
		int workers 	= 3;
		
		ArrayList<Semaphore> done 	= new ArrayList<Semaphore>();	//	makes a semaphore for every customer to indicate their completion status
		Semaphore 	capacity		= new Semaphore(10, true),		//	makes sure only ten customers are inside the post office
					servicing 		= new Semaphore(3, true),		//	controls number of customers able to be helped at once
					ready			= new Semaphore(0, true),		//	lets worker know customer is ready
					FirstMutex 		= new Semaphore(1, true),		//	protects global variable access
					//	there was a SecondMutex, but I found it to be unnecessary once the "ready" semaphore was implemented correctly
					ThirdMutex 		= new Semaphore(0, true),		//	ensures customer requests assistance at appropriate time
					request	 		= new Semaphore(0, true),		//	forces customer to request service before worker can begin working
					scales			= new Semaphore(1, true),		//	ensures only one worker can use the scales at a time
					working			= new Semaphore(0 , true);		//	determines whether a worker is currently helping a customer
		
		System.out.println("Simulating Post Office with 50 customers and 3 postal workers");
		
		//	initialize done ArrayList
		for(int i=0; i < customers; i++)
		{
			done.add(i, new Semaphore(0, true));
		}
		
		//	initializing customer threads
		Customer CustomerThread[] = new Customer[customers];
		Thread Cthreads[] = new Thread[customers];
		
		//	initializing worker threads
		PostalWorker WorkerThread[] = new PostalWorker[workers];
		Thread Wthreads[] = new Thread[workers];
		
		for(int i = 0; i < customers; i++)
		{
			CustomerThread[i] = new Customer(i,
											 done,
											 capacity,
										     servicing,
										     ready,
										     working,
										     FirstMutex,
										     ThirdMutex,
										     request);
			
			Cthreads[i] = new Thread(CustomerThread[i]);
			Cthreads[i].start();
		}
		
		for(int i = 0; i < workers; i++)
		{
			WorkerThread[i] = new PostalWorker(i,
											   done,
											   servicing,
											   ready,
											   working,
											   ThirdMutex,
											   request,
											   scales);
			
			Wthreads[i] = new Thread(WorkerThread[i]);
			Wthreads[i].start();
		}		
		
		// re-integrate customers into original process once they have finished
		for(int i = 0; i < customers; i++)
		{
			try
			{
				Cthreads[i].join();
				System.out.println("Joined customer " + i);
			} catch(InterruptedException e){}
		}
		
		System.exit(0);
	}
	
	//	method to determine how long a thread should sleep based on its current task
	public static int sleep(int task)
	{
		switch(task)
		{			
			case 0: return 1000;	// 	to buy stamps
				
			case 1: return 1500;	// 	to mail a letter
			
			case 2: return 2000;	// 	to mail a package
		}
		
		return 9001;	//	in case a value outside of 0-2 is passed
	}
}