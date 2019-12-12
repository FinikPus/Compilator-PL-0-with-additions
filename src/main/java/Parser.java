
import java.io.IOException;
import java.util.BitSet;

public class Parser {

    private Symbol sym;                    //символ
    private Scanner lex;                   //лексический анализатор
    public SymbolTable table;           //таблица значений
    public Interpreter interp;      //Виртуальная машина
    public Err myErr;               //Ошибки

    private BitSet declbegsys;
    private BitSet statbegsys;
    private BitSet facbegsys;

    private int dx = 0; //Смещение для таблицы символов


    public Parser(Scanner lex, SymbolTable table, Interpreter interp) {
        this.lex = lex;
        this.table = table;
        this.interp = interp;
        this.myErr=new Err();
        /**
         * <Программа> ::= [<Описание констант>][<Описание переменных>]{<Процедуры>}<Операторы>
         * <Описание констант> ::= const<Определение константы>{,<Определение константы>};
         * <Описание переменных>::= var<идентификатор>{,<идентификатор>};
         * <Процедуры> ::= <Заголовок>procedure<идентификатор>; <подпрограмма>;
         * FIRST(declaration)={const var procedure null };
         */
        declbegsys = new BitSet(Symbol.symnum);
        declbegsys.set(Symbol.constsym);
        declbegsys.set(Symbol.varsym);
        declbegsys.set(Symbol.procsym);

        statbegsys = new BitSet(Symbol.symnum);
        statbegsys.set(Symbol.beginsym);
        statbegsys.set(Symbol.callsym);
        statbegsys.set(Symbol.ifsym);
        statbegsys.set(Symbol.whilesym);
        statbegsys.set(Symbol.repeatsym);

        facbegsys = new BitSet(Symbol.symnum);
        facbegsys.set(Symbol.ident);
        facbegsys.set(Symbol.number);
        facbegsys.set(Symbol.lparen);
    }

    //Получение следующей конструкции()
    public void nextsym() {
        sym = lex.getsym();
    }
    //Проверка является ли конструкция допустимой
    void test(BitSet s1, BitSet s2, int errcode) {
        if (!s1.get(sym.symtype)) {
            myErr.report(errcode,lex.lineCnt);
            //если недопустимая конструкция, проверяется дальше, пока не найдет совпадение
            s1.or(s2);        //Объединение s1 и s2
            while (!s1.get(sym.symtype)) {
                nextsym();
            }
        }
    }


    public void parse() {
        BitSet nxtlev = new BitSet(Symbol.symnum);
        nxtlev.or(declbegsys);
        nxtlev.or(statbegsys);
        nxtlev.set(Symbol.peroid);
        //Анализ программы
        block(0, nxtlev);               //Анализ подпрограммы
        if (sym.symtype != Symbol.peroid)         //Если точка
        {
            myErr.report(9,lex.lineCnt);
        }
        //Отображение таблицы переменных
       // table.debugTable(0);
       // try {
            //interp.debugPcodeArray();
       // } catch (IOException ex) {
        //    ex.printStackTrace();
         //   System.out.println("***Ошибка при выводе команд***");
       // }

    }


