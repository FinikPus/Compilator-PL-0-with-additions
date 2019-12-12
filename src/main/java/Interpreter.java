import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;


public class Interpreter {

    //Размер стека
    private static final int stackSize = 1000;
    //размер массива хранимого кода
    private static final int arraySize = 500;
    //указатель вирт машины[0,arraySize-1]
    public int arrayPtr = 0;
    //массив кода вирт машины
    public Pcode[] pcodeArray;
    //показывать промежуточный код
    public static boolean listswitch = true;

    public Interpreter() {
        pcodeArray = new Pcode[arraySize];
    }


    public void gen(int f, int l, int a) {
        if (arrayPtr >= arraySize) {
            throw new Error("***Программа слишком длинная***");
        }
        pcodeArray[arrayPtr++] = new Pcode(f, l, a);

    }


    public void listcode(int start) {
        if (listswitch) {
            for (int i = start; i < arrayPtr; i++) {
                try {
                    String msg = i + "  " + Pcode.pcode[pcodeArray[i].f] + "  " + pcodeArray[i].l + " " + pcodeArray[i].a;      //lit l,a
                  //  System.out.println(msg);
                    PL0.pcodeWriter.write( msg + '\n');
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("***Список команд содержит ошибку***");
                }

            }
        }
    }


    public void interpret(BufferedReader stdin, BufferedWriter stdout) {
        int[] runtimeStack = new int[stackSize];
        Arrays.fill(runtimeStack, 0);
        System.out.println("***Начало интерпретации***");

        int pc = 0, // pc:счетчик команд，
                bp = 1, //bp:базовый адрес，
                sp = 0; //sp:верхушка стека

        do {
            Pcode index = pcodeArray[pc++];
            //System.out.println(pc + "  " + Pcode.pcode[index.f] + " " + index.l + " " + index.a);
            switch (index.f) {
                case Pcode.LIT:       // положить значение на вершину стека
                    sp++;
                    runtimeStack[sp] = index.a;
                    break;
                case Pcode.OPR:                   // Арифметическая операция
                    switch (index.a) {
                        case 0:             //OPR 0 0;RETURN
                            sp = bp-1;
                            pc = runtimeStack[sp + 3];
                            bp = runtimeStack[sp + 2];
                            break;
                        case 1:                                                           //OPR 0 1 ;NEG
                            runtimeStack[sp] = -runtimeStack[sp];
                            break;
                        case 2:                                                           //OPR 0 2;ADD
                            sp--;
                            runtimeStack[sp] += runtimeStack[sp+1];
                            break;
                        case 3:                                                             //OPR 0 3;SUB
                            sp--;
                            runtimeStack[sp] -= runtimeStack[sp+1];
                            break;
                        case 4:                                                             //OPR 0 4;MUL
                            sp--;
                            runtimeStack[sp] =runtimeStack[sp] * runtimeStack[sp+1];
                            break;
                        case 5:                                                             //OPR 0 5;DIV
                            sp--;
                            runtimeStack[sp] /= runtimeStack[sp+1];
                            break;
                        case 6:                                                              //OPR 0 6;ODD нечетность
                            runtimeStack[sp] %= 2;
                            break;
                        case 35:                                                              //OPR 0 7;MOD
                            sp--;
                            runtimeStack[sp] %= runtimeStack[sp+1];
                            break;

                        case 8:                                                             //OPR 0 8;==равно
                            sp--;
                            runtimeStack[sp] = (runtimeStack[sp] == runtimeStack[sp+1] ? 1 : 0);
                            break;
                        case 9:                                                                //OPR 0 9;!=не равно
                            sp--;
                            runtimeStack[sp] = (runtimeStack[sp] != runtimeStack[sp+1] ? 1 : 0);
                            break;
                        case 10:                                                               //OPR 0 10;<
                            sp--;
                            runtimeStack[sp] = (runtimeStack[sp] < runtimeStack[sp+1] ? 1 : 0);
                            break;
                        case 11:                                                                //OPR 0 11;>=
                            sp--;
                            runtimeStack[sp] = (runtimeStack[sp] >= runtimeStack[sp+1] ? 1 : 0);
                            break;
                        case 12:                                                                //OPG 0 12;>
                            sp--;
                            runtimeStack[sp] = (runtimeStack[sp] > runtimeStack[sp+1] ? 1 : 0);
                            break;
                        case 13:                                                                 //OPG 0 13;<=
                            sp--;
                            runtimeStack[sp] = (runtimeStack[sp] <= runtimeStack[sp+1] ? 1 : 0);
                            break;
                        case 14:                                                                 //OPG 0 14;верхушка стека
                            System.out.println("Вывод: " + runtimeStack[sp] + ' ');
                            try {
                                stdout.write(" " + runtimeStack[sp] + ' ');
                                stdout.flush();
                            } catch (Exception ex) {
                                System.out.println("***Ошибка вывода***");
                            }
                            sp--;
                            break;
                        case 15:                                                                 //OPG 0 15;вывести строку
                            System.out.print("\n");
                            try {
                                stdout.write("\n");
                            } catch (Exception ex) {
                                System.out.println("***Ошибка вывода пустой строки***");
                            }
                            break;
                        case 16:                             //OPG 0 16;ввод строки и помещение на вершину стека
                            System.out.print("Введите целое число : ");
                            runtimeStack[sp] = 0;
                            try {
                                Scanner in = new Scanner(System.in);
                                sp++;
                                runtimeStack[sp] = in.nextInt();
                                //runtimeStack[sp] = Integer.parseInt(stdin.readLine().trim());       //чтение в число
                                System.out.println(runtimeStack[sp]);

                            } catch (Exception e) {
                                e.printStackTrace();
                                System.out.println("***При чтении числа произошла ошибка***");
                            }
                            try {
                                stdout.write(" " + runtimeStack[sp] + '\n');
                                stdout.flush();
                            } catch (Exception ex) {
                                System.out.println("***Ошибка при записи числа в стек***");
                            }
                            break;
                    }
                    break;
                case Pcode.LOD:        //положить значение памяти базового адреса на вершину стека
                    sp++;
                    runtimeStack[sp] = runtimeStack[base(index.l, runtimeStack, bp) + index.a];

                    break;
                case Pcode.STO:           //Положить по базовому адресу значение верхушки стека

                    runtimeStack[base(index.l, runtimeStack, bp) + index.a] = runtimeStack[sp];
                    sp--;
                    break;
                case Pcode.CAL:                  //Вызов подпрограммы
                    runtimeStack[sp+1] = base(index.l, runtimeStack, bp);     //Поместить базовый адрес в стек
                    runtimeStack[sp + 2] = bp;                        //Поместить указатель базового адреса в стек
                    runtimeStack[sp + 3] = pc;                        //Поместить счетчик команд в стек
                    bp = sp+1;                       //Изменение значения указателя базового адреса на смещение
                    pc = index.a;                             //Перейти к адресу
                    break;
                case Pcode.INT:
                    sp += index.a;
                    break;
                case Pcode.JMP:                         //Безусловный переход
                    pc = index.a;
                    break;
                case Pcode.JPC:

                    if (runtimeStack[sp] == 0) //Перейти по условию
                    {
                        pc = index.a;
                    }
                    sp--;
                    break;
            }
        } while (pc != 0);
    }


    private int base(int l, int[] runtimeStack, int b) {
        while (l > 0) {
            b = runtimeStack[b];
            l--;
        }
        return b;
    }

    public void debugPcodeArray() throws IOException {
        System.out.println("***Массив команд***");
        String msg = null;
        for (int i = 0; pcodeArray[i] != null; i++) {
            msg = "" + i + "  " + Pcode.pcode[pcodeArray[i].f] + "  " + pcodeArray[i].l + "  " + pcodeArray[i].a;
            System.out.println(msg);
            PL0.pcodeWriter.write(msg + '\n');
        }
    }

}
