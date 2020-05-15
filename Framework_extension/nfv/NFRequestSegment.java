package pt.uminho.algoritmi.netopt.nfv;

public class NFRequestSegment {

	private int requestID;
	private int from;
	private int to;
	private boolean isRequestSource;
	private boolean isRequestDestination;
	
	public NFRequestSegment(int requestID,int s,int d,boolean isSource,boolean isDestination){
		this.setRequestID(requestID);
		this.setFrom(s);
		this.setTo(d);
		this.setRequestSource(isSource);
		this.setRequestDestination(isDestination);
	}
	
	public NFRequestSegment(int rID, int s,int d){
		this(rID,s,d,false,false);
	}

	public int getRequestID() {
		return requestID;
	}

	public void setRequestID(int requestID) {
		this.requestID = requestID;
	}

	public int getFrom() {
		return from;
	}

	public void setFrom(int from) {
		this.from = from;
	}

	public int getTo() {
		return to;
	}

	public void setTo(int to) {
		this.to = to;
	}

	public boolean isRequestSource() {
		return isRequestSource;
	}

	public void setRequestSource(boolean isRequestSource) {
		this.isRequestSource = isRequestSource;
	}

	public boolean isRequestDestination() {
		return isRequestDestination;
	}

	public void setRequestDestination(boolean isRequestDestination) {
		this.isRequestDestination = isRequestDestination;
	}  
	
	public String toString(){
		String s ="Request "+this.getRequestID()+" From "+(this.isRequestSource()?"Node: ":"Service: ")+this.getFrom();
		s +=" To "+(this.isRequestDestination()?"Node: ":"Service: ")+this.getTo();
		return s;
	}
}