    public void block(int lev, BitSet fsys) {
        BitSet nxtlev = new BitSet(Symbol.symnum);

        int dx0 = dx,               //Индекс размещения данных
                tx0 = table.tablePtr,   //Начальный индекс таблицы
                cx0;    //Начальный индекс программы
        //Установка смещения на 3, т.к. идет  SL, DL, RA потом DX
        dx = 3;
        //
        table.get(table.tablePtr).addr = interp.arrayPtr;   //Запись текущего положения в вирт машине в таблицу символов
        interp.gen(Pcode.JMP, 0, 0);                             //JMP 0 0
        if (lev > SymbolTable.levMax) //Количество вложенностей
        {
            myErr.report(32,lex.lineCnt);
        }
        //Анализ раздела констант
        do {
            //<Описание констант> ::= const<Определение константы>{,<Определение константы>};
            if (sym.symtype == Symbol.constsym) {  //const a=0,b=0,... ...,z=0;
                nextsym();
                constdeclaration(lev);
                while (sym.symtype == Symbol.comma) {
                    nextsym();
                    constdeclaration(lev);
                }

                if (sym.symtype == Symbol.semicolon) //Точка с запятой конец блока констант
                {
                    nextsym();
                } else {
                    myErr.report(5,lex.lineCnt);   //Ошибка нет точки с запятой
                }
            }

            //Анализ блока объявления переменных
            //var<Идентификатор>{,<Идентификатор>};
            if (sym.symtype == Symbol.varsym) {
                nextsym();
                vardeclaration(lev);
                while (sym.symtype == Symbol.comma) { //пока{,<идентификатор>}
                    nextsym();
                    vardeclaration(lev);
                }
                if (sym.symtype == Symbol.semicolon) //Точка с запятой конец блока объявления
                {
                    nextsym();
                } else {
                    myErr.report(5,lex.lineCnt);      //  Ошибка нет точки с запятой
                }
            }

            //Анализ блока процедур
            // <Раздел описания процедуры> ::  procedure <идентификатор>; <подпрограмма>
            while (sym.symtype == Symbol.procsym) {
                nextsym();
                if (sym.symtype == Symbol.ident) {       //Запись идентификатора в таблицу
                    table.enter(sym, SymbolTable.Item.procedure, lev, dx);
                    nextsym();
                } else {
                    myErr.report(4,lex.lineCnt);
                }
                if (sym.symtype == Symbol.semicolon)    //Точка с запятой конец блока процедур
                {
                    nextsym();
                } else {
                    myErr.report(5,lex.lineCnt);          //Ошибка нет точки с запятой
                }
                nxtlev = (BitSet) fsys.clone();
                //FOLLOW(block)={ ; }
                nxtlev.set(Symbol.semicolon);
                block(lev + 1, nxtlev);             //Уровень вложенности +1

                if (sym.symtype == Symbol.semicolon) {

                    nextsym();
                    //FIRST(statement)={begin call if while repeat null };
                    nxtlev = (BitSet) statbegsys.clone();
                    //FOLLOW(Вложенная подпрограмма)={ ident , procedure }
                    nxtlev.set(Symbol.ident);
                    nxtlev.set(Symbol.procsym);
                    test(nxtlev, fsys, 6);
                    //6:Неверный символ после описания процедуры
                } else {
                    myErr.report(5,lex.lineCnt);                                    //     Ошибка нет точки с запятой
                }
            }

            nxtlev = (BitSet) statbegsys.clone();
            //FIRST(statement)={ ident }
            nxtlev.set(Symbol.ident);
            test(nxtlev, declbegsys, 7);                           //7:Нет объявления
            //FIRST(declaration)={const var procedure null };
        } while (declbegsys.get(sym.symtype));                     //Пока ни один символ не объявлен

        SymbolTable.Item item = table.get(tx0);
        interp.pcodeArray[item.addr].a = interp.arrayPtr;
        item.addr = interp.arrayPtr;
        item.size = dx;

        cx0 = interp.arrayPtr;
        interp.gen(Pcode.INT, 0, dx);
        table.debugTable(tx0);

        nxtlev = (BitSet) fsys.clone();
        nxtlev.set(Symbol.semicolon);
        nxtlev.set(Symbol.endsym);
        statement(nxtlev, lev);

        interp.gen(Pcode.OPR, 0, 0);

        nxtlev = new BitSet(Symbol.symnum);
        test(fsys, nxtlev, 8);

        interp.listcode(cx0);

        dx = dx0;
        table.tablePtr = tx0;
    }



    void constdeclaration(int lev) {
        if (sym.symtype == Symbol.ident) {             //Идентификатор
            String id = sym.id;
            nextsym();
            if (sym.symtype == Symbol.eql || sym.symtype == Symbol.becomes) {     //Присвоить или равно
                if (sym.symtype == Symbol.becomes) {
                    myErr.report(1,lex.lineCnt);      //Присваивание в разделе констант
                }
                nextsym();
                if (sym.symtype == Symbol.number) {
                    sym.id = id;
                    table.enter(sym, SymbolTable.Item.constant, lev, dx);       //Запись в таблицу значений
                    nextsym();
                } else {
                    myErr.report(2,lex.lineCnt);
                }
            } else {
                myErr.report(3,lex.lineCnt);
            }
        } else {
            myErr.report(4,lex.lineCnt);
        }
    }


