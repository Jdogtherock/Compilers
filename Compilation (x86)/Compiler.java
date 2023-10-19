import java.io.File;
import java.io.IOException;
import java.io.FileWriter;

public class Compiler {
    public static void main(String[] args){
        try {
            String inputfile = args[0];
            File outputfile = new File(args[1]);
            outputfile.createNewFile();
            FileWriter myWriter = new FileWriter(outputfile);
            String output = BaliX86Compiler.compiler(inputfile);
            myWriter.write(output);
            myWriter.close();
            System.out.println("Successfully wrote to the file.");
          } catch (IOException e) {
            System.out.println("An error occurred.");
        }
	}
}