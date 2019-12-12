
import java.io.IOException;

public class SymbolTable {


    public int tablePtr = 0;
    public static final int tableMax = 100;     //Длина таблицы имен
    public static final int symMax = 10;            //Число зарезервированных слов
    public static final int addrMax = 1000000;        //Максимальный адрес
    public static final int levMax = 3;            //Максимальная глубина вложенности
    public static final int numMax = 14;           //Максимальное количество цифр в числах
    public static boolean tableswitch;           //Показывать список имен
    //Список переменных
    public Item[] table = new Item[tableMax];

    public class Item {
        public static final int constant = 0;
        public static final int variable = 1;
        public static final int procedure = 2;
        String name;                                             //Имя
        int type;                                               //Тип，const var or procedur
        int value;                                                 //значение
        int lev;                                                 //какой слой
        int addr;                                                //адрес
        int size;                                               //сколько памяти выделится

        public Item() {
            super();
            this.name = "";
        }

    }


    public Item get(int i) {
        if (table[i] == null) {
            table[i] = new Item();
        }
        return table[i];
    }


    public void enter(Symbol sym, int type, int lev, int dx) {
        tablePtr++;
        Item item = get(tablePtr);
        item.name = sym.id;
        item.type = type;
        switch (type) {
            case Item.constant:                   //константа
                item.value = sym.num;            //заполнение значения
                break;
            case Item.variable:                //Переменная
                item.lev = lev;                  //Слой
                item.addr = dx;                  //относительный адрес
                break;
            case Item.procedure:                   //Процедура
                item.lev = lev;

        }
    }


    public int position(String idt) {
        for (int i = tablePtr; i > 0; i--) //цикл с конца таблицы
        {
            if (get(i).name.equals(idt)) {
                return i;
            }
        }
        return 0;
    }


    void debugTable(int start) {
        if (tableswitch) //флаг показывать таблицу или нет
        {
            return;
        }
       // System.out.println("****Таблица имен****");
        if (start > tablePtr) {
            System.out.println("  NULL");
        }
        for (int i = start + 1; i <= tablePtr; i++) {
            try {
                String msg = "Неизвестный элемент таблицы!";
                switch (table[i].type) {
                    case Item.constant:
                        msg = "   " + i + "  const: " + table[i].name + "  val: " + table[i].value;
                        break;
                    case Item.variable:
                        msg = "    " + i + "  var: " + table[i].name + "  lev: " + table[i].lev + "  addr: " + table[i].addr;
                        break;
                    case Item.procedure:
                        msg = "    " + i + " proc: " + table[i].name + "  lev: " + table[i].lev + "  addr: " + table[i].size;
                        break;
                }
               // System.out.println(msg);
                PL0.tableWriter.write(msg + '\n');
            } catch (IOException ex) {
                ex.printStackTrace();
                System.out.println("**Ошибка при выводе таблицы***");
            }
        }
    }
}
