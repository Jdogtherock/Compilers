import java.util.*;

// Variable -> FBR Offset
// One table per method
public class VarTable {
    Hashtable<String, Integer> vars = new Hashtable<String, Integer>();
	String name;
	int num_args = 0;
	int localsOffset = 1;
	void set_name(String name){
		this.name = name;
	}
    void set(String s, int offset){
        this.vars.put(s, offset);
    }
    int get(String s){
        return this.vars.get(s);
    }
	boolean in(String s){
		return this.vars.containsKey(s);
	}
	int num_args(){
		return this.num_args;
	}
	void add_arg(){
		this.num_args += 1;
	}
	int setLocal(){
		this.localsOffset += 1;
		return this.localsOffset;
	}
	int size(){
		return this.vars.size();
	}
	int numLocals(){
		return this.localsOffset;
	}
}
