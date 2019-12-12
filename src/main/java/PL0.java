

import java.io.BufferedWriter;
import java.io.FileWriter;


public class PL0 {


    public static final String pcodeFile = "src/main/java/pcode.txt";
    public static final String tableFile = "src/main/java/table.txt";
    public static final String runtimeFile = "src/main/java/runtime.txt";
    public static final String errFile = "src/main/java/error.txt";
    public static final String inputFile="src/main/java/Flavii.txt";
    public static BufferedWriter pcodeWriter;
    public static BufferedWriter runtimeWriter;
    public static BufferedWriter tableWriter;
    public static BufferedWriter errWriter;

    public Parser parser;

    public PL0(String filepath) {
        Scanner scan = new Scanner(filepath);
        parser = new Parser(scan,
                new SymbolTable(),
                new Interpreter());
    }

    public boolean compile() {
        try {
            pcodeWriter = new BufferedWriter(new FileWriter(pcodeFile));
            tableWriter = new BufferedWriter(new FileWriter(tableFile));
            runtimeWriter = new BufferedWriter(new FileWriter(runtimeFile));
            errWriter = new BufferedWriter(new FileWriter(errFile));
            parser.nextsym();
            parser.parse();
            pcodeWriter.close();
            tableWriter.close();
            runtimeWriter.close();
            errWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("***Ошибка компиляции***");
        } finally {

        }
        return (parser.myErr.errCount == 0);
    }
}
