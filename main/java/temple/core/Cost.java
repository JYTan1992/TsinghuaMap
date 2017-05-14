package temple.core;
public class Cost{
	public   double dis;
	public   double time;
	public  Cost(){
        this.dis=0;
        this.time=0;
    }
	public   Cost(double dis,double time){
        this.dis=dis;
        this.time=time;
    }
	public   void add(Cost c){
        this.dis+=c.dis;
        this.time+=c.time;
    }
	public   double value(int s) {
        switch (s) {
            case 0:return this.dis;
            case 1:return this.time;
        }
        return this.dis;
    }
	public  boolean under(Cost c){
        return dis <= c.dis && time <= c.time;
    }
}