    void vardeclaration(int lev) {
        if (sym.symtype == Symbol.ident) {
            table.enter(sym, SymbolTable.Item.variable, lev, dx);
            dx++;
            nextsym();
        } else {
            myErr.report(4,lex.lineCnt);
        }
    }


    void statement(BitSet fsys, int lev) {
        // FIRST(statement)={ident,read,write,call,if, while}
        switch (sym.symtype) {
            case Symbol.ident:
                praseAssignStatement(fsys, lev);
                break;
            case Symbol.readsym:
                praseReadStatement(fsys, lev);
                break;
            case Symbol.writesym:
                praseWriteStatement(fsys, lev);
                break;
            case Symbol.callsym:
                praseCallStatement(fsys, lev);
                break;
            case Symbol.ifsym:
                praseIfStatement(fsys, lev);
                break;
            case Symbol.beginsym:
                praseBeginStatement(fsys, lev);
                break;
            case Symbol.whilesym:
                praseWhileStatement(fsys, lev);
                break;
            case Symbol.repeatsym:
                praseRepeatStatement(fsys, lev);
                break;
            default:
                BitSet nxlev = new BitSet(Symbol.symnum);
                test(fsys, nxlev, 19);
                break;
        }
    }


    private void praseRepeatStatement(BitSet fsys, int lev) {
        int cx1 = interp.arrayPtr;
        nextsym();
        BitSet nxtlev = (BitSet) fsys.clone();
        nxtlev.set(Symbol.semicolon);
        nxtlev.set(Symbol.untilsym);
        statement(fsys, lev);

        while (statbegsys.get(sym.symtype) || sym.symtype == Symbol.semicolon) {
            if (sym.symtype == Symbol.semicolon) {
                nextsym();
            } else {
                myErr.report(34,lex.lineCnt);
            }

            statement(nxtlev, lev);
        }
        if (sym.symtype == Symbol.untilsym) {
            nextsym();
            condition(fsys, lev);
            interp.gen(Pcode.JPC, 0, cx1);
        } else {
            //    myErr.report(dx);
        }
    }

    private void praseWhileStatement(BitSet fsys, int lev) {
        int cx1 = interp.arrayPtr;
        nextsym();
        BitSet nxtlev = (BitSet) fsys.clone();
        //FOLLOW(条件)={ do }
        nxtlev.set(Symbol.dosym);
        condition(nxtlev, lev);
        int cx2 = interp.arrayPtr;
        interp.gen(Pcode.JPC, 0, 0);
        if (sym.symtype == Symbol.dosym) {
            nextsym();
        } else {
            myErr.report(18,lex.lineCnt);
        }
        statement(fsys, lev);
        interp.gen(Pcode.JMP, 0, cx1);
        interp.pcodeArray[cx2].a = interp.arrayPtr;
    }


    private void praseBeginStatement(BitSet fsys, int lev) {
        nextsym();
        BitSet nxtlev = (BitSet) fsys.clone();
        //FOLLOW(statement)={ ; end }
        nxtlev.set(Symbol.semicolon);
        nxtlev.set(Symbol.endsym);
        statement(nxtlev, lev);
        while (statbegsys.get(sym.symtype) || sym.symtype == Symbol.semicolon) {
            if (sym.symtype == Symbol.semicolon) {
                nextsym();
            } else {
                myErr.report(10,lex.lineCnt);
            }
            statement(nxtlev, lev);
        }
        if (sym.symtype == Symbol.endsym)
        {
            nextsym();
        } else {
            myErr.report(17,lex.lineCnt);
        }
    }


    private void praseIfStatement(BitSet fsys, int lev) {
        nextsym();
        BitSet nxtlev = (BitSet) fsys.clone();
        nxtlev.set(Symbol.thensym);
        nxtlev.set(Symbol.dosym);
        condition(nxtlev, lev);
        if (sym.symtype == Symbol.thensym) {
            nextsym();
        } else {
            myErr.report(16,lex.lineCnt);
        }
        int cx1 = interp.arrayPtr;
        interp.gen(Pcode.JPC, 0, 0);
        statement(fsys, lev);
        interp.pcodeArray[cx1].a = interp.arrayPtr;

        if (sym.symtype == Symbol.elsesym) {
            interp.pcodeArray[cx1].a++;
            nextsym();
            int tmpPtr = interp.arrayPtr;
            interp.gen(Pcode.JMP, 0, 0);
            statement(fsys, lev);
            interp.pcodeArray[tmpPtr].a = interp.arrayPtr;
        }

    }


