import java.io.*;

public class Run{
    public static void main(String[] args) throws IOException {
        PL0 pl = new  PL0("src/main/java/Flavii.txt");
        boolean compileRet = pl.compile();
        Interpreter interp = new Interpreter();
        interp.pcodeArray = pl.parser.interp.pcodeArray.clone();
        BufferedReader br = new BufferedReader(new FileReader(PL0.inputFile));
        BufferedWriter bw = new BufferedWriter(new FileWriter(PL0.runtimeFile));
        interp.interpret(br, bw);
    }
}