
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Arrays;

public class Scanner {

    public int lineCnt=0;
    private char curCh = ' ';
    private String line;
    public int lineLength = 0;
    public int chCount = 0;
    private int[] ssym;
    private BufferedReader in;

    public Scanner(String filePath) {
        try {
            in = new BufferedReader(new FileReader(filePath));
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            System.out.println("***Файл не найден***");
        }
        ssym = new int[256];
        Arrays.fill(ssym, Symbol.nul);
        ssym['+'] = Symbol.plus;
        ssym['-'] = Symbol.minus;
        ssym['*'] = Symbol.mul;
        ssym['/'] = Symbol.div;
        ssym['('] = Symbol.lparen;
        ssym[')'] = Symbol.rparen;
        ssym['='] = Symbol.eql;
        ssym[','] = Symbol.comma;
        ssym['.'] = Symbol.peroid;
        ssym[';'] = Symbol.semicolon;
        ssym['%'] = Symbol.mod;

    }

    //Построчное считывание
    void getch() {
        if (chCount == lineLength) {
            try {
                String tmp="";
                while (tmp.equals("")) {
                    tmp=in.readLine().trim()+' ';
                }
                line=tmp;
                lineCnt++;
            } catch (Exception e) {
                // throw new Error("***program imcomplete!***");
                e.printStackTrace();
                System.out.println("***При чтении символов произошла ошибка***");
            }
            lineLength = line.length();
            chCount = 0;
            System.out.println(line);
        }
        curCh = line.charAt(chCount++);
    }

    //Анализ символа
    public Symbol getsym() {
        Symbol sym;
        while (curCh == ' ') {
            getch();
        }
        if ((curCh >= 'a' && curCh <= 'z')||(curCh >= 'A' && curCh <= 'Z')) {
            sym = matchKeywordOrIdentifier();                                     //Ключевое слово или идентификатор
        } else if (curCh >= '0' && curCh <= '9') {
            sym = matchNumber();                                                       //Число
        } else {
            sym = matchOperator();                                                     //Оператор
        }
        return sym;
    }

    private Symbol matchKeywordOrIdentifier() {
        StringBuffer sb = new StringBuffer();
        do{
            sb.append(curCh);
            getch();
        }while((curCh >= 'a' && curCh <= 'z')||(curCh>='A'&&curCh<='Z') || (curCh >= '0' && curCh <= '9'));

        String token = sb.toString();
        int index = Arrays.binarySearch(Symbol.word, token);     //Поиск в ключевых словах
        Symbol sym = null;
        if (index < 0) {
            sym = new Symbol(Symbol.ident);                //Добавление к идентификаторам
            sym.id = token;
        } else {
            sym = new Symbol(Symbol.wsym[index]);         //Запись значения ключевого слова
        }
        return sym;
    }

    private Symbol matchNumber() {
        //Анализ числа
        Symbol sym = new Symbol(Symbol.number);
        do {
            sym.num = 10 * sym.num + curCh - '0';        // Вычисление значения числа
            getch();
        } while (curCh >= '0' && curCh <= '9');        //Пока число незакончится

        return sym;
    }

    private Symbol matchOperator() {
        Symbol sym = null;
        switch (curCh) {
            case ':':
                getch();
                if (curCh == '=') { //Является ли присваиванием
                    sym = new Symbol(Symbol.becomes);
                    getch();
                } else {
                    sym = new Symbol(Symbol.nul);        //Не присваивание
                }
                break;
            case '<':
                getch();
                if (curCh == '=') {
                    sym = new Symbol(Symbol.leq);      //<=
                    getch();
                } else if (curCh == '>') {
                    sym = new Symbol(Symbol.neq);       //<>
                    getch();
                } else {
                    sym = new Symbol(Symbol.lss);        //<
                }
                break;
            case '>':
                getch();
                if (curCh == '=') {
                    sym = new Symbol(Symbol.geq);        //>=
                    getch();
                } else {
                    sym = new Symbol(Symbol.gtr);          //>
                }
                break;
            default:
                sym = new Symbol(ssym[curCh]);
                if (sym.symtype != Symbol.peroid) {
                    getch();
                }
        }
        return sym;
    }
}