    private void praseCallStatement(BitSet fsys, int lev) {
        nextsym();
        if (sym.symtype == Symbol.ident) {
            int index = table.position(sym.id);
            if (index != 0) {
                SymbolTable.Item item = table.get(index);
                if (item.type == SymbolTable.Item.procedure)
                {
                    interp.gen(Pcode.CAL, lev - item.lev, item.addr);
                } else {
                    myErr.report(15,lex.lineCnt);
                }
            } else {
                myErr.report(11,lex.lineCnt);
            }
            nextsym();
        } else {
            myErr.report(14,lex.lineCnt);
        }
    }


    private void praseWriteStatement(BitSet fsys, int lev) {
        nextsym();
        if (sym.symtype == Symbol.lparen) {
            do {
                nextsym();
                BitSet nxtlev = (BitSet) fsys.clone();
                //FOLLOW={ , ')' }
                nxtlev.set(Symbol.rparen);
                nxtlev.set(Symbol.comma);
                expression(nxtlev, lev);
                interp.gen(Pcode.OPR, 0, 14);       //OPR 0 14
            } while (sym.symtype == Symbol.comma);

            if (sym.symtype == Symbol.rparen)
            {
                nextsym();
            } else {
                myErr.report(33,lex.lineCnt);
            }
        } else {
            myErr.report(34,lex.lineCnt);
        }
        interp.gen(Pcode.OPR, 0, 15);
    }


    private void praseReadStatement(BitSet fsys, int lev) {
        nextsym();
        if (sym.symtype == Symbol.lparen) {
            int index = 0;
            do {
                nextsym();
                if (sym.symtype == Symbol.ident) //标识符
                {
                    index = table.position(sym.id);
                }
                if (index == 0) {
                    myErr.report(35,lex.lineCnt);
                } else {
                    SymbolTable.Item item = table.get(index);
                    if (item.type != SymbolTable.Item.variable) {
                        myErr.report(32,lex.lineCnt);
                    } else {
                        interp.gen(Pcode.OPR, 0, 16);    //OPR 0 16
                        interp.gen(Pcode.STO, lev - item.lev, item.addr);   //STO L A
                    }
                }
                nextsym();
            } while (sym.symtype == Symbol.comma);
        } else {
            myErr.report(34,lex.lineCnt);
        }

        if (sym.symtype == Symbol.rparen)
        {
            nextsym();
        } else {
            myErr.report(33,lex.lineCnt);
            while (!fsys.get(sym.symtype)) //sym.symtype!=NULL
            {
                nextsym();
            }
        }
    }


    private void praseAssignStatement(BitSet fsys, int lev) {
        int index = table.position(sym.id);
        if (index > 0) {
            SymbolTable.Item item = table.get(index);
            if (item.type == SymbolTable.Item.variable) {
                nextsym();
                if (sym.symtype == Symbol.becomes) {
                    nextsym();
                } else {
                    myErr.report(13,lex.lineCnt);
                }
                BitSet nxtlev = (BitSet) fsys.clone();
                expression(nxtlev, lev);

                interp.gen(Pcode.STO, lev - item.lev, item.addr);
            } else {
                myErr.report(12,lex.lineCnt);
            }
        } else {
            myErr.report(11,lex.lineCnt);
        }
    }


