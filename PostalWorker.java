import java.util.concurrent.Semaphore;
import java.util.ArrayList;

public class PostalWorker implements Runnable
{
	private int num;
	private int customer_Number;
	private int task;
	private ArrayList<Semaphore> done;
	private Semaphore servicing;
	private Semaphore ready;
	private Semaphore working;
	private Semaphore ThirdMutex;
	private Semaphore request;
	private Semaphore scales;
	
	PostalWorker(int num,					//	default constructor, requires many variables to be passed
				 ArrayList<Semaphore> done,
				 Semaphore servicing,
				 Semaphore ready,
				 Semaphore working,
				 Semaphore ThirdMutex,
				 Semaphore request,
				 Semaphore scales)
	{
		this.done			= done;
		this.num			= num;
		this.servicing		= servicing;
		this.ready			= ready;
		this.working		= working;
		this.ThirdMutex		= ThirdMutex;
		this.request		= request;
		this.scales			= scales;
		
		System.out.println( "Postal worker " + num + " created." );
	}
	
	public void run()
	{
		while(true)	//	worker will serve customers until program stops
		{
			try	//	finds a customer that can be served
			{
				ready.acquire();
			} catch(InterruptedException e){}
			
			//	grabs global variables and saves them locally
			this.customer_Number = PostOfficeSimulation.customerNumber;
			this.task = PostOfficeSimulation.task;
			PostOfficeSimulation.assignedWorker[this.customer_Number] = this.num;
			
			ThirdMutex.release();
			
			try
			{
				request.acquire();
			} catch(InterruptedException e){}
			
			if(this.task == 2)	//	reserves scales if mailing a package and waits length of task
			{
				try
				{
					scales.acquire();
				} catch(InterruptedException e){}
				
				System.out.println("Scales in use by postal worker " + num);
				
				try
				{
					Thread.sleep(PostOfficeSimulation.sleep(this.task));
				} catch(InterruptedException e){}
				
				System.out.println("Scales released by postal worker " + num);
				scales.release();
			}
			else	//	otherwise just waits the necessary length
			{
				try
				{
					Thread.sleep(PostOfficeSimulation.sleep(this.task));
				} catch(InterruptedException e){}
			}
			
			System.out.println("Postal worker " + num + " finished serving customer " + this.customer_Number);
			
			done.get(this.customer_Number).release();
			
			try
			{
				working.acquire();		//	waits for customer to exit
			} catch(InterruptedException e){}
			
			servicing.release();	//	allows another customer to be helped
		}
	}
}