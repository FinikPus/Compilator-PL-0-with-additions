
public class Symbol {

    //Коды символов
    public static final int nul = 0;                  //NULL
    public static final int ident = 1;               //идентификатор
    public static final int plus = 2;                //+
    public static final int minus = 3;              //-
    public static final int mul = 4;                 //*
    public static final int div = 5;                  // /
    public static final int mod = 35;
    public static final int oddsym = 6;           //odd
    public static final int number = 7;           //число
    public static final int eql = 8;                  //=(equal)
    public static final int neq = 9;                 //<>(not equal)
    public static final int lss = 10;                 //<(less)
    public static final int geq = 11;                 //>=(greater or equal)
    public static final int gtr = 12;                //>(greater)
    public static final int leq = 13;                //<=(less or equal)
    public static final int lparen = 14;            //(
    public static final int rparen = 15;           // )
    public static final int comma = 16;           // ,
    public static final int semicolon = 17;       // ;
    public static final int peroid = 18;            // .
    public static final int becomes = 19;         //  :=
    public static final int beginsym = 20;        // begin
    public static final int endsym = 21;           // end
    public static final int ifsym = 22;             //if
    public static final int thensym = 23;         //then
    public static final int whilesym = 24;        //while
    public static final int writesym = 25;        //write
    public static final int readsym = 26;         //read
    public static final int dosym = 27;            //do
    public static final int callsym = 28;          //call
    public static final int constsym = 29;       //const
    public static final int varsym = 30;           //var
    public static final int procsym = 31;         //procedure
    public static final int elsesym = 32;
    public static final int repeatsym=33;
    public static final int untilsym=34;

    //Общее количество символов
    public static final int symnum = 36;

    //Ключевые слова в алфавитном порядке
    public static final String[] word = new String[]{
            "begin","call" , "const"    , "do" ,
            "else"  ,"end" ,"if"   , "odd",
            "procedure", "read","repeat","then",
            "until" , "var", "while"    , "write" };
    //Символьные значения для ключевых слов
    public static final int[] wsym = new int[]{
            beginsym, callsym, constsym, dosym,
            elsesym, endsym, ifsym,oddsym,
            procsym,readsym,repeatsym, thensym,
            untilsym ,varsym,whilesym, writesym};

    //Тип символа
    public int symtype;
    //Идентификатор；
    public String id;
    //Число
    public int num;

    //Создание символа с определенным типом
    public Symbol(int stype) {
        symtype = stype;
        id = "";
        num = 0;
    }
}