    private void expression(BitSet fsys, int lev) {
        if (sym.symtype == Symbol.plus || sym.symtype == Symbol.minus) {
            int addOperatorType = sym.symtype;
            nextsym();
            BitSet nxtlev = (BitSet) fsys.clone();
            nxtlev.set(Symbol.plus);
            nxtlev.set(Symbol.minus);
            term(nxtlev, lev);
            if (addOperatorType == Symbol.minus) //OPR 0 1
            {
                interp.gen(Pcode.OPR, 0, 1);
            }
        } else {
            BitSet nxtlev = (BitSet) fsys.clone();
            nxtlev.set(Symbol.plus);
            nxtlev.set(Symbol.minus);
            term(nxtlev, lev);
        }


        while (sym.symtype == Symbol.plus || sym.symtype == Symbol.minus) {
            int addOperatorType = sym.symtype;
            nextsym();
            BitSet nxtlev = (BitSet) fsys.clone();
            //FOLLOW(term)={ +,- }
            nxtlev.set(Symbol.plus);
            nxtlev.set(Symbol.minus);
            term(nxtlev, lev);
            interp.gen(Pcode.OPR, 0, addOperatorType);      //opr 0 2, opr 0 3
        }
    }


    private void term(BitSet fsys, int lev) {
        BitSet nxtlev = (BitSet) fsys.clone();
        //FOLLOW(factor)={ * /}
        nxtlev.set(Symbol.mul);
        nxtlev.set(Symbol.div);
        nxtlev.set(Symbol.mod);
        factor(nxtlev, lev);

        while (sym.symtype == Symbol.mul || sym.symtype == Symbol.div || sym.symtype == Symbol.mod) {
            int mulOperatorType = sym.symtype;
            nextsym();
            factor(nxtlev, lev);
            interp.gen(Pcode.OPR, 0, mulOperatorType);        //OPR 0 4 ,OPR 0 5
        }
    }


    private void factor(BitSet fsys, int lev) {
        test(facbegsys, fsys, 24);

        if (facbegsys.get(sym.symtype)) {
            if (sym.symtype == Symbol.ident) {
                int index = table.position(sym.id);
                if (index > 0) {
                    SymbolTable.Item item = table.get(index);
                    switch (item.type) {
                        case SymbolTable.Item.constant:
                            interp.gen(Pcode.LIT, 0, item.value);
                            break;
                        case SymbolTable.Item.variable:
                            interp.gen(Pcode.LOD, lev - item.lev, item.addr);
                            break;
                        case SymbolTable.Item.procedure:
                            myErr.report(21,lex.lineCnt);
                            break;
                    }
                } else {
                    myErr.report(11,lex.lineCnt);
                }
                nextsym();
            } else if (sym.symtype == Symbol.number) {
                int num = sym.num;
                if (num > SymbolTable.addrMax) {
                    myErr.report(31,lex.lineCnt);
                    num = 0;
                }
                interp.gen(Pcode.LIT, 0, num);
                nextsym();
            } else if (sym.symtype == Symbol.lparen) {
                nextsym();
                BitSet nxtlev = (BitSet) fsys.clone();
                //FOLLOW(expression)={ ) }
                nxtlev.set(Symbol.rparen);
                expression(nxtlev, lev);

                if (sym.symtype == Symbol.rparen)
                {
                    nextsym();
                } else {
                    myErr.report(22,lex.lineCnt);
                }
            } else
            {
                test(fsys, facbegsys, 23);
            }
        }
    }


    private void condition(BitSet fsys, int lev) {
        if (sym.symtype == Symbol.oddsym) {
            nextsym();
            expression(fsys, lev);
            interp.gen(Pcode.OPR, 0, 6);                        //OPR 0 6
        } else {
            BitSet nxtlev = (BitSet) fsys.clone();
            //FOLLOW(expression)={  =  !=  <  <=  >  >= }
            nxtlev.set(Symbol.eql);
            nxtlev.set(Symbol.neq);
            nxtlev.set(Symbol.lss);
            nxtlev.set(Symbol.leq);
            nxtlev.set(Symbol.gtr);
            nxtlev.set(Symbol.geq);
            expression(nxtlev, lev);
            if (sym.symtype == Symbol.eql || sym.symtype == Symbol.neq
                    || sym.symtype == Symbol.lss || sym.symtype == Symbol.leq
                    || sym.symtype == Symbol.gtr || sym.symtype == Symbol.geq) {
                int relationOperatorType = sym.symtype;
                nextsym();
                expression(fsys, lev);
                interp.gen(Pcode.OPR, 0, relationOperatorType);      //symtype=eql... leq7... 13
            } else {
                myErr.report(20,lex.lineCnt);
            }
        }
    }
}
